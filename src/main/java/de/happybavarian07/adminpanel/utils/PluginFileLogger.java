package de.happybavarian07.adminpanel.utils;/*
 * @Author HappyBavarian07
 * @Date 02.10.2021 | 13:15
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

public class PluginFileLogger {
    private static AdminPanelMain plugin;
    private static File logFile;
    private static PluginFileLogger instance;

    public PluginFileLogger() {
        instance = this;
        plugin = AdminPanelMain.getPlugin();
        logFile = new File(plugin.getDataFolder(), "plugin.log");
    }

    @Deprecated
    public PluginFileLogger writeToLog(Level record, String stringToLog, LogPrefix logPrefix) {
        if (!plugin.getConfig().getBoolean("Plugin.LogActions.enabled") || !logPrefix.isEnabled()) return instance;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            String prefix = "[" + dtf.format(now) + " " + record + "]: [" + logPrefix.getLogPrefix() + "] ";
            bw.write(prefix + stringToLog);
            bw.newLine();
            bw.close();
            return instance;
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
            return instance;
        }
    }

    @Deprecated
    public PluginFileLogger writeToLog(Level record, String stringToLog, String logPrefix) {
        if (!plugin.getConfig().getBoolean("Plugin.LogActions.enabled")) return instance;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            String prefix = "[" + dtf.format(now) + " " + record + "]: [" + logPrefix + "] ";
            bw.write(prefix + stringToLog);
            bw.newLine();
            bw.close();
            return instance;
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
            return instance;
        }
    }

    public PluginFileLogger writeToLog(Level record, String stringToLog, LogPrefix logPrefix, boolean sendToConsole) {
        if (!plugin.getConfig().getBoolean("Plugin.LogActions.enabled", true) || !logPrefix.isEnabled()) return instance;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
            Date d = Calendar.getInstance().getTime();
            String prefix = "[" + d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds() + " " + record + "]: [" + logPrefix.getLogPrefix() + "] ";
            bw.write(prefix + stringToLog);
            bw.newLine();
            bw.close();
            if (sendToConsole)
                plugin.getLogger().log(record, "[" + logPrefix + "] " + stringToLog);
            return instance;
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
            if (sendToConsole)
                plugin.getLogger().log(record, "[" + logPrefix + "] " + stringToLog);
            return instance;
        }
    }

    public PluginFileLogger writeToLog(Level record, String stringToLog, String logPrefix, boolean sendToConsole) {
        if (!plugin.getConfig().getBoolean("Plugin.LogActions.enabled", true)) return instance;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
            Date d = Calendar.getInstance().getTime();
            String prefix = "[" + d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds() + " " + record + "]: [" + logPrefix + "] ";
            bw.write(prefix + stringToLog);
            bw.newLine();
            bw.close();
            if (sendToConsole)
                plugin.getLogger().log(record, "[" + logPrefix + "] " + stringToLog);
            return instance;
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
            if (sendToConsole)
                plugin.getLogger().log(record, "[" + logPrefix + "] " + stringToLog);
            return instance;
        }
    }

    public File getLogFile() {
        return logFile;
    }

    public void createLogFile() {
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
