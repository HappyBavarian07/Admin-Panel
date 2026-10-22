package de.happybavarian07.adminpanel.permissions.vault;

import de.happybavarian07.adminpanel.permissions.PermissionService;
import de.happybavarian07.adminpanel.permissions.engine.GroupData;
import de.happybavarian07.adminpanel.permissions.engine.Node;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Prefix, suffix and free info values for Vault. Info values are stored as the entries {@code meta.<node>.<value>} of a player or group, a
 * prefix of a player as {@code prefix.100.<text>}.
 */
public final class VaultChatProvider extends Chat {
    private static final String META = "meta.";
    private static final int PLAYER_PRIORITY = 100;

    private final Plugin plugin;
    private final PermissionService service;

    public VaultChatProvider(Plugin plugin, PermissionService service, Permission permission) {
        super(permission);
        this.plugin = plugin;
        this.service = service;
    }

    @Override
    public String getName() {
        return "Admin-Panel";
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled() && service.isReady();
    }

    // ---- prefix and suffix

    @Override
    public String getPlayerPrefix(String world, String player) {
        return service.resolve(VaultPermissionProvider.idOf(player), VaultPermissionProvider.context(world)).prefix();
    }

    @Override
    public void setPlayerPrefix(String world, String player, String prefix) {
        setPlayerMeta(world, player, "prefix.", prefix);
    }

    @Override
    public String getPlayerSuffix(String world, String player) {
        return service.resolve(VaultPermissionProvider.idOf(player), VaultPermissionProvider.context(world)).suffix();
    }

    @Override
    public void setPlayerSuffix(String world, String player, String suffix) {
        setPlayerMeta(world, player, "suffix.", suffix);
    }

    private void setPlayerMeta(String world, String player, String kind, String text) {
        UUID id = VaultPermissionProvider.idOf(player);
        Map<String, String> context = VaultPermissionProvider.context(world);
        for (Node node : service.nodesOf(id)) {
            if (node.permission().startsWith(kind) && node.context().equals(context)) service.unsetUserPermission(null, id, node.permission(), context);
        }
        if (text != null && !text.isEmpty()) service.setUserPermission(null, id, kind + PLAYER_PRIORITY + "." + text, true, 0, context);
    }

    @Override
    public String getGroupPrefix(String world, String group) {
        GroupData data = service.group(group);
        return data == null ? "" : data.prefix();
    }

    @Override
    public void setGroupPrefix(String world, String group, String prefix) {
        service.setGroupPrefix(null, group, prefix == null ? "" : prefix);
    }

    @Override
    public String getGroupSuffix(String world, String group) {
        GroupData data = service.group(group);
        return data == null ? "" : data.suffix();
    }

    @Override
    public void setGroupSuffix(String world, String group, String suffix) {
        service.setGroupSuffix(null, group, suffix == null ? "" : suffix);
    }

    // ---- info values

    private String read(List<Node> nodes, String node, Map<String, String> context) {
        String start = META + node.toLowerCase() + ".";
        for (Node entry : nodes) {
            if (entry.value() && entry.permission().startsWith(start) && entry.appliesIn(context)) return entry.permission().substring(start.length());
        }
        return null;
    }

    private String playerValue(String world, String player, String node) {
        return read(service.nodesOf(VaultPermissionProvider.idOf(player)), node, VaultPermissionProvider.context(world));
    }

    private String groupValue(String group, String node) {
        GroupData data = service.group(group);
        return data == null ? null : read(data.nodes(), node, Map.of());
    }

    private void writePlayer(String world, String player, String node, Object value) {
        UUID id = VaultPermissionProvider.idOf(player);
        Map<String, String> context = VaultPermissionProvider.context(world);
        String start = META + node.toLowerCase() + ".";
        for (Node entry : service.nodesOf(id)) {
            if (entry.permission().startsWith(start) && entry.context().equals(context)) service.unsetUserPermission(null, id, entry.permission(), context);
        }
        if (value != null) service.setUserPermission(null, id, start + value, true, 0, context);
    }

    private void writeGroup(String group, String node, Object value) {
        GroupData data = service.group(group);
        if (data == null) return;
        String start = META + node.toLowerCase() + ".";
        for (Node entry : data.nodes()) {
            if (entry.permission().startsWith(start) && entry.context().isEmpty()) service.unsetGroupPermission(null, group, entry.permission(), Map.of());
        }
        if (value != null) service.setGroupPermission(null, group, start + value, true, 0, Map.of());
    }

    private static int toInt(String text, int fallback) {
        try {
            return text == null ? fallback : Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double toDouble(String text, double fallback) {
        try {
            return text == null ? fallback : Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static boolean toBoolean(String text, boolean fallback) {
        return text == null ? fallback : Boolean.parseBoolean(text);
    }

    @Override
    public int getPlayerInfoInteger(String world, String player, String node, int defaultValue) {
        return toInt(playerValue(world, player, node), defaultValue);
    }

    @Override
    public void setPlayerInfoInteger(String world, String player, String node, int value) {
        writePlayer(world, player, node, value);
    }

    @Override
    public int getGroupInfoInteger(String world, String group, String node, int defaultValue) {
        return toInt(groupValue(group, node), defaultValue);
    }

    @Override
    public void setGroupInfoInteger(String world, String group, String node, int value) {
        writeGroup(group, node, value);
    }

    @Override
    public double getPlayerInfoDouble(String world, String player, String node, double defaultValue) {
        return toDouble(playerValue(world, player, node), defaultValue);
    }

    @Override
    public void setPlayerInfoDouble(String world, String player, String node, double value) {
        writePlayer(world, player, node, value);
    }

    @Override
    public double getGroupInfoDouble(String world, String group, String node, double defaultValue) {
        return toDouble(groupValue(group, node), defaultValue);
    }

    @Override
    public void setGroupInfoDouble(String world, String group, String node, double value) {
        writeGroup(group, node, value);
    }

    @Override
    public boolean getPlayerInfoBoolean(String world, String player, String node, boolean defaultValue) {
        return toBoolean(playerValue(world, player, node), defaultValue);
    }

    @Override
    public void setPlayerInfoBoolean(String world, String player, String node, boolean value) {
        writePlayer(world, player, node, value);
    }

    @Override
    public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
        return toBoolean(groupValue(group, node), defaultValue);
    }

    @Override
    public void setGroupInfoBoolean(String world, String group, String node, boolean value) {
        writeGroup(group, node, value);
    }

    @Override
    public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
        String value = playerValue(world, player, node);
        return value == null ? defaultValue : value;
    }

    @Override
    public void setPlayerInfoString(String world, String player, String node, String value) {
        writePlayer(world, player, node, value);
    }

    @Override
    public String getGroupInfoString(String world, String group, String node, String defaultValue) {
        String value = groupValue(group, node);
        return value == null ? defaultValue : value;
    }

    @Override
    public void setGroupInfoString(String world, String group, String node, String value) {
        writeGroup(group, node, value);
    }

    @Override
    public String[] getGroups() {
        return service.groups().stream().map(GroupData::name).toArray(String[]::new);
    }

    @Override
    public String getPrimaryGroup(String world, String player) {
        var groups = service.resolve(VaultPermissionProvider.idOf(player), VaultPermissionProvider.context(world)).groups();
        return groups.isEmpty() ? "default" : groups.get(0);
    }

    @Override
    public String[] getPlayerGroups(String world, String player) {
        return service.resolve(VaultPermissionProvider.idOf(player), VaultPermissionProvider.context(world)).groups().toArray(new String[0]);
    }
}
