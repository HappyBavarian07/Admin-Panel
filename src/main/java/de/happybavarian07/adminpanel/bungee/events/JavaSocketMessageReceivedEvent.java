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

public class JavaSocketMessageReceivedEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Message dataMessage;
    private boolean cancelled;

    public JavaSocketMessageReceivedEvent(Message dataMessage) {
        this.dataMessage = dataMessage;
    }

    /**
     * Returns a copy of the Message Objekt, that was send to the Client
     * @return the copy
     */
    public Message getDataMessage() {
        return dataMessage;
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
