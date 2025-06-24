package de.happybavarian07.adminpanel.mysql;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.mysql.annotations.*;
import de.happybavarian07.adminpanel.mysql.exceptions.MySQLSystemExceptions;
import de.happybavarian07.adminpanel.mysql.repository.Repository;
import de.happybavarian07.adminpanel.mysql.utils.DatabaseProperties;
import de.happybavarian07.adminpanel.mysql.utils.MySQLUtils;
import de.happybavarian07.adminpanel.mysql.utils.RepositoryProxy;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class RepositoryController {
    private final SQLExecutor sqlExecutor;
    private final Map<Class<?>, Repository<?, ?>> repositories = new HashMap<>();
    private final Map<String, Class<?>> entityClasses = new HashMap<>();
    private final DatabaseProperties dbProperties;
    private final File defaultRegistrationFile;

    public RepositoryController(File defaultRegistrationFile, DatabaseProperties dbProperties) {
        this.defaultRegistrationFile = defaultRegistrationFile;
        this.dbProperties = dbProperties;
        this.sqlExecutor = new SQLExecutor(dbProperties);

        // Verbindung zur Datenbank herstellen
        setupDefaultConnection();
    }

    private void setupDefaultConnection() {
        try {
            String url = dbProperties.getConnectionString();
            String username = dbProperties.getUsername();
            String password = dbProperties.getPassword();

            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("Database connection URL is empty or null.");
            }

            Connection connection = MySQLUtils.createConnection(url, username, password);
            sqlExecutor.addConnection("default", connection);
            logInfo("Default connection established with driver: " + dbProperties.getDriver());
        } catch (Exception e) {
            logSevere("Failed to establish default database connection: " + e.getMessage(), e);
        }
    }

    /**
     * Registriert eine zusätzliche Verbindung mit einem benutzerdefinierten Namen.
     *
     * @param name                 Name der Verbindung
     * @param connectionProperties Verbindungseigenschaften
     */
    public void addConnection(String name, DatabaseProperties connectionProperties) {
        try {
            String url = connectionProperties.getConnectionString();
            String username = connectionProperties.getUsername();
            String password = connectionProperties.getPassword();

            Connection connection = MySQLUtils.createConnection(url, username, password);
            sqlExecutor.addConnection(name, connection);
            logInfo("Added connection '" + name + "' with driver: " + connectionProperties.getDriver());
        } catch (Exception e) {
            logSevere("Failed to add connection '" + name + "': " + e.getMessage(), e);
        }
    }

    /**
     * Setzt die zu verwendende Verbindung.
     *
     * @param name Name der Verbindung
     */
    public void setDefaultConnection(String name) {
        try {
            sqlExecutor.setDefaultConnection(name);
            logInfo("Set default connection to: " + name);
        } catch (IllegalArgumentException e) {
            logWarning("Failed to set default connection: " + e.getMessage());
        }
    }

    public void setDatabasePrefix(String prefix) {
        dbProperties.setDatabasePrefix(prefix);
        sqlExecutor.setDatabasePrefix(prefix);

        for (Repository<?, ?> repository : repositories.values()) {
            if (repository instanceof RepositoryProxy) {
                ((RepositoryProxy) repository).setDatabasePrefix(prefix);
            }
        }
    }

    /**
     * Registriert ein Repository-Interface und gibt eine Implementierung zurück.
     *
     * @param <T>                 Repository-Typ
     * @param <E>                 Entity-Typ
     * @param <ID>                ID-Typ
     * @param repositoryInterface Repository-Interface
     * @param entityClass         Entity-Klasse
     * @return Implementierung des Repository-Interfaces
     */
    public <T extends Repository<E, ID>, E, ID> T registerRepository(Class<T> repositoryInterface, Class<E> entityClass) {
        if (repositories.containsKey(repositoryInterface) && repositories.get(repositoryInterface) != null &&
                repositories.get(repositoryInterface).getClass().isAssignableFrom(repositoryInterface)) {
            logWarning("Repository " + repositoryInterface.getName() + " is already registered");
            return (T) repositories.get(repositoryInterface);
        }
        if (!Repository.class.isAssignableFrom(repositoryInterface)) {
            logWarning("Class " + repositoryInterface.getName() + " does not implement Repository interface");
            throw new IllegalArgumentException("Class must implement Repository interface");
        }
        if (!entityClass.isAnnotationPresent(Entity.class) || !entityClass.isAnnotationPresent(Table.class)) {
            logWarning("Entity class " + entityClass.getName() + " must be annotated with @Entity and @Table");
            throw new IllegalArgumentException("Entity class must be annotated with @Entity and @Table");
        }

        T repository = RepositoryProxy.create(repositoryInterface, dbProperties.getDatabasePrefix(), sqlExecutor);
        repositories.put(repositoryInterface, repository);
        entityClasses.put(entityClass.getName(), entityClass);

        logInfo("Registered repository for entity: " + entityClass.getSimpleName());

        // --- Neue Logik zur Erkennung des Primärschlüssel-Feldes ---
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                logInfo("Primärschlüssel-Feld entdeckt: " + field.getName());
                if (field.isAnnotationPresent(GeneratedValue.class)) {
                    GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
                    GenerationType strategy = generatedValue.strategy();
                    logInfo("Generierungsstrategie für " + field.getName() + ": " + strategy);
                    // Hier können Sie je nach Strategie den entsprechenden Logikpfad einfügen (z.B. IDENTITY, SEQUENCE, TABLE)
                }
            }
        }
        // ------------------------------------------------------------

        // Tabelle in der Datenbank erstellen, wenn sie nicht existiert
        try {
            sqlExecutor.generateSchema(entityClass);
            logInfo("Generated schema for entity: " + entityClass.getSimpleName());
        } catch (SQLException e) {
            logSevere("Failed to generate schema for entity " + entityClass.getSimpleName() + ": " + e.getMessage(), e);
        }

        return repository;
    }

    /**
     * Gibt ein registriertes Repository zurück.
     *
     * @param <T>                 Repository-Typ
     * @param repositoryInterface Repository-Interface
     * @return Repository-Implementierung oder null, wenn nicht gefunden
     */
    @SuppressWarnings("unchecked")
    public <T extends Repository<?, ?>> T getRepository(Class<T> repositoryInterface) {
        return (T) repositories.get(repositoryInterface);
    }

    /**
     * Prüft, ob ein Repository bereits registriert ist.
     *
     * @param repositoryInterface Repository-Interface
     * @return true, wenn das Repository registriert ist, sonst false
     */
    public boolean isRepositoryRegistered(Class<?> repositoryInterface) {
        return repositories.containsKey(repositoryInterface);
    }

    /**
     * Gibt alle registrierten Repository-Interfaces zurück.
     *
     * @return Set mit allen registrierten Repository-Interfaces
     */
    public Set<Class<?>> getAllRepositoryInterfaces() {
        return new HashSet<>(repositories.keySet());
    }

    /**
     * Gibt alle registrierten Repository-Instanzen zurück.
     *
     * @return Collection mit allen registrierten Repository-Instanzen
     */
    public Collection<Repository<?, ?>> getAllRepositories() {
        return new HashSet<>(repositories.values());
    }

    /**
     * Entfernt ein Repository aus der Registrierung.
     *
     * @param repositoryInterface Repository-Interface, das entfernt werden soll
     * @return true, wenn das Repository entfernt wurde, false wenn es nicht registriert war
     */
    public boolean unregisterRepository(Class<?> repositoryInterface) {
        if (repositories.containsKey(repositoryInterface)) {
            repositories.remove(repositoryInterface);
            logInfo("Unregistered repository: " + repositoryInterface.getName());
            return true;
        }
        return false;
    }

    /**
     * Führt eine SQL-Abfrage direkt über den SQLExecutor aus.
     *
     * @param sql    SQL-Abfrage
     * @param params Parameter für die Abfrage
     * @return ResultSet der Abfrage
     * @throws SQLException Bei Fehlern in der Abfrage
     */
    public java.sql.ResultSet executeQuery(String sql, Object... params) throws SQLException {
        return sqlExecutor.executeQuery(sql, params);
    }

    /**
     * Führt ein SQL-Update direkt über den SQLExecutor aus.
     *
     * @param sql    SQL-Update
     * @param params Parameter für das Update
     * @throws SQLException Bei Fehlern beim Update
     */
    public void executeUpdate(String sql, Object... params) throws SQLException {
        sqlExecutor.executeUpdate(sql, params);
    }

    /**
     * Führt mehrere SQL-Statements als Transaktion aus.
     *
     * @param statements Liste von SQL-Statements
     * @throws SQLException Bei Fehlern in der Transaktion
     */
    public void executeTransaction(List<String> statements) throws SQLException {
        sqlExecutor.executeTransaction(statements);
    }

    /**
     * Lädt Repository-Registrierungen aus einer Datei.
     */
    @SuppressWarnings("unchecked")
    public void loadRepositoriesFromFile() {
        if (!defaultRegistrationFile.exists()) {
            try {
                defaultRegistrationFile.createNewFile();
                // Standardinhalt erstellen
                JSONObject json = new JSONObject();
                json.put("repositories", new JSONArray());
                Files.write(defaultRegistrationFile.toPath(), json.toString(4).getBytes());
            } catch (Exception e) {
                logSevere("Failed to create repository registration file: " + e.getMessage(), e);
                return;
            }
        }

        try {
            String content = new String(Files.readAllBytes(defaultRegistrationFile.toPath()));
            if (content.isEmpty()) {
                content = "{}";
            }

            JSONObject json = new JSONObject(content);
            JSONArray repoArray = json.optJSONArray("repositories");

            if (repoArray == null) {
                repoArray = new JSONArray();
                json.put("repositories", repoArray);
                Files.write(defaultRegistrationFile.toPath(), json.toString(4).getBytes());
                return;
            }

            for (int i = 0; i < repoArray.length(); i++) {
                JSONObject repoObj = repoArray.getJSONObject(i);
                String repositoryClassName = repoObj.getString("repositoryClass");
                String entityClassName = repoObj.getString("entityClass");
                boolean enabled = repoObj.getBoolean("enabled");

                if (!enabled) {
                    logInfo("Repository " + repositoryClassName + " is disabled, skipping.");
                    continue;
                }

                try {
                    // Korrigiere Typkonvertierungsproblem
                    Class<?> repositoryClass = Class.forName(repositoryClassName);
                    Class<?> entityClass = Class.forName(entityClassName);

                    // Sicherstellen, dass das Repository-Interface Repository<?, ?> erweitert
                    if (Repository.class.isAssignableFrom(repositoryClass)) {
                        // Registriere ohne explizites Casting - verwende Raw-Type
                        Object repository = registerRepository(
                                (Class) repositoryClass,
                                entityClass);

                        logInfo("Repository successfully registered: " + repositoryClassName);
                    } else {
                        logWarning("Class " + repositoryClassName + " does not implement Repository interface");
                    }

                } catch (ClassNotFoundException e) {
                    logWarning("Class not found: " + e.getMessage());
                } catch (Exception e) {
                    logSevere("Failed to register repository: " + e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            logSevere("Failed to load repositories from file: " + e.getMessage(), e);
        }
    }

    /**
     * Fügt ein Repository zur Registrierungsdatei hinzu.
     *
     * @param <T>             Repository-Typ
     * @param <E>             Entity-Typ
     * @param repositoryClass Repository-Klasse
     * @param entityClass     Entity-Klasse
     * @param description     Beschreibung des Repositories
     */
    public <T extends Repository<E, ?>, E> void addRepositoryToRegistrationFile(
            Class<T> repositoryClass,
            Class<E> entityClass,
            String description,
            boolean register) {

        if (!defaultRegistrationFile.exists()) {
            try {
                defaultRegistrationFile.createNewFile();
            } catch (Exception e) {
                logSevere("Failed to create repository registration file: " + e.getMessage(), e);
                return;
            }
        }

        try {
            String content = new String(Files.readAllBytes(defaultRegistrationFile.toPath()));
            if (content.isEmpty()) {
                content = "{}";
            }

            JSONObject json = new JSONObject(content);
            JSONArray repoArray = json.optJSONArray("repositories");

            if (repoArray == null) {
                repoArray = new JSONArray();
                json.put("repositories", repoArray);
            }

            // Prüfen, ob das Repository bereits eingetragen ist
            for (int i = 0; i < repoArray.length(); i++) {
                JSONObject repoObj = repoArray.getJSONObject(i);
                if (repoObj.getString("repositoryClass").equals(repositoryClass.getName()) &&
                        repoObj.getString("entityClass").equals(entityClass.getName())) {
                    // Repository ist bereits registriert
                    return;
                }
            }

            // Repository hinzufügen
            JSONObject repoObj = new JSONObject();
            repoObj.put("repositoryClass", repositoryClass.getName());
            repoObj.put("entityClass", entityClass.getName());
            repoObj.put("enabled", true);
            repoObj.put("description", Optional.ofNullable(description)
                    .orElse("Repository for " + entityClass.getSimpleName()));

            repoArray.put(repoObj);

            // Datei schreiben
            Files.write(defaultRegistrationFile.toPath(), json.toString(4).getBytes());
            logInfo("Added repository to registration file: " + repositoryClass.getName());
            if (register) {
                registerRepository((Class) repositoryClass, (Class) entityClass);
            }
        } catch (Exception e) {
            logSevere("Failed to add repository to registration file: " + e.getMessage(), e);
        }
    }

    /**
     * Entfernt ein Repository aus der Registrierungsdatei.
     *
     * @param repositoryClass Repository-Klasse
     * @param entityClass     Entity-Klasse
     * @return true, wenn das Repository entfernt wurde, sonst false
     */
    public boolean removeRepositoryFromRegistrationFile(Class<?> repositoryClass, Class<?> entityClass) {
        if (!defaultRegistrationFile.exists()) {
            return false;
        }

        try {
            String content = new String(Files.readAllBytes(defaultRegistrationFile.toPath()));
            if (content.isEmpty()) {
                return false;
            }

            JSONObject json = new JSONObject(content);
            JSONArray repoArray = json.optJSONArray("repositories");

            if (repoArray == null) {
                return false;
            }

            boolean removed = false;
            for (int i = repoArray.length() - 1; i >= 0; i--) {
                JSONObject repoObj = repoArray.getJSONObject(i);
                if (repoObj.getString("repositoryClass").equals(repositoryClass.getName()) &&
                        repoObj.getString("entityClass").equals(entityClass.getName())) {
                    repoArray.remove(i);
                    removed = true;
                }
            }

            if (removed) {
                Files.write(defaultRegistrationFile.toPath(), json.toString(4).getBytes());
                logInfo("Removed repository from registration file: " + repositoryClass.getName());
            }

            return removed;

        } catch (Exception e) {
            logSevere("Failed to remove repository from registration file: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gibt eine Entity-Klasse anhand ihres Namens zurück.
     *
     * @param entityClassName Klassenname der Entity
     * @return Optional mit der Entity-Klasse oder leer, wenn nicht gefunden
     */
    public Optional<Class<?>> getEntityClassByName(String entityClassName) {
        return Optional.ofNullable(entityClasses.get(entityClassName));
    }

    /**
     * Aktualisiert den Status eines Repositories in der Registrierungsdatei.
     *
     * @param repositoryClass Repository-Klasse
     * @param entityClass     Entity-Klasse
     * @param enabled         Neuer Status (aktiviert/deaktiviert)
     * @return true bei erfolgreicher Aktualisierung, sonst false
     */
    public boolean updateRepositoryStatus(Class<?> repositoryClass, Class<?> entityClass, boolean enabled) {
        if (!defaultRegistrationFile.exists()) {
            return false;
        }

        try {
            String content = new String(Files.readAllBytes(defaultRegistrationFile.toPath()));
            if (content.isEmpty()) {
                return false;
            }

            JSONObject json = new JSONObject(content);
            JSONArray repoArray = json.optJSONArray("repositories");

            if (repoArray == null) {
                return false;
            }

            boolean updated = false;
            for (int i = 0; i < repoArray.length(); i++) {
                JSONObject repoObj = repoArray.getJSONObject(i);
                if (repoObj.getString("repositoryClass").equals(repositoryClass.getName()) &&
                        repoObj.getString("entityClass").equals(entityClass.getName())) {
                    repoObj.put("enabled", enabled);
                    updated = true;
                    break;
                }
            }

            if (updated) {
                Files.write(defaultRegistrationFile.toPath(), json.toString(4).getBytes());
                logInfo("Updated repository status in registration file: " + repositoryClass.getName() + " -> " + (enabled ? "enabled" : "disabled"));
            }

            return updated;

        } catch (Exception e) {
            logSevere("Failed to update repository status in registration file: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Schließt alle Datenbankverbindungen.
     */
    public void closeConnections() {
        try {
            sqlExecutor.close();
        } catch (MySQLSystemExceptions.DatabaseConnectionException e) {
            throw new RuntimeException(e);
        }
        logInfo("All database connections closed.");
    }

    /**
     * Schließt eine spezifische Datenbankverbindung.
     *
     * @param connectionName Name der zu schließenden Verbindung
     * @return true, wenn die Verbindung geschlossen wurde, sonst false
     */
    public boolean closeConnection(String connectionName) {
        Connection connection = sqlExecutor.getConnection(connectionName);
        if (connection != null) {
            try {
                connection.close();
                sqlExecutor.removeConnection(connectionName);
                logInfo("Closed database connection: " + connectionName);
                return true;
            } catch (SQLException e) {
                logSevere("Failed to close database connection: " + connectionName, e);
            }
        }
        return false;
    }

    /**
     * Gibt den verwendeten SQLExecutor zurück.
     *
     * @return SQLExecutor-Instanz
     */
    public SQLExecutor getSqlExecutor() {
        return sqlExecutor;
    }

    /**
     * Gibt die Datenbankverbindungseigenschaften zurück.
     *
     * @return Datenbankverbindungseigenschaften
     */
    public DatabaseProperties getDatabaseProperties() {
        return dbProperties;
    }

    /**
     * Gibt alle registrierten Entity-Klassen zurück.
     *
     * @return Collection aller registrierten Entity-Klassen
     */
    public Collection<Class<?>> getAllEntityClasses() {
        return new HashSet<>(entityClasses.values());
    }

    /**
     * Prüft, ob eine Entity-Klasse bereits registriert wurde.
     *
     * @param entityClassName Name der Entity-Klasse
     * @return true, wenn die Entity-Klasse registriert ist, sonst false
     */
    public boolean isEntityClassRegistered(String entityClassName) {
        return entityClasses.containsKey(entityClassName);
    }

    // Hilfsmethoden für Logging
    private void logInfo(String message) {
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, message, "RepositoryController", true);
    }

    private void logWarning(String message) {
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.WARNING, message, "RepositoryController", true);
    }

    private void logSevere(String message, Throwable e) {
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE, message + (e != null ? ": " + e.getMessage() : ""), "RepositoryController", true);
        if (e != null) {
            StringBuilder stackTrace = new StringBuilder();
            for (StackTraceElement element : e.getStackTrace()) {
                stackTrace.append(element.toString()).append("\n");
            }
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE, stackTrace.toString(), "RepositoryController", false);
        }
    }
}
