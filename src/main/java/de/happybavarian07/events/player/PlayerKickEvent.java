package de.happybavarian07.events.player;

import de.happybavarian07.events.AdminPanelEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class PlayerKickEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final String target;
    private final String reason;
    private final String source;

    public PlayerKickEvent(Player player, String target, String reason, String source) {
        this.player = player;
        this.target = target;
        this.reason = reason;
        this.source = source;
    }

    public Player getPlayer() {
        return player;
    }

    public String getTarget() {
        return target;
    }

    public String getReason() {
        return reason;
    }

    public String getSource() {
        return source;
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
