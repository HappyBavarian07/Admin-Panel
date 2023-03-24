package de.happybavarian07.adminpanel.bungee;/*
 * @Author HappyBavarian07
 * @Date 16.09.2022 | 18:50
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.Result;
import de.happybavarian07.adminpanel.utils.Serialization;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.IOException;
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

    public Result sendPlayerPermissions(UUID player, Map<String, Boolean> permissions, String destination) {
        if(permissions.isEmpty()) return Result.EMPTYMAP;
        String permissionsString = Serialization.serialize(new PermissionsMap(permissions));
        Message message = new Message(dataClient.getClientName(), destination, Action.SENDPERMISSIONS, player.toString(), permissionsString);
        if (dataClient.getSettings().isDebugEnabled()) {
            dataClient.getPluginLogger().dataClientMessage(ChatColor.BLUE, false, true,
                    "Message: " + message,
                    "UUID:" + player,
                    "Permissions: " + permissions,
                    "Destination: " + destination);
        }
        try {
            return dataClient.send(message, true, false);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.ERROR;
        }
    }

    protected Result applyReceivedPermissions(UUID player, String permissionsString) {
        Map<String, Boolean> permissions = new HashMap<>(((PermissionsMap) Serialization.deserialize(permissionsString, PermissionsMap.class)).getPermissions());
        if (dataClient.getSettings().isDebugEnabled()) {
            dataClient.getPluginLogger().dataClientMessage(ChatColor.BLUE, false, true,
                    "UUID: " + player,
                    "Data from Server: " + permissionsString,
                    "Deserialized Data: " + Serialization.deserialize(permissionsString, PermissionsMap.class),
                    "Data from Server == Deserialized Data? " + (permissionsString == Serialization.deserialize(permissionsString, PermissionsMap.class)));
        }
        if(permissions.isEmpty()) return Result.EMPTYMAP;
        if (dataClient.getSettings().isOverwritePermissionsEnabled())
            plugin.getPlayerPermissions().get(player).clear();
        for (String keys : permissions.keySet()) {
            plugin.getPlayerPermissions().get(player).put(keys, permissions.get(keys));
            if (Bukkit.getPlayer(player) != null && Objects.requireNonNull(Bukkit.getPlayer(player)).isOnline()) {
                plugin.getPlayerPermissionsAttachments().get(player).setPermission(keys, permissions.get(keys));
            }
        }
        plugin.savePerms();
        if (Bukkit.getPlayer(player) != null && Objects.requireNonNull(Bukkit.getPlayer(player)).isOnline()) {
            plugin.reloadPerms(Bukkit.getPlayer(player));
        }
        return Result.SUCCESS;
    }

    /**
     * Sends a Map to the Server
     *
     * @param destination   The Destination
     * @param mapIdentifier The Identifier of the List (HurtingWaterMap, ChatMuteMap, VillagerSoundsMap, BlockBreakPreventMap, DupeMobsOnKillMap, FreezePlayerMap)
     */
    public Result sendCustomMap(String destination, String mapIdentifier) {
        CustomMap<UUID, Boolean> map;
        switch (mapIdentifier) {
            case "HurtingWaterMap":
                map = new CustomMap<>(plugin.hurtingwater);
                break;
            case "ChatMuteMap":
                map = new CustomMap<>(plugin.chatmute);
                break;
            case "VillagerSoundsMap":
                map = new CustomMap<>(plugin.villagerSounds);
                break;
            case "BlockBreakPreventMap":
                map = new CustomMap<>(plugin.blockBreakPrevent);
                break;
            case "DupeMobsOnKillMap":
                map = new CustomMap<>(plugin.dupeMobsOnKill);
                break;
            case "FreezePlayersMap":
                map = new CustomMap<>(plugin.freezeplayers);
                break;
            default:
                return Result.WRONGMAPIDENTIFIER;
        }
        if(map.getMap().isEmpty()) return Result.EMPTYMAP;
        String mapString = Serialization.serialize(map);
        Message message = new Message(dataClient.getClientName(), destination, Action.SENDCUSTOMMAP, mapIdentifier, mapString);
        if (dataClient.getSettings().isDebugEnabled()) {
            dataClient.getPluginLogger().dataClientMessage(ChatColor.BLUE, false, true,
                    "Message: " + message,
                    "List: " + map,
                    "Destination: " + destination);
        }
        try {
            return dataClient.send(message, true, false);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.ERROR;
        }
    }

    public Result receiveCustomMap(String mapString, String mapIdentifier) {
        Map<UUID, Boolean> customMap = new HashMap<>(((CustomMap<UUID, Boolean>) Serialization.deserialize(mapString, CustomMap.class)).getMap());
        if(customMap.isEmpty()) return Result.EMPTYMAP;
        switch (mapIdentifier) {
            case "HurtingWaterMap":
                for (UUID tempUUID : customMap.keySet()) {
                    if (plugin.hurtingwater.containsKey(tempUUID)) continue;

                    plugin.hurtingwater.put(tempUUID, customMap.get(tempUUID));
                }
                break;
            case "ChatMuteMap":
                for (UUID tempUUID : customMap.keySet()) {
                    if (plugin.chatmute.containsKey(tempUUID)) continue;

                    plugin.chatmute.put(tempUUID, customMap.get(tempUUID));
                }
                break;
            case "VillagerSoundsMap":
                for (UUID tempUUID : customMap.keySet()) {
                    if (plugin.villagerSounds.containsKey(tempUUID)) continue;

                    plugin.villagerSounds.put(tempUUID, customMap.get(tempUUID));
                }
                break;
            case "BlockBreakPreventMap":
                for (UUID tempUUID : customMap.keySet()) {
                    if (plugin.blockBreakPrevent.containsKey(tempUUID)) continue;

                    plugin.blockBreakPrevent.put(tempUUID, customMap.get(tempUUID));
                }
                break;
            case "DupeMobsOnKillMap":
                for (UUID tempUUID : customMap.keySet()) {
                    if (plugin.dupeMobsOnKill.containsKey(tempUUID)) continue;

                    plugin.dupeMobsOnKill.put(tempUUID, customMap.get(tempUUID));
                }
                break;
            case "FreezePlayerMap":
                for (UUID tempUUID : customMap.keySet()) {
                    if (plugin.freezeplayers.containsKey(tempUUID)) continue;

                    plugin.freezeplayers.put(tempUUID, customMap.get(tempUUID));
                }
                break;
            default:
                return Result.WRONGMAPIDENTIFIER;
        }
        return Result.SUCCESS;
    }
}
