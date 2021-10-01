package de.happybavarian07.events.player;

import de.happybavarian07.events.AdminPanelEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class SelectPlayerEvent extends AdminPanelEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final UUID selectedPlayer;

    public SelectPlayerEvent(Player player, UUID selectedPlayer) {
        this.player = player;
        this.selectedPlayer = selectedPlayer;
    }

    public UUID getSelectedPlayer() {
        return selectedPlayer;
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
