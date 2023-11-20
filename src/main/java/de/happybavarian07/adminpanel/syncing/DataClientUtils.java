package de.happybavarian07.adminpanel.syncing;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.syncing.utils.Action;
import de.happybavarian07.adminpanel.syncing.utils.CustomMap;
import de.happybavarian07.adminpanel.syncing.utils.Packet;
import de.happybavarian07.adminpanel.syncing.utils.PermissionsMap;
import de.happybavarian07.adminpanel.utils.Result;
import de.happybavarian07.adminpanel.utils.Serialization;
import de.happybavarian07.adminpanel.utils.StartUpLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class DataClientUtils {
    private final DataClient dataClient;
    private final AdminPanelMain plugin;

    public DataClientUtils(DataClient dataClient) {
        this.dataClient = dataClient;
        this.plugin = AdminPanelMain.getPlugin();
    }

    public Result sendPlayerPermissions(UUID player, Map<String, Boolean> permissions, String destination) {
        if (permissions.isEmpty()) {
            return Result.EMPTYMAP;
        } else {
            String permissionsString = Serialization.serialize(new PermissionsMap(permissions));
            Packet packet = new Packet(this.dataClient.getConnectionHandler().getClientName(), destination, Action.SENDPERMISSIONS, new String[]{player.toString(), permissionsString});
            this.logDebugInfo(packet, player, permissions, destination);

            try {
                return this.dataClient.getPacketHandler().send(packet, true, false);
            } catch (IOException var7) {
                var7.printStackTrace();
                return Result.ERROR;
            }
        }
    }

    public Result applyReceivedPermissions(UUID player, String permissionsString) {
        PermissionsMap deserializedPermissions = (PermissionsMap) Serialization.deserialize(permissionsString, PermissionsMap.class);
        Map<String, Boolean> permissions = new HashMap<>(deserializedPermissions.getPermissions());
        if (permissions.isEmpty()) {
            return Result.EMPTYMAP;
        } else {
            this.logDebugInfo(player, permissionsString, deserializedPermissions);
            if (this.dataClient.getSettingsManager().isOverwritePermissionsEnabled()) {
                this.plugin.getPlayerPermissions().get(player).clear();
            }

            for (Entry<String, Boolean> stringBooleanEntry : permissions.entrySet()) {
                this.plugin.getPlayerPermissions().get(player).put(stringBooleanEntry.getKey(), stringBooleanEntry.getValue());
                if (Bukkit.getPlayer(player) != null && Objects.requireNonNull(Bukkit.getPlayer(player)).isOnline()) {
                    this.plugin.getPlayerPermissionsAttachments().get(player).setPermission(stringBooleanEntry.getKey(), stringBooleanEntry.getValue());
                }
            }

            this.plugin.savePerms();
            if (Bukkit.getPlayer(player) != null && ((Player) Objects.requireNonNull(Bukkit.getPlayer(player))).isOnline()) {
                this.plugin.reloadPerms(Bukkit.getPlayer(player));
            }

            return Result.SUCCESS;
        }
    }

    public Result sendCustomMap(String destination, String mapIdentifier) {
        CustomMap<UUID, Boolean> map = this.getCustomMap(mapIdentifier);

        assert map != null;

        if (map.getMap().isEmpty()) {
            return Result.EMPTYMAP;
        } else {
            String mapString = Serialization.serialize(map);
            Packet packet = new Packet(this.dataClient.getConnectionHandler().getClientName(), destination, Action.SENDCUSTOMMAP, new String[]{mapIdentifier, mapString});
            this.logDebugInfo(packet, map, destination);

            try {
                return this.dataClient.getPacketHandler().send(packet, true, false);
            } catch (IOException var7) {
                var7.printStackTrace();
                return Result.ERROR;
            }
        }
    }

    public Result receiveCustomMap(String mapString, String mapIdentifier) {
        CustomMap<UUID, Boolean> deserializedMap = (CustomMap) Serialization.deserialize(mapString, CustomMap.class);
        if (deserializedMap != null && !deserializedMap.getMap().isEmpty()) {
            this.updateCustomMap(mapIdentifier, deserializedMap.getMap());
            return Result.SUCCESS;
        } else {
            return Result.EMPTYMAP;
        }
    }

    private void logDebugInfo(Packet packet, UUID player, Map<String, Boolean> permissions, String destination) {
        if (this.dataClient.getSettingsManager().isDebugEnabled()) {
            this.dataClient.getPluginLogger().dataClientMessage(ChatColor.BLUE, false, true, "Packet: " + packet, "UUID: " + player, "Permissions: " + permissions, "Destination: " + destination);
        }

    }

    private void logDebugInfo(UUID player, String permissionsString, PermissionsMap deserializedPermissions) {
        if (this.dataClient.getSettingsManager().isDebugEnabled()) {
            StartUpLogger var10000 = this.dataClient.getPluginLogger();
            ChatColor var10001 = ChatColor.BLUE;
            String[] var10004 = new String[]{"UUID: " + player, "Data from Server: " + permissionsString, "Deserialized Data: " + deserializedPermissions, null};
            boolean var10007 = permissionsString.equals(Serialization.serialize(deserializedPermissions));
            var10004[3] = "Data from Server == Deserialized Data? " + var10007;
            var10000.dataClientMessage(var10001, false, true, var10004);
        }

    }

    private void logDebugInfo(Packet packet, CustomMap<UUID, Boolean> map, String destination) {
        if (this.dataClient.getSettingsManager().isDebugEnabled()) {
            this.dataClient.getPluginLogger().dataClientMessage(ChatColor.BLUE, false, true, "Message: " + packet, "List: " + map, "Destination: " + destination);
        }

    }

    private CustomMap<UUID, Boolean> getCustomMap(String mapIdentifier) {
        byte var3 = -1;
        switch (mapIdentifier.hashCode()) {
            case -1653504362:
                if (mapIdentifier.equals("BlockBreakPreventMap")) {
                    var3 = 3;
                }
                break;
            case -628385102:
                if (mapIdentifier.equals("VillagerSoundsMap")) {
                    var3 = 2;
                }
                break;
            case 138974008:
                if (mapIdentifier.equals("HurtingWaterMap")) {
                    var3 = 0;
                }
                break;
            case 757051883:
                if (mapIdentifier.equals("ChatMuteMap")) {
                    var3 = 1;
                }
                break;
            case 1148780641:
                if (mapIdentifier.equals("FreezePlayersMap")) {
                    var3 = 5;
                }
                break;
            case 1575370054:
                if (mapIdentifier.equals("DupeMobsOnKillMap")) {
                    var3 = 4;
                }
        }

        switch (var3) {
            case 0:
                return new CustomMap<>(this.plugin.hurtingwater);
            case 1:
                return new CustomMap<>(this.plugin.chatmute);
            case 2:
                return new CustomMap<>(this.plugin.villagerSounds);
            case 3:
                return new CustomMap<>(this.plugin.blockBreakPrevent);
            case 4:
                return new CustomMap<>(this.plugin.dupeMobsOnKill);
            case 5:
                return new CustomMap<>(this.plugin.freezeplayers);
            default:
                return null;
        }
    }

    private void updateCustomMap(String mapIdentifier, Map<UUID, Boolean> customMap) {
        CustomMap<UUID, Boolean> map = this.getCustomMap(mapIdentifier);
        if (map != null) {

            for (UUID tempUUID : customMap.keySet()) {
                if (!map.getMap().containsKey(tempUUID)) {
                    map.getMap().put(tempUUID, customMap.get(tempUUID));
                }
            }
        }

    }
}
    