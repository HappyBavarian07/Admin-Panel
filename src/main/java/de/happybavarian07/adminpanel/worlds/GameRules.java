package de.happybavarian07.adminpanel.worlds;

import org.bukkit.GameRule;
import org.bukkit.Keyed;

import java.lang.reflect.Method;

/**
 * Looks gamerules up by name and reads their names. {@code GameRule} is a class in the API the plugin is compiled
 * against and an interface on servers from 1.21.11 on, so calling {@code GameRule.getByName} or {@code getName}
 * directly fails there with an {@code IncompatibleClassChangeError}; reflection works on both.
 */
public final class GameRules {
    private static final Method BY_NAME = method("getByName", String.class);
    private static final Method NAME = method("getName");

    private GameRules() {
    }

    private static Method method(String name, Class<?>... parameters) {
        try {
            return GameRule.class.getMethod(name, parameters);
        } catch (NoSuchMethodException missing) {
            return null;
        }
    }

    /**
     * @return the rule, or {@code null} if this server has no rule of that name
     */
    public static GameRule<?> byName(String name) {
        if (BY_NAME == null) return null;
        try {
            return (GameRule<?>) BY_NAME.invoke(null, name);
        } catch (ReflectiveOperationException | RuntimeException failed) {
            return null;
        }
    }

    public static String nameOf(GameRule<?> rule) {
        if (NAME != null) {
            try {
                return (String) NAME.invoke(rule);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
        Object any = rule;
        return any instanceof Keyed keyed ? keyed.getKey().getKey() : String.valueOf(rule);
    }
}
