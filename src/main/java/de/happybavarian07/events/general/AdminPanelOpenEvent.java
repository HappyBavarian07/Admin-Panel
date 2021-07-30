package de.happybavarian07.events.general;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class AdminPanelOpenEvent extends Event implements Cancellable {

    private final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private String message;
    private Sound openingSound;

    public AdminPanelOpenEvent(Player player, String message, Sound openingSound) {
        this.player = player;
        this.message = message;
        this.openingSound = openingSound;
    }

    public Player getPlayer() {
        return player;
    }

    public Sound getOpeningSound() {
        return openingSound;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setOpeningSound(Sound openingSound) {
        this.openingSound = openingSound;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public HandlerList getHandlerList() {
        return handlers;
    }
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
