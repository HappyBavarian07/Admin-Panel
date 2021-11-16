package de.happybavarian07.main;/*
 * @Author HappyBavarian07
 * @Date 15.11.2021 | 17:59
 */

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OldLanguageFileUpdater {
    private final AdminPanelMain plugin;
    private final LanguageManager lgm;

    public OldLanguageFileUpdater(AdminPanelMain plugin) {
        this.plugin = plugin;
        this.lgm = plugin.getLanguageManager();
    }

    public Map<String, Object> checkForUpdates(File oldFile, FileConfiguration newConfig) {
        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);
        Map<String, Object> updates = new HashMap<>();

        if (!oldConfig.contains("LanguageFullName") ||
                !oldConfig.getString("LanguageFullName", "").equals(newConfig.getString("LanguageFullName", "")))
            updates.put("LanguageFullName", newConfig.getString("LanguageFullName"));
        if (!oldConfig.contains("LanguageVersion") ||
                !oldConfig.getString("LanguageVersion", "").equals(newConfig.getString("LanguageVersion", "")))
            updates.put("LanguageVersion", newConfig.getString("LanguageVersion"));

        // Messages
        for (String path : newConfig.getConfigurationSection("Messages").getKeys(true)) {
            if(!oldConfig.contains("Messages." + path) && !newConfig.isConfigurationSection("Messages." + path)) {
                updates.put("Messages." + path, newConfig.get("Messages." + path));
            }
        }

        // Items
        for (String path : newConfig.getConfigurationSection("Items").getKeys(true)) {
            if(!oldConfig.contains("Items." + path) && !newConfig.isConfigurationSection("Items." + path)) {
                updates.put("Items." + path, newConfig.get("Items." + path));
            }
        }

        // MenuTitles
        for (String path : newConfig.getConfigurationSection("MenuTitles").getKeys(true)) {
            if(!oldConfig.contains("MenuTitles." + path) && !newConfig.isConfigurationSection("MenuTitles." + path)) {
                updates.put("MenuTitles." + path, newConfig.get("MenuTitles." + path));
            }
        }
        return updates;
    }

    public void updateFile(File oldFile, FileConfiguration newConfig, String langName) {
        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);
        Map<String, Object> checkedUpdates = checkForUpdates(oldFile, newConfig);
        //System.out.println("Language: " + langName);
        if(checkedUpdates.isEmpty()) return;
        for(String path : checkedUpdates.keySet()) {
            if((!oldConfig.contains(path) || oldConfig.get(path) == null) && !oldConfig.isConfigurationSection(path)) {
                //System.out.println("Path: " + path);
                oldConfig.set(path, checkedUpdates.get(path));
                // Save the File
                //System.out.println("Value (now): " + oldConfig.get(path));
            }
        }
        // Save the File
        try {
            oldConfig.save(oldFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
