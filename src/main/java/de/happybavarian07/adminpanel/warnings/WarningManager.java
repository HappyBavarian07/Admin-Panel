package de.happybavarian07.adminpanel.warnings;

import de.happybavarian07.adminpanel.journal.AdminJournal;
import de.happybavarian07.adminpanel.data.PlayerWarning;
import de.happybavarian07.adminpanel.data.PlayerWarningRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.BiConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Warnings of players, stored in the {@code player_warnings} table with a per-player cache that is loaded on first
 * use.
 *
 * <p>Methods that change data run their database access on the {@code io} executor and complete their future on the
 * {@code main} executor, so the callbacks of the returned futures run on the main thread. Reads answer from the
 * cache; a player whose warnings are not cached yet is loaded on the calling thread, call {@link #load(UUID)} first
 * to avoid that. Warnings are never deleted, only moved to the status {@code EXPIRED} or {@code REMOVED}. Entries
 * with the status {@code RECORD} are history only: they are listed with the warnings but are never active.</p>
 */
public final class WarningManager {
    private static final int MAX_REASON_LENGTH = 255;

    private final PlayerWarningRepository repository;
    private final Supplier<WarningSettings> settings;
    private final EscalationExecutor escalation;
    private final Executor io;
    private final Executor main;
    private final LongSupplier clock;
    private final BiConsumer<String, Throwable> errorLog;
    private final Map<UUID, List<PlayerWarning>> cache = new ConcurrentHashMap<>();

    /**
     * @param repository the warning table
     * @param settings   supplies the current {@code Warnings} config, read on every use so config reloads apply
     * @param escalation carries out escalation rules
     * @param io         runs database access, should execute tasks one after the other
     * @param main       runs the completion of the returned futures
     * @param clock      the current time in epoch milliseconds
     * @param errorLog   receives failures of escalation rules and of the expiry sweep
     */
    public WarningManager(PlayerWarningRepository repository, Supplier<WarningSettings> settings,
                          EscalationExecutor escalation, Executor io, Executor main, LongSupplier clock,
                          BiConsumer<String, Throwable> errorLog) {
        this.repository = repository;
        this.settings = settings;
        this.escalation = escalation;
        this.io = io;
        this.main = main;
        this.clock = clock;
        this.errorLog = errorLog;
    }

    /**
     * Warns a player with the default expiry from the config and runs the escalation rules.
     *
     * @param issuerOrNull the issuing player, {@code null} for the console
     * @return the new warning once it is stored
     */
    public CompletableFuture<PlayerWarning> add(UUID player, String reason, UUID issuerOrNull) {
        return add(player, reason, issuerOrNull, settings.get().defaultExpiryMillis());
    }

    /**
     * Warns a player and runs every escalation rule whose count equals the number of active warnings afterwards.
     *
     * @param durationMillis how long the warning lasts, {@code 0} if it never expires
     * @param issuerOrNull   the issuing player, {@code null} for the console
     * @return the new warning once it is stored
     */
    public CompletableFuture<PlayerWarning> add(UUID player, String reason, UUID issuerOrNull, long durationMillis) {
        return onIo(() -> {
            List<PlayerWarning> list = cached(player);
            long now = clock.getAsLong();
            PlayerWarning warning = new PlayerWarning(player.toString(), clip(reason), actor(issuerOrNull), now,
                    durationMillis > 0 ? now + durationMillis : 0);
            repository.save(warning);
            list.add(warning);
            AdminJournal.log(actor(issuerOrNull), "WARN", player.toString(), clip(reason));
            escalate(player, countActive(list, now));
            return warning;
        });
    }

    /**
     * Writes a history entry, for example for a ban or a kick, without counting it as an active warning and without
     * running any escalation rule.
     *
     * @param player the player the entry is about
     * @param reason the text of the entry
     * @param issuer who caused it: a UUID text, {@link PlayerWarning#CONSOLE} or {@link PlayerWarning#SYSTEM}
     * @return the new entry once it is stored
     */
    public CompletableFuture<PlayerWarning> record(UUID player, String reason, String issuer) {
        return onIo(() -> {
            List<PlayerWarning> list = cached(player);
            PlayerWarning entry = new PlayerWarning(player.toString(), clip(reason), issuer, clock.getAsLong(), 0,
                    WarningStatus.RECORD, 0, "");
            repository.save(entry);
            list.add(entry);
            return entry;
        });
    }

    /**
     * Changes the reason of a warning that has not been removed.
     *
     * @param editor who edits, reserved for the audit trail
     * @return {@code false} if the warning does not exist or was removed
     */
    public CompletableFuture<Boolean> editReason(long id, String reason, UUID editor) {
        return onIo(() -> {
            PlayerWarning warning = locate(id);
            if (warning == null || warning.getStatus() == WarningStatus.REMOVED) return false;
            warning.setReason(clip(reason));
            repository.save(warning);
            AdminJournal.log(actor(editor), "WARN_EDIT", warning.getPlayerUuid(), "#" + id + ": " + clip(reason));
            return true;
        });
    }

    /**
     * Ends an active warning now.
     *
     * @return {@code false} if the warning does not exist or is not active
     */
    public CompletableFuture<Boolean> expireNow(long id, UUID by) {
        return onIo(() -> {
            PlayerWarning warning = locate(id);
            if (warning == null || warning.getStatus() != WarningStatus.ACTIVE) return false;
            warning.end(WarningStatus.EXPIRED, clock.getAsLong(), actor(by));
            repository.save(warning);
            AdminJournal.log(actor(by), "WARN_END", warning.getPlayerUuid(), "#" + id);
            return true;
        });
    }

    /**
     * Ends every active warning of a player now.
     *
     * @return how many warnings were ended
     */
    public CompletableFuture<Integer> expireAll(UUID player, UUID by) {
        return onIo(() -> {
            int ended = 0;
            long now = clock.getAsLong();
            for (PlayerWarning warning : cached(player)) {
                if (warning.getStatus() != WarningStatus.ACTIVE) continue;
                warning.end(WarningStatus.EXPIRED, now, actor(by));
                repository.save(warning);
                ended++;
            }
            if (ended > 0) AdminJournal.log(actor(by), "WARN_END_ALL", player.toString(), ended + " warnings");
            return ended;
        });
    }

    /**
     * Marks a warning as removed. It stays in the table.
     *
     * @return {@code false} if the warning does not exist or is already removed
     */
    public CompletableFuture<Boolean> remove(long id, UUID by) {
        return onIo(() -> {
            PlayerWarning warning = locate(id);
            if (warning == null || warning.getStatus() == WarningStatus.REMOVED) return false;
            warning.end(WarningStatus.REMOVED, clock.getAsLong(), actor(by));
            repository.save(warning);
            AdminJournal.log(actor(by), "WARN_REMOVE", warning.getPlayerUuid(), "#" + id);
            return true;
        });
    }

    public CompletableFuture<List<PlayerWarning>> load(UUID player) {
        return onIo(() -> List.copyOf(cached(player)));
    }

    public List<PlayerWarning> all(UUID player) {
        return List.copyOf(cached(player));
    }

    public List<PlayerWarning> active(UUID player) {
        long now = clock.getAsLong();
        return cached(player).stream().filter(warning -> warning.isActiveAt(now)).toList();
    }

    /**
     * The entries that matter now: the active warnings and the history records.
     */
    public List<PlayerWarning> current(UUID player) {
        long now = clock.getAsLong();
        return cached(player).stream()
                .filter(warning -> warning.isActiveAt(now) || warning.getStatus() == WarningStatus.RECORD).toList();
    }

    public int activeCount(UUID player) {
        return countActive(cached(player), clock.getAsLong());
    }

    /**
     * The number of a warning as players see it: its position among all of the player's warnings, oldest first,
     * counting ended ones as well.
     *
     * @param warning a warning of a player whose warnings are loaded
     * @return the number starting at 1, or 0 if the warning is not in the cache
     */
    public int number(PlayerWarning warning) {
        List<PlayerWarning> all = cached(warning.playerId());
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == warning.getId()) return i + 1;
        }
        return 0;
    }

    /**
     * The warning behind a number from {@link #number(PlayerWarning)}.
     *
     * @param player the warned player, whose warnings must be loaded
     * @param number the number starting at 1
     * @return the warning, or {@code null} if there is none with that number
     */
    public PlayerWarning byNumber(UUID player, int number) {
        List<PlayerWarning> all = cached(player);
        return number >= 1 && number <= all.size() ? all.get(number - 1) : null;
    }

    public long defaultExpiryMillis() {
        return settings.get().defaultExpiryMillis();
    }

    public void sweep() {
        try {
            io.execute(() -> {
                try {
                    sweepNow();
                } catch (RuntimeException e) {
                    errorLog.accept("Could not expire old warnings", e);
                }
            });
        } catch (RejectedExecutionException ignored) {
        }
    }

    /**
     * Marks every active warning whose expiry time has passed as {@code EXPIRED}.
     *
     * @return how many warnings were expired
     */
    public int sweepNow() {
        long now = clock.getAsLong();
        int expired = 0;
        for (PlayerWarning stored : repository.findByStatusAndExpiresAtBetween(WarningStatus.ACTIVE.name(), 1L, now)) {
            PlayerWarning warning = cachedInstance(stored.getId());
            if (warning == null) warning = stored;
            warning.end(WarningStatus.EXPIRED, stored.getExpiresAt(), PlayerWarning.SYSTEM);
            repository.save(warning);
            expired++;
        }
        return expired;
    }

    private void escalate(UUID player, int activeCount) {
        try {
            for (EscalationRule rule : settings.get().rulesFor(activeCount)) {
                try {
                    escalation.run(rule, player);
                } catch (RuntimeException e) {
                    errorLog.accept("Escalation rule " + rule + " failed for " + player, e);
                }
            }
        } catch (RuntimeException e) {
            errorLog.accept("Could not read the escalation rules", e);
        }
    }

    private List<PlayerWarning> cached(UUID player) {
        return cache.computeIfAbsent(player, id -> {
            List<PlayerWarning> loaded = new ArrayList<>(repository.findByPlayerUuid(id.toString()));
            loaded.sort(Comparator.comparingLong(PlayerWarning::getCreatedAt).thenComparingLong(PlayerWarning::getId));
            return new CopyOnWriteArrayList<>(loaded);
        });
    }

    private PlayerWarning cachedInstance(long id) {
        for (List<PlayerWarning> list : cache.values()) {
            for (PlayerWarning warning : list) {
                if (warning.getId() == id) return warning;
            }
        }
        return null;
    }

    private PlayerWarning locate(long id) {
        PlayerWarning warning = cachedInstance(id);
        return warning != null ? warning : repository.findById(id).orElse(null);
    }

    private static int countActive(List<PlayerWarning> warnings, long now) {
        int count = 0;
        for (PlayerWarning warning : warnings) {
            if (warning.isActiveAt(now)) count++;
        }
        return count;
    }

    private static String actor(UUID uuidOrNull) {
        return uuidOrNull == null ? PlayerWarning.CONSOLE : uuidOrNull.toString();
    }

    private static String clip(String reason) {
        return reason.length() <= MAX_REASON_LENGTH ? reason : reason.substring(0, MAX_REASON_LENGTH);
    }

    private <T> CompletableFuture<T> onIo(Supplier<T> work) {
        CompletableFuture<T> result = new CompletableFuture<>();
        io.execute(() -> {
            try {
                T value = work.get();
                main.execute(() -> result.complete(value));
            } catch (Throwable failure) {
                main.execute(() -> result.completeExceptionally(failure));
            }
        });
        return result;
    }
}
