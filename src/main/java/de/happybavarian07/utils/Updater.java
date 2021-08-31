package de.happybavarian07.utils;

import de.happybavarian07.main.Main;
import io.CodedByYou.spiget.Resource;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XMLTokener;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.logging.Level;

public class Updater implements Listener {
    private final Main plugin;
    private final int resourceID;
    Resource resource;

    public Updater(Main plugin, int resourceID) {
        this.plugin = plugin;
        this.resourceID = resourceID;
        try {
            this.resource = new Resource(resourceID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getLatestVersionID() {
        String version;
        try {
            version = String.valueOf(getObjectFromWebsite(
                    "https://api.spiget.org/v2/resources/" + resourceID + "/versions/latest").getInt("id"));
        } catch (JSONException e) {
            e.printStackTrace();
            version = "NoVersionFound";
        }
        return version;
    }

    public String getLatestVersionName() {
        String version;
        try {
            version = String.valueOf(getObjectFromWebsite(
                    "https://api.spiget.org/v2/resources/" + resourceID + "/versions/latest").getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
            version = "NoVersionFound";
        }
        return version;
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

        while((counter = reader.read()) != -1) {
            sb.append((char) counter);
        }
        return sb.toString();
    }

    public boolean updateAvailable() {
        String spigotVersion = getLatestVersionName();
        String pluginVersion = getPluginVersion();
        return !spigotVersion.equals(pluginVersion);
    }

    public void checkForUpdates() {
        boolean updateAvailable = updateAvailable();
        if(!updateAvailable) {
            plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', Main.getPrefix() + " &a No Update available!"));
        } else {
            JSONObject jsonObject = getObjectFromWebsite("https://api.spiget.org/v2/resources/91800/updates/latest?size=5&page=0&sort=%2B");
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getStartUpLogger().coloredSpacer(ChatColor.RED);
            try {
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&',
                        " &cPlugin outdated! &6Please download the new version on&r\n" +
                                "&6https://www.spigotmc.org/resources/servermanager-an-mc-integrated-admin-panel-german.91800/\n" +
                                "&bCurrent Version: &c" + getPluginVersion() + "&r\n" +
                                "&bNew Version: &c" + getLatestVersionName() + "&r\n" +
                                "&bNew Version ID: &c" + getLatestVersionID() + "&r\n" +
                                "&bNew Version Title: &c" + jsonObject.getString("title") + "&r"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            plugin.getStartUpLogger().coloredSpacer(ChatColor.RED);
        }
    }

    public void downloadPlugin() {
        if(!plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate")) return;
        try {
            File downloadPath = new File(plugin.getDataFolder() + "/downladed-update/91800.jar");
            downloadPath.getParentFile().mkdir();
            if(!downloadPath.exists()) {
                downloadPath.createNewFile();
            }
            URL downloadURL = new URL("https://api.spiget.org/v2/resources/91800/download");
            FileUtils.copyURLToFile(downloadURL, downloadPath);
            downloadPath.renameTo(new File(plugin.getDataFolder() + "/downladed-update/Admin-Panel-" + getLatestVersionName() + ".jar"));
            downloadPath = new File(plugin.getDataFolder() + "/downladed-update/Admin-Panel-" + getLatestVersionName() + ".jar");
            new File(plugin.getDataFolder() + "/downladed-update/91800.jar").delete();
            if(plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") &&
                    !plugin.getConfig().getBoolean("Plugin.Updater.automaticReplace")) {
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', "&aThe new version was downloaded automatically and is located in the update folder!"));
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', "&aThe Update is now available: &c" + downloadPath));
            }
            if(plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") &&
                    plugin.getConfig().getBoolean("Plugin.Updater.automaticReplace")) {
                Bukkit.getPluginManager().disablePlugin(plugin);
                new File("plugins/Admin-Panel-" + getPluginVersion() + ".jar").delete();
                FileUtils.moveFileToDirectory(downloadPath, plugin.getDataFolder().getParentFile(), false);
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&',
                        "&aThe new version was downloaded automatically and the old version was automatically replaced! \n" +
                        "&aAnd The New Version started automatically! If you can please check Console for Errors!"));
                Bukkit.reload();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public Resource getResource() {
        return resource;
    }

    public String getPluginVersion() { return plugin.getDescription().getVersion(); }

    public int getResourceID() {
        return resourceID;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(player.hasPermission("adminpanel.updatenotify")) {
            JSONObject jsonObject = getObjectFromWebsite("https://api.spiget.org/v2/resources/91800/updates/latest?size=5&page=0&sort=%2B");
            try {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        Main.getPrefix() + " &cPlugin outdated! &6Please download the new version on&r\n" +
                                "&6https://www.spigotmc.org/resources/servermanager-an-mc-integrated-admin-panel-german.91800/\n" +
                                "&bCurrent Version: &c" + getPluginVersion() + "&r\n" +
                                "&bNew Version: &c" + getLatestVersionName() + "&r\n" +
                                "&bNew Version ID: &c" + getLatestVersionID() + "&r\n" +
                                "&bNew Version Title: &c" + jsonObject.getString("title") + "&r"));
                if(plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") &&
                        !plugin.getConfig().getBoolean("Plugin.Updater.automaticReplace")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThe new version was downloaded automatically and is located in the update folder!"));
                }
                if(plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") &&
                        plugin.getConfig().getBoolean("Plugin.Updater.automaticReplace")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThe new version was downloaded automatically and the old version was automatically replaced! \n" +
                            "&aYou just need to reload the plugin via Plugin Manager, / reload, / restart or / stop / start"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
