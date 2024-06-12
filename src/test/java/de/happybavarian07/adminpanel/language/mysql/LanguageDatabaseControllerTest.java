package de.happybavarian07.adminpanel.language.mysql;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LanguageDatabaseControllerTest {
    @Mock
    private AdminPanelMain plugin;

    private LanguageDatabaseController languageDatabaseController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        languageDatabaseController = new LanguageDatabaseController(plugin, new File("test.db"), false, "sqlite");
    }

    @AfterEach
    public void tearDown() {
        languageDatabaseController.getConnectionManager().closeConnection();
    }

    @Test
    public void shouldAddLanguageSuccessfully() {
        UUID languageID = UUID.randomUUID();
        Language language = new Language(languageID, "en", "English", "1.0", "English Language", "en.yml");

        languageDatabaseController.getInnerLanguageManager().addLanguage(languageID, language, true);

        Language addedLanguage = languageDatabaseController.getInnerLanguageManager().getLanguage(languageID);
        assertEquals(language, addedLanguage);
    }

    @Test
    public void shouldRemoveLanguageSuccessfully() {
        UUID languageID = UUID.randomUUID();
        Language language = new Language(languageID, "en", "English", "1.0", "English Language", "en.yml");

        languageDatabaseController.getInnerLanguageManager().addLanguage(languageID, language, true);
        languageDatabaseController.getInnerLanguageManager().removeLanguage(languageID);

        Language removedLanguage = languageDatabaseController.getInnerLanguageManager().getLanguage(languageID);
        assertNull(removedLanguage);
    }

    @Test
    public void shouldUpdateLanguageSuccessfully() {
        UUID languageID = UUID.randomUUID();
        Language language = new Language(languageID, "en", "English", "1.0", "English Language", "en.yml");

        languageDatabaseController.getInnerLanguageManager().addLanguage(languageID, language, true);

        Language updatedLanguage = new Language(languageID, "de", "German", "1.0", "German Language", "de.yml");
        languageDatabaseController.getInnerLanguageManager().updateLanguage(languageID, updatedLanguage);

        Language retrievedLanguage = languageDatabaseController.getInnerLanguageManager().getLanguage(languageID);
        assertEquals(updatedLanguage, retrievedLanguage);
    }

    @Test
    public void shouldHandleNonExistentLanguage() {
        UUID languageID = UUID.randomUUID();

        Language retrievedLanguage = languageDatabaseController.getInnerLanguageManager().getLanguage(languageID);
        assertNull(retrievedLanguage);
    }

    @Test
    public void testLanguageExistsMethod() {
        UUID languageID = UUID.randomUUID();
        Language language = new Language(languageID, "en", "English", "1.0", "English Language", "en.yml");

        languageDatabaseController.getInnerLanguageManager().addLanguage(languageID, language, true);

        boolean exists = languageDatabaseController.getInnerLanguageManager().languageExists(languageID);
        assertTrue(exists);

        languageDatabaseController.getInnerLanguageManager().removeLanguage(languageID);

        exists = languageDatabaseController.getInnerLanguageManager().languageExists(languageID);
        assertFalse(exists);
    }

    @Test
    public void testGetLanguageIDsMethod() {
        UUID languageID1 = UUID.randomUUID();
        UUID languageID2 = UUID.randomUUID();
        UUID languageID3 = UUID.randomUUID();
        Language language1 = new Language(languageID1, "en", "English", "1.0", "English Language", "en.yml");
        Language language2 = new Language(languageID2, "de", "German", "1.0", "German Language", "de.yml");
        Language language3 = new Language(languageID3, "fr", "French", "1.0", "French Language", "fr.yml");

        int initialSize = languageDatabaseController.getInnerLanguageManager().getLanguageIDs().size();

        languageDatabaseController.getInnerLanguageManager().addLanguage(languageID1, language1, false);
        languageDatabaseController.getInnerLanguageManager().addLanguage(languageID2, language2, false);
        languageDatabaseController.getInnerLanguageManager().addLanguage(languageID3, language3, false);

        assertEquals(3, languageDatabaseController.getInnerLanguageManager().getLanguageIDs().size() - initialSize);

        languageDatabaseController.getInnerLanguageManager().removeLanguage(languageID1);
        languageDatabaseController.getInnerLanguageManager().removeLanguage(languageID2);
        languageDatabaseController.getInnerLanguageManager().removeLanguage(languageID3);
    }

    @Test
    public void testLanguageContentExistsMethod() {
        UUID languageID = UUID.randomUUID();
        Language language = new Language(languageID, "en", "English", "1.0", "English Language", "en.yml");

        languageDatabaseController.getInnerLanguageManager().addLanguage(languageID, language, true);

        System.out.println(languageDatabaseController.getInnerLanguageManager().getConverterManager().getLanguageConverter(languageID).convertKeyToDatabaseFormat("Messages.Plugin.EnablingMessage"));
        boolean exists = languageDatabaseController.getInnerLanguageManager().getContentManager().languageContentExists(languageID, "Plugin.EnablingMessage");
        assertTrue(exists);

        // Get the language content Value
        String value = languageDatabaseController.getInnerLanguageManager().getContentManager().getContentValue(languageID, "Plugin.EnablingMessage");
        assertEquals("[Admin-Panel] successfully enabled!", value);

        languageDatabaseController.getInnerLanguageManager().getContentManager().removeLanguageContent(languageID, "Plugin.EnablingMessage");

        exists = languageDatabaseController.getInnerLanguageManager().getContentManager().languageContentExists(languageID, "Plugin.EnablingMessage");
        assertFalse(exists);

        languageDatabaseController.getInnerLanguageManager().removeLanguage(languageID);
    }
}