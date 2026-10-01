package de.happybavarian07.adminpanel.warnings;

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * One entry of {@code Warnings.Escalation}: what happens when a player reaches {@code count} active warnings.
 *
 * @param count          the number of active warnings that triggers the rule
 * @param action         what to do
 * @param reason         kick or ban reason, empty for the default reason
 * @param durationMillis ban length for {@link Action#TEMPBAN}
 * @param command        console command for {@link Action#COMMAND}, {@code %player%} is replaced
 */
public record EscalationRule(int count, Action action, String reason, long durationMillis, String command) {
    public enum Action {
        KICK,
        TEMPBAN,
        COMMAND
    }

    /**
     * Reads one rule from its config map.
     *
     * @param raw the map from the config
     * @param log receives a message when the rule is invalid
     * @return the rule, or {@code null} if it is invalid
     */
    public static EscalationRule parse(Map<?, ?> raw, Consumer<String> log) {
        try {
            int count = Integer.parseInt(String.valueOf(raw.get("count")).trim());
            if (count < 1) throw new IllegalArgumentException("count must be at least 1");
            Action action = Action.valueOf(String.valueOf(raw.get("action")).trim().toUpperCase(Locale.ROOT));
            String reason = text(raw.get("reason"));
            String command = text(raw.get("command"));
            long duration = 0;
            if (action == Action.TEMPBAN) {
                duration = DurationParser.parse(String.valueOf(raw.get("duration")));
                if (duration <= 0) throw new IllegalArgumentException("a TEMPBAN needs a duration");
            }
            if (action == Action.COMMAND && command.isBlank()) throw new IllegalArgumentException("a COMMAND needs a command");
            return new EscalationRule(count, action, reason, duration, command);
        } catch (IllegalArgumentException e) {
            log.accept("Skipping invalid Warnings.Escalation rule " + raw + ": " + e.getMessage());
            return null;
        }
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
