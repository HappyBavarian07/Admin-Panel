package de.happybavarian07.adminpanel.utils.managers;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.service.api.DataService;
import de.happybavarian07.adminpanel.utils.LogPrefixExtension;
import de.happybavarian07.adminpanel.utils.NewUpdater;
import de.happybavarian07.adminpanel.utils.PluginUtils;
import de.happybavarian07.adminpanel.utils.VersionComparator;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

/*
 * @Author HappyBavarian07
 * @Date September 10, 2024 | 16:20
 */
public class AutoUpdaterManager {
    private final Map<String, NewUpdater> autoUpdaterPlugins;
    private final File dataFile;
    private final AdminPanelMain plugin;
    private FileConfiguration dataYML;

    public AutoUpdaterManager(AdminPanelMain plugin) {
        this.plugin = plugin;
        this.autoUpdaterPlugins = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        this.dataYML = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void setup(NewUpdater adminPanelUpdater) {
        new Thread(() -> {
            plugin.getAutoUpdaterManager().getAutoUpdaterPlugins().clear();
            plugin.getStartUpLogger().coloredSpacer(ChatColor.BLUE);
            plugin.getStartUpLogger().message("&1&lAuto Plugin Updater initiated&r");
            DataService ds = null;
            try {
                ds = AdminPanelMain.getPlugin().getDataService();
            } catch (Exception ignored) {
            }
            for (String sectionString : Objects.requireNonNull(dataYML.getConfigurationSection("PluginsToUpdate")).getKeys(false)) {
                if (!dataYML.isConfigurationSection("PluginsToUpdate." + sectionString)) continue;

                ConfigurationSection section = dataYML.getConfigurationSection("PluginsToUpdate." + sectionString);
                assert section != null;
                if (section.getInt("spigotID", -1) == -1 || section.getString("fileName", "").isEmpty())
                    continue;

                handleNewUpdater(sectionString, section, adminPanelUpdater);

                if (ds != null) {
                    try {
                        Map<String, Object> meta = ds.loadMap("updater.meta." + sectionString, Object.class).join();
                        if (meta != null) {
                            NewUpdater nu = autoUpdaterPlugins.get(sectionString);
                            if (nu != null) {
                                Object latest = meta.get("latestVersion");
                                if (latest != null) {

                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            if (plugin.getConfig().getBoolean("Plugin.Updater.PluginUpdater.checkForUpdatesFrequently")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getAutoUpdaterManager().getAutoUpdaterPlugins().clear();
                        for (String sectionString : Objects.requireNonNull(dataYML.getConfigurationSection("PluginsToUpdate")).getKeys(false)) {
                            if (!dataYML.isConfigurationSection("PluginsToUpdate." + sectionString)) continue;

                            ConfigurationSection section = dataYML.getConfigurationSection("PluginsToUpdate." + sectionString);
                            assert section != null;
                            if (section.getInt("spigotID", -1) == -1 || section.getString("fileName", "").isEmpty())
                                continue;

                            handleNewUpdater(sectionString, section, adminPanelUpdater);
                        }
                    }
                }.runTaskTimer(plugin, (plugin.getConfig().getLong("Plugin.Updater.PluginUpdater.UpdateCheckTime") * 60 * 20), (plugin.getConfig().getLong("Plugin.Updater.PluginUpdater.UpdateCheckTime") * 60 * 20));
            }
        }).start();
        plugin.getStartUpLogger().coloredSpacer(ChatColor.BLUE);
    }

    private void handleNewUpdater(String sectionString, ConfigurationSection section, NewUpdater updater) {
        int spigotID = section.getInt("spigotID");
        String fileName = section.getString("fileName");
        boolean bypassExternalURL = section.getBoolean("bypassExternalDownload");

        assert fileName != null;
        if (!fileName.endsWith(".jar")) return;

        NewUpdater tempUpdater = new NewUpdater(plugin, spigotID, fileName, (JavaPlugin) new PluginUtils().getPluginByName(sectionString), section.getString("link", ""), bypassExternalURL);
        plugin.getAutoUpdaterManager().getAutoUpdaterPlugins().put(sectionString, tempUpdater);
        if (!tempUpdater.resourceIsOnSpigot()) return;
        if (tempUpdater.isExternalFile() && tempUpdater.getLinkToFile().isEmpty() && !updater.bypassExternalURL()) {
            if (!plugin.getConfig().getBoolean("Plugin.Updater.logNoUpdate")) return;
            plugin.getStartUpLogger().message("Plugin: " + tempUpdater.getPluginName() + " is external and the Plugin will not download it!");
            tempUpdater.checkForUpdatesAsync(true, (available, latest) -> {
            });
            return;

        }
        if ((tempUpdater.getPluginName() == null) && plugin.getConfig().getBoolean("Plugin.Updater.PluginUpdater.downloadIfNotExists")) {
            tempUpdater.downloadLatestUpdateAsync(true, true, false, r -> {
            });
            return;
        }

        tempUpdater.setVersionComparator(VersionComparator.SEMANTIC_VERSION);
        tempUpdater.checkForUpdatesAsync(true, (available, latest) -> {
            if (available) {
                tempUpdater.downloadLatestUpdateAsync(
                        plugin.getConfig().getBoolean("Plugin.Updater.PluginUpdater.automaticReplace"),
                        plugin.getConfig().getBoolean("Plugin.Updater.PluginUpdater.downloadPluginUpdate"),
                        true,
                        r -> {
                        }
                );
            }
            // Persist small metadata about this plugin updater
            try {
                DataService ds = AdminPanelMain.getPlugin().getDataService();
                if (ds != null) {
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("lastCheckMillis", System.currentTimeMillis());
                    meta.put("latestVersion", latest);
                    meta.put("available", available);
                    ds.save("updater.meta." + sectionString, meta);
                }
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * Removes a Plugin from the AutoUpdater
     *
     * @param selectedPlugin The Plugin
     */
    public void removePluginFromUpdater(Plugin selectedPlugin) {
        dataYML.set("PluginsToUpdate." + selectedPlugin.getName(), null);

        try {
            dataYML.save(new File(plugin.getDataFolder() + "/data.yml"));
        } catch (IOException e) {
            plugin.getStartUpLogger().message("Failed to save data.yml while removing plugin: " + e.getMessage());
        }
        autoUpdaterPlugins.remove(selectedPlugin.getName());
    }

    public void addPluginToUpdater(Plugin pl, int spigotID, String fileName) {
        String path = "PluginsToUpdate." + pl.getName() + ".";
        dataYML.set(path + "spigotID", spigotID);
        dataYML.set(path + "fileName", fileName);
        dataYML.set(path + "link", "");
        dataYML.set(path + "bypassExternalDownload", false);

        try {
            dataYML.save(new File(AdminPanelMain.getPlugin().getDataFolder() + "/data.yml"));
        } catch (IOException e) {
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE, "Failed to save data.yml while adding plugin: " + e.getMessage(), LogPrefixExtension.UPDATER);
        }
        NewUpdater updater = new NewUpdater(AdminPanelMain.getPlugin(), spigotID, fileName, (JavaPlugin) pl, "", false);
        autoUpdaterPlugins.put(pl.getName(), updater);
    }

    /**
     * Returns the Updater of the Plugin
     *
     * @param plugin The Plugin
     * @return The Updater of the Plugin
     */
    public NewUpdater getPluginUpdater(Plugin plugin) {
        return autoUpdaterPlugins.get(plugin.getName());
    }

    /**
     * Returns the Updater of the Plugin
     *
     * @param pluginName The Name of the Plugin
     * @return The Updater of the Plugin
     */
    public NewUpdater getPluginUpdater(String pluginName) {
        return autoUpdaterPlugins.get(pluginName);
    }

    /**
     * Saves the Permissions to the Permissions.yml File
     */
    public void reloadData() {
        plugin.saveResource("data.yml", false);
        dataYML = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + "/data.yml"));
    }

    public Map<String, NewUpdater> getAutoUpdaterPlugins() {
        return autoUpdaterPlugins;
    }
}
