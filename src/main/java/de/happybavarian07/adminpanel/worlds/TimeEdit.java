package de.happybavarian07.adminpanel.worlds;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Time of day arithmetic. A Minecraft day has 24000 ticks and tick 0 is 6:00 in the morning.
 */
public final class TimeEdit {
    public static final long DAY = 24000L;
    private static final Pattern TICKS = Pattern.compile("\\d{1,9}");
    private static final Pattern HOURS = Pattern.compile("(\\d{1,2})h", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOCK = Pattern.compile("(\\d{1,2}):(\\d{2})");

    private TimeEdit() {
    }

    public static long add(long time, long delta) {
        return Math.floorMod(time + delta, DAY);
    }

    /**
     * Reads {@code 6000} (ticks), {@code 18h} (hour of the day) or {@code 6:30} (24-hour clock).
     *
     * @return the tick of the day, or {@code -1} if the text is not a time
     */
    public static long parse(String typed) {
        String text = typed.trim();
        if (TICKS.matcher(text).matches()) return Long.parseLong(text) % DAY;
        Matcher hours = HOURS.matcher(text);
        if (hours.matches()) return fromClock(Integer.parseInt(hours.group(1)), 0);
        Matcher clock = CLOCK.matcher(text);
        if (clock.matches()) return fromClock(Integer.parseInt(clock.group(1)), Integer.parseInt(clock.group(2)));
        return -1;
    }

    private static long fromClock(int hour, int minute) {
        if (hour > 23 || minute > 59) return -1;
        return Math.floorMod((hour - 6) * 1000L + minute * 1000L / 60, DAY);
    }
}
