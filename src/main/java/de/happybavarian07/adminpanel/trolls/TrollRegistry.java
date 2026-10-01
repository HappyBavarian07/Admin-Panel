package de.happybavarian07.adminpanel.trolls;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Holds the {@link Troll}s, starts and stops them and cleans up after a target. A troll that is switched off in the
 * config is not listed and cannot be started, but can still be stopped.
 */
public final class TrollRegistry {
    /** Outcome of {@link #start}, {@link #stop} and {@link #toggle}. */
    public enum Result {
        STARTED, STOPPED, UNKNOWN, DISABLED, ALREADY_ACTIVE, NOT_ACTIVE, CANCELLED
    }

    /** Lets other code, for example an event, veto a start or stop that a staff member asked for. */
    @FunctionalInterface
    public interface Gate {
        boolean allow(Troll troll, Player staff, Player target, boolean starting);
    }

    private final Map<String, Troll> trolls = new LinkedHashMap<>();
    private final Predicate<String> enabled;
    private final Gate gate;
    private final Supplier<? extends Collection<? extends Player>> online;
    private final BiConsumer<String, Throwable> errorLog;

    /**
     * @param enabled  tells if the troll with this id is switched on in the config
     * @param gate     asked before a staff member starts or stops a troll
     * @param online   the players currently on the server, used when everything is stopped
     * @param errorLog receives failures of a single troll while stopping, so the others are still stopped
     */
    public TrollRegistry(Predicate<String> enabled, Gate gate, Supplier<? extends Collection<? extends Player>> online,
                         BiConsumer<String, Throwable> errorLog) {
        this.enabled = enabled;
        this.gate = gate;
        this.online = online;
        this.errorLog = errorLog;
    }

    /** @throws IllegalArgumentException if a troll with the same id is already registered */
    public void register(Troll troll) {
        if (trolls.putIfAbsent(troll.id().toLowerCase(), troll) != null) {
            throw new IllegalArgumentException("Troll " + troll.id() + " is already registered");
        }
    }

    public Optional<Troll> find(String id) {
        return Optional.ofNullable(trolls.get(id.toLowerCase()));
    }

    /** @return every registered troll in registration order */
    public List<Troll> all() {
        return new ArrayList<>(trolls.values());
    }

    /** @return the registered trolls that are switched on in the config */
    public List<Troll> enabled() {
        List<Troll> result = new ArrayList<>();
        for (Troll troll : trolls.values()) {
            if (enabled.test(troll.id())) result.add(troll);
        }
        return result;
    }

    /** @return the trolls that currently run on the target */
    public List<Troll> active(UUID target) {
        List<Troll> result = new ArrayList<>();
        for (Troll troll : trolls.values()) {
            if (troll.isActive(target)) result.add(troll);
        }
        return result;
    }

    /**
     * @param staff the player who asks for it, {@code null} for the console
     * @return {@code STARTED}, or why nothing started
     */
    public Result start(String id, Player staff, Player target) {
        Troll troll = trolls.get(id.toLowerCase());
        if (troll == null) return Result.UNKNOWN;
        if (!enabled.test(troll.id())) return Result.DISABLED;
        if (troll.kind() == Troll.Kind.TOGGLE && troll.isActive(target.getUniqueId())) return Result.ALREADY_ACTIVE;
        if (!gate.allow(troll, staff, target, true)) return Result.CANCELLED;
        troll.start(staff, target);
        return Result.STARTED;
    }

    /**
     * @param staff the player who asks for it, {@code null} for the console
     * @return {@code STOPPED}, or why nothing stopped
     */
    public Result stop(String id, Player staff, Player target) {
        Troll troll = trolls.get(id.toLowerCase());
        if (troll == null) return Result.UNKNOWN;
        if (!troll.isActive(target.getUniqueId())) return Result.NOT_ACTIVE;
        if (!gate.allow(troll, staff, target, false)) return Result.CANCELLED;
        troll.stop(target);
        return Result.STOPPED;
    }

    /** Stops a toggle that runs, starts everything else. */
    public Result toggle(String id, Player staff, Player target) {
        Troll troll = trolls.get(id.toLowerCase());
        if (troll == null) return Result.UNKNOWN;
        if (troll.kind() == Troll.Kind.TOGGLE && troll.isActive(target.getUniqueId())) return stop(id, staff, target);
        return start(id, staff, target);
    }

    /**
     * Stops every troll that runs on the target. Nothing can veto this, it is also used when the target quits.
     *
     * @return how many trolls were stopped
     */
    public int stopAll(Player target) {
        int stopped = 0;
        for (Troll troll : trolls.values()) {
            if (!troll.isActive(target.getUniqueId())) continue;
            try {
                troll.stop(target);
                stopped++;
            } catch (RuntimeException e) {
                errorLog.accept("The troll " + troll.id() + " could not be stopped", e);
            }
        }
        return stopped;
    }

    /** Stops every troll on every online player; called when the plugin is disabled. */
    public int stopEverything() {
        int stopped = 0;
        for (Player player : new ArrayList<>(online.get())) stopped += stopAll(player);
        return stopped;
    }
}
