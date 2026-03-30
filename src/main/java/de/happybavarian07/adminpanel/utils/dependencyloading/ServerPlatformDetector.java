package de.happybavarian07.adminpanel.utils.dependencyloading;

import org.bukkit.Bukkit;

public class ServerPlatformDetector {
    private static Boolean isHybridServer = null;
    private static String platformName = null;

    public static boolean isHybridServer() {
        if (isHybridServer == null) {
            detectPlatform();
        }
        return isHybridServer;
    }

    public static String getPlatformName() {
        if (platformName == null) {
            detectPlatform();
        }
        return platformName;
    }

    private static void detectPlatform() {
        String serverVersion = Bukkit.getVersion();
        String serverName = Bukkit.getName();

        if (serverVersion.contains("Cardboard") || serverName.contains("Cardboard")) {
            isHybridServer = true;
            platformName = "Cardboard";
        } else if (serverVersion.contains("Mohist") || serverName.contains("Mohist")) {
            isHybridServer = true;
            platformName = "Mohist";
        } else if (serverVersion.contains("Banner") || serverName.contains("Banner")) {
            isHybridServer = true;
            platformName = "Banner";
        } else if (serverVersion.contains("Arclight") || serverName.contains("Arclight")) {
            isHybridServer = true;
            platformName = "Arclight";
        } else if (serverVersion.contains("Magma") || serverName.contains("Magma")) {
            isHybridServer = true;
            platformName = "Magma";
        } else if (serverVersion.contains("CatServer") || serverName.contains("CatServer")) {
            isHybridServer = true;
            platformName = "CatServer";
        } else {
            isHybridServer = false;
            platformName = "Spigot/Paper";
        }
    }

    public static boolean requiresAlternativeClassLoading() {
        return isHybridServer();
    }
}

