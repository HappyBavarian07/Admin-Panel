package de.happybavarian07.adminpanel.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/** Reads what the removed DataService stored in plugin_data.db, so it can be moved to the new tables once. */
public final class LegacyDataImporter {
    private static final Gson GSON = new Gson();
    private static final String STATE_PREFIX = "plugin.state.";

    private LegacyDataImporter() {
    }

    public record LegacyData(Map<String, String> playerLanguages, Map<String, String> pluginDescriptions,
                             Map<String, Boolean> flags) {
    }

    /** The old file was created relative to the server folder; check the plugin folder first. */
    public static File findLegacyFile(File dataFolder) {
        File inPluginFolder = new File(dataFolder, "plugin_data.db");
        if (inPluginFolder.isFile()) return inPluginFolder;
        File serverRoot = dataFolder.getAbsoluteFile().getParentFile().getParentFile();
        File inServerRoot = new File(serverRoot, "plugin_data.db");
        return inServerRoot.isFile() ? inServerRoot : null;
    }

    public static LegacyData read(File sqliteFile) throws SQLException {
        Map<String, String> languages = new HashMap<>();
        Map<String, String> descriptions = new HashMap<>();
        Map<String, Boolean> flags = new HashMap<>();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile.getAbsolutePath());
             Statement statement = connection.createStatement()) {
            ResultSet tables = connection.getMetaData().getTables(null, null, "data", null);
            if (!tables.next()) return new LegacyData(languages, descriptions, flags);
            try (ResultSet rows = statement.executeQuery("SELECT id, value FROM data")) {
                while (rows.next()) {
                    String key = rows.getString(1);
                    String json = rows.getString(2);
                    if (key == null || json == null || json.isEmpty()) continue;
                    if (key.equals("language.players")) {
                        languages.putAll(GSON.fromJson(json, new TypeToken<Map<String, String>>() {}.getType()));
                    } else if (key.equals("plugin.descriptions")) {
                        descriptions.putAll(GSON.fromJson(json, new TypeToken<Map<String, String>>() {}.getType()));
                    } else if (key.startsWith(STATE_PREFIX)) {
                        flags.put(key.substring(STATE_PREFIX.length()), GSON.fromJson(json, Boolean.class));
                    }
                }
            }
        }
        return new LegacyData(languages, descriptions, flags);
    }
}
