package de.happybavarian07.adminpanel.commands.subcommands.perms;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.data.AdminAction;
import de.happybavarian07.adminpanel.journal.AdminJournal;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.permissionmanager.PermissionHubMenu;
import de.happybavarian07.adminpanel.permissions.NodeInput;
import de.happybavarian07.adminpanel.permissions.PermissionService;
import de.happybavarian07.adminpanel.permissions.PermissionTransfer;
import de.happybavarian07.adminpanel.permissions.engine.GroupData;
import de.happybavarian07.adminpanel.permissions.engine.Node;
import de.happybavarian07.adminpanel.permissions.engine.Resolution;
import de.happybavarian07.adminpanel.sanctions.Durations;
import de.happybavarian07.adminpanel.utils.tfidfsearch.TFIDFSearch;
import de.happybavarian07.coolstufflib.CoolStuffLib;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.ArgumentCompletionContext;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * The sub commands of {@code /perms}. Each one is a {@link Verb}; the grammar is
 * <pre>
 * /perms user  &lt;player&gt; info|set|unset|addgroup|removegroup|clear|check|explain ...
 * /perms group &lt;name&gt; create|delete|info|set|unset|parent|weight|prefix|suffix|displayname|members ...
 * </pre>
 * where {@code set} takes the permission, then in any order {@code true|false}, a duration such as {@code 7d} and {@code world=nether}.
 */
public final class PermsCommands {
    private static final String KEY = "Permissions.Command.";
    private static final String BASE = "AdminPanel.Permissions.";
    private static final int ARGUMENTS = 6;

    private PermsCommands() {
    }

    @FunctionalInterface
    private interface Handler {
        void run(Verb verb, CommandSender sender, CommandArgs args) throws Exception;
    }

    private static final class Verb extends SubCommand {
        private final String name;
        private final String info;
        private final String permission;
        private final List<String> argumentNames;
        private final Handler handler;

        Verb(String mainCommandName, String name, String info, String permission, List<String> argumentNames, Handler handler) {
            super(mainCommandName);
            this.name = name;
            this.info = info;
            this.permission = permission;
            this.argumentNames = argumentNames;
            this.handler = handler;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String info() {
            return info;
        }

        @Override
        public String permissionAsString() {
            return permission;
        }

        @Override
        public boolean autoRegisterPermission() {
            return false;
        }

        @Override
        protected String defaultSyntax() {
            StringBuilder syntax = new StringBuilder("/").append(mainCommandName).append(' ').append(name);
            for (String argument : argumentNames) syntax.append(' ').append(argument);
            return syntax.toString();
        }

        @Override
        public List<Argument> arguments() {
            List<Argument> arguments = new ArrayList<>();
            for (int i = 0; i < argumentNames.size() && i < ARGUMENTS; i++) {
                int position = i;
                String argumentName = argumentNames.get(i).replaceAll("[<>\\[\\].]", "");
                Argument argument = i == argumentNames.size() - 1 && argumentNames.get(i).endsWith("...]")
                        ? Argument.text(argumentName)
                        : Argument.contextualChoice(argumentName, (ArgumentCompletionContext context) -> suggest(name, position, context));
                arguments.add(argument.optional());
            }
            return arguments;
        }

        @Override
        public boolean execute(CommandSender sender, CommandArgs args) {
            try {
                handler.run(this, sender, args);
            } catch (IllegalArgumentException e) {
                CommandText.send(lgm, sender, KEY + "BadArgument", "%error%", String.valueOf(e.getMessage()));
            } catch (Exception e) {
                AdminPanelMain.logError("/perms " + name + " failed", e);
                CommandText.send(lgm, sender, KEY + "Error", "%error%", String.valueOf(e.getMessage()));
            }
            return true;
        }

        void send(CommandSender sender, String key, Object... placeholders) {
            CommandText.send(lgm, sender, KEY + key, placeholders);
        }

        String text(CommandSender sender, String key, Object... placeholders) {
            return CommandText.get(lgm, sender, KEY + key, placeholders);
        }

        void usage(CommandSender sender, String syntax) {
            send(sender, "Usage", "%usage%", syntax);
        }
    }

