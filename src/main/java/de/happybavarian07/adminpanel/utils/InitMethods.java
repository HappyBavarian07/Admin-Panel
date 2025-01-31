package de.happybavarian07.adminpanel.utils;/*
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
import de.happybavarian07.adminpanel.commands.managers.PanelOpenManager;
import de.happybavarian07.adminpanel.language.LanguageFile;
import de.happybavarian07.adminpanel.language.LanguageManager;
import de.happybavarian07.adminpanel.listeners.MenuListener;
import de.happybavarian07.adminpanel.listeners.PlayerEventHandler;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.Metrics;
import de.happybavarian07.adminpanel.placeholders.DataClientExpansion;
import de.happybavarian07.adminpanel.placeholders.PanelExpansion;
import de.happybavarian07.adminpanel.placeholders.PlayerExpansion;
import de.happybavarian07.adminpanel.placeholders.PluginExpansion;
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
        metrics.addCustomChart(new Metrics.SimplePie("servers_with_addonsystem", () -> String.valueOf(plugin.getPluginStateManager().isAddonSystemEnabled())));
        metrics.addCustomChart(new Metrics.SimplePie("servers_with_updater", () -> String.valueOf(plugin.getPluginStateManager().isUpdaterEnabled())));
        metrics.addCustomChart(new Metrics.SimplePie("servers_with_pluginupdater", () -> String.valueOf(plugin.getPluginStateManager().isPluginUpdaterEnabled())));
        metrics.addCustomChart(new Metrics.SimplePie("servers_with_plugin_replace_enabled", () -> String.valueOf(plugin.getPluginStateManager().isUpdateReplacerEnabled())));
        metrics.addCustomChart(new Metrics.SimplePie("servers_with_config_corruption_check_disabled", () -> String.valueOf(plugin.getPluginStateManager().checkIfCorruptionCheckIsDisabled())));
        if (!plugin.getLanguageManager().getPlhandler().getPlayerLanguages().isEmpty()) {
            metrics.addCustomChart(new Metrics.SimplePie("most_used_player_lang", () -> getMostUsedPlayerLang().getLangName()));
        }
    }

    public LanguageFile getMostUsedPlayerLang() {
        Map<UUID, LanguageFile> playerLangs = plugin.getLanguageManager().getPlhandler().getPlayerLanguages();
        Map<LanguageFile, Integer> popularityMap = new HashMap<>();
        for (LanguageFile i : playerLangs.values()) {
            popularityMap.compute(i, (k, count) -> count != null ? count + 1 : 1);
        }
        return Collections.max(popularityMap.entrySet(),
                Map.Entry.comparingByValue()).getKey();
    }

    public void initPluginCheck() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlayerExpansion().register();
            new PluginExpansion().register();
            new PanelExpansion().register();
            new DataClientExpansion().register();
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
                boolean ignored = permissionFile.createNewFile();
            } catch (IOException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Error while creating permissions.yml file!", LogPrefix.ERROR);
            }
            logger.message("&e&lDone!&r");
        }
    }

    public void initAddonLoader(AddonLoader loader) {
        new Thread(() -> {
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
                    plugin.getFileLogger().writeToLog(Level.SEVERE, "Error while loading Addon: " + jarFile.getName(), LogPrefix.ADDONLOADER);
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
                        System.out.println("Addon file is not null: " + addonFile.getName());
                        Addon mainClass = loader.getMainClassOfAddon(addonFile, false);
                        System.out.println("Addon Main Class: " + mainClass);
                        System.out.println("Before enabling addon: " + addonFile.getName());
                        System.out.println("Calling enableAddon method...");
                        AddonLoader.EnableResult result = loader.enableAddon(addonFile, new HashSet<>());
                        System.out.println("Result from enabling: " + result);
                        System.out.println("After enabling addon: " + addonFile.getName());
                    } else {
                        System.out.println("Addon file is null");
                    }
                } catch (Exception e) {
                    this.plugin.getFileLogger().writeToLog(Level.SEVERE, "Error while enabling Addon: " + addonFile.getName(), LogPrefix.ADDONLOADER);
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
        logger.coloredSpacer(ChatColor.DARK_RED).message("&2&lStarting Eventregistration:&r");
        PluginManager pm = plugin.getServer().getPluginManager();
        logger.message("&3&lLoading Player Event Listener Events!&r");
        pm.registerEvents(new PlayerEventHandler(AdminPanelMain.getPlugin().getPermissionsManager()), AdminPanelMain.getPlugin());
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
        plugin.getCommandManagerRegistry().register(new DataClientCommandManager());
        plugin.getCommandManagerRegistry().register(new AddonCommandManager());
    }
}
