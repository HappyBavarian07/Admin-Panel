package de.happybavarian07.adminpanel.permissions.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Works out what a user may do. The user's own entries come first, then the groups the user is in by weight with their parents after
 * them, then the {@code default} group. The first source that defines a permission exactly decides; if none does, wildcards decide from
 * the most specific one to {@code *}. See {@link Resolution}.
 */
public final class Resolver {
    public static final String DEFAULT_GROUP = "default";

    private Resolver() {
    }

    /**
     * @param userNodes     the entries of the user, including {@code group.<name>} memberships
     * @param groups        finds a group by name, {@code null} if there is none
     * @param context       the current context, for example {@code world -> nether}
     * @param now           the current time in milliseconds, entries that ended before are ignored
     * @param includeDefault whether every user also inherits the {@code default} group
     */
    public static Resolution resolve(List<Node> userNodes, Function<String, GroupData> groups, Map<String, String> context, long now,
                                     boolean includeDefault) {
        List<Resolution.Source> sources = new ArrayList<>();
        List<String> problems = new ArrayList<>();
        List<Meta> prefixes = new ArrayList<>();
        List<Meta> suffixes = new ArrayList<>();

        List<Node> own = applicable(userNodes, context, now);
        sources.add(new Resolution.Source("user", true, Integer.MAX_VALUE, withoutGroups(own)));
        collectMeta(own, 0, prefixes, suffixes);

        Set<String> visited = new LinkedHashSet<>();
        for (String name : memberships(own, groups)) walk(name, new ArrayList<>(), visited, groups, context, now, sources, problems, prefixes, suffixes);
        if (includeDefault && !visited.contains(DEFAULT_GROUP) && groups.apply(DEFAULT_GROUP) != null) {
            walk(DEFAULT_GROUP, new ArrayList<>(), visited, groups, context, now, sources, problems, prefixes, suffixes);
        }
        return new Resolution(sources, problems, best(prefixes), best(suffixes));
    }

    private static void walk(String name, List<String> path, Set<String> visited, Function<String, GroupData> groups, Map<String, String> context,
                             long now, List<Resolution.Source> sources, List<String> problems, List<Meta> prefixes, List<Meta> suffixes) {
        if (path.contains(name)) {
            problems.add("group cycle: " + String.join(" -> ", path) + " -> " + name);
            return;
        }
        if (visited.contains(name)) return;
        GroupData group = groups.apply(name);
        if (group == null) {
            problems.add("unknown group: " + name);
            return;
        }
        visited.add(name);
        List<Node> nodes = applicable(group.nodes(), context, now);
        sources.add(new Resolution.Source(group.name(), false, group.weight(), withoutGroups(nodes)));
        int index = sources.size() - 1;
        if (!group.prefix().isEmpty()) prefixes.add(new Meta(group.weight(), index, group.prefix()));
        if (!group.suffix().isEmpty()) suffixes.add(new Meta(group.weight(), index, group.suffix()));
        collectMeta(nodes, index, prefixes, suffixes);
        List<String> next = new ArrayList<>(path);
        next.add(name);
        for (String parent : memberships(nodes, groups)) walk(parent, next, visited, groups, context, now, sources, problems, prefixes, suffixes);
    }

    private static List<Node> applicable(List<Node> nodes, Map<String, String> context, long now) {
        List<Node> result = new ArrayList<>();
        for (Node node : nodes) if (!node.expired(now) && node.appliesIn(context)) result.add(node);
        return result;
    }

    private static List<Node> withoutGroups(List<Node> nodes) {
        List<Node> result = new ArrayList<>();
        for (Node node : nodes) if (!node.isGroup()) result.add(node);
        return result;
    }

    /** The groups the nodes point at, heaviest first, then by name. */
    private static List<String> memberships(List<Node> nodes, Function<String, GroupData> groups) {
        List<String> names = new ArrayList<>();
        for (Node node : nodes) if (node.isGroup() && node.value() && !names.contains(node.groupName())) names.add(node.groupName());
        names.sort(Comparator.comparingInt((String name) -> {
            GroupData group = groups.apply(name);
            return group == null ? Integer.MIN_VALUE : group.weight();
        }).reversed().thenComparing(Comparator.naturalOrder()));
        return names;
    }

    private record Meta(int priority, int source, String text) {
    }

    private static void collectMeta(List<Node> nodes, int source, List<Meta> prefixes, List<Meta> suffixes) {
        for (Node node : nodes) {
            if (!node.value()) continue;
            List<Meta> target = node.permission().startsWith("prefix.") ? prefixes : node.permission().startsWith("suffix.") ? suffixes : null;
            if (target == null) continue;
            String rest = node.permission().substring(7);
            int dot = rest.indexOf('.');
            if (dot <= 0 || dot == rest.length() - 1) continue;
            try {
                target.add(new Meta(Integer.parseInt(rest.substring(0, dot)), source, rest.substring(dot + 1)));
            } catch (NumberFormatException ignored) {
                // not a meta node
            }
        }
    }

    private static String best(List<Meta> candidates) {
        Meta best = null;
        for (Meta candidate : candidates) {
            if (best == null || candidate.priority() > best.priority()) best = candidate;
        }
        return best == null ? "" : best.text();
    }
}
