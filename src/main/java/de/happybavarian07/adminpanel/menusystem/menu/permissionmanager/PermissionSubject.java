package de.happybavarian07.adminpanel.menusystem.menu.permissionmanager;

import de.happybavarian07.adminpanel.permissions.PermissionService;
import de.happybavarian07.adminpanel.permissions.engine.Node;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

/** Whose entries a permission manager menu edits: a player or a group. */
record PermissionSubject(UUID user, String userName, String group) {
    static PermissionSubject ofUser(UUID user, String name) {
        return new PermissionSubject(user, name, null);
    }

    static PermissionSubject ofGroup(String group) {
        return new PermissionSubject(null, null, group);
    }

    boolean isUser() {
        return user != null;
    }

    String label() {
        return isUser() ? userName : group;
    }

    String editPermission() {
        return isUser() ? "AdminPanel.Permissions.User.Edit" : "AdminPanel.Permissions.Group.Edit";
    }

    List<Node> nodes(PermissionService service) {
        if (isUser()) return service.nodesOf(user);
        return service.group(group) == null ? List.of() : service.group(group).nodes();
    }

    boolean set(PermissionService service, CommandSender actor, Node node) {
        return isUser()
                ? runUser(service, actor, node)
                : service.setGroupPermission(actor, group, node.permission(), node.value(), node.expiresAt(), node.context());
    }

    private boolean runUser(PermissionService service, CommandSender actor, Node node) {
        service.setUserPermission(actor, user, node.permission(), node.value(), node.expiresAt(), node.context());
        return true;
    }

    boolean unset(PermissionService service, CommandSender actor, Node node) {
        return isUser()
                ? service.unsetUserPermission(actor, user, node.permission(), node.context())
                : service.unsetGroupPermission(actor, group, node.permission(), node.context());
    }
}
