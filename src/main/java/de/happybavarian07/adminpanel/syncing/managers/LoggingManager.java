package de.happybavarian07.adminpanel.syncing.managers;/*
 * @Author HappyBavarian07
 * @Date 17.09.2023 | 19:46
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;

import java.util.logging.Level;

public class LoggingManager {
    private final SettingsManager settingsManager;

    public LoggingManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    public void logIntoFile(Level record, String stringToLog) {
        if (settingsManager.isFileLogging()) {
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(record, stringToLog, settingsManager.getFileLoggingPrefix());
        }
    }

    public void logIntoFile(Level record, String stringToLog, String prefix) {
        if (settingsManager.isFileLogging()) {
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(record, stringToLog, prefix);
        }
    }
}
