package de.happybavarian07.adminpanel.listeners;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.managers.PermissionsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.Map;

/*
 * @Author HappyBavarian07
 * @Date September 10, 2024 | 15:54
 */
public class PlayerEventHandler implements Listener {
    private PermissionsManager permissionsManager;

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
        Player player = event.getPlayer();
        if (permissionsManager.getPlayerPermissionsAttachments().containsKey(player.getUniqueId())) {
            player.removeAttachment(permissionsManager.getPlayerPermissionsAttachments().get(player.getUniqueId()));
        }
    }

    /**
     * Adds the Permissions of the Player when he joins the Server
     *
     * @param event The PlayerJoinEvent
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PermissionAttachment attachment = player.addAttachment(AdminPanelMain.getPlugin());
        if (permissionsManager.getPlayerPermissionsAttachments().containsKey(player.getUniqueId())) {
            if (!permissionsManager.getPlayerPermissions().containsKey(player.getUniqueId())) {
                permissionsManager.getPlayerPermissions().put(player.getUniqueId(), new HashMap<>());
            }
            Map<String, Boolean> permissions = permissionsManager.getPlayerPermissions().get(player.getUniqueId());
            for (Map.Entry<String, Boolean> perms : permissions.entrySet()) {
                attachment.setPermission(perms.getKey(), perms.getValue());
            }
            permissionsManager.getPlayerPermissionsAttachments().put(player.getUniqueId(), attachment);
            player.recalculatePermissions();
        }
    }
}
