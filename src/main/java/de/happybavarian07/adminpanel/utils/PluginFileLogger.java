package de.happybavarian07.adminpanel.utils;/*
 * @Author HappyBavarian07
 * @Date 02.10.2021 | 13:15
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    public PluginFileLogger writeToLog(Level record, String stringToLog, String logPrefix) {
        if (!plugin.getConfig().getBoolean("Plugin.LogActions")) return instance;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
            Date d = Calendar.getInstance().getTime();
            String prefix = "[" + d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds() + " " + record + "]: [" + logPrefix + "] ";
            bw.write(prefix + stringToLog);
            bw.newLine();
            bw.close();
            return instance;
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
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
