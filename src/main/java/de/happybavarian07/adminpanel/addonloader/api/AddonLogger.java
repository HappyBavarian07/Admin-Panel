package de.happybavarian07.adminpanel.addonloader.api;

import org.bukkit.Bukkit;


import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/*
 * @Author HappyBavarian07
 * @Date September 08, 2024 | 14:41
 */
public class AddonLogger extends Logger {
    private final String pluginName;

    /**
     * Creates a new AddonLogger that extracts the name from a Addon.
     *
     * @param context A reference to the plugin
     */
    public AddonLogger(Addon context) {
        super(context.getClass().getCanonicalName(), null);
        String prefix = context.getPrefix();
        pluginName = prefix != null ? "[" + prefix + "] " : "[" + context.getName() + "] ";
        setParent(Bukkit.getServer().getLogger());
        setLevel(Level.ALL);
    }

    @Override
    public void log(LogRecord logRecord) {
        logRecord.setMessage(pluginName + logRecord.getMessage());
        super.log(logRecord);
    }

}
