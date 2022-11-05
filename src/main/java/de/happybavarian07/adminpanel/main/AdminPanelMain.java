package de.happybavarian07.adminpanel.main;

import de.happybavarian07.adminpanel.addonloader.api.Addon;
import de.happybavarian07.adminpanel.addonloader.loadingutils.AddonLoader;
import de.happybavarian07.adminpanel.bungee.BungeeTestCommand;
import de.happybavarian07.adminpanel.bungee.BungeeUtils;
import de.happybavarian07.adminpanel.bungee.DataClientUtils;
import de.happybavarian07.adminpanel.bungee.NewDataClient;
import de.happybavarian07.adminpanel.commandmanagement.CommandManagerRegistry;
import de.happybavarian07.adminpanel.configupdater.OldConfigUpdater;
import de.happybavarian07.adminpanel.utils.*;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
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
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
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
    public final Map<Player, Boolean> hurtingwater = new HashMap<>();
    public final Map<Player, Boolean> chatmute = new HashMap<>();
    public final Map<Player, Boolean> villagerSounds = new HashMap<>();
    public final Map<Player, Boolean> blockBreakPrevent = new HashMap<>();
    public final Map<Player, Boolean> dupeMobsOnKill = new HashMap<>();
    final StartUpLogger logger = StartUpLogger.create();
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
    private NewDataClient dataClient;
    private boolean languageManagerEnabled;

    public FileConfiguration getPermissionsConfig() {
        return permissionsConfig;
    }

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

    public boolean isLanguageManagerEnabled() {
        return languageManagerEnabled;
    }

    private void setLanguageManagerEnabled(boolean languageManagerEnabled) {
        this.languageManagerEnabled = languageManagerEnabled;
    }

    public NewDataClient getDataClient() {
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

    public boolean isInMaintenanceMode() {
        return inMaintenanceMode;
    }

    public void setInMaintenanceMode(boolean inMaintenanceMode) {
        this.inMaintenanceMode = inMaintenanceMode;
    }

    public Map<String, NewUpdater> getAutoUpdaterPlugins() {
        return autoUpdaterPlugins;
    }

    public boolean isChatMuted() {
        return chatMuted;
    }

    public void setChatMuted(boolean chatMuted) {
        this.chatMuted = chatMuted;
    }

    public List<Addon> getLoadedAddons() {
        return loadedAddons;
    }

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

    public boolean isAddonSystemEnabled() {
        return getConfig().getBoolean("Plugin.AddonSystem.enabled");
    }

    public boolean isUpdaterEnabled() {
        return getConfig().getBoolean("Plugin.Updater.checkForUpdates");
    }

    public boolean isPluginUpdaterEnabled() {
        return getConfig().getBoolean("Plugin.Updater.PluginUpdater.enabled");
    }

    public boolean isUpdateReplacerEnabled() {
        return getConfig().getBoolean("Plugin.Updater.automaticReplace");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
    }

    public void updateConfig() {
        try {
            OldConfigUpdater.update(this, "config.yml", new File(this.getDataFolder() + "/config.yml"), new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig();
    }

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

    @Override
    public void onEnable() {
        // bStats
        int bStatsID = 11778;
        Metrics metrics = new Metrics(this, bStatsID);

        logger
                .coloredSpacer(ChatColor.GREEN)
                .messages(
                        "&e&lStarting Admin Panel Plugin:&r"
                );
        logger.coloredSpacer(ChatColor.DARK_RED).message("&4&lInitialize Plugin Main Variable to this!&r");
        setPlugin(this);


        languageManager = new LanguageManager(this, new File(this.getDataFolder() + "/languages"), "[Admin-Panel]");
        commandManagerRegistry = new CommandManagerRegistry(this);
        langFileUpdater = new OldLanguageFileUpdater();
        API = new LocalAdminPanelAPI(this);
        InitMethods initMethods = new InitMethods(logger, Bukkit.getPluginManager(), plugin);
        new ChatUtil();
        new Utils();
        new File(this.getDataFolder() + "/languages").mkdir();
        logger.message("&e&lVariable Done!&r");
        logger.message("&e&lStarting Bungee Registration");
        if (checkIfBungee()) {
            String path = "Plugin.BungeeSyncSystem.ChannelNames.";
            bungeeUtils = new BungeeUtils(getConfig().getString(path + "In", ""), getConfig().getString(path + "Out", ""));
            bungeeUtils.openBungeeChannel();
            this.getCommand("bungeetest").setExecutor(new BungeeTestCommand());
            path = "Plugin.BungeeSyncSystem.JavaSockets.";
            if (getConfig().getBoolean(path + "enabled")) {
                /*dataClient = new DataClient(getConfig().getString(path + "hostName"),
                        getConfig().getInt(path + "port"), getConfig().getString(path + "ClientName"));
                dataClient.connect();*/
                try {
                    dataClient = new NewDataClient(getConfig().getString(path + "hostName"),
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
        fileLogger = new PluginFileLogger();
        // Config Files
        initMethods.initConfigFiles(fileLogger, permissionFile);
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
        LanguageFile deLang = new LanguageFile(this, "de");
        LanguageFile enLang = new LanguageFile(this, "en");
        languageManager.addLanguagesToList(true);
        languageManager.addLang(deLang, deLang.getLangName());
        languageManager.addLang(enLang, enLang.getLangName());
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

        /*if (!languageManager.getPlhandler().getPlayerLanguages().isEmpty())
            System.out.println("Most Used Player Lang: " + initMethods.getMostUsedPlayerLang().getLangName());*/

        updateConfig();
        if (getConfig().getBoolean("Plugin.Updater.AutomaticLanguageFileUpdating")) {
            languageManager.reloadLanguages(null, false);
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

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        getFileLogger().writeToLog(Level.INFO, "Admin-Panel successfully started on '" + dtf.format(now) + "'", "DateLogger");
    }

    public String getVersion() {
        if (getDescription().getVersion().equals("")) return "N/A";
        return getDescription().getVersion();
    }

    public FileConfiguration getDataYML() {
        return dataYML;
    }

    public void removePluginFromUpdater(Plugin selectedPlugin) {
        getDataYML().set("PluginsToUpdate." + selectedPlugin.getName(), null);

        try {
            getDataYML().save(new File(getDataFolder() + "/data.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    }

    public PluginFileLogger getFileLogger() {
        return fileLogger;
    }

    public @NotNull
    File getPluginFile() {
        return this.getFile();
    }

    public StartUpLogger getStartUpLogger() {
        return logger;
    }

    @Override
    public void onDisable() {
        if (loader != null) {
            loader.crashAddons();
        }
        loader = null;
        if (isLanguageManagerEnabled()) {
            getServer().getConsoleSender().sendMessage(languageManager.getMessage("Plugin.DisablingMessage", null, true));
        } else {
            getServer().getConsoleSender().sendMessage("[Admin-Panel] disabled!");
        }
        savePerms();
        if (checkIfBungee()) {
            bungeeUtils.closeBungeeChannel();
            String path = "Plugin.BungeeSyncSystem.JavaSockets.";
            if (getConfig().getBoolean(path + "enabled")) {
                dataClient.disconnect(true, true);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
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
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playerPermissionsAttachments.containsKey(player.getUniqueId())) {
            player.removeAttachment(playerPermissionsAttachments.get(player.getUniqueId()));
        }
    }

    public void savePerms() {
        for (UUID uuid : playerPermissions.keySet()) {
            String path = "Permissions." + uuid + ".Permissions.";
            Map<String, Boolean> perms = playerPermissions.get(uuid);
            permissionsConfig.set("Permissions." + uuid + ".Permissions", null);
            for (String permissions : perms.keySet()) {
                permissionsConfig.set(path + permissions/*.replace(".", "(<->)")*/, perms.get(permissions));
            }
        }
        try {
            permissionsConfig.save(permissionFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
}
