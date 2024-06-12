package de.happybavarian07.webui.websockethandlers;/*
 * @Author HappyBavarian07
 * @Date 27.01.2024 | 12:42
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefix;
import de.happybavarian07.webui.listeners.PlayerListWebSocketListener;
import de.happybavarian07.webui.main.WebUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

@WebSocket
public class PlayerListWebSocketHandler {

    private static final int MAX_LOG_LINES = 300;
    private static PlayerListWebSocketHandler instance;
    private final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    private final LinkedHashMap<OfflinePlayer, JSONArray> players = new LinkedHashMap<>();

    public PlayerListWebSocketHandler() {
        instance = this;

        // Populate the players map
        int count = 0;
        for (OfflinePlayer player : Bukkit.getServer().getOfflinePlayers()) {
            JSONArray playerArray = new JSONArray();
            playerArray.put(String.valueOf(count));
            playerArray.put(new HashMap<String, String>() {{
                put("username", player.getName());
                if (player.isBanned()) put("status", "BANNED");
                else if (player.isOnline()) put("status", "ONLINE");
                else put("status", "OFFLINE");
                put("op", String.valueOf(player.isOp()));
                put("uuid", player.getUniqueId().toString());
                put("world", player.isOnline() ? player.getPlayer().getWorld().getName() : "null");
                put("gamemode", player.isOnline() ? player.getPlayer().getGameMode().toString() : "null");
                put("ip", player.isOnline() ? player.getPlayer().getAddress().getAddress().toString() : "null");
            }});
            players.put(player, playerArray);
            count++;
        }

        // Register PlayerListWebSocketListener
        Bukkit.getPluginManager().registerEvents(new PlayerListWebSocketListener(this), AdminPanelMain.getPlugin());

        // Register a Runnable that sends updates of the players map every 2.5 seconds
        Bukkit.getScheduler().runTaskTimerAsynchronously(AdminPanelMain.getPlugin(), () -> {
            for (Session session : sessions) {
                try {
                    session.getRemote().sendString(getPlayersMapAsJSONString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 50L, 50L);
    }

    public static PlayerListWebSocketHandler getInstance() {
        if (instance == null) {
            instance = new PlayerListWebSocketHandler();
        }
        return instance;
    }

    public Map.Entry<OfflinePlayer, JSONArray> getPlayerEntry(UUID uuid) {
        for (Map.Entry<OfflinePlayer, JSONArray> entry : players.entrySet()) {
            if (entry.getKey().getUniqueId().equals(uuid)) {
                return entry;
            }
        }
        return null;
    }

    public void updatePlayer(UUID playerUUID) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);

        JSONArray playerArray = new JSONArray();
        // Get Count from already existing entry
        if (getPlayerEntry(playerUUID) != null) {
            try {
                playerArray.put(getPlayerEntry(playerUUID).getValue().get(0));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            playerArray.put(String.valueOf(players.size()));
        }
        playerArray.put(new HashMap<String, String>() {{
            put("username", player.getName());
            if (player.isBanned()) put("status", "BANNED");
            else if (player.isOnline()) put("status", "ONLINE");
            else put("status", "OFFLINE");
            put("op", String.valueOf(player.isOp()));
            put("uuid", player.getUniqueId().toString());
            put("world", player.isOnline() ? player.getPlayer().getWorld().getName() : "null");
            put("gamemode", player.isOnline() ? player.getPlayer().getGameMode().toString() : "null");
            put("ip", player.isOnline() ? player.getPlayer().getAddress().getAddress().toString() : "null");
        }});
        players.put(player, playerArray);
    }

    public void removePlayer(OfflinePlayer player) {
        players.remove(player);
    }

    public void updatePlayersMap() {
        for (OfflinePlayer player : Bukkit.getServer().getOfflinePlayers()) {
            updatePlayer(player.getUniqueId());
        }
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
            try {
                session.getRemote().sendString(getPlayersMapAsJSONString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            connectionSuccessful = true;
        } else {
            // If the JWT is not valid, close the connection
            session.close(1000, "Unauthorized");
        }
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                Level.INFO,
                String.format(
                        "New PlayerList WebSocket Connection from %s (Session: %s) %s",
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
                        "Error in PlayerList WebSocket (Session: %s): " + error + ": " + error.getMessage(),
                        session.getRemote().getInetSocketAddress().getAddress().getHostAddress()
                ),
                LogPrefix.WEBUI_CONSOLE
        );
        AdminPanelMain.getPlugin().getLogger().log(
                Level.SEVERE,
                String.format(
                        "Error in PlayerList WebSocket (Session: %s): " + error + ": " + error.getMessage(),
                        session.getRemote().getInetSocketAddress().getAddress().getHostAddress()
                ),
                LogPrefix.WEBUI_CONSOLE
        );
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
    }

    public String getPlayersMapAsJSONString() {
        String jsonString;
        if (players.isEmpty()) {
            jsonString = "[]";
        } else {
            JSONArray playersArray = new JSONArray();
            for (Map.Entry<OfflinePlayer, JSONArray> entry : players.entrySet()) {
                playersArray.put(entry.getValue());
            }
            jsonString = playersArray.toString();
        }
        return jsonString;
    }

    @OnWebSocketMessage
    public void message(Session session, String message) {
        // TODO Use this method to handle incoming action requests from the web interface
        String urlDecodedMessage = URLDecoder.decode(message, StandardCharsets.UTF_8);

        AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                Level.INFO,
                String.format(
                        "New PlayerList WebSocket Message from %s (Session: %s): %s",
                        session.getRemote().getInetSocketAddress().getAddress().getHostAddress(),
                        session.getRemote().getInetSocketAddress().getPort(),
                        urlDecodedMessage
                ),
                LogPrefix.WEBUI_CONSOLE
        );

        String jwt = session.getUpgradeRequest().getParameterMap().get("jwt_token").get(0);
        String userid = session.getUpgradeRequest().getParameterMap().get("userid").get(0);

        // Validate the JWT
        if (WebUI.getInstance().validateToken(jwt, UUID.fromString(userid))) {
            if (urlDecodedMessage.equals("UpdatePlayersMapToThisClient")) {
                updatePlayersMap();
                try {
                    session.getRemote().sendString(getPlayersMapAsJSONString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (urlDecodedMessage.startsWith("UpdatePlayer:")) {
                String[] args = urlDecodedMessage.split(":");
                if (args.length == 2) {
                    updatePlayer(UUID.fromString(args[1]));
                    try {
                        session.getRemote().sendString(getPlayerEntry(UUID.fromString(args[1])).getValue().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            // If the JWT is not valid, close the connection
            session.close(1008, "Unauthorized");
        }
    }
}
