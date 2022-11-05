package de.happybavarian07.adminpanel.bungee;/*
 * @Author HappyBavarian07
 * @Date 16.09.2022 | 18:50
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.Serialization;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DataClientUtils {
    // Send Permissions, etc.
    private final NewDataClient dataClient;
    private final AdminPanelMain plugin;

    public DataClientUtils(NewDataClient dataClient) {
        this.dataClient = dataClient;
        this.plugin = AdminPanelMain.getPlugin();
    }

    public void sendPlayerPermissions(UUID player, Map<String, Boolean> permissions, String destination) {
        String permissionsString = Serialization.serialize(new PermissionsMap(permissions));
        Message message = new Message(dataClient.getClientName(), destination, Action.SENDPERMISSIONS, player.toString(), permissionsString);
        System.out.println("Message: " + message);
        dataClient.send(message.toStringArray());
    }

    protected void applyReceivedPermissions(UUID player, String permissionsString) {
        Map<String, Boolean> permissions = new HashMap<>(((PermissionsMap) Serialization.deserialize(permissionsString, PermissionsMap.class)).getPermissions());
        System.out.println("Data from Server: " + permissionsString);
        System.out.println("Deserialized Data: " + Serialization.deserialize(permissionsString, PermissionsMap.class));
        // TODO Option hinzufügen, wenn deaktivert die Keys einfach überschreiben, wenn aktiviert nur wenn sie nicht existieren

        //plugin.getPlayerPermissions().get(player).clear();
        for(String keys : permissions.keySet()) {
            plugin.getPlayerPermissions().get(player).put(keys, permissions.get(keys));
            if(Bukkit.getPlayer(player) != null && Objects.requireNonNull(Bukkit.getPlayer(player)).isOnline()) {
                plugin.getPlayerPermissionsAttachments().get(player).setPermission(keys, permissions.get(keys));
            }
        }
        plugin.savePerms();
        if(Bukkit.getPlayer(player) != null && Objects.requireNonNull(Bukkit.getPlayer(player)).isOnline()) {
            plugin.reloadPerms(Bukkit.getPlayer(player));
        }
    }
}
