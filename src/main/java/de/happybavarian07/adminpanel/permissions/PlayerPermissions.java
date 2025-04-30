package de.happybavarian07.adminpanel.permissions;

import java.util.HashMap;
import java.util.Map;

public class PlayerPermissions {
    private final String playerUUID;
    private final Map<String, Boolean> permissions = new HashMap<>();

    public PlayerPermissions(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public void addPermission(String permission, boolean value) {
        permissions.put(permission, value);
    }
}
