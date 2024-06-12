package de.happybavarian07.webui.utils;/*
 * @Author HappyBavarian07
 * @Date 28.12.2023 | 22:54
 */

import java.util.ArrayList;
import java.util.List;

public enum Permissions {
    // TOP LEVEL PERMISSIONS (EXTREMELY DANGEROUS, AS THEY CAN BE USED TO BYPASS ALL PERMISSIONS)
    EVERYTHING("*", "*"),

    // General Permissions
    SERVER_INFO("server.info", "Server.Info"),
    SERVER_PROPERTIES("server.properties", "Server.Properties"),

    // Console Panel
    CP_ACCESS("cp.access", "ControlPanel.Access"),
    CP_SEND_COMMAND("cp.send_command", "ControlPanel.Send_Command"),
    CP_CONSOLE_LOG("cp.console_log", "ControlPanel.Console_Log"),
    CP("cp.*", "ControlPanel.*", CP_ACCESS, CP_SEND_COMMAND, CP_CONSOLE_LOG),

    // Server Control Panel
    SCP_ACCESS("scp.access", "ServerControlPanel.Access"),
    SCP_SHUTDOWN("scp.shutdown", "ServerControlPanel.Shutdown"),
    SCP_RESTART("scp.restart", "ServerControlPanel.Restart"),
    SCP_RELOAD("scp.reload", "ServerControlPanel.Reload"),
    SCP_BROADCAST("scp.broadcast", "ServerControlPanel.Broadcast"),
    SCP_SERVERINFO("scp.serverinfo", "ServerControlPanel.Server_Info", SERVER_INFO),
    SCP("scp.*", "ServerControlPanel.*", SCP_ACCESS, SCP_SHUTDOWN, SCP_RESTART, SCP_RELOAD, SCP_BROADCAST, SCP_SERVERINFO),

    // Player Control Panel
    PCP_PLAYER_LIST("pcp.player_list", "PlayerControlPanel.Player_List"),
    PCP_ACCESS("pcp.access", "PlayerControlPanel.Access", PCP_PLAYER_LIST),
    PCP_KICK("pcp.kick", "PlayerControlPanel.Kick"),
    PCP_BAN("pcp.ban", "PlayerControlPanel.Ban"),
    PCP_MUTE("pcp.mute", "PlayerControlPanel.Mute"),
    PCP_UNBAN("pcp.unban", "PlayerControlPanel.Unban"),
    PCP_UNMUTE("pcp.unmute", "PlayerControlPanel.Unmute"),
    PCP_SEND_MESSAGE("pcp.send_message", "PlayerControlPanel.Send_Message"),
    PCP_OP("pcp.op", "PlayerControlPanel.Op"),
    PCP_DEOP("pcp.deop", "PlayerControlPanel.Deop"),
    PCP_GAMEMODE("pcp.gamemode", "PlayerControlPanel.Gamemode"),
    PCP_GIVE("pcp.give", "PlayerControlPanel.Give"),
    PCP("pcp.*", "PlayerControlPanel.*", PCP_ACCESS, PCP_KICK, PCP_BAN, PCP_MUTE, PCP_UNBAN, PCP_UNMUTE, PCP_SEND_MESSAGE, PCP_OP, PCP_DEOP, PCP_GAMEMODE, PCP_GIVE),

    // Manage Web Server Permissions
    WS_MANAGE_USERS("ws.manage.users", "WebServer.Manage.Users"),
    WS_MANAGE_ROLES("ws.manage.roles", "WebServer.Manage.Roles"),
    WS_MANAGE_PERMISSIONS("ws.manage.permissions", "WebServer.Manage.Permissions"),
    WS_MANAGE("ws.manage.*", "WebServer.Manage.*", WS_MANAGE_USERS, WS_MANAGE_ROLES, WS_MANAGE_PERMISSIONS),
    WS_USERS_ADD("ws.users.add", "WebServer.Users.Add"),
    WS_USERS_REMOVE("ws.users.remove", "WebServer.Users.Remove"),
    WS_USERS_EDIT("ws.users.edit", "WebServer.Users.Edit"),
    WS_USERS_VIEW("ws.users.view", "WebServer.Users.View"),
    WS_USERS_LIST("ws.users.list", "WebServer.Users.List"),
    WS_USERS_CHANGE_PASSWORD("ws.users.change_password", "WebServer.Users.ChangePassword"),
    WB_USERS_INFO("ws.users.info", "WebServer.Users.Info"),
    WS_USERS("ws.users.*", "WebServer.Users.*", WS_USERS_ADD, WS_USERS_REMOVE, WS_USERS_EDIT, WS_USERS_VIEW, WS_USERS_LIST, WS_USERS_CHANGE_PASSWORD),
    WS_ROLES_ADD("ws.roles.add", "WebServer.Roles.Add"),
    WS_ROLES_REMOVE("ws.roles.remove", "WebServer.Roles.Remove"),
    WS_ROLES_EDIT("ws.roles.edit", "WebServer.Roles.Edit"),
    WS_ROLES_VIEW("ws.roles.view", "WebServer.Roles.View"),
    WS_ROLES_LIST("ws.roles.list", "WebServer.Roles.List"),
    WS_ROLES_INFO("ws.roles.info", "WebServer.Roles.Info"),
    WS_ROLES("ws.roles.*", "WebServer.Roles.*", WS_ROLES_ADD, WS_ROLES_REMOVE, WS_ROLES_EDIT, WS_ROLES_VIEW, WS_ROLES_LIST),
    WS_PERMISSIONS_ADD("ws.permissions.add", "WebServer.Permissions.Add"),
    WS_PERMISSIONS_REMOVE("ws.permissions.remove", "WebServer.Permissions.Remove"),
    WS_PERMISSIONS_EDIT("ws.permissions.edit", "WebServer.Permissions.Edit"),
    WS_PERMISSIONS_VIEW("ws.permissions.view", "WebServer.Permissions.View"),
    WS_PERMISSIONS_LIST("ws.permissions.list", "WebServer.Permissions.List"),
    WS_PERMISSIONS("ws.permissions.*", "WebServer.Permissions.*", WS_PERMISSIONS_ADD, WS_PERMISSIONS_REMOVE, WS_PERMISSIONS_EDIT, WS_PERMISSIONS_VIEW, WS_PERMISSIONS_LIST),
    WS("ws.*", "WebServer.*", WS_MANAGE, WS_USERS, WS_ROLES, WS_PERMISSIONS);

    private final String permission;
    private final String fullPermissionName;
    private final List<Permissions> children = new ArrayList<>();

    Permissions(String permission, String fullPermissionName, Permissions... children) {
        this.permission = permission;
        this.fullPermissionName = fullPermissionName;
        this.children.addAll(List.of(children));
    }

    public static boolean contains(String upperCase) {
        for (Permissions p : values()) {
            if (p.getPermission().equalsIgnoreCase(upperCase) || p.getFullPermissionName().equalsIgnoreCase(upperCase)) {
                return true;
            }
        }
        return false;
    }

    // Add a static method to get a permission by its string value
    public static Permissions getByPermission(String permission) {
        for (Permissions p : values()) {
            if (p.getPermission().equalsIgnoreCase(permission) || p.getFullPermissionName().equalsIgnoreCase(permission)) {
                return p;
            }
        }
        return null;
    }

    public String getFullPermissionName() {
        return fullPermissionName;
    }

    public String getPermission() {
        return permission;
    }

    public List<Permissions> getChildren() {
        return children;
    }
}
