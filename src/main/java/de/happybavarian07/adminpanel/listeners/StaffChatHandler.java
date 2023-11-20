package de.happybavarian07.adminpanel.listeners;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class StaffChatHandler implements Listener {
    private final String prefix = AdminPanelMain.getPlugin().getConfig().getString("StaffChat.Prefix", "&r[&4Staff&d-&8Chat&r]");
    private final List<UUID> staffChatPlayers = new ArrayList<>();
    private final List<UUID> disabledStaffChatPlayers = new ArrayList<>();
    private final String messageFormat = AdminPanelMain.getPlugin().getLanguageManager().getMessage("Player.StaffChat.MessageFormat", null, false);
    private final String chatPrefix = AdminPanelMain.getPlugin().getConfig().getString("StaffChat.ChatPrefix", "#SC ");

    public boolean toggleStaffChatForPlayer(Player player) {
        if (player == null) {
            return false;
        } else if (this.staffChatPlayers.contains(player.getUniqueId())) {
            this.staffChatPlayers.remove(player.getUniqueId());
            return false;
        } else {
            this.staffChatPlayers.add(player.getUniqueId());
            return true;
        }
    }

    public boolean toggleStaffChatForPlayer(UUID player) {
        if (player == null) {
            return false;
        } else if (this.staffChatPlayers.contains(player)) {
            this.staffChatPlayers.remove(player);
            return false;
        } else {
            this.staffChatPlayers.add(player);
            return true;
        }
    }

    public boolean toggleDisableStaffChatForPlayer(Player player) {
        if (player == null) {
            return false;
        } else if (this.disabledStaffChatPlayers.contains(player.getUniqueId())) {
            this.disabledStaffChatPlayers.remove(player.getUniqueId());
            return false;
        } else {
            this.disabledStaffChatPlayers.add(player.getUniqueId());
            return true;
        }
    }

    public boolean toggleDisableStaffChatForPlayer(UUID player) {
        if (player == null) {
            return false;
        } else if (this.disabledStaffChatPlayers.contains(player)) {
            this.disabledStaffChatPlayers.remove(player);
            return false;
        } else {
            this.disabledStaffChatPlayers.add(player);
            return true;
        }
    }

    public boolean isPlayerDisabledStaffChat(UUID uuid) {
        return this.disabledStaffChatPlayers.contains(uuid);
    }

    public boolean isPlayerDisabledStaffChat(Player player) {
        return this.disabledStaffChatPlayers.contains(player.getUniqueId());
    }

    public boolean isPlayerInStaffChat(UUID uuid) {
        return this.staffChatPlayers.contains(uuid);
    }

    public boolean isPlayerInStaffChat(Player player) {
        return this.staffChatPlayers.contains(player.getUniqueId());
    }

    public String getPrefix() {
        return this.prefix;
    }

    public List<UUID> getStaffChatPlayers() {
        return this.staffChatPlayers;
    }

    public void setStaffChatPlayers(List<UUID> uuids) {
        this.staffChatPlayers.clear();
        this.staffChatPlayers.addAll(uuids);
    }

    public void addStaffChatPlayer(UUID uuid) {
        this.staffChatPlayers.add(uuid);
    }

    public void removeStaffChatPlayer(UUID uuid) {
        this.staffChatPlayers.remove(uuid);
    }

    public void clearStaffChatPlayers() {
        this.staffChatPlayers.clear();
    }

    public String formatMessageAsStaffChat(String message, Player player) {
        assert this.prefix != null;
        return ChatColor.translateAlternateColorCodes('&', this.messageFormat.replace("%sc_prefix%", this.prefix).replace("%player_name%", player.getName()).replace("%message%", message));
    }

    public void sendMessageInStaffChat(String s, Player player, boolean format) {
        Bukkit.getOnlinePlayers().stream().filter((onlinePlayer) -> {
            return (this.isPlayerInStaffChat(onlinePlayer.getUniqueId()) || onlinePlayer.hasPermission("AdminPanel.AdminPanelAdminCommands.StaffChat")) && !this.isPlayerDisabledStaffChat(onlinePlayer.getUniqueId());
        }).forEach((onlinePlayer) -> {
            onlinePlayer.sendMessage(format ? this.formatMessageAsStaffChat(s, player) : s);
        });
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        assert this.chatPrefix != null;
        if (event.getMessage().startsWith(this.chatPrefix) && player.hasPermission("AdminPanel.AdminPanelAdminCommands.StaffChat")) {
            event.setCancelled(true);
            String message = event.getMessage().replaceFirst(this.chatPrefix, "");
            this.sendMessageInStaffChat(message, player, true);
        } else {
            if (this.isPlayerInStaffChat(player)) {
                event.setCancelled(true);
                this.sendMessageInStaffChat(event.getMessage(), player, true);
            }

        }
    }
}
    