package de.happybavarian07.webui.main.handlers;/*
 * @Author HappyBavarian07
 * @Date 09.05.2024 | 13:43
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefix;
import de.happybavarian07.webui.main.WebUI;
import de.happybavarian07.webui.utils.JwtUtil;
import de.happybavarian07.webui.utils.Permissions;
import de.happybavarian07.webui.utils.UserAuthUtils;
import de.happybavarian07.webui.websockethandlers.ConsoleLogWebSocketHandler;
import de.happybavarian07.webui.websockethandlers.PlayerListWebSocketHandler;
import spark.Spark;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class WebUIEndpoints {
    private long startTime;
    private final int port;
    private final JwtUtil jwtUtil;
    private final WebUIUtils webUIUtils;
    private final AdminPanelMain plugin;
    private final UserAuthUtils authUtils;
    private final WebUIPageHandler webUIPageHandler;
    private final WebUIConfigManager webUIConfigManager;
    private final WebUIRequestHandler webUIRequestHandler;

    public WebUIEndpoints(int port, AdminPanelMain plugin, WebUIConfigManager webUIConfigManager) {
        this.port = port;
        this.plugin = plugin;
        this.webUIUtils = new WebUIUtils();
        this.webUIPageHandler = new WebUIPageHandler();
        this.webUIConfigManager = webUIConfigManager;

        File secretKeyFile = new File(plugin.getDataFolder(), "webui/config/secret.key");
        jwtUtil = new JwtUtil(webUIConfigManager.generateSecretKey(secretKeyFile));
        File userFile = new File(plugin.getDataFolder(), "webui/config/users.json");
        File roleFile = new File(plugin.getDataFolder(), "webui/config/roles.json");
        File tokenFile = new File(plugin.getDataFolder(), "webui/config/tokens.json");
        authUtils = new UserAuthUtils(userFile, roleFile, tokenFile, jwtUtil);

        this.webUIRequestHandler = new WebUIRequestHandler(plugin, webUIUtils, this);
    }

    public void setupEndpoints() {
        //testUserAuthUtils();
        startTime = System.currentTimeMillis();
        Spark.port(port); // Adjust the port as needed
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, "WebUI starting on Port: " + port, LogPrefix.WEBUI);
        Spark.staticFileLocation("webui/public");

        // Add the Log4J Appender to Root Logger (Moved to AdminPanelMain onLoad Method to preserve more logs)

        // WebSocket Endpoints
        Spark.webSocket("/adminpanel/console/log", ConsoleLogWebSocketHandler.getInstance());
        Spark.webSocket("/adminpanel/players/list", PlayerListWebSocketHandler.getInstance());

        // WebUI Endpoints
        Spark.get("/adminpanel/dashboard", webUIPageHandler::getDashboardPage);
        Spark.get("/adminpanel/console", webUIPageHandler::getConsolePage);
        Spark.get("/adminpanel/players", webUIPageHandler::getPlayersPage);
        Spark.get("/adminpanel/plugins", webUIPageHandler::getPluginsPage);
        Spark.get("/adminpanel/server", webUIPageHandler::getServerPage);
        Spark.get("/adminpanel/settings", webUIPageHandler::getSettingsPage);
        Spark.get("/adminpanel/worlds", webUIPageHandler::getWorldsPage);

        // Login Page
        Spark.get("/adminpanel/login", webUIPageHandler::getLoginPage);

        // API Endpoints
        Spark.get("/adminpanel/secure-api/request", webUIRequestHandler::handleAPIRequest);
        Spark.post("/adminpanel/secure-api/action", webUIRequestHandler::handleAPIAction);
        Spark.post("/adminpanel/api/logout", webUIRequestHandler::handleLogoutRequest);

        Spark.get("/adminpanel/secure-api/check-permission", (req, res) -> {
            // Check if Cookie Token is valid
            String token = req.cookie("adminpanel_webui_jwtToken");
            if (token == null) {
                res.status(401);
                return "Unauthorized";
            }

            if (!authUtils.isValidUserAuthToken(UUID.fromString(jwtUtil.getSubject(token)), token)) {
                res.status(401);
                return "Unauthorized";
            }

            // Get the permission from the query parameters and the roles from the usermanager with the token
            String[] roles = authUtils.getUserRoles(UUID.fromString(jwtUtil.getSubject(token))).toArray(new String[0]);
            //System.out.println("Roles: " + Arrays.toString(roles));
            String permission = req.queryParams("permission");
            //System.out.println("Permission: " + permission);

            // Get the role hierarchy
            List<String> roleHierarchy = authUtils.getRoleManager().getRoleHierarchy();

            // Sort the roles according to the role hierarchy
            Arrays.sort(roles, Comparator.comparingInt(roleHierarchy::indexOf));

            AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                    Level.INFO,
                    String.format(
                            "Permission check for user '%s' (UUID: %s) for '%s'",
                            authUtils.getUser(UUID.fromString(jwtUtil.getSubject(token))).getUsername(),
                            jwtUtil.getSubject(token),
                            permission
                    ),
                    LogPrefix.WEBUI_AUTH
            );

            // Check if any of the roles has the required permission
            for (String role : roles) {
                // Check if Permission exists in Permissions Enum
                if (!Permissions.contains(permission)) {
                    res.status(400);
                    return "Error 400: Bad Request - Invalid permission";
                }
                //System.out.println("Checking role: " + role);
                //System.out.println("Has permission: " + authUtils.getRoleManager().getRole(role).hasPermission(Permissions.getByPermission(permission)));
                if (authUtils.getRoleManager().getRole(role).hasPermission(Objects.requireNonNull(Permissions.getByPermission(permission)))) {
                    return "true";
                }
            }

            return "false";
        });

        // Secure Endpoint
        Spark.get("/adminpanel/token-check", (req, res) -> {

            // Get the token from Body
            String token = req.cookie("adminpanel_webui_jwtToken");
            if (token != null) {
                try {
                    String subject = jwtUtil.getSubject(token);
                    // Perform authentication logic based on the subject
                    if (authUtils.isValidUserAuthToken(UUID.fromString(subject), token)) {
                        res.status(200);
                        return "Token Valid";
                    } else {
                        res.status(401);
                        return "Unauthorized";
                    }
                } catch (Exception e) {
                    res.status(401);
                    return "Unauthorized";
                }
            } else {
                res.status(401);
                return "Unauthorized";
            }
        });

        // Login API Route
        Spark.post("/adminpanel/api/login", webUIRequestHandler::handleLoginRequest);

        // Temp Signup Page + Route for API (that Admin can disable via Config after configuring)

        // Ensure Spark is stopped when the JVM is shut down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Spark.stop();
            long endTime = System.currentTimeMillis();
            long durationMillis = endTime - startTime;
            double durationSecs = durationMillis / 1000.0;

            String durationStr = String.format("%02dh:%02dmin:%02ds",
                    TimeUnit.SECONDS.toHours((long) durationSecs),
                    TimeUnit.SECONDS.toMinutes((long) durationSecs) % TimeUnit.HOURS.toMinutes(1),
                    (long) durationSecs % TimeUnit.MINUTES.toSeconds(1));

            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO,
                    String.format("WebUI stopped after running for %.2f secs (%s).", durationSecs, durationStr),
                    LogPrefix.WEBUI);
        }));
    }

    public static void stopWebUI() {
        try {
            Spark.stop();
            while (true) {
                try {
                    Spark.port();
                    Thread.sleep(500);
                } catch (final IllegalStateException ignored) {
                    break;
                }
            }
        } catch (final Exception ex) {
            // Ignore
        }
    }

    // Validate Token with UserID
    public boolean validateToken(String token, UUID userId) {
        try {
            String subject = jwtUtil.getSubject(token);
            if (!subject.equals(userId.toString())) return false;
            // Perform authentication logic based on the subject
            return authUtils.isValidUserAuthToken(userId, token);
        } catch (Exception e) {
            plugin.getFileLogger().writeToLog(Level.WARNING, "Failed to validate token", LogPrefix.ERROR);
            plugin.getLogger().log(Level.WARNING, "Failed to validate token", e);
            return false;
        }
    }

    public JwtUtil getJwtUtil() {
        return jwtUtil;
    }

    public UserAuthUtils getAuthUtils() {
        return authUtils;
    }
}
