package de.happybavarian07.adminpanel.events.troll;

import de.happybavarian07.adminpanel.events.AdminPanelEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called before a staff member starts or stops a troll of the troll registry. Cancel it to keep the troll as it is.
 */
public class TrollToggleEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final Player target;
    private final String trollId;
    private final boolean starting;

    /**
     * @param player   the staff member, {@code null} when the console acts
     * @param target   the trolled player
     * @param trollId  the id of the troll
     * @param starting {@code true} for a start, {@code false} for a stop
     */
    public TrollToggleEvent(Player player, Player target, String trollId, boolean starting) {
        this.player = player;
        this.target = target;
        this.trollId = trollId;
        this.starting = starting;
    }

    public Player getPlayer() {
        return player;
    }

    public Player getTarget() {
        return target;
    }

    public String getTrollId() {
        return trollId;
    }

    public boolean isStarting() {
        return starting;
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
