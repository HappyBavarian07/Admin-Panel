package de.happybavarian07.events.plugins;

import de.happybavarian07.events.AdminPanelEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class PluginInstallEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private int resourceID;
    private String fileName;
    private boolean enableAfterInstall;

    public PluginInstallEvent(Player player, Integer resourceID, String fileName, Boolean enableAfterInstall) {
        this.player = player;
        this.resourceID = resourceID;
        this.fileName = fileName;
        this.enableAfterInstall = enableAfterInstall;
    }

    public Player getPlayer() {
        return player;
    }

    public Integer getPlugin() {
        return resourceID;
    }

    public int getResourceID() {
        return resourceID;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isEnableAfterInstall() {
        return enableAfterInstall;
    }

    public void setEnableAfterInstall(boolean enableAfterInstall) {
        this.enableAfterInstall = enableAfterInstall;
    }

    public void setResourceID(int resourceID) {
        this.resourceID = resourceID;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
