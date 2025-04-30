package de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions.utils;

public class CustomPermission {
    private final String finalPermissionPart;
    private final PermissionGroup parentGroup;

    public CustomPermission(String finalPermissionPart, PermissionGroup parentGroup) {
        this.finalPermissionPart = finalPermissionPart;
        this.parentGroup = parentGroup;
    }

    public String getFinalPermissionPart() {
        return finalPermissionPart;
    }

    // Rebuilds the full permission string using the parent groups.
    public String getFullPermission() {
        String parentFullName = parentGroup.getFullName();
        if (parentFullName.equals("root") || parentFullName.isEmpty()) {
            return finalPermissionPart;
        }
        return parentFullName + "." + finalPermissionPart;
    }
}
