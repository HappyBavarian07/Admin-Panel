package de.happybavarian07.adminpanel.utils;

/**
 * Reads a constant of a Bukkit type by its name. The plugin is compiled against an older Spigot API than the servers it
 * runs on, and types like {@code Sound} changed from enums to interfaces in between, so {@code Sound.valueOf} breaks on
 * newer servers while the public constant fields exist on all of them.
 */
public final class ConstantLookup {
    private ConstantLookup() {
    }

    /**
     * @param names the constant under its different names, newest name first
     * @return the constant of the first name that exists on the running server, or {@code null} if none does
     */
    public static <T> T find(Class<T> type, String... names) {
        for (String name : names) {
            try {
                Object value = type.getField(name).get(null);
                if (type.isInstance(value)) return type.cast(value);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }
}
