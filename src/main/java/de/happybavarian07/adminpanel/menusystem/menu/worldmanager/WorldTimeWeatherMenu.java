package de.happybavarian07.adminpanel.menusystem.menu.worldmanager;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.world.TimeChangeEvent;
import de.happybavarian07.adminpanel.events.world.WeatherChangeEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.worldmanager.time.Time;
import de.happybavarian07.adminpanel.menusystem.menu.worldmanager.weather.Weather;
import de.happybavarian07.adminpanel.sanctions.Durations;
import de.happybavarian07.adminpanel.worlds.TimeEdit;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

/**
 * Time and weather of one world in one screen: the time presets, an hour forward or back, a typed time, freezing the
 * day cycle, and the weather with an optional duration.
 */
public class WorldTimeWeatherMenu extends Menu {
    private static final String TIME_PERMISSION = "AdminPanel.WorldManager.Time";
    private static final String WEATHER_PERMISSION = "AdminPanel.WorldManager.Weather";
    private static final String PATH = "WorldManager.TimeWeather.";
    private static final int[] PRESET_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7};
    private static final int MAX_WEATHER_SECONDS = 86_400;

    private final World world;
    private int weatherSeconds;

    public WorldTimeWeatherMenu(PlayerMenuUtility playerMenuUtility, World world) {
        super(playerMenuUtility);
        this.world = world;
        setOpeningPermission(TIME_PERMISSION);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("WorldManager.TimeWeatherMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "WorldTimeWeatherMenu";
    }

    @Override
    public int getSlots() {
        return 36;
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player owner = playerMenuUtility.getOwner();
        Time[] presets = Time.values();
        for (int i = 0; i < presets.length && i < PRESET_SLOTS.length; i++) {
            Time preset = presets[i];
            String presetPath = "WorldManager.Time." + preset;
            registerButton(getSlot(presetPath, PRESET_SLOTS[i]), lgm.getItem(presetPath, owner, false),
                    MenuAction.of((p, e) -> setTime(p, preset.getTime())).then((p, e) -> refresh()).requires(TIME_PERMISSION));
        }

        lgm.addPlaceholder(PlaceholderType.ITEM, "%time%", world.getTime(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%clock%", clock(world.getTime()), false);
        registerButton(getSlot(PATH + "Info", 8), lgm.getItem(PATH + "Info", owner, true), MenuAction.of((p, e) -> {
        }));
        registerButton(getSlot(PATH + "HourBack", 10), lgm.getItem(PATH + "HourBack", owner, false),
                MenuAction.of((p, e) -> setTime(p, TimeEdit.add(world.getTime(), -1000))).then((p, e) -> refresh()).requires(TIME_PERMISSION));
        registerButton(getSlot(PATH + "HourForward", 11), lgm.getItem(PATH + "HourForward", owner, false),
                MenuAction.of((p, e) -> setTime(p, TimeEdit.add(world.getTime(), 1000))).then((p, e) -> refresh()).requires(TIME_PERMISSION));
        registerButton(getSlot(PATH + "SetExact", 13), lgm.getItem(PATH + "SetExact", owner, false),
                MenuAction.prompt(CommandText.get(lgm, owner, "TimeWeatherMenu.EnterTime"), this::setTypedTime).requires(TIME_PERMISSION));
        boolean frozen = Boolean.FALSE.equals(world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE));
        String freezePath = PATH + "Freeze." + frozen;
        registerButton(getSlot(freezePath, 15), lgm.getItem(freezePath, owner, false),
                MenuAction.of((p, e) -> world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, frozen)).then((p, e) -> refresh())
                        .requires(TIME_PERMISSION));

        for (Weather weather : Weather.values()) {
            String weatherPath = "WorldManager.Weather." + weather;
            registerButton(getSlot(weatherPath, 19 + weather.ordinal()), lgm.getItem(weatherPath, owner, false),
                    MenuAction.of((p, e) -> setWeather(p, weather)).then((p, e) -> refresh()).requires(WEATHER_PERMISSION));
        }
        lgm.addPlaceholder(PlaceholderType.ITEM, "%duration%",
                weatherSeconds == 0 ? "-" : Durations.format(weatherSeconds * 1000L), false);
        registerButton(getSlot(PATH + "WeatherDuration", 23), lgm.getItem(PATH + "WeatherDuration", owner, true),
                MenuAction.prompt(CommandText.get(lgm, owner, "TimeWeatherMenu.EnterDuration"), this::setWeatherDuration)
                        .requires(WEATHER_PERMISSION));

        registerButton(getSlot("General.Refresh", 27), lgm.getItem("General.Refresh", owner, false),
                MenuAction.of((p, e) -> refresh()).requires("AdminPanel.Button.refresh"));
        registerButton(getSlot("General.Close", 35), lgm.getItem("General.Back", owner, false),
                MenuAction.of((p, e) -> new WorldSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), world).open())
                        .requires("AdminPanel.Button.Close"));
    }

    private void setTime(Player staff, long ticks) {
        TimeChangeEvent event = new TimeChangeEvent(staff, world, ticks);
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(event);
        } catch (NotAPanelEventException invalid) {
            AdminPanelMain.logError("The time change action was not a valid panel event", invalid);
            return;
        }
        if (event.isCancelled()) return;
        world.setTime(event.getTicks());
        if (Math.abs(world.getTime() - event.getTicks()) > 5) {
            CommandText.send(lgm, staff, "TimeWeatherMenu.TimeNotApplied", "%world%", world.getName());
            return;
        }
        CommandText.send(lgm, staff, "TimeWeatherMenu.TimeSet", "%time%", event.getTicks(), "%clock%", clock(event.getTicks()), "%world%", world.getName());
    }

    private void setTypedTime(Player staff, String text) {
        long ticks = TimeEdit.parse(text);
        if (ticks < 0) {
            CommandText.send(lgm, staff, "TimeWeatherMenu.NotATime");
            open();
            return;
        }
        setTime(staff, ticks);
        open();
    }

    private void setWeatherDuration(Player staff, String text) {
        try {
            long seconds = text.trim().equals("-") ? 0 : Durations.parse(text) / 1000;
            if (seconds < 0 || seconds > MAX_WEATHER_SECONDS) throw new IllegalArgumentException("out of range");
            weatherSeconds = (int) seconds;
            CommandText.send(lgm, staff, "TimeWeatherMenu.DurationSet",
                    "%duration%", weatherSeconds == 0 ? "-" : Durations.format(seconds * 1000));
        } catch (IllegalArgumentException invalid) {
            CommandText.send(lgm, staff, "TimeWeatherMenu.NotADuration", "%max%", Durations.format(MAX_WEATHER_SECONDS * 1000L));
        }
        open();
    }

    private void setWeather(Player staff, Weather weather) {
        WeatherChangeEvent event = new WeatherChangeEvent(staff, world, weather);
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(event);
        } catch (NotAPanelEventException invalid) {
            AdminPanelMain.logError("The weather change action was not a valid panel event", invalid);
            return;
        }
        if (event.isCancelled()) return;
        int ticks = weatherSeconds * 20;
        switch (event.getWeather()) {
            case CLEAR -> {
                world.setStorm(false);
                world.setThundering(false);
                if (ticks > 0) world.setClearWeatherDuration(ticks);
            }
            case RAINING -> {
                world.setStorm(true);
                world.setThundering(false);
                if (ticks > 0) world.setWeatherDuration(ticks);
            }
            case THUNDERING -> {
                world.setStorm(true);
                world.setThundering(true);
                if (ticks > 0) {
                    world.setWeatherDuration(ticks);
                    world.setThunderDuration(ticks);
                }
            }
        }
        boolean raining = event.getWeather() != Weather.CLEAR;
        if (world.hasStorm() != raining) {
            CommandText.send(lgm, staff, "TimeWeatherMenu.WeatherNotApplied", "%world%", world.getName());
            return;
        }
        CommandText.send(lgm, staff, "TimeWeatherMenu.WeatherSet", "%weather%", event.getWeather().getWeatherName(), "%world%", world.getName());
    }

    /**
     * The 24-hour clock for a time of day; tick 0 is 6:00.
     */
    static String clock(long ticks) {
        long minutes = Math.floorMod(ticks + 6000, TimeEdit.DAY) * 60 / 1000;
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
