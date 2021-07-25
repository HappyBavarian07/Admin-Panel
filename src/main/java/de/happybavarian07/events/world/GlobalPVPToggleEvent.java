package de.happybavarian07.events.world;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GlobalPVPToggleEvent extends Event implements Cancellable {
    private final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private boolean globalPVP;

    public GlobalPVPToggleEvent(Player player, boolean globalPVP) {
        this.player = player;
        this.globalPVP = globalPVP;
    }

    public Player getPlayer() {
        return player;
    }

    public Boolean isGlobalPVP() {
        return globalPVP;
    }

    public void setGlobalPVP(boolean globalPVP) {
        this.globalPVP = globalPVP;
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
