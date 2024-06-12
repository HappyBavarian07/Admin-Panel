package de.happybavarian07.adminpanel.language;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class LanguageConfig {

    private final JavaPlugin plugin;
    private final String langName;
    private File file;
    private FileConfiguration config;

    public LanguageConfig(File langFile, String langName, JavaPlugin plugin, boolean autoSaveAndLoad) {
        this.plugin = plugin;
        this.langName = langName;
        this.file = langFile;
        //System.out.println("Creating Language Config: " + this.langName + "  |  " + this.file);
        if(autoSaveAndLoad) {
            saveDefaultConfig();
            this.config = YamlConfiguration.loadConfiguration(this.file);
        }
    }

    public void reloadConfig() {
        if (this.file == null)
            this.file = new File(this.plugin.getDataFolder() + "/languages", this.langName + ".yml");

        this.config = YamlConfiguration.loadConfiguration(this.file);

        if (this.plugin.getResource("languages/" + this.langName + ".yml") != null) {
            InputStream defaultStream = this.plugin.getResource("languages/" + this.langName + ".yml");
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
                this.config.setDefaults(defaultConfig);
            }
        }
    }

    public FileConfiguration getConfig() {
        if (this.config == null)
            reloadConfig();

        return this.config;
    }

    public void saveConfig() {
        if (this.config == null || this.file == null)
            return;

        try {
            this.getConfig().save(this.file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save Config to " + this.file, e);
        }
    }

    public void saveDefaultConfig() {
        if (this.file == null)
            this.file = new File(this.plugin.getDataFolder() + "/languages", this.langName + ".yml");

        if (!this.file.exists()) {
            this.plugin.saveResource("languages/" + this.langName + ".yml", false);
        }
    }

    public String getLangName() {
        return langName;
    }

    public File getFile() {
        return file;
    }
}
