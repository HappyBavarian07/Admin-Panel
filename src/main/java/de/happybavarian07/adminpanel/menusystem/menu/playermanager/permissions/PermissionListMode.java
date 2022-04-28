package de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions;/*
 * @Author HappyBavarian07
 * @Date 30.10.2021 | 14:17
 */

public enum PermissionListMode {
    ALL("All"),
    PLAYER("Player");

    private final String name;

    PermissionListMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PermissionListMode getNext(PermissionListMode current) {
        PermissionListMode[] modes = values();
        int count = 0;
        for(PermissionListMode mode : modes) {
            if(mode.equals(current)) {
                try {
                    return modes[(count+1)];
                } catch (Exception e) {
                    return modes[0];
                }
            }
            count++;
        }
        return null;
    }
}
