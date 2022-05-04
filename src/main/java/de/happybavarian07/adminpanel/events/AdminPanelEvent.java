package de.happybavarian07.adminpanel.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class AdminPanelEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public AdminPanelEvent() {
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
