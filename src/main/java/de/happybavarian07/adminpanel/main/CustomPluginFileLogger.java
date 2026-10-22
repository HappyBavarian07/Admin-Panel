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

    /**
     * Writes a message unless its kind is switched off in {@code Plugin.LogActions}. Warnings and errors are always written, so switching
     * a kind off never hides a problem.
     */
    public PluginFileLogger writeToLog(Level record, String stringToLog, LogPrefixExtension logPrefix, boolean sendToConsole) {
        if (record.intValue() < Level.WARNING.intValue() && !logPrefix.isEnabled()) return this;
        return super.writeToLog(record, stringToLog, logPrefix.getLogPrefix(), sendToConsole);
    }

    public PluginFileLogger writeToLog(Level record, String stringToLog, LogPrefixExtension logPrefix) {
        return writeToLog(record, stringToLog, logPrefix, false);
    }
}
