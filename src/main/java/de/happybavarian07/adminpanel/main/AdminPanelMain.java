package de.happybavarian07.adminpanel.main;

import com.saicone.ezlib.Ezlib;
import de.happybavarian07.adminpanel.addonloader.api.Addon;
import de.happybavarian07.adminpanel.addonloader.loadingutils.AddonLoader;
import de.happybavarian07.adminpanel.backupmanager.BackupManager;
import de.happybavarian07.adminpanel.backupmanager.FileBackup;
import de.happybavarian07.adminpanel.commandmanagement.CommandManagerRegistry;
import de.happybavarian07.adminpanel.configupdater.ConfigUpdater;
import de.happybavarian07.adminpanel.hooks.BanManagerHook;
import de.happybavarian07.adminpanel.hooks.LiteBansHook;
import de.happybavarian07.adminpanel.language.LanguageFile;
import de.happybavarian07.adminpanel.language.LanguageManager;
import de.happybavarian07.adminpanel.language.OldLanguageFileUpdater;
import de.happybavarian07.adminpanel.language.PerPlayerLanguageHandler;
import de.happybavarian07.adminpanel.language.mysql.MySQLLanguageManager;
import de.happybavarian07.adminpanel.listeners.StaffChatHandler;
import de.happybavarian07.adminpanel.menusystem.MenuAddonManager;
import de.happybavarian07.adminpanel.syncing.DataClient;
import de.happybavarian07.adminpanel.syncing.DataClientUtils;
import de.happybavarian07.adminpanel.syncing.utils.BungeeUtils;
import de.happybavarian07.adminpanel.utils.*;
import de.happybavarian07.adminpanel.utils.managers.PluginDescriptionManager;
import de.happybavarian07.adminpanel.utils.tfidfsearch.TFIDFSearch;
import de.happybavarian07.webui.log.Log4JAppender;
import de.happybavarian07.webui.main.WebUI;
import dte.hooksystem.api.HookSystemAPI;
import dte.hooksystem.service.HookService;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

public class AdminPanelMain extends JavaPlugin implements Listener {
    private static String prefix;
    private static AdminPanelAPI API;
    private static AdminPanelMain plugin;
    private static PluginFileLogger fileLogger;
    public final Map<UUID, Boolean> hurtingwater = new HashMap<>();
    public final Map<UUID, Boolean> chatmute = new HashMap<>();
    public final Map<UUID, Boolean> villagerSounds = new HashMap<>();
    public final Map<UUID, Boolean> blockBreakPrevent = new HashMap<>();
    public final Map<UUID, Boolean> dupeMobsOnKill = new HashMap<>();
    public final Map<UUID, Boolean> freezeplayers = new HashMap<>();
    private final List<String> disabledCommands = new ArrayList<>();
    private final File configFile = new File(this.getDataFolder(), "config.yml");
    private final File permissionFile = new File(this.getDataFolder(), "permissions.yml");
    private final Map<UUID, PermissionAttachment> playerPermissionsAttachments = new HashMap<>();
    private final Map<UUID, Map<String, Boolean>> playerPermissions = new HashMap<>();
    private final List<Addon> loadedAddons = new ArrayList<>();
    private final Map<String, NewUpdater> autoUpdaterPlugins = new HashMap<>();
    public Economy eco = null;
    public net.milkbowl.vault.permission.Permission perms = null;
    public Chat chat = null;
    public boolean inMaintenanceMode = false;
    public boolean chatMuted = false;
    private long lastStartTimeMillis;
    private StartUpLogger logger;
    private FileConfiguration permissionsConfig;
    private NewUpdater updater;
    private LanguageManager languageManager;
    private OldLanguageFileUpdater langFileUpdater;
    private AddonLoader loader;
    private FileConfiguration dataYML;
    private CommandManagerRegistry commandManagerRegistry;
    private BungeeUtils bungeeUtils;
    //private DataClient dataClient;
    private DataClientUtils dataClientUtils;
    private DataClient dataClient;
    private boolean languageManagerEnabled;
    private WarningManager warningManager;
    private InitMethods initMethods;
    private BackupManager backupManager;
    private MenuAddonManager menuAddonManager;
    private TFIDFSearch permissionSearcher;
    private StaffChatHandler staffChatHandler;
    private WebUI webUI;
    private TPSMeter tpsMeter;
    private PluginDescriptionManager pluginDescriptionManager;

