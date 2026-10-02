package de.happybavarian07.adminpanel.trolls;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A reversible prank that can be started on a player from the troll menu. A troll never changes blocks or items,
 * and everything it starts over time ends in {@link #stop(Player)}.
 */
public interface Troll {
    String PERMISSION_PREFIX = "AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.";

    /** What a click on the troll does. */
    enum Kind {
        /** Switches between started and stopped. */
        TOGGLE,
        /** Plays once; may keep a short task running that {@link #stop(Player)} cancels. */
        ONE_SHOT
    }

    /**
     * @return the name used in the config ({@code Pman.Actions.Trolls.<Id>}), the permission and the language file
     */
    String id();

    /** @return the permission needed to use this troll */
    default String permission() {
        return PERMISSION_PREFIX + id();
    }

    Kind kind();

    /**
     * @return the item below {@code PlayerManager.TrollMenu} in the language file; toggles have a {@code true} and a
     * {@code false} item below it
     */
    default String itemKey() {
        return "More." + id();
    }

    /**
     * @param target the player the troll may be running on
     * @return {@code true} while the troll runs on the target, for a one shot only while it still has a pending task
     */
    default boolean isActive(UUID target) {
        return false;
    }

    /**
     * @param staff  the player who started the troll, {@code null} when started by the console
     * @param target the player to troll
     */
    void start(Player staff, Player target);

    /**
     * Ends everything this troll started on the target. Must be safe to call when nothing runs.
     *
     * @param target the player the troll runs on, may already be on the way out of the server
     */
    default void stop(Player target) {
    }
}
