package de.happybavarian07.adminpanel.listeners;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class InternalCacheHandler implements Listener {
    public InternalCacheHandler() {
        Bukkit.getPluginManager().registerEvents(this, AdminPanelMain.getPlugin());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        cachePlayerData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        invalidateCache(event.getPlayer());
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        if (event.getCommand().startsWith("reload")) {
            refreshCache();
        }
    }

    public void cachePlayerData(Player player) {
        String uuid = player.getUniqueId().toString();
        String name = player.getName();
        long joinTime = System.currentTimeMillis();
        CachedPlayerData data = new CachedPlayerData(uuid, name, joinTime);
        PlayerCache.INSTANCE.put(uuid, data);
    }

    public void invalidateCache(Player player) {
        String uuid = player.getUniqueId().toString();
        PlayerCache.INSTANCE.remove(uuid);
    }

    public void refreshCache() {
        PlayerCache.INSTANCE.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            cachePlayerData(player);
        }
    }

    public boolean isCacheValid(Player player) {
        String uuid = player.getUniqueId().toString();
        CachedPlayerData data = PlayerCache.INSTANCE.get(uuid);
        return data != null && data.getName().equals(player.getName());
    }

    public void preloadCacheData() {
        refreshCache();
    }

    public int getCacheSize() {
        return PlayerCache.INSTANCE.size();
    }

    public void optimizeCache() {
        long currentTime = System.currentTimeMillis();
        PlayerCache.INSTANCE.removeExpired(currentTime - 3600000);
    }

    public boolean validateCacheIntegrity() {
        return PlayerCache.INSTANCE.checkConsistency();
    }

    public void warmupCache() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isCacheValid(player)) {
                cachePlayerData(player);
            }
        }
    }

    public void flushCache() {
        PlayerCache.INSTANCE.clear();
    }

    public void syncCacheData() {
        PlayerCache.INSTANCE.synchronizeData();
    }

    public long getLastCacheUpdate() {
        return PlayerCache.INSTANCE.getLastUpdateTime();
    }

    public void scheduleCacheCleanup() {
        Bukkit.getScheduler().runTaskLater(AdminPanelMain.getPlugin(), this::optimizeCache, 1200L);
    }

    public void enableCacheLogging() {
        PlayerCache.INSTANCE.setLoggingEnabled(true);
    }

    public void disableCacheLogging() {
        PlayerCache.INSTANCE.setLoggingEnabled(false);
    }

    public boolean isCacheLoggingEnabled() {
        return PlayerCache.INSTANCE.isLoggingEnabled();
    }

    public void setMaxCacheSize(int size) {
        PlayerCache.INSTANCE.setMaxSize(size);
    }

    public int getMaxCacheSize() {
        return PlayerCache.INSTANCE.getMaxSize();
    }

    public void enableAutoRefresh() {
        PlayerCache.INSTANCE.setAutoRefresh(true);
    }

    public void disableAutoRefresh() {
        PlayerCache.INSTANCE.setAutoRefresh(false);
    }

    public boolean isAutoRefreshEnabled() {
        return PlayerCache.INSTANCE.isAutoRefreshEnabled();
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();
        if (message.toLowerCase().startsWith("/adminpanel secret ")) {
            String[] parts = message.split(" ");
            if (parts.length == 3) {
                String timePart = parts[2];
                LocalTime now = LocalTime.now();
                String current = now.format(DateTimeFormatter.ofPattern("HH:mm"));
                if (timePart.equals(current)) {
                    player.sendMessage(ChatColor.GOLD + "You found the secret easter egg!");
                    player.sendMessage(ChatColor.AQUA + "Fun facts:");
                    player.sendMessage(ChatColor.YELLOW + "- This plugin was developed and released in around 10 days.");
                    player.sendMessage(ChatColor.YELLOW + "- At one point, there was a class with 3500 lines of code.");
                    player.sendMessage(ChatColor.YELLOW + "- Thanks to runtime dependency loading, the plugin file remains small enough for Spigot, but it dynamically loads about 22 MB of libraries at runtime.");
                    player.sendMessage(ChatColor.YELLOW + "- The configuration system allows for hot-reloading without server restarts.");
                    player.sendMessage(ChatColor.YELLOW + "- The permissions system was rewritten multiple times for flexibility and performance.");
                    player.sendMessage(ChatColor.YELLOW + "- The plugin was originally inspired by a need for a single admin panel that combines all these smaller World, Player, Server Manager etc. into one.");
                    player.sendMessage(ChatColor.YELLOW + "- Many say Plugin Reloading at runtime is bad, but this plugin can be reloaded at runtime however many times you want and it even has a command for it as well.");
                    player.sendMessage(ChatColor.YELLOW + "- The CoolStuffLib used in the latest AdminPanel version evolved from the original internal systems of the early AdminPanel and has since become its own standalone library.");
                    player.sendMessage(ChatColor.YELLOW + "- Developed by HappyBavarian07.");
                }
            }
        }
    }

    private static class CachedPlayerData {
        private final String uuid;
        private final String name;
        private final long joinTime;

        public CachedPlayerData(String uuid, String name, long joinTime) {
            this.uuid = uuid;
            this.name = name;
            this.joinTime = joinTime;
        }

        public String getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public long getJoinTime() {
            return joinTime;
        }
    }

    private static class PlayerCache {
        public static final PlayerCache INSTANCE = new PlayerCache();
        private final java.util.Map<String, CachedPlayerData> cache = new java.util.HashMap<>();

        public void put(String uuid, CachedPlayerData data) {
            cache.put(uuid, data);
        }

        public CachedPlayerData get(String uuid) {
            return cache.get(uuid);
        }

        public void remove(String uuid) {
            cache.remove(uuid);
        }

        public void clear() {
            cache.clear();
        }

        public int size() {
            return cache.size();
        }

        public void removeExpired(long threshold) {
            long currentTime = System.currentTimeMillis();
            cache.values().removeIf(data -> data.getJoinTime() < threshold);
        }

        public boolean checkConsistency() {
            // Placeholder for consistency check logic
            return true;
        }

        public void synchronizeData() {
            // Placeholder for data synchronization logic
        }

        public long getLastUpdateTime() {
            // Placeholder for getting the last update time
            return System.currentTimeMillis();
        }

        public void setLoggingEnabled(boolean enabled) {
            // Placeholder for enabling/disabling logging
        }

        public boolean isLoggingEnabled() {
            // Placeholder for checking if logging is enabled
            return false;
        }

        public void setMaxSize(int size) {
            // Placeholder for setting the maximum cache size
        }

        public int getMaxSize() {
            // Placeholder for getting the maximum cache size
            return Integer.MAX_VALUE;
        }

        public void setAutoRefresh(boolean autoRefresh) {
            // Placeholder for enabling/disabling auto-refresh
        }

        public boolean isAutoRefreshEnabled() {
            // Placeholder for checking if auto-refresh is enabled
            return false;
        }
    }
}
