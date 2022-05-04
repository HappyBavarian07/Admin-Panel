package de.happybavarian07.adminpanel.events.world;

import de.happybavarian07.adminpanel.menusystem.menu.worldmanager.time.Time;
import de.happybavarian07.adminpanel.events.AdminPanelEvent;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
