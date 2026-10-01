package de.happybavarian07.adminpanel.warnings;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarningSettingsTest {
    private final List<String> log = new ArrayList<>();

    @Test
    void parsesDurations() {
        assertEquals(TimeUnit.MINUTES.toMillis(30), DurationParser.parse("30m"));
        assertEquals(TimeUnit.HOURS.toMillis(12), DurationParser.parse("12h"));
        assertEquals(TimeUnit.DAYS.toMillis(7), DurationParser.parse("7d"));
        assertEquals(TimeUnit.DAYS.toMillis(7), DurationParser.parse(" 7D "));
        assertEquals(0, DurationParser.parse("0"));
        assertEquals(0, DurationParser.parse("never"));
        assertEquals(0, DurationParser.parse("NEVER"));
    }

    @Test
    void rejectsInvalidDurations() {
        for (String invalid : new String[]{"", "d", "7", "7x", "-5d", "0d", "abc", "1.5d"}) {
            assertThrows(IllegalArgumentException.class, () -> DurationParser.parse(invalid), invalid);
        }
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parse(null));
    }

    @Test
    void readsTheDefaultsOfTheBundledConfig() {
        WarningSettings settings = WarningSettings.parse("30d", List.of(
                Map.of("count", 3, "action", "KICK", "reason", "Too many warnings"),
                Map.of("count", 5, "action", "TEMPBAN", "duration", "7d", "reason", "Too many warnings"),
                Map.of("count", 7, "action", "COMMAND", "command", "ban %player% Too many warnings")), log::add);

        assertEquals(TimeUnit.DAYS.toMillis(30), settings.defaultExpiryMillis());
        assertEquals(3, settings.rules().size());
        assertEquals(new EscalationRule(5, EscalationRule.Action.TEMPBAN, "Too many warnings", TimeUnit.DAYS.toMillis(7), ""),
                settings.rules().get(1));
        assertEquals("ban %player% Too many warnings", settings.rules().get(2).command());
        assertTrue(log.isEmpty());
    }

    @Test
    void rulesForSelectsByExactCount() {
        WarningSettings settings = WarningSettings.parse("30d", List.of(
                Map.of("count", 3, "action", "KICK"), Map.of("count", "3", "action", "kick"),
                Map.of("count", 4, "action", "KICK")), log::add);

        assertEquals(2, settings.rulesFor(3).size());
        assertEquals(1, settings.rulesFor(4).size());
        assertTrue(settings.rulesFor(2).isEmpty());
    }

    @Test
    void invalidRulesAreLoggedAndSkipped() {
        WarningSettings settings = WarningSettings.parse("30d", List.of(
                Map.of("action", "KICK"),
                Map.of("count", 0, "action", "KICK"),
                Map.of("count", "many", "action", "KICK"),
                Map.of("count", 1, "action", "EXPLODE"),
                Map.of("count", 1),
                Map.of("count", 1, "action", "TEMPBAN"),
                Map.of("count", 1, "action", "TEMPBAN", "duration", "forever"),
                Map.of("count", 1, "action", "TEMPBAN", "duration", "never"),
                Map.of("count", 1, "action", "COMMAND"),
                Map.of("count", 1, "action", "COMMAND", "command", "  "),
                Map.of("count", 2, "action", "KICK")), log::add);

        assertEquals(1, settings.rules().size());
        assertEquals(2, settings.rules().get(0).count());
        assertEquals(10, log.size());
    }

    @Test
    void anInvalidDefaultExpiryFallsBackToThirtyDays() {
        WarningSettings settings = WarningSettings.parse("soon", null, log::add);

        assertEquals(TimeUnit.DAYS.toMillis(30), settings.defaultExpiryMillis());
        assertTrue(settings.rules().isEmpty());
        assertEquals(1, log.size());
    }

    @Test
    void neverAsDefaultExpiryMeansNoExpiry() {
        assertEquals(0, WarningSettings.parse("never", List.of(), log::add).defaultExpiryMillis());
        assertEquals(0, WarningSettings.parse("0", List.of(), log::add).defaultExpiryMillis());
    }
}
