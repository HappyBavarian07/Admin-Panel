package de.happybavarian07.events.player;

import de.happybavarian07.events.AdminPanelEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class PlayerBanEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final String playerToBan;
    private final String reason;
    private final String sourcename;

    public PlayerBanEvent(Player player, String playerToBan, String reason, String sourcename) {
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

    public String getPlayerToBan() {
        return playerToBan;
    }

    public String getSourcename() {
        return sourcename;
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
