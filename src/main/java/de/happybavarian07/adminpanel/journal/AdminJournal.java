package de.happybavarian07.adminpanel.journal;

import de.happybavarian07.adminpanel.data.AdminAction;
import de.happybavarian07.adminpanel.data.AdminActionRepository;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.BiConsumer;
import java.util.function.LongSupplier;

/**
 * The admin journal: an append-only table of what admins did (bans, kicks, warnings, op changes, permission changes, gamerules, ...),
 * who did it, to whom and when. Writing happens on a separate thread so a slow database never blocks a menu click.
 * Other code records through {@link #log}, which does nothing while the journal is off.
 */
public final class AdminJournal {
    /** Name stored as the actor when the console or a plugin did the action. */
    public static final String CONSOLE = "CONSOLE";

    private static volatile AdminJournal current;

    private final AdminActionRepository repository;
    private final Executor io;
    private final LongSupplier clock;
    private final BiConsumer<String, Throwable> errorLog;
    private final Function<UUID, String> names;
    private volatile Predicate<String> categoryEnabled = category -> true;

    /**
     * @param repository where the rows are stored
     * @param io         runs the writes, one at a time
     * @param clock      gives the current time in milliseconds
     * @param errorLog   receives failures of a write
     */
    public AdminJournal(AdminActionRepository repository, Executor io, LongSupplier clock, BiConsumer<String, Throwable> errorLog) {
        this(repository, io, clock, errorLog, null);
    }

    /**
     * @param names turns a UUID into a player name, used for actors and targets that were given as a UUID text; {@code null} keeps the text
     */
    public AdminJournal(AdminActionRepository repository, Executor io, LongSupplier clock, BiConsumer<String, Throwable> errorLog,
                        Function<UUID, String> names) {
        this.names = names;
        this.repository = repository;
        this.io = io;
        this.clock = clock;
        this.errorLog = errorLog;
    }

    /**
     * Sets which kinds of actions are recorded. The predicate gets the category of an action (see {@link #category}) and is asked for
     * every action, so a config change takes effect without a restart.
     */
    public void setCategoryFilter(Predicate<String> categoryEnabled) {
        this.categoryEnabled = categoryEnabled == null ? category -> true : categoryEnabled;
    }

    /**
     * The group an action belongs to, which is the name used under {@code Journal.Record} in the config: {@code Bans}, {@code Kicks},
     * {@code Warnings}, {@code Operators}, {@code Whitelist}, {@code Permissions}, {@code Gamerules}, {@code Plugins}, {@code Server}
     * or {@code Other}.
     */
    public static String category(String action) {
        String key = action == null ? "" : action;
        if (key.startsWith("BAN") || key.startsWith("UNBAN")) return "Bans";
        if (key.startsWith("KICK")) return "Kicks";
        if (key.startsWith("WARN")) return "Warnings";
        if (key.equals("OP") || key.equals("DEOP")) return "Operators";
        if (key.startsWith("WHITELIST")) return "Whitelist";
        if (key.startsWith("PERMISSION") || key.startsWith("GROUP") || key.startsWith("USER")) return "Permissions";
        if (key.startsWith("GAMERULE")) return "Gamerules";
        if (key.startsWith("PLUGIN")) return "Plugins";
        if (key.startsWith("SERVER")) return "Server";
        return "Other";
    }

    /** Makes this the journal that {@link #log} writes to; {@code null} turns recording off. */
    public static void install(AdminJournal journal) {
        current = journal;
    }

    /**
     * Records an action if a journal is installed.
     *
     * @param actor   who did it, may be {@code null} for the console
     * @param action  the kind, for example {@code BAN} or {@code OP}
     * @param target  who or what it was done to, may be empty
     * @param details extra text such as the reason or the old and new value, may be empty
     */
    public static void log(CommandSender actor, String action, String target, String details) {
        AdminJournal journal = current;
        if (journal != null) journal.record(actor, action, target, details);
    }

    /** Same as {@link #log} with a text for the actor, for code that has no sender. */
    public static void log(String actor, String action, String target, String details) {
        AdminJournal journal = current;
        if (journal != null) journal.record(actor, "", action, target, details);
    }

