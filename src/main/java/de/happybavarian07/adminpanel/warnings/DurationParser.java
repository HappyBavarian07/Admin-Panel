package de.happybavarian07.adminpanel.warnings;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class DurationParser {
    private DurationParser() {
    }

    /**
     * Parses a duration such as {@code 30m}, {@code 12h} or {@code 7d}.
     *
     * @param text the duration; {@code 0} and {@code never} mean no duration
     * @return the duration in milliseconds, {@code 0} for {@code 0} and {@code never}
     * @throws IllegalArgumentException if the text is not a duration
     */
    public static long parse(String text) {
        if (text == null) throw new IllegalArgumentException("duration is missing");
        String value = text.trim().toLowerCase(Locale.ROOT);
        if (value.equals("0") || value.equals("never")) return 0;
        if (value.length() < 2) throw new IllegalArgumentException("not a duration: " + text);
        TimeUnit unit = switch (value.charAt(value.length() - 1)) {
            case 'm' -> TimeUnit.MINUTES;
            case 'h' -> TimeUnit.HOURS;
            case 'd' -> TimeUnit.DAYS;
            default -> throw new IllegalArgumentException("unknown unit in duration: " + text);
        };
        long amount;
        try {
            amount = Long.parseLong(value.substring(0, value.length() - 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("not a duration: " + text);
        }
        if (amount <= 0) throw new IllegalArgumentException("duration must be positive: " + text);
        return unit.toMillis(amount);
    }
}
