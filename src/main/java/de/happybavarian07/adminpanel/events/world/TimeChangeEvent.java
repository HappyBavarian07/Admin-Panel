package de.happybavarian07.adminpanel.events.world;

import de.happybavarian07.adminpanel.events.AdminPanelEvent;
import de.happybavarian07.adminpanel.menusystem.menu.worldmanager.time.Time;
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
    private long ticks;

    public TimeChangeEvent(Player player, World world, Time time) {
        this.player = player;
        this.world = world;
        this.time = time;
        this.ticks = time.getTime();
    }

    /**
     * For a time that is not one of the presets. {@link #getTime()} then returns the closest preset, the exact value is
     * {@link #getTicks()}.
     */
    public TimeChangeEvent(Player player, World world, long ticks) {
        this(player, world, Time.closest(ticks));
        this.ticks = ticks;
    }

    /**
     * @return the time of day the world is set to, 0 to 23999
     */
    public long getTicks() {
        return ticks;
    }

    public void setTicks(long ticks) {
        this.ticks = ticks;
    }

    public Time getTime() {
        return time;
    }

    public World getWorld() {
        return world;
    }

    public void setTime(Time time) {
        this.time = time;
        this.ticks = time.getTime();
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
