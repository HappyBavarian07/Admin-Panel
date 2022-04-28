package de.happybavarian07.adminpanel.events.troll;

import de.happybavarian07.adminpanel.events.AdminPanelEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class PlayerKickBecauseErrorEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final Player target;
    private String errorMessage;

    public PlayerKickBecauseErrorEvent(Player player, Player target, String errorMessage) {
        this.player = player;
        this.target = target;
        this.errorMessage = errorMessage;
    }

    public Player getTarget() {
        return target;
    }

    public Player getPlayer() {
        return player;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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