    /** @return the sub commands in the order they are listed in the help */
    public static List<SubCommand> all(String main) {
        List<String> userArguments = List.of("<player>", "<action>", "[permission|group]", "[value|duration]", "[world=<name>]", "[...]");
        List<String> groupArguments = List.of("<group>", "<action>", "[permission|group]", "[value|duration]", "[world=<name>]", "[...]");
        return List.of(
                new Verb(main, "user", "Shows and changes the permissions and groups of a player", BASE + "User", userArguments, PermsCommands::user),
                new Verb(main, "group", "Shows and changes a group", BASE + "Group", groupArguments, PermsCommands::group),
                new Verb(main, "groups", "Lists the groups", BASE + "Group", List.of(), PermsCommands::groups),
                new Verb(main, "check", "Says whether a player has a permission", BASE + "Check", List.of("<player>", "<permission>"),
                        (verb, sender, args) -> check(verb, sender, args, 0, false)),
                new Verb(main, "explain", "Shows where the answer for a permission comes from", BASE + "Check",
                        List.of("<player>", "<permission>"), (verb, sender, args) -> check(verb, sender, args, 0, true)),
                new Verb(main, "search", "Searches the permissions of all plugins", BASE + "Search", List.of("<text...>"), PermsCommands::search),
                new Verb(main, "audit", "Shows the permission changes from the journal", BASE + "Audit", List.of("[player|group]"), PermsCommands::audit),
                new Verb(main, "export", "Writes all groups and permissions to a file", BASE + "Export", List.of("[file]"), PermsCommands::export),
                new Verb(main, "import", "Reads groups and permissions from a file", BASE + "Import", List.of("<file>"), PermsCommands::importFile),
                new Verb(main, "reload", "Reads the groups again and applies everyone", BASE + "Reload", List.of(), PermsCommands::reload),
                new Verb(main, "menu", "Opens the permission manager menu", BASE + "Menu", List.of(), PermsCommands::menu));
    }

    // ---- completion

    private static Collection<String> suggest(String verb, int position, ArgumentCompletionContext context) {
        PermissionService service = service();
        String[] typed = context.rawArguments();
        String action = typed.length > 1 ? typed[1].toLowerCase(Locale.ROOT) : "";
        if (position == 0) {
            if (verb.equals("group") && service != null) return service.groups().stream().map(GroupData::name).toList();
            return playerNames();
        }
        boolean isUser = verb.equals("user");
        if (verb.equals("check") || verb.equals("explain")) return position == 1 ? permissionNames() : List.of();
        if (!isUser && !verb.equals("group")) return List.of();
        if (position == 1) {
            return isUser
                    ? List.of("info", "set", "unset", "addgroup", "removegroup", "clear", "check", "explain")
                    : List.of("create", "delete", "info", "set", "unset", "parent", "weight", "prefix", "suffix", "displayname", "members");
        }
        if (position == 2) {
            return switch (action) {
                case "set", "unset", "check", "explain" -> permissionNames();
                case "addgroup", "removegroup", "parent" -> action.equals("parent") ? List.of("add", "remove") : groupNames(service);
                default -> List.of();
            };
        }
        if (action.equals("parent") && position == 3) return groupNames(service);
        if (action.equals("set") || action.equals("addgroup") || action.equals("parent")) {
            List<String> options = new ArrayList<>(List.of("permanent", "1h", "1d", "7d", "30d"));
            if (action.equals("set")) options.addAll(List.of("true", "false"));
            Bukkit.getWorlds().forEach(world -> options.add("world=" + world.getName()));
            return options;
        }
        if (action.equals("unset") || action.equals("removegroup")) {
            return Bukkit.getWorlds().stream().map(world -> "world=" + world.getName()).toList();
        }
        return List.of();
    }

