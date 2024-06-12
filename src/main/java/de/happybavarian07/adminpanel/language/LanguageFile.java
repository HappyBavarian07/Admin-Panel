package de.happybavarian07.adminpanel.language;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public class LanguageFile {

    private final JavaPlugin plugin;
    private final File langFile;
    private final String langName;
    private final LanguageConfig langConfig;

    public LanguageFile(JavaPlugin plugin, String langName, boolean autoSaveAndLoad) {
        this.plugin = plugin;
        this.langFile = new File(plugin.getDataFolder() + "/languages/" + langName + ".yml");
        this.langName = langName;
        this.langConfig = new LanguageConfig(this.langFile, this.langName, this.plugin, autoSaveAndLoad);
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

    public String getFullName() {
        return langConfig.getConfig().getString("LanguageFullName");
    }

    public String getFileVersion() {
        return langConfig.getConfig().getString("LanguageVersion");
    }

    public String getLanguageDescription() {
        return langConfig.getConfig().getString("LanguageDescription");
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public UUID getLangUUID() {
        if(langConfig.getConfig().getString("LanguageUUID") == null) {
            langConfig.getConfig().set("LanguageUUID", UUID.randomUUID().toString());
            langConfig.saveConfig();
        }
        return UUID.fromString(Objects.requireNonNull(langConfig.getConfig().getString("LanguageUUID")));
    }
}
