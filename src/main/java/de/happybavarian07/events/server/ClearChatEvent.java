package de.happybavarian07.events.server;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClearChatEvent extends Event implements Cancellable {
    private final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private int lines;
    private boolean showPlayerName;

    public ClearChatEvent(Player player, int lines, boolean showPlayerName) {
        this.player = player;
        this.lines = lines;
        this.showPlayerName = showPlayerName;
    }

    public Player getPlayer() {
        return player;
    }

    public int getLines() {
        return lines;
    }

    public boolean showPlayerName() {
        return showPlayerName;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    public void setShowPlayerName(boolean showPlayerName) {
        this.showPlayerName = showPlayerName;
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
