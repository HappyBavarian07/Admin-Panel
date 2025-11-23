package de.happybavarian07.adminpanel.utils;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.service.api.DataService;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private volatile Boolean cachedUpdateAvailable;
    private volatile String cachedLatestVersionName;
    private volatile long lastUpdateCheckMillis;
    private final AtomicBoolean updateInProgress = new AtomicBoolean(false);
    private boolean listenerRegistered;
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;
    private volatile String cachedUpdateMessage;

    public NewUpdater(AdminPanelMain plugin, int resourceID, String fileName, JavaPlugin pluginToUpdate, String linkToFile, boolean bypassExternalURL) {
        this.plugin = plugin;
        this.pluginUtils = new PluginUtils();
        this.resourceID = resourceID;
        this.versionComparator = VersionComparator.SEMANTIC_VERSION;
        this.fileName = fileName;
        this.pluginToUpdate = pluginToUpdate;
        this.linkToFile = linkToFile == null ? "" : linkToFile;
        messages = new Messages();
        this.bypassExternalURL = bypassExternalURL;

        try {
            DataService ds = AdminPanelMain.getPlugin().getDataService();
            if (ds != null) {
                try {
                    String savedLatest = ds.load("updater.cachedLatestVersionName", String.class).join();
                    if (savedLatest != null) cachedLatestVersionName = savedLatest;
                    Boolean savedAvailable = ds.load("updater.cachedUpdateAvailable", Boolean.class).join();
                    if (savedAvailable != null) cachedUpdateAvailable = savedAvailable;
                    Long savedLastCheck = ds.load("updater.lastUpdateCheckMillis", Long.class).join();
                    if (savedLastCheck != null) lastUpdateCheckMillis = savedLastCheck;
                    String savedMessage = ds.load("updater.cachedUpdateMessage", String.class).join();
                    if (savedMessage != null) cachedUpdateMessage = savedMessage;
                } catch (Exception ignored) {
                    // Any failure to load cached values should not block startup
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public boolean resourceIsOnSpigot() {
        JSONObject returnObject = getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID);
        if (returnObject == null) return false;
        return !returnObject.has("error");
    }

    public VersionComparator getVersionComparator() {
        return versionComparator;
    }

    public void setVersionComparator(VersionComparator versionComparator) {
        this.versionComparator = versionComparator;
    }

    public String html2text(String html) {
        String text = html.replaceAll("<.*?>", "");
        text = text.replaceAll("\\s+", " ").trim();
        return text;
    }

    public String getLatestVersionID() {
        try {
            JSONObject obj = getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID + "/versions/latest");
            if (obj == null) return "NoVersionFound";
            String version = String.valueOf(obj.getInt("id"));
            plugin.getFileLogger().writeToLog(Level.INFO, "Requested Latest Version ID for Plugin: " + getPluginName() + " -> " + version, LogPrefixExtension.UPDATER);
            return version;
        } catch (JSONException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to parse latest version id for resource " + resourceID + ": " + e.getMessage(), LogPrefixExtension.UPDATER);
            return "NoVersionFound";
        }
    }

    public String getLatestVersionName() {
        if (cachedLatestVersionName != null) return cachedLatestVersionName;
        try {
            JSONObject obj = getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID + "/versions/latest");
            if (obj == null) return "NoVersionFound";
            String version = String.valueOf(obj.getString("name"));
            cachedLatestVersionName = version;
            try {
                DataService ds = AdminPanelMain.getPlugin().getDataService();
                if (ds != null) ds.save("updater.cachedLatestVersionName", version);
            } catch (Throwable ignored) {
            }
            plugin.getFileLogger().writeToLog(Level.INFO, "Requested Latest Version Name for Plugin: " + getPluginName() + " -> " + version, LogPrefixExtension.UPDATER);
            return version;
        } catch (JSONException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to parse latest version name for resource " + resourceID + ": " + e.getMessage(), LogPrefixExtension.UPDATER);
            return "NoVersionFound";
        }
    }

    public String getPluginName() {
        if (fileName.equals("Admin-Panel-%version%.jar") && resourceID == 91800)
            return plugin.getName();
        if(pluginToUpdate == null) return null;
        return pluginToUpdate.getName();
    }

    public JSONObject getObjectFromWebsite(String url) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            try (InputStream in = conn.getInputStream(); InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(isr)) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                return new JSONObject(sb.toString());
            }
        } catch (Exception e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to fetch JSON from " + url + ": " + e.getMessage(), LogPrefixExtension.UPDATER);
            return null;
        }
    }

    public JSONArray getArrayFromWebsite(String url) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            try (InputStream in = conn.getInputStream(); InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(isr)) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                return new JSONArray(sb.toString());
            }
        } catch (Exception e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to fetch JSON array from " + url + ": " + e.getMessage(), LogPrefixExtension.UPDATER);
            return null;
        }
    }

    public boolean updateAvailable() {
        String spigotVersion = cachedLatestVersionName != null ? cachedLatestVersionName : getLatestVersionName();
        if (spigotVersion == null || spigotVersion.equals("NoVersionFound")) return false;
        String pluginVersion = getPluginVersion();
        boolean available = versionComparator.updateAvailable(pluginVersion, spigotVersion);
        plugin.getFileLogger().writeToLog(Level.WARNING, "Checked for Plugin: " + getPluginName() + " if an Update is available -> " + available, LogPrefixExtension.UPDATER);
        return available;
    }

    public interface UpdateCheckListener {
        void onCheck(boolean updateAvailable, String latestVersion);
    }

    public void checkForUpdatesAsync(boolean logInConsole, UpdateCheckListener listener) {
        if (updateInProgress.get()) return;
        updateInProgress.set(true);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String latest = getLatestVersionName();
            String current = getPluginVersion();
            boolean available = false;
            if (latest != null && !latest.equals("NoVersionFound")) {
                available = versionComparator.updateAvailable(current, latest);
            }
            String messageToCache = null;
            if (available) {
                messageToCache = buildUpdateMessageString(latest);
            }
            String finalMessageToCache = messageToCache;
            boolean finalAvailable = available;
            cachedLatestVersionName = latest;
            cachedUpdateAvailable = available;
            lastUpdateCheckMillis = System.currentTimeMillis();
            try {
                DataService ds = AdminPanelMain.getPlugin().getDataService();
                if (ds != null) {
                    ds.save("updater.cachedLatestVersionName", latest);
                    ds.save("updater.cachedUpdateAvailable", available);
                    ds.save("updater.lastUpdateCheckMillis", lastUpdateCheckMillis);
                    if (finalMessageToCache != null) ds.save("updater.cachedUpdateMessage", finalMessageToCache);
                }
            } catch (Throwable ignored) {
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (finalAvailable) {
                    if (!listenerRegistered) {
                        Bukkit.getPluginManager().registerEvents(this, plugin);
                        listenerRegistered = true;
                    }
                    cachedUpdateMessage = finalMessageToCache;
                    if (logInConsole) {
                        messages.sendUpdateMessage(plugin.getServer().getConsoleSender());
                    }
                } else {
                    if (logInConsole && plugin.getConfig().getBoolean("Plugin.Updater.logNoUpdate")) {
                        plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', AdminPanelMain.getPrefix() + "&a No Update available for Plugin: " + getPluginName()));
                    }
                }
                if (listener != null) listener.onCheck(finalAvailable, latest);
                updateInProgress.set(false);
            });
        });
    }

    private String buildUpdateMessageString(String resolvedLatest) {
        try {
            JSONObject jsonObject = getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID + "/updates/latest");
            JSONObject resourceObject = getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID);
            String descriptionDecoded = null;
            String title = null;
            String fileUrl = "";
            if (jsonObject != null) {
                try {
                    String descriptionEncoded = jsonObject.optString("description", "");
                    descriptionDecoded = html2text(new String(Base64.getDecoder().decode(descriptionEncoded)));
                    title = jsonObject.optString("title", "");
                } catch (Exception ignored) {
                }
            }
            if (resourceObject != null) {
                try {
                    fileUrl = resourceObject.has("file") ? resourceObject.getJSONObject("file").optString("url", "") : "";
                } catch (JSONException ignored) {
                }
            }
            String latestId = getLatestVersionID();
            StringBuilder sb = new StringBuilder();
            sb.append(ChatColor.translateAlternateColorCodes('&',
                    " &cPlugin: " + getPluginName() + " outdated! &6Please download the new version on&r\n" +
                            "&6https://www.spigotmc.org/" + fileUrl + "\n" +
                            "&6or just activate automatic updating and replacing in the Config!\n" +
                            "&bCurrent Version: &c" + getPluginVersion() + "&r\n" +
                            "&bNew Version: &c" + resolvedLatest + "&r\n" +
                            "&bNew Version ID: &c" + latestId + "&r"));
            if (title != null && !title.isEmpty())
                sb.append(ChatColor.translateAlternateColorCodes('&', "\n&bNew Version Title: &c" + title + "&r"));
            if (descriptionDecoded != null && !descriptionDecoded.isEmpty())
                sb.append(ChatColor.translateAlternateColorCodes('&', "\n&bNew Version Description: &c" + descriptionDecoded));
            return sb.toString();
        } catch (Exception e) {
            plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to build cached update message: " + e.getMessage(), LogPrefixExtension.UPDATER);
            return ChatColor.translateAlternateColorCodes('&',
                    " &cPlugin: " + getPluginName() + " outdated!\n" +
                            "&bCurrent Version: &c" + getPluginVersion() + "&r\n" +
                            "&bNew Version: &c" + resolvedLatest);
        }
    }

    public void checkForUpdates(boolean logInConsole) {
        checkForUpdatesAsync(logInConsole, null);
    }

    public UpdateResponse downloadSpecificUpdate(boolean replace, boolean force, boolean logInConsole, String spigotVersion, String versionID) {
        if (!plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") && !force)
            return UpdateResponse.CONFIG_OPTIONS_ARE_NOT_SET_RIGHT;
        File downloadPath = new File(plugin.getDataFolder() + "/downloaded-update/" + resourceID + ".jar");
        File oldPluginFile = new File("plugins/Admin-Panel-" + getPluginVersion() + ".jar");
        File newPluginFile = new File("plugins/Admin-Panel-" + spigotVersion + ".jar");
        File updateDir = new File(plugin.getDataFolder() + "/downloaded-update");
        if (!updateDir.exists()) {
            boolean mk = updateDir.mkdir();
            if (!mk)
                plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to create update directory: " + updateDir, LogPrefixExtension.UPDATER);
        }
        if (!downloadPath.exists()) {
            try {
                boolean created = downloadPath.createNewFile();
                if (!created)
                    plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to create download file: " + downloadPath, LogPrefixExtension.UPDATER);
            } catch (IOException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", LogPrefixExtension.UPDATER);
                return UpdateResponse.ERROR;
            }
        }
        URL downloadURL;
        try {
            downloadURL = new URL("https://api.spiget.org/v2/resources/" + resourceID + "/versions/" + versionID + "/download");
        } catch (Exception e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", LogPrefixExtension.UPDATER);
            return UpdateResponse.SPIGET_ERROR;
        }
        File tempFile = new File(downloadPath.getParentFile(), downloadPath.getName() + ".tmp");
        try {
            FileUtils.copyURLToFile(downloadURL, tempFile);
            if (tempFile.length() <= 0) {
                boolean del = tempFile.delete();
                if (!del)
                    plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to delete empty temp file: " + tempFile, LogPrefixExtension.UPDATER);
                return UpdateResponse.DOWNLOAD_FAIL;
            }
            Files.move(tempFile.toPath(), downloadPath.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", LogPrefixExtension.UPDATER);
            boolean del = tempFile.delete();
            if (!del)
                plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to delete temp file after error: " + tempFile, LogPrefixExtension.UPDATER);
            return UpdateResponse.DOWNLOAD_FAIL;
        }
        File target = new File(plugin.getDataFolder() + "/downloaded-update/Admin-Panel-" + spigotVersion + ".jar");
        try {
            Files.move(downloadPath.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to move downloaded file to target name: " + e.getMessage(), LogPrefixExtension.UPDATER);
            return UpdateResponse.ERROR;
        }
        File residual = new File(plugin.getDataFolder() + "/downloaded-update/" + resourceID + ".jar");
        if (residual.exists()) {
            boolean del = residual.delete();
            if (!del)
                plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to cleanup residual file: " + residual, LogPrefixExtension.UPDATER);
        }
        if ((plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") || force) && !replace) {
            if (logInConsole) {
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', "&aThe new version for Plugin: " + getPluginName() + " was downloaded automatically and is located in the update folder!"));
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', "&aThe Update is now available: &c" + target));
            }
            plugin.getFileLogger().writeToLog(Level.INFO, "New Version for Plugin: " + getPluginName() + " (" + spigotVersion + ") got downloaded into the Update Folder! (Plugin Version: " + getPluginVersion() + ")", LogPrefixExtension.UPDATER);
            return UpdateResponse.UPDATE_SUCCESS;
        } else if ((plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") || force) && replace) {
            try {
                pluginUtils.unload(plugin);
                if (oldPluginFile.exists()) {
                    boolean del = oldPluginFile.delete();
                    if (!del)
                        plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to delete old plugin file: " + oldPluginFile, LogPrefixExtension.UPDATER);
                }
                if (newPluginFile.exists()) {
                    boolean del = newPluginFile.delete();
                    if (!del)
                        plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to delete existing new plugin file: " + newPluginFile, LogPrefixExtension.UPDATER);
                }
                FileUtils.moveFileToDirectory(target, plugin.getDataFolder().getParentFile(), false);
            } catch (IOException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", LogPrefixExtension.UPDATER);
                return UpdateResponse.ERROR;
            }
            try {
                pluginUtils.load(newPluginFile);
            } catch (InvalidPluginException | InvalidDescriptionException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", LogPrefixExtension.UPDATER);
                return UpdateResponse.LOADING_ERROR;
            }
            Bukkit.getPluginManager().enablePlugin(pluginUtils.getPluginByName("Admin-Panel"));
            if (logInConsole) {
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&',
                        "&aThe new version for Plugin: " + getPluginName() + " was downloaded automatically and the old one replaced! \n" +
                                "&aAnd The New Version started automatically! If you can, please check Console for Errors!"));
            }
            plugin.getFileLogger().writeToLog(Level.INFO, "New Version for Plugin: " + getPluginName() + " (" + spigotVersion + ") got downloaded and replaced with the Plugin Version (" + getPluginVersion() + ")!", LogPrefixExtension.UPDATER);
            return UpdateResponse.UPDATE_SUCCESS;
        } else
            return UpdateResponse.ERROR;
    }

    public UpdateResponse downloadLatestUpdate(boolean replace, boolean force, boolean logInConsole) {
        if (!plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") && !force)
            return UpdateResponse.CONFIG_OPTIONS_ARE_NOT_SET_RIGHT;
        File downloadPath = new File(plugin.getDataFolder() + "/downloaded-update/" + resourceID + ".jar");
        File oldPluginFile = new File("plugins/" + fileName.replace("%version%", getPluginVersion()));
        String resolvedLatest = getLatestVersionName();
        File newPluginFile = new File("plugins/" + fileName.replace("%version%", resolvedLatest));
        File updateDir = new File(plugin.getDataFolder() + "/downloaded-update");
        if (!updateDir.exists()) {
            boolean mk = updateDir.mkdir();
            if (!mk)
                plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to create update directory: " + updateDir, LogPrefixExtension.UPDATER);
        }
        if (!downloadPath.exists()) {
            try {
                boolean created = downloadPath.createNewFile();
                if (!created)
                    plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to create download file: " + downloadPath, LogPrefixExtension.UPDATER);
            } catch (IOException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", LogPrefixExtension.UPDATER);
                return UpdateResponse.ERROR;
            }
        }
        URL downloadURL;
        try {
            if (isExternalFile() && !getLinkToFile().isEmpty() && !bypassExternalURL()) {
                downloadURL = new URL(linkToFile.replace("%version%", resolvedLatest));
            } else {
                downloadURL = new URL("https://api.spiget.org/v2/resources/" + resourceID + "/download");
            }
        } catch (Exception e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", LogPrefixExtension.UPDATER);
            return UpdateResponse.SPIGET_ERROR;
        }
        File tempFile = new File(downloadPath.getParentFile(), downloadPath.getName() + ".tmp");
        try {
            FileUtils.copyURLToFile(downloadURL, tempFile);
            if (tempFile.length() <= 0) {
                boolean del = tempFile.delete();
                if (!del)
                    plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to delete empty temp file: " + tempFile, LogPrefixExtension.UPDATER);
                return UpdateResponse.DOWNLOAD_FAIL;
            }
            Files.move(tempFile.toPath(), downloadPath.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", LogPrefixExtension.UPDATER);
            boolean del = tempFile.delete();
            if (!del)
                plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to delete temp file after error: " + tempFile, LogPrefixExtension.UPDATER);
            return UpdateResponse.DOWNLOAD_FAIL;
        }
        File target = new File(plugin.getDataFolder() + "/downloaded-update/" + fileName.replace("%version%", resolvedLatest));
        try {
            Files.move(downloadPath.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to move downloaded file to target name: " + e.getMessage(), LogPrefixExtension.UPDATER);
            return UpdateResponse.ERROR;
        }
        File residual = new File(plugin.getDataFolder() + "/downloaded-update/" + resourceID + ".jar");
        if (residual.exists()) {
            boolean del = residual.delete();
            if (!del)
                plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to cleanup residual file: " + residual, LogPrefixExtension.UPDATER);
        }
        if (force && !replace) {
            if (logInConsole) {
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', "&aThe new version for Plugin: " + getPluginName() + " was downloaded automatically and is located in the update folder!"));
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', "&aThe Update is now available: &c" + target));
            }
            plugin.getFileLogger().writeToLog(Level.INFO, "New Version for Plugin: " + getPluginName() + " (" + resolvedLatest + ") got downloaded into the Update Folder! (Plugin Version: " + getPluginVersion() + ")", LogPrefixExtension.UPDATER);
            return UpdateResponse.UPDATE_SUCCESS;
        } else if (force) {
            try {
                if (getPluginName() != null)
                    pluginUtils.unload(pluginUtils.getPluginByName(getPluginName()));
                if (oldPluginFile.exists()) {
                    boolean del = oldPluginFile.delete();
                    if (!del)
                        plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to delete old plugin file: " + oldPluginFile, LogPrefixExtension.UPDATER);
                }
                if (newPluginFile.exists()) {
                    boolean del = newPluginFile.delete();
                    if (!del)
                        plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to delete existing new plugin file: " + newPluginFile, LogPrefixExtension.UPDATER);
                }
                FileUtils.moveFileToDirectory(target, plugin.getDataFolder().getParentFile(), false);
            } catch (IOException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", LogPrefixExtension.UPDATER);
                return UpdateResponse.ERROR;
            }
            Plugin loadedPlugin;
            try {
                loadedPlugin = pluginUtils.load(newPluginFile);
            } catch (InvalidPluginException | InvalidDescriptionException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", LogPrefixExtension.UPDATER);
                return UpdateResponse.LOADING_ERROR;
            }
            Bukkit.getPluginManager().enablePlugin(loadedPlugin);
            if (logInConsole) {
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&',
                        "&aThe new Version for Plugin: " + getPluginName() + " was downloaded automatically and the old one replaced! \n" +
                                "&aAnd The New Version started automatically! If you can, please check Console for Errors!"));
            }
            plugin.getFileLogger().writeToLog(Level.INFO, "New Version for Plugin: " + getPluginName() + " (" + resolvedLatest + ") got downloaded and replaced with the Plugin Version (" + getPluginVersion() + ")!", LogPrefixExtension.UPDATER);
            return UpdateResponse.UPDATE_SUCCESS;
        } else
            return UpdateResponse.ERROR;
    }

    public void downloadLatestUpdateAsync(boolean replace, boolean force, boolean logInConsole, java.util.function.Consumer<UpdateResponse> callback) {
        if (replace) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                UpdateResponse res = downloadLatestUpdate(true, force, logInConsole);
                if (callback != null) callback.accept(res);
            });
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                UpdateResponse res = downloadLatestUpdate(false, force, logInConsole);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (callback != null) callback.accept(res);
                });
            });
        }
    }

    public VersionComparator.VersionStatus getVersionStatus() {
        String latest = cachedLatestVersionName != null ? cachedLatestVersionName : getLatestVersionName();
        return VersionComparator.evaluateStatus(getPluginVersion(), latest);
    }

    public String getPluginVersion() {
        if (pluginToUpdate == null) {
            return plugin.getDescription().getVersion();
        }
        plugin.getFileLogger().writeToLog(Level.INFO, "Requested Plugin: " + getPluginName() + " Version -> " + pluginUtils.getPluginByName(getPluginName()).getDescription().getVersion(), LogPrefixExtension.UPDATER);
        return pluginUtils.getPluginByName(getPluginName()).getDescription().getVersion();
    }

    public int getResourceID() {
        return resourceID;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("adminpanel.updatenotify")) return;
        if (cachedUpdateAvailable != null && cachedUpdateAvailable) {
            messages.sendUpdateMessage(player);
            if (plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") && !plugin.getConfig().getBoolean("Plugin.Updater.automaticReplace")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThe new version was downloaded automatically and is located in the update folder!"));
            }
            if (plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") && plugin.getConfig().getBoolean("Plugin.Updater.automaticReplace")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThe new version was downloaded automatically and the old version was automatically replaced! \n" +
                        "&aYou just need to reload the plugin via Plugin Manager, / reload, / restart or / stop / start"));
            }
        } else if (cachedUpdateAvailable == null && !updateInProgress.get()) {
            checkForUpdatesAsync(false, (u, v) -> {
            });
        }
    }

    public Messages getMessages() {
        return messages;
    }

    public boolean isExternalFile() {
        JSONObject returnObject = getObjectFromWebsite("https://api.spiget.org/v2/resources/" + resourceID);
        if (returnObject == null) return false;
        try {
            return returnObject.getBoolean("external");
        } catch (JSONException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to parse external flag: " + e.getMessage(), LogPrefixExtension.UPDATER);
            return false;
        }
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
            if (cachedUpdateMessage != null && !cachedUpdateMessage.isEmpty()) {
                sender.sendMessage(cachedUpdateMessage);
                return;
            }
            String latest = cachedLatestVersionName != null ? cachedLatestVersionName : "Unknown";
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    " &cPlugin: " + getPluginName() + " outdated!\n" +
                            "&bCurrent Version: &c" + getPluginVersion() + "&r\n" +
                            "&bNew Version: &c" + latest));
        }

        public void sendNoUpdateMessage(CommandSender sender) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', AdminPanelMain.getPrefix() + "&a No Updates Available for Plugin: " + getPluginName() + "!"));
        }
    }
}
