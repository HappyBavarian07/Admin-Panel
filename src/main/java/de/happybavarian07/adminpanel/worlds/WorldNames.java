package de.happybavarian07.adminpanel.worlds;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * World names double as folder names inside the server directory, so they are limited to characters that are safe on
 * every file system.
 */
public final class WorldNames {
    private static final Pattern VALID = Pattern.compile("[A-Za-z0-9_][A-Za-z0-9_.-]{0,63}");
    private static final Pattern RESERVED = Pattern.compile("(con|prn|aux|nul|com[1-9]|lpt[1-9])(\\..*)?");

    private WorldNames() {
    }

    /**
     * Turns spaces into dashes and trims the text.
     */
    public static String normalize(String input) {
        return input.trim().replaceAll("\\s+", "-");
    }

    /**
     * Reads a seed the way Minecraft does: a number is used as it is, any other text is hashed.
     *
     * @return the seed, or {@code null} for an empty text, which means a random seed
     */
    public static Long seed(String input) {
        String text = input.trim();
        if (text.isEmpty()) return null;
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException notANumber) {
            return (long) text.hashCode();
        }
    }

    public static boolean isValid(String name) {
        return name != null
                && VALID.matcher(name).matches()
                && !name.endsWith(".")
                && !RESERVED.matcher(name.toLowerCase(Locale.ROOT)).matches();
    }
}
