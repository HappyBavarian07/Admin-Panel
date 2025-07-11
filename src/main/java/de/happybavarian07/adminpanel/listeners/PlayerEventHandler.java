package de.happybavarian07.adminpanel.listeners;

import de.happybavarian07.adminpanel.permissions.PermissionsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

    /**
     * Removes the Permissions of the Player when he quits the Server
     *
     * @param event The PlayerQuitEvent
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        permissionsManager.unloadPlayerPermissions(event.getPlayer());
    }

    /**
     * Adds the Permissions of the Player when he joins the Server
     *
     * @param event The PlayerJoinEvent
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        permissionsManager.loadPlayerPermissions(event.getPlayer());
        /*Player player = event.getPlayer();
        PermissionAttachment attachment = player.addAttachment(AdminPanelMain.getPlugin());
        if (permissionsManager.getPlayerPermissionsAttachments().containsKey(player.getUniqueId())) {
            Map<String, Boolean> permissions = permissionsManager.getPlayerPermissions(player.getUniqueId());
            for (Map.Entry<String, Boolean> perms : permissions.entrySet()) {
                attachment.setPermission(perms.getKey(), perms.getValue());
            }
            permissionsManager.getPlayerPermissionsAttachments().put(player.getUniqueId(), attachment);
            player.recalculatePermissions();
        }*/
    }
}
