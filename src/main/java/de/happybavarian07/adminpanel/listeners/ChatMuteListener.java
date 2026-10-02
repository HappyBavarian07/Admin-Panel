package de.happybavarian07.adminpanel.listeners;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.managers.PluginStateManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatMuteListener implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        PluginStateManager state = plugin.getPluginStateManager();
        Player player = event.getPlayer();
        if (state != null && state.isGlobalChatMuted() && !player.hasPermission("AdminPanel.Bypass.ChatMute")) {
            event.setCancelled(true);
            player.sendMessage(plugin.getLanguageManager().getMessage("Player.ChatMute.PlayerMessage", player, true));
        }
    }
}
