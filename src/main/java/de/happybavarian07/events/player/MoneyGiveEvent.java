package de.happybavarian07.events.player;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MoneyGiveEvent extends Event implements Cancellable {
    private final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final OfflinePlayer target;
    private double amount;

    public MoneyGiveEvent(Player player, OfflinePlayer target, Double amount) {
        this.player = player;
        this.target = target;
        this.amount = amount;
    }

    public OfflinePlayer getTarget() {
        return target;
    }

    public Player getPlayer() {
        return player;
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
