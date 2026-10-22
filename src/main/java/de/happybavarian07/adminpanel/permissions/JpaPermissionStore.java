package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.adminpanel.permissions.engine.GroupData;
import de.happybavarian07.adminpanel.permissions.engine.Node;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** {@link PermissionStore} on the three CoolStuffLib repositories. */
public final class JpaPermissionStore implements PermissionStore {
    private final PlayerPermissionRepository users;
    private final PermissionGroupRepository groups;
    private final GroupNodeRepository groupNodes;

    public JpaPermissionStore(PlayerPermissionRepository users, PermissionGroupRepository groups, GroupNodeRepository groupNodes) {
        this.users = users;
        this.groups = groups;
        this.groupNodes = groupNodes;
    }

    /** The id of a row: a hash of owner, permission and context, so the same permission can exist once per context. */
    static String entryId(String owner, Node node) {
        return hash(owner + ":" + node.permission() + ":" + node.contextText());
    }

    private static String hash(String text) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 is not available", e);
        }
    }

    /** The id an entry had before contexts existed (uuid and permission only), or an empty text if the entry has a context. */
    private static String legacyEntryId(UUID user, Node node) {
        return node.context().isEmpty() ? hash(user + ":" + node.permission()) : "";
    }

    private static Node toNode(PlayerPermission row) {
        return new Node(row.getPermission(), row.isValue(), row.getExpiresAt(), Node.parseContext(row.getContext()));
    }

    private static Node toNode(GroupNodeEntity row) {
        return new Node(row.getPermission(), row.isValue(), row.getExpiresAt(), Node.parseContext(row.getContext()));
    }

    @Override
    public List<Node> loadUser(UUID user) {
        List<Node> nodes = new ArrayList<>();
        for (PlayerPermission row : users.findByPlayerUUID(user)) {
            if (row != null && row.getPermission() != null) nodes.add(toNode(row));
        }
        return nodes;
    }

    @Override
    public void saveUserNode(UUID user, Node node) {
        PlayerPermission row = new PlayerPermission();
        row.setEntryID(entryId(user.toString(), node));
        row.setPlayerUUID(user);
        row.setPermission(node.permission());
        row.setValue(node.value());
        row.setExpiresAt(node.expiresAt());
        row.setContext(node.contextText());
        users.save(row);
        removeLegacyRow(user, node, row.getEntryID());
    }

    /** Rows from before the manager were keyed by uuid and permission only; replace them when the same entry is saved. */
    private void removeLegacyRow(UUID user, Node node, String currentId) {
        String legacy = legacyEntryId(user, node);
        if (legacy.isEmpty() || legacy.equals(currentId)) return;
        users.findById(legacy).ifPresent(users::delete);
    }

    @Override
    public void deleteUserNode(UUID user, Node node) {
        users.findById(entryId(user.toString(), node)).ifPresent(users::delete);
        String legacy = legacyEntryId(user, node);
        if (!legacy.isEmpty()) users.findById(legacy).ifPresent(users::delete);
    }

    @Override
    public void deleteUser(UUID user) {
        users.deleteAllByPlayerUUID(user);
    }

    @Override
    public Set<UUID> knownUsers() {
        Set<UUID> known = new HashSet<>();
        for (PlayerPermission row : users.findAll()) if (row.getPlayerUUID() != null) known.add(row.getPlayerUUID());
        return known;
    }

    @Override
    public List<GroupData> loadGroups() {
        Map<String, List<Node>> nodes = new HashMap<>();
        for (GroupNodeEntity row : groupNodes.findAll()) {
            if (row.getGroupName() != null) nodes.computeIfAbsent(row.getGroupName().toLowerCase(), key -> new ArrayList<>()).add(toNode(row));
        }
        List<GroupData> result = new ArrayList<>();
        for (PermissionGroupEntity row : groups.findAll()) {
            result.add(new GroupData(row.getName(), row.getDisplayName(), row.getWeight(), row.getPrefix(), row.getSuffix(),
                    nodes.getOrDefault(row.getName().toLowerCase(), List.of())));
        }
        return result;
    }

    @Override
    public void saveGroup(GroupData group) {
        long created = groups.findById(group.name()).map(PermissionGroupEntity::getCreatedAt).orElse(System.currentTimeMillis());
        groups.save(new PermissionGroupEntity(group.name(), group.displayName(), group.weight(), group.prefix(), group.suffix(), created));
    }

    @Override
    public void saveGroupNode(String group, Node node) {
        groupNodes.save(new GroupNodeEntity(entryId("group:" + group, node), group, node.permission(), node.value(), node.expiresAt(), node.contextText()));
    }

    @Override
    public void deleteGroupNode(String group, Node node) {
        groupNodes.findById(entryId("group:" + group, node)).ifPresent(groupNodes::delete);
    }

    @Override
    public void deleteGroup(String group) {
        groupNodes.deleteByGroupName(group);
        for (GroupNodeEntity row : groupNodes.findByPermission(Node.GROUP_PREFIX + group)) groupNodes.delete(row);
        for (PlayerPermission row : users.findByPermission(Node.GROUP_PREFIX + group)) users.delete(row);
        groups.findById(group).ifPresent(groups::delete);
    }

    @Override
    public List<Expired> removeExpired(long now) {
        List<Expired> removed = new ArrayList<>();
        for (PlayerPermission row : users.findByExpiresAtBetween(1, now)) {
            removed.add(new Expired("user:" + row.getPlayerUUID(), toNode(row)));
            users.delete(row);
        }
        for (GroupNodeEntity row : groupNodes.findByExpiresAtBetween(1, now)) {
            removed.add(new Expired("group:" + row.getGroupName(), toNode(row)));
            groupNodes.delete(row);
        }
        return removed;
    }
}
