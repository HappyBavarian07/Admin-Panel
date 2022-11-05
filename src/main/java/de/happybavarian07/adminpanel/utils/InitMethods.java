package de.happybavarian07.adminpanel.utils;/*
 * @Author HappyBavarian07
 * @Date 25.04.2022 | 21:34
 */

import de.happybavarian07.adminpanel.addonloader.api.Addon;
import de.happybavarian07.adminpanel.addonloader.api.Dependency;
import de.happybavarian07.adminpanel.addonloader.loadingutils.AddonLoader;
import de.happybavarian07.adminpanel.addonloader.utils.FileUtils;
import de.happybavarian07.adminpanel.commands.AdminPanelOpenCommand;
import de.happybavarian07.adminpanel.commands.LanguageReloadCommand;
import de.happybavarian07.adminpanel.commands.PerPlayerLanguageCommand;
import de.happybavarian07.adminpanel.commands.UpdateCommand;
import de.happybavarian07.adminpanel.commands.managers.AdminPanelAdminManager;
import de.happybavarian07.adminpanel.commands.managers.PanelOpenManager;
import de.happybavarian07.adminpanel.listeners.MenuListener;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.LanguageFile;
import de.happybavarian07.adminpanel.main.LanguageManager;
import de.happybavarian07.adminpanel.main.Metrics;
import de.happybavarian07.adminpanel.placeholders.PanelExpansion;
import de.happybavarian07.adminpanel.placeholders.PlayerExpansion;
import de.happybavarian07.adminpanel.placeholders.PluginExpansion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class InitMethods {
    private final StartUpLogger logger;
    private final PluginManager pluginManager;
    private final AdminPanelMain plugin;
    private final LanguageManager languageManager;

    public InitMethods(StartUpLogger logger, PluginManager pluginManager, AdminPanelMain plugin) {
        this.logger = logger;
        this.pluginManager = pluginManager;
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
    }

    public void initUpdater(NewUpdater updater, FileConfiguration dataYML) {
        updater.setVersionComparator(VersionComparator.SEMATIC_VERSION);
        if (plugin.getConfig().getBoolean("Plugin.Updater.checkForUpdates")) {
            updater.checkForUpdates(true);
            if (updater.updateAvailable()) {
                updater.downloadLatestUpdate(plugin.getConfig().getBoolean("Plugin.Updater.automaticReplace"), plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate"), true);
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    updater.checkForUpdates(false);
                    if (updater.updateAvailable()) {
                        updater.downloadLatestUpdate(plugin.getConfig().getBoolean("Plugin.Updater.automaticReplace"), plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate"), true);
                    }
                }
            }.runTaskTimer(plugin, (plugin.getConfig().getLong("Plugin.Updater.UpdateCheckTime") * 60 * 20), (plugin.getConfig().getLong("Plugin.Updater.UpdateCheckTime") * 60 * 20));
        }
        if (plugin.getConfig().getBoolean("Plugin.Updater.PluginUpdater.enabled")) {
            new Thread(() -> {
                plugin.getAutoUpdaterPlugins().clear();
                logger.coloredSpacer(ChatColor.BLUE);
                logger.message("&1&lAuto Plugin Updater initiated&r");
                for (String sectionString : dataYML.getConfigurationSection("PluginsToUpdate").getKeys(false)) {
                    if (!dataYML.isConfigurationSection("PluginsToUpdate." + sectionString)) continue;

                    ConfigurationSection section = dataYML.getConfigurationSection("PluginsToUpdate." + sectionString);
                    assert section != null;
                    if (section.getInt("spigotID", -1) == -1 || section.getString("fileName", "").equals(""))
                        continue;

                    int spigotID = section.getInt("spigotID");
                    String fileName = section.getString("fileName");
                    boolean bypassExternalURL = section.getBoolean("bypassExternalDownload");

                    assert fileName != null;
                    if (!fileName.endsWith(".jar")) continue;

                    NewUpdater tempUpdater = new NewUpdater(plugin, spigotID, fileName, (JavaPlugin) new PluginUtils().getPluginByName(sectionString), section.getString("link", ""), bypassExternalURL);
                    plugin.getAutoUpdaterPlugins().put(sectionString, tempUpdater);
                    if (!tempUpdater.resourceIsOnSpigot()) continue;
                    if (tempUpdater.isExternalFile() && tempUpdater.getLinkToFile().equals("") && !updater.bypassExternalURL()) {
                        if(!plugin.getConfig().getBoolean("Plugin.Updater.logNoUpdate")) continue;
                        logger.message("Plugin: " + tempUpdater.getPluginName() + " is external and the Plugin will not download it!");
                        tempUpdater.checkForUpdates(true);
                        continue;
                    }
                    if ((tempUpdater.getPluginName() == null) && plugin.getConfig().getBoolean("Plugin.Updater.PluginUpdater.downloadIfNotExists")) {
                        tempUpdater.downloadLatestUpdate(true, true, false);
                        continue;
                    }

                    tempUpdater.setVersionComparator(VersionComparator.SEMATIC_VERSION);
                    tempUpdater.checkForUpdates(true);
                    if (tempUpdater.updateAvailable()) {
                        tempUpdater.downloadLatestUpdate(plugin.getConfig().getBoolean("Plugin.Updater.PluginUpdater.automaticReplace"),
                                plugin.getConfig().getBoolean("Plugin.Updater.PluginUpdater.downloadPluginUpdate"), true);
                    }
                }
                if (plugin.getConfig().getBoolean("Plugin.Updater.PluginUpdater.checkForUpdatesFrequently")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            plugin.getAutoUpdaterPlugins().clear();
                            for (String sectionString : dataYML.getConfigurationSection("PluginsToUpdate").getKeys(false)) {
                                if (!dataYML.isConfigurationSection("PluginsToUpdate." + sectionString)) continue;

                                ConfigurationSection section = dataYML.getConfigurationSection("PluginsToUpdate." + sectionString);
                                assert section != null;
                                if (section.getInt("spigotID", -1) == -1 || section.getString("fileName", "").equals(""))
                                    continue;

                                int spigotID = section.getInt("spigotID");
                                String fileName = section.getString("fileName");
                                boolean bypassExternalURL = section.getBoolean("bypassExternalDownload");

                                assert fileName != null;
                                if (!fileName.endsWith(".jar")) continue;

                                NewUpdater tempUpdater = new NewUpdater(plugin, spigotID, fileName, (JavaPlugin) new PluginUtils().getPluginByName(sectionString), section.getString("link", ""), bypassExternalURL);
                                plugin.getAutoUpdaterPlugins().put(sectionString, tempUpdater);
                                if (!tempUpdater.resourceIsOnSpigot()) continue;
                                if (tempUpdater.isExternalFile() && tempUpdater.getLinkToFile().equals("") && !updater.bypassExternalURL()) {
                                    if(!plugin.getConfig().getBoolean("Plugin.Updater.logNoUpdate")) continue;
                                    logger.message("Plugin: " + tempUpdater.getPluginName() + " is external and the Plugin will not download it!");
                                    tempUpdater.checkForUpdates(true);
                                    continue;

                                }
                                if ((tempUpdater.getPluginName() == null) && plugin.getConfig().getBoolean("Plugin.Updater.PluginUpdater.downloadIfNotExists")) {
                                    tempUpdater.downloadLatestUpdate(true, true, false);
                                    continue;
                                }

                                tempUpdater.setVersionComparator(VersionComparator.SEMATIC_VERSION);
                                tempUpdater.checkForUpdates(true);
                                if (tempUpdater.updateAvailable()) {
                                    tempUpdater.downloadLatestUpdate(plugin.getConfig().getBoolean("Plugin.Updater.PluginUpdater.automaticReplace"),
                                            plugin.getConfig().getBoolean("Plugin.Updater.PluginUpdater.downloadPluginUpdate"), true);
                                }
                            }
                        }
                    }.runTaskTimer(plugin, (plugin.getConfig().getLong("Plugin.Updater.PluginUpdater.UpdateCheckTime") * 60 * 20), (plugin.getConfig().getLong("Plugin.Updater.PluginUpdater.UpdateCheckTime") * 60 * 20));
                }
            }).start();
            logger.coloredSpacer(ChatColor.BLUE);
        }
    }

    public void initPermissionFiles(FileConfiguration permissionsConfig, File permissionFile, Map<UUID, Map<String, Boolean>> playerPermissions) {
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

    public void initPermissions(Map<UUID, PermissionAttachment> playerPermissionsAttachments, Map<UUID, Map<String, Boolean>> playerPermissions) {
        for(String configSection : plugin.getPermissionsConfig().getConfigurationSection("Permissions").getKeys(false)) {
            UUID tempUUID = UUID.fromString(configSection);
            if(!playerPermissionsAttachments.containsKey(tempUUID)) {
                if (!playerPermissions.containsKey(tempUUID)) {
                    playerPermissions.put(tempUUID, new HashMap<>());
                }
                Map<String, Boolean> permissions = new HashMap<>();
                for(String permissionName : plugin.getPermissionsConfig().getConfigurationSection("Permissions." + configSection + ".Permissions").getKeys(true)) {
                    if(plugin.getPermissionsConfig().isConfigurationSection("Permissions." + configSection + ".Permissions." + permissionName)) continue;

                    permissions.put(permissionName, plugin.getPermissionsConfig().getBoolean("Permissions." + configSection + ".Permissions." + permissionName));
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
                        for (String perms : permissions.keySet()) {
                            attachment.setPermission(perms, permissions.get(perms));
                        }
                        playerPermissionsAttachments.put(online.getUniqueId(), attachment);
                        online.recalculatePermissions();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 180);
    }

    public void initCommands() {
        try {
            Objects.requireNonNull(plugin.getCommand("update")).setExecutor(new UpdateCommand());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(plugin.getCommand("adminpanel")).setExecutor(new AdminPanelOpenCommand());
        PerPlayerLanguageCommand perPLanguageCommand = new PerPlayerLanguageCommand();
        Objects.requireNonNull(plugin.getCommand("perplayerlang")).setExecutor(perPLanguageCommand);
        Objects.requireNonNull(plugin.getCommand("perplayerlang")).setTabCompleter(perPLanguageCommand);
        Objects.requireNonNull(plugin.getCommand("reloadlang")).setExecutor(new LanguageReloadCommand());
    }

    public void initbStatsMetrics(Metrics metrics) {
        metrics.addCustomChart(new Metrics.SimplePie("used_language", () -> languageManager.getCurrentLang().getLangName()));
        metrics.addCustomChart(new Metrics.SimplePie("language_count", () -> {
            int value = 0;
            for (LanguageFile lang : languageManager.getRegisteredLanguages().values()) {
                if (lang.getPlugin() == plugin)
                    value++;
            }
            return String.valueOf(value);
        }));
        metrics.addCustomChart(new Metrics.SimplePie("external_api_language_count", () -> {
            int value = 0;
            for (LanguageFile lang : languageManager.getRegisteredLanguages().values()) {
                if (lang.getPlugin() != plugin)
                    value++;
            }
            return String.valueOf(value);
        }));
        /*metrics.addCustomChart(new Metrics.AdvancedBarChart("exampleBar", () -> {
            Map<String, int[]> map = new HashMap<>();
            map.put("Addon System", plugin.isAddonSystemEnabled() ? new int[]{0, 1} : new int[]{1, 0});
            map.put("Updater", plugin.isUpdaterEnabled() ? new int[]{0, 1} : new int[]{1, 0});
            map.put("Plugin Updater", plugin.isPluginUpdaterEnabled() ? new int[]{0, 1} : new int[]{1, 0});
            return map;
        }));*/
        metrics.addCustomChart(new Metrics.SimplePie("servers_with_addonsystem", () -> String.valueOf(plugin.isAddonSystemEnabled())));
        metrics.addCustomChart(new Metrics.SimplePie("servers_with_updater", () -> String.valueOf(plugin.isUpdaterEnabled())));
        metrics.addCustomChart(new Metrics.SimplePie("servers_with_pluginupdater", () -> String.valueOf(plugin.isPluginUpdaterEnabled())));
        metrics.addCustomChart(new Metrics.SimplePie("servers_with_plugin_replace_enabled", () -> String.valueOf(plugin.isUpdateReplacerEnabled())));
        if (!plugin.getLanguageManager().getPlhandler().getPlayerLanguages().isEmpty()) {
            metrics.addCustomChart(new Metrics.SimplePie("most_used_player_lang", () -> {
                Map<UUID, LanguageFile> playerLangs = plugin.getLanguageManager().getPlhandler().getPlayerLanguages();
                Map<LanguageFile, Integer> popularityMap = new HashMap<>();
                for (LanguageFile i : playerLangs.values()) {
                    Integer count = popularityMap.get(i);
                    popularityMap.put(i, count != null ? count + 1 : 1);
                }
                LanguageFile popular = Collections.max(popularityMap.entrySet(),
                        Map.Entry.comparingByValue()).getKey();
                return popular.getLangName();
            }));
        }
    }

    public LanguageFile getMostUsedPlayerLang() {
        Map<UUID, LanguageFile> playerLangs = plugin.getLanguageManager().getPlhandler().getPlayerLanguages();
        Map<LanguageFile, Integer> popularityMap = new HashMap<>();
        for (LanguageFile i : playerLangs.values()) {
            Integer count = popularityMap.get(i);
            popularityMap.put(i, count != null ? count + 1 : 1);
        }
        return Collections.max(popularityMap.entrySet(),
                Map.Entry.comparingByValue()).getKey();
    }

    public void initPluginCheck() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlayerExpansion().register();
            new PluginExpansion().register();
            new PanelExpansion().register();
            logger.message("&a&lInitialized PlaceHolderAPI with Placeholders!&r");
        } else {
            logger.spacer()
                    .coloredMessage(ChatColor.RED, "")
                    .coloredMessage(ChatColor.RED, "No PlaceholderAPI found please install PlaceholderAPI before starting again!")
                    .coloredMessage(ChatColor.RED, "The Plugin cannot work without this Plugin!")
                    .coloredMessage(ChatColor.RED, "");
            pluginManager.disablePlugin(plugin);
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("SuperVanish") == null) {
            logger.spacer()
                    .coloredMessage(ChatColor.RED, "")
                    .coloredMessage(ChatColor.RED, "No SuperVanish found please install SuperVanish,")
                    .coloredMessage(ChatColor.RED, "if you want to use the Vanish Feature!")
                    .coloredMessage(ChatColor.RED, "");
        }
        logger.coloredSpacer(ChatColor.DARK_RED).messages("&c&lStarting Vault initialization!&r");
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            logger.spacer()
                    .coloredMessage(ChatColor.RED, "")
                    .coloredMessage(ChatColor.RED, "No Vault found please install Vault and an Economy Plugin,")
                    .coloredMessage(ChatColor.RED, "if you want to use the Money Features!")
                    .coloredMessage(ChatColor.RED, "");
        } else {
            setupEconomy();
        }
    }

    public void initConfigFiles(PluginFileLogger fileLogger, File permissionFile) {
        if (!plugin.getConfigFile().exists()) {
            logger.coloredSpacer(ChatColor.DARK_RED).message("&c&lCreating Default Config!&r");
        }
        plugin.saveDefaultConfig();
        if (!plugin.getConfigFile().exists()) {
            logger.message("&e&lDone!&r");
        }
        if (!new File(plugin.getDataFolder() + "/data.yml").exists()) {
            logger.spacer().message("&c&lCreating data.yml file!&r");
            plugin.saveResource("data.yml", false);
            logger.message("&e&lDone!&r");
        }
        if (!fileLogger.getLogFile().exists()) {
            logger.spacer().message("&c&lCreating plugin.log file!&r");
            fileLogger.createLogFile();
            logger.message("&e&lDone!&r");
        }
        if (!permissionFile.exists()) {
            logger.spacer().message("&c&lCreating permissions.yml file!&r");
            try {
                permissionFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.message("&e&lDone!&r");
        }
    }

    public void initAddonLoader(AddonLoader loader) {
        new Thread(() -> {
            logger.coloredMessage(ChatColor.GREEN, "Done with Phase 1/3! (Adding)");
            logger.coloredSpacer(ChatColor.BLUE);
            logger.emptySpacer();

            File[] temp = loader.getAddonFolder().listFiles(pathname -> pathname.getName().endsWith(".jar"));

            if (!loader.getAddonFolder().exists() || temp == null || temp.length == 0) {
                logger.coloredSpacer(ChatColor.RED);
                logger.message("&cThere are no Addons in that Folder! Stopping the Addon Loader!&r");
                logger.coloredSpacer(ChatColor.RED);
                loader.crashAddons();
                return;
            }

            logger.coloredSpacer(ChatColor.BLUE);
            logger.coloredMessage(ChatColor.GREEN, "Loading Addons!");

            for (File jarFile : loader.getLoadedJarFiles().keySet()) {
                plugin.getLogger().log(Level.INFO, "Loading Addon Jar File: " + jarFile.getName());
                try {
                    loader.loadAddon(jarFile);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            logger.coloredMessage(ChatColor.GREEN, "Done with Phase 2/3! (Loading)");
            logger.coloredSpacer(ChatColor.BLUE);
            logger.emptySpacer();
            logger.coloredSpacer(ChatColor.BLUE);
            logger.coloredMessage(ChatColor.GREEN, "Enabling Addons!");

            for (File addonFile : loader.getLoadedJarFiles().keySet()) {
                try {
                    //System.out.println("File: " + addonFile);
                    final Class<? extends Addon> addonClass = FileUtils.findClass(addonFile, Addon.class);

                    if (addonClass == null) {
                        //System.out.println("Addon Class is null!");
                        continue;
                    }

                    Addon addon = addonClass.getConstructor().newInstance();
                    //System.out.println("Resource:" + addon);

                    boolean allowedToStart = true;
                    if (!addon.getDependencies().isEmpty()) {
                        for (Dependency dependency : addon.getDependencies()) {
                            if (!Bukkit.getPluginManager().isPluginEnabled(dependency.getName())) {
                                allowedToStart = false;
                                logger.coloredMessage(ChatColor.DARK_RED, "Dependency Error enabling Addon: " + addon.getName());
                                logger.coloredMessage(ChatColor.DARK_RED, "Please install the following Dependency '" + dependency.getName() + "' (Link: " + dependency.getLink() + ")!");
                            }
                        }
                    }
                    if (allowedToStart) {
                        plugin.getLogger().log(Level.INFO, "Enabled Addon: " + addon.getName());
                        addon.onEnable();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            logger.coloredMessage(ChatColor.GREEN, "Done with Phase 3/3! (Enabling)");
            logger.coloredSpacer(ChatColor.BLUE);
        }).start();
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economy = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (economy != null)
            plugin.eco = economy.getProvider();
        return plugin.eco != null;
    }

    public void initEvents() {
        logger.coloredSpacer(ChatColor.DARK_RED).message("&2&lStarting Region of Evnts:&r");
        PluginManager pm = plugin.getServer().getPluginManager();
        logger.message("&3&lLoading Menu Listener Events!&r");
        pm.registerEvents(new MenuListener(), plugin);
        logger.message("&3&lLoading Main Class Listener Events!&r");
        pm.registerEvents(plugin, plugin);
        logger.message("&4&lEventregistration: Done!&r");
        logger.coloredSpacer(ChatColor.DARK_RED).message("&e&lStarting Done!&r");
        logger.coloredSpacer(ChatColor.GREEN);
    }

    public void initCommandManagers() {
        plugin.getCommandManagerRegistry().register(new PanelOpenManager());
        plugin.getCommandManagerRegistry().register(new AdminPanelAdminManager());
    }
}
