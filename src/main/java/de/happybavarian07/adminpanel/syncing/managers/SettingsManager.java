package de.happybavarian07.adminpanel.syncing.managers;/*
 * @Author HappyBavarian07
 * @Date 17.09.2023 | 19:14
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class SettingsManager {
    private boolean debugEnabled;
    private boolean overwritePermissionsEnabled;
    private boolean checkConnection;
    private long checkConnectionTiming;
    private boolean fileLogging;
    private String fileLoggingPrefix;

    private File settingsFile;
    private FileConfiguration settingsConfig;

    public SettingsManager(File settingsFile) {
        this.settingsFile = settingsFile;
        loadSettings();
    }

    public boolean isCheckConnection() {
        return checkConnection;
    }

    public long getCheckConnectionTiming() {
        return checkConnectionTiming;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public boolean isOverwritePermissionsEnabled() {
        return overwritePermissionsEnabled;
    }

    public boolean isFileLogging() {
        return fileLogging;
    }

    public String getFileLoggingPrefix() {
        return fileLoggingPrefix;
    }

    public void setCheckConnection(boolean checkConnection) {
        this.checkConnection = checkConnection;
        settingsConfig.set("Settings.General.CheckConnection", checkConnection);
        saveConfig();
    }

    public void setCheckConnectionTiming(long checkConnectionTiming) {
        this.checkConnectionTiming = checkConnectionTiming;
        settingsConfig.set("Settings.General.CheckConnectionTiming", checkConnectionTiming);
        saveConfig();
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
        settingsConfig.set("Settings.General.Debug", debugEnabled);
        saveConfig();
    }

    public void setFileLoggingPrefix(String fileLoggingPrefix) {
        this.fileLoggingPrefix = fileLoggingPrefix;
        settingsConfig.set("Settings.General.FileLoggerPrefix", fileLoggingPrefix);
        saveConfig();
    }

    public void setFileLogging(boolean fileLogging) {
        this.fileLogging = fileLogging;
        settingsConfig.set("Settings.General.FileLogging", fileLogging);
        saveConfig();
    }

    public void setOverwritePermissionsEnabled(boolean overwritePermissionsEnabled) {
        this.overwritePermissionsEnabled = overwritePermissionsEnabled;
        settingsConfig.set("Settings.Permissions.OverwritePerms", overwritePermissionsEnabled);
        saveConfig();
    }

    public FileConfiguration getSettingsConfig() {
        return settingsConfig;
    }

    public File getSettingsFile() {
        return settingsFile;
    }

    public void reloadConfig() {
        settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);

        InputStream defaultStream = AdminPanelMain.getPlugin().getResource("DataClientSettings.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            settingsConfig.setDefaults(defaultConfig);
        }
    }

    public void saveConfig() {
        if (settingsConfig == null || settingsFile == null)
            return;

        try {
            settingsConfig.save(settingsFile);
        } catch (IOException e) {
            AdminPanelMain.getPlugin().getLogger().log(Level.SEVERE, "Could not save Config to " + settingsFile, e);
        }
    }

    public void saveDefaultConfig() {
        if (settingsFile == null)
            settingsFile = new File(AdminPanelMain.getPlugin().getDataFolder(), "DataClientSettings.yml");

        if (!settingsFile.exists()) {
            AdminPanelMain.getPlugin().saveResource("DataClientSettings.yml", false);
        }
    }

    public void reloadValuesFromConfig() {
        reloadConfig();
        debugEnabled = settingsConfig.getBoolean("Settings.General.Debug", false);
        overwritePermissionsEnabled = settingsConfig.getBoolean("Settings.Permissions.OverwritePerms", false);
        checkConnection = settingsConfig.getBoolean("Settings.General.CheckConnection", false);
        checkConnectionTiming = settingsConfig.getLong("Settings.General.CheckConnectionTiming", 300);
        fileLogging = settingsConfig.getBoolean("Settings.General.FileLogging", false);
        fileLoggingPrefix = settingsConfig.getString("Settings.General.FileLoggerPrefix", "JSBDSS");
    }

    private void loadSettings() {
        settingsFile = new File(AdminPanelMain.getPlugin().getDataFolder(), "DataClientSettings.yml");
        reloadValuesFromConfig();
    }
}
