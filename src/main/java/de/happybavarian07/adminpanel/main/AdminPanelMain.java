package de.happybavarian07.adminpanel.main;

import de.happybavarian07.coolstufflib.utils.ChatUtils;
import de.happybavarian07.adminpanel.startup.Startup;
import de.happybavarian07.adminpanel.startup.StartupAbortedException;
import de.happybavarian07.coolstufflib.utils.StartUpLogger;

import de.happybavarian07.adminpanel.addonloader.loadingutils.AddonLoader;
import de.happybavarian07.coolstufflib.configstuff.ConfigUpdater;
import de.happybavarian07.adminpanel.data.AdminPanelData;
import de.happybavarian07.adminpanel.journal.AdminJournal;
import de.happybavarian07.adminpanel.data.LegacyDataImporter;
import de.happybavarian07.adminpanel.data.LegacyWarningImporter;
import de.happybavarian07.adminpanel.data.PluginDescriptionEntry;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.troll.TrollToggleEvent;
import de.happybavarian07.adminpanel.listeners.InternalCacheHandler;
import de.happybavarian07.adminpanel.listeners.StaffChatHandler;
import de.happybavarian07.adminpanel.permissions.GroupNodeEntity;
import de.happybavarian07.adminpanel.permissions.GroupNodeRepository;
import de.happybavarian07.adminpanel.permissions.PermissionGroupEntity;
import de.happybavarian07.adminpanel.permissions.PermissionGroupRepository;
import de.happybavarian07.adminpanel.permissions.PermissionService;
import de.happybavarian07.adminpanel.permissions.PermissionsManager;
import de.happybavarian07.adminpanel.permissions.PlayerPermission;
import de.happybavarian07.adminpanel.permissions.PlayerPermissionRepository;
import de.happybavarian07.adminpanel.syncing.DataClient;
import de.happybavarian07.adminpanel.syncing.DataClientUtils;
import de.happybavarian07.adminpanel.syncing.utils.BungeeUtils;
import de.happybavarian07.adminpanel.trolls.TrollCatalog;
import de.happybavarian07.adminpanel.trolls.TrollListener;
import de.happybavarian07.adminpanel.trolls.TrollRegistry;
import de.happybavarian07.adminpanel.utils.*;
import de.happybavarian07.adminpanel.utils.dependencyloading.ServerPlatformDetector;
import de.happybavarian07.adminpanel.utils.managers.*;
import de.happybavarian07.adminpanel.warnings.BukkitEscalationExecutor;
import de.happybavarian07.adminpanel.warnings.WarningManager;
import de.happybavarian07.adminpanel.warnings.WarningSettings;
import de.happybavarian07.coolstufflib.CoolStuffLib;
import de.happybavarian07.coolstufflib.CoolStuffLibBuilder;
import de.happybavarian07.coolstufflib.backupmanager.BackupManager;
import de.happybavarian07.coolstufflib.backupmanager.FileBackup;
import de.happybavarian07.coolstufflib.backupmanager.RegexFileFilter;
import de.happybavarian07.coolstufflib.commandmanagement.CommandManagerRegistry;
import de.happybavarian07.coolstufflib.jpa.utils.DatabaseProperties;
import de.happybavarian07.coolstufflib.languagemanager.LanguageFile;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import de.happybavarian07.coolstufflib.languagemanager.PerPlayerLanguageHandler;
import de.happybavarian07.coolstufflib.menusystem.MenuAddonManager;
import de.happybavarian07.coolstufflib.repository.RepositoryManager;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    private final Startup startup = new Startup(AdminPanelMain::logError);
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
    private ExecutorService warningIo;
    private ExecutorService journalIo;
    private AdminJournal journal;
    private TrollRegistry trollRegistry;
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
    private PluginUtils globalPluginUtils;
    private CoolStuffLib coolStuffLib;
    private AdminPanelData data;
    private LegacyDataImporter.LegacyData legacyData;
    private File legacyFile;

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

    /**
     * Writes an error with its stack trace to the plugin log file and the console.
     *
     * <p>Safe to call before {@link #onLoad()} has created the file logger, for example from static
     * initializers: the error then goes to the server logger instead and nothing is lost.</p>
     *
     * @param context what was being done when the error happened, shown in front of the stack trace
     * @param error   the failure to log
     */
    public static void logError(String context, Throwable error) {
        if (fileLogger == null) {
            java.util.logging.Logger.getLogger("Admin-Panel").log(Level.SEVERE, context, error);
            return;
        }
        java.io.StringWriter trace = new java.io.StringWriter();
        error.printStackTrace(new java.io.PrintWriter(trace));
        fileLogger.writeToLog(Level.SEVERE, context + ": " + trace, LogPrefixExtension.ADMINPANEL_MAIN, true);
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

    public AdminPanelData getData() {
        return data;
    }

    public RepositoryManager getRepositoryManager() {
        return coolStuffLib.getRepositoryManager();
    }

    public PermissionService getPermissionService() {
        return permissionsManager.getService();
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
        return dataClient;
    }

    public DataClientUtils getDataClientUtils() {
        return dataClientUtils;
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
            logError("Could not update config.yml", e);
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
        logger = StartUpLogger.create(this);

        ServerPlatformDetector.isHybridServer();
        logger.coloredMessage(ChatColor.YELLOW, "Detected server platform: " +
                ServerPlatformDetector.getPlatformName());
        if (ServerPlatformDetector.isHybridServer()) {
            logger.coloredMessage(ChatColor.YELLOW, "Running on hybrid server - using alternative classloading");
        }

        dependencyManager = new APDependencyManager(this);
        dependencyManager.loadCoreDependencies();

        File file = new File(getDataFolder() + "/addons/onLoadExecution.yml");
        if (file.exists()) {
            logger.coloredSpacer(ChatColor.BLUE);
            logger.coloredMessage(ChatColor.GREEN, "Loading Addons on Startup/Load!");
            logger.coloredSpacer(ChatColor.BLUE);
            loader = new AddonLoader(new File(this.getDataFolder() + "/addons"));
            FileConfiguration tempConfig = YamlConfiguration.loadConfiguration(file);
            var onLoadSection = tempConfig.getConfigurationSection("OnLoad");
            if (onLoadSection != null) {
                onLoadSection.getKeys(false).forEach(addon -> {
                    String path = "OnLoad." + addon + ".";
                    File addonFile = new File(this.getDataFolder() + "/addons/" + tempConfig.getString(path + "FileName"));
                    String addonName = tempConfig.getString(path + "Name");
                    try {
                        loader.loadAddon(addonFile);
                        loader.enableAddon(addonFile, new HashSet<>(), true);
                        logger.coloredMessage(ChatColor.GREEN, "Loaded Addon: " + addonName);
                    } catch (IOException | ClassNotFoundException | RuntimeException e) {
                        fileLogger.writeToLog(Level.SEVERE, "Could not load addon " + addonName + " (" + addonFile.getName() + "): " + e,
                                LogPrefixExtension.ADMINPANEL_MAIN, true);
                    }
                });
            }
            logger.coloredSpacer(ChatColor.BLUE);
            logger.coloredMessage(ChatColor.GREEN, "Finished loading Addons on Startup/Load!");
            logger.coloredSpacer(ChatColor.BLUE);
        }
    }

    @Override
    public void onEnable() {
        try {
            runStartupStages();
        } catch (StartupAbortedException aborted) {
            getLogger().severe(aborted.getMessage());
            getLogger().severe("Admin-Panel cannot run without it and is disabling itself. " + startup.summary());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info(startup.summary());
        logStartupMessage();
    }

    private void runStartupStages() {
        globalPluginUtils = new PluginUtils();
        lastStartTimeMillis = System.currentTimeMillis();
        tpsMeter = new TPSMeter();
        setPlugin(this);

        startup.run("libraries", true, this::setupLibraries);
        startup.run("data", true, () -> {
            data = new AdminPanelData(getRepositoryManager());
            importLegacyTables();
            setupPluginStateManager();
            pluginDescriptionManager = new PluginDescriptionManager();
            menuAddonManager = coolStuffLib.getMenuAddonManager();
            languageManager = coolStuffLib.getLanguageManager();
            commandManagerRegistry = coolStuffLib.getCommandManagerRegistry();
        }, "libraries");
        startup.run("api", true, () -> {
            setupAPI();
            initializeUtils();
        }, "data");
        startup.run("bungee", false, this::registerBungeeChannels, "data");
        startup.run("placeholderapi", true, initMethods::initPlaceholderApi, "api");
        startup.run("supervanish", false, initMethods::initSuperVanish, "api");
        startup.run("vault", false, initMethods::initVault, "api");
        startup.run("config", true, this::initializeConfigFiles, "api");
        startup.run("warnings", false, this::initializeWarningManager, "data", "config");
        startup.run("journal", false, this::initializeJournal, "data", "config");
        startup.run("trolls", false, this::initializeTrollRegistry, "data", "config");
        startup.run("permissions", true, this::setupPermissionsManager, "data");
        startup.run("prefix", true, this::initializePrefix, "config");
        startup.run("events", true, this::initEvents, "permissions");
        startup.run("language", true, () -> {
            setupLanguageHandler();
            importLegacyLanguages();
            enableLanguageManager();
        }, "config");
        startup.run("staffchat", true, this::setupStaffChatHandler, "language");
        startup.run("configupdate", true, this::updateConfigFiles, "language");
        startup.run("bstats", false, () -> initializebStats(setupMetrics()), "language");
        startup.run("updater", false, this::initializeUpdater, "language");
        startup.run("autoupdater", false, this::setupAutoUpdaterManager, "updater");
        startup.run("permissionsetup", true, this::setupPermissions, "permissions", "language");
        startup.run("commands", true, () -> {
            initializeCommands();
            initializeCommandManagers();
        }, "language");
        startup.run("addons", false, this::setupAddonLoader, "commands");
        startup.run("backup", false, this::executeBackupManager, "config");
    }

    private void setupLibraries() {
        CoolStuffLibBuilder coolStuffLibBuilder = new CoolStuffLibBuilder(this)
                .setDataFile(new File(getDataFolder(), "data.yml"))
                .setBackupManager(backupManager)
                .withLogging().create(fileLogger).build();
        configureDatabase(coolStuffLibBuilder);
        getDependencyManager().useHookSystem();
        sendStartupMessages();
        setupMenuAddonManager(coolStuffLibBuilder);
        setupLanguageManager(coolStuffLibBuilder);
        setupCommandManagerRegistry(coolStuffLibBuilder);
        coolStuffLib = coolStuffLibBuilder.createCoolStuffLib();
        coolStuffLib.setup();
    }

    public Startup getStartup() {
        return startup;
    }

    public static final String DB_CONNECTION = "main";

    private void configureDatabase(CoolStuffLibBuilder builder) {
        File propertiesFile = new File(getDataFolder(), "database.properties");
        if (!propertiesFile.exists()) saveResource("database.properties", false);
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(propertiesFile)) {
            properties.load(in);
        } catch (IOException e) {
            fileLogger.writeToLog(Level.SEVERE, "Could not read database.properties, using SQLite defaults: " + e.getMessage(), LogPrefixExtension.ADMINPANEL_MAIN, true);
        }
        DatabaseProperties databaseProperties = DatabaseSettings.parse(properties, getDataFolder());
        fileLogger.writeToLog(Level.INFO, "Using Database: " + databaseProperties.getDriver() + " (" + databaseProperties.getDatabase() + ")", LogPrefixExtension.ADMINPANEL_MAIN, true);
        builder.withRepositoryManager()
                .setDatabasePrefix(databaseProperties.getDatabasePrefix())
                .addConnection(DB_CONNECTION, databaseProperties)
                .build();
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
                .enableCommands("aplang")
                .watchFiles(true)
                .build();
    }

    private void setupCommandManagerRegistry(CoolStuffLibBuilder builder) {
        builder.withCommandManager().enableSyntaxOnZeroArgs().build();
    }

    private void importLegacyTables() {
        legacyFile = LegacyDataImporter.findLegacyFile(getDataFolder());
        if (legacyFile == null) return;
        try {
            legacyData = LegacyDataImporter.read(legacyFile);
        } catch (SQLException e) {
            fileLogger.writeToLog(Level.SEVERE, "Could not import " + legacyFile + ": " + e.getMessage(), LogPrefixExtension.ADMINPANEL_MAIN, true);
            legacyFile = null;
            return;
        }
        legacyData.pluginDescriptions().forEach((name, description) ->
                data.descriptions().save(new PluginDescriptionEntry(name, description, -1)));
        legacyData.flags().forEach(data::setFlag);
    }

    private void importLegacyLanguages() {
        if (legacyData == null) return;
        legacyData.playerLanguages().forEach((uuid, language) -> {
            try {
                if (languageManager.getLang(language, false) != null) {
                    languageManager.getPLHandler().setPlayerLanguage(UUID.fromString(uuid), language);
                }
            } catch (IllegalArgumentException ignored) {
                // not a UUID, skip the entry
            }
        });
        File done = new File(legacyFile.getParentFile(), legacyFile.getName() + ".imported");
        if (!legacyFile.renameTo(done)) {
            fileLogger.writeToLog(Level.WARNING, "Imported " + legacyFile + " but could not rename it; it will be imported again next start",
                    LogPrefixExtension.ADMINPANEL_MAIN, true);
        }
        fileLogger.writeToLog(Level.INFO, "Imported old data from " + legacyFile + ": " + legacyData.playerLanguages().size()
                + " player languages, " + legacyData.pluginDescriptions().size() + " plugin descriptions, "
                + legacyData.flags().size() + " flags", LogPrefixExtension.ADMINPANEL_MAIN, true);
        legacyData = null;
    }

    private void setupAPI() {
        API = new LocalAdminPanelAPI(this);
    }

    private void initializeUtils() {
        initMethods = new InitMethods(logger, Bukkit.getPluginManager(), plugin);
    }

    private void setupBackupManager() {
        new File(this.getDataFolder() + "/languages").mkdir();
        String backupFolderConfig = getConfig().getString("Plugin.BackupManager.BackupFolder", "backups");
        int numberOfBackupsBeforeDeleting = getConfig().getInt("Plugin.BackupManager.NumberOfBackupsAtOnce", 5);
        backupManager = new BackupManager(numberOfBackupsBeforeDeleting, getBackupIntervalSeconds());
        List<RegexFileFilter> filters = List.of(new RegexFileFilter(".*\\.(yml|db|properties)"));
        List<RegexFileFilter> excludeFilters = List.of(new RegexFileFilter(".*\\.txt"), new RegexFileFilter("addons/.*\\.jar"));
        File destination = new File(new File(getDataFolder(), backupFolderConfig), "ConfigBackup");
        destination.mkdirs();
        backupManager.addFileBackup(new FileBackup("ConfigBackup", filters, excludeFilters, destination, getDataFolder()));
    }

    /** "24" = hours, or a number with s/m/h/d, from Plugin.BackupManager.BackupTime. */
    private long getBackupIntervalSeconds() {
        String backupTime = getConfig().getString("Plugin.BackupManager.BackupTime", "24");
        if (backupTime.isEmpty()) {
            throw new IllegalArgumentException("Backup time is not configured properly.");
        }
        char unit = backupTime.charAt(backupTime.length() - 1);
        String number = Character.isDigit(unit) ? backupTime : backupTime.substring(0, backupTime.length() - 1);
        long value;
        try {
            value = Long.parseLong(number);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid time value: " + number);
        }
        return switch (unit) {
            case 's' -> value;
            case 'm' -> value * 60;
            case 'd' -> value * 60 * 60 * 24;
            default -> value * 60 * 60;
        };
    }

    private void registerBungeeChannels() {
        logger.message("&e&lStarting Bungee Registration");
        if (!pluginStateManager.checkIfBungee(true)) {
            throw new Startup.DisabledException("not running behind BungeeCord, or Plugin.BungeeSyncSystem.enabled is false");
        }
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
                throw new IllegalStateException("Could not connect the socket data client: " + e.getMessage(), e);
            }
        }
        logger.message("&a&lDone");
    }

    private void initializeConfigFiles() {
        initMethods.initConfigFiles(fileLogger);
    }

    private void setupPluginStateManager() {
        pluginStateManager = new PluginStateManager(getConfig(), data, this);
    }

    private void initializeWarningManager() {
        if (!getConfig().getBoolean("Pman.Actions.WarningSystem")) {
            throw new Startup.DisabledException("Pman.Actions.WarningSystem is false");
        }
        importLegacyWarnings();
        warningIo = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "AdminPanel-Warnings");
            thread.setDaemon(true);
            return thread;
        });
        warningManager = new WarningManager(data.warnings(), this::readWarningSettings, new BukkitEscalationExecutor(this),
                warningIo, task -> {
                    if (isEnabled()) Bukkit.getScheduler().runTask(this, task);
                    else task.run();
                }, System::currentTimeMillis, AdminPanelMain::logError);
        warningManager.sweep();
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, warningManager::sweep, 20L * 60, 20L * 60);
    }

    private void initializeJournal() {
        if (!getConfig().getBoolean("Journal.Enabled", true)) {
            throw new Startup.DisabledException("Journal.Enabled is false");
        }
        journalIo = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "AdminPanel-Journal");
            thread.setDaemon(true);
            return thread;
        });
        journal = new AdminJournal(data.actions(), journalIo, System::currentTimeMillis, AdminPanelMain::logError,
                id -> Bukkit.getOfflinePlayer(id).getName());
        journal.setCategoryFilter(category -> getConfig().getBoolean("Journal.Record." + category, true));
        AdminJournal.install(journal);
        long purged = journal.purgeOlderThanDays(getConfig().getInt("Journal.KeepDays", 0));
        if (purged > 0) {
            fileLogger.writeToLog(Level.INFO, "Removed " + purged + " admin journal entries older than " + getConfig().getInt("Journal.KeepDays", 0) + " days", LogPrefixExtension.ADMINPANEL_MAIN, true);
        }
    }

    public AdminJournal getJournal() {
        return journal;
    }

    private void initializeTrollRegistry() {
        trollRegistry = new TrollRegistry(
                id -> getConfig().getBoolean("Pman.Actions.Trolls." + id, true),
                (troll, staff, target, starting) -> {
                    try {
                        TrollToggleEvent event = new TrollToggleEvent(staff, target, troll.id(), starting);
                        getAPI().callAdminPanelEvent(event);
                        return !event.isCancelled();
                    } catch (NotAPanelEventException e) {
                        logError("A troll action was not a valid panel event", e);
                        return true;
                    }
                },
                Bukkit::getOnlinePlayers, AdminPanelMain::logError);
        TrollCatalog.registerAll(trollRegistry, this, getLanguageManager());
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new TrollListener(trollRegistry), this);
        for (var troll : trollRegistry.all()) {
            if (troll instanceof Listener listener) pluginManager.registerEvents(listener, this);
        }
    }

    private WarningSettings readWarningSettings() {
        return WarningSettings.parse(getConfig().getString("Warnings.DefaultExpiry", "30d"), getConfig().getMapList("Warnings.Escalation"),
                message -> fileLogger.writeToLog(Level.WARNING, message, LogPrefixExtension.ADMINPANEL_MAIN, true));
    }

    private void importLegacyWarnings() {
        File legacyWarnings = new File(getDataFolder(), "warnings.yml");
        if (!legacyWarnings.isFile()) return;
        try {
            var warnings = LegacyWarningImporter.read(legacyWarnings, System.currentTimeMillis());
            data.warnings().saveAll(warnings);
            File done = new File(getDataFolder(), "warnings.yml.imported");
            if (!legacyWarnings.renameTo(done)) {
                fileLogger.writeToLog(Level.WARNING, "Imported " + legacyWarnings + " but could not rename it; it will be imported again next start",
                        LogPrefixExtension.ADMINPANEL_MAIN, true);
            }
            fileLogger.writeToLog(Level.INFO, "Imported " + warnings.size() + " warnings from " + legacyWarnings, LogPrefixExtension.ADMINPANEL_MAIN, true);
        } catch (RuntimeException e) {
            logError("Could not import " + legacyWarnings, e);
        }
    }

    private void setupPermissionsManager() {
        getRepositoryManager().registerRepository(PlayerPermissionRepository.class, PlayerPermission.class);
        getRepositoryManager().registerRepository(PermissionGroupRepository.class, PermissionGroupEntity.class);
        getRepositoryManager().registerRepository(GroupNodeRepository.class, GroupNodeEntity.class);
        permissionsManager = new PermissionsManager(this);
    }

    private void initializePrefix() {
        logger.message("&3&lMain.Prefix &9= &7Config.Plugin.Prefix&r");
        setPrefix(ChatUtils.chat(Objects.requireNonNull(getConfig().getString("Plugin.Prefix"))));
        languageManager.setPrefix(getPrefix());
        logger.message("&e&lPrefix Done!&r");
    }

    private void initEvents() {
        initMethods.initEvents();
    }

    private void setupLanguageHandler() {
        File dataFile = new File(getDataFolder(), "data.yml");
        dataYML = YamlConfiguration.loadConfiguration(dataFile);
        languageManager.setPLHandler(new PerPlayerLanguageHandler(languageManager, dataFile, dataYML));
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
        if (!isLanguageManagerEnabled()) {
            throw new IllegalStateException("the language '" + getConfig().getString("Plugin.language") + "' could not be loaded, "
                    + "all items and messages would be missing");
        }
        getServer().getConsoleSender().sendMessage(languageManager.getMessage("Plugin.EnablingMessage", null, true));
    }

    private void setupStaffChatHandler() {
        staffChatHandler = new StaffChatHandler();
        Bukkit.getPluginManager().registerEvents(staffChatHandler, plugin);
    }

    private void reportConfigProblems() {
        try (InputStream template = getResource("config.yml")) {
            if (template == null) return;
            YamlConfiguration shipped = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(template, java.nio.charset.StandardCharsets.UTF_8));
            for (ConfigValidator.Problem problem : ConfigValidator.validate(shipped, getConfig())) {
                fileLogger.writeToLog(Level.WARNING, "config.yml " + problem, LogPrefixExtension.ADMINPANEL_MAIN, true);
            }
        } catch (IOException e) {
            logError("Could not check config.yml", e);
        }
    }

    private void updateConfigFiles() {
        updateConfig();
        reportConfigProblems();
        boolean autoLanguageFileUpdating = getConfig().getBoolean("Plugin.Updater.AutomaticLanguageFileUpdating");
        if (autoLanguageFileUpdating) {
            fileLogger.writeToLog(Level.INFO, "Auto Language File Updating is enabled!", LogPrefixExtension.ADMINPANEL_MAIN, true);
            /*

            if (languageManager instanceof MySQLLanguageManager) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        while (((MySQLLanguageManager) languageManager).getDatabaseController().getInnerLanguageManager().getLanguageIDs().size() != ((MySQLLanguageManager) languageManager).getDatabaseController().getInnerLanguageManager().getConverterManager().getLanguageConverterMap().size()) {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                logError("Interrupted while waiting for the language converters", e);
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
        if (!plugin.getConfig().getBoolean("Plugin.Updater.PluginUpdater.enabled")) {
            throw new Startup.DisabledException("Plugin.Updater.PluginUpdater.enabled is false");
        }
        autoUpdaterManager = new AutoUpdaterManager(plugin);
        autoUpdaterManager.setup(updater);
    }

    private void setupPermissions() {
        permissionsManager.setup();
    }

    private void initializeCommands() {
        initMethods.initCommands();
    }

    private void initializeCommandManagers() {
        initMethods.initCommandManagers();
    }

    private void setupAddonLoader() {
        if (!plugin.getConfig().getBoolean("Plugin.AddonSystem.enabled")) {
            throw new Startup.DisabledException("Plugin.AddonSystem.enabled is false");
        }
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
        if (trollRegistry != null) {
            trollRegistry.stopEverything();
        }
        if (permissionsManager != null) {
            permissionsManager.shutdown();
        }
        AdminJournal.install(null);
        if (journalIo != null) {
            journalIo.shutdown();
            try {
                journalIo.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (warningIo != null) {
            warningIo.shutdown();
            try {
                warningIo.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (pluginStateManager != null) {
            pluginStateManager.saveAllState();
        }
        if (pluginStateManager != null && pluginStateManager.checkIfBungee(false)) {
            if (bungeeUtils != null) bungeeUtils.closeBungeeChannel();
            if (dataClient != null) {
                dataClient.disconnect(true);
                dataClient.getStatsManager().saveStatsToFile();
            }
        }

        if (isLanguageManagerEnabled()) {
            getServer().getConsoleSender().sendMessage(languageManager.getMessage("Plugin.DisablingMessage", null, true));
        } else {
            getServer().getConsoleSender().sendMessage("[Admin-Panel] disabled!");
        }
        if (coolStuffLib != null) {
            coolStuffLib.shutdown();
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

    /** Reloads data.yml into the same object, so everything holding {@link #getDataYML()} sees the new values. */
    public boolean reloadDataYML() {
        File dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) saveResource("data.yml", false);
        try {
            dataYML.load(dataFile);
            return true;
        } catch (IOException | InvalidConfigurationException e) {
            fileLogger.writeToLog(Level.SEVERE, "Could not reload data.yml: " + e.getMessage(), LogPrefixExtension.ADMINPANEL_MAIN, true);
            return false;
        }
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

    public TrollRegistry getTrollRegistry() {
        return trollRegistry;
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
