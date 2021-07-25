package de.happybavarian07.events.server;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class KickAllPlayersEvent extends Event implements Cancellable {
    private final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final List<Player> kickedPlayers;

    public KickAllPlayersEvent(Player player, List<Player> kickedPlayers) {
        this.player = player;
        this.kickedPlayers = kickedPlayers;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Player> getKickedPlayers() {
        return kickedPlayers;
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
