package de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionGroup {

    private final String name;
    private final PermissionGroup parent;
    private final Map<String, PermissionGroup> subGroups = new HashMap<>();
    private final List<CustomPermission> permissions = new ArrayList<>();

    public PermissionGroup(String name, PermissionGroup parent) {
        this.name = name;
        this.parent = parent;
    }

    // Get the full name by traversing up the tree (ignores the "root" node)
    public String getFullName() {
        if (parent == null || parent.getName().equals("root")) return name;
        return parent.getFullName() + "." + name;
    }

    public String getName() {
        return name;
    }

    public Map<String, PermissionGroup> getSubGroups() {
        return subGroups;
    }

    public PermissionGroup getParent() {
        return parent;
    }

    public List<CustomPermission> getPermissions() {
        return permissions;
    }

    // Retrieve or create a subgroup with the given name.
    public PermissionGroup getOrCreateSubGroup(String subGroupName) {
        return subGroups.computeIfAbsent(subGroupName, k -> new PermissionGroup(k, this));
    }

    public void addPermission(CustomPermission customPermission) {
        permissions.add(customPermission);
    }
}
