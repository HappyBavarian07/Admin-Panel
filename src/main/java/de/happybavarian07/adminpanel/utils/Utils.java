package de.happybavarian07.adminpanel.utils;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.menusystem.menu.misc.ConfirmationMenu;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.BanList.Type;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.bukkit.Bukkit.getServer;

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
        StringBuilder sbOut = new StringBuilder();

        for (String s : array) {
            if (!s.isEmpty())
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
                if (!sourcename.isEmpty()) {
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

    public static void serverStop(int timeBeforeStop) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&r[&4&lWARNING&r] " + "&c&lThe server will now shut down and all players will be kicked!", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));

        for (int i = 6; i > 0; i--) {
            final int secondsLeft = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                clearChat(100, false, null);
                if (secondsLeft <= 2) {
                    Bukkit.broadcastMessage(Utils.format(null, "&4Serverstop in: &c&l" + secondsLeft, AdminPanelMain.getPrefix()));
                } else if (secondsLeft <= 4) {
                    Bukkit.broadcastMessage(Utils.format(null, "&6Serverstop in: &b&l" + secondsLeft, AdminPanelMain.getPrefix()));
                } else {
                    Bukkit.broadcastMessage(Utils.format(null, "&aServerstop in: &a&l" + secondsLeft, AdminPanelMain.getPrefix()));
                }
            }, (long) (6 - i) * 20);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            clearChat(100, false, null);
            Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
            Bukkit.broadcastMessage(Utils.format(null, "&r[&4&lWARNING&r] " + "&c&lServer Stop initiated!", AdminPanelMain.getPrefix()));
            Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        }, 6 * 20);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                p2.kickPlayer(Utils.format(null, "&4&lThe server is now shutting down!", AdminPanelMain.getPrefix()));
            }
            Bukkit.shutdown();
        }, (6 * 20) + timeBeforeStop);
    }

    public static void serverRestart(int timeBeforeRestart) {
        Bukkit.getOnlinePlayers().forEach(Player::closeInventory);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&r[&4&lWARNING&r] " + "&c&lThe server is about to restart!", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (int i = 6; i > 0; i--) {
                final int secondsLeft = i;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    clearChat(100, false, null);
                    if (secondsLeft <= 2) {
                        Bukkit.broadcastMessage(Utils.format(null, "&4Serverrestart in: &c&l" + secondsLeft, AdminPanelMain.getPrefix()));
                    } else if (secondsLeft <= 4) {
                        Bukkit.broadcastMessage(Utils.format(null, "&6Serverrestart in: &b&l" + secondsLeft, AdminPanelMain.getPrefix()));
                    } else {
                        Bukkit.broadcastMessage(Utils.format(null, "&aServerrestart in: &a&l" + secondsLeft, AdminPanelMain.getPrefix()));
                    }
                }, (long) (7 - secondsLeft) * 20);
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                clearChat(100, false, null);
                Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
                Bukkit.broadcastMessage(Utils.format(null, "&r[&4&lWARNING&r] " + "&c&lServer Restart initiated!", AdminPanelMain.getPrefix()));
                Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
            }, 6L * 20);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Bukkit.getOnlinePlayers().forEach(p2 -> p2.kickPlayer(Utils.format(null, "&4&lThe server is now restarting!", AdminPanelMain.getPrefix())));
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "restart");
            }, 6L * 20 + timeBeforeRestart);
        }, 20);
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

    public static List<String> stringToList(String input) {
        List<String> result = new ArrayList<>();
        if (input != null && input.startsWith("[") && input.endsWith("]")) {
            input = input.substring(1, input.length() - 1);
            String[] items = input.split(", ");
            result.addAll(Arrays.asList(items));
        }
        return result;
    }

    public static Properties getProperties(File propertiesFile) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFile));
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load properties file: " + propertiesFile.getName());
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Could not load properties file: " + propertiesFile.getName(), LogPrefix.ERROR);
        }
        return properties;
    }

    public static void saveProperties(Properties properties, File propertiesFile) {
        try {
            properties.store(new FileOutputStream(propertiesFile), null);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save properties file: " + propertiesFile.getName());
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Could not save properties file: " + propertiesFile.getName(), LogPrefix.ERROR);
        }
    }

    public static GameMode getGameMode(String gamemode) {
        return switch (gamemode) {
            case "0", "survival", "SURVIVAL" -> GameMode.SURVIVAL;
            case "1", "creative", "CREATIVE" -> GameMode.CREATIVE;
            case "2", "adventure", "ADVENTURE" -> GameMode.ADVENTURE;
            case "3", "spectator", "SPECTATOR" -> GameMode.SPECTATOR;
            default -> null;
        };
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

    private static void createDisabledItemsSection() {
        FileConfiguration dataConfig = plugin.getDataYML();
        if (!dataConfig.contains("DisabledItems")) dataConfig.set("DisabledItems", new ArrayList<>());
        saveDataConfig();
        dataConfig.contains("DisabledItems");
    }

    private static void saveDataConfig() {
        try {
            plugin.getDataYML().save(new File(plugin.getDataFolder() + "/data.yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml");
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Could not save data.yml", LogPrefix.ERROR);
        }
    }

    public static boolean sendMessageToWebhook(String message) {
        //System.out.println("Message: " + message);
        if (message == null || message.isEmpty()) return false;
        try {
            URL url = new URL("https://discord.com/api/webhooks/1158431051492892763/ZOtRZf0BMDECMw9WQ8vijRhRHXAEYYGD39plafrgxT903gPWOysWfvTbOrQA23vGkhX6");
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
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to send message to Discord webhook", LogPrefix.ERROR);
            plugin.getLogger().severe("Failed to send message to Discord webhook");
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
                if (file.isDirectory()) {
                    // Add if in a dir
                    zipDirIntoZipFile(zos, file);
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

    private static ZipOutputStream zipDirIntoZipFile(ZipOutputStream zos, File directory) throws IOException {
        if(!directory.isDirectory()) return zos;
        File[] filesInDir = directory.listFiles();
        if (filesInDir != null) {
            for (File fileInDir : filesInDir) {
                try (FileInputStream fis = new FileInputStream(fileInDir)) {
                    ZipEntry zipEntry = new ZipEntry(
                            (!fileInDir.getParentFile().getName().equals("Admin-Panel") &&
                                    !fileInDir.getParentFile().getName().equals("plugins") ? fileInDir.getParentFile().getName() + File.separator : "")
                                    + fileInDir.getName());
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
        return zos;
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
            plugin.getLogger().severe("Failed to unzip files: " + e.getMessage());
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to unzip files: " + e.getMessage(), LogPrefix.ERROR);
        }
    }

    /*private void ban(final Player p, final OfflinePlayer target, final String reason, final String sourcename) {
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
    }*/

    public void loadLibraryFolder(String pathToFolder) throws MalformedURLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        File folder = new File(pathToFolder);
        if (!folder.isDirectory() || !folder.exists()) return;
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) return;

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

    public static Menu getMenuByClassName(String className, Player player) {
        String menuPackage = "de.happybavarian07.adminpanel.menusystem.menu";
        String fullClassName = menuPackage + "." + className;

        try {
            Class<?> clazz = Class.forName(fullClassName);
            if (Menu.class.isAssignableFrom(clazz)) {
                return (Menu) clazz.getDeclaredConstructor(PlayerMenuUtility.class).newInstance(AdminPanelMain.getAPI().getPlayerMenuUtility(player));
            } else {
                System.err.println("The class does not extend Menu: " + fullClassName);
                return null; // Or handle it as needed
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + fullClassName);
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Class not found: " + fullClassName, LogPrefix.ERROR);
            plugin.getLogger().severe("Class not found: " + fullClassName);
            return null; // You can modify the return value based on your needs
        } catch (IllegalAccessException | InstantiationException e) {
            System.err.println("Error creating an instance: " + fullClassName);
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Error creating an instance: " + fullClassName, LogPrefix.ERROR);
            plugin.getLogger().severe("Error creating an instance: " + fullClassName);
            return null; // You can modify the return value based on your needs
        } catch (InvocationTargetException | NoSuchMethodException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Error invoking constructor: " + fullClassName, LogPrefix.ERROR);
            plugin.getLogger().severe("Error invoking constructor: " + fullClassName);
            throw new RuntimeException("Error invoking constructor: " + fullClassName, e);
        }
    }

    public static void openConfirmationMenu(String reason,
                                            String menuToOpenAfter,
                                            Method methodToExecuteAfter,
                                            Object objectToInvokeOn,
                                            List<Object> methodArgs,
                                            List<Class<? extends Exception>> exceptionsToCatch,
                                            Player player) {
        PlayerMenuUtility playerMenuUtility = AdminPanelMain.getAPI().getPlayerMenuUtility(player);
        ConfirmationMenu confirmationMenu = new ConfirmationMenu(playerMenuUtility);
        playerMenuUtility.setData("ConfirmationMenu_MenuToOpenAfter", menuToOpenAfter, true);
        playerMenuUtility.setData("ConfirmationMenu_Reason", reason, true);
        playerMenuUtility.setData("ConfirmationMenu_MethodToExecuteAfter", methodToExecuteAfter, true);
        playerMenuUtility.setData("ConfirmationMenu_ObjectToInvokeMethodOn", objectToInvokeOn, true);
        for(int i = 0; i < methodArgs.size(); i++) {
            playerMenuUtility.setData("ConfirmationMenu_MethodArgs_" + i, methodArgs.get(i), true);
        }
        playerMenuUtility.setData("ConfirmationMenu_ExceptionsToCatch", exceptionsToCatch, true);
        confirmationMenu.open();
    }

    public static void deleteMinecraftWorld(World world) {

        for (Player playerInWorld : world.getPlayers()) {
            playerInWorld.kickPlayer(Utils.chat("The World just got deleted!"));
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.unloadWorld(world, false);
                getServer().getWorlds().remove(world);
                deleteWorldFolder(world.getWorldFolder().getAbsoluteFile());
            }
        }.runTaskLater(plugin, 5);
    }

    public static void deleteWorldFolder(File path) {
        if (path.exists() && path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteWorldFolder(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        path.delete();
    }
    public static String getServerUptime(SimpleDateFormat format) {
        long uptime = System.currentTimeMillis() - plugin.getLastStartTimeMillis();
        return format.format(uptime);
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
