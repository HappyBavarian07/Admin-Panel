package de.happybavarian07.adminpanel.main;

import de.happybavarian07.adminpanel.addonloader.loadingutils.AddonLoader;
import de.happybavarian07.adminpanel.backupmanager.BackupManager;
import de.happybavarian07.adminpanel.backupmanager.FileBackup;
import de.happybavarian07.adminpanel.configupdater.ConfigUpdater;
import de.happybavarian07.adminpanel.listeners.InternalCacheHandler;
import de.happybavarian07.adminpanel.listeners.StaffChatHandler;
import de.happybavarian07.adminpanel.permissions.PermissionsManager;
import de.happybavarian07.adminpanel.permissions.PlayerPermission;
import de.happybavarian07.adminpanel.permissions.PlayerPermissionRepository;
import de.happybavarian07.adminpanel.syncing.DataClient;
import de.happybavarian07.adminpanel.syncing.DataClientUtils;
import de.happybavarian07.adminpanel.syncing.utils.BungeeUtils;
import de.happybavarian07.adminpanel.utils.*;
import de.happybavarian07.adminpanel.utils.managers.*;
import de.happybavarian07.coolstufflib.CoolStuffLib;
import de.happybavarian07.coolstufflib.CoolStuffLibBuilder;
import de.happybavarian07.coolstufflib.commandmanagement.CommandManagerRegistry;
import de.happybavarian07.coolstufflib.jpa.RepositoryController;
import de.happybavarian07.coolstufflib.jpa.utils.DatabaseProperties;
import de.happybavarian07.coolstufflib.languagemanager.LanguageFile;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import de.happybavarian07.coolstufflib.languagemanager.PerPlayerLanguageHandler;
import de.happybavarian07.coolstufflib.menusystem.MenuAddonManager;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

public class AdminPanelMain extends JavaPlugin implements Listener {
    private static String prefix;
    private static AdminPanelAPI API;
    private static AdminPanelMain plugin;
    private static CustomPluginFileLogger fileLogger;
    private final List<String> disabledCommands = new ArrayList<>();
    public Economy eco = null;
    public net.milkbowl.vault.permission.Permission perms = null;
    public Chat chat = null;
    private long lastStartTimeMillis;
    private StartUpLogger logger;
    private NewUpdater updater;
    private LanguageManager languageManager;
    private AddonLoader loader;
    private FileConfiguration dataYML;
    private CommandManagerRegistry commandManagerRegistry;
    private BungeeUtils bungeeUtils;
    private DataClientUtils dataClientUtils;
    private DataClient dataClient;
    private boolean languageManagerEnabled;
    private WarningManager warningManager;
    private InitMethods initMethods;
    private BackupManager backupManager;
    private MenuAddonManager menuAddonManager;
    private StaffChatHandler staffChatHandler;
    private TPSMeter tpsMeter;
    private PluginDescriptionManager pluginDescriptionManager;
    private PermissionsManager permissionsManager;
    private AutoUpdaterManager autoUpdaterManager;
    private PluginStateManager pluginStateManager;
    private FileCorruptionManager fileCorruptionManager;
    private APDependencyManager dependencyManager;
    private RepositoryController repositoryController;
    private PluginUtils globalPluginUtils;
    private CoolStuffLib coolStuffLib;

