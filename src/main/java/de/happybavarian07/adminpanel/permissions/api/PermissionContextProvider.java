package de.happybavarian07.adminpanel.permissions.api;

import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Adds a kind of context to the permission manager, for example the game mode or a region. An entry with the context
 * {@code gamemode=creative} only applies while some provider reports {@code gamemode -> creative} for the player.
 * Register an implementation with {@code PermissionService#registerContextProvider}.
 */
@FunctionalInterface
public interface PermissionContextProvider {
    /**
     * @param player   the player the permissions are worked out for
     * @param contexts the current context, add your keys here; keys are case sensitive, values are compared ignoring case
     */
    void contribute(Player player, Map<String, String> contexts);
}
