package de.happybavarian07.webui.config;/*
 * @Author HappyBavarian07
 * @Date 16.02.2024 | 13:15
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefix;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {
    private final File file;
    private final FileConfiguration config;
    private Configuration defaultValues;
    private Configuration values;
    private final AdminPanelMain plugin;

    public ConfigManager(File file) {
        this.plugin = AdminPanelMain.getPlugin();
        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);
        this.defaultValues = config.getDefaults();
        this.values = config.getRoot();
    }

    public Configuration getDefaults() {
        return defaultValues;
    }

    public Configuration getValues() {
        return values;
    }

    public void set(String path, Object value) {
        values.set(path, value);
        save();
    }

    public void setDefault(String path, Object value) {
        if (!defaultValues.contains(path)) {
            defaultValues.set(path, value);
            save();
        }
    }

    public void save() {
        // Set the values to the config
        for (Map.Entry<String, Object> entry : values.getValues(true).entrySet()) {
            // Check if Server Version over 1.18 if using Spigot and Paper Build if using Paper is higher then 134 then use setComments method to set comments in the config using the get comments method from the values
            if ((Bukkit.getServer().getVersion().contains("1.18") && Bukkit.getServer().getVersion().contains("Spigot")) ||
                    (Bukkit.getServer().getVersion().contains("Paper") && Integer.parseInt(Bukkit.getServer().getVersion().split("Paper version ")[1].split("-")[0]) > 134)) {
                // Check if methods exist in the file config class to avoid errors on older versions of Spigot and Paper and set the comments if all goes well
                try {
                    FileConfiguration.class.getMethod("setComments", String.class, List.class);
                    Configuration.class.getMethod("getComments", String.class);
                    config.setComments(entry.getKey(), values.getComments(entry.getKey()));
                } catch (NoSuchMethodException e) {
                    // Log to File using FileLogger
                    plugin.getFileLogger().writeToLog(Level.WARNING,
                            "The method setComments or getComments does not exist in the FileConfiguration or Configuration class." +
                                    "This is most likely due to the server version being lower than 1.18 or the Paper version being lower than 135." +
                                    "Please update your server to the latest version to retain comments in configs.", LogPrefix.ERROR);
                }
            }
            if (entry.getValue() instanceof ConfigurationSection) {
                config.createSection(entry.getKey());
                continue;
            }
            if (!entry.getValue().equals(defaultValues.get(entry.getKey()))) {
                config.set(entry.getKey(), entry.getValue());
            }
        }
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        try {
            config.load(file);
            values = config.getRoot();
            defaultValues = config.getDefaults();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadDefaults() {
        for (Map.Entry<String, Object> entry : defaultValues.getValues(true).entrySet()) {
            if (!values.contains(entry.getKey())) {
                values.set(entry.getKey(), entry.getValue());
            }
        }
        save();
    }

    public Object get(String path) {
        return values.get(path);
    }

    public Object getDefault(String path) {
        return defaultValues.get(path);
    }

    public File getFile() {
        return file;
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
