package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefixExtension;
import de.happybavarian07.adminpanel.utils.tfidfsearch.TFIDFSearch;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PermissionsManager {
    private final AdminPanelMain plugin;
    private final Map<UUID, PermissionAttachment> playerPermissionsAttachments = new HashMap<>();
    private final Map<UUID, Map<String, Boolean>> permissionMapCache = new HashMap<>();
    private final TFIDFSearch permissionSearcher;
    private final PlayerPermissionRepository permissionRepository;
    private CompletableFuture<Void> permissionIndexFuture;
    private Thread permissionIndexThread;

    public PermissionsManager(AdminPanelMain plugin, long cacheClearDelayTicks) {
        // TODO Add Player Groups to this to be like any other Permission system with groups and stuff.
        this.plugin = plugin;
        permissionSearcher = new TFIDFSearch(new String[]{"permissionName", "permissionDescription", "permissionDefault", "permissionChildren"});
        this.permissionRepository = plugin.getRepositoryController().getRepository(PlayerPermissionRepository.class);

        new BukkitRunnable() {
            @Override
            public void run() {
                permissionIndexThread = getPermissionIndexThread();
                if (!permissionIndexThread.isAlive()) {
                    permissionIndexThread.start();
                }
            }
        }.runTaskTimer(plugin, 0L, 6 * 60 * 20L);
    }

    public void setup() {
        migrateOldPermissions();

        new Thread(() -> {
            // wait for the database to be ready
            while (!permissionRepository.isDatabaseReady()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    plugin.getFileLogger().writeToLog(Level.SEVERE, "Error while waiting for database to be ready: " + e.getMessage(), LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
                }
            }

            try {
                long totalEntries = permissionRepository.count();
                long uniquePlayers = permissionRepository.countDistinctPlayers();
                long uniquePermissions = permissionRepository.countDistinctPermissions();

                plugin.getFileLogger().writeToLog(Level.INFO, "Permission Repository Statistics:", LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
                plugin.getFileLogger().writeToLog(Level.INFO, "- Total permission entries: " + totalEntries, LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
                plugin.getFileLogger().writeToLog(Level.INFO, "- Unique players with permissions: " + uniquePlayers, LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
                plugin.getFileLogger().writeToLog(Level.INFO, "- Unique permissions defined: " + uniquePermissions, LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
            } catch (Exception e) {
                e.printStackTrace();
                plugin.getFileLogger().writeToLog(Level.WARNING, "Could not retrieve permission repository statistics: " + e, LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
            }
            plugin.getFileLogger().writeToLog(Level.INFO, "Database is ready starting Permission init code", LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
            initPermissions();
        }).start();
    }

    public void onServerLoad(ServerLoadEvent event) {
        if (event.getType() == ServerLoadEvent.LoadType.STARTUP) {
            plugin.getFileLogger().writeToLog(Level.INFO, "Starting Permission Indexer Thread after Server is done starting now", LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
            if (permissionIndexThread == null) {
                permissionIndexThread = getPermissionIndexThread();
            }
            if (!permissionIndexThread.isAlive()) {
                permissionIndexThread.start();
            }
        }
    }

    public TFIDFSearch getPermissionSearcher() {
        if (permissionIndexFuture != null && !permissionIndexFuture.isDone()) {
            plugin.getFileLogger().writeToLog(Level.INFO, "Waiting for permission index to complete...", LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
            try {
                permissionIndexFuture.get();
            } catch (Exception e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Error while waiting for permission index to complete: " + e.getMessage(), LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
            }
        }
        return permissionSearcher;
    }

    public Map<UUID, PermissionAttachment> getPlayerPermissionsAttachments() {
        return playerPermissionsAttachments;
    }

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

            permissionIndexFuture = permissionSearcher.indexItems(permissionItems);
            permissionIndexFuture.join();

            long endTime = System.currentTimeMillis();
            plugin.getFileLogger().writeToLog(Level.INFO, "Permission Searcher took '" + (endTime - startTime) + "' ms to index " + permissionItems.size() + " permissions", LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
        });
        permissionIndexThread.setDaemon(true);
        permissionIndexThread.setName("Permission Indexer Thread");
        return permissionIndexThread;
    }

    private void migrateOldPermissions() {
        File permissionFile = new File(plugin.getDataFolder(), "permissions.yml");
        if (!permissionFile.exists()) {
            plugin.getFileLogger().writeToLog(Level.WARNING, "Old permissions.yml file does not exist or is empty: No migration needed!", "Permission Manager", true);
            return;
        }
        plugin.getFileLogger().writeToLog(Level.INFO, "Starting migration of old permissions to database system", "Permission Manager", true);
        FileConfiguration permissionsConfig = YamlConfiguration.loadConfiguration(permissionFile);
        Objects.requireNonNull(permissionsConfig.getConfigurationSection("Permissions")).getKeys(false).forEach(playerUUID -> {
            try {
                UUID uuid = UUID.fromString(playerUUID);
                String path = "Permissions." + playerUUID + ".Permissions";
                Objects.requireNonNull(permissionsConfig.getConfigurationSection(path)).getKeys(false).forEach(perm -> {
                    boolean value = permissionsConfig.getBoolean(path + "." + perm);
                    // Speichern in der Datenbank
                    PlayerPermission permission = new PlayerPermission();
                    permission.setPlayerUUID(uuid);
                    permission.setPermission(perm);
                    permission.setValue(value);
                    permission.setEntryID(generateUniqueEntryID(uuid.toString(), perm));
                    permissionRepository.save(permission);
                });
            } catch (IllegalArgumentException e) {
                plugin.getFileLogger().writeToLog(Level.WARNING, "Invalid UUID in permissions.yml: " + playerUUID, "Permission Manager", true);
            }
        });

        // Umbenennen der alten permissions.yml um sie zu archivieren
        File backupFile = new File(plugin.getDataFolder(), "permissions_old.yml");
        if (permissionFile.renameTo(backupFile)) {
            plugin.getFileLogger().writeToLog(Level.INFO, "Old permissions.yml has been renamed to permissions_old.yml", "Permission Manager", true);
        } else {
            plugin.getFileLogger().writeToLog(Level.WARNING, "Could not rename old permissions.yml file", "Permission Manager", true);
        }

        plugin.getFileLogger().writeToLog(Level.INFO, "Migration of old permissions completed", "Permission Manager", true);
    }

    private String generateUniqueEntryID(String playerUUID, String permission) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String input = playerUUID + ":" + permission;
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    public void initPermissions() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!permissionRepository.isDatabaseReady()) {
                    plugin.getFileLogger().writeToLog(Level.WARNING, "Database not ready, skipping permission initialization", LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
                    return;
                }

                for (Player online : Bukkit.getOnlinePlayers()) {
                    UUID playerUUID = online.getUniqueId();
                    if (!playerPermissionsAttachments.containsKey(playerUUID)) {
                        try {
                            PermissionAttachment attachment = online.addAttachment(plugin);
                            Map<String, Boolean> permissions;

                            if (permissionMapCache.containsKey(playerUUID)) {
                                permissions = permissionMapCache.get(playerUUID);
                                plugin.getFileLogger().writeToLog(Level.FINE, "Loading permissions from cache for " + online.getName(), LogPrefixExtension.DATELOGGER.getLogPrefix(), false);
                            } else {
                                List<PlayerPermission> playerPermissions = permissionRepository.findByPlayerUUID(playerUUID);
                                plugin.getFileLogger().writeToLog(Level.FINE, "Loading permissions from database for " + online.getName(), LogPrefixExtension.DATELOGGER.getLogPrefix(), false);
                                permissions = new HashMap<>();

                                if (playerPermissions != null && !playerPermissions.isEmpty()) {
                                    for (PlayerPermission playerPermission : playerPermissions) {
                                        if (playerPermission != null && playerPermission.getPermission() != null) {
                                            permissions.put(playerPermission.getPermission(), playerPermission.isValue());
                                        }
                                    }
                                    plugin.getFileLogger().writeToLog(Level.INFO, "Loaded " + permissions.size() + " permissions from database for " + online.getName(), LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
                                } else {
                                    plugin.getFileLogger().writeToLog(Level.INFO, "No permissions found in database for " + online.getName(), LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
                                }

                                permissionMapCache.put(playerUUID, permissions);
                            }

                            for (Map.Entry<String, Boolean> perms : permissions.entrySet()) {
                                if (perms.getKey() != null) {
                                    attachment.setPermission(perms.getKey(), perms.getValue());
                                }
                            }

                            playerPermissionsAttachments.put(playerUUID, attachment);
                            online.recalculatePermissions();

                            plugin.getFileLogger().writeToLog(Level.FINE, "Applied " + permissions.size() + " permissions to " + online.getName(), LogPrefixExtension.DATELOGGER.getLogPrefix(), false);

                        } catch (Exception e) {
                            plugin.getFileLogger().writeToLog(Level.SEVERE, "Error loading permissions for " + online.getName() + ": " + e.getMessage(), LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 5 * 60 * 20L);
    }

    public void addPermission(UUID playerUUID, String permission, boolean value, boolean reloadPermissions) {
        List<PlayerPermission> existingPermission = permissionRepository.findByPlayerUUIDAndPermission(playerUUID, permission);
        PlayerPermission firstPermission = existingPermission.isEmpty() ? null : existingPermission.get(0);

        if (firstPermission != null) {
            firstPermission.setValue(value);
            permissionRepository.save(firstPermission);
        } else {
            PlayerPermission newPermission = new PlayerPermission();
            newPermission.setPlayerUUID(playerUUID);
            newPermission.setPermission(permission);
            newPermission.setValue(value);
            newPermission.setEntryID(generateUniqueEntryID(playerUUID.toString(), permission));
            permissionRepository.save(newPermission);
        }

        if (reloadPermissions) {
            reloadPermissions(Bukkit.getPlayer(playerUUID));
        }
    }

    public void addPermissions(UUID playerUUID, Map<String, Boolean> permissions, boolean reloadPermissions) {
        if (playerUUID == null) return;
        if (permissions.isEmpty()) return;

        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            String permission = entry.getKey();
            boolean value = entry.getValue();

            List<PlayerPermission> existingPermission = permissionRepository.findByPlayerUUIDAndPermission(playerUUID, permission);
            PlayerPermission firstPermission = existingPermission.isEmpty() ? null : existingPermission.get(0);

            if (firstPermission != null) {
                // Aktualisieren des bestehenden Eintrags
                firstPermission.setValue(value);
                permissionRepository.save(firstPermission);
            } else {
                PlayerPermission newPermission = new PlayerPermission();
                newPermission.setPlayerUUID(playerUUID);
                newPermission.setPermission(permission);
                newPermission.setValue(value);
                newPermission.setEntryID(generateUniqueEntryID(playerUUID.toString(), permission));
                permissionRepository.save(newPermission);
            }
        }

        if (reloadPermissions) {
            reloadPermissions(Bukkit.getPlayer(playerUUID));
        }
    }

    public void setPermissionValue(UUID playerUUID, String permission, boolean value, boolean reloadPermissions, boolean createIfNotExists) {
        List<PlayerPermission> existingPermission = permissionRepository.findByPlayerUUIDAndPermission(playerUUID, permission);
        PlayerPermission firstPermission = existingPermission.isEmpty() ? null : existingPermission.get(0);

        if (firstPermission != null) {
            firstPermission.setValue(value);
            permissionRepository.save(firstPermission);

            if (reloadPermissions) {
                reloadPermissions(Bukkit.getPlayer(playerUUID));
            }
        } else if (createIfNotExists) {
            addPermission(playerUUID, permission, value, reloadPermissions);
        }
    }

    public void setPermissionValues(UUID playerUUID, Map<String, Boolean> permissions, boolean reloadPermissions, boolean createIfNotExists) {
        Map<String, Boolean> nonExistingPermissions = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            String permission = entry.getKey();
            boolean value = entry.getValue();

            List<PlayerPermission> existingPermission = permissionRepository.findByPlayerUUIDAndPermission(playerUUID, permission);
            PlayerPermission firstPermission = existingPermission.isEmpty() ? null : existingPermission.get(0);

            if (firstPermission != null) {
                firstPermission.setValue(value);
                permissionRepository.save(firstPermission);
            } else if (createIfNotExists) {
                nonExistingPermissions.put(permission, value);
            }
        }

        if (createIfNotExists) {
            addPermissions(playerUUID, nonExistingPermissions, false);
        }

        if (reloadPermissions) {
            reloadPermissions(Bukkit.getPlayer(playerUUID));
        }
    }

    public void removePermission(UUID playerUUID, String permission, boolean reloadPermissions) {
        List<PlayerPermission> existingPermission = permissionRepository.findByPlayerUUIDAndPermission(playerUUID, permission);
        PlayerPermission firstPermission = existingPermission.isEmpty() ? null : existingPermission.get(0);

        if (firstPermission != null) {
            permissionRepository.delete(firstPermission);

            if (reloadPermissions) {
                reloadPermissions(Bukkit.getPlayer(playerUUID));
            }
        }
    }

    public void removePermissions(UUID playerUUID, String[] permissions, boolean reloadPermissions) {
        for (String permission : permissions) {
            List<PlayerPermission> existingPermission = permissionRepository.findByPlayerUUIDAndPermission(playerUUID, permission);
            PlayerPermission firstPermission = existingPermission.isEmpty() ? null : existingPermission.get(0);

            if (firstPermission != null) {
                permissionRepository.delete(firstPermission);
            }
        }

        if (reloadPermissions) {
            reloadPermissions(Bukkit.getPlayer(playerUUID));
        }
    }

    public Map<String, Boolean> getPlayerPermissions(UUID playerUUID) {
        Map<String, Boolean> permissions = permissionMapCache.get(playerUUID);
        if (permissions == null) {
            permissions = new HashMap<>();
            List<PlayerPermission> playerPermissions = permissionRepository.findByPlayerUUID(playerUUID);
            for (PlayerPermission playerPermission : playerPermissions) {
                permissions.put(playerPermission.getPermission(), playerPermission.isValue());
            }
            permissionMapCache.put(playerUUID, permissions);
        }
        return permissions;
    }

    public Map<UUID, Map<String, Boolean>> getAllPlayerPermissions() {
        Map<UUID, Map<String, Boolean>> allPermissions = new HashMap<>();
        for (PlayerPermission permission : permissionRepository.findAll()) {
            allPermissions.computeIfAbsent(permission.getPlayerUUID(), k -> new HashMap<>()).put(permission.getPermission(), permission.isValue());
        }
        return allPermissions;
    }

    /**
     * Reloads the Permissions for a Player
     * <p>
     * This Method reloads the Permissions for a Player.
     * It removes the Permissions from the Player and adds them again.
     *
     * @param player The Player to reload the Permissions for
     */
    public void reloadPermissions(Player player) {
        if (player == null) return;

        UUID playerUUID = player.getUniqueId();

        if (playerPermissionsAttachments.containsKey(playerUUID)) {
            player.removeAttachment(playerPermissionsAttachments.get(playerUUID));
        }

        PermissionAttachment attachment = player.addAttachment(plugin);
        Map<String, Boolean> permissions;
        if (permissionMapCache.containsKey(playerUUID)) {
            permissions = permissionMapCache.get(playerUUID);
        } else {
            List<PlayerPermission> playerPermissions = permissionRepository.findByPlayerUUID(playerUUID);
            permissions = new HashMap<>();
            for (PlayerPermission playerPermission : playerPermissions) {
                permissions.put(playerPermission.getPermission(), playerPermission.isValue());
            }
            permissionMapCache.put(playerUUID, permissions);
        }

        for (Map.Entry<String, Boolean> perms : permissions.entrySet()) {
            attachment.setPermission(perms.getKey(), perms.getValue());
        }

        playerPermissionsAttachments.put(playerUUID, attachment);
        player.recalculatePermissions();
        player.updateCommands();
    }

    /**
     * Reloads the Permissions for all Players on the Server
     * <p>
     * This Method reloads the Permissions for all Players on the Server.
     * It removes the Permissions from the Player and adds them again.
     */
    public void reloadPermissionsGlobally() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            reloadPermissions(online);
        }
    }

    public void clearPermissions(UUID playerUUID) {
        if (playerPermissionsAttachments.containsKey(playerUUID)) {
            playerPermissionsAttachments.get(playerUUID).remove();
            playerPermissionsAttachments.remove(playerUUID);
        }
        permissionRepository.deleteAllByPlayerUUID(playerUUID);
    }

    public boolean hasPermissions(UUID permissionHolder) {
        return permissionRepository.countEntriesByUUID(permissionHolder) > 0;
    }

    public void loadPlayerPermissions(Player player) {
        if (player == null) return;

        UUID playerUUID = player.getUniqueId();
        if (playerPermissionsAttachments.containsKey(playerUUID)) {
            plugin.getFileLogger().writeToLog(Level.WARNING, "Player " + player.getName() + " already has permissions loaded, skipping.", LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
            return;
        }

        try {
            PermissionAttachment attachment = player.addAttachment(plugin);
            Map<String, Boolean> permissions = getPlayerPermissions(playerUUID);

            for (Map.Entry<String, Boolean> perms : permissions.entrySet()) {
                attachment.setPermission(perms.getKey(), perms.getValue());
            }

            playerPermissionsAttachments.put(playerUUID, attachment);
            player.recalculatePermissions();
            plugin.getFileLogger().writeToLog(Level.INFO, "Loaded permissions for player: " + player.getName(), LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Error loading permissions for player " + player.getName() + ": " + e.getMessage(), LogPrefixExtension.DATELOGGER.getLogPrefix(), true);
        }
    }

    public void unloadPlayerPermissions(Player player) {
        if (player == null) return;
        UUID playerUUID = player.getUniqueId();
        if (playerPermissionsAttachments.containsKey(playerUUID)) {
            player.removeAttachment(playerPermissionsAttachments.get(playerUUID));
            playerPermissionsAttachments.remove(playerUUID);
        }
        permissionMapCache.remove(playerUUID);
    }
}
