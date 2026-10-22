package de.happybavarian07.adminpanel.utils;/*
 * @Author HappyBavarian07
 * @Date 28.07.2023 | 11:00
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;

public enum LogPrefixExtension {
    ACTIONSLOGGER_PLAYER("ActionsLogger - Player"),
    ACTIONSLOGGER_SERVER("ActionsLogger - Server"),
    ACTIONSLOGGER_PANEL("ActionsLogger - Panel"),
    ACTIONSLOGGER_PLUGIN("ActionsLogger - Plugin"),
    ADDONLOADER("Addon-Loader"),
    ADMINPANEL("AdminPanel"),
    ADMINPANEL_MAIN("AdminPanel - Main"),
    ADMINPANEL_COMMANDS("AdminPanel - Commands"),
    ADMINPANEL_GUI("AdminPanel - GUI"),
    ADMINPANEL_LISTENER("AdminPanel - Listener"),
    ADMINPANEL_UTILS("AdminPanel - Utils"),
    DATELOGGER("DateLogger"),
    API("API"),
    COMMANDS("Commands"),
    CONFIG("Config"),
    DATABASE("Database"),
    DEBUG("Debug"),
    ERROR("Error"),
    FILE("File"),
    INFO("Info"),
    INITIALIZER("Initializer"),
    VAULT_MONEY("Vault - Money"),
    VAULT_PERMISSION("Vault - Permission"),
    VAULT_CHAT("Vault - Chat"),
    VAULT_ECONOMY("Vault - Economy"),
    VAULT_PLUGIN("Vault - Plugin"),
    UPDATER("Updater"),
    WEBUI("WebUI"),
    WEBUI_API("WebUI - API"),
    WEBUI_CONSOLE("WebUI - Commands"),
    WEBUI_LOGIN("WebUI - Login"),
    WEBUI_AUTH("WebUI - Auth"),
    PERMISSIONS("Permissions"),
    JOURNAL("Journal");

    private final String logPrefix;

    LogPrefixExtension(String logPrefix) {
        this.logPrefix = logPrefix;
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    /**
     * Whether messages of this kind are written to the log file. Read from the config each time, so {@code /adminpaneladmin reloadconfig}
     * applies it: {@code Plugin.LogActions.enabled} switches all kinds off, {@code Plugin.LogActions.IndividualActions.<NAME>} one kind.
     */
    public boolean isEnabled() {
        AdminPanelMain plugin = AdminPanelMain.getPlugin();
        if (plugin == null) return true;
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
        return config.getBoolean("Plugin.LogActions.enabled", true)
                && config.getBoolean("Plugin.LogActions.IndividualActions." + name(), true);
    }
}