    private static Collection<String> groupNames(PermissionService service) {
        return service == null ? List.of() : service.groups().stream().map(GroupData::name).toList();
    }

    private static Collection<String> playerNames() {
        return Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).filter(Objects::nonNull).distinct().toList();
    }

    private static Collection<String> permissionNames() {
        return Bukkit.getPluginManager().getPermissions().stream().map(Permission::getName).sorted().toList();
    }

    // ---- shared helpers

    private static PermissionService service() {
        AdminPanelMain plugin = AdminPanelMain.getPlugin();
        return plugin == null || plugin.getPermissionsManager() == null ? null : plugin.getPermissionService();
    }

    private static PermissionService ready(Verb verb, CommandSender sender) {
        PermissionService service = service();
        if (service == null || !service.isReady()) {
            verb.send(sender, "NotReady");
            return null;
        }
        return service;
    }

    private static boolean mayEdit(Verb verb, CommandSender sender, String node) {
        if (sender.hasPermission(BASE + node + ".Edit")) return true;
        verb.send(sender, "NoEditPermission");
        return false;
    }

    private static OfflinePlayer target(Verb verb, CommandSender sender, String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online;
        for (OfflinePlayer known : Bukkit.getOfflinePlayers()) {
            if (name.equalsIgnoreCase(known.getName())) return known;
        }
        verb.send(sender, "PlayerNotFound", "%target%", name);
        return null;
    }

    /** The value, end and context typed after a permission or group name. */
    private record Options(boolean value, long expiresAt, Map<String, String> context) {
        static Options parse(String[] raw, int from) {
            Node node = NodeInput.parse("x", raw, from, System.currentTimeMillis());
            return new Options(node.value(), node.expiresAt(), node.context());
        }
    }

    private static String describe(Verb verb, CommandSender sender, Node node) {
        StringBuilder text = new StringBuilder(node.value() ? "&a" : "&c").append(node.permission());
        if (node.isTemporary()) {
            text.append(" &e(").append(verb.text(sender, "Remaining", "%time%",
                    Durations.format(node.expiresAt() - System.currentTimeMillis()))).append(")");
        }
        if (!node.context().isEmpty()) text.append(" &7[").append(node.contextText()).append("]");
        return text.toString();
    }

    // ---- user

