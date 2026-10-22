package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.adminpanel.journal.AdminJournal;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.permissions.api.PermissionChangeEvent;
import de.happybavarian07.adminpanel.permissions.api.PermissionContextProvider;
import de.happybavarian07.adminpanel.permissions.engine.GroupData;
import de.happybavarian07.adminpanel.permissions.engine.Node;
import de.happybavarian07.adminpanel.permissions.engine.Resolution;
import de.happybavarian07.adminpanel.utils.LogPrefixExtension;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

/**
 * The permission manager. It keeps users, groups and their entries (see {@link PermissionData}), works out what each online player may do
 * (see {@link Resolution}) and puts the result on one {@link PermissionAttachment} per player.
 *
 * <p>Every change method takes the {@code actor}, who is written to the admin journal; pass {@code null} for the console or the system.
 * After a change the affected online players are recalculated and a {@link PermissionChangeEvent} is fired.</p>
 *
 * <p>Other plugins reach it through {@code AdminPanelAPI#getPermissionService()}.</p>
 */
public final class PermissionService {
    private final AdminPanelMain plugin;
    private final PermissionData data;
    private final ExecutorService io;
    private final Map<UUID, PermissionAttachment> attachments = new ConcurrentHashMap<>();
    private final Map<UUID, Resolution> resolutions = new ConcurrentHashMap<>();
    private final List<PermissionContextProvider> providers = new CopyOnWriteArrayList<>();
    private volatile boolean ready;
    private BukkitTask sweepTask;

    public PermissionService(AdminPanelMain plugin, PermissionData data, ExecutorService io) {
        this.plugin = plugin;
        this.data = data;
        this.io = io;
        providers.add((player, contexts) -> contexts.put("world", player.getWorld().getName()));
    }

    // ---- start and stop

