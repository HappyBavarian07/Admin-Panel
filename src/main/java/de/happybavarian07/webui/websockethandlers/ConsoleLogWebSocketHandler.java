package de.happybavarian07.webui.websockethandlers;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefix;
import de.happybavarian07.webui.main.WebUI;
import org.bukkit.Bukkit;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

@WebSocket
public class ConsoleLogWebSocketHandler {

    private static final int MAX_LOG_LINES = 1500;
    private static ConsoleLogWebSocketHandler instance;
    private final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    private final LinkedList<String> recentLogLines = new LinkedList<>();


    public ConsoleLogWebSocketHandler() {
        instance = this;
    }

    public static ConsoleLogWebSocketHandler getInstance() {
        if (instance == null) {
            instance = new ConsoleLogWebSocketHandler();
        }
        return instance;
    }

    @OnWebSocketConnect
    public void connected(Session session) {
        // Extract the JWT from the URL
        String jwt = session.getUpgradeRequest().getParameterMap().get("jwt_token").get(0);
        String userid = session.getUpgradeRequest().getParameterMap().get("userid").get(0);
        boolean connectionSuccessful = false;
        // Validate the JWT
        if (WebUI.getInstance().validateToken(jwt, UUID.fromString(userid))) {
            // If the JWT is valid, add the session to the list of connected sessions
            sessions.add(session);

            // Send the recent log lines to the new session
            recentLogLines.forEach(line -> {
                try {
                    session.getRemote().sendString(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            connectionSuccessful = true;
        } else {
            // If the JWT is not valid, close the connection
            session.close(1000, "Unauthorized");
        }
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                Level.INFO,
                String.format(
                        "New ConsoleLog WebSocket Connection from %s (Session: %s) %s",
                        session.getRemote().getInetSocketAddress().getAddress().getHostAddress(),
                        session.getRemote().getInetSocketAddress().getPort(),
                        connectionSuccessful ? "successful" : "unsuccessful"
                ),
                LogPrefix.WEBUI_CONSOLE
        );
    }

    @OnWebSocketError
    public void error(Session session, Throwable error) {
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                Level.SEVERE,
                String.format(
                        "Error in ConsoleLog WebSocket (Session: %s): " + error + ": " + error.getMessage(),
                        session.getRemote().getInetSocketAddress().getAddress().getHostAddress()
                ),
                LogPrefix.WEBUI_CONSOLE
        );
        AdminPanelMain.getPlugin().getLogger().log(
                Level.SEVERE,
                String.format(
                        "Error in ConsoleLog WebSocket (Session: %s): " + error + ": " + error.getMessage(),
                        session.getRemote().getInetSocketAddress().getAddress().getHostAddress()
                ),
                LogPrefix.WEBUI_CONSOLE
        );
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) {
        String urlDecodedMessage = URLDecoder.decode(message, StandardCharsets.UTF_8);

        // If the message starts with a '/', remove it
        if (urlDecodedMessage.startsWith("/")) {
            urlDecodedMessage = urlDecodedMessage.substring(1);
        }

        AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                Level.INFO,
                String.format(
                        "Received following Command from WebUI: '/" + urlDecodedMessage + "' (Session: %s)",
                        session.getRemote().getInetSocketAddress().getAddress().getHostAddress()
                ),
                LogPrefix.WEBUI_CONSOLE
        );
        AdminPanelMain.getPlugin().getLogger().info("Received following Command from WebUI: '/" + urlDecodedMessage + "'");

        String jwt = session.getUpgradeRequest().getParameterMap().get("jwt_token").get(0);
        String userid = session.getUpgradeRequest().getParameterMap().get("userid").get(0);

        // Validate the JWT
        if (WebUI.getInstance().validateToken(jwt, UUID.fromString(userid))) {
            // Process the message as a console command
            // Schedule this task to be run on the main server thread
            String finalMessage = urlDecodedMessage;
            Bukkit.getScheduler().runTask(AdminPanelMain.getPlugin(), () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalMessage));
        } else {
            // If the JWT is not valid, close the connection
            session.close(1008, "Unauthorized");
        }
    }

    public void broadcastMessage(String message) {
        // Add the new message to the recent log lines
        recentLogLines.addLast(message);

        // If there are more than MAX_LOG_LINES in recentLogLines, remove the oldest one
        if (recentLogLines.size() > MAX_LOG_LINES) {
            recentLogLines.removeFirst();
        }
        // Send the message to all connected sessions
        sessions.forEach(session -> {
            try {
                session.getRemote().sendString(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}