    private static void user(Verb verb, CommandSender sender, CommandArgs args) {
        PermissionService service = ready(verb, sender);
        if (service == null) return;
        if (!args.has(1)) {
            verb.usage(sender, "/perms user <player> <info|set|unset|addgroup|removegroup|clear|check|explain>");
            return;
        }
        OfflinePlayer target = target(verb, sender, args.string(0));
        if (target == null) return;
        UUID id = target.getUniqueId();
        String name = String.valueOf(target.getName());
        String action = args.string(1).toLowerCase(Locale.ROOT);
        String[] raw = args.raw();
        switch (action) {
            case "info" -> userInfo(verb, sender, service, id, name);
            case "check", "explain" -> {
                if (!args.has(2)) {
                    verb.usage(sender, "/perms user <player> " + action + " <permission>");
                    return;
                }
                showCheck(verb, sender, service, id, name, args.string(2), action.equals("explain"));
            }
            case "set" -> {
                if (!mayEdit(verb, sender, "User")) return;
                if (!args.has(2)) {
                    verb.usage(sender, "/perms user <player> set <permission> [true|false] [duration] [world=<name>]");
                    return;
                }
                Options options = Options.parse(raw, 3);
                service.setUserPermission(sender, id, args.string(2), options.value(), options.expiresAt(), options.context());
                verb.send(sender, "Set", "%permission%", args.string(2).toLowerCase(Locale.ROOT), "%value%", options.value(), "%target%", name,
                        "%extra%", extra(verb, sender, options));
            }
            case "unset" -> {
                if (!mayEdit(verb, sender, "User")) return;
                if (!args.has(2)) {
                    verb.usage(sender, "/perms user <player> unset <permission> [world=<name>]");
                    return;
                }
                boolean removed = service.unsetUserPermission(sender, id, args.string(2), Options.parse(raw, 3).context());
                verb.send(sender, removed ? "Unset" : "UnsetMissing", "%permission%", args.string(2), "%target%", name);
            }
            case "addgroup" -> {
                if (!mayEdit(verb, sender, "User")) return;
                if (!args.has(2)) {
                    verb.usage(sender, "/perms user <player> addgroup <group> [duration] [world=<name>]");
                    return;
                }
                Options options = Options.parse(raw, 3);
                if (!service.addUserGroup(sender, id, args.string(2), options.expiresAt(), options.context())) {
                    verb.send(sender, "GroupNotFound", "%group%", args.string(2));
                    return;
                }
                verb.send(sender, "GroupAdded", "%group%", args.string(2).toLowerCase(Locale.ROOT), "%target%", name,
                        "%extra%", extra(verb, sender, options));
            }
            case "removegroup" -> {
                if (!mayEdit(verb, sender, "User")) return;
                if (!args.has(2)) {
                    verb.usage(sender, "/perms user <player> removegroup <group> [world=<name>]");
                    return;
                }
                boolean removed = service.removeUserGroup(sender, id, args.string(2), Options.parse(raw, 3).context());
                verb.send(sender, removed ? "GroupRemoved" : "GroupNotMember", "%group%", args.string(2), "%target%", name);
            }
            case "clear" -> {
                if (!mayEdit(verb, sender, "User")) return;
                service.clearUser(sender, id);
                verb.send(sender, "Cleared", "%target%", name);
            }
            default -> verb.usage(sender, "/perms user <player> <info|set|unset|addgroup|removegroup|clear|check|explain>");
        }
    }

    private static String extra(Verb verb, CommandSender sender, Options options) {
        StringBuilder text = new StringBuilder();
        if (options.expiresAt() > 0) {
            text.append(" &7for &e").append(Durations.format(options.expiresAt() - System.currentTimeMillis()));
        }
        if (!options.context().isEmpty()) text.append(" &7in &e").append(Node.formatContext(options.context()));
        return text.toString();
    }

    private static void userInfo(Verb verb, CommandSender sender, PermissionService service, UUID id, String name) {
        long now = System.currentTimeMillis();
        List<Node> nodes = service.nodesOf(id).stream().filter(node -> !node.expired(now)).toList();
        Resolution resolution = service.resolve(id);
        verb.send(sender, "UserHeader", "%target%", name, "%groups%", resolution.groups().isEmpty() ? "-" : String.join(", ", resolution.groups()),
                "%prefix%", resolution.prefix().isEmpty() ? "-" : resolution.prefix(), "%suffix%", resolution.suffix().isEmpty() ? "-" : resolution.suffix());
        if (nodes.isEmpty()) {
            verb.send(sender, "NoEntries");
        }
        for (Node node : nodes) {
            verb.send(sender, node.isGroup() ? "GroupLine" : "EntryLine", "%entry%", describe(verb, sender, node));
        }
        for (String problem : resolution.problems()) verb.send(sender, "Problem", "%problem%", problem);
    }

    private static void check(Verb verb, CommandSender sender, CommandArgs args, int from, boolean explain) {
        PermissionService service = ready(verb, sender);
        if (service == null) return;
        if (!args.has(from + 1)) {
            verb.usage(sender, "/perms " + (explain ? "explain" : "check") + " <player> <permission>");
            return;
        }
        OfflinePlayer target = target(verb, sender, args.string(from));
        if (target == null) return;
        showCheck(verb, sender, service, target.getUniqueId(), String.valueOf(target.getName()), args.string(from + 1), explain);
    }

