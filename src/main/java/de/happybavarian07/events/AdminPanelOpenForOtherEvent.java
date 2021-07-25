package de.happybavarian07.events;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AdminPanelOpenForOtherEvent extends Event implements Cancellable {

    private final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final Player targetPlayer;
    private String messageSelf;
    private String messageOther;

    public AdminPanelOpenForOtherEvent(Player player, Player targetPlayer, String messageSelf, String messageOther) {
        this.player = player;
        this.messageSelf = messageSelf;
        this.messageOther = messageOther;
        this.targetPlayer = targetPlayer;
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }

    public Player getPlayer() {
        return player;
    }

    public String getMessageToPlayer() {
        return messageSelf;
    }

    public String getMessageToTarget() {
        return messageOther;
    }

    public void setMessageToTarget(String messageOther) {
        this.messageOther = messageOther;
    }

    public void setMessageToPlayer(String messageSelf) {
        this.messageSelf = messageSelf;
    }


    @Override
    public HandlerList getHandlers() {
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
