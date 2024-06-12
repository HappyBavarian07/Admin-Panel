package de.happybavarian07.adminpanel.language.mysql;/*
 * @Author HappyBavarian07
 * @Date 20.02.2024 | 19:05
 */

import de.happybavarian07.adminpanel.language.mysql.controller.ConnectionManager;
import de.happybavarian07.adminpanel.language.mysql.controller.InnerLanguageManager;
import de.happybavarian07.adminpanel.language.mysql.controller.SettingsManager;
import de.happybavarian07.adminpanel.language.mysql.utils.CustomThreadFactory;
import de.happybavarian07.adminpanel.main.AdminPanelMain;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LanguageDatabaseController {
    // Table Schema Language List: ID, Language, LanguageCode, LanguageName, LanguageVersion, LanguageDescription, LanguageFile
    // Table Schema Language Content (unique table for each language): ID, LanguageID, LanguageKey, LanguageValue

    private static LanguageDatabaseController instance;
    private final ConnectionManager connectionManager;
    private final SettingsManager settingsManager;
    private final InnerLanguageManager innerLanguageManager;
    private final AdminPanelMain plugin;

    private final ExecutorService executorService = Executors.newCachedThreadPool(new CustomThreadFactory("LanguageDatabaseController-", true));

    /**
     * Constructor for the LanguageDatabaseController
     *
     * @param propertiesOrDatabaseFile The properties file or the database file
     * @param isPropertiesFile         If true, the properties file will be used to create a connection to the database. If false, the database file will be used to create a connection to the database
     */
    public LanguageDatabaseController(AdminPanelMain plugin, File propertiesOrDatabaseFile, boolean isPropertiesFile, String mysqlSchema) {
        instance = this;
        this.plugin = plugin;

        connectionManager = new ConnectionManager(plugin, propertiesOrDatabaseFile, isPropertiesFile, mysqlSchema);

        // Old Connection Code
        /*if (mysqlSchema.equals("mysql") || mysqlSchema.equals("mariadb")) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                ignoreString = "IGNORE";
            } catch (ClassNotFoundException ignored) {
                throw new RuntimeException("Failed to load the MySQL driver while trying to determine the schema type of the database");
            }
        } else if (mysqlSchema.equals("sqlite")) {
            ignoreString = "OR IGNORE";
        } else {
            // Use the mysql schema as default and hope for the best
            ignoreString = "IGNORE";
        }

        if (isPropertiesFile) {
            if (mysqlSchema.equals("sqlite")) {
                // Load the properties file and create a connection to the database
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(propertiesOrDatabaseFile)) {
                    props.load(fis);
                    // Get URL from properties file and create a connection to the database
                    String url = "jdbc:sqlite:" + Paths.get(plugin.getDataFolder().getAbsolutePath(), props.getProperty("sqlite_path_language"));
                    tablePrefix = props.getProperty("mysql_prefix");
                    this.connection = DriverManager.getConnection(url);
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Failed to create a connection to the database using the properties file");
                }
            } else {
                // Load the properties file and create a connection to the database
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(propertiesOrDatabaseFile)) {
                    props.load(fis);
                    String url = "jdbc:" +
                            props.getProperty("dbtype") + "://" +
                            props.getProperty("mysql_host") + ":" +
                            props.getProperty("mysql_port") + "/" +
                            props.getProperty("mysql_database");
                    String user = props.getProperty("mysql_user");
                    String password = props.getProperty("mysql_password");
                    tablePrefix = props.getProperty("mysql_prefix");
                    DriverManager.registerDriver(new Driver());
                    this.connection = DriverManager.getConnection(url, user, password);
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Failed to create a connection to the database using the properties file");
                }
            }
        } else {
            // Create a connection to the database file and create the tables if they don't exist
            String url = "jdbc:sqlite:" + propertiesOrDatabaseFile.getAbsolutePath();
            tablePrefix = "adminpanel_";
            if (!propertiesOrDatabaseFile.exists()) {
                try {
                    propertiesOrDatabaseFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Failed to create the database file");
                }
            }
            try {
                this.connection = DriverManager.getConnection(url);
                DriverManager.drivers().forEach(driver -> System.out.println(driver.getClass().getName()));
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to create a connection to the database using the database file");
            }
        }*/

        settingsManager = new SettingsManager(connectionManager);

        // Language Manager
        innerLanguageManager = new InnerLanguageManager(plugin, connectionManager, executorService);

        // Load default languages from resources into inputstream and then into the database
        //addLanguage(UUID.fromString("d6ef09c8-8cb9-4335-85be-1e95d81d6e93"), "en", "English", "1.0", "The English language", "en.yml");
        //addLanguage(UUID.fromString("34a3eaac-5fd8-4d82-950e-e52153529b53"), "de", "Deutsch", "1.0", "The English language", "en.yml");
    }

    public static LanguageDatabaseController getInstance() {
        return instance;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public InnerLanguageManager getInnerLanguageManager() {
        return innerLanguageManager;
    }



    public ExecutorService getExecutorService() {
        return executorService;
    }

    public String getPrefix() {
        return settingsManager.getSetting("prefix");
    }

    public void setPrefix(String prefix) {
        settingsManager.updateSetting("prefix", prefix);
    }

    public Language getCurrentLanguage() {
        return innerLanguageManager.getLanguage(UUID.fromString(settingsManager.getSetting("currentLanguageID")));
    }

    public void setCurrentLanguage(String languageShort) {
        settingsManager.updateSetting("currentLanguageName", languageShort);
        settingsManager.updateSetting("currentLanguageID", innerLanguageManager.getLanguageIDByShortname(languageShort).toString());
    }

    public void resetCurrentLanguage() {
        settingsManager.updateSetting("currentLanguageName", settingsManager.getSetting("defaultLanguage"));
        settingsManager.updateSetting("currentLanguageID", innerLanguageManager.getLanguageIDByShortname(settingsManager.getSetting("defaultLanguage")).toString());
    }
}