    /**
     * Returns the Prefix of the Plugin
     *
     * @return The Prefix of the Plugin
     */
    public static String getPrefix() {
        return prefix;
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

    public AutoUpdaterManager getAutoUpdaterManager() {
        return autoUpdaterManager;
    }

    public FileCorruptionManager getFileCorruptionManager() {
        return fileCorruptionManager;
    }

    public RepositoryController getRepositoryController() {
        return repositoryController;
    }

    public PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    public PluginDescriptionManager getPluginDescriptionManager() {
        return pluginDescriptionManager;
    }

    public TPSMeter getTpsMeter() {
        return tpsMeter;
    }

    public CoolStuffLib getCoolStuffLib() {
        return coolStuffLib;
    }

    public MenuAddonManager getMenuAddonManager() {
        return menuAddonManager;
    }

    public BackupManager getBackupManager() {
        return backupManager;
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

    public CommandManagerRegistry getCommandManagerRegistry() {
        return commandManagerRegistry;
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
            fileLogger.writeToLog(Level.SEVERE, "Error updating Config File from AdminPanel", LogPrefixExtension.ADMINPANEL_MAIN);
            e.printStackTrace();
        }
        reloadConfig();
    }

    public InitMethods getInitMethods() {
        return initMethods;
    }

    public File getConfigFile() {
        return new File(getDataFolder(), "config.yml");
    }

    @Override
    public void onLoad() {
        setPlugin(this);
        setupBackupManager();
        setupFileLogger();
        new AdminPanelUtils();

        fileCorruptionManager = new FileCorruptionManager(this, backupManager.getFileBackup("ConfigBackup"));
        fileCorruptionManager.handleConfigBackupCheck();

        saveDefaultConfig();
        logger = StartUpLogger.create();

        dependencyManager = new APDependencyManager(this);
        dependencyManager.loadCoreDependencies();

        File file = new File(getDataFolder() + "/addons/onLoadExecution.yml");
        if (file.exists()) {
            logger.coloredSpacer(ChatColor.BLUE);
            logger.coloredMessage(ChatColor.GREEN, "Loading Addons on Startup/Load!");
            logger.coloredSpacer(ChatColor.BLUE);
            loader = new AddonLoader(new File(this.getDataFolder() + "/addons"));
            FileConfiguration tempConfig = YamlConfiguration.loadConfiguration(file);
            tempConfig.getConfigurationSection("OnLoad").getKeys(false).forEach(addon -> {
                String path = "OnLoad." + addon + ".";
                File addonFile = new File(this.getDataFolder() + "/addons/" + tempConfig.getString(path + "FileName"));
                String addonName = tempConfig.getString(path + "Name");
                try {
                    loader.loadAddon(addonFile);
                    logger.coloredMessage(ChatColor.GREEN, "Loaded Addon: " + addonName);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                try {
                    loader.enableAddon(addonFile, new HashSet<>(), true);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
            logger.coloredSpacer(ChatColor.BLUE);
            logger.coloredMessage(ChatColor.GREEN, "Finished loading Addons on Startup/Load!");
            logger.coloredSpacer(ChatColor.BLUE);
        }
    }

    @Override
    public void onEnable() {
        pluginDescriptionManager = new PluginDescriptionManager();
        globalPluginUtils = new PluginUtils();
        lastStartTimeMillis = System.currentTimeMillis();
        CoolStuffLibBuilder coolStuffLibBuilder = new CoolStuffLibBuilder(this)
                .setDataFile(new File(getDataFolder(), "data.yml"))
                .withLogging().create(fileLogger).build()
                .setUsePlayerLangHandler(true)
                .setSendSyntaxOnZeroArgs(true);
        tpsMeter = new TPSMeter();
        setPlugin(this);

        setupRepositoryController();
        getDependencyManager().useHookSystem();
        Metrics metrics = setupMetrics();
        sendStartupMessages();
        setupPluginStateManager();
        setupMenuAddonManager(coolStuffLibBuilder);
        setupLanguageManager(coolStuffLibBuilder);
        setupCommandManagerRegistry(coolStuffLibBuilder);
        coolStuffLib = coolStuffLibBuilder.createCoolStuffLib();
        coolStuffLib.setup();
        menuAddonManager = coolStuffLib.getMenuAddonManager();
        languageManager = coolStuffLib.getLanguageManager();
        commandManagerRegistry = coolStuffLib.getCommandManagerRegistry();
        setupAPI();
        initializeUtils();
        registerBungeeChannels();
        initPluginChecks();
        initializeConfigFiles();
        initializeWarningManager();
        setupPermissionsManager();
        initializePrefix();
        initEvents();
        setupLanguageHandler();
        enableLanguageManager();
        setupStaffChatHandler();
        updateConfigFiles();
        initializebStats(metrics);
        initializeUpdater();
        setupAutoUpdaterManager();
        setupPermissions();
        initializeCommands();
        initializeCommandManagers();
        setupAddonLoader();
        executeBackupManager();
        logStartupMessage();
    }

    private void setupRepositoryController() {
        Properties properties = new Properties();
        File propertiesFile = new File(getDataFolder(), "database.properties");
        saveResource("database.properties", false);
        InputStream is = null;
        try {
            if (propertiesFile.exists()) {
                is = new FileInputStream(propertiesFile);
            } else {
                is = getClass().getResourceAsStream("/database.properties");
            }
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                assert is != null;
                is.close();
            } catch (IOException e) {
                fileLogger.writeToLog(Level.SEVERE, "Error closing InputStream: " + e.getMessage(), LogPrefixExtension.ADMINPANEL_MAIN, true);
            }
        }
        DatabaseProperties databaseProperties = new DatabaseProperties();
        String host = properties.getProperty("host", "localhost");
        int port = Integer.parseInt(properties.getProperty("port", "-1"));
        String databaseFilePath = properties.getProperty("sqlite_file_path");
        String username = properties.getProperty("username", "root");
        String password = properties.getProperty("password", "password");
        String driver = properties.getProperty("dbtype");
        String databasePrefix = properties.getProperty("database_prefix", "");
        if (driver == null || driver.isEmpty()) {
            driver = "sqlite";
            if (databaseFilePath.isEmpty()) {
                databaseFilePath = "AdminPanelDatabase.db";
            }
            if (!databaseFilePath.startsWith(getDataFolder().getAbsolutePath())) {
                databaseFilePath = new File(getDataFolder(), databaseFilePath).getAbsolutePath();
            }
            fileLogger.writeToLog(Level.WARNING, "Database type not specified. Defaulting to SQLite.", LogPrefixExtension.ADMINPANEL_MAIN.getLogPrefix(), true);
            fileLogger.writeToLog(Level.WARNING, "You might experience issues if you switch to MySQL later.", LogPrefixExtension.ADMINPANEL_MAIN.getLogPrefix(), true);
        } else if (driver.equalsIgnoreCase("sqlite")) {
            if (!databaseFilePath.startsWith(getDataFolder().getAbsolutePath())) {
                databaseFilePath = new File(getDataFolder(), databaseFilePath).getAbsolutePath();
            }
        }
        databaseProperties.setDriver(driver);
        databaseProperties.setHost(host);
        databaseProperties.setPort(String.valueOf(port));
        databaseProperties.setDatabase(databaseFilePath);
        databaseProperties.setUsername(username);
        databaseProperties.setPassword(password);
        databaseProperties.setDatabasePrefix(databasePrefix);
        fileLogger.writeToLog(Level.INFO, "Using Database: " + databaseProperties.getDriver() + " at " + databaseProperties.getHost() + ":" + databaseProperties.getPort() +
                " with Database File: " + databaseProperties.getDatabase(), LogPrefixExtension.ADMINPANEL_MAIN, true);

        repositoryController = new RepositoryController(this, new File(getDataFolder(), "repo_registration.json"), databaseProperties);
        repositoryController.loadRepositoriesFromFile();
    }

    private Metrics setupMetrics() {
        int bStatsID = 11778;
        return new Metrics(this, bStatsID);
    }

    public APDependencyManager getDependencyManager() {
        return dependencyManager;
    }

    private void sendStartupMessages() {
        logger
                .coloredSpacer(ChatColor.GREEN)
                .messages("&e&lStarting Admin Panel Plugin:&r");
        logger.coloredSpacer(ChatColor.DARK_RED).message("&4&lInitialize Plugin Main Variable to this!&r");
    }

    private void setupFileLogger() {
        fileLogger = new CustomPluginFileLogger(this, "plugin.log");
    }

    private void setupMenuAddonManager(CoolStuffLibBuilder builder) {
        builder.withMenuSystem().build();
    }

    private void setupLanguageManager(CoolStuffLibBuilder builder) {
        // TODO Maybe reimplement the MySQL Language Manager
        //boolean mysqlLanguageManagerTemp = false;
        new InternalCacheHandler();
        builder.withLanguageManager()
                .setLanguageFolder(new File(this.getDataFolder() + "/languages"))
                .setResourceDirectory("languages")
                .setPrefix("[Admin-Panel]")
                .enablePlayerLanguageHandler()
                .build();
    }

    private void setupCommandManagerRegistry(CoolStuffLibBuilder builder) {
        builder.withCommandManager().enableSyntaxOnZeroArgs().build();
    }

    private void setupAPI() {
        API = new LocalAdminPanelAPI(this);
    }

    private void initializeUtils() {
        initMethods = new InitMethods(logger, Bukkit.getPluginManager(), plugin);
        new ChatUtil();
    }

    private void setupBackupManager() {
        new File(this.getDataFolder() + "/languages").mkdir();
        String backupFolderConfig = getConfig().getString("Plugin.BackupManager.BackupFolder", "backups"); // Default "backups"
        int numberOfBackupsBeforeDeleting = getConfig().getInt("Plugin.BackupManager.NumberOfBackupsAtOnce", 5); // Default 5
        backupManager = new BackupManager(plugin, numberOfBackupsBeforeDeleting, getTimeInTicks(), backupFolderConfig + "/");
        if (!new File(this.getDataFolder(), backupFolderConfig).exists()) {
            new File(this.getDataFolder(), backupFolderConfig).mkdirs();
        }
        List<RegexFileFilter> filters = new ArrayList<>();
        List<RegexFileFilter> excludeFilters = new ArrayList<>();
        filters.add(new RegexFileFilter(".*\\.(yml|db|properties)"));
        excludeFilters.add(new RegexFileFilter(".*\\.txt"));
        excludeFilters.add(new RegexFileFilter("addons/.*\\.jar"));
        backupManager.addFileBackup(new FileBackup(
                "ConfigBackup",
                filters,
                excludeFilters,
                backupManager.getBackupFolder() + "ConfigBackup"
        ));
    }

    private long getTimeInTicks() {
        String backupTime = getConfig().getString("Plugin.BackupManager.BackupTime", "24");
        if (backupTime.isEmpty()) {
            throw new IllegalArgumentException("Backup time is not configured properly.");
        }

        char lastChar = backupTime.charAt(backupTime.length() - 1);
        long timeValue;
        long ticks;

        if (Character.isDigit(lastChar)) {
            timeValue = Long.parseLong(backupTime);
            ticks = timeValue * 20 * 60 * 60;
        } else {
            String timeValueStr = backupTime.substring(0, backupTime.length() - 1);
            try {
                timeValue = Long.parseLong(timeValueStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid time value: " + timeValueStr);
            }

            ticks = switch (lastChar) {
                case 's' -> timeValue * 20;
                case 'm' -> timeValue * 20 * 60;
                case 'd' -> timeValue * 20 * 60 * 60 * 24;
                default -> timeValue * 20 * 60 * 60;
            };
        }

        return ticks;
    }

    private void registerBungeeChannels() {
        logger.message("&e&lStarting Bungee Registration");
        if (pluginStateManager.checkIfBungee(true)) {
            String path = "Plugin.BungeeSyncSystem.ChannelNames.";
            bungeeUtils = new BungeeUtils(getConfig().getString(path + "In", ""), getConfig().getString(path + "Out", ""));
            bungeeUtils.openBungeeChannel();
            path = "Plugin.BungeeSyncSystem.JavaSockets.";
            if (getConfig().getBoolean(path + "enabled")) {
                try {
                    dataClient = new DataClient(getConfig().getString(path + "hostName"),
                            getConfig().getInt(path + "port"),
                            getConfig().getString(path + "ClientName"));
                    dataClientUtils = new DataClientUtils(dataClient);
                } catch (IOException e) {
                    fileLogger.writeToLog(Level.SEVERE, "Error while initializing DataClient (" + e + ": " + e.getMessage() + ")", LogPrefixExtension.ADMINPANEL_MAIN);
                }
            }
        }
        logger.message("&a&lDone");
    }

    private void initPluginChecks() {
        initMethods.initPluginCheck();
        logger.messages("&c&lFinished Vault initialization!&r");
    }

    private void initializeConfigFiles() {
        initMethods.initConfigFiles(fileLogger);
    }

    private void setupPluginStateManager() {
        pluginStateManager = new PluginStateManager(getConfig(), this);
    }

    private void initializeWarningManager() {
        if (getConfig().getBoolean("Pman.Actions.WarningSystem")) {
            warningManager = new WarningManager(
                    plugin,
                    new File(this.getDataFolder() + "/warnings.yml"));
            warningManager.loadWarnings();
        }
    }

    private void setupPermissionsManager() {
        permissionsManager = new PermissionsManager(this, 5 * 60 * 20L);
    }

    private void initializePrefix() {
        logger.message("&3&lMain.Prefix &9= &7Config.Plugin.Prefix&r");
        setPrefix(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getConfig().getString("Plugin.Prefix"))));
        languageManager.setPrefix(getPrefix());
        logger.message("&e&lPrefix Done!&r");
    }

    private void initEvents() {
        initMethods.initEvents();
    }

    private void setupLanguageHandler() {
        dataYML = YamlConfiguration.loadConfiguration(new File(getDataFolder() + "/data.yml"));
        languageManager.setPLHandler(new PerPlayerLanguageHandler(languageManager, new File(getDataFolder() + "/data.yml"), dataYML));
    }

    private void enableLanguageManager() {
        LanguageFile deLang = new LanguageFile(new File(getDataFolder() + "/languages"), "languages", "de");
        LanguageFile enLang = new LanguageFile(new File(getDataFolder() + "/languages"), "languages", "en");
        boolean autoLanguageFileUpdating = getConfig().getBoolean("Plugin.Updater.AutomaticLanguageFileUpdating");
        languageManager.addLanguagesToList(true);
        languageManager.addLang(deLang, deLang.getLangName());
        languageManager.addLang(enLang, enLang.getLangName());
        languageManager.setCurrentLang(languageManager.getLang(getConfig().getString("Plugin.language"), true), true);
        setLanguageManagerEnabled(languageManager != null && languageManager.getCurrentLang() != null);
        if (isLanguageManagerEnabled()) {
            getServer().getConsoleSender().sendMessage(languageManager.getMessage("Plugin.EnablingMessage", null, true));
        } else {
            getServer().getConsoleSender().sendMessage(AdminPanelUtils.chat("&f[&aAdmin-&ePanel&f] &cLanguage Manager is not enabled, " +
                    "which means all the Items and Messages won't work! " +
                    "The Plugin will automatically unload! " +
                    "Look for Errors from the Admin-Panel in the Console!"));
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void setupStaffChatHandler() {
        staffChatHandler = new StaffChatHandler();
        Bukkit.getPluginManager().registerEvents(new StaffChatHandler(), plugin);
    }

    private void updateConfigFiles() {
        updateConfig();
        boolean autoLanguageFileUpdating = getConfig().getBoolean("Plugin.Updater.AutomaticLanguageFileUpdating");
        if (autoLanguageFileUpdating) {
            System.out.println("Auto Language File Updating is enabled!");
            /*

            if (languageManager instanceof MySQLLanguageManager) {
                System.out.println("Language IDs: " + ((MySQLLanguageManager) languageManager).getDatabaseController().getInnerLanguageManager().getLanguageIDs());
                System.out.println("Language Converter Map: " + ((MySQLLanguageManager) languageManager).getDatabaseController().getInnerLanguageManager().getConverterManager().getLanguageConverterMap());
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
                        languageManager.reloadLanguages(null, true);
                    }
                }.runTaskLater(this, 20L * 10);
            } else {
            languageManager.reloadLanguages(null, false);
            }
             */
            languageManager.reloadLanguages(null, false);
        }
    }

    private void initializebStats(Metrics metrics) {
        initMethods.initbStatsMetrics(metrics);
    }

    private void initializeUpdater() {
        updater = new NewUpdater(plugin, 91800, "Admin-Panel-%version%.jar", plugin, "", true);
        initMethods.initUpdater(updater, dataYML);
    }

    private void setupAutoUpdaterManager() {
        if (plugin.getConfig().getBoolean("Plugin.Updater.PluginUpdater.enabled")) {
            autoUpdaterManager = new AutoUpdaterManager(plugin);
            autoUpdaterManager.setup(updater);
        }
    }

    private void setupPermissions() {
        repositoryController.addRepositoryToRegistrationFile(
                PlayerPermissionRepository.class,
                PlayerPermission.class,
                "Repository for PermissionEntry",
                true
        );
        permissionsManager.setup();
    }

    private void initializeCommands() {
        initMethods.initCommands();
    }

    private void initializeCommandManagers() {
        initMethods.initCommandManagers();
    }

    private void setupAddonLoader() {
        if (plugin.getConfig().getBoolean("Plugin.AddonSystem.enabled")) {
            logger.emptySpacer();
            logger.emptySpacer();
            logger.coloredSpacer(ChatColor.RED);
            logger.message("&cIt could happen that an Addon needs another Plugin to work.&r");
            logger.message("&cThen this Addon will not start and the Loader&r");
            logger.message("&cwill continue loading!&r");
            logger.coloredSpacer(ChatColor.RED);
            if (loader == null) {
                loader = new AddonLoader(new File(this.getDataFolder() + "/addons"));
            }
            initMethods.initAddonLoader(loader);
        }
    }

    private void executeBackupManager() {
        backupManager.startBackup("ConfigBackup");
    }

    private void logStartupMessage() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        getFileLogger().writeToLog(Level.INFO, "Admin-Panel successfully started on '" + dtf.format(now) + "'", LogPrefixExtension.DATELOGGER);
    }

    @Override
    public void onDisable() {
        if (loader != null) {
            loader.crashAddons();
        }
        loader = null;
        if (warningManager != null) {
            warningManager.saveWarnings();
        }
        if (pluginStateManager.checkIfBungee(false)) {
            bungeeUtils.closeBungeeChannel();
            String path = "Plugin.BungeeSyncSystem.JavaSockets.";
            if (getConfig().getBoolean(path + "enabled")) {
                dataClient.disconnect(true);
                dataClient.getStatsManager().saveStatsToFile();
            }
        }

        if (isLanguageManagerEnabled()) {
            getServer().getConsoleSender().sendMessage(languageManager.getMessage("Plugin.DisablingMessage", null, true));
        } else {
            getServer().getConsoleSender().sendMessage("[Admin-Panel] disabled!");
        }
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

    public CustomPluginFileLogger getFileLogger() {
        return fileLogger;
    }

    public File getPluginFile() {
        return this.getFile();
    }

    public StartUpLogger getStartUpLogger() {
        return logger;
    }

    public PluginUtils getPluginUtils() {
        return globalPluginUtils;
    }

    public WarningManager getWarningManager() {
        return warningManager;
    }

    public AddonLoader getAddonLoader() {
        return loader;
    }

    public PluginStateManager getPluginStateManager() {
        return pluginStateManager;
    }

    public boolean isSendSyntaxOnArgsZero() {
        return getConfig().getBoolean("Plugin.SendSyntaxOnArgsZero", true);
    }
}
