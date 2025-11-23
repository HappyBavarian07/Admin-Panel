package de.happybavarian07.adminpanel.utils.managers;/*
 * @Author HappyBavarian07
 * @Date 28.04.2024 | 11:09
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.service.api.DataService;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PluginDescriptionManager {
    private final AdminPanelMain plugin;
    private final Map<String, String> pluginDescriptionMap;
    private final File configFile;
    private FileConfiguration config;

    public PluginDescriptionManager() {
        this.plugin = AdminPanelMain.getPlugin();
        this.pluginDescriptionMap = new HashMap<>();
        this.configFile = new File(plugin.getDataFolder(), "plugin_descriptions.yml");
        if (!configFile.exists()) {
            plugin.saveResource("plugin_descriptions.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
        loadPluginDescriptions();

        try {
            DataService ds = plugin.getDataService();
            if (ds != null) {
                Map<String, String> saved = ds.loadMap("plugin.descriptions", String.class).join();
                if (saved != null && !saved.isEmpty()) {
                    pluginDescriptionMap.clear();
                    pluginDescriptionMap.putAll(saved);
                } else {
                    if (!pluginDescriptionMap.isEmpty()) ds.save("plugin.descriptions", pluginDescriptionMap);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void loadPluginDescriptions() {
        if (!configFile.exists()) {
            plugin.saveResource("plugin_descriptions.yml", false);
            this.config = YamlConfiguration.loadConfiguration(configFile);
        }
        if (config.isConfigurationSection("descriptions")) {
            for (String pluginName : config.getConfigurationSection("descriptions").getKeys(false)) {
                pluginDescriptionMap.put(pluginName, config.getString("descriptions." + pluginName + ".description"));
            }
        }
    }

    public String getDescriptionFromPlugin(Plugin plugin) {
        return getDescriptionFromPluginName(plugin.getName());
    }

    public String getDescriptionFromPluginName(String pluginName) {
        return pluginDescriptionMap.getOrDefault(pluginName, "NoDescriptionFound");
    }

    public void addPluginDescription(Plugin plugin, String description) {
        addPluginDescription(plugin.getName(), description, -1);
    }

    public void removePluginDescription(Plugin plugin) {
        removePluginDescription(plugin.getName());
    }

    public void removePluginDescription(String pluginName) {
        config.set("descriptions." + pluginName, null);
        pluginDescriptionMap.remove(pluginName);
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            DataService ds = AdminPanelMain.getPlugin().getDataService();
            if (ds != null) ds.save("plugin.descriptions", pluginDescriptionMap);
        } catch (Exception ignored) {
        }
    }

    public void addPluginDescription(String pluginName, String description, int resourceID) {
        if (resourceID != -1) {
            config.set("descriptions." + pluginName + ".resourceID", resourceID);
            pluginDescriptionMap.put(pluginName, getDescriptionFromResourceID(resourceID));
            config.set("descriptions." + pluginName + ".description", getDescriptionFromResourceID(resourceID));
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            config.set("descriptions." + pluginName + ".description", description);
            pluginDescriptionMap.put(pluginName, description);
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            DataService ds = AdminPanelMain.getPlugin().getDataService();
            if (ds != null) ds.save("plugin.descriptions", pluginDescriptionMap);
        } catch (Exception ignored) {
        }
    }

    public void addPluginDescription(Plugin plugin, String description, int resourceID) {
        addPluginDescription(plugin.getName(), description, resourceID);
    }

    public String getDescriptionFromPluginNameFromSpiget(String pluginName) {
        return getDescriptionFromResourceID(getResourceID(pluginName));
    }

    public int getResourceID(String pluginName) {
        // Read ResourceID Value From Config if Exists
        if (config.contains(pluginName)) {
            return config.getInt("descriptions." + pluginName + ".resourceID");
        } else {
            return -1;
        }
    }

    public String getDescriptionFromResourceID(int resourceID) {
        if (resourceID == -1) return "NoDescriptionTagFound";
        String description;
        try {
            description = String.valueOf(getObjectFromWebsite(
                    "https://api.spiget.org/v2/resources/" + resourceID).getString("tag"));
        } catch (JSONException e) {
            e.printStackTrace();
            description = "NoDescriptionTagFound";
        }
        return description;
    }

    public JSONObject getObjectFromWebsite(String url) {
        try {
            InputStream inputStream = new URL(url).openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String rawJsonText = readWebsite(reader);
            JSONObject jsonObject;
            jsonObject = new JSONObject(rawJsonText);
            reader.close();
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String readWebsite(final Reader reader) throws IOException {
        final StringBuilder sb = new StringBuilder();

        int counter;

        while ((counter = reader.read()) != -1) {
            sb.append((char) counter);
        }
        return sb.toString();
    }
}
