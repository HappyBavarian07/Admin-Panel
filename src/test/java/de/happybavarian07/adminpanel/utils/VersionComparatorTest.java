package de.happybavarian07.adminpanel.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersionComparatorTest {
    @Test
    void patchIncrementDetected() {
        assertTrue(VersionComparator.SEMANTIC_VERSION.updateAvailable("1.2.3", "1.2.4"));
    }

    @Test
    void twoDigitMinorHandledCorrectly() {
        assertFalse(VersionComparator.SEMANTIC_VERSION.updateAvailable("1.10.0", "1.2.0"));
    }

    @Test
    void missingPatchTreatedAsZero() {
        assertFalse(VersionComparator.SEMANTIC_VERSION.updateAvailable("1.2", "1.2.0"));
        assertFalse(VersionComparator.SEMANTIC_VERSION.updateAvailable("1.2.0", "1.2"));
    }

    @Test
    void snapshotLowerThanRelease() {
        assertTrue(VersionComparator.SEMANTIC_VERSION.updateAvailable("1.2-SNAPSHOT", "1.2"));
        assertFalse(VersionComparator.SEMANTIC_VERSION.updateAvailable("1.2", "1.2-SNAPSHOT"));
    }

    @Test
    void extraComponentMakesVersionHigher() {
        assertFalse(VersionComparator.SEMANTIC_VERSION.updateAvailable("1.2.3.4", "1.2.3"));
    }

    @Test
    void prereleaseOrderingLexicalNumeric() {
        assertTrue(VersionComparator.SEMANTIC_VERSION.updateAvailable("1.2.3-beta", "1.2.3-rc1"));
        assertFalse(VersionComparator.SEMANTIC_VERSION.updateAvailable("1.2.3-rc1", "1.2.3-beta"));
    }

    @Test
    void identicalVersionsNoUpdate() {
        assertFalse(VersionComparator.SEMANTIC_VERSION.updateAvailable("1.2.3", "1.2.3"));
    }
}

