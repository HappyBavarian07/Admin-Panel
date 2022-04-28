package de.happybavarian07.adminpanel.events.server;

import de.happybavarian07.adminpanel.events.AdminPanelEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.List;

public class KickAllPlayersEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final List<Player> playersToKick;

    public KickAllPlayersEvent(Player player, List<Player> playersToKick) {
        this.player = player;
        this.playersToKick = playersToKick;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Player> getPlayersToKick() {
        return playersToKick;
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
