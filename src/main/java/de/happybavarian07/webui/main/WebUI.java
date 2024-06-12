package de.happybavarian07.webui.main;/*
 * @Author HappyBavarian07
 * @Date 23.12.2023 | 20:59
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.webui.config.ConfigManager;
import de.happybavarian07.webui.main.handlers.WebUIConfigManager;
import de.happybavarian07.webui.main.handlers.WebUIEndpoints;
import spark.Spark;

import java.io.File;
import java.util.*;

public class WebUI {
    private static WebUI instance;
    private final ConfigManager configManager;
    private final WebUIConfigManager webUIConfigManager;
    private final WebUIEndpoints webUIEndpoints;

    public WebUI(int port, AdminPanelMain plugin) {
        instance = this;
        this.webUIConfigManager = new WebUIConfigManager();
        this.configManager = new ConfigManager(new File(plugin.getDataFolder(), "webui_config.yml"));
        this.webUIEndpoints = new WebUIEndpoints(port, plugin, webUIConfigManager);
    }

    public void startWebUI() {
        webUIEndpoints.setupEndpoints();
    }

    public WebUIEndpoints getWebUIEndpoints() {
        return webUIEndpoints;
    }

    public WebUIConfigManager getWebUIConfigManager() {
        return webUIConfigManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public static WebUI getInstance() {
        return instance;
    }

    public boolean validateToken(String jwt, UUID userid) {
        return webUIEndpoints.validateToken(jwt, userid);
    }

    public void stopWebUI() {
        webUIEndpoints.stopWebUI();
    }

    public void restartWebUI() {
        stopWebUI();
        startWebUI();
    }

    public void reloadWebUI() {
        stopWebUI();
        configManager.reload();
        startWebUI();
    }
}
