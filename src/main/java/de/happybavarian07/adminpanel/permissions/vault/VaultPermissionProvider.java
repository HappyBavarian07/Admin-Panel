package de.happybavarian07.adminpanel.permissions.vault;

import de.happybavarian07.adminpanel.permissions.PermissionData;
import de.happybavarian07.adminpanel.permissions.PermissionService;
import de.happybavarian07.adminpanel.permissions.engine.GroupData;
import de.happybavarian07.adminpanel.permissions.engine.Node;
import de.happybavarian07.adminpanel.permissions.engine.Resolution;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;

/**
 * Lets other plugins that use Vault read and change permissions and groups through the permission manager. A {@code null} world means
 * every world.
 */
public final class VaultPermissionProvider extends Permission {
    private final PermissionService service;

    public VaultPermissionProvider(Plugin plugin, PermissionService service) {
        this.plugin = plugin;
        this.service = service;
    }

    static UUID idOf(String player) {
        Player online = Bukkit.getPlayerExact(player);
        return online != null ? online.getUniqueId() : Bukkit.getOfflinePlayer(player).getUniqueId();
    }

    static Map<String, String> context(String world) {
        return world == null || world.isBlank() ? Map.of() : Map.of("world", world);
    }

    @Override
    public String getName() {
        return "Admin-Panel";
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled() && service.isReady();
    }

    @Override
    public boolean hasSuperPermsCompat() {
        return true;
    }

    @Override
    public boolean playerHas(String world, String player, String permission) {
        Player online = Bukkit.getPlayerExact(player);
        if (online != null && (world == null || online.getWorld().getName().equalsIgnoreCase(world))) return online.hasPermission(permission);
        return Boolean.TRUE.equals(service.resolve(idOf(player), context(world)).check(permission));
    }

    @Override
    public boolean playerAdd(String world, String player, String permission) {
        service.setUserPermission(null, idOf(player), permission, true, 0, context(world));
        return true;
    }

    @Override
    public boolean playerRemove(String world, String player, String permission) {
        return service.unsetUserPermission(null, idOf(player), permission, context(world));
    }

    @Override
    public boolean groupHas(String world, String group, String permission) {
        GroupData data = service.group(group);
        if (data == null) return false;
        String wanted = Node.of(permission, true).permission();
        return data.nodes().stream().anyMatch(node -> node.value() && node.permission().equals(wanted) && node.appliesIn(context(world)));
    }

    @Override
    public boolean groupAdd(String world, String group, String permission) {
        return service.setGroupPermission(null, group, permission, true, 0, context(world));
    }

    @Override
    public boolean groupRemove(String world, String group, String permission) {
        return service.unsetGroupPermission(null, group, permission, context(world));
    }

    @Override
    public boolean playerInGroup(String world, String player, String group) {
        Resolution resolution = service.resolve(idOf(player), context(world));
        return resolution.groups().contains(PermissionData.normalizeGroup(group));
    }

    @Override
    public boolean playerAddGroup(String world, String player, String group) {
        return service.addUserGroup(null, idOf(player), group, 0, context(world));
    }

    @Override
    public boolean playerRemoveGroup(String world, String player, String group) {
        return service.removeUserGroup(null, idOf(player), group, context(world));
    }

    @Override
    public String[] getPlayerGroups(String world, String player) {
        return service.resolve(idOf(player), context(world)).groups().toArray(new String[0]);
    }

    @Override
    public String getPrimaryGroup(String world, String player) {
        var groups = service.resolve(idOf(player), context(world)).groups();
        return groups.isEmpty() ? "default" : groups.get(0);
    }

    @Override
    public String[] getGroups() {
        return service.groups().stream().map(GroupData::name).toArray(String[]::new);
    }

    @Override
    public boolean hasGroupSupport() {
        return true;
    }
}
