package de.happybavarian07.main;/*
 * @Author HappyBavarian07
 * @Date 15.11.2021 | 17:59
 */

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OldLanguageFileUpdater {
    private final AdminPanelMain plugin;
    private final LanguageManager lgm;

    public OldLanguageFileUpdater(AdminPanelMain plugin) {
        this.plugin = plugin;
        this.lgm = plugin.getLanguageManager();
    }

    public Map<String, Object> checkForUpdates(File oldFile, FileConfiguration newConfig, boolean nonDefaultLang) {
        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);
        Map<String, Object> updates = new HashMap<>();

        if (!nonDefaultLang) {
            if (!oldConfig.contains("LanguageFullName") ||
                    !oldConfig.getString("LanguageFullName", "").equals(newConfig.getString("LanguageFullName", "")))
                updates.put("LanguageFullName", newConfig.getString("LanguageFullName"));
        }
        if (!oldConfig.contains("LanguageVersion") ||
                !oldConfig.getString("LanguageVersion", "").equals(newConfig.getString("LanguageVersion", "")))
            updates.put("LanguageVersion", newConfig.getString("LanguageVersion"));

        // Messages
        if(newConfig.getConfigurationSection("Messages") != null) {
            for (String path : newConfig.getConfigurationSection("Messages").getKeys(true)) {
                if(newConfig.isConfigurationSection("Messages." + path)) {
                    updates.put("Messages." + path, null);
                    continue;
                }
                if (!oldConfig.contains("Messages." + path)) {
                    updates.put("Messages." + path, newConfig.get("Messages." + path));
                }
            }
        }

        // Items
        if(newConfig.getConfigurationSection("Items") != null) {
            for (String path : newConfig.getConfigurationSection("Items").getKeys(true)) {
                if(newConfig.isConfigurationSection("Items." + path)) {
                    updates.put("Items." + path, null);
                    continue;
                }
                if (!oldConfig.contains("Items." + path)) {
                    updates.put("Items." + path, newConfig.get("Items." + path));
                }
            }
        }

        // MenuTitles
        if(newConfig.getConfigurationSection("MenuTitles") != null) {
            for (String path : newConfig.getConfigurationSection("MenuTitles").getKeys(true)) {
                if(newConfig.isConfigurationSection("MenuTitles." + path)) {
                    updates.put("MenuTitles." + path, null);
                    continue;
                }
                if (!oldConfig.contains("MenuTitles." + path)) {
                    updates.put("MenuTitles." + path, newConfig.get("MenuTitles." + path));
                }
            }
        }
        return updates;
    }

    public void updateFile(File oldFile, FileConfiguration newConfig, String langName, boolean nonDefaultLang) {
        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);
        Map<String, Object> checkedUpdates = checkForUpdates(oldFile, newConfig, nonDefaultLang);
        //System.out.println("Language: " + langName);
        if (checkedUpdates.isEmpty()) return;
        //System.out.println("Checked Updates:" + checkedUpdates);
        for (String path : checkedUpdates.keySet()) {
            //System.out.println("Check:" + (!oldConfig.contains(path) || oldConfig.get(path) == null));
            if ((!oldConfig.contains(path) || oldConfig.get(path) == null)) {
                //System.out.println("Path: " + path);
                oldConfig.set(path, checkedUpdates.get(path));
                // Save the File
                //System.out.println("Value (now): " + oldConfig.get(path));
            }
            if(newConfig.isConfigurationSection(path) && !oldConfig.isConfigurationSection(path)) {
                oldConfig.createSection(path);
            }
        }
        oldConfig.options().header(newConfig.options().header());
        //oldConfig.options().header("Test Header");
        //System.out.println("Header: " + newConfig.options().header());
        // Save the File
        try {
            oldConfig.save(oldFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
