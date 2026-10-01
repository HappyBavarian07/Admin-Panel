package de.happybavarian07.adminpanel.warnings;

import java.util.UUID;

/**
 * Carries out the action of an escalation rule against a player.
 */
public interface EscalationExecutor {
    /**
     * Runs the rule. Called from the io thread of the warning manager, so an implementation that touches the
     * Bukkit API has to switch to the server thread itself.
     *
     * @param rule   the rule that matched the player's active warning count
     * @param player the warned player
     */
    void run(EscalationRule rule, UUID player);
}