    /** Reads the groups (this blocks), applies everyone who is online and starts the sweep of ended entries. */
    public void start() {
        data.setIncludeDefault(plugin.getConfig().getBoolean("Permissions.DefaultGroup", true));
        data.load();
        ready = true;
        long seconds = Math.max(5, plugin.getConfig().getLong("Permissions.SweepSeconds", 60));
        Bukkit.getScheduler().runTask(plugin, this::applyAll);
        sweepTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::sweepNow, seconds * 20, seconds * 20);
        log("Permission manager ready: " + data.groups().size() + " groups");
    }

    public void stop() {
        if (sweepTask != null) sweepTask.cancel();
        io.shutdown();
        try {
            io.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        for (Map.Entry<UUID, PermissionAttachment> entry : attachments.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                try {
                    player.removeAttachment(entry.getValue());
                } catch (IllegalArgumentException ignored) {
                    // already removed
                }
            }
        }
        attachments.clear();
        resolutions.clear();
    }

    public boolean isReady() {
        return ready;
    }

    public PermissionData data() {
        return data;
    }

    private void log(String message) {
        plugin.getFileLogger().writeToLog(Level.INFO, message, LogPrefixExtension.PERMISSIONS);
    }

    // ---- extension points

    /** Adds a kind of context that entries can be limited to. */
    public void registerContextProvider(PermissionContextProvider provider) {
        providers.add(provider);
    }

    public void unregisterContextProvider(PermissionContextProvider provider) {
        providers.remove(provider);
    }

    /** @return the current context of a player: the world and whatever the registered providers add */
    public Map<String, String> contextOf(Player player) {
        Map<String, String> contexts = new TreeMap<>();
        for (PermissionContextProvider provider : providers) {
            try {
                provider.contribute(player, contexts);
            } catch (RuntimeException e) {
                AdminPanelMain.logError("A permission context provider failed", e);
            }
        }
        return contexts;
    }

    // ---- reading

    /**
     * @return what the user may do. For an online player the current context is used, for everyone else only entries without a context
     * apply
     */
    public Resolution resolve(UUID user) {
        Player online = Bukkit.getPlayer(user);
        return data.resolve(user, online == null ? Map.of() : contextOf(online));
    }

    public Resolution resolve(UUID user, Map<String, String> context) {
        return data.resolve(user, context);
    }

    /** @return {@code true}, {@code false} or {@code null} if nothing decides */
    public Boolean check(UUID user, String permission) {
        return resolve(user).check(permission);
    }

    public List<Node> nodesOf(UUID user) {
        return List.copyOf(data.userNodes(user));
    }

    /** @return the groups a user was put in directly (not the inherited ones), including temporary and context limited ones */
    public List<Node> membershipsOf(UUID user) {
        List<Node> memberships = new ArrayList<>();
        for (Node node : data.userNodes(user)) if (node.isGroup()) memberships.add(node);
        return memberships;
    }

    public List<GroupData> groups() {
        return data.groups();
    }

    public GroupData group(String name) {
        return data.group(name);
    }

    public Map<String, PermissionAttachment> attachmentsByName() {
        Map<String, PermissionAttachment> byName = new HashMap<>();
        attachments.forEach((id, attachment) -> {
            Player player = Bukkit.getPlayer(id);
            if (player != null) byName.put(player.getName(), attachment);
        });
        return byName;
    }

    public Map<UUID, PermissionAttachment> attachments() {
        return attachments;
    }

    /** @return the last result for an online player, {@code null} for anyone else */
    public Resolution lastResolution(UUID user) {
        return resolutions.get(user);
    }

    public String prefixOf(UUID user) {
        Resolution resolution = resolutions.get(user);
        return resolution == null ? resolve(user).prefix() : resolution.prefix();
    }

    public String suffixOf(UUID user) {
        Resolution resolution = resolutions.get(user);
        return resolution == null ? resolve(user).suffix() : resolution.suffix();
    }

    public Set<UUID> knownUsers() {
        return data.knownUsers();
    }

    public String nameOf(UUID user) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(user);
        return player.getName() == null ? user.toString() : player.getName();
    }

    // ---- changing users

    public void setUserPermission(CommandSender actor, UUID user, String permission, boolean value, long expiresAt, Map<String, String> context) {
        Node node = new Node(permission, value, expiresAt, context);
        data.setUserNode(user, node);
        changed(actor, node.isGroup() ? "USER_GROUP_ADD" : "PERMISSION_SET", user.toString(), describe(node),
                node.isGroup() ? PermissionChangeEvent.Kind.GROUP_ADDED : PermissionChangeEvent.Kind.SET, "user:" + user, node.permission(), user);
    }

    public boolean unsetUserPermission(CommandSender actor, UUID user, String permission, Map<String, String> context) {
        if (!data.unsetUserNode(user, permission, context)) return false;
        boolean group = permission.toLowerCase().startsWith(Node.GROUP_PREFIX);
        changed(actor, group ? "USER_GROUP_REMOVE" : "PERMISSION_UNSET", user.toString(), permission + contextSuffix(context),
                group ? PermissionChangeEvent.Kind.GROUP_REMOVED : PermissionChangeEvent.Kind.UNSET, "user:" + user, permission, user);
        return true;
    }

    /** @return {@code false} if the group does not exist */
    public boolean addUserGroup(CommandSender actor, UUID user, String group, long expiresAt, Map<String, String> context) {
        if (data.group(group) == null) return false;
        setUserPermission(actor, user, Node.GROUP_PREFIX + PermissionData.normalizeGroup(group), true, expiresAt, context);
        return true;
    }

    public boolean removeUserGroup(CommandSender actor, UUID user, String group, Map<String, String> context) {
        return unsetUserPermission(actor, user, Node.GROUP_PREFIX + PermissionData.normalizeGroup(group), context);
    }

    public void clearUser(CommandSender actor, UUID user) {
        data.clearUser(user);
        changed(actor, "USER_CLEAR", user.toString(), "", PermissionChangeEvent.Kind.USER_CLEARED, "user:" + user, "", user);
    }

    // ---- changing groups

    public boolean createGroup(CommandSender actor, String name) {
        if (!data.createGroup(name)) return false;
        changed(actor, "GROUP_CREATE", "group:" + PermissionData.normalizeGroup(name), "", PermissionChangeEvent.Kind.GROUP_CREATED,
                "group:" + PermissionData.normalizeGroup(name), "", null);
        return true;
    }

    public boolean deleteGroup(CommandSender actor, String name) {
        if (!data.deleteGroup(name)) return false;
        changed(actor, "GROUP_DELETE", "group:" + PermissionData.normalizeGroup(name), "", PermissionChangeEvent.Kind.GROUP_DELETED,
                "group:" + PermissionData.normalizeGroup(name), "", null);
        return true;
    }

    public boolean setGroupWeight(CommandSender actor, String name, int weight) {
        return groupChanged(actor, name, "weight " + weight, data.setWeight(name, weight));
    }

    public boolean setGroupPrefix(CommandSender actor, String name, String prefix) {
        return groupChanged(actor, name, "prefix '" + prefix + "'", data.setPrefix(name, prefix));
    }

    public boolean setGroupSuffix(CommandSender actor, String name, String suffix) {
        return groupChanged(actor, name, "suffix '" + suffix + "'", data.setSuffix(name, suffix));
    }

    public boolean setGroupDisplayName(CommandSender actor, String name, String displayName) {
        return groupChanged(actor, name, "display name '" + displayName + "'", data.setDisplayName(name, displayName));
    }

    private boolean groupChanged(CommandSender actor, String name, String details, boolean done) {
        if (!done) return false;
        changed(actor, "GROUP_UPDATE", "group:" + PermissionData.normalizeGroup(name), details, PermissionChangeEvent.Kind.GROUP_CHANGED,
                "group:" + PermissionData.normalizeGroup(name), details, null);
        return true;
    }

    /** @return {@code false} if the group does not exist or a parent entry would make a cycle */
    public boolean setGroupPermission(CommandSender actor, String group, String permission, boolean value, long expiresAt, Map<String, String> context) {
        Node node = new Node(permission, value, expiresAt, context);
        if (!data.setGroupNode(group, node)) return false;
        String name = PermissionData.normalizeGroup(group);
        changed(actor, node.isGroup() ? "GROUP_PARENT_ADD" : "PERMISSION_SET", "group:" + name, describe(node),
                node.isGroup() ? PermissionChangeEvent.Kind.GROUP_ADDED : PermissionChangeEvent.Kind.SET, "group:" + name, node.permission(), null);
        return true;
    }

    public boolean unsetGroupPermission(CommandSender actor, String group, String permission, Map<String, String> context) {
        if (!data.unsetGroupNode(group, permission, context)) return false;
        String name = PermissionData.normalizeGroup(group);
        boolean parent = permission.toLowerCase().startsWith(Node.GROUP_PREFIX);
        changed(actor, parent ? "GROUP_PARENT_REMOVE" : "PERMISSION_UNSET", "group:" + name, permission + contextSuffix(context),
                parent ? PermissionChangeEvent.Kind.GROUP_REMOVED : PermissionChangeEvent.Kind.UNSET, "group:" + name, permission, null);
        return true;
    }

    public boolean addGroupParent(CommandSender actor, String group, String parent) {
        return data.group(parent) != null && setGroupPermission(actor, group, Node.GROUP_PREFIX + PermissionData.normalizeGroup(parent), true, 0, Map.of());
    }

    public boolean removeGroupParent(CommandSender actor, String group, String parent) {
        return unsetGroupPermission(actor, group, Node.GROUP_PREFIX + PermissionData.normalizeGroup(parent), Map.of());
    }

    // ---- applying

    private void changed(CommandSender actor, String action, String target, String details, PermissionChangeEvent.Kind kind, String subject,
                         String detail, UUID user) {
        AdminJournal.log(actor, action, target, details);
        Runnable follow = () -> {
            if (user != null) {
                Player online = Bukkit.getPlayer(user);
                if (online != null) apply(online);
            } else {
                applyAll();
            }
            Bukkit.getPluginManager().callEvent(new PermissionChangeEvent(kind, subject, detail, actor));
        };
        if (Bukkit.isPrimaryThread()) follow.run();
        else Bukkit.getScheduler().runTask(plugin, follow);
    }

    private static String describe(Node node) {
        return node.permission() + ": " + node.value() + (node.isTemporary() ? " until " + java.time.Instant.ofEpochMilli(node.expiresAt()) : "")
                + contextSuffix(node.context());
    }

    private static String contextSuffix(Map<String, String> context) {
        return context == null || context.isEmpty() ? "" : " [" + Node.formatContext(context) + "]";
    }

    public void onJoin(Player player) {
        apply(player);
    }

    public void onQuit(Player player) {
        UUID id = player.getUniqueId();
        PermissionAttachment attachment = attachments.remove(id);
        if (attachment != null) {
            try {
                player.removeAttachment(attachment);
            } catch (IllegalArgumentException ignored) {
                // already removed
            }
        }
        resolutions.remove(id);
        data.forget(id);
    }

    public void onWorldChange(Player player) {
        if (plugin.getConfig().getBoolean("Permissions.WorldChange", true)) apply(player);
    }

    /** Recalculates every player that is online. */
    public void applyAll() {
        for (Player online : Bukkit.getOnlinePlayers()) apply(online);
    }

    /** Works out the permissions of a player again and puts them on a fresh attachment. Call it on the main thread. */
    public void apply(Player player) {
        if (player == null || !ready) return;
        UUID id = player.getUniqueId();
        try {
            Resolution resolution = data.resolve(id, contextOf(player));
            Map<String, Boolean> flat = resolution.expand(knownPermissions());
            PermissionAttachment old = attachments.remove(id);
            if (old != null) {
                try {
                    player.removeAttachment(old);
                } catch (IllegalArgumentException ignored) {
                    // already removed
                }
            }
            PermissionAttachment attachment = player.addAttachment(plugin);
            fill(attachment, flat);
            attachments.put(id, attachment);
            resolutions.put(id, resolution);
            for (String problem : resolution.problems()) {
                plugin.getFileLogger().writeToLog(Level.WARNING, "Permissions of " + player.getName() + ": " + problem, LogPrefixExtension.PERMISSIONS);
            }
            player.recalculatePermissions();
            player.updateCommands();
        } catch (RuntimeException e) {
            AdminPanelMain.logError("Could not apply the permissions of " + player.getName(), e);
        }
    }

    private static Collection<String> knownPermissions() {
        Set<String> names = new HashSet<>();
        for (Permission permission : Bukkit.getPluginManager().getPermissions()) names.add(permission.getName());
        return names;
    }

    private static Field attachmentMap;
    private static boolean attachmentMapChecked;

    /**
     * Puts the permissions on the attachment. {@code setPermission} recalculates the player each time, which is slow for hundreds of
     * entries, so the map of the attachment is filled directly when that is possible.
     */
    private static void fill(PermissionAttachment attachment, Map<String, Boolean> flat) {
        if (!attachmentMapChecked) {
            attachmentMapChecked = true;
            try {
                attachmentMap = PermissionAttachment.class.getDeclaredField("permissions");
                attachmentMap.setAccessible(true);
            } catch (ReflectiveOperationException | RuntimeException e) {
                attachmentMap = null;
            }
        }
        if (attachmentMap != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Boolean> target = (Map<String, Boolean>) attachmentMap.get(attachment);
                target.putAll(flat);
                return;
            } catch (ReflectiveOperationException | RuntimeException e) {
                attachmentMap = null;
            }
        }
        flat.forEach(attachment::setPermission);
    }

    // ---- export and import

    /** Writes all groups and users to a YAML file. Reads the database, so call it off the main thread. */
    public int exportTo(java.io.File file) throws java.io.IOException {
        org.bukkit.configuration.file.YamlConfiguration yaml = PermissionTransfer.export(data);
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
        yaml.save(file);
        return yaml.getConfigurationSection("users") == null ? 0 : yaml.getConfigurationSection("users").getKeys(false).size();
    }

    /** Adds the groups and users of a YAML file written by {@link #exportTo}. Call it off the main thread. */
    public PermissionTransfer.Result importFrom(CommandSender actor, java.io.File file) throws java.io.IOException,
            org.bukkit.configuration.InvalidConfigurationException {
        org.bukkit.configuration.file.YamlConfiguration yaml = new org.bukkit.configuration.file.YamlConfiguration();
        yaml.load(file);
        PermissionTransfer.Result result = PermissionTransfer.load(data, yaml);
        AdminJournal.log(actor, "PERMISSION_IMPORT", file.getName(), result.groups() + " groups, " + result.users() + " users, "
                + result.entries() + " entries");
        Bukkit.getScheduler().runTask(plugin, () -> {
            applyAll();
            Bukkit.getPluginManager().callEvent(new PermissionChangeEvent(PermissionChangeEvent.Kind.RELOADED, "import", file.getName(), actor));
        });
        return result;
    }

    // ---- sweep and reload

    /** Removes ended entries from memory and the database (runs on an async thread) and applies the affected players on the main thread. */
    public void sweepNow() {
        if (!ready) return;
        List<PermissionStore.Expired> removed;
        try {
            removed = data.sweep();
        } catch (RuntimeException e) {
            AdminPanelMain.logError("Could not remove ended permissions", e);
            return;
        }
        if (removed.isEmpty()) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Set<UUID> users = new HashSet<>();
            boolean groupsChanged = false;
            for (PermissionStore.Expired entry : removed) {
                AdminJournal.log("SYSTEM", "PERMISSION_EXPIRE", entry.owner().startsWith("user:") ? entry.owner().substring(5) : entry.owner(),
                        describe(entry.node()));
                if (entry.owner().startsWith("user:")) {
                    try {
                        users.add(UUID.fromString(entry.owner().substring(5)));
                    } catch (IllegalArgumentException ignored) {
                        // not a uuid
                    }
                } else {
                    groupsChanged = true;
                }
                Bukkit.getPluginManager().callEvent(new PermissionChangeEvent(PermissionChangeEvent.Kind.EXPIRED, entry.owner(),
                        entry.node().permission(), null));
            }
            if (groupsChanged) {
                applyAll();
            } else {
                for (UUID user : users) {
                    Player online = Bukkit.getPlayer(user);
                    if (online != null) apply(online);
                }
            }
        });
    }

    /** Reads the groups again from the database and applies everyone who is online. */
    public void reload(CommandSender actor) {
        io.execute(() -> {
            try {
                data.load();
            } catch (RuntimeException e) {
                AdminPanelMain.logError("Could not reload the permission groups", e);
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                applyAll();
                Bukkit.getPluginManager().callEvent(new PermissionChangeEvent(PermissionChangeEvent.Kind.RELOADED, "", "", actor));
            });
        });
    }
}
