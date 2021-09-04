package de.happybavarian07.events.world;

import de.happybavarian07.events.AdminPanelEvent;
import de.happybavarian07.menusystem.menu.worldmanager.time.Time;
import de.happybavarian07.menusystem.menu.worldmanager.weather.Weather;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class WeatherChangeEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final World world;
    private Weather weather;

    public WeatherChangeEvent(Player player, World world, Weather weather) {
        this.player = player;
        this.world = world;
        this.weather = weather;
    }

    public Weather getWeather() {
        return weather;
    }

    public World getWorld() {
        return world;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
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
