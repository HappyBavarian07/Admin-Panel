package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.adminpanel.permissions.engine.GroupData;
import de.happybavarian07.adminpanel.permissions.engine.Node;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Writes all groups and users of a {@link PermissionData} to a YAML file and reads such a file back. Importing adds and replaces entries,
 * it never deletes anything that is not in the file.
 */
public final class PermissionTransfer {
    /** What an import did. */
    public record Result(int groups, int users, int entries, List<String> problems) {
    }

    private PermissionTransfer() {
    }

    /** Reads every user from the store, so call it off the main thread. */
    public static YamlConfiguration export(PermissionData data) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("version", 1);
        for (GroupData group : data.groups()) {
            String path = "groups." + group.name();
            yaml.set(path + ".weight", group.weight());
            yaml.set(path + ".display-name", group.displayName());
            yaml.set(path + ".prefix", group.prefix());
            yaml.set(path + ".suffix", group.suffix());
            yaml.set(path + ".nodes", write(group.nodes()));
        }
        for (UUID user : data.knownUsers()) {
            List<Map<String, Object>> nodes = write(data.userNodes(user));
            if (!nodes.isEmpty()) yaml.set("users." + user, nodes);
        }
        return yaml;
    }

    private static List<Map<String, Object>> write(List<Node> nodes) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Node node : nodes) {
            Map<String, Object> entry = new java.util.LinkedHashMap<>();
            entry.put("permission", node.permission());
            entry.put("value", node.value());
            if (node.expiresAt() > 0) entry.put("expires", node.expiresAt());
            if (!node.context().isEmpty()) entry.put("context", node.contextText());
            list.add(entry);
        }
        return list;
    }

    public static Result load(PermissionData data, YamlConfiguration yaml) {
        List<String> problems = new ArrayList<>();
        int groups = 0;
        int users = 0;
        int entries = 0;
        ConfigurationSection groupSection = yaml.getConfigurationSection("groups");
        List<String> imported = new ArrayList<>();
        if (groupSection != null) {
            for (String name : groupSection.getKeys(false)) {
                if (data.group(name) == null && !data.createGroup(name)) {
                    problems.add("group " + name + ": invalid name");
                    continue;
                }
                ConfigurationSection section = groupSection.getConfigurationSection(name);
                if (section == null) continue;
                data.setWeight(name, section.getInt("weight"));
                data.setDisplayName(name, section.getString("display-name", ""));
                data.setPrefix(name, section.getString("prefix", ""));
                data.setSuffix(name, section.getString("suffix", ""));
                imported.add(name);
                groups++;
            }
            for (String name : imported) {
                for (Node node : read(groupSection.getMapList(name + ".nodes"), problems, "group " + name)) {
                    if (data.setGroupNode(name, node)) entries++;
                    else problems.add("group " + name + ": " + node.permission() + " was skipped (it would make the group inherit itself)");
                }
            }
        }
        ConfigurationSection userSection = yaml.getConfigurationSection("users");
        if (userSection != null) {
            for (String key : userSection.getKeys(false)) {
                UUID user;
                try {
                    user = UUID.fromString(key);
                } catch (IllegalArgumentException e) {
                    problems.add("user " + key + ": not a uuid");
                    continue;
                }
                for (Node node : read(userSection.getMapList(key), problems, "user " + key)) {
                    data.setUserNode(user, node);
                    entries++;
                }
                users++;
            }
        }
        return new Result(groups, users, entries, problems);
    }

    private static List<Node> read(List<Map<?, ?>> maps, List<String> problems, String owner) {
        List<Node> nodes = new ArrayList<>();
        for (Map<?, ?> map : maps) {
            Object permission = map.get("permission");
            if (permission == null || permission.toString().isBlank()) {
                problems.add(owner + ": an entry has no permission");
                continue;
            }
            Object value = map.get("value");
            Object expires = map.get("expires");
            Object context = map.get("context");
            nodes.add(new Node(permission.toString(), value == null || Boolean.parseBoolean(value.toString()),
                    expires instanceof Number number ? number.longValue() : 0,
                    Node.parseContext(context == null ? "" : context.toString())));
        }
        return nodes;
    }
}
