package de.happybavarian07.events.plugins;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

public class PluginRestartEvent extends Event implements Cancellable {
    private final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final Plugin plugin;

    public PluginRestartEvent(Player player, Plugin plugin) {
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
    public HandlerList getHandlerList() {
        return handlers;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
