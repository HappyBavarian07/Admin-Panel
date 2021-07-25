package de.happybavarian07.events.player;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MoneyTakeEvent extends Event implements Cancellable {
    private final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final OfflinePlayer target;
    private double amount;
    private final double currentBal;

    public MoneyTakeEvent(Player player, OfflinePlayer target, double amount, double currentBal) {
        this.player = player;
        this.target = target;
        this.amount = amount;
        this.currentBal = currentBal;
    }

    public OfflinePlayer getTarget() {
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

    public void setAmount(double amount) {
        this.amount = amount;
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
