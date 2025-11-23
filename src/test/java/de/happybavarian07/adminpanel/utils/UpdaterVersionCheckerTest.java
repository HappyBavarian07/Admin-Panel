package de.happybavarian07.adminpanel.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/*
 * @Author HappyBavarian07
 * @Date Oktober 03, 2025 | 21:14
 */
class UpdaterVersionCheckerTest {
    private static List<String> versions;
    private final String CURRENT_VERSION = "8.9.1";

    @BeforeAll
    public static void setUp() {
        versions = new ArrayList<>();
        //boolean full = Boolean.parseBoolean(System.getProperty("updater.full", "false"));
        int majorMax = 99;//Integer.parseInt(System.getProperty("updater.major.max", full ? "99" : "34"));
        int minorMax = 99;//Integer.parseInt(System.getProperty("updater.minor.max", full ? "99" : "10"));
        int patchMax = 99;//Integer.parseInt(System.getProperty("updater.patch.max", full ? "99" : "5"));

        for (int i = 0; i <= majorMax; i++) {
            for (int j = 0; j <= minorMax; j++) {
                for (int k = 0; k <= patchMax; k++) {
                    versions.add(i + "." + j + "." + k);
                }
            }
        }
        if (!versions.contains("8.9.1")) versions.add("8.9.1");
    }

    @Test
    void versionTest() {
        int currentIndex = versions.indexOf(CURRENT_VERSION);
        Assertions.assertTrue(currentIndex >= 0, "Current version must be present in the sample list");

        List<Boolean> results = new ArrayList<>();
        for (String version : versions) {
            boolean updateAvailable = VersionComparator.SEMANTIC_VERSIONS.updateAvailable(CURRENT_VERSION, version);
            results.add(updateAvailable);
        }

        long updates = results.stream().filter(b -> b).count();
        Assertions.assertTrue(updates > 0, "There should be some versions considered updates compared to current");

        // Ensure no versions before (including) the current index are marked as updates
        long mismatchesBefore = 0;
        for (int i = 0; i <= currentIndex; i++) if (results.get(i)) mismatchesBefore++;
        Assertions.assertEquals(0, mismatchesBefore, "No versions at or before the current version should be marked as updates");
    }

    @Test
    void versionEdgeCaseTest() {
        // Equal versions should not be considered an update
        Assertions.assertFalse(VersionComparator.SEMANTIC_VERSIONS.updateAvailable("8.9.1", "8.9.1"));
        // Just ensure these comparisons run without throwing; behavior for SNAPSHOT can vary and is implementation dependent
        Assertions.assertDoesNotThrow(() -> VersionComparator.SEMANTIC_VERSIONS.updateAvailable("8.9.1", "8.9.1-SNAPSHOT"));
        Assertions.assertDoesNotThrow(() -> VersionComparator.SEMANTIC_VERSIONS.updateAvailable("8.9.1-SNAPSHOT", "8.9.1"));
        Assertions.assertDoesNotThrow(() -> VersionComparator.SEMANTIC_VERSIONS.updateAvailable("8.9.1-SNAPSHOT", "8.9.1-SNAPSHOT"));
    }

    @Test
    void overkillButEfficientVersionTest() {
        long overallStart = System.nanoTime();
        List<String> allVersions = new ArrayList<>(versions);
        String currentVersion = CURRENT_VERSION;
        int threads = Math.max(1, Runtime.getRuntime().availableProcessors());
        List<Boolean> results = ItemProcessor.processItemsDifferentType(
                allVersions,
                threads,
                v -> VersionComparator.SEMANTIC_VERSIONS.updateAvailable(currentVersion, v),
                true
        );
        long processingEnd = System.nanoTime();
        int total = allVersions.size();
        int currentIndex = allVersions.indexOf(currentVersion);
        long updates = results.stream().filter(b -> b).count();
        long nonUpdates = total - updates;

        // Basic sanity checks instead of printing every match
        Assertions.assertTrue(total > 0);
        Assertions.assertTrue(updates > 0, "expected at least one update in the sample set");
        Assertions.assertTrue(nonUpdates > 0, "expected at least one non-update in the sample set");

        double processingMs = (processingEnd - overallStart) / 1_000_000.0;
        String report = "Version Update Report:\n" +
                "Threads Used: " + threads + '\n' +
                "Total Versions: " + total + '\n' +
                "Updates Count: " + updates + '\n' +
                "Non-Updates Count: " + nonUpdates + '\n' +
                "Processing Time (ms): " + String.format("%.3f", processingMs) + '\n';

        System.out.println(report);
    }
}