    private static void showCheck(Verb verb, CommandSender sender, PermissionService service, UUID id, String name, String permission,
                                  boolean explain) {
        Resolution resolution = service.resolve(id);
        Boolean result = resolution.check(permission);
        String answer = result == null ? "&7not set" : result ? "&atrue" : "&cfalse";
        verb.send(sender, "CheckResult", "%target%", name, "%permission%", permission.toLowerCase(Locale.ROOT), "%result%", answer);
        if (!explain) return;
        Player online = Bukkit.getPlayer(id);
        if (online != null) {
            verb.send(sender, "ExplainBukkit", "%result%", online.hasPermission(permission) ? "&atrue" : "&cfalse");
        }
        List<Resolution.Step> steps = resolution.explain(permission);
        if (steps.isEmpty()) {
            verb.send(sender, "ExplainNone");
            return;
        }
        for (Resolution.Step step : steps) {
            verb.send(sender, "ExplainLine", "%mark%", step.decisive() ? "&a>" : "&8-", "%source%", step.source(),
                    "%entry%", (step.value() ? "&a" : "&c") + step.permission() + (step.expiresAt() > 0 ? " &e(temporary)" : ""));
        }
    }

    // ---- group

    private static void groups(Verb verb, CommandSender sender, CommandArgs args) {
        PermissionService service = ready(verb, sender);
        if (service == null) return;
        verb.send(sender, "GroupsHeader", "%count%", service.groups().size());
        for (GroupData group : service.groups()) {
            verb.send(sender, "GroupsLine", "%group%", group.name(), "%weight%", group.weight(), "%entries%", group.nodes().size());
        }
    }

    private static void group(Verb verb, CommandSender sender, CommandArgs args) {
        PermissionService service = ready(verb, sender);
        if (service == null) return;
        if (!args.has(1)) {
            verb.usage(sender, "/perms group <group> <create|delete|info|set|unset|parent|weight|prefix|suffix|displayname|members>");
            return;
        }
        String group = args.string(0).toLowerCase(Locale.ROOT);
        String action = args.string(1).toLowerCase(Locale.ROOT);
        String[] raw = args.raw();
        boolean edit = !Set.of("info", "members").contains(action);
        if (edit && !mayEdit(verb, sender, "Group")) return;
        if (!action.equals("create") && service.group(group) == null) {
            verb.send(sender, "GroupNotFound", "%group%", group);
            return;
        }
        switch (action) {
            case "create" -> verb.send(sender, service.createGroup(sender, group) ? "GroupCreated" : "GroupCreateFailed", "%group%", group);
            case "delete" -> verb.send(sender, service.deleteGroup(sender, group) ? "GroupDeleted" : "GroupDeleteFailed", "%group%", group);
            case "info" -> groupInfo(verb, sender, service, group);
            case "members" -> {
                Map<UUID, Node> members = service.data().loadedMembers(group);
                verb.send(sender, "MembersHeader", "%group%", group, "%count%", members.size());
                members.forEach((id, node) -> verb.send(sender, "MembersLine", "%member%", service.nameOf(id), "%entry%", describe(verb, sender, node)));
            }
            case "set" -> {
                if (!args.has(2)) {
                    verb.usage(sender, "/perms group <group> set <permission> [true|false] [duration] [world=<name>]");
                    return;
                }
                Options options = Options.parse(raw, 3);
                if (!service.setGroupPermission(sender, group, args.string(2), options.value(), options.expiresAt(), options.context())) {
                    verb.send(sender, "GroupCycle", "%group%", group);
                    return;
                }
                verb.send(sender, "Set", "%permission%", args.string(2).toLowerCase(Locale.ROOT), "%value%", options.value(), "%target%", group,
                        "%extra%", extra(verb, sender, options));
            }
            case "unset" -> {
                if (!args.has(2)) {
                    verb.usage(sender, "/perms group <group> unset <permission> [world=<name>]");
                    return;
                }
                boolean removed = service.unsetGroupPermission(sender, group, args.string(2), Options.parse(raw, 3).context());
                verb.send(sender, removed ? "Unset" : "UnsetMissing", "%permission%", args.string(2), "%target%", group);
            }
            case "parent" -> parent(verb, sender, service, group, args);
            case "weight" -> {
                int weight = args.integer(2);
                service.setGroupWeight(sender, group, weight);
                verb.send(sender, "WeightSet", "%group%", group, "%weight%", weight);
            }
            case "prefix", "suffix", "displayname" -> {
                String text = args.has(2) ? args.joined(2) : "";
                switch (action) {
                    case "prefix" -> service.setGroupPrefix(sender, group, text);
                    case "suffix" -> service.setGroupSuffix(sender, group, text);
                    default -> service.setGroupDisplayName(sender, group, text);
                }
                verb.send(sender, "MetaSet", "%group%", group, "%kind%", action, "%text%", text.isEmpty() ? "-" : text);
            }
            default -> verb.usage(sender, "/perms group <group> <create|delete|info|set|unset|parent|weight|prefix|suffix|displayname|members>");
        }
    }

