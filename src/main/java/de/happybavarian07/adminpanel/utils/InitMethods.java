package de.happybavarian07.adminpanel.utils;

import de.happybavarian07.adminpanel.listeners.DisabledCommandsListener;
import de.happybavarian07.adminpanel.startup.Startup;
import de.happybavarian07.coolstufflib.utils.StartUpLogger;

/*
 * @Author HappyBavarian07
 * @Date 25.04.2022 | 21:34
 */

import de.happybavarian07.adminpanel.addonloader.api.Addon;
import de.happybavarian07.adminpanel.addonloader.loadingutils.AddonLoader;
import de.happybavarian07.adminpanel.commands.AdminPanelOpenCommand;
import de.happybavarian07.adminpanel.commands.LanguageReloadCommand;
import de.happybavarian07.adminpanel.commands.PerPlayerLanguageCommand;
import de.happybavarian07.adminpanel.commands.UpdateCommand;
import de.happybavarian07.adminpanel.commands.managers.AddonCommandManager;
import de.happybavarian07.adminpanel.commands.managers.AdminPanelAdminManager;
import de.happybavarian07.adminpanel.commands.managers.DataClientCommandManager;
import de.happybavarian07.adminpanel.commands.managers.DisabledItemsManager;
import de.happybavarian07.adminpanel.commands.managers.PanelOpenManager;
import de.happybavarian07.adminpanel.commands.managers.PermsManager;
import de.happybavarian07.adminpanel.commands.managers.UnwarnManager;
import de.happybavarian07.adminpanel.commands.managers.WarnManager;
import de.happybavarian07.adminpanel.commands.managers.WarningsManager;
import de.happybavarian07.adminpanel.listeners.DisabledItemsListener;
import de.happybavarian07.adminpanel.listeners.ChatMuteListener;
import de.happybavarian07.adminpanel.listeners.MaintenanceListener;
import de.happybavarian07.adminpanel.listeners.MenuListener;
import de.happybavarian07.adminpanel.listeners.PlayerEventHandler;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.Metrics;
import de.happybavarian07.adminpanel.placeholders.DataClientExpansion;
import de.happybavarian07.adminpanel.placeholders.PanelExpansion;
import de.happybavarian07.adminpanel.placeholders.PlayerExpansion;
import de.happybavarian07.adminpanel.placeholders.PluginExpansion;
import de.happybavarian07.coolstufflib.languagemanager.LanguageFile;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import de.happybavarian07.coolstufflib.utils.PluginFileLogger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
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
        updater.setVersionComparator(VersionComparator.SEMANTIC_VERSION);
        if (plugin.getConfig().getBoolean("Plugin.Updater.checkForUpdates")) {
            updater.checkForUpdatesAsync(true, (available, latest) -> {
                if (available) {
                    updater.downloadLatestUpdateAsync(
                            plugin.getConfig().getBoolean("Plugin.Updater.automaticReplace"),
                            plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate"),
                            true,
                            r -> {
                            }
                    );
                }
            });
            new BukkitRunnable() {
                @Override
                public void run() {
                    updater.checkForUpdatesAsync(false, (available, latest) -> {
                        if (available) {
                            updater.downloadLatestUpdateAsync(
                                    plugin.getConfig().getBoolean("Plugin.Updater.automaticReplace"),
                                    plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate"),
                                    true,
                                    r -> {
                                    }
                            );
                        }
                    });
                }
            }.runTaskTimer(plugin, (plugin.getConfig().getLong("Plugin.Updater.UpdateCheckTime") * 60 * 20), (plugin.getConfig().getLong("Plugin.Updater.UpdateCheckTime") * 60 * 20));
        }
    }

    public void initCommands() {
        try {
            Objects.requireNonNull(plugin.getCommand("update")).setExecutor(new UpdateCommand());
        } catch (JSONException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to initialize UpdateCommand: " + e.getMessage(), LogPrefixExtension.ADMINPANEL_MAIN);
        }
        Objects.requireNonNull(plugin.getCommand("adminpanel")).setExecutor(new AdminPanelOpenCommand());
        PerPlayerLanguageCommand perPLanguageCommand = new PerPlayerLanguageCommand();
        Objects.requireNonNull(plugin.getCommand("perplayerlang")).setExecutor(perPLanguageCommand);
        Objects.requireNonNull(plugin.getCommand("perplayerlang")).setTabCompleter(perPLanguageCommand);
        Objects.requireNonNull(plugin.getCommand("reloadlang")).setExecutor(new LanguageReloadCommand());
    }

    public void initbStatsMetrics(Metrics metrics) {
        metrics.addCustomChart(new Metrics.SimplePie("used_language", () -> languageManager.getCurrentLang().getLangName()));
        metrics.addCustomChart(new Metrics.SimplePie("language_count", () -> String.valueOf(languageManager.getRegisteredLanguages().size())));
        /*metrics.addCustomChart(new Metrics.SimplePie("external_api_language_count", () -> {
            int value = 0;
            for (LanguageFile lang : languageManager.getRegisteredLanguages().values()) {
                    value++;
            }
            return String.valueOf(value);
        }));*/
        /*metrics.addCustomChart(new Metrics.AdvancedBarChart("exampleBar", () -> {
            Map<String, int[]> map = new HashMap<>();
            map.put("Addon System", plugin.isAddonSystemEnabled() ? new int[]{0, 1} : new int[]{1, 0});
            map.put("Updater", plugin.isUpdaterEnabled() ? new int[]{0, 1} : new int[]{1, 0});
            map.put("Plugin Updater", plugin.isPluginUpdaterEnabled() ? new int[]{0, 1} : new int[]{1, 0});
            return map;
        }));*/
        metrics.addCustomChart(new Metrics.SimplePie("servers_with_addonsystem", () -> String.valueOf(plugin.getPluginStateManager().isAddonSystemEnabled())));
        metrics.addCustomChart(new Metrics.SimplePie("servers_with_updater", () -> String.valueOf(plugin.getPluginStateManager().isUpdaterEnabled())));
        metrics.addCustomChart(new Metrics.SimplePie("servers_with_pluginupdater", () -> String.valueOf(plugin.getPluginStateManager().isPluginUpdaterEnabled())));
        metrics.addCustomChart(new Metrics.SimplePie("servers_with_plugin_replace_enabled", () -> String.valueOf(plugin.getPluginStateManager().isUpdateReplacerEnabled())));
        metrics.addCustomChart(new Metrics.SimplePie("servers_with_config_corruption_check_disabled", () -> String.valueOf(plugin.getPluginStateManager().checkIfCorruptionCheckIsDisabled())));
        if (!plugin.getLanguageManager().getPLHandler().getPlayerLanguages().isEmpty()) {
            metrics.addCustomChart(new Metrics.SimplePie("most_used_player_lang", () -> getMostUsedPlayerLang().getLangName()));
        }
    }

    public LanguageFile getMostUsedPlayerLang() {
        Map<UUID, LanguageFile> playerLangs = plugin.getLanguageManager().getPLHandler().getPlayerLanguages();
        Map<LanguageFile, Integer> popularityMap = new HashMap<>();
        for (LanguageFile i : playerLangs.values()) {
            popularityMap.compute(i, (k, count) -> count != null ? count + 1 : 1);
        }
        return Collections.max(popularityMap.entrySet(),
                Map.Entry.comparingByValue()).getKey();
    }

    /**
     * @throws IllegalStateException if PlaceholderAPI is not installed; the panel cannot work without it
     */
    public void initPlaceholderApi() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            throw new IllegalStateException("PlaceholderAPI is not installed, please install it and start again");
        }
        new PlayerExpansion().register();
        new PluginExpansion().register();
        new PanelExpansion().register();
        new DataClientExpansion().register();
        logger.message("&a&lInitialized PlaceHolderAPI with Placeholders!&r");
    }

    public void initSuperVanish() {
        if (Bukkit.getPluginManager().getPlugin("SuperVanish") == null) {
            throw new Startup.DisabledException("SuperVanish is not installed, the vanish feature is off");
        }
    }

    public void initVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            throw new Startup.DisabledException("Vault is not installed, the money features are off");
        }
        if (!setupEconomy()) {
            throw new Startup.DisabledException("Vault has no economy plugin registered, the money features are off");
        }
    }

    public void initConfigFiles(PluginFileLogger fileLogger) {
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
        // Replaced with SQL Database
        /*if (!permissionFile.exists()) {
            logger.spacer().message("&c&lCreating permissions.yml file!&r");
            try {
                boolean ignored = permissionFile.createNewFile();
            } catch (IOException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Error while creating permissions.yml file!", LogPrefix.ERROR);
            }
            logger.message("&e&lDone!&r");
        }*/
    }

    public void initAddonLoader(AddonLoader loader) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            logger.emptySpacer();
            logger.coloredSpacer(ChatColor.BLUE);
            logger.coloredMessage(ChatColor.GREEN, "Adding Addons to the List!");
            logger.coloredMessage(ChatColor.GREEN, "Done with Phase 1/3! (Initialization)");
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
                    plugin.getFileLogger().writeToLog(Level.SEVERE, "Error while loading Addon: " + jarFile.getName(), LogPrefixExtension.ADDONLOADER.getLogPrefix(), false);
                }
            }

            logger.coloredMessage(ChatColor.GREEN, "Done with Phase 2/3! (Loading)");
            logger.coloredSpacer(ChatColor.BLUE);
            logger.emptySpacer();
            logger.coloredSpacer(ChatColor.BLUE);
            logger.coloredMessage(ChatColor.GREEN, "Enabling Addons!");

            for (File addonFile : loader.getLoadedJarFiles().keySet()) {
                try {
                    if (addonFile != null) {
                        Addon mainClass = loader.getMainClassOfAddon(addonFile, false);
                        AddonLoader.EnableResult result = loader.enableAddon(addonFile, new HashSet<>(), false);
                        this.plugin.getStartUpLogger().coloredMessage(ChatColor.GREEN, "Addon enabling status for '" + addonFile.getName() + "' is '" + result + "'!");
                        this.plugin.getFileLogger().writeToLog(Level.INFO, "Addon enabling status for '" + addonFile.getName() + "' is '" + result + "' (" + mainClass.getName() + ")!", LogPrefixExtension.ADDONLOADER.getLogPrefix(), false);
                    } else {
                        throw new NullPointerException("Addon file is null");
                    }
                } catch (Exception e) {
                    String name = addonFile != null ? addonFile.getName() : "unknown";
                    this.plugin.getFileLogger().writeToLog(Level.SEVERE, "Error while enabling Addon: " + name + " (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")", LogPrefixExtension.ADDONLOADER.getLogPrefix(), true);
                }
            }

            logger.coloredMessage(ChatColor.GREEN, "Done with Phase 3/3! (Enabling)");
            logger.coloredSpacer(ChatColor.BLUE);
        });
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economy = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (economy != null)
            plugin.eco = economy.getProvider();
        return plugin.eco != null;
    }

    public void initEvents() {
        logger.coloredSpacer(ChatColor.DARK_RED).message("&2&lStarting Eventregistration:&r");
        PluginManager pm = plugin.getServer().getPluginManager();
        logger.message("&3&lLoading Player Event Listener Events!&r");
        pm.registerEvents(new PlayerEventHandler(AdminPanelMain.getPlugin().getPermissionsManager()), AdminPanelMain.getPlugin());
        logger.message("&3&lLoading Menu Listener Events!&r");
        pm.registerEvents(new MenuListener(), plugin);
        logger.message("&3&lLoading Maintenance and Chat Mute Listener Events!&r");
        pm.registerEvents(new MaintenanceListener(), plugin);
        pm.registerEvents(new DisabledCommandsListener(), plugin);
        pm.registerEvents(new ChatMuteListener(), plugin);
        logger.message("&3&lLoading Disabled Items Listener Events!&r");
        new DisabledItemsListener();
        logger.message("&3&lLoading Main Class Listener Events!&r");
        pm.registerEvents(plugin, plugin);
        logger.message("&4&lEventregistration: Done!&r");
        logger.coloredSpacer(ChatColor.DARK_RED).message("&e&lStarting Done!&r");
        logger.coloredSpacer(ChatColor.GREEN);
    }

    public void initCommandManagers() {
        plugin.getCommandManagerRegistry().register(new PanelOpenManager());
        plugin.getCommandManagerRegistry().register(new AdminPanelAdminManager());
        plugin.getCommandManagerRegistry().register(new DataClientCommandManager());
        plugin.getCommandManagerRegistry().register(new AddonCommandManager());
        plugin.getCommandManagerRegistry().register(new DisabledItemsManager());
        plugin.getCommandManagerRegistry().register(new WarnManager());
        plugin.getCommandManagerRegistry().register(new PermsManager());
        plugin.getCommandManagerRegistry().register(new WarningsManager());
        plugin.getCommandManagerRegistry().register(new UnwarnManager());
    }
}
