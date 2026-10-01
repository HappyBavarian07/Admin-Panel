package de.happybavarian07.adminpanel.sanctions;

import de.happybavarian07.adminpanel.warnings.DurationParser;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Reads and writes the durations typed into the chat by the player menus.
 */
public final class Durations {
    private static final Set<String> PERMANENT = Set.of("permanent", "perm", "forever", "never");

    private Durations() {
    }

    /**
     * Parses a duration such as {@code 30s}, {@code 5m}, {@code 2h} or {@code 7d}. A plain number is a number of
     * seconds.
     *
     * @param text the typed text; {@code 0}, {@code permanent}, {@code perm}, {@code forever} and {@code never}
     *             mean no end
     * @return the duration in milliseconds, {@code 0} if there is no end
     * @throws IllegalArgumentException if the text is not a duration
     */
    public static long parse(String text) {
        if (text == null) throw new IllegalArgumentException("duration is missing");
        String value = text.trim().toLowerCase(Locale.ROOT);
        if (PERMANENT.contains(value)) return 0;
        if (value.matches("\\d+")) return TimeUnit.SECONDS.toMillis(Long.parseLong(value));
        if (value.length() > 1 && value.endsWith("s")) {
            String digits = value.substring(0, value.length() - 1);
            if (!digits.matches("\\d+")) throw new IllegalArgumentException("not a duration: " + text);
            long amount = Long.parseLong(digits);
            if (amount <= 0) throw new IllegalArgumentException("duration must be positive: " + text);
            return TimeUnit.SECONDS.toMillis(amount);
        }
        return DurationParser.parse(value);
    }

    /**
     * Writes a duration with the units {@code d}, {@code h}, {@code m} and {@code s}, for example {@code 1d 2h 5m}.
     *
     * @param millis the duration; zero or less is written as {@code 0s}
     */
    public static String format(long millis) {
        long total = Math.max(0, millis) / 1000;
        long days = total / 86400;
        long hours = total % 86400 / 3600;
        long minutes = total % 3600 / 60;
        long seconds = total % 60;
        StringBuilder text = new StringBuilder();
        append(text, days, 'd');
        append(text, hours, 'h');
        append(text, minutes, 'm');
        append(text, seconds, 's');
        return text.isEmpty() ? "0s" : text.toString();
    }

    private static void append(StringBuilder text, long amount, char unit) {
        if (amount == 0) return;
        if (!text.isEmpty()) text.append(' ');
        text.append(amount).append(unit);
    }
}
