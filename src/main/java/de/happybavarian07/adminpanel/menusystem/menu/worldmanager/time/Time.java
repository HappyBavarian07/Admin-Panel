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
