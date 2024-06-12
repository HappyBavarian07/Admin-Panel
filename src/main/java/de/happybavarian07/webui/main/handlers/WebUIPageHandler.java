package de.happybavarian07.webui.main.handlers;/*
 * @Author HappyBavarian07
 * @Date 09.05.2024 | 13:27
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import spark.Request;
import spark.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class WebUIPageHandler {
    public String getDashboardPage(Request req, Response res) {
        res.type("text/html");
        return getHTMLPageFromResources("dashboard.html");
    }

    public String getConsolePage(Request req, Response res) {
        res.type("text/html");
        return getHTMLPageFromResources("console/index.html");
    }

    public String getPlayersPage(Request req, Response res) {
        res.type("text/html");
        return getHTMLPageFromResources("players/index.html");
    }

    public String getPluginsPage(Request req, Response res) {
        res.type("text/html");
        return getHTMLPageFromResources("plugins/index.html");
    }

    public String getServerPage(Request req, Response res) {
        res.type("text/html");
        return getHTMLPageFromResources("server/index.html");
    }

    public String getSettingsPage(Request req, Response res) {
        res.type("text/html");
        return getHTMLPageFromResources("settings/index.html");
    }

    public String getWorldsPage(Request req, Response res) {
        res.type("text/html");
        return getHTMLPageFromResources("worlds/index.html");
    }


    public String getLoginPage(Request req, Response res) {
        res.type("text/html");
        return getHTMLPageFromResources("login.html");
    }

    public String getPermissionsScript(Request req, Response res) {
        res.type("application/javascript");
        return getHTMLPageFromResources("permissions.js");
    }

    public String getHTMLPageFromResources(String path) {
        // Check if the file exists in the plugin directory
        File pluginDirectory = new File(AdminPanelMain.getPlugin().getDataFolder(), "webui");
        File htmlFile = new File(pluginDirectory, path);

        if (htmlFile.exists()) {
            // File found in plugin directory, read and return content
            return readHtmlFile(htmlFile.toPath());
        } else {
            // File not found in plugin directory, try resources
            return readHtmlFileFromResources(path, pluginDirectory);
        }
    }

    public String readHtmlFileFromResources(String path, File pluginDirectory) {
        try {
            Path targetPath = pluginDirectory.toPath().resolve(path);

            // Check if the HTML file already exists in the data directory
            //if (!Files.exists(targetPath)) {
            try (InputStream inputStream = AdminPanelMain.getPlugin().getResource("webui/" + path)) {
                if (inputStream != null) {
                    // Copy the HTML file from resources to the data directory
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    return "Error: HTML file not found in resources";
                }
            }
            //}

            // Read and return the content of the HTML file
            return readHtmlFile(targetPath);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Failed to read or create HTML file";
        }
    }

    public String readHtmlFile(Path filePath) {
        // Read the HTML file content
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Failed to read HTML file";
        }
    }
}
