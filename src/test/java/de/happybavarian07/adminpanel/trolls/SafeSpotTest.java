package de.happybavarian07.adminpanel.trolls;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeSpotTest {
    private static String key(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    private static class FakeWorld implements SafeSpot.Probe {
        final Set<String> solid = new HashSet<>();
        final Set<String> unsafe = new HashSet<>();

        void floor(int x0, int x1, int y, int z0, int z1) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) solid.add(key(x, y, z));
            }
        }

        @Override
        public boolean isFloor(int x, int y, int z) {
            return solid.contains(key(x, y, z)) && !unsafe.contains(key(x, y, z));
        }

        @Override
        public boolean isFree(int x, int y, int z) {
            return !solid.contains(key(x, y, z));
        }
    }

    @Test
    void findsAFlatSpotInsideTheRadiusAndAwayFromTheStart() {
        FakeWorld world = new FakeWorld();
        world.floor(-10, 10, 63, -10, 10);

        for (long seed = 0; seed < 50; seed++) {
            SafeSpot.Spot spot = SafeSpot.find(0, 64, 0, 5, new Random(seed), world).orElseThrow();
            int distanceSquared = spot.x() * spot.x() + spot.z() * spot.z();
            assertTrue(distanceSquared >= 4 && distanceSquared <= 25, "distance " + distanceSquared);
            assertEquals(64, spot.y());
        }
    }

    @Test
    void isDeterministicForTheSameSeed() {
        FakeWorld world = new FakeWorld();
        world.floor(-10, 10, 63, -10, 10);

        assertEquals(SafeSpot.find(0, 64, 0, 5, new Random(7), world), SafeSpot.find(0, 64, 0, 5, new Random(7), world));
    }

    @Test
    void emptyWhenThereIsNoFloor() {
        assertEquals(Optional.empty(), SafeSpot.find(0, 64, 0, 5, new Random(1), new FakeWorld()));
    }

    @Test
    void skipsHarmfulFloorsAndBlockedColumns() {
        FakeWorld world = new FakeWorld();
        world.floor(-10, 10, 63, -10, 10);
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                if (x != 3 || z != 0) world.unsafe.add(key(x, 63, z));
            }
        }

        assertEquals(new SafeSpot.Spot(3, 64, 0), SafeSpot.find(0, 64, 0, 5, new Random(1), world).orElseThrow());

        world.solid.add(key(3, 65, 0));
        world.unsafe.add(key(3, 65, 0));
        assertEquals(Optional.empty(), SafeSpot.find(0, 64, 0, 5, new Random(1), world));
    }

    @Test
    void followsSlopesUpToThreeBlocks() {
        FakeWorld world = new FakeWorld();
        world.floor(4, 4, 66, 0, 0);

        assertEquals(new SafeSpot.Spot(4, 67, 0), SafeSpot.find(0, 64, 0, 5, new Random(1), world).orElseThrow());

        FakeWorld tooHigh = new FakeWorld();
        tooHigh.floor(4, 4, 68, 0, 0);
        assertEquals(Optional.empty(), SafeSpot.find(0, 64, 0, 5, new Random(1), tooHigh));
    }
}
