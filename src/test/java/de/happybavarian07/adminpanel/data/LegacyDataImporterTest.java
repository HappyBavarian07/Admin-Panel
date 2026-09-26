package de.happybavarian07.adminpanel.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyDataImporterTest {
    @TempDir
    Path dir;

    @Test
    void readsLanguagesDescriptionsAndFlags() throws Exception {
        File db = dir.resolve("plugin_data.db").toFile();
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath());
             Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE data (id TEXT PRIMARY KEY, value TEXT)");
            try (PreparedStatement p = c.prepareStatement("INSERT INTO data VALUES (?, ?)")) {
                insert(p, "language.players", "{\"6f1c0b1e-0000-0000-0000-000000000001\":\"de\"}");
                insert(p, "plugin.descriptions", "{\"Essentials\":\"Basics\"}");
                insert(p, "plugin.state.maintenance", "true");
                insert(p, "plugin.state.globalChatMuted", "false");
                insert(p, "updater.cachedLatestVersionName", "\"9.9.9\"");
            }
        }

        LegacyDataImporter.LegacyData data = LegacyDataImporter.read(db);

        assertEquals(Map.of("6f1c0b1e-0000-0000-0000-000000000001", "de"), data.playerLanguages());
        assertEquals(Map.of("Essentials", "Basics"), data.pluginDescriptions());
        assertEquals(Map.of("maintenance", true, "globalChatMuted", false), data.flags());
    }

    @Test
    void aMissingTableGivesEmptyData() throws Exception {
        File db = dir.resolve("empty.db").toFile();
        DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath()).close();
        LegacyDataImporter.LegacyData data = LegacyDataImporter.read(db);
        assertTrue(data.playerLanguages().isEmpty() && data.pluginDescriptions().isEmpty() && data.flags().isEmpty());
    }

    private static void insert(PreparedStatement p, String key, String json) throws Exception {
        p.setString(1, key);
        p.setString(2, json);
        p.executeUpdate();
    }
}
