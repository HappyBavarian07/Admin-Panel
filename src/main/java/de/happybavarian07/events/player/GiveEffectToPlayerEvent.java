package de.happybavarian07.events.player;

import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GiveEffectToPlayerEvent extends Event implements Cancellable {

    private final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    public GiveEffectToPlayerEvent(Player player, Effect effect) {
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
