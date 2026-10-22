package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.adminpanel.permissions.engine.GroupData;
import de.happybavarian07.adminpanel.permissions.engine.Node;
import de.happybavarian07.adminpanel.permissions.engine.Resolution;
import de.happybavarian07.adminpanel.permissions.engine.Resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.LongSupplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * The users and groups in memory, with every change written to the {@link PermissionStore} on another thread. This class knows nothing
 * about Bukkit: it is the part that can be tested without a server. {@link PermissionService} adds the players, events, journal and
 * attachments around it.
 */
public final class PermissionData {
    public static final Pattern GROUP_NAME = Pattern.compile("[a-z0-9_-]{1,32}");

    private final PermissionStore store;
    private final Executor io;
    private final LongSupplier clock;
    private final BiConsumer<String, Throwable> errorLog;
    private final Map<String, GroupData> groups = new ConcurrentHashMap<>();
    private final Map<UUID, List<Node>> users = new ConcurrentHashMap<>();
    private volatile boolean includeDefault = true;

    public PermissionData(PermissionStore store, Executor io, LongSupplier clock, BiConsumer<String, Throwable> errorLog) {
        this.store = store;
        this.io = io;
        this.clock = clock;
        this.errorLog = errorLog;
    }

    /** Reads all groups from the store (this blocks) and makes sure the {@code default} group exists. */
    public void load() {
        groups.clear();
        for (GroupData group : store.loadGroups()) groups.put(group.name(), group);
        if (!groups.containsKey(Resolver.DEFAULT_GROUP)) {
            GroupData created = new GroupData(Resolver.DEFAULT_GROUP, "", 0, "", "", List.of());
            groups.put(created.name(), created);
            async("save the default group", () -> store.saveGroup(created));
        }
    }

    /** @param includeDefault whether every user also inherits the {@code default} group */
    public void setIncludeDefault(boolean includeDefault) {
        this.includeDefault = includeDefault;
    }

    private void async(String what, Runnable task) {
        io.execute(() -> {
            try {
                task.run();
            } catch (RuntimeException e) {
                errorLog.accept("Could not " + what, e);
            }
        });
    }

    private static String key(Node node) {
        return node.permission() + "|" + node.contextText();
    }

