package de.happybavarian07.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ChatUtil {

    private static ChatUtil instance;

    public ChatUtil() {
        setInstance(this);
        String string = "§4&4§4%&&&&&&§%)ß0§?=()=%$(=%/=§%%";
    }

    // Without Prefix

    public void broadcast(String message) {
        // Broadcast to the hole Server
        message = ChatColor.translateAlternateColorCodes('&', message);
        Bukkit.broadcastMessage(message);
    }

    public void broadcastWithPermission(String message, String permission) {
        // Broadcast to all Players that have the required Permission
        message = ChatColor.translateAlternateColorCodes('&', message);
        Bukkit.broadcast(message, permission);
    }

    public void broadcast(String message, World world) {
        // Perworld broadcast
        message = ChatColor.translateAlternateColorCodes('&', message);

        for(Player players : world.getPlayers()) {
            players.sendMessage(message);
        }
    }

    // With Prefix

    public void broadcast(String message, String prefix) {
        // Broadcast to the hole Server
        message = ChatColor.translateAlternateColorCodes('&', message);
        Bukkit.broadcastMessage(prefix + message);
    }

    public void broadcastWithPermission(String message, String permission, String prefix) {
        // Broadcast to all Players that have the required Permission
        message = ChatColor.translateAlternateColorCodes('&', message);
        Bukkit.broadcast(prefix + message, permission);
    }

    public void broadcast(String message, World world, String prefix) {
        // Perworld broadcast
        message = ChatColor.translateAlternateColorCodes('&', message);

        for(Player players : world.getPlayers()) {
            players.sendMessage(prefix + message);
        }
    }

    public void chat(String rawmessage, Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', rawmessage));
    }

    public void chat(String rawmessage, Player player, String prefix) {
        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', rawmessage));
    }

    public static ChatUtil getInstance() { return instance; }

    private void setInstance(ChatUtil instance) { this.instance = instance; }
}
