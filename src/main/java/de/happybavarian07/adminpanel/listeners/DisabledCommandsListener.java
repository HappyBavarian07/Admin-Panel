package de.happybavarian07.adminpanel.listeners;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;

/**
 * Blocks the commands that were switched off in the plugin command menu. The menu cannot do this itself, a menu that is
 * a listener is unregistered when its inventory closes.
 */
public class DisabledCommandsListener implements Listener {
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        int space = message.indexOf(' ');
        String command = (space < 0 ? message.substring(1) : message.substring(1, space)).toLowerCase(Locale.ROOT);
        if (AdminPanelMain.getPlugin().getDisabledCommands().stream().anyMatch(disabled -> disabled.equalsIgnoreCase(command))) {
            event.setCancelled(true);
        }
    }
}
