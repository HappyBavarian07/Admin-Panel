package de.happybavarian07.adminpanel.permissions.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The permissions of one user in one context, worked out by {@link Resolver}. It answers single questions ({@link #check},
 * {@link #explain}), gives the flat map for a Bukkit attachment ({@link #expand}) and the chat meta.
 */
public final class Resolution {
    /** One definition of a permission: where it came from and what it says. */
    public record Step(String source, String permission, boolean value, long expiresAt, boolean decisive) {
    }

    /** A source in the order the resolver looked at it: the user itself or a group. */
    public record Source(String name, boolean user, int weight, List<Node> nodes) {
    }

    private final List<Source> sources;
    private final List<String> problems;
    private final String prefix;
    private final String suffix;
    private final Map<String, Step> exact = new LinkedHashMap<>();
    private final Map<String, List<Step>> definitions = new LinkedHashMap<>();

    Resolution(List<Source> sources, List<String> problems, String prefix, String suffix) {
        this.sources = List.copyOf(sources);
        this.problems = List.copyOf(problems);
        this.prefix = prefix;
        this.suffix = suffix;
        for (Source source : sources) {
            for (Node node : source.nodes()) {
                Step step = new Step(source.name(), node.permission(), node.value(), node.expiresAt(), false);
                definitions.computeIfAbsent(node.permission(), key -> new ArrayList<>()).add(step);
                exact.putIfAbsent(node.permission(), step);
            }
        }
    }

    /** @return the sources in the order they were consulted; the first one is the user */
    public List<Source> sources() {
        return sources;
    }

    /** @return the names of the groups the user inherits, in the order they were consulted */
    public List<String> groups() {
        List<String> names = new ArrayList<>();
        for (Source source : sources) if (!source.user()) names.add(source.name());
        return names;
    }

    /** @return things that look wrong, such as a group that inherits itself or a parent that does not exist */
    public List<String> problems() {
        return problems;
    }

    public String prefix() {
        return prefix;
    }

    public String suffix() {
        return suffix;
    }

    /**
     * @param permission the permission
     * @return {@code true} or {@code false} if an entry or wildcard decides, {@code null} if nothing says anything
     */
    public Boolean check(String permission) {
        Step step = decide(normalize(permission));
        return step == null ? null : step.value();
    }

    /**
     * Every definition that is relevant for a permission, in the order they were consulted, the deciding one marked.
     *
     * @return the steps, empty if nothing defines the permission
     */
    public List<Step> explain(String permission) {
        String node = normalize(permission);
        Step winner = decide(node);
        List<Step> steps = new ArrayList<>();
        for (String candidate : candidates(node)) {
            for (Step step : definitions.getOrDefault(candidate, List.of())) {
                steps.add(new Step(step.source(), step.permission(), step.value(), step.expiresAt(), step == winner));
            }
        }
        return steps;
    }

    /**
     * The flat map for an attachment: every exact entry, and for every known permission without an exact entry the answer of the
     * wildcards.
     *
     * @param known the permissions registered on the server
     */
    public Map<String, Boolean> expand(Collection<String> known) {
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (Map.Entry<String, Step> entry : exact.entrySet()) {
            if (entry.getValue().permission().startsWith(Node.GROUP_PREFIX) && isMembership(entry.getKey())) continue;
            if (!entry.getKey().endsWith("*") && !isMeta(entry.getKey())) result.put(entry.getKey(), entry.getValue().value());
        }
        for (String permission : known) {
            String node = normalize(permission);
            if (result.containsKey(node) || isMeta(node)) continue;
            Step step = decide(node);
            if (step != null) result.put(node, step.value());
        }
        return result;
    }

    private boolean isMembership(String permission) {
        return permission.startsWith(Node.GROUP_PREFIX) && permission.length() > Node.GROUP_PREFIX.length();
    }

    static boolean isMeta(String permission) {
        return permission.startsWith("prefix.") || permission.startsWith("suffix.") || permission.startsWith("weight.");
    }

    private Step decide(String node) {
        Step direct = exact.get(node);
        if (direct != null) return direct;
        for (String candidate : candidates(node)) {
            if (candidate.equals(node)) continue;
            Step wildcard = exact.get(candidate);
            if (wildcard != null) return wildcard;
        }
        return null;
    }

    /** The permission itself, then its wildcards from the most specific to {@code *}. */
    private static List<String> candidates(String node) {
        List<String> candidates = new ArrayList<>();
        candidates.add(node);
        String rest = node;
        int dot;
        while ((dot = rest.lastIndexOf('.')) > 0) {
            rest = rest.substring(0, dot);
            candidates.add(rest + ".*");
        }
        candidates.add("*");
        return candidates;
    }

    private static String normalize(String permission) {
        return permission == null ? "" : permission.trim().toLowerCase(Locale.ROOT);
    }
}
