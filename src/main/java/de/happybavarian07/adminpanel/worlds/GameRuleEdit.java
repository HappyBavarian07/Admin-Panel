package de.happybavarian07.adminpanel.worlds;

import java.util.Locale;
import java.util.Set;

/**
 * The value changes of the gamerule menu. A rule is either a boolean or a non-negative integer; anything else is left
 * alone.
 */
public final class GameRuleEdit {
    public enum Step {
        UP(1), DOWN(-1), UP_BIG(10), DOWN_BIG(-10);

        private final int delta;

        Step(int delta) {
            this.delta = delta;
        }
    }

    private static final Set<String> TRUE = Set.of("true", "on", "yes", "1");
    private static final Set<String> FALSE = Set.of("false", "off", "no", "0");

    private GameRuleEdit() {
    }

    /**
     * @return the value after the click, or {@code null} if the rule is neither a boolean nor an integer
     */
    public static Object next(Object current, Step step) {
        if (current instanceof Boolean bool) return !bool;
        if (current instanceof Integer number) return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, (long) number + step.delta));
        return null;
    }

    /**
     * @return the typed value as the type of {@code current}, or {@code null} if the text does not fit that type
     */
    public static Object parse(Object current, String typed) {
        String text = typed.trim().toLowerCase(Locale.ROOT);
        if (current instanceof Boolean) {
            if (TRUE.contains(text)) return true;
            if (FALSE.contains(text)) return false;
            return null;
        }
        if (current instanceof Integer) {
            try {
                int value = Integer.parseInt(text);
                return value < 0 ? null : value;
            } catch (NumberFormatException notANumber) {
                return null;
            }
        }
        return null;
    }
}
