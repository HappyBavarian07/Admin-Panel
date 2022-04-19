package de.happybavarian07.main;

import com.tchristofferson.configupdater.ConfigUpdater;
import de.happybavarian07.addonloader.api.Addon;
import de.happybavarian07.addonloader.api.Dependency;
import de.happybavarian07.addonloader.loadingutils.AddonLoader;
import de.happybavarian07.addonloader.utils.FileUtils;
import de.happybavarian07.commands.AdminPanelOpenCommand;
import de.happybavarian07.commands.UpdateCommand;
import de.happybavarian07.listeners.MenuListener;
import de.happybavarian07.placeholders.PanelExpansion;
import de.happybavarian07.placeholders.PlayerExpansion;
import de.happybavarian07.placeholders.PluginExpansion;
import de.happybavarian07.utils.*;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

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
    public Economy eco = null;
    public net.milkbowl.vault.permission.Permission perms = null;
    public Chat chat = null;
    public boolean inMaintenanceMode = false;
    public boolean chatMuted = false;
    private FileConfiguration permissionsConfig;
    private NewUpdater updater;
    private LanguageManager languageManager;
    private OldLanguageFileUpdater langFileUpdater;
    private List<Addon> loadedAddons = new ArrayList<>();
    private AddonLoader loader;
    private FileConfiguration dataYML;
    private Map<String, NewUpdater> autoUpdaterPlugins = new HashMap<>();

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

    public boolean isInMaintenanceMode() {
        return inMaintenanceMode;
    }

    public Map<String, NewUpdater> getAutoUpdaterPlugins() {
        return autoUpdaterPlugins;
    }

    public void setInMaintenanceMode(boolean inMaintenanceMode) {
        this.inMaintenanceMode = inMaintenanceMode;
    }

    public boolean isChatMuted() {
        return chatMuted;
    }

    public void setChatMuted(boolean chatMuted) {
        this.chatMuted = chatMuted;
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
        languageManager = new LanguageManager(this, new File(this.getDataFolder() + "/languages"));
        langFileUpdater = new OldLanguageFileUpdater(this);
        API = new LocalAdminPanelAPI(this);
        new ChatUtil();
        new Utils();
        new File(this.getDataFolder() + "/languages").mkdir();
        logger.message("&e&lVariable Done!&r");
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlayerExpansion().register();
            new PluginExpansion().register();
            new PanelExpansion().register();
            logger.message("&a&lInitialized PlaceHolderAPI with Placeholders!&r");
        } else {
            logger
                    .spacer()
                    .coloredMessage(ChatColor.RED, "")
                    .coloredMessage(ChatColor.RED, "No PlaceholderAPI found please install PlaceholderAPI before starting again!")
                    .coloredMessage(ChatColor.RED, "The Plugin cannot work without this Plugin!")
                    .coloredMessage(ChatColor.RED, "");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("SuperVanish") == null) {
            logger
                    .spacer()
                    .coloredMessage(ChatColor.RED, "")
                    .coloredMessage(ChatColor.RED, "No SuperVanish found please install SuperVanish,")
                    .coloredMessage(ChatColor.RED, "if you want to use the Vanish Feature!")
                    .coloredMessage(ChatColor.RED, "");
        }
        logger
                .coloredSpacer(ChatColor.DARK_RED)
                .messages(
                        "&c&lStarting Vault initialization!&r"
                );
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            logger
                    .spacer()
                    .coloredMessage(ChatColor.RED, "")
                    .coloredMessage(ChatColor.RED, "No Vault found please install Vault and an Economy Plugin,")
                    .coloredMessage(ChatColor.RED, "if you want to use the Money Features!")
                    .coloredMessage(ChatColor.RED, "");
        } else {
            setupEconomy();
        }
        logger
                .messages(
                        "&c&lFinished Vault initialization!&r"
                );
        if (!configFile.exists()) {
            logger.coloredSpacer(ChatColor.DARK_RED).message("&c&lCreating Default Config!&r");
        }
        saveDefaultConfig();
        if (!configFile.exists()) {
            logger.message("&e&lDone!&r");
        }
        fileLogger = new PluginFileLogger();
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
        permissionsConfig = YamlConfiguration.loadConfiguration(permissionFile);
        if (!permissionsConfig.isConfigurationSection("Permissions")) {
            permissionsConfig.createSection("Permissions");
            try {
                permissionsConfig.save(permissionFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        permissionsConfig.getConfigurationSection("Permissions").getKeys(false).forEach(player -> {
            String path = "Permissions." + player + ".Permissions";
            Map<String, Boolean> perms = new HashMap<>();
            permissionsConfig.getConfigurationSection(path).getKeys(false).forEach(perm -> {
                perms.put(perm.replace("(<->)", "."), permissionsConfig.getBoolean(path + "." + perm));
            });
            playerPermissions.put(UUID.fromString(player), perms);
        });
        logger.message("&3&lMain.Prefix &9= &7Config.Plugin.Prefix&r");
        setPrefix(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getConfig().getString("Plugin.Prefix"))));
        logger.message("&e&lPrefix Done!&r");
        logger.coloredSpacer(ChatColor.DARK_RED).message("&2&lStarting Region of Evnts:&r");
        PluginManager pm = this.getServer().getPluginManager();
        logger.message("&3&lLoading Menu Listener Events!&r");
        pm.registerEvents(new MenuListener(), this);
        logger.message("&3&lLoading Main Class Listener Events!&r");
        pm.registerEvents(this, this);
        logger.message("&4&lEventregistration: Done!&r");
        logger.coloredSpacer(ChatColor.DARK_RED).message("&e&lStarting Done!&r");
        logger.coloredSpacer(ChatColor.GREEN);
        // Language Manager Enabling
        LanguageFile deLang = new LanguageFile(this, "de");
        LanguageFile enLang = new LanguageFile(this, "en");
        languageManager.addLanguagesToList(true);
        languageManager.addLang(deLang, deLang.getLangName());
        languageManager.addLang(enLang, enLang.getLangName());
        languageManager.setCurrentLang(languageManager.getLang(getConfig().getString("Plugin.language")), true);
        if (languageManager != null && languageManager.getMessage("Plugin.EnablingMessage", null, false) != null &&
                !languageManager.getMessage("Plugin.EnablingMessage", null, false).equals("null config") &&
                !languageManager.getMessage("Plugin.EnablingMessage", null, false).startsWith("null path: Messages.")) {
            getServer().getConsoleSender().sendMessage(languageManager.getMessage("Plugin.EnablingMessage", null, true));
        } else {
            getServer().getConsoleSender().sendMessage("[Admin-Panel] enabled!");
        }
        try {
            ConfigUpdater.update(this, "config.yml", new File(this.getDataFolder() + "/config.yml"), new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (getConfig().getBoolean("Plugin.Updater.AutomaticLanguageFileUpdating")) {
            languageManager.reloadLanguages(null, false);
        }

        metrics.addCustomChart(new Metrics.SimplePie("used_language", () -> languageManager.getCurrentLang().getLangName()));
        metrics.addCustomChart(new Metrics.SimplePie("language_count", () -> {
            int value = 0;
            for (LanguageFile lang : getLanguageManager().getRegisteredLanguages().values()) {
                if (lang.getPlugin() == this)
                    value++;
            }
            return String.valueOf(value);
        }));
        metrics.addCustomChart(new Metrics.SimplePie("external_api_language_count", () -> {
            int value = 0;
            for (LanguageFile lang : getLanguageManager().getRegisteredLanguages().values()) {
                if (lang.getPlugin() != this)
                    value++;
            }
            return String.valueOf(value);
        }));

        updater = new NewUpdater(getPlugin(), 91800, "Admin-Panel-%version%.jar", getPlugin());
        updater.setVersionComparator(VersionComparator.SEMATIC_VERSION);
        if (getConfig().getBoolean("Plugin.Updater.checkForUpdates")) {
            updater.checkForUpdates(true);
            if (updater.updateAvailable()) {
                updater.downloadLatestUpdate(getConfig().getBoolean("Plugin.Updater.automaticReplace"), getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate"), true);
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    updater.checkForUpdates(false);
                    if (updater.updateAvailable()) {
                        updater.downloadLatestUpdate(getConfig().getBoolean("Plugin.Updater.automaticReplace"), getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate"), true);
                    }
                }
            }.runTaskTimer(plugin, (getConfig().getLong("Plugin.Updater.UpdateCheckTime") * 60 * 20), (getConfig().getLong("Plugin.Updater.UpdateCheckTime") * 60 * 20));
        }
        if (!new File(getDataFolder() + "/data.yml").exists()) {
            saveResource("data.yml", false);
        }
        dataYML = YamlConfiguration.loadConfiguration(new File(getDataFolder() + "/data.yml"));
        if (getConfig().getBoolean("Plugin.Updater.PluginUpdater.enabled")) {
            autoUpdaterPlugins.clear();
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

                assert fileName != null;
                if (!fileName.endsWith(".jar")) continue;

                NewUpdater tempUpdater = new NewUpdater(getPlugin(), spigotID, fileName, (JavaPlugin) new PluginUtils().getPluginByName(sectionString));
                autoUpdaterPlugins.put(sectionString, tempUpdater);
                if (!tempUpdater.resourceIsOnSpigot()) continue;
                if(tempUpdater.isExternalFile()) {
                    logger.message("Plugin: " + tempUpdater.getPluginName() + " is external and the Plugin will not download it!");
                    tempUpdater.checkForUpdates(true);
                    continue;
                }
                if ((tempUpdater.getPluginName() == null) && getConfig().getBoolean("Plugin.Updater.PluginUpdater.downloadIfNotExists")) {
                    tempUpdater.downloadLatestUpdate(true, true, false);
                    continue;
                }

                tempUpdater.setVersionComparator(VersionComparator.SEMATIC_VERSION);
                tempUpdater.checkForUpdates(true);
                if (tempUpdater.updateAvailable()) {
                    tempUpdater.downloadLatestUpdate(getConfig().getBoolean("Plugin.Updater.PluginUpdater.automaticReplace"),
                            getConfig().getBoolean("Plugin.Updater.PluginUpdater.downloadPluginUpdate"), true);
                }
            }
            if (getConfig().getBoolean("Plugin.Updater.PluginUpdater.checkForUpdatesFrequently")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        autoUpdaterPlugins.clear();
                        for (String sectionString : dataYML.getConfigurationSection("PluginsToUpdate").getKeys(false)) {
                            if (!dataYML.isConfigurationSection("PluginsToUpdate." + sectionString)) continue;

                            ConfigurationSection section = dataYML.getConfigurationSection("PluginsToUpdate." + sectionString);
                            assert section != null;
                            if (section.getInt("spigotID", -1) == -1 || section.getString("fileName", "").equals(""))
                                continue;

                            int spigotID = section.getInt("spigotID");
                            String fileName = section.getString("fileName");

                            assert fileName != null;
                            if (!fileName.endsWith(".jar")) continue;

                            NewUpdater tempUpdater = new NewUpdater(getPlugin(), spigotID, fileName, (JavaPlugin) new PluginUtils().getPluginByName(sectionString));
                            autoUpdaterPlugins.put(sectionString, tempUpdater);
                            if (!tempUpdater.resourceIsOnSpigot()) continue;
                            if(tempUpdater.isExternalFile()) {
                                logger.message("Plugin: " + tempUpdater.getPluginName() + " is external and the Plugin will not download it!");
                                tempUpdater.checkForUpdates(true);
                                continue;
                            }
                            if ((tempUpdater.getPluginName() == null) && getConfig().getBoolean("Plugin.Updater.PluginUpdater.downloadIfNotExists")) {
                                tempUpdater.downloadLatestUpdate(true, true, false);
                                continue;
                            }

                            tempUpdater.setVersionComparator(VersionComparator.SEMATIC_VERSION);
                            tempUpdater.checkForUpdates(true);
                            if (tempUpdater.updateAvailable()) {
                                tempUpdater.downloadLatestUpdate(getConfig().getBoolean("Plugin.Updater.PluginUpdater.automaticReplace"),
                                        getConfig().getBoolean("Plugin.Updater.PluginUpdater.downloadPluginUpdate"), true);
                            }
                        }
                    }
                }.runTaskTimer(plugin, (getConfig().getLong("Plugin.Updater.PluginUpdater.UpdateCheckTime") * 60 * 20), (getConfig().getLong("Plugin.Updater.PluginUpdater.UpdateCheckTime") * 60 * 20));
            }
            logger.coloredSpacer(ChatColor.BLUE);
        }

        /*new BukkitRunnable() {
            @Override
            public void run() {
                languageManager.resetPlaceholders();
            }
        }.runTaskTimer(plugin, 0, 180);*/

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
        try {
            Objects.requireNonNull(this.getCommand("update")).setExecutor(new UpdateCommand());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(this.getCommand("adminpanel")).setExecutor(new AdminPanelOpenCommand());

        //System.out.println("Init Addon Loader!!!!!!!!!!!!!!!!!!!!!!!!! 111111");
        //System.out.println("Config Option: " + plugin.getConfig().getBoolean("Plugin.AddonSystem.enabled"));
        if (plugin.getConfig().getBoolean("Plugin.AddonSystem.enabled")) {
            //System.out.println("Init Addon Loader!!!!!!!!!!!!!!!!!!!!!!!!! 222222");
            initAddonLoader();
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        getFileLogger().writeToLog(Level.INFO, "Admin-Panel successfully started on '" + dtf.format(now) + "'", "DateLogger");
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

        try {
            getDataYML().save(new File(getDataFolder() + "/data.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initAddonLoader() {
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
            getLogger().log(Level.INFO, "Loading Addon Jar File: " + jarFile.getName());
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
                    getLogger().log(Level.INFO, "Enabled Addon: " + addon.getName());
                    addon.onEnable();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        logger.coloredMessage(ChatColor.GREEN, "Done with Phase 3/3! (Enabling)");
        logger.coloredSpacer(ChatColor.BLUE);
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
        loader = null;
        if (languageManager != null && languageManager.getMessage("Plugin.DisablingMessage", null, false) != null &&
                !languageManager.getMessage("Plugin.DisablingMessage", null, false).equals("null config") &&
                !languageManager.getMessage("Plugin.DisablingMessage", null, false).startsWith("null path: Messages.")) {
            getServer().getConsoleSender().sendMessage(languageManager.getMessage("Plugin.DisablingMessage", null, true));
        } else {
            getServer().getConsoleSender().sendMessage("[Admin-Panel] disabled!");
        }
        savePerms();
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economy = getServer().getServicesManager().getRegistration(Economy.class);
        if (economy != null)
            eco = economy.getProvider();
        return eco != null;
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
                permissionsConfig.set(path + permissions.replace(".", "(<->)"), perms.get(permissions));
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
                    online.removeAttachment(playerPermissionsAttachments.get(player.getUniqueId()));
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
