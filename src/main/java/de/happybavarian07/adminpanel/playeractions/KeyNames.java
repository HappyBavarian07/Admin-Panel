package de.happybavarian07.adminpanel.playeractions;

/**
 * Turns registry keys into readable names.
 */
public final class KeyNames {
    private KeyNames() {
    }

    /**
     * Writes a registry key as a title, for example {@code night_vision} as {@code Night Vision}.
     *
     * @param key the key without namespace
     */
    public static String pretty(String key) {
        StringBuilder name = new StringBuilder(key.length());
        boolean wordStart = true;
        for (char c : key.toCharArray()) {
            if (c == '_' || c == ' ') {
                name.append(' ');
                wordStart = true;
            } else {
                name.append(wordStart ? Character.toUpperCase(c) : c);
                wordStart = false;
            }
        }
        return name.toString();
    }
}
