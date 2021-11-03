package de.happybavarian07.utils;

import de.happybavarian07.main.AdminPanelMain;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                player.sendMessage(plugin.getLanguageManager().getMessage("Player.PlayerManager.BanMenu.NotBanned", player));
            } else {
                Bukkit.getBanList(Type.NAME).pardon(Objects.requireNonNull(target.getName()));
                player.sendMessage(plugin.getLanguageManager().getMessage("Player.PlayerManager.BanMenu.SuccessfullyUnbanned", player));
            }
        } catch (NullPointerException e) {
            player.sendMessage(plugin.getLanguageManager().getMessage("Player.General.TargetedPlayerIsNull", player));
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
            player.sendMessage(plugin.getLanguageManager().getMessage("Player.General.TargetedPlayerIsNull", player));
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

    public static void serverReload(int time) throws InterruptedException {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&r[&4&lWARNING&r] " + "&c&lThe server is about to reload, please do not move, write in the chat or do something else!", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Thread.sleep(3000);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&aServerreload in: &c&l6", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&aServerreload in: &c&l5", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&6Serverreload in: &c&l4", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&6Serverreload in: &c&l3", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&4Serverreload in: &c&l2", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&4Serverreload in: &c&l1", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "        &r[&4&lAnnouncement&r] " + "&c&lReload started!", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Bukkit.reload();
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "        &r[&4&lAnnouncement&r] " + "&c&lReload finished!", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
    }

    public static void serverRestart(int time) throws InterruptedException {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&r[&4&lWARNING&r] " + "&c&lThe server is about to restart!", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Thread.sleep(3000);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&aServerrestart in: &c&l6", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&aServerrestart in: &c&l5", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&6Serverrestart in: &c&l4", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&6Serverrestart in: &c&l3", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&4Serverrestart in: &c&l2", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&4Serverrestart in: &c&l1", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        clearChat(100, false, null);
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&r[&4&lWARNING&r] " + "&c&lServer Restart initiated!", AdminPanelMain.getPrefix()));
        Bukkit.broadcastMessage(Utils.format(null, "&a+---------------------------------------------------+", AdminPanelMain.getPrefix()));
        Thread.sleep(time);
        for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
            p2.kickPlayer(Utils.format(null, "&4&lThe server is now restarting!", AdminPanelMain.getPrefix()));
        }
    }

    public static String format(Player player, String message, String prefix) {
        return PlaceholderAPI.setPlaceholders(player, ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", prefix)));
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
            Bukkit.getServer().broadcastMessage(plugin.getLanguageManager().getMessage("Player.Chat.Header", player));
            Bukkit.getServer().broadcastMessage(plugin.getLanguageManager().getMessage("Player.Chat.Message", player));
            Bukkit.getServer().broadcastMessage(plugin.getLanguageManager().getMessage("Player.Chat.Footer", player));
        }
    }

    public static Utils getInstance() {
        return instance;
    }

    private void setInstance(Utils instance) {
        Utils.instance = instance;
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
