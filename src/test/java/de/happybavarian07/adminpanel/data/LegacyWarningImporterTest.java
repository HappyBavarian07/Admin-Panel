package de.happybavarian07.adminpanel.data;

import de.happybavarian07.adminpanel.warnings.WarningStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyWarningImporterTest {
    private static final String PLAYER = "6f1c0b1e-0000-0000-0000-000000000001";

    @TempDir
    Path dir;

    @Test
    void importsWarningsWithTheirDatesAndExpiresThePassedOnes() throws Exception {
        File file = dir.resolve("warnings.yml").toFile();
        Files.writeString(file.toPath(), """
                WarningCount:
                  %1$s: 3
                Warnings:
                  %1$s:
                    '1':
                      playerUUID: %1$s
                      warningCount: 1
                      creationDate: 1000
                      expirationDate: 5000
                      reason: Spam
                    '2':
                      playerUUID: %1$s
                      warningCount: 2
                      creationDate: 2000
                      expirationDate: 99000
                      reason: Griefing
                    '3':
                      playerUUID: %1$s
                      warningCount: 3
                      creationDate: 3000
                      expirationDate: 0
                      reason: Rude
                  not-a-uuid:
                    '1':
                      reason: ignored
                """.formatted(PLAYER));

        List<PlayerWarning> warnings = LegacyWarningImporter.read(file, 10_000);

        assertEquals(3, warnings.size());
        PlayerWarning expired = warnings.get(0);
        assertEquals("Spam", expired.getReason());
        assertEquals(1000, expired.getCreatedAt());
        assertEquals(5000, expired.getExpiresAt());
        assertEquals(WarningStatus.EXPIRED, expired.getStatus());
        assertEquals(5000, expired.getEndedAt());
        assertEquals(PlayerWarning.SYSTEM, expired.getEndedBy());
        assertEquals(WarningStatus.ACTIVE, warnings.get(1).getStatus());
        assertEquals(99000, warnings.get(1).getExpiresAt());
        assertEquals(WarningStatus.ACTIVE, warnings.get(2).getStatus());
        assertEquals(0, warnings.get(2).getExpiresAt());
        assertEquals(PLAYER, warnings.get(2).getPlayerUuid());
        assertEquals(PlayerWarning.CONSOLE, warnings.get(2).getIssuer());
    }

    @Test
    void anEmptyFileGivesNoWarnings() throws Exception {
        File file = dir.resolve("warnings.yml").toFile();
        Files.writeString(file.toPath(), "");

        assertTrue(LegacyWarningImporter.read(file, 1).isEmpty());
    }
}
