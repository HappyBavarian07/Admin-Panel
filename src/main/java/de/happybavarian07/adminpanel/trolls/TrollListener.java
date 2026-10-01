package de.happybavarian07.adminpanel.trolls;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/** Stops every troll of a player who leaves the server. */
public final class TrollListener implements Listener {
    private final TrollRegistry registry;

    public TrollListener(TrollRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        registry.stopAll(event.getPlayer());
    }
}