    /**
     * Returns the Prefix of the Plugin
     *
     * @return The Prefix of the Plugin
     */
    public static String getPrefix() {
        return prefix;
    }

    public PluginDescriptionManager getPluginDescriptionManager() {
        return pluginDescriptionManager;
    }

    public static void setPrefix(String prefix) {
        AdminPanelMain.prefix = prefix;
    }

    public static AdminPanelAPI getAPI() {
        return API;
    }

    public static AdminPanelMain getPlugin() {
        return plugin;
    }

    private void setPlugin(AdminPanelMain plugin) {
        AdminPanelMain.plugin = plugin;
    }

    public TPSMeter getTpsMeter() {
        return tpsMeter;
    }

    public TFIDFSearch getPermissionSearcher() {
        return permissionSearcher;
    }

    public MenuAddonManager getMenuAddonManager() {
        return menuAddonManager;
    }

    public BackupManager getBackupManager() {
        return backupManager;
    }

    public FileConfiguration getPermissionsConfig() {
        return permissionsConfig;
    }

    public boolean isLanguageManagerEnabled() {
        return languageManagerEnabled;
    }

    private void setLanguageManagerEnabled(boolean languageManagerEnabled) {
        this.languageManagerEnabled = languageManagerEnabled;
    }

    public long getLastStartTimeMillis() {
        return lastStartTimeMillis;
    }

    public DataClient getDataClient() {
        try {
            return dataClient;
        } catch (CommandException e) {
            System.out.println("Data Client is not connected to Data Server from BungeeCord!");
        }
        return null;
    }

    public DataClientUtils getDataClientUtils() {
        try {
            return dataClientUtils;
        } catch (CommandException e) {
            System.out.println("Data Client is not connected to Data Server from BungeeCord!");
        }
        return null;
    }

    public BungeeUtils getBungeeUtils() {
        return bungeeUtils;
    }

    /**
     * Returns if the Plugin is in Maintenance Mode
     *
     * @return If the Plugin is in Maintenance Mode
     */
    public boolean isInMaintenanceMode() {
        return inMaintenanceMode;
    }

    public void setInMaintenanceMode(boolean inMaintenanceMode) {
        this.inMaintenanceMode = inMaintenanceMode;
    }

    public Map<String, NewUpdater> getAutoUpdaterPlugins() {
        return autoUpdaterPlugins;
    }

    /**
     * Returns if the Chat is Muted
     *
     * @return If the Chat is Muted
     */
    public boolean isChatMuted() {
        return chatMuted;
    }

    public void setChatMuted(boolean chatMuted) {
        this.chatMuted = chatMuted;
    }

    public List<Addon> getLoadedAddons() {
        return loadedAddons;
    }