    public static String normalizeGroup(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    // ---- groups

    /** @return the groups, heaviest first, then by name */
    public List<GroupData> groups() {
        List<GroupData> all = new ArrayList<>(groups.values());
        all.sort(Comparator.comparingInt(GroupData::weight).reversed().thenComparing(GroupData::name));
        return all;
    }

    public GroupData group(String name) {
        return groups.get(normalizeGroup(name));
    }

    /** @return {@code false} if the name is not valid or the group exists */
    public synchronized boolean createGroup(String name) {
        String normalized = normalizeGroup(name);
        if (!GROUP_NAME.matcher(normalized).matches() || groups.containsKey(normalized)) return false;
        GroupData created = new GroupData(normalized, "", 0, "", "", List.of());
        groups.put(normalized, created);
        async("save the group " + normalized, () -> store.saveGroup(created));
        return true;
    }

    /** @return {@code false} if the group does not exist or is the {@code default} group */
    public synchronized boolean deleteGroup(String name) {
        String normalized = normalizeGroup(name);
        if (normalized.equals(Resolver.DEFAULT_GROUP) || groups.remove(normalized) == null) return false;
        String membership = Node.GROUP_PREFIX + normalized;
        for (Map.Entry<String, GroupData> entry : groups.entrySet()) {
            GroupData group = entry.getValue();
            if (group.nodes().stream().anyMatch(node -> node.permission().equals(membership))) {
                List<Node> kept = new ArrayList<>(group.nodes());
                kept.removeIf(node -> node.permission().equals(membership));
                groups.put(entry.getKey(), new GroupData(group.name(), group.displayName(), group.weight(), group.prefix(), group.suffix(), kept));
            }
        }
        for (Map.Entry<UUID, List<Node>> entry : users.entrySet()) entry.getValue().removeIf(node -> node.permission().equals(membership));
        async("delete the group " + normalized, () -> store.deleteGroup(normalized));
        return true;
    }

    private synchronized GroupData replaceGroup(String name, UnaryOperator<GroupData> change) {
        String normalized = normalizeGroup(name);
        GroupData current = groups.get(normalized);
        if (current == null) return null;
        GroupData changed = change.apply(current);
        groups.put(normalized, changed);
        return changed;
    }

    private boolean updateGroup(String name, UnaryOperator<GroupData> change) {
        GroupData changed = replaceGroup(name, change);
        if (changed == null) return false;
        async("save the group " + changed.name(), () -> store.saveGroup(changed));
        return true;
    }

    public boolean setWeight(String name, int weight) {
        return updateGroup(name, g -> new GroupData(g.name(), g.displayName(), weight, g.prefix(), g.suffix(), g.nodes()));
    }

    public boolean setPrefix(String name, String prefix) {
        return updateGroup(name, g -> new GroupData(g.name(), g.displayName(), g.weight(), prefix, g.suffix(), g.nodes()));
    }

    public boolean setSuffix(String name, String suffix) {
        return updateGroup(name, g -> new GroupData(g.name(), g.displayName(), g.weight(), g.prefix(), suffix, g.nodes()));
    }

    public boolean setDisplayName(String name, String displayName) {
        return updateGroup(name, g -> new GroupData(g.name(), displayName, g.weight(), g.prefix(), g.suffix(), g.nodes()));
    }

    /**
     * Sets an entry of a group, replacing the entry with the same permission and context.
     *
     * @return {@code false} if the group does not exist or the entry would make the group inherit itself
     */
    public synchronized boolean setGroupNode(String group, Node node) {
        String normalized = normalizeGroup(group);
        if (!groups.containsKey(normalized)) return false;
        if (node.isGroup() && node.value() && !canInherit(normalized, node.groupName())) return false;
        replaceGroup(normalized, g -> {
            List<Node> nodes = new ArrayList<>(g.nodes());
            nodes.removeIf(existing -> key(existing).equals(key(node)));
            nodes.add(node);
            return new GroupData(g.name(), g.displayName(), g.weight(), g.prefix(), g.suffix(), nodes);
        });
        async("save an entry of the group " + normalized, () -> store.saveGroupNode(normalized, node));
        return true;
    }

    /** @return {@code true} if an entry with the permission and context existed */
    public synchronized boolean unsetGroupNode(String group, String permission, Map<String, String> context) {
        String normalized = normalizeGroup(group);
        GroupData current = groups.get(normalized);
        if (current == null) return false;
        Node probe = new Node(permission, true, 0, context);
        Node existing = current.nodes().stream().filter(node -> key(node).equals(key(probe))).findFirst().orElse(null);
        if (existing == null) return false;
        replaceGroup(normalized, g -> {
            List<Node> nodes = new ArrayList<>(g.nodes());
            nodes.removeIf(node -> key(node).equals(key(probe)));
            return new GroupData(g.name(), g.displayName(), g.weight(), g.prefix(), g.suffix(), nodes);
        });
        async("remove an entry of the group " + normalized, () -> store.deleteGroupNode(normalized, existing));
        return true;
    }

    /** @return whether {@code group} can get {@code parent} as a parent without a cycle */
    public boolean canInherit(String group, String parent) {
        String child = normalizeGroup(group);
        String start = normalizeGroup(parent);
        if (child.equals(start)) return false;
        Set<String> seen = new HashSet<>();
        List<String> open = new ArrayList<>(List.of(start));
        while (!open.isEmpty()) {
            String current = open.remove(open.size() - 1);
            if (!seen.add(current)) continue;
            GroupData data = groups.get(current);
            if (data == null) continue;
            for (Node node : data.nodes()) {
                if (!node.isGroup()) continue;
                if (node.groupName().equals(child)) return false;
                open.add(node.groupName());
            }
        }
        return true;
    }

    /** @return the members of a group that are loaded right now, with their entry */
    public Map<UUID, Node> loadedMembers(String group) {
        String membership = Node.GROUP_PREFIX + normalizeGroup(group);
        Map<UUID, Node> members = new ConcurrentHashMap<>();
        for (Map.Entry<UUID, List<Node>> entry : users.entrySet()) {
            for (Node node : entry.getValue()) if (node.permission().equals(membership)) members.put(entry.getKey(), node);
        }
        return members;
    }

    // ---- users

    /** @return the entries of a user; read from the store the first time (this may block) */
    public List<Node> userNodes(UUID user) {
        return users.computeIfAbsent(user, id -> new java.util.concurrent.CopyOnWriteArrayList<>(store.loadUser(id)));
    }

    /** Sets an entry of a user, replacing the entry with the same permission and context. */
    public synchronized void setUserNode(UUID user, Node node) {
        List<Node> nodes = userNodes(user);
        nodes.removeIf(existing -> key(existing).equals(key(node)));
        nodes.add(node);
        async("save an entry of " + user, () -> store.saveUserNode(user, node));
    }

    /** @return {@code true} if an entry with the permission and context existed */
    public synchronized boolean unsetUserNode(UUID user, String permission, Map<String, String> context) {
        Node probe = new Node(permission, true, 0, context);
        List<Node> nodes = userNodes(user);
        Node existing = nodes.stream().filter(node -> key(node).equals(key(probe))).findFirst().orElse(null);
        if (existing == null) return false;
        nodes.remove(existing);
        async("remove an entry of " + user, () -> store.deleteUserNode(user, existing));
        return true;
    }

    public synchronized void clearUser(UUID user) {
        userNodes(user).clear();
        async("clear " + user, () -> store.deleteUser(user));
    }

    /** Drops the cached entries of a user, for example when the player left. */
    public void forget(UUID user) {
        users.remove(user);
    }

    /** @return every user that has entries in the store (this blocks) */
    public Set<UUID> knownUsers() {
        Set<UUID> known = new HashSet<>(store.knownUsers());
        known.addAll(users.keySet());
        return known;
    }

    public Resolution resolve(UUID user, Map<String, String> context) {
        return Resolver.resolve(userNodes(user), groups::get, context, clock.getAsLong(), includeDefault);
    }

    /**
     * Removes the entries that ended, from the store and from memory. Run it on a thread that may block.
     *
     * @return what was removed
     */
    public synchronized List<PermissionStore.Expired> sweep() {
        long now = clock.getAsLong();
        List<PermissionStore.Expired> removed = store.removeExpired(now);
        for (List<Node> nodes : users.values()) nodes.removeIf(node -> node.expired(now));
        for (Map.Entry<String, GroupData> entry : groups.entrySet()) {
            GroupData group = entry.getValue();
            if (group.nodes().stream().anyMatch(node -> node.expired(now))) {
                List<Node> kept = new ArrayList<>(group.nodes());
                kept.removeIf(node -> node.expired(now));
                groups.put(entry.getKey(), new GroupData(group.name(), group.displayName(), group.weight(), group.prefix(), group.suffix(), kept));
            }
        }
        return removed;
    }

    /** @return all group names, for tab completion */
    public Collection<String> groupNames() {
        return new ArrayList<>(groups.keySet());
    }
}
