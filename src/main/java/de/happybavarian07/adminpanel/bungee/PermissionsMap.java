package de.happybavarian07.adminpanel.bungee;/*
 * @Author HappyBavarian07
 * @Date 17.10.2022 | 15:41
 */

import java.util.Map;

public class PermissionsMap {
    private Map<String, Boolean> permissions;

    public PermissionsMap(Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "PermissionsMap{" +
                "permissions=" + permissions +
                '}';
    }
}
