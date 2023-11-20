package de.happybavarian07.adminpanel.syncing.events;/*
 * @Author HappyBavarian07
 * @Date 25.10.2022 | 18:57
 */

import de.happybavarian07.adminpanel.syncing.DataClient;
import de.happybavarian07.adminpanel.syncing.utils.Packet;
import de.happybavarian07.adminpanel.events.AdminPanelEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class JavaSocketPacketSendEvent extends AdminPanelEvent /*implements Cancellable*/ {
    private static final HandlerList handlers = new HandlerList();
    private final Packet dataPacket;
    private boolean cancelled;

    public JavaSocketPacketSendEvent(Packet dataPacket) {
        this.dataPacket = dataPacket;
    }

    /**
     * Returns a copy of the Message Objekt, that was send to the Client
     * @return the copy
     */
    public Packet getDataPacket() {
        return dataPacket;
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

    /*@Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }*/
}
