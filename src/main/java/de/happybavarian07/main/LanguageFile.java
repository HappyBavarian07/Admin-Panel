package de.happybavarian07.main;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class LanguageFile {

    private final Main plugin;
    private final File langFile;
    private final String langName;
    private final LanguageConfig langConfig;

    public LanguageFile(Main plugin, String langName) {
        this.plugin = plugin;
        this.langFile = new File(plugin.getDataFolder() + "/languages/" + langName + ".yml");
        this.langName = langName;
        this.langConfig = new LanguageConfig(this.langFile, this.langName, this.plugin);
    }

    public File getLangFile() {
        return langFile;
    }

    public String getLangName() {
        return langName;
    }

    public LanguageConfig getLangConfig() {
        return langConfig;
    }
}