    public void record(CommandSender actor, String action, String target, String details) {
        if (actor instanceof Player player) record(player.getName(), player.getUniqueId().toString(), action, target, details);
        else record(actor == null ? CONSOLE : actor.getName(), "", action, target, details);
    }

    public void record(String actor, String actorUuid, String action, String target, String details) {
        if (!categoryEnabled.test(category(action))) return;
        long now = clock.getAsLong();
        io.execute(() -> {
            try {
                String actorName = actor;
                String actorId = actorUuid == null ? "" : actorUuid;
                UUID actorAsUuid = asUuid(actor);
                if (actorAsUuid != null) {
                    actorId = actor;
                    actorName = nameOf(actorAsUuid, actor);
                }
                String targetText = target == null ? "" : target;
                UUID targetAsUuid = asUuid(targetText);
                if (targetAsUuid != null) targetText = nameOf(targetAsUuid, targetText);
                repository.save(new AdminAction(now, actorName, actorId, action, targetText, details == null ? "" : details));
            } catch (RuntimeException e) {
                errorLog.accept("Could not write the admin journal entry " + action, e);
            }
        });
    }

    private String nameOf(UUID id, String fallback) {
        if (names == null) return fallback;
        try {
            String name = names.apply(id);
            return name == null || name.isBlank() ? fallback : name;
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    static UUID asUuid(String text) {
        if (text == null || text.length() != 36) return null;
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * @param page zero based page number
     * @param size entries per page
     * @return the entries of the page, newest first
     */
    public List<AdminAction> page(int page, int size) {
        return repository.query().orderByDesc("createdAt").orderByDesc("id").limit(size).offset(Math.max(0, page) * size).findAll();
    }

    /**
     * @param filter which entries to keep
     * @param limit  how many matching entries to return at most
     * @return the newest entries that match, newest first; reads the table page by page until enough were found
     */
    public List<AdminAction> recent(Predicate<AdminAction> filter, int limit) {
        List<AdminAction> found = new java.util.ArrayList<>();
        for (int page = 0; found.size() < limit; page++) {
            List<AdminAction> rows = page(page, 500);
            for (AdminAction row : rows) {
                if (filter.test(row)) found.add(row);
                if (found.size() >= limit) break;
            }
            if (rows.size() < 500) break;
        }
        return found;
    }

    /** @return all entries, newest first; use it for exports, not for menus */
    public List<AdminAction> all() {
        return repository.query().orderByDesc("createdAt").orderByDesc("id").findAll();
    }

    public long count() {
        return repository.count();
    }

    /**
     * Deletes entries older than a number of days.
     *
     * @param days entries older than this are removed; 0 or less keeps everything
     * @return how many entries were removed
     */
    public long purgeOlderThanDays(int days) {
        if (days <= 0) return 0;
        long before = clock.getAsLong() - days * 86_400_000L;
        long count = repository.countByCreatedAtLessThan(before);
        if (count > 0) repository.deleteByCreatedAtLessThan(before);
        return count;
    }

    /**
     * Writes every entry as CSV with a header line, oldest first.
     *
     * @return how many entries were written
     */
    public int export(Path file) throws IOException {
        List<AdminAction> rows = all();
        try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            out.write("id,time,actor,actor_uuid,action,target,details");
            out.newLine();
            for (int i = rows.size() - 1; i >= 0; i--) {
                out.write(csvLine(rows.get(i)));
                out.newLine();
            }
        }
        return rows.size();
    }

    static String csvLine(AdminAction row) {
        return row.getId() + "," + Instant.ofEpochMilli(row.getCreatedAt()) + "," + csv(row.getActor()) + "," + csv(row.getActorUuid()) + ","
                + csv(row.getAction()) + "," + csv(row.getTarget()) + "," + csv(row.getDetails());
    }

    /** Quotes a CSV field when it contains a comma, a quote or a line break, and guards against spreadsheet formulas. */
    static String csv(String value) {
        String text = value == null ? "" : value;
        if (!text.isEmpty() && "=+-@".indexOf(text.charAt(0)) >= 0) text = "'" + text;
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
