package de.happybavarian07.adminpanel.sanctions;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DurationsTest {
    @Test
    void parsesUnitsAndPlainSeconds() {
        assertEquals(TimeUnit.SECONDS.toMillis(30), Durations.parse("30s"));
        assertEquals(TimeUnit.MINUTES.toMillis(5), Durations.parse("5m"));
        assertEquals(TimeUnit.HOURS.toMillis(2), Durations.parse("2h"));
        assertEquals(TimeUnit.DAYS.toMillis(7), Durations.parse("7d"));
        assertEquals(TimeUnit.SECONDS.toMillis(90), Durations.parse("90"));
        assertEquals(TimeUnit.SECONDS.toMillis(30), Durations.parse(" 30S "));
    }

    @Test
    void zeroAndTheWordsForNoEndAreZero() {
        for (String text : new String[]{"0", "never", "Permanent", "perm", "forever"}) {
            assertEquals(0, Durations.parse(text), text);
        }
    }

    @Test
    void rejectsWhatIsNoDuration() {
        for (String invalid : new String[]{"", "s", "m", "0s", "-5", "-5m", "1.5h", "abc", "5x", "5 m", "99999999999999999999"}) {
            assertThrows(IllegalArgumentException.class, () -> Durations.parse(invalid), invalid);
        }
        assertThrows(IllegalArgumentException.class, () -> Durations.parse(null));
    }

    @Test
    void formatsWithTheLargestUnitsFirst() {
        assertEquals("30s", Durations.format(30_000));
        assertEquals("5m", Durations.format(TimeUnit.MINUTES.toMillis(5)));
        assertEquals("1d 2h 3m 4s", Durations.format(TimeUnit.DAYS.toMillis(1) + TimeUnit.HOURS.toMillis(2)
                + TimeUnit.MINUTES.toMillis(3) + TimeUnit.SECONDS.toMillis(4)));
        assertEquals("30d", Durations.format(TimeUnit.DAYS.toMillis(30)));
        assertEquals("0s", Durations.format(0));
        assertEquals("0s", Durations.format(-5));
        assertEquals("0s", Durations.format(999));
    }

    @Test
    void formatAndParseAgreeOnASingleUnit() {
        assertEquals(TimeUnit.HOURS.toMillis(12), Durations.parse(Durations.format(TimeUnit.HOURS.toMillis(12))));
    }
}
