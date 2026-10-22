package de.happybavarian07.adminpanel.permissions.engine;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * One entry of a user or group: a permission with a value, an optional expiry and optional contexts. A group membership or a parent
 * group is the node {@code group.<name>}.
 *
 * @param permission the permission, lower case (except the text of {@code prefix.<priority>.<text>} and {@code suffix.<priority>.<text>})
 * @param value      {@code true} grants, {@code false} denies
 * @param expiresAt  when the entry ends in milliseconds since the epoch, {@code 0} for never
 * @param context    where the entry applies, for example {@code world=nether}; empty means everywhere
 */
public record Node(String permission, boolean value, long expiresAt, Map<String, String> context) {
    public static final String GROUP_PREFIX = "group.";

    public Node {
        permission = permission == null ? "" : permission.stripLeading();
        String start = permission.length() > 7 ? permission.substring(0, 7).toLowerCase(Locale.ROOT) : "";
        if (start.equals("prefix.") || start.equals("suffix.")) {
            permission = start + permission.substring(7);
        } else {
            permission = permission.trim().toLowerCase(Locale.ROOT);
        }
        context = context == null ? Map.of() : Map.copyOf(new TreeMap<>(context));
    }

    public static Node of(String permission, boolean value) {
        return new Node(permission, value, 0, Map.of());
    }

    public static Node group(String name) {
        return new Node(GROUP_PREFIX + name, true, 0, Map.of());
    }

    public boolean isGroup() {
        return permission.startsWith(GROUP_PREFIX) && permission.length() > GROUP_PREFIX.length();
    }

    /** @return the group a {@code group.<name>} node points at, otherwise an empty string */
    public String groupName() {
        return isGroup() ? permission.substring(GROUP_PREFIX.length()) : "";
    }

    public boolean expired(long now) {
        return expiresAt > 0 && expiresAt <= now;
    }

    public boolean isTemporary() {
        return expiresAt > 0;
    }

    /** @return whether every context entry of this node is present with the same value in the current context */
    public boolean appliesIn(Map<String, String> current) {
        for (Map.Entry<String, String> required : context.entrySet()) {
            String actual = current.get(required.getKey());
            if (actual == null || !actual.equalsIgnoreCase(required.getValue())) return false;
        }
        return true;
    }

    public Node withValue(boolean newValue) {
        return new Node(permission, newValue, expiresAt, context);
    }

    /** @return the context as the text stored in the database, {@code key=value;key=value} */
    public String contextText() {
        return formatContext(context);
    }

    public static String formatContext(Map<String, String> context) {
        StringBuilder text = new StringBuilder();
        new TreeMap<>(context).forEach((key, value) -> {
            if (text.length() > 0) text.append(';');
            text.append(key).append('=').append(value);
        });
        return text.toString();
    }

    public static Map<String, String> parseContext(String text) {
        Map<String, String> context = new TreeMap<>();
        if (text == null || text.isBlank()) return context;
        for (String part : text.split(";")) {
            int equals = part.indexOf('=');
            if (equals > 0 && equals < part.length() - 1) context.put(part.substring(0, equals).trim(), part.substring(equals + 1).trim());
        }
        return context;
    }
}
