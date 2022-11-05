package de.happybavarian07.adminpanel.utils;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Level;

public class NewUpdater implements Listener {
    private final AdminPanelMain plugin;
    private final int resourceID;
    private final PluginUtils pluginUtils;
    private final Messages messages;
    private final String fileName;
    private final JavaPlugin pluginToUpdate;
    private VersionComparator versionComparator;
    private String linkToFile;
    boolean bypassExternalURL;

    public NewUpdater(AdminPanelMain plugin, int resourceID, String fileName, @Nullable JavaPlugin pluginToUpdate, String linkToFile, boolean bypassExternalURL) {
        this.plugin = plugin;
        this.pluginUtils = new PluginUtils();
        this.resourceID = resourceID;
        this.versionComparator = VersionComparator.EQUALVERSIONS;
        this.fileName = fileName;
        this.pluginToUpdate = pluginToUpdate;
        this.linkToFile = linkToFile == null ? "" : linkToFile.replace("%version%", getLatestVersionName());
        messages = new Messages();
        this.bypassExternalURL = bypassExternalURL;
    }

    public boolean resourceIsOnSpigot() {
        JSONObject returnObject = getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID);
        return !returnObject.has("error");
    }

    public VersionComparator getVersionComparator() {
        return versionComparator;
    }

    public void setVersionComparator(VersionComparator versionComparator) {
        this.versionComparator = versionComparator;
    }

    public String html2text(String html) {
        return Jsoup.parse(html).text();
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
        plugin.getFileLogger().writeToLog(Level.INFO, "Requested Latest Version ID for Plugin: " + getPluginName() + " -> " + version, "Updater");
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
        plugin.getFileLogger().writeToLog(Level.INFO, "Requested Latest Version Name for Plugin: " + getPluginName() + " -> " + version, "Updater");
        return version;
    }

    public String getPluginName() {
        if (fileName.equals("Admin-Panel-%version%.jar") && resourceID == 91800)
            return plugin.getName();
        if(pluginToUpdate == null) return null;
        return pluginToUpdate.getName();
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

    public JSONArray getArrayFromWebsite(String url) {
        try {
            InputStream inputStream = new URL(url).openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String rawJsonText = readWebsite(reader);
            JSONArray jsonArray;
            jsonArray = new JSONArray(rawJsonText);
            reader.close();
            return jsonArray;
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

    public boolean updateAvailable() {
        String spigotVersion = getLatestVersionName();
        String pluginVersion = getPluginVersion();

        boolean available = versionComparator.updateAvailable(pluginVersion, spigotVersion);

        if (available) {
            plugin.getFileLogger().writeToLog(Level.WARNING, "Checked for Plugin: " + getPluginName() + " if an Update is available -> true", "Updater");
            return true;
        } else {
            plugin.getFileLogger().writeToLog(Level.WARNING, "Checked for Plugin: " + getPluginName() + " if an Update is available -> false", "Updater");
            return false;
        }
    }

    public void checkForUpdates(boolean logInConsole) {
        boolean updateAvailable = updateAvailable();
        if (!updateAvailable) {
            if (logInConsole && plugin.getConfig().getBoolean("Plugin.Updater.logNoUpdate")) {
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', AdminPanelMain.getPrefix() + "&a No Update available for Plugin: " + getPluginName()));
            }
            plugin.getFileLogger().writeToLog(Level.INFO, "Checked Plugin: " + getPluginName() + " for Updates -> There is no Update Available", "Updater");
        } else {
            JSONObject jsonObject = getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID + "/updates/latest");
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getStartUpLogger().coloredSpacer(ChatColor.RED);
            try {
                String descriptionEncoded = jsonObject.getString("description");
                String descriptionDecoded = html2text(new String(Base64.getDecoder().decode(descriptionEncoded)));
                getMessages().sendUpdateMessage(plugin.getServer().getConsoleSender());
                /*plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&',
                        " &cPlugin: " + getPluginName() + " outdated! &6Please download the new version on&r\n" +
                                "&6https://www.spigotmc.org/" + getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID).getJSONObject("file").get("url") + "\n" +
                                "&6or just activate automatic updating and replacing in the Config!\n" +
                                "&bCurrent Version: &c" + getPluginVersion() + "&r\n" +
                                "&bNew Version: &c" + getLatestVersionName() + "&r\n" +
                                "&bNew Version ID: &c" + getLatestVersionID() + "&r\n" +
                                "&bNew Version Title: &c" + jsonObject.getString("title") + "&r\n" +
                                "&bNew Version Description: &c" + descriptionDecoded));*/
                plugin.getFileLogger().writeToLog(Level.WARNING, "Checked Plugin: " + getPluginName() + " for Updates -> There is an Update Available! (Version Change: " + getPluginVersion() + " -> " + getLatestVersionName() + ")", "Updater");
            } catch (Exception e) {
                e.printStackTrace();
            }
            plugin.getStartUpLogger().coloredSpacer(ChatColor.RED);
        }
    }

    /**
     * Downloaded eine spezifische Version des Plugins
     * @param versionID Die ID der Version
     * @param replace       Ob die alte Version automatisch erneuert werden soll
     * @param force         Ob das System dazu gezwungen wird
     * @param logInConsole  Ob das Plugin eine Nachricht ausgeben soll oder nur in den plugin.log File schreiben soll
     * @param spigotVersion Die Version die heruntergeladen werden soll
     * @return Eine Update Response
     */
    public UpdateResponse downloadSpecificUpdate(boolean replace, boolean force, boolean logInConsole, String spigotVersion, String versionID) {
        if (!plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") && !force)
            return UpdateResponse.CONFIG_OPTIONS_ARE_NOT_SET_RIGHT;
        File downloadPath = new File(plugin.getDataFolder() + "/downloaded-update/" + resourceID + ".jar");
        File oldPluginFile = new File("plugins/Admin-Panel-" + getPluginVersion() + ".jar");
        File newPluginFile = new File("plugins/Admin-Panel-" + spigotVersion + ".jar");
        if (!new File(plugin.getDataFolder() + "/downloaded-update").exists()) {
            new File(plugin.getDataFolder() + "/downloaded-update").mkdir();
        }
        if (!downloadPath.exists()) {
            try {
                downloadPath.createNewFile();
            } catch (IOException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", "Updater");
                return UpdateResponse.ERROR;
            }
        }
        URL downloadURL;
        try {
            downloadURL = new URL("https://api.spiget.org/v2/resources/" + resourceID + "/versions/" + versionID + "/download");
        } catch (MalformedURLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", "Updater");
            return UpdateResponse.SPIGET_ERROR;
        }
        try {
            //pluginUtils.downloadFileFromURL(downloadPath, downloadURL);
            FileUtils.copyURLToFile(downloadURL, downloadPath);
        } catch (IOException /*| InterruptedException*/ e) {
            e.printStackTrace();
            plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", "Updater");
            return UpdateResponse.DOWNLOAD_FAIL;
        }
        downloadPath.renameTo(new File(plugin.getDataFolder() + "/downloaded-update/Admin-Panel-" + spigotVersion + ".jar"));
        downloadPath = new File(plugin.getDataFolder() + "/downloaded-update/Admin-Panel-" + spigotVersion + ".jar");
        new File(plugin.getDataFolder() + "/downloaded-update/" + resourceID + ".jar").delete();
        if ((plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") || force) && !replace) {
            if (logInConsole) {
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', "&aThe new version for Plugin: " + getPluginName() + " was downloaded automatically and is located in the update folder!"));
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', "&aThe Update is now available: &c" + downloadPath));
            }
            plugin.getFileLogger().writeToLog(Level.INFO, "New Version for Plugin: " + getPluginName() + " (" + spigotVersion + ") got downloaded into the Update Folder! (Plugin Version: " + getPluginVersion() + ")", "Updater");
            return UpdateResponse.UPDATE_SUCCESS;
        } else if ((plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") || force) && replace) {

            try {
                pluginUtils.unload(plugin);
                if (oldPluginFile.exists()) {
                    oldPluginFile.delete();
                }
                if (newPluginFile.exists()) {
                    newPluginFile.delete();
                }
                FileUtils.moveFileToDirectory(downloadPath, plugin.getDataFolder().getParentFile(), false);
            } catch (IOException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", "Updater");
                return UpdateResponse.ERROR;
            }
            try {
                pluginUtils.load(newPluginFile);
            } catch (InvalidPluginException | InvalidDescriptionException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", "Updater");
                return UpdateResponse.LOADING_ERROR;
            }
            Bukkit.getPluginManager().enablePlugin(pluginUtils.getPluginByName("Admin-Panel"));
            if (logInConsole) {
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&',
                        "&aThe new version for Plugin: " + getPluginName() + " was downloaded automatically and the old one replaced! \n" +
                                "&aAnd The New Version started automatically! If you can, please check Console for Errors!"));
            }
            plugin.getFileLogger().writeToLog(Level.INFO, "New Version for Plugin: " + getPluginName() + " (" + spigotVersion + ") got downloaded and replaced with the Plugin Version (" + getPluginVersion() + ")!", "Updater");
            return UpdateResponse.UPDATE_SUCCESS;
        } else
            return UpdateResponse.ERROR;
    }

    /**
     * @param replace      Ob die alte Version automatisch erneuert werden soll
     * @param force        Ob das System dazu gezwungen wird
     * @param logInConsole Ob das Plugin eine Nachricht ausgeben soll oder nur in den plugin.log File schreiben soll
     * @return ein Update Response Object
     */
    public UpdateResponse downloadLatestUpdate(boolean replace, boolean force, boolean logInConsole) {
        if (!plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") && !force)
            return UpdateResponse.CONFIG_OPTIONS_ARE_NOT_SET_RIGHT;
        //try {
        File downloadPath = new File(plugin.getDataFolder() + "/downloaded-update/" + resourceID + ".jar");
        File oldPluginFile = new File("plugins/" + fileName.replace("%version%", getPluginVersion()));
        File newPluginFile = new File("plugins/" + fileName.replace("%version%", getLatestVersionName()));
        if (!new File(plugin.getDataFolder() + "/downloaded-update").exists()) {
            new File(plugin.getDataFolder() + "/downloaded-update").mkdir();
        }
        if (!downloadPath.exists()) {
            try {
                downloadPath.createNewFile();
            } catch (IOException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", "Updater");
                return UpdateResponse.ERROR;
            }
        }
        URL downloadURL;
        try {
            if(isExternalFile() && !getLinkToFile().equals("") && !bypassExternalURL()) {
                downloadURL = new URL(linkToFile);
            } else {
                downloadURL = new URL("https://api.spiget.org/v2/resources/" + resourceID + "/download");
            }
        } catch (MalformedURLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", "Updater");
            return UpdateResponse.SPIGET_ERROR;
        }
        try {
            //pluginUtils.downloadFileFromURL(downloadPath, downloadURL);
            FileUtils.copyURLToFile(downloadURL, downloadPath);
        } catch (IOException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", "Updater");
            return UpdateResponse.DOWNLOAD_FAIL;
        }
        downloadPath.renameTo(new File(plugin.getDataFolder() + "/downloaded-update/" + fileName.replace("%version%", getLatestVersionName())));
        downloadPath = new File(plugin.getDataFolder() + "/downloaded-update/" + fileName.replace("%version%", getLatestVersionName()));
        new File(plugin.getDataFolder() + "/downloaded-update/" + resourceID + ".jar").delete();
        if (force && !replace) {
            if (logInConsole) {
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', "&aThe new version for Plugin: " + getPluginName() + " was downloaded automatically and is located in the update folder!"));
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', "&aThe Update is now available: &c" + downloadPath));
            }
            plugin.getFileLogger().writeToLog(Level.INFO, "New Version for Plugin: " + getPluginName() + " (" + getLatestVersionName() + ") got downloaded into the Update Folder! (Plugin Version: " + getPluginVersion() + ")", "Updater");
            return UpdateResponse.UPDATE_SUCCESS;
        } else if (force) {
            try {
                if (getPluginName() != null)
                    pluginUtils.unload(pluginUtils.getPluginByName(getPluginName()));
                if (oldPluginFile.exists()) {
                    oldPluginFile.delete();
                }
                if (newPluginFile.exists()) {
                    newPluginFile.delete();
                }
                FileUtils.moveFileToDirectory(downloadPath, plugin.getDataFolder().getParentFile(), false);
            } catch (IOException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", "Updater");
                return UpdateResponse.ERROR;
            }
            Plugin loadedPlugin;
            try {
                loadedPlugin = pluginUtils.load(newPluginFile);
            } catch (InvalidPluginException | InvalidDescriptionException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", "Updater");
                return UpdateResponse.LOADING_ERROR;
            }
            Bukkit.getPluginManager().enablePlugin(loadedPlugin);
            if (logInConsole) {
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&',
                        "&aThe new Version for Plugin: " + getPluginName() + " was downloaded automatically and the old one replaced! \n" +
                                "&aAnd The New Version started automatically! If you can, please check Console for Errors!"));
            }
            plugin.getFileLogger().writeToLog(Level.INFO, "New Version for Plugin: " + getPluginName() + " (" + getLatestVersionName() + ") got downloaded and replaced with the Plugin Version (" + getPluginVersion() + ")!", "Updater");
            return UpdateResponse.UPDATE_SUCCESS;
        } else
            return UpdateResponse.ERROR;
    }

    public String getPluginVersion() {
        if(getPluginName() == null) return getLatestVersionName();
        plugin.getFileLogger().writeToLog(Level.INFO, "Requested Plugin: " + getPluginName() + " Version -> " + pluginUtils.getPluginByName(getPluginName()).getDescription().getVersion(), "Updater");
        return pluginUtils.getPluginByName(getPluginName()).getDescription().getVersion();
    }

    public int getResourceID() {
        return resourceID;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("adminpanel.updatenotify") && updateAvailable()) {
            JSONObject jsonObject = getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID + "/updates/latest");
            getMessages().sendUpdateMessage(player);
            if (plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") &&
                    !plugin.getConfig().getBoolean("Plugin.Updater.automaticReplace")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThe new version was downloaded automatically and is located in the update folder!"));
            }
            if (plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") &&
                    plugin.getConfig().getBoolean("Plugin.Updater.automaticReplace")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThe new version was downloaded automatically and the old version was automatically replaced! \n" +
                        "&aYou just need to reload the plugin via Plugin Manager, / reload, / restart or / stop / start"));
            }
        }
    }

    public Messages getMessages() {
        return messages;
    }

    public boolean isExternalFile() {
        JSONObject returnObject = getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID);
        try {
            return returnObject.getBoolean("external");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Plugin getPluginToUpdate() {
        return pluginToUpdate;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLinkToFile() {
        return linkToFile;
    }

    public void setLinkToFile(String linkToFile) {
        this.linkToFile = linkToFile;
    }

    public boolean bypassExternalURL() {
        return bypassExternalURL;
    }

    public enum UpdateResponse {
        NO_UPDATE, UPDATE_SUCCESS, DOWNLOAD_FAIL, SPIGET_ERROR, CONFIG_OPTIONS_ARE_NOT_SET_RIGHT, ERROR, LOADING_ERROR
    }

    public class Messages {
        public void sendUpdateMessage(CommandSender sender) {
            JSONObject jsonObject = getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID + "/updates/latest");
            try {
                String descriptionEncoded = jsonObject.getString("description");
                String descriptionDecoded = html2text(new String(Base64.getDecoder().decode(descriptionEncoded)));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        " &cPlugin: " + getPluginName() + " outdated! &6Please download the new version on&r\n" +
                                "&6https://www.spigotmc.org/" + getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID).getJSONObject("file").get("url") + "\n" +
                                "&6or just activate automatic updating and replacing in the Config!\n" +
                                "&bCurrent Version: &c" + getPluginVersion() + "&r\n" +
                                "&bNew Version: &c" + getLatestVersionName() + "&r\n" +
                                "&bNew Version ID: &c" + getLatestVersionID() + "&r\n" +
                                "&bNew Version Title: &c" + jsonObject.getString("title") + "&r\n" +
                                "&bNew Version Description: &c" + descriptionDecoded));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void sendNoUpdateMessage(CommandSender sender) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', AdminPanelMain.getPrefix() + "&a No Updates Available for Plugin: " + getPluginName() + "!"));
        }
    }
}
