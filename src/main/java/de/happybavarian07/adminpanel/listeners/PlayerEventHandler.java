package de.happybavarian07.adminpanel.listeners;

import de.happybavarian07.adminpanel.permissions.PermissionsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/*
 * @Author HappyBavarian07
 * @Date September 10, 2024 | 15:54
 */
public class PlayerEventHandler implements Listener {
    private final PermissionsManager permissionsManager;

    public PlayerEventHandler(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }

    /** Reads the entries of the player from the database on the login thread, so the join does not wait for it. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (permissionsManager.getService().isReady()) permissionsManager.getService().data().userNodes(event.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        permissionsManager.unloadPlayerPermissions(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        permissionsManager.loadPlayerPermissions(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        permissionsManager.getService().onWorldChange(event.getPlayer());
    }
}
