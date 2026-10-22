package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.adminpanel.permissions.engine.Node;
import de.happybavarian07.adminpanel.sanctions.Durations;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** Reads the words typed after a permission in a command or in the chat: {@code true|false}, a duration and {@code key=value} contexts. */
public final class NodeInput {
    private NodeInput() {
    }

    /**
     * @param permission the permission or {@code group.<name>}
     * @param words      the typed words
     * @param from       the first word to read
     * @param now        the current time, a duration is added to it
     * @return the entry; the value is {@code true} unless {@code false} was typed, a duration of 0 or {@code permanent} means no end
     * @throws IllegalArgumentException if a word is none of the three kinds
     */
    public static Node parse(String permission, String[] words, int from, long now) {
        boolean value = true;
        long expiresAt = 0;
        Map<String, String> context = new TreeMap<>();
        for (int i = from; i < words.length; i++) {
            String word = words[i];
            String lower = word.toLowerCase(Locale.ROOT);
            if (lower.equals("true") || lower.equals("false")) {
                value = Boolean.parseBoolean(lower);
            } else if (word.indexOf('=') > 0) {
                Map<String, String> parsed = Node.parseContext(word);
                if (parsed.isEmpty()) throw new IllegalArgumentException("not a context: " + word);
                context.putAll(parsed);
            } else {
                long duration = Durations.parse(word);
                expiresAt = duration > 0 ? now + duration : 0;
            }
        }
        return new Node(permission, value, expiresAt, context);
    }

    /** Splits a chat line on spaces, the first word is the permission. */
    public static Node parseLine(String line, long now) {
        String[] words = line.trim().split("\\s+");
        if (words.length == 0 || words[0].isEmpty()) throw new IllegalArgumentException("no permission typed");
        return parse(words[0], words, 1, now);
    }
}
