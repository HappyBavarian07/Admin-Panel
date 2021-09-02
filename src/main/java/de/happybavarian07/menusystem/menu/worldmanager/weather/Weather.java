package de.happybavarian07.menusystem.menu.worldmanager.weather;

public enum Weather {
    CLEAR("Clear", "RAINING"),
    RAINING("Rain", "THUNDERING"),
    THUNDERING("Thundering", "CLEAR");

    /*
    Sunrise, Day, Morning, Noon, Afternoon, Sunset, Night, Midnight
     */

    private final String name;
    private final String nextWeather;

    Weather(String name, String nextWeather) {
        this.name = name;
        this.nextWeather = nextWeather;
    }

    public String getWeatherName() {
        return name;
    }

    public Weather getNextTimeAsTime() {
        return Weather.valueOf(nextWeather);
    }

    public String getNextTimeAsString() {
        return nextWeather;
    }
}
