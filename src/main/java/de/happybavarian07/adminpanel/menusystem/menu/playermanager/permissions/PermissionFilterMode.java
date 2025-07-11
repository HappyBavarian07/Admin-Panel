package de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions;

public enum PermissionFilterMode {
    GLOBAL("Global"),
    PLAYER("Player");

    private final String configName;

    PermissionFilterMode(String configName) {
        this.configName = configName;
    }

    public String getConfigName() {
        return configName;
    }

    public static PermissionFilterMode fromConfigName(String configName) {
        for (PermissionFilterMode mode : values()) {
            if (mode.configName.equals(configName)) {
                return mode;
            }
        }
        return GLOBAL;
    }
}
