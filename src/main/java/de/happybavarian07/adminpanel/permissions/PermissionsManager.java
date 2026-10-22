package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.permissions.engine.Node;
import de.happybavarian07.adminpanel.utils.LogPrefixExtension;
import de.happybavarian07.adminpanel.utils.tfidfsearch.TFIDFSearch;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Entry point of the permission manager for the rest of the plugin. The real work is done by {@link PermissionService}; this class keeps
 * the methods the plugin always had (single permissions of a player, reload, attachments), starts the service once the database is
 * ready, imports the old {@code permissions.yml} and keeps the search index of all known permissions.
 */
public class PermissionsManager {
    private final AdminPanelMain plugin;
    private final PermissionService service;
    private final JpaPermissionStore store;
    private final PlayerPermissionRepository repository;
    private final TFIDFSearch permissionSearcher;
    private volatile CompletableFuture<Void> permissionIndexFuture;
    private volatile int indexedHash;
    private Thread permissionIndexThread;
    private volatile boolean vaultRegistered;

    public PermissionsManager(AdminPanelMain plugin) {
        this.plugin = plugin;
        this.repository = plugin.getRepositoryManager().getRepository(PlayerPermissionRepository.class);
        this.store = new JpaPermissionStore(repository, plugin.getRepositoryManager().getRepository(PermissionGroupRepository.class),
                plugin.getRepositoryManager().getRepository(GroupNodeRepository.class));
        ExecutorService io = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "AdminPanel-Permission-IO");
            thread.setDaemon(true);
            return thread;
        });
        PermissionData data = new PermissionData(store, io, System::currentTimeMillis, AdminPanelMain::logError);
        this.service = new PermissionService(plugin, data, io);
        this.permissionSearcher = new TFIDFSearch(new String[]{"permissionName", "permissionDescription", "permissionDefault", "permissionChildren"});

        new BukkitRunnable() {
            @Override
            public void run() {
                startIndexer();
            }
        }.runTaskTimer(plugin, 0L, 6 * 60 * 20L);
    }

    public PermissionService getService() {
        return service;
    }

    public void setup() {
        if (!plugin.getConfig().getBoolean("Permissions.Enabled", true)) {
            plugin.getFileLogger().writeToLog(Level.INFO, "Permissions.Enabled is false: the permission manager does not hand out permissions",
                    LogPrefixExtension.PERMISSIONS, true);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            while (!repository.isDatabaseReady()) {
                if (!plugin.isEnabled()) return;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            try {
                migrateOldPermissions();
                service.start();
                if (plugin.getConfig().getBoolean("Permissions.Vault", false) && Bukkit.getPluginManager().getPlugin("Vault") != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        de.happybavarian07.adminpanel.permissions.vault.VaultHook.register(plugin, service);
                        vaultRegistered = true;
                        plugin.getFileLogger().writeToLog(Level.INFO, "Registered as the Vault permission and chat provider", LogPrefixExtension.PERMISSIONS, true);
                    });
                }
            } catch (RuntimeException e) {
                AdminPanelMain.logError("Could not start the permission manager", e);
            }
        });
    }

    public void shutdown() {
        if (vaultRegistered) de.happybavarian07.adminpanel.permissions.vault.VaultHook.unregister(plugin);
        service.stop();
    }

    // ---- search index

    public void onServerLoad(ServerLoadEvent event) {
        if (event.getType() == ServerLoadEvent.LoadType.STARTUP) startIndexer();
    }

    private synchronized void startIndexer() {
        if (permissionIndexThread != null && permissionIndexThread.isAlive()) return;
        permissionIndexThread = new Thread(this::indexPermissions, "Permission Indexer Thread");
        permissionIndexThread.setDaemon(true);
        permissionIndexThread.start();
    }

    private void indexPermissions() {
        long startTime = System.currentTimeMillis();
        List<Permission> permissions = new ArrayList<>(Bukkit.getPluginManager().getPermissions());
        List<TFIDFSearch.Item> items = new ArrayList<>();
        int hash = 1;
        for (Permission permission : permissions) {
            Map<String, Object> fields = new HashMap<>();
            fields.put("permissionName", permission.getName());
            fields.put("permissionDescription", permission.getDescription());
            fields.put("permissionDefault", permission.getDefault().toString());
            fields.put("permissionChildren", permission.getChildren().toString());
            items.add(new TFIDFSearch.Item(fields));
            hash += fields.hashCode();
        }
        if (permissionIndexFuture != null && hash == indexedHash) return;
        permissionIndexFuture = permissionSearcher.indexItems(items);
        permissionIndexFuture.join();
        indexedHash = hash;
        plugin.getFileLogger().writeToLog(Level.INFO, "Permission search index built for " + items.size() + " permissions in "
                + (System.currentTimeMillis() - startTime) + " ms", LogPrefixExtension.PERMISSIONS);
    }

    public TFIDFSearch getPermissionSearcher() {
        CompletableFuture<Void> running = permissionIndexFuture;
        if (running != null && !running.isDone()) {
            try {
                running.get();
            } catch (Exception e) {
                AdminPanelMain.logError("Could not wait for the permission search index", e);
            }
        }
        return permissionSearcher;
    }

    // ---- import of the old permissions.yml

    private void migrateOldPermissions() {
        File permissionFile = new File(plugin.getDataFolder(), "permissions.yml");
        if (!permissionFile.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(permissionFile);
        ConfigurationSection players = config.getConfigurationSection("Permissions");
        if (players != null) {
            for (String key : players.getKeys(false)) {
                ConfigurationSection entries = config.getConfigurationSection("Permissions." + key + ".Permissions");
                if (entries == null) continue;
                try {
                    UUID uuid = UUID.fromString(key);
                    for (String permission : entries.getKeys(false)) {
                        store.saveUserNode(uuid, Node.of(permission, entries.getBoolean(permission)));
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getFileLogger().writeToLog(Level.WARNING, "Invalid UUID in permissions.yml: " + key, LogPrefixExtension.PERMISSIONS, true);
                }
            }
        }
        File backup = new File(plugin.getDataFolder(), "permissions_old.yml");
        if (permissionFile.renameTo(backup)) {
            plugin.getFileLogger().writeToLog(Level.INFO, "Imported permissions.yml and renamed it to permissions_old.yml", LogPrefixExtension.PERMISSIONS, true);
        } else {
            plugin.getFileLogger().writeToLog(Level.WARNING, "Imported permissions.yml but could not rename it; it will be imported again next start",
                    LogPrefixExtension.PERMISSIONS, true);
        }
    }

    // ---- the methods the plugin always had

    public Map<UUID, PermissionAttachment> getPlayerPermissionsAttachments() {
        return service.attachments();
    }

    public void addPermission(UUID playerUUID, String permission, boolean value, boolean reloadPermissions) {
        if (reloadPermissions) {
            service.setUserPermission(null, playerUUID, permission, value, 0, Map.of());
        } else {
            service.data().setUserNode(playerUUID, Node.of(permission, value));
        }
    }

    public void addPermissions(UUID playerUUID, Map<String, Boolean> permissions, boolean reloadPermissions) {
        if (playerUUID == null || permissions.isEmpty()) return;
        permissions.forEach((permission, value) -> service.data().setUserNode(playerUUID, Node.of(permission, value)));
        if (reloadPermissions) reloadPermissions(Bukkit.getPlayer(playerUUID));
    }

    public void setPermissionValue(UUID playerUUID, String permission, boolean value, boolean reloadPermissions, boolean createIfNotExists) {
        if (createIfNotExists || hasEntry(playerUUID, permission)) addPermission(playerUUID, permission, value, reloadPermissions);
    }

    public void setPermissionValues(UUID playerUUID, Map<String, Boolean> permissions, boolean reloadPermissions, boolean createIfNotExists) {
        permissions.forEach((permission, value) -> {
            if (createIfNotExists || hasEntry(playerUUID, permission)) service.data().setUserNode(playerUUID, Node.of(permission, value));
        });
        if (reloadPermissions) reloadPermissions(Bukkit.getPlayer(playerUUID));
    }

    private boolean hasEntry(UUID playerUUID, String permission) {
        String normalized = Node.of(permission, true).permission();
        return service.data().userNodes(playerUUID).stream().anyMatch(node -> node.permission().equals(normalized) && node.context().isEmpty());
    }

    public void removePermission(UUID playerUUID, String permission, boolean reloadPermissions) {
        if (reloadPermissions) {
            service.unsetUserPermission(null, playerUUID, permission, Map.of());
        } else {
            service.data().unsetUserNode(playerUUID, permission, Map.of());
        }
    }

    public void removePermissions(UUID playerUUID, String[] permissions, boolean reloadPermissions) {
        for (String permission : permissions) service.data().unsetUserNode(playerUUID, permission, Map.of());
        if (reloadPermissions) reloadPermissions(Bukkit.getPlayer(playerUUID));
    }

    /** @return the plain permissions of a player (no groups, no entries limited to a context, nothing that ended) */
    public Map<String, Boolean> getPlayerPermissions(UUID playerUUID) {
        long now = System.currentTimeMillis();
        Map<String, Boolean> permissions = new TreeMap<>();
        for (Node node : service.data().userNodes(playerUUID)) {
            if (!node.isGroup() && node.context().isEmpty() && !node.expired(now)) permissions.put(node.permission(), node.value());
        }
        return permissions;
    }

    public Map<UUID, Map<String, Boolean>> getAllPlayerPermissions() {
        Map<UUID, Map<String, Boolean>> all = new HashMap<>();
        for (UUID user : service.knownUsers()) {
            Map<String, Boolean> permissions = getPlayerPermissions(user);
            if (!permissions.isEmpty()) all.put(user, permissions);
        }
        return all;
    }

    /** Works the permissions of a player out again and puts them on a fresh attachment. */
    public void reloadPermissions(Player player) {
        if (player != null) service.apply(player);
    }

    public void reloadPermissionsGlobally() {
        service.applyAll();
    }

    public void clearPermissions(UUID playerUUID) {
        service.clearUser(null, playerUUID);
    }

    public boolean hasPermissions(UUID permissionHolder) {
        return !service.data().userNodes(permissionHolder).isEmpty();
    }

    public void loadPlayerPermissions(Player player) {
        if (player != null) service.onJoin(player);
    }

    public void unloadPlayerPermissions(Player player) {
        if (player != null) service.onQuit(player);
    }
}
