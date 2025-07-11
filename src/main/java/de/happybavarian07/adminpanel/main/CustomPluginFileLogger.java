package de.happybavarian07.adminpanel.main;

import de.happybavarian07.adminpanel.utils.LogPrefixExtension;
import de.happybavarian07.coolstufflib.utils.PluginFileLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

/*
 * @Author HappyBavarian07
 * @Date Juli 05, 2025 | 21:56
 */
public class CustomPluginFileLogger extends PluginFileLogger {
    public CustomPluginFileLogger(JavaPlugin javaPluginUsingThisLib) {
        super(javaPluginUsingThisLib);
    }

    public CustomPluginFileLogger(JavaPlugin javaPluginUsingThisLib, String logFileName) {
        super(javaPluginUsingThisLib, logFileName);
    }

    public CustomPluginFileLogger(File dataFolder, String logFileName) {
        super(dataFolder, logFileName);
    }

    public PluginFileLogger writeToLog(Level record, String stringToLog, LogPrefixExtension logPrefix, boolean sendToConsole) {
        return super.writeToLog(record, stringToLog, logPrefix.getLogPrefix(), sendToConsole);
    }

    public PluginFileLogger writeToLog(Level record, String stringToLog, LogPrefixExtension logPrefix) {
        return super.writeToLog(record, stringToLog, logPrefix.getLogPrefix(), false);
    }
}
