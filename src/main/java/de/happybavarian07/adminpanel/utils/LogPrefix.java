package de.happybavarian07.adminpanel.utils;/*
 * @Author HappyBavarian07
 * @Date 28.07.2023 | 11:00
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;

public enum LogPrefix {
    ACTIONSLOGGER_PLAYER("ActionsLogger - Player", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.ACTIONSLOGGER_PLAYER", true)),
    ACTIONSLOGGER_SERVER("ActionsLogger - Server", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.ACTIONSLOGGER_SERVER", true)),
    ACTIONSLOGGER_PANEL("ActionsLogger - Panel", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.ACTIONSLOGGER_PANEL", true)),
    ACTIONSLOGGER_PLUGIN("ActionsLogger - Plugin", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.ACTIONSLOGGER_PLUGIN", true)),
    ADDONLOADER("Addon-Loader", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.ADDONLOADER", true)),
    ADMINPANEL("AdminPanel", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.ADMINPANEL", true)),
    ADMINPANEL_MAIN("AdminPanel - Main", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.ADMINPANEL_MAIN", true)),
    ADMINPANEL_COMMANDS("AdminPanel - Commands", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.ADMINPANEL_COMMANDS", true)),
    ADMINPANEL_GUI("AdminPanel - GUI", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.ADMINPANEL_GUI", true)),
    ADMINPANEL_LISTENER("AdminPanel - Listener", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.ADMINPANEL_LISTENER", true)),
    ADMINPANEL_UTILS("AdminPanel - Utils", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.ADMINPANEL_UTILS", true)),
    DATELOGGER("DateLogger", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.DATELOGGER", true)),
    API("API", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.API", true)),
    COMMANDS("Commands", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.COMMANDS", true)),
    CONFIG("Config", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.CONFIG", true)),
    DATABASE("Database", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.DATABASE", true)),
    DEBUG("Debug", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.DEBUG", true)),
    ERROR("Error", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.ERROR", true)),
    FILE("File", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.FILE", true)),
    INFO("Info", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.INFO", true)),
    INITIALIZER("Initializer", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.INITIALIZER", true)),
    VAULT_MONEY("Vault - Money", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.VAULT_MONEY", true)),
    VAULT_PERMISSION("Vault - Permission", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.VAULT_PERMISSION", true)),
    VAULT_CHAT("Vault - Chat", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.VAULT_CHAT", true)),
    VAULT_ECONOMY("Vault - Economy", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.VAULT_ECONOMY", true)),
    VAULT_PLUGIN("Vault - Plugin", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.VAULT_PLUGIN", true)),
    UPDATER("Updater", AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.LogActions.IndividualActions.UPDATER", true));

    private final String logPrefix;
    private final boolean enabled;

    LogPrefix(String logPrefix, boolean enabled) {
        this.logPrefix = logPrefix;
        this.enabled = enabled;
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
