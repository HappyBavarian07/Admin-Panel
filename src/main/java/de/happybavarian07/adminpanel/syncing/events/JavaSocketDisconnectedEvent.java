package de.happybavarian07.adminpanel.syncing.events;/*
 * @Author HappyBavarian07
 * @Date 25.10.2022 | 18:57
 */

import de.happybavarian07.adminpanel.syncing.DataClient;
import de.happybavarian07.adminpanel.events.AdminPanelEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class JavaSocketDisconnectedEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final boolean serverNotified;

    public JavaSocketDisconnectedEvent(boolean serverNotified) {
        this.serverNotified = serverNotified;
    }

    /**
     * Returns the Boolean if the server is notified about the disconnect
     * @return boolean
     */
    public boolean isServerNotified() {
        return serverNotified;
    }

    /**
     * Gehts the Data Client Instance of the Plugin
     * {AdminPanelMain.getPlugin().getDataClient()}
     * @return Data Client Instance
     */
    public DataClient getDataClient() {
        return AdminPanelMain.getPlugin().getDataClient();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
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
