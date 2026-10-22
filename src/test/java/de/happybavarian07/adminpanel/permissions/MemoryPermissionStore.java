package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.adminpanel.permissions.engine.GroupData;
import de.happybavarian07.adminpanel.permissions.engine.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** A {@link PermissionStore} that keeps everything in maps, for tests. */
class MemoryPermissionStore implements PermissionStore {
    final Map<UUID, Map<String, Node>> users = new HashMap<>();
    final Map<String, GroupData> groups = new HashMap<>();
    final Map<String, Map<String, Node>> groupNodes = new HashMap<>();

    private static String key(Node node) {
        return node.permission() + "|" + node.contextText();
    }

    @Override
    public List<Node> loadUser(UUID user) {
        return new ArrayList<>(users.getOrDefault(user, Map.of()).values());
    }

    @Override
    public void saveUserNode(UUID user, Node node) {
        users.computeIfAbsent(user, id -> new HashMap<>()).put(key(node), node);
    }

    @Override
    public void deleteUserNode(UUID user, Node node) {
        users.getOrDefault(user, new HashMap<>()).remove(key(node));
    }

    @Override
    public void deleteUser(UUID user) {
        users.remove(user);
    }

    @Override
    public Set<UUID> knownUsers() {
        Set<UUID> known = new HashSet<>();
        users.forEach((id, nodes) -> {
            if (!nodes.isEmpty()) known.add(id);
        });
        return known;
    }

    @Override
    public List<GroupData> loadGroups() {
        List<GroupData> result = new ArrayList<>();
        for (GroupData group : groups.values()) {
            result.add(new GroupData(group.name(), group.displayName(), group.weight(), group.prefix(), group.suffix(),
                    new ArrayList<>(groupNodes.getOrDefault(group.name(), Map.of()).values())));
        }
        return result;
    }

    @Override
    public void saveGroup(GroupData group) {
        groups.put(group.name(), group);
    }

    @Override
    public void saveGroupNode(String group, Node node) {
        groupNodes.computeIfAbsent(group, id -> new HashMap<>()).put(key(node), node);
    }

    @Override
    public void deleteGroupNode(String group, Node node) {
        groupNodes.getOrDefault(group, new HashMap<>()).remove(key(node));
    }

    @Override
    public void deleteGroup(String group) {
        groups.remove(group);
        groupNodes.remove(group);
        String membership = Node.GROUP_PREFIX + group;
        groupNodes.values().forEach(nodes -> nodes.values().removeIf(node -> node.permission().equals(membership)));
        users.values().forEach(nodes -> nodes.values().removeIf(node -> node.permission().equals(membership)));
    }

    @Override
    public List<Expired> removeExpired(long now) {
        List<Expired> removed = new ArrayList<>();
        users.forEach((id, nodes) -> nodes.values().removeIf(node -> {
            boolean gone = node.expired(now);
            if (gone) removed.add(new Expired("user:" + id, node));
            return gone;
        }));
        groupNodes.forEach((name, nodes) -> nodes.values().removeIf(node -> {
            boolean gone = node.expired(now);
            if (gone) removed.add(new Expired("group:" + name, node));
            return gone;
        }));
        return removed;
    }
}
