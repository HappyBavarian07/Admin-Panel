package de.happybavarian07.events.world;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MenuGameruleChangeEvent extends Event implements Cancellable {
    private final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private String gameRule;

    public MenuGameruleChangeEvent(Player player, String gameRule) {
        this.player = player;
        this.gameRule = gameRule;
    }

    public Player getPlayer() {
        return player;
    }

    public String getGameRule() {
        return gameRule;
    }

    public void setGameRule(String gameRule) {
        this.gameRule = gameRule;
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
