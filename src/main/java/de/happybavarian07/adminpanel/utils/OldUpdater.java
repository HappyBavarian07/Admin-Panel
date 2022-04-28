package de.happybavarian07.adminpanel.utils;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;

public class OldUpdater implements Listener {
    private final AdminPanelMain plugin;
    private final int resourceID;
    private final PluginUtils pluginUtils;

    public OldUpdater(AdminPanelMain plugin, int resourceID) {
        this.plugin = plugin;
        this.pluginUtils = new PluginUtils();
        this.resourceID = resourceID;
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
        plugin.getFileLogger().writeToLog(Level.INFO, "Requested Latest Version ID -> " + version, "Updater");
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
        plugin.getFileLogger().writeToLog(Level.INFO, "Requested Latest Version Name -> " + version, "Updater");
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

        while ((counter = reader.read()) != -1) {
            sb.append((char) counter);
        }
        return sb.toString();
    }

    public boolean updateAvailable() {
        String[] spigotVersion = getLatestVersionName().split("\\.");
        String[] pluginVersion = getPluginVersion().split("\\.");
        String[] fullSpigotVersion = new String[3];
        String[] fullPluginVersion = new String[3];
        if (spigotVersion.length < 1) {
            fullSpigotVersion[0] = "0";
        } else {
            fullSpigotVersion[0] = spigotVersion[0];
        }
        if (spigotVersion.length < 2) {
            fullSpigotVersion[1] = "0";
        } else {
            fullSpigotVersion[1] = spigotVersion[1];
        }
        if (spigotVersion.length < 3) {
            fullSpigotVersion[2] = "0";
        } else {
            fullSpigotVersion[2] = spigotVersion[2];
        }

        if (pluginVersion.length < 1) {
            fullPluginVersion[0] = "0";
        } else {
            fullPluginVersion[0] = pluginVersion[0];
        }
        if (pluginVersion.length < 2) {
            fullPluginVersion[1] = "0";
        } else {
            fullPluginVersion[1] = pluginVersion[1];
        }
        if (pluginVersion.length < 3) {
            fullPluginVersion[2] = "0";
        } else {
            fullPluginVersion[2] = pluginVersion[2];
        }
        int[] majorVersions = new int[]{Integer.parseInt(fullSpigotVersion[0]), Integer.parseInt(fullPluginVersion[0])};
        int[] minorVersions = new int[]{Integer.parseInt(fullSpigotVersion[1]), Integer.parseInt(fullPluginVersion[1])};
        int[] patchVersions = new int[]{Integer.parseInt(fullSpigotVersion[2]), Integer.parseInt(fullPluginVersion[2])};
        boolean major = majorVersions[0] > majorVersions[1];
        boolean minor = minorVersions[0] > minorVersions[1];
        boolean patch = patchVersions[0] > patchVersions[1];
        // Debug
        /*System.out.println("Major: " + major);
        System.out.println("Minor: " + minor);
        System.out.println("Patch: " + patch);
        System.out.println("Versions: " + majorVersions[0] + "|" + majorVersions[1] + " : " +
                minorVersions[0] + "|" + minorVersions[1] + " : " +
                patchVersions[0] + "|" + patchVersions[1]);
        System.out.println("Versions (normal): " +
                majorVersions[0] + "." + minorVersions[0] + "." + patchVersions[0] + " | " +
                majorVersions[1] + "." + minorVersions[1] + "." + patchVersions[1]);
        if (major) {
            System.out.println("Insgesamt: true");
        } else {
            if (majorVersions[0] >= majorVersions[1]) {
                if (minorVersions[0] >= minorVersions[1]) {
                    System.out.println("Insgesamt: " + (minor || patch));
                }
                System.out.println("Insgesamt: false");
            }
            System.out.println("Insgesamt: " + (minor && patch));
        }*/

        if (major) {
            return true;
        } else {
            if (majorVersions[0] >= majorVersions[1]) {
                if (minorVersions[0] >= minorVersions[1]) {
                    plugin.getFileLogger().writeToLog(Level.WARNING, "Checked if an Update is available -> " + (minor || patch), "Updater");
                    return minor || patch;
                }
                plugin.getFileLogger().writeToLog(Level.WARNING, "Checked if an Update is available -> false", "Updater");
                return false;
            }
            plugin.getFileLogger().writeToLog(Level.WARNING, "Checked if an Update is available -> " + (minor && patch), "Updater");
            return minor && patch;
        }
    }