    private static void parent(Verb verb, CommandSender sender, PermissionService service, String group, CommandArgs args) {
        if (!args.has(3)) {
            verb.usage(sender, "/perms group <group> parent <add|remove> <parent>");
            return;
        }
        String mode = args.string(2).toLowerCase(Locale.ROOT);
        String parent = args.string(3).toLowerCase(Locale.ROOT);
        if (mode.equals("add")) {
            if (service.group(parent) == null) {
                verb.send(sender, "GroupNotFound", "%group%", parent);
            } else {
                verb.send(sender, service.addGroupParent(sender, group, parent) ? "ParentAdded" : "GroupCycle", "%group%", group, "%parent%", parent);
            }
        } else if (mode.equals("remove")) {
            verb.send(sender, service.removeGroupParent(sender, group, parent) ? "ParentRemoved" : "ParentMissing", "%group%", group, "%parent%", parent);
        } else {
            verb.usage(sender, "/perms group <group> parent <add|remove> <parent>");
        }
    }

    private static void groupInfo(Verb verb, CommandSender sender, PermissionService service, String name) {
        GroupData group = service.group(name);
        long now = System.currentTimeMillis();
        verb.send(sender, "GroupHeader", "%group%", group.name(), "%weight%", group.weight(),
                "%display%", group.displayName().isEmpty() ? "-" : group.displayName(),
                "%prefix%", group.prefix().isEmpty() ? "-" : group.prefix(), "%suffix%", group.suffix().isEmpty() ? "-" : group.suffix());
        List<Node> nodes = group.nodes().stream().filter(node -> !node.expired(now)).toList();
        if (nodes.isEmpty()) verb.send(sender, "NoEntries");
        for (Node node : nodes) verb.send(sender, node.isGroup() ? "ParentLine" : "EntryLine", "%entry%", describe(verb, sender, node));
    }

    // ---- search, audit, export, import, reload, menu

    private static void search(Verb verb, CommandSender sender, CommandArgs args) throws Exception {
        if (!args.has(0)) {
            verb.usage(sender, "/perms search <text>");
            return;
        }
        String query = args.joined(0);
        Bukkit.getScheduler().runTaskAsynchronously(AdminPanelMain.getPlugin(), () -> {
            try {
                List<TFIDFSearch.Item> hits = AdminPanelMain.getPlugin().getPermissionsManager().getPermissionSearcher().search(query);
                if (hits.isEmpty()) {
                    verb.send(sender, "SearchEmpty", "%query%", query);
                    return;
                }
                verb.send(sender, "SearchHeader", "%query%", query, "%count%", hits.size());
                for (TFIDFSearch.Item hit : hits.stream().limit(15).toList()) {
                    String description = hit.getFieldValue("permissionDescription");
                    verb.send(sender, "SearchLine", "%permission%", hit.getFieldValue("permissionName"),
                            "%description%", description == null || description.isBlank() ? "" : " &7- " + description);
                }
            } catch (Exception e) {
                AdminPanelMain.logError("Could not search the permissions", e);
                verb.send(sender, "Error", "%error%", String.valueOf(e.getMessage()));
            }
        });
    }

