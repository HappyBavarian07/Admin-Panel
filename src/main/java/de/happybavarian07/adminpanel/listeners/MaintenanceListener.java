package de.happybavarian07.adminpanel.listeners;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.managers.PluginStateManager;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class MaintenanceListener implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("AdminPanel.Bypass.KickInMaintenanceMode") ||
                player.hasPermission("AdminPanel.*") ||
                player.hasPermission("AdminPanel.Bypass.*")) return;
        if (inMaintenance()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                    plugin.getLanguageManager().getMessage("Player.ServerManager.MaintenanceMode", player, true));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerPing(ServerListPingEvent event) {
        if (!inMaintenance()) return;
        LanguageManager lgm = plugin.getLanguageManager();
        event.setMotd(lgm.getMessage("Player.ServerManager.MaintenanceModeMOTD", null, true));
        event.setMaxPlayers(lgm.getCustomObject("Messages.Player.ServerManager.MaintenanceMaxPlayerCount", null, 0, false));
    }

    private boolean inMaintenance() {
        PluginStateManager state = plugin.getPluginStateManager();
        return state != null && state.isInMaintenanceMode();
    }
}
