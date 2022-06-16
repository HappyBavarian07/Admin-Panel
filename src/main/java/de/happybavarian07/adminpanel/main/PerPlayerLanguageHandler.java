package de.happybavarian07.adminpanel.main;/*
 * @Author HappyBavarian07
 * @Date 26.04.2022 | 17:05
 */

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PerPlayerLanguageHandler {
    private final LanguageManager lgm;
    private final File dataFile;
    private final FileConfiguration dataConfig;

    public PerPlayerLanguageHandler(LanguageManager lgm, File dataFile, FileConfiguration dataConfig) {
        this.lgm = lgm;
        this.dataFile = dataFile;
        this.dataConfig = dataConfig;
    }

    public String getPlayerLanguageName(UUID uuid) {
        return dataConfig.getString("playerdata." + uuid.toString() + ".language", lgm.getCurrentLangName());
    }

    public LanguageFile getPlayerLanguage(UUID uuid) {
        return lgm.getLang(dataConfig.getString("playerdata." + uuid.toString() + ".language", lgm.getCurrentLangName()), true);
    }

    public Map<UUID, LanguageFile> getPlayerLanguages() {
        Map<UUID, LanguageFile> playerLangs = new HashMap<>();
        for(String configSec : dataConfig.getConfigurationSection("playerdata").getKeys(false)) {
            playerLangs.put(UUID.fromString(configSec),
                    lgm.getLang(dataConfig.getString("playerdata." + UUID.fromString(configSec).toString() + ".language",
                            lgm.getCurrentLangName()), true));
        }
        return playerLangs;
    }

    public void setPlayerLanguage(UUID uuid, String language) {
        if(lgm.getLang(language, false) == null) language = lgm.getCurrentLangName();
        dataConfig.set("playerdata." + uuid.toString() + ".language", language);

        saveConfig();
    }

    public void removePlayerLanguage(UUID uuid) {
        dataConfig.set("playerdata." + uuid.toString(), null);

        saveConfig();
    }

    public void saveConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