    public void checkForUpdates(boolean logInConsole) {
        boolean updateAvailable = updateAvailable();
        if (!updateAvailable) {
            if (logInConsole) {
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', AdminPanelMain.getPrefix() + "&a No Update available!"));
            }
            plugin.getFileLogger().writeToLog(Level.INFO, "Checked For Updates -> There is no Update Available!", "Updater");
        } else {
            JSONObject jsonObject = getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID + "/updates/latest");
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getStartUpLogger().coloredSpacer(ChatColor.RED);
            try {
                String descriptionEncoded = jsonObject.getString("description");
                String descriptionDecoded = html2text(new String(Base64.getDecoder().decode(descriptionEncoded)));
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&',
                        " &cPlugin outdated! &6Please download the new version on&r\n" +
                                "&6https://www.spigotmc.org/resources/servermanager-an-mc-integrated-admin-panel-german.91800/\n" +
                                "&6or just activate automatic updating and replacing in the Config!\n" +
                                "&bCurrent Version: &c" + getPluginVersion() + "&r\n" +
                                "&bNew Version: &c" + getLatestVersionName() + "&r\n" +
                                "&bNew Version ID: &c" + getLatestVersionID() + "&r\n" +
                                "&bNew Version Title: &c" + jsonObject.getString("title") + "&r\n" +
                                "&bNew Version Description: &c" + descriptionDecoded));
                plugin.getFileLogger().writeToLog(Level.WARNING, "Checked For Updates -> There is an Update Available! (Version Change: " + getPluginVersion() + " -> " + getLatestVersionName() + ")", "Updater");
            } catch (Exception e) {
                e.printStackTrace();
            }
            plugin.getStartUpLogger().coloredSpacer(ChatColor.RED);
        }
    }

    public void sendUpdateMessage(Player sender) {
        JSONObject jsonObject = getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID + "/updates/latest");
        try {
            String descriptionEncoded = jsonObject.getString("description");
            String descriptionDecoded = html2text(new String(Base64.getDecoder().decode(descriptionEncoded)));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    " &cPlugin outdated! &6Please download the new version on&r\n" +
                            "&6https://www.spigotmc.org/resources/servermanager-an-mc-integrated-admin-panel-german.91800/\n" +
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

    public void sendUpdateMessage(ConsoleCommandSender sender) {
        JSONObject jsonObject = getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID + "/updates/latest");
        try {
            String descriptionEncoded = jsonObject.getString("description");
            String descriptionDecoded = html2text(new String(Base64.getDecoder().decode(descriptionEncoded)));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    " &cPlugin outdated! &6Please download the new version on&r\n" +
                            "&6https://www.spigotmc.org/resources/servermanager-an-mc-integrated-admin-panel-german.91800/\n" +
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

    public void sendNoUpdateMessage(Player sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', AdminPanelMain.getPrefix() + "&a No Updates Available!"));
    }

    public void sendNoUpdateMessage(ConsoleCommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', AdminPanelMain.getPrefix() + "&a No Updates Available!"));
    }

    /**
     * @param replace      Ob die alte Version automatisch erneuert werden soll
     * @param force        Ob das System dazu gezwungen wird
     * @param logInConsole Ob das Plugin eine Nachricht ausgeben soll oder nur in den plugin.log File schreiben soll
     */
    public void downloadPlugin(boolean replace, boolean force, boolean logInConsole) {
        if (!plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") && !force) return;
        try {
            File downloadPath = new File(plugin.getDataFolder() + "/downloaded-update/" + resourceID + ".jar");
            File oldPluginFile = new File("plugins/Admin-Panel-" + getPluginVersion() + ".jar");
            File newPluginFile = new File("plugins/Admin-Panel-" + getLatestVersionName() + ".jar");
            if (!new File(plugin.getDataFolder() + "/downloaded-update").exists()) {
                new File(plugin.getDataFolder() + "/downloaded-update").mkdir();
            }
            if (!downloadPath.exists()) {
                downloadPath.createNewFile();
            }
            URL downloadURL = new URL("https://api.spiget.org/v2/resources/" + resourceID + "/download");
            FileUtils.copyURLToFile(downloadURL, downloadPath);
            downloadPath.renameTo(new File(plugin.getDataFolder() + "/downloaded-update/Admin-Panel-" + getLatestVersionName() + ".jar"));
            downloadPath = new File(plugin.getDataFolder() + "/downloaded-update/Admin-Panel-" + getLatestVersionName() + ".jar");
            new File(plugin.getDataFolder() + "/downloaded-update/" + resourceID + ".jar").delete();
            if ((plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") || force) && !replace) {
                if (logInConsole) {
                    plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', "&aThe new version was downloaded automatically and is located in the update folder!"));
                    plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', "&aThe Update is now available: &c" + downloadPath));
                }
                plugin.getFileLogger().writeToLog(Level.INFO, "New Version (" + getLatestVersionName() + ") got downloaded into the Update Folder! (Plugin Version: " + getPluginVersion() + ")", "Updater");
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
                    e.printStackTrace();
                }
                pluginUtils.load(newPluginFile);
                Bukkit.getPluginManager().enablePlugin(pluginUtils.getPluginByName("Admin-Panel"));
                if (logInConsole) {
                    plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&',
                            "&aThe new version was downloaded automatically and the old version was automatically replaced! \n" +
                                    "&aAnd The New Version started automatically! If you can please check Console for Errors!"));
                }
                plugin.getFileLogger().writeToLog(Level.INFO, "New Version (" + getLatestVersionName() + ") got downloaded and replaced with the Plugin Version (" + getPluginVersion() + ")!", "Updater");
            }
        } catch (Exception e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e, "Updater");
            e.printStackTrace();
        }
    }

    public String getPluginVersion() {
        plugin.getFileLogger().writeToLog(Level.INFO, "Requested Plugin Version -> " + plugin.getDescription().getVersion(), "Updater");
        return plugin.getDescription().getVersion();
    }

    public int getResourceID() {
        return resourceID;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("adminpanel.updatenotify")) {
            JSONObject jsonObject = getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID + "/updates/latest");
            try {
                String descriptionEncoded = jsonObject.getString("description");
                String descriptionDecoded = html2text(new String(Base64.getDecoder().decode(descriptionEncoded)));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        " &cPlugin outdated! &6Please download the new version on&r\n" +
                                "&6https://www.spigotmc.org/resources/servermanager-an-mc-integrated-admin-panel-german.91800/\n" +
                                "&6or just activate automatic updating and replacing in the Config!\n" +
                                "&bCurrent Version: &c" + getPluginVersion() + "&r\n" +
                                "&bNew Version: &c" + getLatestVersionName() + "&r\n" +
                                "&bNew Version ID: &c" + getLatestVersionID() + "&r\n" +
                                "&bNew Version Title: &c" + jsonObject.getString("title") + "&r\n" +
                                "&bNew Version Description: &c" + descriptionDecoded));
                if (plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") &&
                        !plugin.getConfig().getBoolean("Plugin.Updater.automaticReplace")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThe new version was downloaded automatically and is located in the update folder!"));
                }
                if (plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") &&
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
