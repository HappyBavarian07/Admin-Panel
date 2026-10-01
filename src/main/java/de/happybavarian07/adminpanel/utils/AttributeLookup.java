package de.happybavarian07.adminpanel.utils;

import org.bukkit.attribute.Attribute;

/**
 * Looks up attributes by name at runtime. The plugin is compiled against an older Spigot API than the servers it runs
 * on, and the attribute constants lost their {@code GENERIC_} prefix in 1.21.2, so a direct reference to either name
 * breaks on one of the two.
 */
public final class AttributeLookup {
    private AttributeLookup() {
    }

    /**
     * Finds an attribute by the first name that exists on the running server.
     *
     * @param names the constant under its different names, newest name first, for example
     *              {@code "MAX_HEALTH", "GENERIC_MAX_HEALTH"}
     * @return the attribute, or {@code null} if no name matches
     */
    public static Attribute find(String... names) {
        for (String name : names) {
            try {
                return (Attribute) Attribute.class.getField(name).get(null);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    /**
     * The max health attribute under its name on the running server.
     *
     * @return the attribute, never {@code null} on a supported server
     */
    public static Attribute maxHealth() {
        return find("MAX_HEALTH", "GENERIC_MAX_HEALTH");
    }
}
