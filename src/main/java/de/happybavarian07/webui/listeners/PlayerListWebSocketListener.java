package de.happybavarian07.webui.listeners;/*
 * @Author HappyBavarian07
 * @Date 27.01.2024 | 13:02
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.webui.websockethandlers.PlayerListWebSocketHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class PlayerListWebSocketListener implements Listener {
    private final PlayerListWebSocketHandler handler;
    private final AdminPanelMain plugin;

    public PlayerListWebSocketListener(PlayerListWebSocketHandler handler) {
        this.plugin = AdminPanelMain.getPlugin();
        this.handler = handler;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        handler.updatePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        handler.updatePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        handler.updatePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        handler.updatePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        handler.updatePlayer(event.getPlayer().getUniqueId());
    }
}
