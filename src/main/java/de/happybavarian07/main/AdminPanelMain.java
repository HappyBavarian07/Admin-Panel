package de.happybavarian07.main;

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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

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
    private Updater updater;
    private LanguageManager languageManager;
    private OldLanguageFileUpdater langFileUpdater;

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

    public void setInMaintenanceMode(boolean inMaintenanceMode) {
        this.inMaintenanceMode = inMaintenanceMode;
    }

    public boolean isChatMuted() {
        return chatMuted;
    }

    public void setChatMuted(boolean chatMuted) {
        this.chatMuted = chatMuted;
    }

    public Updater getUpdater() {
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
        if (languageManager != null && languageManager.getMessage("Plugin.EnablingMessage", null) != null &&
                !languageManager.getMessage("Plugin.EnablingMessage", null).equals("null config") &&
                !languageManager.getMessage("Plugin.EnablingMessage", null).startsWith("null path: Messages.")) {
            getServer().getConsoleSender().sendMessage(languageManager.getMessage("Plugin.EnablingMessage", null));
        } else {
            getServer().getConsoleSender().sendMessage("[Admin-Panel] enabled!");
        }
        if (getConfig().getBoolean("Plugin.Updater.AutomaticLanguageFileUpdating")) {
            for (LanguageFile langFiles : languageManager.getRegisteredLanguages().values()) {
                File oldFile = langFiles.getLangFile();
                File newFile = new File(langFiles.getLangFile().getParentFile().getPath() + "/" + langFiles.getLangName() + "-new.yml");
                YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(newFile);
                InputStream defaultStream = plugin.getResource("languages/" + langFiles.getLangName() + ".yml");
                if (defaultStream != null) {
                    YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
                    newConfig.setDefaults(defaultConfig);
                }
                newConfig.options().copyDefaults(true);
                try {
                    newConfig.save(newFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                langFileUpdater.updateFile(oldFile, newConfig, langFiles.getLangName());
                newFile.delete();
                languageManager.reloadLanguages(null, false);
            }
            /*for (LanguageFile langFiles : languageManager.getRegisteredLanguages().values()) {
                File file = langFiles.getLangFile();
                String resource = "languages/" + langFiles.getLangFile().getName();
                try {
                    List<String> ignoredSections = new ArrayList<>();
                    for (int i = 1; i < 54; i++) {
                        ignoredSections.add("slot: " + i);
                    }
                    ignoredSections.add("enchanted: true");
                    ignoredSections.add("enchanted: false");
                    ConfigUpdater.update(plugin, resource, file, ignoredSections);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
        }
        languageManager.reloadLanguages(null, false);
        updater = new Updater(getPlugin(), 91800);
        if (getConfig().getBoolean("Plugin.Updater.checkForUpdates")) {
            updater.checkForUpdates(true);
            if (updater.updateAvailable()) {
                updater.downloadPlugin(getConfig().getBoolean("Plugin.Updater.automaticReplace"), false, true);
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    updater.checkForUpdates(false);
                    if (updater.updateAvailable()) {
                        updater.downloadPlugin(getConfig().getBoolean("Plugin.Updater.automaticReplace"), false, true);
                    }
                }
            }.runTaskTimer(plugin, (getConfig().getLong("Plugin.Updater.UpdateCheckTime") * 60 * 20), (getConfig().getLong("Plugin.Updater.UpdateCheckTime") * 60 * 20));
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
        Objects.requireNonNull(this.getCommand("update")).setExecutor(new UpdateCommand());
        Objects.requireNonNull(this.getCommand("adminpanel")).setExecutor(new AdminPanelOpenCommand());
    }

    public PluginFileLogger getFileLogger() {
        return fileLogger;
    }

    public @NotNull File getPluginFile() {
        return this.getFile();
    }

    public StartUpLogger getStartUpLogger() {
        return logger;
    }

    @Override
    public void onDisable() {
        if (languageManager != null && languageManager.getMessage("Plugin.DisablingMessage", null) != null &&
                !languageManager.getMessage("Plugin.DisablingMessage", null).equals("null config") &&
                !languageManager.getMessage("Plugin.DisablingMessage", null).startsWith("null path: Messages.")) {
            getServer().getConsoleSender().sendMessage(languageManager.getMessage("Plugin.DisablingMessage", null));
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
