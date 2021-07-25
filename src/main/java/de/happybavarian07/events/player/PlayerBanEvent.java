package de.happybavarian07.events.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerBanEvent extends Event implements Cancellable {
    private final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final Player playerToBan;
    private String reason;
    private String sourcename;

    public PlayerBanEvent(Player player, Player playerToBan, String reason, String sourcename) {
        this.player = player;
        this.playerToBan = playerToBan;
        this.reason = reason;
        this.sourcename = sourcename;
    }

    public Player getPlayer() {
        return player;
    }

    public String getReason() {
        return reason;
    }

    public Player getPlayerToBan() {
        return playerToBan;
    }

    public String getSourcename() {
        return sourcename;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setSourcename(String sourcename) {
        this.sourcename = sourcename;
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
