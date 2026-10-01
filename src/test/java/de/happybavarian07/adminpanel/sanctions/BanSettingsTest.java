package de.happybavarian07.adminpanel.sanctions;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BanSettingsTest {
    private final List<String> log = new ArrayList<>();

    private static Map<String, Object> preset(Object name, Object reason) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (name != null) map.put("name", name);
        if (reason != null) map.put("reason", reason);
        return map;
    }

    @Test
    void readsPresetsAndDurationsInConfigOrder() {
        BanSettings settings = BanSettings.parse(
                List.of(preset("Hacking", "Using a hacked client"), preset("Spam", "Spamming the chat")),
                List.of("30m", "1d", "7d", "permanent"), log::add);

        assertEquals(List.of(new BanSettings.Preset("Hacking", "Using a hacked client"),
                new BanSettings.Preset("Spam", "Spamming the chat")), settings.presets());
        assertEquals(List.of(new BanSettings.Length("30m", TimeUnit.MINUTES.toMillis(30)),
                new BanSettings.Length("1d", TimeUnit.DAYS.toMillis(1)),
                new BanSettings.Length("7d", TimeUnit.DAYS.toMillis(7)),
                new BanSettings.Length("permanent", 0)), settings.lengths());
        assertTrue(log.isEmpty());
    }

    @Test
    void aPresetWithoutReasonUsesItsName() {
        BanSettings settings = BanSettings.parse(List.of(preset("Griefing", null), preset("Raid", "  ")), List.of(), log::add);

        assertEquals("Griefing", settings.presets().get(0).reason());
        assertEquals("Raid", settings.presets().get(1).reason());
    }

    @Test
    void skipsInvalidEntriesAndLogsThem() {
        BanSettings settings = BanSettings.parse(
                List.of(preset(null, "no name"), preset("  ", "blank name"), preset("Fine", "ok")),
                List.of("1d", "soon", "-3h", "2h"), log::add);

        assertEquals(1, settings.presets().size());
        assertEquals("Fine", settings.presets().get(0).name());
        assertEquals(List.of("1d", "2h"), settings.lengths().stream().map(BanSettings.Length::label).toList());
        assertEquals(4, log.size());
    }

    @Test
    void numbersInThePresetValuesAreRead() {
        BanSettings settings = BanSettings.parse(List.of(preset(123, 456)), List.of("3600"), log::add);

        assertEquals(new BanSettings.Preset("123", "456"), settings.presets().get(0));
        assertEquals(TimeUnit.HOURS.toMillis(1), settings.lengths().get(0).millis());
    }

    @Test
    void missingListsGiveEmptySettings() {
        BanSettings settings = BanSettings.parse(null, null, log::add);

        assertTrue(settings.presets().isEmpty());
        assertTrue(settings.lengths().isEmpty());
    }
}
