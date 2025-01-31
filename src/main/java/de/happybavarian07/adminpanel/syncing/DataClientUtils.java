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
                this.plugin.getPermissionsManager().getPlayerPermissions().get(player).clear();
            }

            for (Entry<String, Boolean> stringBooleanEntry : permissions.entrySet()) {
                this.plugin.getPermissionsManager().getPlayerPermissions().get(player).put(stringBooleanEntry.getKey(), stringBooleanEntry.getValue());
                if (Bukkit.getPlayer(player) != null && Objects.requireNonNull(Bukkit.getPlayer(player)).isOnline()) {
                    this.plugin.getPermissionsManager().getPlayerPermissionsAttachments().get(player).setPermission(stringBooleanEntry.getKey(), stringBooleanEntry.getValue());
                }
            }

            this.plugin.getPermissionsManager().savePermissionsToConfig();
            if (Bukkit.getPlayer(player) != null && Objects.requireNonNull(Bukkit.getPlayer(player)).isOnline()) {
                this.plugin.getPermissionsManager().reloadPermissions(Objects.requireNonNull(Bukkit.getPlayer(player)));
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
            StartUpLogger logger = this.dataClient.getPluginLogger();
            String[] data = new String[]{"UUID: " + player, "Data from Server: " + permissionsString, "Deserialized Data: " + deserializedPermissions, null};
            data[3] = "Data from Server == Deserialized Data? " + permissionsString.equals(Serialization.serialize(deserializedPermissions));
            logger.dataClientMessage(ChatColor.BLUE, false, true, data);
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

        return switch (var3) {
            case 0 -> new CustomMap<>(this.plugin.getPluginStateManager().getHurtingWaterMap());
            case 1 -> new CustomMap<>(this.plugin.getPluginStateManager().getChatMuteMap());
            case 2 -> new CustomMap<>(this.plugin.getPluginStateManager().getVillagerSoundsMap());
            case 3 -> new CustomMap<>(this.plugin.getPluginStateManager().getBlockBreakPreventMap());
            case 4 -> new CustomMap<>(this.plugin.getPluginStateManager().getDupeMobsOnKillMap());
            case 5 -> new CustomMap<>(this.plugin.getPluginStateManager().getFreezePlayersMap());
            default -> null;
        };
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
    