package de.happybavarian07.adminpanel.sanctions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BanInfoTest {
    private static BanInfo ban(long expires) {
        return new BanInfo("Steve", null, false, "Hacking", "Alex", 1000, expires);
    }

    @Test
    void aBanWithoutEndIsPermanentAndHasNoTimeLeft() {
        assertTrue(ban(0).permanent());
        assertEquals(0, ban(0).remainingMillis(5000));
    }

    @Test
    void theTimeLeftCountsDownToZero() {
        BanInfo temporary = ban(10_000);

        assertFalse(temporary.permanent());
        assertEquals(7000, temporary.remainingMillis(3000));
        assertEquals(0, temporary.remainingMillis(10_000));
        assertEquals(0, temporary.remainingMillis(20_000));
    }
}
