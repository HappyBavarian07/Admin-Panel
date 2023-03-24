package de.happybavarian07.adminpanel.utils;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Utils {

    private static AdminPanelMain plugin;
    private static Utils instance;

    public Utils() {
        plugin = AdminPanelMain.getPlugin();
        setInstance(this);
    }

    public static String chat(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static ItemStack createItem(Inventory inv, String materialString, int amount, int invSlot, String displayName, String... loreString) {

        ItemStack item;
        List<String> lore = new ArrayList<>();

        item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(materialString)), amount);

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(chat(displayName));
        for (String s : loreString) {
            lore.add(chat(s));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        inv.setItem(invSlot - 1, item);
        return item;
    }

    public static String arrayToString(String[] array) {
        StringBuilder sbOut = new StringBuilder("");
        if (array.length <= 0) return sbOut.toString();

        for (String s : array) {
            if (!s.equals(""))
                sbOut.append(Utils.format(null, s, AdminPanelMain.getPrefix())).append(" ");
        }
        return sbOut.toString();
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createItemByte(Inventory inv, String materialString, int byteId, int amount, int invSlot, String displayName, String... loreString) {

        ItemStack item;
        List<String> lore = new ArrayList<>();

        item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(materialString)), amount, (short) byteId);

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(chat(displayName));
        for (String s : loreString) {
            lore.add(chat(s));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        inv.setItem(invSlot - 1, item);
        return item;
    }

    public static void unban(Player player, OfflinePlayer target) {
        try {
            if (!target.isBanned()) {
                player.sendMessage(plugin.getLanguageManager().getMessage("Player.PlayerManager.BanMenu.NotBanned", player, true));
            } else {

                Bukkit.getBanList(Type.NAME).pardon(Objects.requireNonNull(target.getName()));
                player.sendMessage(plugin.getLanguageManager().getMessage("Player.PlayerManager.BanMenu.SuccessfullyUnbanned", player, true));
            }
        } catch (NullPointerException e) {
            player.sendMessage(plugin.getLanguageManager().getMessage("Player.General.TargetedPlayerIsNull", player, true));
        }
    }

    public static void kick(final Player player, final String target, final String reason, final String sourcename) {
        try {
            Player kickedPlayer = Bukkit.getPlayerExact(target);
            assert kickedPlayer != null;
            if (kickedPlayer.isOnline()) {
                if (!sourcename.equals("")) {
                    kickedPlayer.kickPlayer(format(kickedPlayer, "&cYou got kicked!\n" +
                            "\n" +
                            "&3By: &e" + sourcename + "\n" +
                            "\n" +
                            "&3Reason: &e" + reason + "\n" +
                            "\n" +
                            "&3Please join again!", AdminPanelMain.getPrefix()));
                } else {
                    kickedPlayer.kickPlayer(format(kickedPlayer, "&cYou got kicked!\n" +
                            "\n" +
                            "&3By: &e" + player.getName() + "\n" +
                            "\n" +
                            "&3Reason: &e" + reason + "\n" +
                            "\n" +
                            "&3Please join again!", AdminPanelMain.getPrefix()));
                }
            }
        } catch (NullPointerException e) {
            player.sendMessage(plugin.getLanguageManager().getMessage("Player.General.TargetedPlayerIsNull", player, true));
        }
    }

    public static void serverStop(int time, int time2) throws InterruptedException {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&r[&4&lWARNING&r] " + "&c&lThe server will now shut down and all players will be kicked!", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&aServerstop in: &c&l6", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&aServerstop in: &c&l5", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&6Serverstop in: &c&l4", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&6Serverstop in: &c&l3", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&4Serverstop in: &c&l2", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&4Serverstop in: &c&l1", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&r[&4&lWARNING&r] " + "&c&lServer Stop initiated!", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Thread.sleep(time2);
        for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
            p2.kickPlayer(Utils.format(null, "&4&lThe server is now shuting down!", AdminPanelMain.getPrefix()));
        }
        Bukkit.shutdown();
    }

    public static void serverRestart(int time, int time2) throws InterruptedException {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&r[&4&lWARNING&r] " + "&c&lThe server is about to restart!", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            clearChat(100, false, null);
            Bukkit.broadcastMessage(Utils.format(null, "&aServerrestart in: &c&l6", AdminPanelMain.getPrefix()));
        }, time);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            clearChat(100, false, null);
            Bukkit.broadcastMessage(Utils.format(null, "&aServerrestart in: &c&l5", AdminPanelMain.getPrefix()));
        }, 2L * time);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            clearChat(100, false, null);
            Bukkit.broadcastMessage(Utils.format(null, "&6Serverrestart in: &c&l4", AdminPanelMain.getPrefix()));
        }, 3L * time);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            clearChat(100, false, null);
            Bukkit.broadcastMessage(Utils.format(null, "&6Serverrestart in: &c&l3", AdminPanelMain.getPrefix()));
        }, 4L * time);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            clearChat(100, false, null);
            Bukkit.broadcastMessage(Utils.format(null, "&4Serverrestart in: &c&l2", AdminPanelMain.getPrefix()));
        }, 5L * time);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            clearChat(100, false, null);
            Bukkit.broadcastMessage(Utils.format(null, "&4Serverrestart in: &c&l1", AdminPanelMain.getPrefix()));
        }, 6L * time);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            clearChat(100, false, null);
            Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
            Bukkit.broadcastMessage(Utils.format(null, "&r[&4&lWARNING&r] " + "&c&lServer Restart initiated!", AdminPanelMain.getPrefix()));
            Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        }, 7L * time);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                p2.kickPlayer(Utils.format(null, "&4&lThe server is now restarting!", AdminPanelMain.getPrefix()));
            }
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "restart");
        }, 7L * time + time2);
    }

    public static String format(Player player, String message, String prefix) {
        try {
            return PlaceholderAPI.setPlaceholders(player, ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", prefix)));
        } catch (Exception e) {
            return ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", prefix));
        }
    }

    public static void clearChat(int lines, boolean broadcastChatClear, Player player) {
        if (!broadcastChatClear) {
            for (int i = 0; i <= lines; i++) {
                Bukkit.getServer().broadcastMessage("");
            }
        } else {
            for (int i = 0; i <= lines; i++) {
                Bukkit.getServer().broadcastMessage("");
            }
            Bukkit.getServer().broadcastMessage(plugin.getLanguageManager().getMessage("Player.Chat.Header", player, true));
            Bukkit.getServer().broadcastMessage(plugin.getLanguageManager().getMessage("Player.Chat.Message", player, true));
            Bukkit.getServer().broadcastMessage(plugin.getLanguageManager().getMessage("Player.Chat.Footer", player, true));
        }
    }

    public static Utils getInstance() {
        return instance;
    }

    private void setInstance(Utils instance) {
        Utils.instance = instance;
    }

    public static List<String> emptyList() {
        List<String> list = new ArrayList<>();
        list.add("");
        list.add("");
        return list;
    }

    public static boolean isVanillaItemDisabled(ItemStack stack) {
        FileConfiguration dataConfig = plugin.getDataYML();
        if (stack == null) return false;
        return dataConfig.getStringList("DisabledItems").contains(stack.getType().toString());
    }

    public static List<Material> getDisabledItems() {
        FileConfiguration dataConfig = plugin.getDataYML();
        List<String> materialNames = dataConfig.getStringList("DisabledItems");
        List<Material> material = new ArrayList<>();
        for (String materialString : materialNames) {
            Material value = Material.matchMaterial(materialString);
            if (value == null) continue;
            material.add(value);
        }
        return material;
    }

    public static boolean disableVanillaItem(ItemStack stack) {
        boolean success = false;
        FileConfiguration dataConfig = plugin.getDataYML();
        if (!dataConfig.isList("DisabledItems") || !dataConfig.contains("DisabledItems")) {
            createDisabledItemsSection();
            return false;
        }

        if (!dataConfig.getStringList("DisabledItems").contains(stack.getType().toString())) return false;

        List<String> disabledItems = dataConfig.getStringList("DisabledItems");
        disabledItems.add(stack.getType().toString());
        dataConfig.set("DisabledItems", disabledItems);
        saveDataConfig();

        if (!dataConfig.getStringList("DisabledItems").contains(stack.getType().toString())) success = true;

        return success;
    }

    public static boolean enableVanillaItem(ItemStack stack) {
        boolean success = false;
        FileConfiguration dataConfig = plugin.getDataYML();
        if (!dataConfig.isList("DisabledItems") || !dataConfig.contains("DisabledItems")) {
            createDisabledItemsSection();
            return false;
        }

        List<String> disabledItems = dataConfig.getStringList("DisabledItems");
        disabledItems.remove(stack.getType().toString());
        dataConfig.set("DisabledItems", disabledItems);
        saveDataConfig();

        if (dataConfig.getStringList("DisabledItems").contains(stack.getType().toString())) success = true;

        return success;
    }

    private static boolean createDisabledItemsSection() {
        FileConfiguration dataConfig = plugin.getDataYML();
        if (!dataConfig.contains("DisabledItems")) dataConfig.set("DisabledItems", new ArrayList<>());
        saveDataConfig();
        return !dataConfig.contains("DisabledItems");
    }

    private static void saveDataConfig() {
        try {
            plugin.getDataYML().save(new File(plugin.getDataFolder() + "/data.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean sendMessageToWebhook(String message) {
        //System.out.println("Message: " + message);
        if (message == null || message.isEmpty()) return false;
        try {
            URL url = new URL("https://discord.com/api/webhooks/1068586078627450920/XADdYRfzsgFse7yQe_2zozz0ajFl3ez_NNanjYuC0mfnw1aqZXPWs6TTFjRsgKAVlOUZ");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            JSONObject json = new JSONObject();

            json.put("content", message);
            json.put("username", "Admin-Panel-Report-System");
            connection.getOutputStream().write(json.toString().getBytes());

            int responseCode = connection.getResponseCode();
            if (responseCode != 204) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                throw new IOException("Failed to send message to Discord webhook: " + response);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void zipFiles(File[] files, String zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (File file : files) {
                if (!file.exists()) {
                    continue;
                }
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(
                            (!file.getParentFile().getName().equals("Admin-Panel") &&
                                    !file.getParentFile().getName().equals("plugins") ? file.getParentFile().getName() + File.separator : "")
                                    + file.getName());
                    zos.putNextEntry(zipEntry);
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    public static void unzipFiles(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                //System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ban(final Player p, final OfflinePlayer target, final String reason, final String sourcename) {
        try {
            if (target.isBanned()) {
                p.sendMessage(Utils.format(null, "&cThe Player &a" + target.getName() + "&c is already banned!", AdminPanelMain.getPrefix()));
            } else {
                if (sourcename.equals("")) {
                    Bukkit.getBanList(Type.NAME).addBan(Objects.requireNonNull(target.getName()), reason, null, p.getName());
                    if (target.isOnline()) {
                        ((Player) target).kickPlayer(Utils.format(null, "&cYou got banned from that Server!\n" +
                                "\n" +
                                "&3By: &e" + Objects.requireNonNull(Bukkit.getBanList(Type.NAME).getBanEntry(target.getName())).getSource() + "\n" +
                                "\n" +
                                "&3Reason: &e" + Objects.requireNonNull(Bukkit.getBanList(Type.NAME).getBanEntry(target.getName())).getReason() + "\n" +
                                "\n" +
                                "&3Permanently banned!" + "\n", AdminPanelMain.getPrefix()));
                    }
                } else {
                    Bukkit.getBanList(Type.NAME).addBan(Objects.requireNonNull(target.getName()), reason, null, sourcename);
                    if (target.isOnline()) {
                        ((Player) target).kickPlayer(Utils.format(null, "&cYou got banned from that Server!\n" +
                                "\n" +
                                "&3By: &e" + sourcename + "\n" +
                                "\n" +
                                "&3Reason: &e" + Objects.requireNonNull(Bukkit.getBanList(Type.NAME).getBanEntry(target.getName())).getReason() + "\n" +
                                "\n" +
                                "&3Permanently banned!" + "\n", AdminPanelMain.getPrefix()));
                    }
                }
                p.sendMessage(Utils.format(null, "&c&cYou have successfully banned &a" +
                        target.getName() + " &cfor &a" +
                        Objects.requireNonNull(Bukkit.getBanList(Type.NAME).getBanEntry(target.getName())).getReason() + "&c!", AdminPanelMain.getPrefix()));
            }
        } catch (NullPointerException e) {
            p.sendMessage(Utils.format(null, "&cThe Player is not online or doesn't exists!", AdminPanelMain.getPrefix()));
        }
    }

    public void loadLibraryFolder(String pathToFolder) throws MalformedURLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        File folder = new File(pathToFolder);
        if (!folder.isDirectory() || !folder.exists()) return;
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null || listOfFiles.length == 0) return;

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                URL url = file.toURI().toURL();
                URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                Class<URLClassLoader> urlClass = URLClassLoader.class;
                Method method;
                method = urlClass.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(urlClassLoader, url);
            }
        }
    }

    public Economy getEconomy() {
        return plugin.eco;
    }

    public Permission getPermissions() {
        return plugin.perms;
    }

    public Chat getChat() {
        return plugin.chat;
    }
}
