package de.happybavarian07.webui.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Role {
    private String name;
    private Map<String, Boolean> permissions;

    public Role() {
    }

    public Role(String name, Map<Permissions, Boolean> permissions) {
        this.name = name;
        this.permissions = new HashMap<>();
        permissions.forEach((permission, value) -> this.permissions.put(permission.getPermission(), value));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Permissions, Boolean> getPermissions() {
        Map<Permissions, Boolean> permissionMap = new HashMap<>();
        permissions.forEach((permission, value) -> permissionMap.put(Permissions.getByPermission(permission), value));
        return permissionMap;
    }

    public void setPermissions(Map<Permissions, Boolean> permissions) {
        this.permissions = new HashMap<>();
        permissions.forEach((permission, value) -> this.permissions.put(permission.getPermission(), value));
    }

    public boolean hasPermission(Permissions permission) {
        Boolean hasSpecificPermission = permissions.get(permission.getPermission());
        if (hasSpecificPermission != null) {
            return hasSpecificPermission;
        }

        String category = permission.getPermission().split("\\.")[0] + ".*";
        Boolean hasCategoryPermission = permissions.get(category);
        if (hasCategoryPermission != null) {
            return hasCategoryPermission;
        }

        Boolean hasGlobalPermission = permissions.get(Permissions.EVERYTHING.getPermission());
        return Objects.requireNonNullElse(hasGlobalPermission, false);
    }

    public boolean addPermission(Permissions permission, boolean value) {
        if (permissions.containsKey(permission.getPermission())) {
            return false;
        }
        permissions.put(permission.getPermission(), value);
        return true;
    }

    public boolean removePermission(Permissions permission) {
        return permissions.remove(permission.getPermission()) != null;
    }
}