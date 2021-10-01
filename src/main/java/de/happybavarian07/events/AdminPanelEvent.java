package de.happybavarian07.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class AdminPanelEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public AdminPanelEvent() {
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
