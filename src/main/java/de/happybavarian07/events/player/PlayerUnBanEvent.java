package de.happybavarian07.events.player;

import de.happybavarian07.events.AdminPanelEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class PlayerUnBanEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final Player playerToBan;

    public PlayerUnBanEvent(Player player, Player playerToBan) {
        this.player = player;
        this.playerToBan = playerToBan;
    }

    public Player getPlayer() {
        return player;
    }

    public Player getPlayerToBan() {
        return playerToBan;
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
