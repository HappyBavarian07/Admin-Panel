package de.happybavarian07.events.player;

import de.happybavarian07.events.AdminPanelEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class MoneyTakeEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final UUID target;
    private final double amount;
    private final double currentBal;

    public MoneyTakeEvent(Player player, UUID target, double amount, double currentBal) {
        this.player = player;
        this.target = target;
        this.amount = amount;
        this.currentBal = currentBal;
    }

    public UUID getTarget() {
        return target;
    }

    public Player getPlayer() {
        return player;
    }

    public double getCurrentBal() {
        return currentBal;
    }

    public double getAmount() {
        return amount;
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