    /**
     * Returns the Updater
     *
     * @return The Updater
     */
    public NewUpdater getUpdater() {
        return updater;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public List<String> getDisabledCommands() {
        return disabledCommands;
    }

    public Map<UUID, PermissionAttachment> getPlayerPermissionsAttachments() {
        return playerPermissionsAttachments;
    }

    public OldLanguageFileUpdater getLangFileUpdater() {
        return langFileUpdater;
    }

    public Map<UUID, Map<String, Boolean>> getPlayerPermissions() {
        return playerPermissions;
    }

    public CommandManagerRegistry getCommandManagerRegistry() {
        return commandManagerRegistry;
    }

    public File getConfigFile() {
        return configFile;
    }

    /**
     * Returns if the Addon System is enabled
     *
     * @return If the Addon System is enabled
     */
    public boolean isAddonSystemEnabled() {
        return getConfig().getBoolean("Plugin.AddonSystem.enabled");
    }

    /**
     * Returns if the Updater is enabled
     *
     * @return If the Updater is enabled
     */
    public boolean isUpdaterEnabled() {
        return getConfig().getBoolean("Plugin.Updater.checkForUpdates");
    }

    /**
     * Returns if the Plugin Updater is enabled
     *
     * @return If the Plugin Updater is enabled
     */
    public boolean isPluginUpdaterEnabled() {
        return getConfig().getBoolean("Plugin.Updater.PluginUpdater.enabled");
    }

    /**
     * Returns if the Automatic Replace of the Plugin is enabled
     *
     * @return If the Automatic Replace of the Plugin is enabled
     */
    public boolean isUpdateReplacerEnabled() {
        return getConfig().getBoolean("Plugin.Updater.automaticReplace");
    }

    /**
     * Returns if the Language Manager is enabled
     *
     * @return If the Language Manager is enabled
     */
    public StaffChatHandler getStaffChatHandler() {
        return staffChatHandler;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
    }

    public void updateConfig() {
        try {
            ConfigUpdater.update(this, "config.yml", new File(this.getDataFolder() + "/config.yml"), new ArrayList<>());
        } catch (IOException e) {
            // Log error using LogToFile From FileLogger
            fileLogger.writeToLog(Level.SEVERE, "Error updating Config File from AdminPanel", LogPrefix.ADMINPANEL_MAIN);
            e.printStackTrace();
        }
        reloadConfig();
    }

    public InitMethods getInitMethods() {
        return initMethods;
    }

    /**
     * Saves the Permissions to the Permissions.yml File
     */
    public void reloadData() {
        saveResource("data.yml", false);
        dataYML = YamlConfiguration.loadConfiguration(new File(getDataFolder() + "/data.yml"));
    }

    private boolean checkIfBungee() {
        if (getServer().spigot().getConfig().getConfigurationSection("settings").getBoolean("bungeecord", false) &&
                getConfig().getBoolean("Plugin.BungeeSyncSystem.enabled")) {
            return true;
        } else {
            getLogger().severe("This Server is not BungeeCord.");
            getLogger().severe("If the Server is already hooked to BungeeCord, please enable it into your spigot.yml aswell.");
            getLogger().severe("Plugin - BungeeCord Connection Feature disabled!");
            return false;
        }
    }

    private void loadDependenciesOverDependencyManager() {
        logger.coloredSpacer(ChatColor.RED).message("&e&lLoading Dependencies&r");
        Ezlib ezlib = new Ezlib(new File(this.getDataFolder() + "/libs"));
        ezlib.init();
        logger.message("&e&lLoading Lucene Core Dependency&r");
        ezlib.dependency("org.apache.lucene:lucene-core:9.8.0").parent(true).load();
        logger.message("&e&lLoading Lucene QueryParser Dependency&r");
        ezlib.dependency("org.apache.lucene:lucene-queryparser:9.8.0").parent(true).load();
        logger.message("&e&lLoading Commons Codec Dependency&r");
        ezlib.dependency("commons-codec:commons-codec:1.16.0").parent(true).load();
        // Add Spark Dependency (only if WebUI Feature is enabled in Config)
        // Needed Dependencies for Jetty Server and Jetty Webapp
        ezlib.dependency("javax.servlet:javax.servlet-api:3.1.0").parent(true).load();
        ezlib.dependency("org.eclipse.jetty:jetty-util:9.4.48.v20220622").parent(true).load();
        ezlib.dependency("org.eclipse.jetty:jetty-xml:9.4.48.v20220622").parent(true).load();
        ezlib.dependency("org.eclipse.jetty:jetty-io:9.4.48.v20220622").parent(true).load();
        ezlib.dependency("org.eclipse.jetty:jetty-http:9.4.48.v20220622").parent(true).load();
        ezlib.dependency("org.eclipse.jetty:jetty-servlet:9.4.48.v20220622").parent(true).load();
        ezlib.dependency("org.eclipse.jetty:jetty-security:9.4.48.v20220622").parent(true).load();
        ezlib.dependency("org.eclipse.jetty:jetty-util-ajax:9.4.48.v20220622").parent(true).load();

        // Jetty Server and Webapp
        ezlib.dependency("org.eclipse.jetty:jetty-server:9.4.48.v20220622").parent(true).load();
        ezlib.dependency("org.eclipse.jetty:jetty-webapp:9.4.48.v20220622").parent(true).load();

        // Needed Dependencies for Jetty Websocket
        ezlib.dependency("org.eclipse.jetty.websocket:websocket-common:9.4.48.v20220622").parent(true).load();
        ezlib.dependency("org.eclipse.jetty.websocket:websocket-api:9.4.48.v20220622").parent(true).load();
        ezlib.dependency("org.eclipse.jetty.websocket:websocket-client:9.4.48.v20220622").parent(true).load();
        ezlib.dependency("org.eclipse.jetty:jetty-client:9.4.48.v20220622").parent(true).load();
        ezlib.dependency("org.eclipse.jetty.websocket:websocket-servlet:9.4.48.v20220622").parent(true).load();

        // Jetty Websocket
        ezlib.dependency("org.eclipse.jetty.websocket:websocket-server:9.4.48.v20220622").parent(true).load();

        // Jetty Websocket Servlet
        ezlib.dependency("org.eclipse.jetty.websocket:websocket-servlet:9.4.48.v20220622").parent(true).load();

        // Spark Dependency
        logger.message("&e&lLoading Spark Dependency and its needed Dependencies&r");
        ezlib.dependency("com.sparkjava:spark-core:2.9.4").parent(true).load();

        // Needed Dependencies for JSON Web Token
        ezlib.dependency("com.fasterxml.jackson.core:jackson-databind:2.9.6").parent(true).load();
        ezlib.dependency("com.fasterxml.jackson.core:jackson-annotations:2.9.0").parent(true).load();
        ezlib.dependency("com.fasterxml.jackson.core:jackson-core:2.9.6").parent(true).load();
        ezlib.dependency("javax.xml.bind:jaxb-api:2.3.1").parent(true).load();

        // JSON Web Token
        ezlib.dependency("io.jsonwebtoken:jjwt:0.9.1").parent(true).load();

        // Needed Dependencies for MySQL
        ezlib.dependency("org.xerial:sqlite-jdbc:3.36.0.3").parent(true).load();
        // MariaDB Driver
        ezlib.dependency("org.mariadb.jdbc:mariadb-java-client:3.3.3").parent(true).load();


        logger.message("&a&lDone&r").coloredSpacer(ChatColor.RED);
        logger.emptySpacer().emptySpacer();
    }

    private void useHookSystem() {
        HookService hookService = HookSystemAPI.getService(this);

        hookService.register(new BanManagerHook("BanManager")).orElse((handler) -> {
            getLogger().warning("Failed to hook into BanManager! If you are using LiteBans as the Ban Plugin, please ignore this message. \n" +
                    "Else please check if Ban Manager is installed and running or send me your Ban Plugin if it is publically available over the /admin-panel:report feature, \n" +
                    "so i can implement it!");
        });
        hookService.register(new LiteBansHook("LiteBans")).orElse((handler) -> {
            getLogger().warning("Failed to hook into LiteBans! If you are using Ban Manager as the Ban Plugin, please ignore this message. \n" +
                    "Else please check if LiteBans is installed and running or send me your Ban Plugin if it is publically available over the /admin-panel:report feature, \n" +
                    "so i can implement it!");
        });
    }

    public void setupSparkWebUI() {
        webUI = new WebUI(8080, this);
        webUI.startWebUI();

        // Console Log Web Socket Handler
    }

    @Override
    public void onLoad() {
        saveDefaultConfig();
        setPlugin(this);
        logger = StartUpLogger.create();

        // Load Dependencies via Manager
        loadDependenciesOverDependencyManager();

        // Add Log4J Appender
        Logger rootLogger = (Logger) LogManager.getRootLogger();
        rootLogger.addAppender(new Log4JAppender());
    }

    @Override
    public void onEnable() {
        pluginDescriptionManager = new PluginDescriptionManager();
        lastStartTimeMillis = System.currentTimeMillis();
        tpsMeter = new TPSMeter();
        setPlugin(this);

        // Start Hook System
        useHookSystem();

        // bStats
        int bStatsID = 11778;
        Metrics metrics = new Metrics(this, bStatsID);

        logger
                .coloredSpacer(ChatColor.GREEN)
                .messages(
                        "&e&lStarting Admin Panel Plugin:&r"
                );
        logger.coloredSpacer(ChatColor.DARK_RED).message("&4&lInitialize Plugin Main Variable to this!&r");

        fileLogger = new PluginFileLogger();

        menuAddonManager = new MenuAddonManager();
        boolean mysqlLanguageManagerTemp = true;
        if (mysqlLanguageManagerTemp) {
            try {
                languageManager = new MySQLLanguageManager(this, new File(this.getDataFolder() + "/languages"), "[Admin-Panel]");
            } catch (RuntimeException e) {
                if(e.getMessage().contains("Failed to create a connection to the database")) {
                    languageManager = new LanguageManager(this, new File(this.getDataFolder() + "/languages"), "[Admin-Panel]");
                    mysqlLanguageManagerTemp = false;
                    plugin.getLogger().warning("Failed to connect to the MySQL Database! Using the File Language Manager instead!");
                }
            }
        } else {
            languageManager = new LanguageManager(this, new File(this.getDataFolder() + "/languages"), "[Admin-Panel]");
        }

        commandManagerRegistry = new CommandManagerRegistry(this);
        langFileUpdater = new OldLanguageFileUpdater();
        API = new LocalAdminPanelAPI(this);
        initMethods = new InitMethods(logger, Bukkit.getPluginManager(), plugin);
        new ChatUtil();
        new Utils();
        new File(this.getDataFolder() + "/languages").mkdir();
        backupManager = new BackupManager(plugin, 5, "config_backups/");
        if (!new File(this.getDataFolder() + "/config_backups").exists()) {
            new File(this.getDataFolder() + "/config_backups").mkdirs();
        }
        // Backup Manager Backup Adding
        File[] filesToBackup = new File[]{
                new File(this.getDataFolder() + "/languages/"),
                //new File(this.getDataFolder() + "/webui/"),
                new File(this.getDataFolder() + "/config.yml"),
                new File(this.getDataFolder() + "/data.yml"),
                new File(this.getDataFolder() + "/DataClientSettings.yml"),
                new File(this.getDataFolder() + "/permissions.yml"),
                new File(this.getDataFolder() + "/language.db"),
                new File(this.getDataFolder() + "/plugin_descriptions.yml"),
                new File(this.getDataFolder() + "/webui_config.yml"),
                new File(this.getDataFolder() + "/database.properties")
        };
        backupManager.addFileBackup(new FileBackup(
                "ConfigBackup",
                filesToBackup,
                backupManager.getBackupFolder() + "ConfigBackup"
        ));
        // Backup on Start/End of the onEnable Method (maybe with a String Config Option)

        logger.message("&e&lVariable Done!&r");
        logger.message("&e&lStarting Bungee Registration");
        if (checkIfBungee()) {
            String path = "Plugin.BungeeSyncSystem.ChannelNames.";
            bungeeUtils = new BungeeUtils(getConfig().getString(path + "In", ""), getConfig().getString(path + "Out", ""));
            bungeeUtils.openBungeeChannel();
            //this.getCommand("bungeetest").setExecutor(new BungeeTestCommand());
            path = "Plugin.BungeeSyncSystem.JavaSockets.";
            if (getConfig().getBoolean(path + "enabled")) {
                /*dataClient = new DataClient(getConfig().getString(path + "hostName"),
                        getConfig().getInt(path + "port"), getConfig().getString(path + "ClientName"));
                dataClient.connect();*/
                try {
                    dataClient = new DataClient(getConfig().getString(path + "hostName"),
                            getConfig().getInt(path + "port"),
                            getConfig().getString(path + "ClientName"));
                    dataClientUtils = new DataClientUtils(dataClient);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        logger.message("&a&lDone");
        // Plugin Checks
        initMethods.initPluginCheck();
        logger.messages("&c&lFinished Vault initialization!&r");
        // Config Files
        initMethods.initConfigFiles(fileLogger, permissionFile);
        // Init Warnings Manager
        if (getConfig().getBoolean("Pman.Actions.WarningSystem")) {
            warningManager = new WarningManager(
                    plugin,
                    new File(this.getDataFolder() + "/warnings.yml"));
            warningManager.loadWarnings();
        }
        // Permission Init
        permissionsConfig = YamlConfiguration.loadConfiguration(permissionFile);
        initMethods.initPermissionFiles(permissionsConfig, permissionFile, playerPermissions);

        //permissionsConfig.set("Permissions.0c069d0e-5778-4d51-8929-6b2f69b475c0.Permissions.test.test.test1", true);
        //permissionsConfig.set("Permissions.0c069d0e-5778-4d51-8929-6b2f69b475c0.Permissions.test.test.test2", true);
        try {
            permissionsConfig.save(permissionFile);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // ...
        logger.message("&3&lMain.Prefix &9= &7Config.Plugin.Prefix&r");
        setPrefix(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getConfig().getString("Plugin.Prefix"))));
        languageManager.setPrefix(getPrefix());
        logger.message("&e&lPrefix Done!&r");

        // Init Events
        initMethods.initEvents();

        dataYML = YamlConfiguration.loadConfiguration(new File(getDataFolder() + "/data.yml"));
        languageManager.setPlhandler(new PerPlayerLanguageHandler(languageManager, new File(getDataFolder() + "/data.yml"), dataYML));

        // Language Manager Enabling
        LanguageFile deLang = new LanguageFile(this, "de", true);
        LanguageFile enLang = new LanguageFile(this, "en", true);
        boolean autoLanguageFileUpdating = getConfig().getBoolean("Plugin.Updater.AutomaticLanguageFileUpdating");
        languageManager.addLanguagesToList(true);
        if (languageManager instanceof MySQLLanguageManager) {
            ((MySQLLanguageManager) languageManager).addLang(UUID.fromString("34a3eaac-5fd8-4d82-950e-e52153529b53"), deLang, deLang.getLangName(), !autoLanguageFileUpdating);
            ((MySQLLanguageManager) languageManager).addLang(UUID.fromString("d6ef09c8-8cb9-4335-85be-1e95d81d6e93"), enLang, enLang.getLangName(), !autoLanguageFileUpdating);
        } else {
            languageManager.addLang(deLang, deLang.getLangName());
            languageManager.addLang(enLang, enLang.getLangName());
        }
        languageManager.setCurrentLang(languageManager.getLang(getConfig().getString("Plugin.language"), true), true);
        setLanguageManagerEnabled(languageManager != null && languageManager.getCurrentLang() != null);
        if (isLanguageManagerEnabled()) {
            getServer().getConsoleSender().sendMessage(languageManager.getMessage("Plugin.EnablingMessage", null, true));
        } else {
            getServer().getConsoleSender().sendMessage(Utils.chat("&f[&aAdmin-&ePanel&f] &cLanguage Manager is not enabled, " +
                    "which means all the Items and Messages won't work! " +
                    "The Plugin will automatically unload! " +
                    "Look for Errors from the Admin-Panel in the Console!"));
            Bukkit.getPluginManager().disablePlugin(this);
        }

        // WebUI temp Setup Code / TODO REMOVE LATER AND MAKE IT BETTER
        setupSparkWebUI();

        // Staff Chat
        staffChatHandler = new StaffChatHandler();
        Bukkit.getPluginManager().registerEvents(new StaffChatHandler(), plugin);

        /*if (!languageManager.getPlhandler().getPlayerLanguages().isEmpty())
            System.out.println("Most Used Player Lang: " + initMethods.getMostUsedPlayerLang().getLangName());*/

        updateConfig();
        if (autoLanguageFileUpdating) {
            System.out.println("Auto Language File Updating is enabled!");
            if (languageManager instanceof MySQLLanguageManager) {
                // List Language IDs
                System.out.println("Language IDs: " + ((MySQLLanguageManager) languageManager).getDatabaseController().getInnerLanguageManager().getLanguageIDs());
                System.out.println("Language Converter Map: " + ((MySQLLanguageManager) languageManager).getDatabaseController().getInnerLanguageManager().getConverterManager().getLanguageConverterMap());
                // Compare Language ID Size to Converter Map Size and wait for the Converter Map to be filled
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        while (((MySQLLanguageManager) languageManager).getDatabaseController().getInnerLanguageManager().getLanguageIDs().size() != ((MySQLLanguageManager) languageManager).getDatabaseController().getInnerLanguageManager().getConverterManager().getLanguageConverterMap().size()) {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        languageManager.reloadLanguages(/*Bukkit.getConsoleSender()*/null, true);
                    }
                }.runTaskLater(this, 20L*10);
            } else {
                languageManager.reloadLanguages(/*Bukkit.getConsoleSender()*/null, false);
            }
        }

        // Init bStats Metrics
        initMethods.initbStatsMetrics(metrics);

        // Init Updater
        updater = new NewUpdater(plugin, 91800, "Admin-Panel-%version%.jar", plugin, "", true);
        initMethods.initUpdater(updater, dataYML);

        // Init Permissions
        initMethods.initPermissions(playerPermissionsAttachments, playerPermissions);

        // Init Commands
        initMethods.initCommands();

        // Init Command Managers
        initMethods.initCommandManagers();

        // Init Addon Loader
        if (plugin.getConfig().getBoolean("Plugin.AddonSystem.enabled")) {
            logger.emptySpacer();
            logger.emptySpacer();
            logger.coloredSpacer(ChatColor.RED);
            logger.message("&cIt could happen that an Addon needs another Plugin to work.&r");
            logger.message("&cThen this Addon will not start and the Loader&r");
            logger.message("&cwill continue loading!&r");
            logger.coloredSpacer(ChatColor.RED);
            logger.emptySpacer();
            logger.coloredSpacer(ChatColor.BLUE);
            logger.coloredMessage(ChatColor.GREEN, "Adding Addons to the List!");


            loader = new AddonLoader(new File(this.getDataFolder() + "/addons"));
            initMethods.initAddonLoader(loader);
        }

        // Permission Searcher Init
        permissionSearcher = new TFIDFSearch(new String[]{"permissionName", "permissionDescription", "permissionDefault", "permissionChildren"});
        Thread permissionIndexThread = getPermissionIndexThread();
        permissionIndexThread.start();

        // Backup Manager Execute
        // if Clause fertig
        // Backup on Start/End of the onEnable Method (maybe with a String Config Option)
        backupManager.startBackup("ConfigBackup");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        getFileLogger().writeToLog(Level.INFO, "Admin-Panel successfully started on '" + dtf.format(now) + "'", LogPrefix.DATELOGGER);
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
            fileLogger.writeToLog(Level.INFO, "Permission Searcher took '" + (endTime - startTime) + "' ms to index " + permissionItems.size() + " permissions", LogPrefix.DATELOGGER);
        });
        permissionIndexThread.setDaemon(true);
        return permissionIndexThread;
    }

