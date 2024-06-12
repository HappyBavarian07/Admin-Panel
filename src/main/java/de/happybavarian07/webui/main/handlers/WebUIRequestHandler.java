package de.happybavarian07.webui.main.handlers;/*
 * @Author HappyBavarian07
 * @Date 09.05.2024 | 13:27
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.ChatUtil;
import de.happybavarian07.adminpanel.utils.LogPrefix;
import de.happybavarian07.adminpanel.utils.Utils;
import de.happybavarian07.webui.utils.SaltedChallengeResponseAuthenticator;
import de.happybavarian07.webui.utils.Permissions;
import de.happybavarian07.webui.utils.User;
import io.jsonwebtoken.SignatureException;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

public class WebUIRequestHandler {
    private final AdminPanelMain plugin;
    private final WebUIUtils webUIUtils;
    private final WebUIEndpoints webUIEndpoints;
    private final SaltedChallengeResponseAuthenticator scram;

    public WebUIRequestHandler(AdminPanelMain plugin, WebUIUtils webUIUtils, WebUIEndpoints webUIEndpoints) {
        this.plugin = plugin;
        this.webUIUtils = webUIUtils;
        this.webUIEndpoints = webUIEndpoints;
        this.scram = new SaltedChallengeResponseAuthenticator();
    }

    public Object handleAPIRequest(Request request, Response response) {
        String infoToGet = request.queryParams("info");
        String token = request.cookie("adminpanel_webui_jwtToken");

        if (infoToGet == null) {
            response.status(401); // Unauthorized
            return "Error 401: Unauthorized - Missing 'info' parameter";
        }

        if (token == null) {
            response.status(401); // Unauthorized
            return "Error 401: Unauthorized - Missing 'token' cookie";
        }

        if (!webUIEndpoints.getAuthUtils().isValidUserAuthToken(UUID.fromString(webUIEndpoints.getJwtUtil().getSubject(token)), token)) {
            response.status(401); // Unauthorized
            return "Error 401: Unauthorized - Invalid token";
        }

        AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                Level.INFO,
                String.format(
                        "API Request from %s (Token: %s) for '%s'",
                        request.ip(),
                        token,
                        infoToGet
                ),
                LogPrefix.WEBUI_API
        );

        try {
            // Continue with your API logic here based on 'infoToGet'
            // Check if User has permission before returning data
            switch (infoToGet) {
                case "UserInfo": {
                    if (!webUIEndpoints.getAuthUtils().userHasPermission(UUID.fromString(webUIEndpoints.getJwtUtil().getSubject(token)), Permissions.WB_USERS_INFO)) {
                        response.status(403); // Forbidden
                        return "Forbidden - You do not have permission to view this data";
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("username", webUIEndpoints.getAuthUtils().getUser(UUID.fromString(webUIEndpoints.getJwtUtil().getSubject(token))).getUsername());
                    jsonObject.put("roles", webUIEndpoints.getAuthUtils().getUser(UUID.fromString(webUIEndpoints.getJwtUtil().getSubject(token))).getRoles());
                    jsonObject.put("userID", webUIEndpoints.getAuthUtils().getUser(UUID.fromString(webUIEndpoints.getJwtUtil().getSubject(token))).getUUID());
                    jsonObject.put("token", token);

                    response.status(200); // OK

                    response.type("application/json");
                    return jsonObject.toString();
                }
                case "WorldsArrayList": {
                    JSONObject jsonObject = new JSONObject();
                    List<String> worldNameList = new ArrayList<>();
                    for (int i = 0; i < Bukkit.getServer().getWorlds().size(); i++) {
                        worldNameList.add(Bukkit.getServer().getWorlds().get(i).getName());
                    }
                    jsonObject.put("worldsArray", worldNameList);

                    response.status(200); // OK

                    response.type("application/json");
                    return jsonObject.toString();
                }
                case "generalInfos": {
                    if (!webUIEndpoints.getAuthUtils().userHasPermission(UUID.fromString(webUIEndpoints.getJwtUtil().getSubject(token)), Permissions.SCP_SERVERINFO)) {
                        response.status(403); // Forbidden
                        return "Forbidden - You do not have permission to view this data";
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("serverName", Bukkit.getServer().getName());
                    jsonObject.put("serverVersion", Bukkit.getServer().getVersion());
                    jsonObject.put("serverIP", Bukkit.getServer().getIp().isEmpty() ? "localhost" : Bukkit.getServer().getIp());
                    jsonObject.put("serverPort", Bukkit.getServer().getPort());
                    jsonObject.put("serverMaxPlayers", Bukkit.getServer().getMaxPlayers());
                    jsonObject.put("serverOnlinePlayers", Bukkit.getServer().getOnlinePlayers().size());
                    jsonObject.put("serverWhitelist", Bukkit.getServer().hasWhitelist());
                    jsonObject.put("serverTPS", plugin.getTpsMeter().getTPS());
                    double memUsed = (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
                    double memMax = (double) Runtime.getRuntime().maxMemory() / 1048576;
                    jsonObject.put("memUsed", Math.round(memUsed));
                    jsonObject.put("memMax", Math.round(memMax));
                    jsonObject.put("memFree", Math.round(memMax - memUsed));
                    jsonObject.put("memPercentageFree", Math.round((100 / memMax) * (memMax - memUsed)));
                    jsonObject.put("memPercentageUsed", Math.round((100 / memMax) * memUsed));
                    jsonObject.put("serverUptime", Utils.getServerUptime(new SimpleDateFormat("HH:mm:ss")));

                    response.status(200); // OK

                    response.type("application/json");
                    return jsonObject.toString();
                }
                case "playerList": {
                    if (!webUIEndpoints.getAuthUtils().userHasPermission(UUID.fromString(webUIEndpoints.getJwtUtil().getSubject(token)), Permissions.PCP_PLAYER_LIST)) {
                        response.status(403); // Forbidden
                        return "Forbidden - You do not have permission to view this data";
                    }
                    /* Example Format that Client expects:
                    // This is just an example.
                    let players = [
                        [0, {username: 'If You', status: 'BANNED', op: true, uuid: '6e840d3c-b3b3-42a3-b91a-cdf5984b816e'}],
                        [1, {username: 'See This', status: 'BANNED', op: false, uuid: '5df2f13d-b2c9-41b2-a69f-1a5c8cc03a50'}],
                        [2, {username: 'That Means', status: 'BANNED', op: true, uuid: '8b8279a1-2e0a-447e-b82f-6e53f433e8e2'}],
                        [3, {username: 'Something Went', status: 'BANNED', op: false, uuid: 'ada47a29-2731-4c3e-8cde-43e9df15a426'}],
                        [4, {username: 'terribly wrong', status: 'BANNED', op: true, uuid: '67953f98-c266-4373-9e5d-089861702f8e'}],
                        [5, {username: 'while loading', status: 'BANNED', op: false, uuid: '1dd39fbc-b506-4d9f-be4c-7be2c45217cc'}],
                        [6, {username: 'the players.', status: 'BANNED', op: true, uuid: '5aacf122-2018-4d72-8a8f-733efe3de5de'}]
                    ];
                    */
                    JSONArray jsonObject = new JSONArray();
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
                            put("world", player.isOnline() ? Objects.requireNonNull(player.getPlayer()).getWorld().getName() : "null");
                            put("gamemode", player.isOnline() ? Objects.requireNonNull(player.getPlayer()).getGameMode().toString() : "null");
                            put("ip", player.isOnline() ? Objects.requireNonNull(Objects.requireNonNull(player.getPlayer()).getAddress()).getAddress().toString() : "null");
                        }});
                        jsonObject.put(playerArray);
                        count++;
                    }
                    System.out.println(jsonObject);
                    return jsonObject.toString();
                }
                case "pluginList": {
                    // TODO Add Permission
                    JSONObject jsonObject = new JSONObject();
                    for (int i = 0; i < Bukkit.getServer().getPluginManager().getPlugins().length; i++) {
                        jsonObject.put("plugin" + i, Bukkit.getServer().getPluginManager().getPlugins()[i]);
                    }

                    response.status(200); // OK

                    response.type("application/json");
                    return jsonObject.toString();
                }
                case "serverProperties": {
                    if (!webUIEndpoints.getAuthUtils().userHasPermission(UUID.fromString(webUIEndpoints.getJwtUtil().getSubject(token)), Permissions.SERVER_PROPERTIES)) {
                        response.status(403); // Forbidden
                        return "Forbidden - You do not have permission to view this data";
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("allowEnd", Bukkit.getServer().getAllowEnd());
                    jsonObject.put("allowFlight", Bukkit.getServer().getAllowFlight());
                    jsonObject.put("allowNether", Bukkit.getServer().getAllowNether());
                    jsonObject.put("connectionThrottle", Bukkit.getServer().getConnectionThrottle());
                    jsonObject.put("defaultGamemode", Bukkit.getServer().getDefaultGameMode());
                    jsonObject.put("generateStructures", Bukkit.getServer().getGenerateStructures());
                    jsonObject.put("hardcore", Bukkit.getServer().isHardcore());
                    jsonObject.put("idleTimeout", Bukkit.getServer().getIdleTimeout());
                    jsonObject.put("maxPlayers", Bukkit.getServer().getMaxPlayers());
                    jsonObject.put("maxWorldSize", Bukkit.getServer().getMaxWorldSize());
                    jsonObject.put("monsterSpawnLimit", Bukkit.getServer().getSpawnLimit(SpawnCategory.MONSTER));
                    jsonObject.put("animalSpawnLimit", Bukkit.getServer().getSpawnLimit(SpawnCategory.ANIMAL));
                    jsonObject.put("ambientSpawnLimit", Bukkit.getServer().getSpawnLimit(SpawnCategory.AMBIENT));
                    jsonObject.put("miscSpawnLimit", Bukkit.getServer().getSpawnLimit(SpawnCategory.MISC));
                    jsonObject.put("waterAmbientSpawnLimit", Bukkit.getServer().getSpawnLimit(SpawnCategory.WATER_AMBIENT));
                    jsonObject.put("waterAnimalSpawnLimit", Bukkit.getServer().getSpawnLimit(SpawnCategory.WATER_ANIMAL));
                    jsonObject.put("waterUndergroundSpawnLimit", Bukkit.getServer().getSpawnLimit(SpawnCategory.WATER_UNDERGROUND_CREATURE));
                    jsonObject.put("axolotlSpawnLimit", Bukkit.getServer().getSpawnLimit(SpawnCategory.AXOLOTL));
                    jsonObject.put("motd", Bukkit.getServer().getMotd());
                    jsonObject.put("onlineMode", Bukkit.getServer().getOnlineMode());
                    jsonObject.put("playerIdleTimeout", Bukkit.getServer().getIdleTimeout());

                    response.status(200); // OK

                    response.type("application/json");
                    return jsonObject.toString();
                }
                default:
                    response.status(404); // Not Found

                    return "Error 404: Invalid 'info' parameter: " + infoToGet;
            }
        } catch (JSONException e) {
            return "Error 500: Internal Server Error";
        }
    }

    public Object handleAPIAction(Request request, Response response) {
        String action = request.queryParams("action");
        String data = request.queryParams("data");
        Map<String, String> dataMap = null;
        if (data != null) {
            dataMap = webUIUtils.parseData(URLDecoder.decode(data, StandardCharsets.UTF_8));
        }
        String authToken = request.cookie("adminpanel_webui_jwtToken");
        JSONObject jsonObject = null;

        if (Objects.equals(request.queryParams("jsonBodyWithData"), "true")) {
            try {
                jsonObject = new JSONObject(request.body());
            } catch (JSONException e) {
                response.status(400); // Bad Request
                return "Error 400: Bad Request - Invalid JSON body";
            }
        }

        if (action == null) {
            response.status(400); // Bad Request
            return "Error 400: Bad Request - Missing 'action' or 'data' parameter";
        }

        AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                Level.INFO,
                String.format(
                        "API Action from %s (Token: %s) for '%s'",
                        request.ip(),
                        authToken,
                        action
                ),
                LogPrefix.WEBUI_API
        );

        try {
            // Validate the JWT token
            String userId = webUIEndpoints.getJwtUtil().getSubject(authToken);
            if (!webUIEndpoints.getAuthUtils().isValidUserAuthToken(UUID.fromString(userId), authToken)) {
                response.status(401); // Unauthorized
                return "Error 401: Unauthorized - Invalid token";
            }

            // Continue with your API logic here based on 'action'
            switch (action) {
                case "playerAction":
                    // Sample API logic
                    response.status(200); // OK
                    response.type("text/plain");

                    // Check if the data parameter is present
                    if (jsonObject == null) {
                        response.status(400); // Bad Request
                        return "Error 400: Bad Request - Missing or Incorrect 'data' parameters";
                    }

                    // Get the action from data parameter
                    String playerAction = jsonObject.getString("playerAction");
                    String playerUUID = jsonObject.getString("playerUUID");

                    // Check if the playerAction parameter is present
                    if (playerAction == null) {
                        response.status(400); // Bad Request
                        return "Error 400: Bad Request - Missing or Incorrect 'playerAction' parameter";
                    } else if (playerUUID == null) {
                        response.status(400); // Bad Request
                        return "Error 400: Bad Request - Missing or Incorrect 'playerUUID' parameter";
                    } else {
                        // Perform the action without case sensitivity
                        playerAction = playerAction.toUpperCase().replace(" ", "");

                        Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));

                        switch (playerAction) {
                            case "KICK":
                                // Check if the player is online
                                if (player == null || !player.isOnline()) {
                                    response.status(400); // Bad Request
                                    return "Error 400: Bad Request - Player is not online";
                                } else {
                                    // Perform the action
                                    player.kickPlayer(Utils.format(player, jsonObject.getString("kickMessage"), AdminPanelMain.getPrefix()));
                                    return "Player kicked successfully!";
                                }
                            case "BAN":
                                if (player == null || !player.isOnline()) {
                                    response.status(400); // Bad Request
                                    return "Error 400: Bad Request - Player is not online";
                                }
                                if (player.isBanned()) {
                                    response.status(400); // Bad Request
                                    return "Error 400: Bad Request - Player is banned";
                                }
                                // Perform the action
                                // Get Date from String in JSON date Parameter
                                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jsonObject.getString("banDate"));
                                player.ban(Utils.format(player, jsonObject.getString("banMessage"), AdminPanelMain.getPrefix()), date, "AdminPanel", true);
                                break;
                            case "UNBAN":
                                if (player == null || !player.isOnline()) {
                                    response.status(400); // Bad Request
                                    return "Error 400: Bad Request - Player is not online";
                                }
                                if (!player.isBanned()) {
                                    response.status(400); // Bad Request
                                    return "Error 400: Bad Request - Player is not banned";
                                }
                                // Perform the action
                                Bukkit.getBanList(BanList.Type.PROFILE).pardon(String.valueOf(player.getPlayerProfile()));
                                break;
                            case "MUTE":
                                if (player == null || !player.isOnline()) {
                                    response.status(400); // Bad Request
                                    return "Error 400: Bad Request - Player is not online";
                                }
                                plugin.chatmute.put(player.getUniqueId(), true);
                                break;
                            case "UNMUTE":
                                if (player == null || !player.isOnline()) {
                                    response.status(400); // Bad Request
                                    return "Error 400: Bad Request - Player is not online";
                                }
                                plugin.chatmute.put(player.getUniqueId(), false);
                                break;
                            case "OP":
                                // Check if the player is online
                                if (player == null || !player.isOnline()) {
                                    response.status(400); // Bad Request
                                    return "Error 400: Bad Request - Player is not online";
                                } else {
                                    // Perform the action
                                    player.setOp(true);
                                    return "Player opped successfully!";
                                }
                            case "DEOP":
                                // Check if the player is online
                                if (player == null || !player.isOnline()) {
                                    response.status(400); // Bad Request
                                    return "Error 400: Bad Request - Player is not online";
                                } else {
                                    // Perform the action
                                    player.setOp(false);
                                    return "Player deopped successfully!";
                                }
                            case "GAMEMODE":
                                // Check if the player is online
                                if (player == null || !player.isOnline()) {
                                    response.status(400); // Bad Request
                                    return "Error 400: Bad Request - Player is not online";
                                } else {
                                    // Perform the action
                                    GameMode gameMode = Utils.getGameMode(jsonObject.getString("gamemode"));
                                    if (gameMode == null) {
                                        response.status(400); // Bad Request
                                        return "Error 400: Bad Request - Invalid 'gamemode' parameter";
                                    }
                                    player.setGameMode(gameMode);
                                    return "Player gamemode set successfully!";
                                }
                            case "GIVE":
                                // Check if the itemMap parameter is present and is a valid serialized itemMap json string
                                Map<String, Object> itemMap = webUIUtils.parseItemString(jsonObject.getString("itemMap"));
                                if (itemMap == null) {
                                    response.status(400); // Bad Request
                                    return "Error 400: Bad Request - Missing or Incorrect 'itemMap' parameter";
                                } else {
                                    ItemStack itemStack = ItemStack.deserialize(itemMap);
                                    // Perform the action
                                    if (player != null && player.isOnline()) {
                                        player.getInventory().addItem(itemStack);
                                    } else {
                                        response.status(400); // Bad Request
                                        return "Error 400: Bad Request - Player is not online";
                                    }
                                    return "Item given successfully! Item: " + itemStack.getType();
                                }
                            case "SENDMESSAGE":
                                if (!webUIEndpoints.getAuthUtils().userHasPermission(UUID.fromString(webUIEndpoints.getJwtUtil().getSubject(authToken)), Permissions.PCP_SEND_MESSAGE)) {
                                    response.status(403); // Forbidden
                                    return "Forbidden - You do not have permission to perform this action";
                                }
                                String message = jsonObject.getString("message");
                                if (message == null) {
                                    response.status(400); // Bad Request
                                    return "Error 400: Bad Request - Missing or Incorrect 'message' parameter";
                                } else {
                                    // Perform the action
                                    if (player != null && player.isOnline()) {
                                        player.sendMessage(Utils.format(player, message, AdminPanelMain.getPrefix()));
                                    } else {
                                        response.status(400); // Bad Request
                                        return "Error 400: Bad Request - Player is not online";
                                    }
                                    return "Message sent successfully! Message: " + message;
                                }
                            default:
                                response.status(404); // Not Found

                                return "Error 404: Invalid 'playerAction' parameter: " + playerAction;
                        }
                    }
                case "shutdown":
                    if (!webUIEndpoints.getAuthUtils().userHasPermission(UUID.fromString(webUIEndpoints.getJwtUtil().getSubject(authToken)), Permissions.SCP_SHUTDOWN)) {
                        response.status(403); // Forbidden
                        return "Forbidden - You do not have permission to perform this action";
                    }
                    // Sample API logic
                    response.status(200); // OK


                    // Perform the action
                    Bukkit.getScheduler().runTaskLater(plugin, Bukkit::shutdown, 100);
                    ChatUtil.getInstance().broadcast("&cThe server is shutting down in 5 seconds!");

                    return "Shutdown initiated successfully! Shutting down in 5 seconds";
                case "restart":
                    if (!webUIEndpoints.getAuthUtils().userHasPermission(UUID.fromString(webUIEndpoints.getJwtUtil().getSubject(authToken)), Permissions.SCP_RESTART)) {
                        response.status(403); // Forbidden
                        return "Forbidden - You do not have permission to perform this action";
                    }
                    // Sample API logic
                    response.status(200); // OK


                    // Perform the action
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart"), 100);
                    ChatUtil.getInstance().broadcast("&cThe server is restarting in 5 seconds!");

                    return "Restart initiated successfully! Restarting in 5 seconds";
                case "reload":
                    if (!webUIEndpoints.getAuthUtils().userHasPermission(UUID.fromString(webUIEndpoints.getJwtUtil().getSubject(authToken)), Permissions.SCP_RELOAD)) {
                        response.status(403); // Forbidden
                        return "Forbidden - You do not have permission to perform this action";
                    }
                    // Sample API logic
                    response.status(200); // OK


                    // Perform the action
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reload confirm"), 100);
                    ChatUtil.getInstance().broadcast("&cThe server is reloading in 5 seconds!");
                    return "Reload initiated successfully! Reloading in 5 seconds";
                case "broadcast":
                    if (!webUIEndpoints.getAuthUtils().userHasPermission(UUID.fromString(webUIEndpoints.getJwtUtil().getSubject(authToken)), Permissions.SCP_BROADCAST)) {
                        response.status(403); // Forbidden
                        return "Forbidden - You do not have permission to perform this action";
                    }
                    if (dataMap == null) {
                        response.status(400); // Bad Request
                        return "Error 400: Bad Request - Missing or Incorrect 'data' parameter";
                    }
                    // Sample API logic
                    response.status(200); // OK

                    // Get Message from data parameter that should have the form message:message,<OtherData>
                    String message = Utils.format(null, URLDecoder.decode(dataMap.get("message"), StandardCharsets.UTF_8), AdminPanelMain.getPrefix());
                    if (message == null) {
                        response.status(400); // Bad Request
                        return "Error 400: Bad Request - Missing or Incorrect 'data' parameter";
                    }

                    // Perform the action
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "broadcast " + message));
                    return "Broadcast successful! Broadcasted message: " + message;
                default:
                    response.status(404); // Not Found

                    return "Error 404: Invalid 'action' parameter";
            }
        } catch (SignatureException e) {
            response.status(401); // Unauthorized
            return "Error 401: Unauthorized - Invalid token";
        } catch (JSONException | ParseException e) {
            response.status(400); // Bad Request
            return "Error 400: Bad Request - Invalid JSON body";
        }
    }

    public Object handleLogoutRequest(Request req, Response res) {
        // Get the JWT token from the cookie
        String token = req.cookie("adminpanel_webui_jwtToken");

        // Check if the token is present
        if (token == null) {
            res.status(401); // Unauthorized
            return "Error 401: Unauthorized - Missing 'token' cookie";
        }

        // Validate the JWT token
        String userId = webUIEndpoints.getJwtUtil().getSubject(token);
        if (!webUIEndpoints.getAuthUtils().isValidUserAuthToken(UUID.fromString(userId), token)) {
            res.status(401); // Unauthorized
            return "Error 401: Unauthorized - Invalid token";
        }

        // Invalidate the JWT token
        webUIEndpoints.getAuthUtils().getUserManager().removeUserToken(UUID.fromString(userId));

        // Log the logout
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                Level.INFO,
                String.format(
                        "User '%s' (UUID: %s) logged out",
                        webUIEndpoints.getAuthUtils().getUser(UUID.fromString(userId)).getUsername(),
                        userId
                ),
                LogPrefix.WEBUI_LOGIN
        );

        // Return a success message
        res.status(200); // OK
        return "Successfully logged out";
    }

    public Object handleLoginRequest(Request req, Response res) {
        // TODO Change Login Request to Request Salt first and then Login with Salt hence never sending the actual passwords over the network and just the salt and hashed versions
        // Get the entire body as a string
        String body = req.body();

        // Parse the username and password from the body
        String[] pairs = body.split("&");
        Map<String, String> bodyParams = new HashMap<>();
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            bodyParams.put(keyValue[0], keyValue[1]);
        }

        // Extract the username and password from the body parameters
        String username = URLDecoder.decode(bodyParams.get("username"), StandardCharsets.UTF_8);
        String password;
        if (bodyParams.containsKey("password") && bodyParams.get("password") != null) {
            password = URLDecoder.decode(bodyParams.get("password"), StandardCharsets.UTF_8);
        } else {
            password = null;
        }

        //System.out.println("Login request received");
        //System.out.println("Username: " + username);
        //System.out.println("Password: " + password);

        // Get the UUID of the user
        UUID userId = webUIEndpoints.getAuthUtils().getUserUUID(username);
        //System.out.println("User ID: " + userId);
        if (userId != null) {
            // Get the user
            User user = webUIEndpoints.getAuthUtils().getUserManager().getUser(userId);

            if (user == null) {
                // The user does not exist, return an error message in the response
                res.status(401); // Unauthorized
                AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                        Level.WARNING,
                        String.format(
                                "Failed login attempt for user '%s': User does not exist",
                                username
                        ),
                        LogPrefix.WEBUI_LOGIN
                );
                return "Error: Invalid username or password";
            }

            //System.out.println("User: " + user.getUsername());
            // Check if password got send and if yes then check if it is correct but if not then send salt back to client
            if (password == null || password.isEmpty()) {
                // Send Salt back to client
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("salt", user.getSalt());
                    SaltedChallengeResponseAuthenticator.Challenge c = scram.storeChallenge(scram.generateChallenge(), System.currentTimeMillis(), user);
                    if(c == null) {
                        throw new RuntimeException("Could not store challenge!");
                    }
                    jsonObject.put("challenge", c.getChallenge());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(jsonObject);
                res.status(200); // OK
                res.type("application/json");
                return jsonObject.toString();
            }

            // Hashed Password is here after the salt got send back to the client and the client hashed the password with the salt and send it back. Now we can compare it with the stored hashed password

            // Get the salt and hashed password for the user
            /*String salt = user.getSalt();

            // Hash the provided password with the retrieved salt
            String hashedPassword = webUIEndpoints.getAuthUtils().hashPassword(password, salt);*/

            //System.out.println("Hashed password: " + hashedPassword);
            //System.out.println("Stored password: " + user.getHashedPassword());

            // File Log Login Attempt with IP and Username and UserID
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                    Level.INFO,
                    String.format(
                            "Login attempt for user '%s' (UUID: %s) from %s",
                            username,
                            userId,
                            req.ip()
                    ),
                    LogPrefix.WEBUI_LOGIN
            );

            String challenge = scram.getUserChallenge(user).getChallenge();
            System.out.println("Challenge: " + challenge);
            if(scram.challengeExpired(challenge)) {
                System.out.println("Challenge expired");
                res.status(401); // Unauthorized
                AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                        Level.WARNING,
                        String.format(
                                "Failed login attempt for user '%s': Challenge expired",
                                username
                        ),
                        LogPrefix.WEBUI_LOGIN
                );
                return "Error: Challenge expired";
            }

            if (!scram.isChallengeUsed(challenge, user)) {
                System.out.println("Wrong Challenge");
                res.status(401); // Unauthorized
                AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                        Level.WARNING,
                        String.format(
                                "Failed login attempt for user '%s': Wrong Challenge",
                                username
                        ),
                        LogPrefix.WEBUI_LOGIN
                );
                return "Error: Wrong Challenge";
            }

            // Compare the hashed password with the stored hashed password
            if (scram.verifyResponse(user.getHashedPassword(), password, challenge)) {
                System.out.println("Password correct. Logging in");
            //if (webUIEndpoints.getAuthUtils().isValidUser(userId, password, true)) {
                //System.out.println("Password correct. Logging in");
                // The password is correct, generate a JWT token and return it in the response
                String token = webUIEndpoints.getAuthUtils().getUserManager().getUserToken(userId) == null ?
                        webUIEndpoints.getJwtUtil().generateToken(userId.toString(), username, user.getRoles()) :
                        webUIEndpoints.getAuthUtils().getUserManager().getUserToken(userId);
                res.status(200); // OK
                res.type("text/plain");
                // Set the cookie with the correct attributes
                //res.header("Set-Cookie", "adminpanel_webui_jwtToken=" + token + "; Max-Age=172800; Secure; HttpOnly; SameSite=Strict");
                res.cookie("/adminpanel", "adminpanel_webui_jwtToken", token, 172800, true, false);

                //System.out.println("Token: " + token);
                //System.out.println("Token Valid: " + webUIEndpoints.getAuthUtils().isValidUserAuthToken(userId, token));
                //System.out.println("Token Valid: " + validateToken(token, userId));
                //System.out.println("User Token: " + webUIEndpoints.getAuthUtils().getUserManager().getUserToken(userId));
                if (webUIEndpoints.getAuthUtils().getUserManager().getUserToken(userId) == null) {
                    webUIEndpoints.getAuthUtils().getUserManager().addUserToken(userId, token);
                }

                AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                        Level.INFO,
                        String.format(
                                "User '%s' (UUID: %s) logged in successfully from %s",
                                username,
                                userId,
                                req.ip()
                        ),
                        LogPrefix.WEBUI_LOGIN
                );
                return "Successfully logged in.";
            } else {
                // The password is not correct, return an error message in the response
                res.status(401); // Unauthorized
                AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                        Level.WARNING,
                        String.format(
                                "Failed login attempt for user '%s': Incorrect password",
                                username
                        ),
                        LogPrefix.WEBUI_LOGIN
                );
                return "Error: Invalid username or password";
            }
        } else {
            // The user does not exist, return an error message in the response
            res.status(401); // Unauthorized
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                    Level.WARNING,
                    String.format(
                            "Failed login attempt for user '%s': User does not exist",
                            username
                    ),
                    LogPrefix.WEBUI_LOGIN
            );
            return "Error: Invalid username or password";
        }
    }
}
