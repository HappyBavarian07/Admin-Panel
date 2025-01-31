package de.happybavarian07.adminpanel.utils.managers;/*
 * @Author HappyBavarian07
 * @Date 21.12.2023 | 17:17
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefix;
import de.happybavarian07.adminpanel.utils.tfidfsearch.TFIDFSearch;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class PermissionsManager {
    private final ThreadGroup fileActionGroup;
    private final AdminPanelMain plugin;
    private final File permissionFile;
    private final Map<UUID, PermissionAttachment> playerPermissionsAttachments = new HashMap<>();
    private final Map<UUID, Map<String, Boolean>> playerPermissions = new HashMap<>();
    private Thread permissionIndexThread;
    private TFIDFSearch permissionSearcher;
    private FileConfiguration permissionsConfig;

    public PermissionsManager(AdminPanelMain plugin) {
        this.plugin = plugin;
        this.fileActionGroup = new ThreadGroup("AdminPanel - Permission File Action Group");
        this.permissionFile = new File(plugin.getDataFolder(), "permissions.yml");
        initAndCheckFiles();
        permissionSearcher = new TFIDFSearch(new String[]{"permissionName", "permissionDescription", "permissionDefault", "permissionChildren"});
        this.permissionIndexThread = getPermissionIndexThread();
    }

    public void setup() {
        initPermissions();
        this.permissionIndexThread.start();
        savePermissionsToConfig();
    }

    public TFIDFSearch getPermissionSearcher() {
        return permissionSearcher;
    }

    public Map<UUID, PermissionAttachment> getPlayerPermissionsAttachments() {
        return playerPermissionsAttachments;
    }

    public Map<UUID, Map<String, Boolean>> getPlayerPermissions() {
        return playerPermissions;
    }

    @NotNull
    private Thread getPermissionIndexThread() {
        Thread permissionIndexThread = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            List<Permission> permissions = new ArrayList<>(Bukkit.getPluginManager().getPermissions());
            List<TFIDFSearch.Item> permissionItems = new ArrayList<>();

            for (Permission permission : permissions) {
                Map<String, Object> permissionData = new HashMap<>();
                permissionData.put("permissionName", permission.getName());
                permissionData.put("permissionDescription", permission.getDescription());
                permissionData.put("permissionDefault", permission.getDefault().toString());
                permissionData.put("permissionChildren", permission.getChildren().toString());
                permissionItems.add(new TFIDFSearch.Item(permissionData));
            }

            try {
                permissionSearcher.indexItems(permissionItems);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            long endTime = System.currentTimeMillis();
            plugin.getFileLogger().writeToLog(Level.INFO, "Permission Searcher took '" + (endTime - startTime) + "' ms to index " + permissionItems.size() + " permissions", LogPrefix.DATELOGGER);
        });
        permissionIndexThread.setDaemon(true);
        return permissionIndexThread;
    }

    public void initAndCheckFiles() {
        if (!permissionFile.exists()) {
            try {
                permissionFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        permissionsConfig = YamlConfiguration.loadConfiguration(permissionFile);

        if (!permissionsConfig.isConfigurationSection("Permissions")) {
            permissionsConfig.createSection("Permissions");
            try {
                permissionsConfig.save(permissionFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Objects.requireNonNull(permissionsConfig.getConfigurationSection("Permissions")).getKeys(false).forEach(player -> {
            String path = "Permissions." + player + ".Permissions";
            Map<String, Boolean> perms = new HashMap<>();
            Objects.requireNonNull(permissionsConfig.getConfigurationSection(path)).getKeys(false)
                    .forEach(perm ->
                            perms.put(perm, permissionsConfig.getBoolean(path + "." + perm)));
            playerPermissions.put(UUID.fromString(player), perms);
        });
    }

    public void initPermissions() {
        for (String configSection : Objects.requireNonNull(permissionsConfig.getConfigurationSection("Permissions")).getKeys(false)) {
            UUID tempUUID = UUID.fromString(configSection);
            if (!playerPermissionsAttachments.containsKey(tempUUID)) {
                if (!playerPermissions.containsKey(tempUUID)) {
                    playerPermissions.put(tempUUID, new HashMap<>());
                }
                Map<String, Boolean> permissions = new HashMap<>();
                for (String permissionName : Objects.requireNonNull(permissionsConfig.getConfigurationSection("Permissions." + configSection + ".Permissions")).getKeys(true)) {
                    if (permissionsConfig.isConfigurationSection("Permissions." + configSection + ".Permissions." + permissionName))
                        continue;
                    if (permissionName.contains("(<->)")) permissionName = permissionName.replace("(<->)", ".");
                    permissions.put(permissionName, permissionsConfig.getBoolean("Permissions." + configSection + ".Permissions." + permissionName));
                }
                playerPermissions.put(tempUUID, permissions);
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!playerPermissionsAttachments.containsKey(online.getUniqueId())) {
                        if (!playerPermissions.containsKey(online.getUniqueId())) {
                            playerPermissions.put(online.getUniqueId(), new HashMap<>());
                        }
                        PermissionAttachment attachment = online.addAttachment(plugin);
                        Map<String, Boolean> permissions = playerPermissions.get(online.getUniqueId());
                        for (Map.Entry<String, Boolean> perms : permissions.entrySet()) {
                            attachment.setPermission(perms.getKey(), perms.getValue());
                        }
                        playerPermissionsAttachments.put(online.getUniqueId(), attachment);
                        online.recalculatePermissions();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 180);
    }

    public void addPermission(UUID playerUUID, String permission, boolean value, boolean saveToConfig, boolean reloadPermissions) {
        if (playerPermissions.containsKey(playerUUID)) {
            playerPermissions.get(playerUUID).put(permission, value);
        } else {
            Map<String, Boolean> permissions = new HashMap<>();
            permissions.put(permission, value);
            playerPermissions.put(playerUUID, permissions);
        }

        if (saveToConfig) {
            new PermissionFileActionThread(playerUUID, permission, value).start();
        }

        if (reloadPermissions) {
            reloadPermissions(Bukkit.getPlayer(playerUUID));
        }
    }

    public void addPermissions(UUID playerUUID, Map<String, Boolean> permissions, boolean saveToConfig, boolean reloadPermissions) {
        if (playerPermissions.containsKey(playerUUID)) {
            playerPermissions.get(playerUUID).putAll(permissions);
        } else {
            playerPermissions.put(playerUUID, permissions);
        }

        if (saveToConfig) {
            new PermissionFileActionThread(playerUUID, permissions).start();
        }

        if (reloadPermissions) {
            reloadPermissions(Bukkit.getPlayer(playerUUID));
        }
    }

    public void setPermissionValue(UUID playerUUID, String permission, boolean value, boolean saveToConfig, boolean reloadPermissions) {
        if (playerPermissions.containsKey(playerUUID)) {
            if (!playerPermissions.get(playerUUID).containsKey(permission)) return;

            playerPermissions.get(playerUUID).replace(permission, value);
        } else {
            playerPermissions.put(playerUUID, new HashMap<>());
            return;
        }

        if (saveToConfig) {
            new PermissionFileActionThread(playerUUID, permission, value).start();
        }

        if (reloadPermissions) {
            reloadPermissions(Bukkit.getPlayer(playerUUID));
        }
    }

    public void setPermissionValues(UUID playerUUID, Map<String, Boolean> permissions, boolean saveToConfig, boolean reloadPermissions) {
        if (playerPermissions.containsKey(playerUUID)) {
            for (String permission : permissions.keySet()) {
                if (!playerPermissions.get(playerUUID).containsKey(permission)) continue;

                playerPermissions.get(playerUUID).replace(permission, permissions.get(permission));
            }
        } else {
            playerPermissions.put(playerUUID, new HashMap<>());
            return;
        }

        if (saveToConfig) {
            new PermissionFileActionThread(playerUUID, permissions).start();
        }

        if (reloadPermissions) {
            reloadPermissions(Bukkit.getPlayer(playerUUID));
        }
    }

    public void removePermission(UUID playerUUID, String permission, boolean removeFromConfig, boolean reloadPermissions) {
        if (playerPermissions.containsKey(playerUUID)) {
            playerPermissions.get(playerUUID).remove(permission);
        }

        if (removeFromConfig) {
            new PermissionFileActionThread(playerUUID, permission, null).start();
        }

        if (reloadPermissions) {
            reloadPermissions(Bukkit.getPlayer(playerUUID));
        }
    }

    public void removePermissions(UUID playerUUID, String[] permissions, boolean removeFromConfig, boolean reloadPermissions) {
        if (playerPermissions.containsKey(playerUUID)) {
            for (String permission : permissions) {
                playerPermissions.get(playerUUID).remove(permission);
            }
        }

        if (removeFromConfig) {
            for (String permission : permissions) {
                new PermissionFileActionThread(playerUUID, permission, null).start();
            }
        }

        if (reloadPermissions) {
            reloadPermissions(Bukkit.getPlayer(playerUUID));
        }
    }

    /**
     * Reloads the Permissions for a Player
     * <p>
     *     This Method reloads the Permissions for a Player.
     *     It removes the Permissions from the Player and adds them again.
     * @param player The Player to reload the Permissions for
     */
    public void reloadPermissions(Player player) {
        if (playerPermissionsAttachments.containsKey(player.getUniqueId())) {
            player.removeAttachment(playerPermissionsAttachments.get(player.getUniqueId()));
        }
        PermissionAttachment attachment = player.addAttachment(plugin);
        if (playerPermissionsAttachments.containsKey(player.getUniqueId())) {
            if (!playerPermissions.containsKey(player.getUniqueId())) {
                playerPermissions.put(player.getUniqueId(), new HashMap<>());
            }
            Map<String, Boolean> permissions = playerPermissions.get(player.getUniqueId());
            for (String perms : permissions.keySet()) {
                attachment.setPermission(perms, permissions.get(perms));
            }
            playerPermissionsAttachments.put(player.getUniqueId(), attachment);
            player.recalculatePermissions();
        }
    }

    /**
     * Reloads the Permissions for all Players on the Server
     * <p>
     *     This Method reloads the Permissions for all Players on the Server.
     *     It removes the Permissions from the Player and adds them again.
     *
     */
    public void reloadPermissionsGlobally() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (playerPermissionsAttachments.containsKey(online.getUniqueId())) {
                online.removeAttachment(playerPermissionsAttachments.get(online.getUniqueId()));
            }
            PermissionAttachment attachment = online.addAttachment(plugin);
            if (playerPermissionsAttachments.containsKey(online.getUniqueId())) {
                if (!playerPermissions.containsKey(online.getUniqueId())) {
                    playerPermissions.put(online.getUniqueId(), new HashMap<>());
                    continue;
                }
                Map<String, Boolean> permissions = playerPermissions.get(online.getUniqueId());
                for (String perms : permissions.keySet()) {
                    attachment.setPermission(perms, permissions.get(perms));
                }
                playerPermissionsAttachments.put(online.getUniqueId(), attachment);
                online.recalculatePermissions();
            }
        }
    }

    public void savePermissionsToConfig() {
        for (UUID playerUUID : playerPermissions.keySet()) {
            new PermissionFileActionThread(playerUUID, playerPermissions.get(playerUUID)).start();
        }
    }

    /**
     * Thread to handle the File Actions for Permissions
     * <p>
     *     This Thread is used to handle the File Actions for Permissions. It can add, remove or set Permissions for a Player.
     *     It can also add or remove multiple Permissions at once.
     *     It can also save the Permissions to the permissions.yml file.
     *     <br> <br>
     *     This Thread is used to prevent the Server from lagging when saving Permissions to the permissions.yml file.
     *     <br> <br>
     *     This Thread is a private class of the PermissionsManager Class.
     */
    private class PermissionFileActionThread extends Thread {
        private final Map<String, Boolean> permissions;
        private final UUID playerUUID;

        public PermissionFileActionThread(UUID playerUUID, String permission, @Nullable Boolean value) {
            super(fileActionGroup, "AdminPanel - Permission File Action Thread");
            this.playerUUID = playerUUID;
            this.permissions = new HashMap<>();
            permissions.put(permission, value);
        }

        public PermissionFileActionThread(UUID playerUUID, Map<String, Boolean> permissions) {
            super(fileActionGroup, "AdminPanel - Permission File Action Thread");
            this.playerUUID = playerUUID;
            this.permissions = permissions;
        }

        @Override
        public void run() {
            File permissionFile = new File(AdminPanelMain.getPlugin().getDataFolder(), "permissions.yml");
            FileConfiguration permissionsConfig = YamlConfiguration.loadConfiguration(permissionFile);

            if (!permissionsConfig.isConfigurationSection("Permissions")) {
                permissionsConfig.createSection("Permissions");
            }

            if (permissions.size() == 1) {
                permissionsConfig.set("Permissions." + playerUUID.toString() + ".Permissions." + permissions.keySet().toArray()[0], permissions.values().toArray()[0]);
            } else {
                permissions.forEach((permission, value) -> permissionsConfig.set("Permissions." + playerUUID.toString() + ".Permissions." + permission, value));
            }

            try {
                permissionsConfig.save(permissionFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