    /**
     * Returns the Version of the Plugin
     *
     * @return The Version of the Plugin
     */
    public String getVersion() {
        if (getDescription().getVersion().isEmpty()) return "N/A";
        return getDescription().getVersion();
    }

    public FileConfiguration getDataYML() {
        return dataYML;
    }

    /**
     * Removes a Plugin from the AutoUpdater
     *
     * @param selectedPlugin The Plugin
     */
    public void removePluginFromUpdater(Plugin selectedPlugin) {
        getDataYML().set("PluginsToUpdate." + selectedPlugin.getName(), null);

        try {
            getDataYML().save(new File(getDataFolder() + "/data.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        getAutoUpdaterPlugins().remove(selectedPlugin.getName());
    }

    public void addPluginToUpdater(Plugin plugin, int spigotID, String fileName) {
        String path = "PluginsToUpdate." + plugin.getName() + ".";
        getDataYML().set(path + "spigotID", spigotID);
        getDataYML().set(path + "fileName", fileName);
        getDataYML().set(path + "link", "");
        getDataYML().set(path + "bypassExternalDownload", false);

        try {
            getDataYML().save(new File(getDataFolder() + "/data.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        NewUpdater updater = new NewUpdater(AdminPanelMain.plugin, spigotID, fileName, (JavaPlugin) plugin, "", false);
        getAutoUpdaterPlugins().put(plugin.getName(), updater);
    }

    /**
     * Returns the Updater of the Plugin
     *
     * @param plugin The Plugin
     * @return The Updater of the Plugin
     */
    public NewUpdater getPluginUpdater(Plugin plugin) {
        return getAutoUpdaterPlugins().get(plugin.getName());
    }

    /**
     * Returns the Updater of the Plugin
     *
     * @param pluginName The Name of the Plugin
     * @return The Updater of the Plugin
     */
    public NewUpdater getPluginUpdater(String pluginName) {
        return getAutoUpdaterPlugins().get(pluginName);
    }

    public PluginFileLogger getFileLogger() {
        return fileLogger;
    }

    /**
     * Returns the File of the Plugin
     *
     * @return The File of the Plugin
     */
    public File getPluginFile() {
        return this.getFile();
    }

    /**
     * Returns the Addon Loader
     *
     * @return The Addon Loader
     */
    public StartUpLogger getStartUpLogger() {
        return logger;
    }

    @Override
    public void onDisable() {
        if (loader != null) {
            loader.crashAddons();
        }
        loader = null;
        savePerms();
        if (warningManager != null) {
            warningManager.saveWarnings();
        }
        if (checkIfBungee()) {
            bungeeUtils.closeBungeeChannel();
            String path = "Plugin.BungeeSyncSystem.JavaSockets.";
            if (getConfig().getBoolean(path + "enabled")) {
                dataClient.disconnect(true);
                dataClient.getStatsManager().saveStatsToFile();
            }
        }
        if(webUI != null) {
            webUI.stopWebUI();
        }

        if (isLanguageManagerEnabled()) {
            getServer().getConsoleSender().sendMessage(languageManager.getMessage("Plugin.DisablingMessage", null, true));
        } else {
            getServer().getConsoleSender().sendMessage("[Admin-Panel] disabled!");
        }
    }

    /**
     * Adds the Permissions of the Player when he joins the Server
     *
     * @param event The PlayerJoinEvent
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PermissionAttachment attachment = player.addAttachment(plugin);
        if (playerPermissionsAttachments.containsKey(player.getUniqueId())) {
            if (!playerPermissions.containsKey(player.getUniqueId())) {
                playerPermissions.put(player.getUniqueId(), new HashMap<>());
            }
            Map<String, Boolean> permissions = playerPermissions.get(player.getUniqueId());
            for (Map.Entry<String, Boolean> perms : permissions.entrySet()) {
                attachment.setPermission(perms.getKey(), perms.getValue());
            }
            playerPermissionsAttachments.put(player.getUniqueId(), attachment);
            player.recalculatePermissions();
        }
    }

    /**
     * Removes the Permissions of the Player when he quits the Server
     *
     * @param event The PlayerQuitEvent
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playerPermissionsAttachments.containsKey(player.getUniqueId())) {
            player.removeAttachment(playerPermissionsAttachments.get(player.getUniqueId()));
        }
    }

    /**
     * Saves the Permissions of the Players to the permissions.yml File
     * This Method is called when the Plugin is disabled
     * or when the Permissions are reloaded
     * or when the Permissions are saved
     */
    public void savePerms() {
        for (UUID uuid : playerPermissions.keySet()) {
            String path = "Permissions." + uuid + ".Permissions.";
            Map<String, Boolean> perms = playerPermissions.get(uuid);
            permissionsConfig.set("Permissions." + uuid + ".Permissions", null);
            for (String permissions : perms.keySet()) {
                if (permissions.contains("(<->)")) permissions.replace("(<->)", ".");
                permissionsConfig.set(path + permissions/*.replace(".", "(<->)")*/, perms.get(permissions));
            }
        }
        try {
            permissionsConfig.save(permissionFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reloads the Permissions of the Players from the permissions.yml File
     *
     * @param player The Player to reload the Permissions from the permissions.yml File
     */
    public void reloadPerms(Player player) {
        if (player != null) {
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
        } else {
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
    }

    /**
     * Returns the Warning Manager (unfinished)
     *
     * @return The Warning Manager
     */
    public WarningManager getWarningManager() {
        return warningManager;
    }
}
