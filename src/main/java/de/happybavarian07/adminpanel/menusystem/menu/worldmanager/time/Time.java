package de.happybavarian07.adminpanel.menusystem.menu.worldmanager.time;

public enum Time {
    SUNRISE("", 23000L, "DAY"),
    DAY("", 0L, "MORNING"),
    MORNING("", 1000L, "NOON"),
    NOON("", 6000L, "AFTERNOON"),
    AFTERNOON("", 9000L, "SUNSET"),
    SUNSET("", 12000L, "NIGHT"),
    NIGHT("", 14000L, "MIDNIGHT"),
    MIDNIGHT("", 18000L, "SUNRISE");

    /*
    Sunrise, Day, Morning, Noon, Afternoon, Sunset, Night, Midnight
     */

    private final Long time;
    private final String nextTime;
    private final String name;

    Time(String name, Long time, String nextTime) {
        this.name = name;
        this.time = time;
        this.nextTime = nextTime;
    }

    /**
     * @return the preset whose time of day is closest to {@code ticks}, counting around the end of the day
     */
    public static Time closest(long ticks) {
        Time best = values()[0];
        long bestDistance = Long.MAX_VALUE;
        for (Time candidate : values()) {
            long distance = Math.abs(candidate.time - Math.floorMod(ticks, 24000L));
            distance = Math.min(distance, 24000L - distance);
            if (distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    public Long getTime() {
        return time;
    }

    public Time getNextTimeAsTime() {
        return Time.valueOf(nextTime);
    }

    public String getNextTimeAsString() {
        return nextTime;
    }

    public String getName() {
        return name;
    }
}
