package de.happybavarian07.adminpanel.events.plugins;

import de.happybavarian07.adminpanel.events.AdminPanelEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

public class PluginDisableEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final Plugin plugin;

    public PluginDisableEvent(Player player, Plugin plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public Player getPlayer() {
        return player;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;

    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
