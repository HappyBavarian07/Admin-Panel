package de.happybavarian07.adminpanel.trolls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/** Looks for a place near a position where a player can stand. Knows nothing about Bukkit. */
public final class SafeSpot {
    private static final int[] HEIGHT_OFFSETS = {0, 1, -1, 2, -2, 3, -3};

    /** Answers what the world looks like at a block position. */
    public interface Probe {
        /** @return {@code true} if the block is solid and harmless to stand on */
        boolean isFloor(int x, int y, int z);

        /** @return {@code true} if a player can be inside the block without being hurt or swimming */
        boolean isFree(int x, int y, int z);
    }

    /** The block a player's feet are in. */
    public record Spot(int x, int y, int z) {
    }

    private SafeSpot() {
    }

    /**
     * Picks a random standing spot at least 2 and at most {@code radius} blocks away horizontally and up to 3
     * blocks above or below the start.
     *
     * @return the spot, or empty if none within reach is safe
     */
    public static Optional<Spot> find(int cx, int cy, int cz, int radius, Random random, Probe probe) {
        List<int[]> columns = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int distanceSquared = dx * dx + dz * dz;
                if (distanceSquared >= 4 && distanceSquared <= radius * radius) columns.add(new int[]{dx, dz});
            }
        }
        Collections.shuffle(columns, random);
        for (int[] column : columns) {
            int x = cx + column[0];
            int z = cz + column[1];
            for (int offset : HEIGHT_OFFSETS) {
                int y = cy + offset;
                if (probe.isFloor(x, y - 1, z) && probe.isFree(x, y, z) && probe.isFree(x, y + 1, z)) {
                    return Optional.of(new Spot(x, y, z));
                }
            }
        }
        return Optional.empty();
    }
}
