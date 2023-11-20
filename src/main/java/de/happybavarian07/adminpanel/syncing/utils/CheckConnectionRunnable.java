package de.happybavarian07.adminpanel.syncing.utils;/*
 * @Author HappyBavarian07
 * @Date 12.10.2023 | 14:50
 */

import de.happybavarian07.adminpanel.syncing.managers.*;
import de.happybavarian07.adminpanel.utils.StartUpLogger;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class CheckConnectionRunnable extends BukkitRunnable {
    private final PacketHandler packetHandler;
    private final ConnectionHandler connectionHandler;
    private final LoggingManager loggingManager;
    private final StatsManager statsManager;
    private final StartUpLogger pluginLogger;
    private final SettingsManager settingsManager;

    public CheckConnectionRunnable(PacketHandler packetHandler, ConnectionHandler connectionHandler, LoggingManager loggingManager, StatsManager statsManager, StartUpLogger pluginLogger, SettingsManager settingsManager) {
        this.packetHandler = packetHandler;
        this.connectionHandler = connectionHandler;
        this.loggingManager = loggingManager;
        this.statsManager = statsManager;
        this.pluginLogger = pluginLogger;
        this.settingsManager = settingsManager;
    }

    @Override
    public void run() {
        if (settingsManager.isCheckConnection()) {
            return;
        }
        CompletableFuture<Integer> pingResult = packetHandler.pingServer();
        pingResult.thenAccept(statusCode -> {
            if (statusCode != 200) {
                pluginLogger.dataClientMessage(ChatColor.RED, false, true,
                        "Client Connection to Server got interrupted or the Server is not responding and noticed by Check Connection Thread!",
                        "Trying Automatic Reconnect....");
                boolean reconnectResponse;
                try {
                    reconnectResponse = connectionHandler.reconnect(false);
                } catch (IOException e) {
                    // Handle the exception as needed
                    reconnectResponse = false;
                    statsManager.addErrorsThisSession(1);
                    loggingManager.logIntoFile(Level.SEVERE, "Error while reconnecting: " + e + ":" + e.getMessage());
                }
                CompletableFuture<Integer> secondPingResult = packetHandler.pingServer();
                boolean finalReconnectResponse = reconnectResponse;
                secondPingResult.thenAccept(secondStatusCode -> {
                    pluginLogger.dataClientMessage(finalReconnectResponse ? ChatColor.GREEN : ChatColor.RED,
                            finalReconnectResponse ? "Server reconnected. (Server responding? " + (secondStatusCode == 200) + ")"
                                    : "Automatic Reconnect failed.", false, true);
                    loggingManager.logIntoFile(finalReconnectResponse ? Level.WARNING : Level.SEVERE,
                            finalReconnectResponse ? "Server reconnected. (Server responding? " + (secondStatusCode == 200) + ")"
                                    : "Automatic Reconnect failed.");
                });
            }
            loggingManager.logIntoFile(Level.INFO, "Check Connection Thread checked");
        });
    }
}
