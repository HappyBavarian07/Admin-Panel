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
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    public Economy eco = null;
    public Permission perms = null;
    public Chat chat = null;
    public boolean inMaintenanceMode = false;
    public boolean chatMuted = false;
    private Updater updater;
    private LanguageManager languageManager;

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
            logger.coloredSpacer(ChatColor.RED);
            logger.message("&4&lCould not find PlaceholderAPI!!&r");
            logger.message("&4&lPlugin can not work without it!&r");
            logger.coloredSpacer(ChatColor.RED);
            getServer().getPluginManager().disablePlugin(this);
        }
        if (Bukkit.getPluginManager().getPlugin("SuperVanish") == null) {
            logger.coloredSpacer(ChatColor.RED);
            logger.message("&4&lCould not find SuperVanish!!&r");
            logger.message("&4&lPlugin can not work without it!&r");
            logger.coloredSpacer(ChatColor.RED);
            getServer().getPluginManager().disablePlugin(this);
        }
        logger
                .coloredSpacer(ChatColor.DARK_RED)
                .messages(
                        "&c&lStarting Vault initialization!&r"
                );
        if (!setupEconomy()) {
            logger
                    .spacer()
                    .coloredMessage(ChatColor.RED, "")
                    .coloredMessage(ChatColor.RED, "No Vault found please install Vault before starting again!")
                    .coloredMessage(ChatColor.RED, "and you must have an Economy Plugin installed!")
                    .coloredMessage(ChatColor.RED, "");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermission();
        setupChat();
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
        logger.message("&3&lMain.Prefix &9= &7Config.Plugin.Prefix&r");
        setPrefix(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getConfig().getString("Plugin.Prefix"))));
        logger.message("&e&lPrefix Done!&r");
        logger.coloredSpacer(ChatColor.DARK_RED).message("&2&lStarting Registration of Events:&r");
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
        languageManager.addLanguagesToList();
        languageManager.addLang(deLang, deLang.getLangName());
        languageManager.addLang(enLang, enLang.getLangName());
        languageManager.setCurrentLang(languageManager.getLang(getConfig().getString("Plugin.language")));
        if (languageManager != null && languageManager.getMessage("Plugin.EnablingMessage", null) != null &&
                !languageManager.getMessage("Plugin.EnablingMessage", null).equals("null config") &&
                !languageManager.getMessage("Plugin.EnablingMessage", null).startsWith("null path: Messages.")) {
            getServer().getConsoleSender().sendMessage(languageManager.getMessage("Plugin.EnablingMessage", null));
        } else {
            getServer().getConsoleSender().sendMessage("[Admin-Panel] enabled!");
        }
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
    }

    private void setupPermission() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null)
            perms = rsp.getProvider();
    }

    private void setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp != null)
            chat = rsp.getProvider();
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economy = getServer().getServicesManager().getRegistration(Economy.class);
        if (economy != null)
            eco = economy.getProvider();
        return eco != null;
    }
}
