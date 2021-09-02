package de.happybavarian07.utils;

import de.happybavarian07.main.Main;
import io.CodedByYou.spiget.Resource;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
        /*System.out.println("Major: " + major);
        System.out.println("Minor: " + minor);
        System.out.println("Patch: " + patch);
        System.out.println("Versions: " + majorVersions[0] + "|" + majorVersions[1] + " : " +
                                          minorVersions[0] + "|" + minorVersions[1] + " : " +
                                          patchVersions[0] + "|" + patchVersions[1]);*/
        return major || minor || patch;
    }

    public void checkForUpdates() {
        boolean updateAvailable = updateAvailable();
        if (!updateAvailable) {
            plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', Main.getPrefix() + " &a No Update available!"));
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
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.getPrefix() + "&a No Updates Available!"));
    }

    public void sendNoUpdateMessage(ConsoleCommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.getPrefix() + "&a No Updates Available!"));
    }

    public void downloadPlugin(boolean replace) {
        if (!plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate")) return;
        try {
            File downloadPath = new File(plugin.getDataFolder() + "/downladed-update/91800.jar");
            File oldPluginFile = new File("plugins/Admin-Panel-" + getPluginVersion() + ".jar");
            File newPluginFile = new File("plugins/Admin-Panel-" + getLatestVersionName() + ".jar");
            downloadPath.getParentFile().mkdir();
            if (!downloadPath.exists()) {
                downloadPath.createNewFile();
            }
            URL downloadURL = new URL("https://api.spiget.org/v2/resources/91800/download");
            FileUtils.copyURLToFile(downloadURL, downloadPath);
            downloadPath.renameTo(new File(plugin.getDataFolder() + "/downladed-update/Admin-Panel-" + getLatestVersionName() + ".jar"));
            downloadPath = new File(plugin.getDataFolder() + "/downladed-update/Admin-Panel-" + getLatestVersionName() + ".jar");
            new File(plugin.getDataFolder() + "/downladed-update/91800.jar").delete();
            if (plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") && !replace) {
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', "&aThe new version was downloaded automatically and is located in the update folder!"));
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&', "&aThe Update is now available: &c" + downloadPath));
                return;
            }
            if (plugin.getConfig().getBoolean("Plugin.Updater.downloadPluginUpdate") && replace) {

                try {
                    unload(plugin);
                    oldPluginFile.delete();
                    FileUtils.moveFileToDirectory(downloadPath, plugin.getDataFolder().getParentFile(), false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                load(Bukkit.getPluginManager().getPlugin("Admin-Panel"));
                plugin.getStartUpLogger().message(ChatColor.translateAlternateColorCodes('&',
                        "&aThe new version was downloaded automatically and the old version was automatically replaced! \n" +
                                "&aAnd The New Version started automatically! If you can please check Console for Errors!"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load(Plugin plugin) {
        load(plugin.getName(), plugin.getDescription().getVersion());
    }

    public void load(String name, String version) {

        Plugin target = null;

        File pluginDir = new File("plugins");

        if (!pluginDir.isDirectory()) {
            return;
        }

        File pluginFile = new File(pluginDir, name + "-" + version + ".jar");

        if (!pluginFile.isFile()) {
            for (File f : pluginDir.listFiles()) {
                if (f.getName().endsWith(".jar")) {
                    try {
                        PluginDescriptionFile desc = plugin.getPluginLoader().getPluginDescription(f);
                        if (desc.getName().equalsIgnoreCase(name)) {
                            pluginFile = f;
                            break;
                        }
                    } catch (InvalidDescriptionException e) {
                        return;
                    }
                }
            }
        }

        try {
            target = Bukkit.getPluginManager().loadPlugin(pluginFile);
        } catch (InvalidDescriptionException | InvalidPluginException e) {
            e.printStackTrace();
            return;
        }

        target.onLoad();
        Bukkit.getPluginManager().enablePlugin(target);
    }

    public void unload(Plugin plugin) {

        String name = plugin.getName();

        PluginManager pluginManager = Bukkit.getPluginManager();

        SimpleCommandMap commandMap = null;

        List<Plugin> plugins = null;

        Map<String, Plugin> names = null;
        Map<String, Command> commands = null;
        Map<Event, SortedSet<RegisteredListener>> listeners = null;

        boolean reloadlisteners = true;

        if (pluginManager != null) {
            pluginManager.disablePlugin(plugin);

            try {

                Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
                pluginsField.setAccessible(true);
                plugins = (List<Plugin>) pluginsField.get(pluginManager);

                Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
                lookupNamesField.setAccessible(true);
                names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

                try {
                    Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
                    listenersField.setAccessible(true);
                    listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(pluginManager);
                } catch (Exception e) {
                    reloadlisteners = false;
                }

                Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                commands = (Map<String, Command>) knownCommandsField.get(commandMap);

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        pluginManager.disablePlugin(plugin);

        if (plugins != null && plugins.contains(plugin))
            plugins.remove(plugin);

        if (names != null && names.containsKey(name))
            names.remove(name);

        if (listeners != null && reloadlisteners) {
            for (SortedSet<RegisteredListener> set : listeners.values()) {
                for (Iterator<RegisteredListener> it = set.iterator(); it.hasNext(); ) {
                    RegisteredListener value = it.next();
                    if (value.getPlugin() == plugin) {
                        it.remove();
                    }
                }
            }
        }

        if (commandMap != null) {
            assert commands != null;
            for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Command> entry = it.next();
                if (entry.getValue() instanceof PluginCommand) {
                    PluginCommand c = (PluginCommand) entry.getValue();
                    if (c.getPlugin() == plugin) {
                        c.unregister(commandMap);
                        it.remove();
                    }
                }
            }
        }

        // Attempt to close the classloader to unlock any handles on the plugin's jar file.
        ClassLoader cl = plugin.getClass().getClassLoader();

        if (cl instanceof URLClassLoader) {

            try {

                Field pluginField = cl.getClass().getDeclaredField("plugin");
                pluginField.setAccessible(true);
                pluginField.set(cl, null);

                Field pluginInitField = cl.getClass().getDeclaredField("pluginInit");
                pluginInitField.setAccessible(true);
                pluginInitField.set(cl, null);

            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                ex.printStackTrace();
            }

            try {

                ((URLClassLoader) cl).close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

        // Will not work on processes started with the -XX:+DisableExplicitGC flag, but lets try it anyway.
        // This tries to get around the issue where Windows refuses to unlock jar files that were previously loaded into the JVM.
        System.gc();
    }

    public Resource getResource() {
        return resource;
    }

    public String getPluginVersion() {
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
