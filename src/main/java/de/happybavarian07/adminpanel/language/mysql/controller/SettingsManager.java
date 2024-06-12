package de.happybavarian07.adminpanel.language.mysql.controller;/*
 * @Author HappyBavarian07
 * @Date 08.05.2024 | 16:52
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SettingsManager {
    private final Map<String, Object> settingsMap = new HashMap<>();
    private final ConnectionManager connectionManager;

    public SettingsManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        createSettingsTable();
        loadSettings();
    }

    private void createSettingsTable() {
        try {
            // Unique Settings key, Settings value
            connectionManager.createTable("CREATE TABLE IF NOT EXISTS {table_prefix}Settings (SettingsKey VARCHAR(255) UNIQUE NOT NULL, SettingsValue TEXT)");
            // Default Settings
            connectionManager.prepareStatement(
                    "INSERT {ignore} INTO {table_prefix}Settings (SettingsKey, SettingsValue) VALUES " +
                            "('currentLanguageName', 'en'), " +
                            "('currentLanguageID', '<Filler>'), " +
                            "('defaultLanguage', 'en'), " +
                            "('defaultLanguageID', '<Filler>')," +
                            "('prefix', " + AdminPanelMain.getPrefix() + ")"
            ).executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSettings() {
        try {
            var resultSet = connectionManager.prepareStatement("SELECT * FROM {table_prefix}Settings").executeQuery();
            while (resultSet.next()) {
                String settingsKey = resultSet.getString("SettingsKey");
                String settingsValue = resultSet.getString("SettingsValue");
                settingsMap.put(settingsKey, settingsValue);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSetting(String settingsKey, String settingsValue) {
        try {
            connectionManager.prepareStatement("UPDATE {table_prefix}Settings SET SettingsValue = '" + settingsValue + "' WHERE SettingsKey = '" + settingsKey + "'").executeUpdate();
            loadSettings();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getSetting(String settingsKey) {
        if (!settingsMap.containsKey(settingsKey)) {
            try {
                var resultSet = connectionManager.prepareStatement("SELECT SettingsValue FROM {table_prefix}Settings WHERE SettingsKey = '" + settingsKey + "'").executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("SettingsValue");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return (String) settingsMap.get(settingsKey);
    }

    public Map<String, Object> getSettingsMap() {
        return settingsMap;
    }
}
