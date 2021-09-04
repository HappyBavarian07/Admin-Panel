package de.happybavarian07.events.world;

import de.happybavarian07.events.AdminPanelEvent;
import de.happybavarian07.menusystem.menu.worldmanager.time.Time;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class TimeChangeEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final World world;
    private Time time;

    public TimeChangeEvent(Player player, World world, Time time) {
        this.player = player;
        this.world = world;
        this.time = time;
    }

    public Time getTime() {
        return time;
    }

    public World getWorld() {
        return world;
    }

    public void setTime(Time time) {
        this.time = time;
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

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
