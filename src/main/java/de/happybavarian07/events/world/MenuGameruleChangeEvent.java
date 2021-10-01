package de.happybavarian07.events.world;

import de.happybavarian07.events.AdminPanelEvent;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class MenuGameruleChangeEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final World world;
    private final String gameRule;
    private final boolean value;

    public MenuGameruleChangeEvent(Player player, World world, String gameRule, boolean value) {
        this.player = player;
        this.world = world;
        this.gameRule = gameRule;
        this.value = value;
    }

    public World getWorld() {
        return world;
    }

    public boolean value() {
        return value;
    }

    public Player getPlayer() {
        return player;
    }

    public String getGameRule() {
        return gameRule;
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
