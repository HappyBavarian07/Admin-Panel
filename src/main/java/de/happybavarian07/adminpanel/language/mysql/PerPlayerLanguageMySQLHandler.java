package de.happybavarian07.adminpanel.language.mysql;/*
 * @Author HappyBavarian07
 * @Date 24.02.2024 | 20:05
 */

import de.happybavarian07.adminpanel.language.LanguageFile;
import de.happybavarian07.adminpanel.language.PerPlayerLanguageHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PerPlayerLanguageMySQLHandler extends PerPlayerLanguageHandler {
    private final MySQLLanguageManager lgm;
    private final LanguageDatabaseController databaseController;

    public PerPlayerLanguageMySQLHandler(MySQLLanguageManager lgm, LanguageDatabaseController databaseController) {
        super(lgm, null, null);
        this.lgm = lgm;
        this.databaseController = databaseController;

    }

    public void setupDatabaseTable() {
        try {
            databaseController.getConnectionManager().executeUpdate("CREATE TABLE IF NOT EXISTS adminpanel_perplayerlanguage (UUID VARCHAR(100) PRIMARY KEY, language VARCHAR(10), LanguageID VARCHAR(100))");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LanguageFile getPlayerLanguageFile(UUID uuid) {
        // Execute a query to get the language of the player with the given UUID
        String languageName = getPlayerLanguageName(uuid);
        UUID languageID = databaseController.getInnerLanguageManager().getLanguageIDByShortname(languageName);
        return databaseController.getInnerLanguageManager().getConverterManager().getLanguageConverter(languageID).getLanguageFile();
    }

    public Language getPlayerLanguage(UUID uuid) {
        return lgm.getLanguage(getPlayerLanguageName(uuid), false);
    }

    @Override
    public String getPlayerLanguageName(UUID uuid) {
        try {
            ResultSet resultSet = databaseController.getConnectionManager().executeQuery("SELECT language FROM adminpanel_perplayerlanguage WHERE UUID = ?", uuid.toString());
            while (resultSet.next()) {
                return resultSet.getString("language");
            }
            return lgm.getCurrentLangName();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removePlayerLanguage(UUID uuid) {
        try {
            databaseController.getConnectionManager().executeUpdate("DELETE FROM adminpanel_perplayerlanguage WHERE UUID = ?", uuid.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPlayerLanguage(UUID uuid, String language, UUID languageID) {
        try {
            databaseController.getConnectionManager().executeUpdate("INSERT INTO adminpanel_perplayerlanguage (UUID, language, LanguageID) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE language = ?, LanguageID = ?", uuid.toString(), language, languageID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setPlayerLanguage(UUID uuid, String language) {
        try {
            databaseController.getConnectionManager().executeUpdate("INSERT INTO adminpanel_perplayerlanguage (UUID, language) VALUES (?, ?) ON DUPLICATE KEY UPDATE language = ?", uuid.toString(), language);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<UUID, LanguageFile> getPlayerLanguages() {
        // Query the database for all players and their languages
        Map<UUID, LanguageFile> playerLanguages = new HashMap<>();
        try (ResultSet result = databaseController.getConnectionManager().executeQuery("SELECT * FROM adminpanel_perplayerlanguage")) {
            // use the ResultSet to create a Map of UUIDs to LanguageFiles and return it
            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString("UUID"));
                UUID languageID = UUID.fromString(result.getString("LanguageID"));
                playerLanguages.put(uuid, databaseController.getInnerLanguageManager().getConverterManager().getLanguageConverter(languageID).getLanguageFile());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return playerLanguages;
    }

    public Map<UUID, Language> getPlayerLanguagesAsLanguage() {
        // Query the database for all players and their languages
        Map<UUID, Language> playerLanguages = new HashMap<>();
        try (ResultSet result = databaseController.getConnectionManager().executeQuery("SELECT * FROM adminpanel_perplayerlanguage")) {
            // use the ResultSet to create a Map of UUIDs to LanguageFiles and return it
            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString("UUID"));
                playerLanguages.put(uuid, lgm.getLanguage(result.getString("language"), false));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return playerLanguages;
    }
}