    private static void audit(Verb verb, CommandSender sender, CommandArgs args) {
        AdminJournal journal = AdminPanelMain.getPlugin().getJournal();
        if (journal == null) {
            verb.send(sender, "JournalOff");
            return;
        }
        String subject = args.has(0) ? args.string(0).toLowerCase(Locale.ROOT) : "";
        Bukkit.getScheduler().runTaskAsynchronously(AdminPanelMain.getPlugin(), () -> {
            List<AdminAction> rows = journal.recent(row -> AdminJournal.category(row.getAction()).equals("Permissions")
                    && (subject.isEmpty() || row.getTarget().toLowerCase(Locale.ROOT).contains(subject)
                    || row.getActor().toLowerCase(Locale.ROOT).contains(subject)), 15);
            if (rows.isEmpty()) {
                verb.send(sender, "AuditEmpty");
                return;
            }
            verb.send(sender, "AuditHeader", "%count%", rows.size());
            for (AdminAction row : rows) {
                verb.send(sender, "AuditLine", "%time%", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date(row.getCreatedAt())),
                        "%actor%", row.getActor(), "%action%", row.getAction(), "%target%", row.getTarget(), "%details%", row.getDetails());
            }
        });
    }

    private static File exchangeFile(String name) {
        String safe = name.replaceAll("[^A-Za-z0-9._-]", "_");
        if (!safe.endsWith(".yml")) safe += ".yml";
        return new File(new File(AdminPanelMain.getPlugin().getDataFolder(), "permission-exports"), safe);
    }

    private static void export(Verb verb, CommandSender sender, CommandArgs args) {
        PermissionService service = ready(verb, sender);
        if (service == null) return;
        File file = exchangeFile(args.has(0) ? args.string(0)
                : "permissions-" + new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date()));
        Bukkit.getScheduler().runTaskAsynchronously(AdminPanelMain.getPlugin(), () -> {
            try {
                int users = service.exportTo(file);
                AdminJournal.log(sender, "PERMISSION_EXPORT", file.getName(), users + " users");
                verb.send(sender, "Exported", "%file%", file.getName(), "%users%", users, "%groups%", service.groups().size());
            } catch (Exception e) {
                AdminPanelMain.logError("Could not export the permissions", e);
                verb.send(sender, "ExportFailed", "%error%", String.valueOf(e.getMessage()));
            }
        });
    }

    private static void importFile(Verb verb, CommandSender sender, CommandArgs args) {
        PermissionService service = ready(verb, sender);
        if (service == null) return;
        if (!args.has(0)) {
            verb.usage(sender, "/perms import <file>");
            return;
        }
        File file = exchangeFile(args.string(0));
        if (!file.isFile()) {
            verb.send(sender, "ImportMissing", "%file%", file.getName());
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(AdminPanelMain.getPlugin(), () -> {
            try {
                PermissionTransfer.Result result = service.importFrom(sender, file);
                verb.send(sender, "Imported", "%file%", file.getName(), "%groups%", result.groups(), "%users%", result.users(),
                        "%entries%", result.entries());
                for (String problem : result.problems()) verb.send(sender, "Problem", "%problem%", problem);
            } catch (Exception e) {
                AdminPanelMain.logError("Could not import the permissions", e);
                verb.send(sender, "ImportFailed", "%error%", String.valueOf(e.getMessage()));
            }
        });
    }

    private static void reload(Verb verb, CommandSender sender, CommandArgs args) {
        PermissionService service = ready(verb, sender);
        if (service == null) return;
        service.reload(sender);
        verb.send(sender, "Reloaded");
    }

    private static void menu(Verb verb, CommandSender sender, CommandArgs args) {
        if (!(sender instanceof Player player)) {
            verb.send(sender, "PlayerOnly");
            return;
        }
        if (ready(verb, sender) == null) return;
        new PermissionHubMenu(CoolStuffLib.getLib().getPlayerMenuUtility(player.getUniqueId())).open();
    }
}
