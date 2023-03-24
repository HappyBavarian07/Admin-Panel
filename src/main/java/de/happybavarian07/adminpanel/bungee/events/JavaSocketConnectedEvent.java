package de.happybavarian07.adminpanel.bungee.events;/*
 * @Author HappyBavarian07
 * @Date 25.10.2022 | 18:57
 */

import de.happybavarian07.adminpanel.bungee.Message;
import de.happybavarian07.adminpanel.bungee.NewDataClient;
import de.happybavarian07.adminpanel.events.AdminPanelEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class JavaSocketConnectedEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final String ipAddress;
    private final int port;
    private final String clientName;

    public JavaSocketConnectedEvent(String ipAddress, int port, String clientName) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.clientName = clientName;
    }

    /**
     * Returns the IP Address of the Server
     * @return ip
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Returns the Port of the Connection
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the current Client Name
     * @return client name
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Gehts the Data Client Instance of the Plugin
     * {AdminPanelMain.getPlugin().getDataClient()}
     * @return Data Client Instance
     */
    public NewDataClient getDataClient() {
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
