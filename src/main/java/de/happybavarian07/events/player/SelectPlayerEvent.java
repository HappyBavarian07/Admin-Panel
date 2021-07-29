package de.happybavarian07.events.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SelectPlayerEvent extends Event implements Cancellable {

    private final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final Player selectedPlayer;

    public SelectPlayerEvent(Player player, Player selectedPlayer) {
        this.player = player;
        this.selectedPlayer = selectedPlayer;
    }

    public Player getSelectedPlayer() {
        return selectedPlayer;
    }

    public Player getPlayer() {
        return player;
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
