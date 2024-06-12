package de.happybavarian07.adminpanel.language.mysql.controller;/*
 * @Author HappyBavarian07
 * @Date 08.05.2024 | 16:46
 */

import de.happybavarian07.adminpanel.language.mysql.Language;
import de.happybavarian07.adminpanel.language.mysql.LanguageConverter;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefix;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class InnerLanguageManager {
    private final AdminPanelMain plugin;
    private final ConnectionManager connectionManager;
    private final ConverterManager converterManager;
    private final ContentManager contentManager;
    // List of all languages in the database (gets filled on startup)
    private final Map<UUID, Language> languageMap = new HashMap<>();
    // Map of all language names to UUIDs (gets filled on startup)
    private final Map<UUID, String> languageNameToUUIDMap = new HashMap<>();
    // Map of all language shortnames to UUIDs (gets filled on startup)
    private final Map<UUID, String> languageShortnameToUUIDMap = new HashMap<>();
    private final Map<UUID, ReentrantLock> locksMap = new HashMap<>();

    private final ExecutorService executorService;

    public InnerLanguageManager(AdminPanelMain plugin, ConnectionManager connectionManager, ExecutorService executorService) {
        this.plugin = plugin;
        this.connectionManager = connectionManager;
        this.converterManager = new ConverterManager(plugin, this);
        this.contentManager = new ContentManager(plugin, this, executorService, connectionManager);
        this.executorService = executorService;

        createLanguageListTable();

        for (UUID languageID : getLanguageIDs()) {
            contentManager.createLanguageContentTable(languageID);
            locksMap.put(languageID, new ReentrantLock());
            contentManager.addDownloadedContent(languageID, new HashMap<>());
        }
    }

    public ContentManager getContentManager() {
        return contentManager;
    }

    public Map<UUID, ReentrantLock> getLocksMap() {
        return locksMap;
    }

    public ReentrantLock getLock(UUID languageID) {
        return locksMap.get(languageID);
    }

    public void addLock(UUID languageID) {
        locksMap.put(languageID, new ReentrantLock());
    }

    public void removeLock(UUID languageID) {
        locksMap.remove(languageID);
    }

    public void clearLocks() {
        locksMap.clear();
    }

    public Map<UUID, Language> getLanguageMap() {
        try {
            String ids = languageMap.keySet().stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining("','", "'", "'"));
            var resultSet = connectionManager.prepareStatement("SELECT * FROM {table_prefix}LanguageList WHERE ID NOT IN (" + ids + ")").executeQuery();
            while (resultSet.next()) {
                UUID languageID = UUID.fromString(resultSet.getString("ID"));
                String languageShort = resultSet.getString("LanguageShort");
                String languageName = resultSet.getString("LanguageName");
                String languageVersion = resultSet.getString("LanguageVersion");
                String languageDescription = resultSet.getString("LanguageDescription");
                String languageFilePath = resultSet.getString("languageFilePath");
                languageMap.putIfAbsent(languageID, new Language(languageID, languageShort, languageName, languageVersion, languageDescription, languageFilePath));
            }
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to get the language map from the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
            plugin.getLogger().log(Level.SEVERE, "Failed to get the language map from the database: " + e);
        }
        return languageMap;
    }

    public List<UUID> getLanguageIDs() {
        // Query data from the database
        List<UUID> languageIDs = new ArrayList<>();
        String ids = languageMap.keySet().stream()
                .map(UUID::toString)
                .collect(Collectors.joining("','", "'", "'"));
        try {
            ResultSet resultSet = connectionManager.prepareStatement("SELECT ID, LanguageName FROM {table_prefix}LanguageList WHERE ID NOT IN (" + ids + ")").executeQuery();
            while (resultSet.next()) {
                languageIDs.add(UUID.fromString(resultSet.getString("ID")));
            }
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to get the language IDs from the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
            plugin.getLogger().log(Level.SEVERE, "Failed to get the language IDs from the database: " + e);
        }
        return languageIDs;
    }

    public Map<UUID, String> getLanguageNameToUUIDMap() {
        try {
            String ids = languageNameToUUIDMap.keySet().stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining("','", "'", "'"));
            ResultSet resultSet = connectionManager.prepareStatement("SELECT ID, LanguageName FROM {table_prefix}LanguageList WHERE ID NOT IN (" + ids + ")").executeQuery();
            while (resultSet.next()) {
                UUID languageID = UUID.fromString(resultSet.getString("ID"));
                String languageName = resultSet.getString("LanguageName");
                languageNameToUUIDMap.putIfAbsent(languageID, languageName);
            }
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to get the language name to UUID map from the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
            plugin.getLogger().log(Level.SEVERE, "Failed to get the language name to UUID map from the database: " + e);
        }
        return languageNameToUUIDMap;
    }

    public Map<UUID, String> getLanguageShortnameToUUIDMap() {
        try {
            String ids = languageShortnameToUUIDMap.keySet().stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining("','", "'", "'"));
            ResultSet resultSet = connectionManager.prepareStatement("SELECT ID, LanguageShort FROM {table_prefix}LanguageList WHERE ID NOT IN (" + ids + ")").executeQuery();
            while (resultSet.next()) {
                UUID languageID = UUID.fromString(resultSet.getString("ID"));
                String languageShort = resultSet.getString("LanguageShort");
                languageShortnameToUUIDMap.putIfAbsent(languageID, languageShort);
            }
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to get the language shortname to UUID map from the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
            plugin.getLogger().log(Level.SEVERE, "Failed to get the language shortname to UUID map from the database: " + e);
        }
        return languageShortnameToUUIDMap;
    }

    public UUID getLanguageID(String languageName) {
        if (!languageNameToUUIDMap.containsValue(languageName)) {
            // Query data from the database
            try {
                ResultSet resultSet = connectionManager.prepareStatement("SELECT ID FROM {table_prefix}LanguageList WHERE LanguageName = '" + languageName + "'").executeQuery();
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("ID"));
                }
            } catch (SQLException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to get the language ID from the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                plugin.getLogger().log(Level.SEVERE, "Failed to get the language ID from the database: " + e);
            }
            return null;
        }
        return languageNameToUUIDMap.entrySet().stream().filter(entry -> entry.getValue().equals(languageName)).findFirst().map(Map.Entry::getKey).orElse(null);
    }

    public UUID getLanguageIDByShortname(String languageShortname) {
        if (!languageShortnameToUUIDMap.containsValue(languageShortname)) {
            // Query data from the database
            try {
                ResultSet resultSet = connectionManager.prepareStatement("SELECT ID FROM {table_prefix}LanguageList WHERE LanguageShort = '" + languageShortname + "'").executeQuery();
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("ID"));
                }
            } catch (SQLException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to get the language ID by shortname from the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                plugin.getLogger().log(Level.SEVERE, "Failed to get the language ID by shortname from the database: " + e);
            }
            return null;
        }
        return languageShortnameToUUIDMap.entrySet().stream().filter(entry -> entry.getValue().equals(languageShortname)).findFirst().map(Map.Entry::getKey).orElse(null);
    }

    public String getLanguageName(UUID languageID) {
        if (!languageNameToUUIDMap.containsKey(languageID)) {
            // Query data from the database
            try {
                ResultSet resultSet = connectionManager.prepareStatement("SELECT LanguageName FROM {table_prefix}LanguageList WHERE ID = '" + languageID.toString() + "'").executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("LanguageName");
                }
            } catch (SQLException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to get the language name from the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                plugin.getLogger().log(Level.SEVERE, "Failed to get the language name from the database: " + e);
            }
        }
        return languageNameToUUIDMap.get(languageID);
    }

    public String getLanguageShortname(UUID languageID) {
        if (!languageShortnameToUUIDMap.containsKey(languageID)) {
            // Query data from the database
            try {
                ResultSet resultSet = connectionManager.prepareStatement("SELECT LanguageShort FROM {table_prefix}LanguageList WHERE ID = '" + languageID.toString() + "'").executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("LanguageShort");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return languageShortnameToUUIDMap.get(languageID);
    }

    public Language getLanguage(UUID languageID) {
        if (!languageMap.containsKey(languageID)) {
            // Query data from the database
            try {
                ResultSet resultSet = connectionManager.prepareStatement("SELECT * FROM {table_prefix}LanguageList WHERE ID = '" + languageID.toString() + "'").executeQuery();
                if (resultSet.next()) {
                    String languageShort = resultSet.getString("LanguageShort");
                    String languageName = resultSet.getString("LanguageName");
                    String languageVersion = resultSet.getString("LanguageVersion");
                    String languageDescription = resultSet.getString("LanguageDescription");
                    String languageFilePath = resultSet.getString("LanguageFilePath");
                    return new Language(languageID, languageShort, languageName, languageVersion, languageDescription, languageFilePath);
                }
            } catch (SQLException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to get the language from the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                plugin.getLogger().log(Level.SEVERE, "Failed to get the language from the database: " + e);
            }
            return null;
        }
        return languageMap.get(languageID);
    }

    public boolean languageExists(UUID languageID) {
        if (!languageMap.containsKey(languageID)) {
            // Query data from the database
            try {
                ResultSet resultSet = connectionManager.prepareStatement("SELECT * FROM {table_prefix}LanguageList WHERE ID = '" + languageID.toString() + "'").executeQuery();
                return resultSet.next();
            } catch (SQLException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to check if the language exists in the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                plugin.getLogger().log(Level.SEVERE, "Failed to check if the language exists in the database: " + e);
            }
            return false;
        } else {
            return true;
        }
    }

    public CompletableFuture<Boolean> addLanguage(UUID languageID, String language, String languageName, String languageVersion, String languageDescription, String languageFile, boolean autoUploadContent) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        executorService.submit(() -> {
            try {
                connectionManager.prepareStatement("INSERT {ignore} INTO {table_prefix}LanguageList (ID, LanguageShort, LanguageName, LanguageVersion, LanguageDescription, LanguageFilePath) " +
                        "VALUES ('" + languageID.toString() + "', '" + language + "', '" + languageName + "', '" + languageVersion + "', " +
                        "'" + languageDescription + "', '" + languageFile + "')").executeUpdate();
                CompletableFuture<LanguageConverter> converterFuture = new CompletableFuture<>();

                executorService.submit(() -> {
                    // Code to set up LanguageConverter
                    converterManager.createLanguageConverter(languageID, language);
                    converterFuture.complete(converterManager.getLanguageConverter(languageID));
                });
                converterManager.addLanguageConverterFuture(languageID, converterFuture);
                contentManager.createLanguageContentTable(languageID);
                locksMap.put(languageID, new ReentrantLock());
                contentManager.addDownloadedContent(languageID, new HashMap<>());
                if (autoUploadContent) {
                    LanguageConverter languageConverter;
                    if (!converterFuture.isDone()) {
                        // If the setup is not done, wait for it to complete
                        languageConverter = converterFuture.get(); // This will block until the future is done
                    } else {
                        languageConverter = converterManager.getLanguageConverter(languageID);
                    }
                    CompletableFuture<Boolean> contentFuture = contentManager.addLanguageContentAsync(languageID, languageConverter.convertLanguageToDatabaseFormat(true, null, true, null));
                    contentFuture.thenAccept(future::complete);
                } else {
                    future.complete(true);
                }
            } catch (SQLException | InterruptedException | ExecutionException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to add the language to the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                plugin.getLogger().log(Level.SEVERE, "Failed to add the language to the database: " + e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public CompletableFuture<Boolean> addLanguage(UUID languageID, Language language, boolean autoUploadContent) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        executorService.submit(() -> {
            try {
                connectionManager.prepareStatement("INSERT {ignore} INTO {table_prefix}LanguageList (ID, LanguageShort, LanguageName, LanguageVersion, LanguageDescription, LanguageFilePath) " +
                        "VALUES ('" + languageID.toString() + "', '" + language.getLanguageShort() + "', '" + language.getLanguageName() + "', " +
                        "'" + language.getLanguageVersion() + "', '" + language.getLanguageDescription() + "', '" + language.getLanguageFilePath() + "')").executeUpdate();
                CompletableFuture<LanguageConverter> converterFuture = new CompletableFuture<>();

                executorService.submit(() -> {
                    // Code to set up LanguageConverter
                    converterManager.createLanguageConverter(languageID, language.getLanguageShort());
                    converterFuture.complete(converterManager.getLanguageConverter(languageID));
                });
                converterManager.addLanguageConverterFuture(languageID, converterFuture);
                contentManager.createLanguageContentTable(languageID);
                locksMap.put(languageID, new ReentrantLock());
                contentManager.addDownloadedContent(languageID, new HashMap<>());
                if (autoUploadContent) {
                    LanguageConverter languageConverter;
                    if (!converterFuture.isDone()) {
                        // If the setup is not done, wait for it to complete
                        languageConverter = converterFuture.get(); // This will block until the future is done
                    } else {
                        languageConverter = converterManager.getLanguageConverter(languageID);
                    }
                    CompletableFuture<Boolean> contentFuture = contentManager.addLanguageContentAsync(languageID, languageConverter.convertLanguageToDatabaseFormat(true, null, true, null));
                    contentFuture.thenAccept(future::complete);
                } else {
                    future.complete(true);
                }
            } catch (SQLException | InterruptedException | ExecutionException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to add the language to the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                plugin.getLogger().log(Level.SEVERE, "Failed to add the language to the database: " + e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public void removeLanguage(UUID languageID) {
        try {
            connectionManager.prepareStatement("DELETE FROM {table_prefix}LanguageList WHERE ID = '" + languageID.toString() + "'").executeUpdate();
            connectionManager.prepareStatement("DROP TABLE IF EXISTS {table_prefix}" + getLanguageShortname(languageID) + "_content").executeUpdate();
            languageMap.remove(languageID);
            languageNameToUUIDMap.values().removeIf(value -> value.equals(getLanguageName(languageID)));
            languageShortnameToUUIDMap.values().removeIf(value -> value.equals(getLanguageShortname(languageID)));
            contentManager.removeLanguageContent(languageID);
            converterManager.removeLanguageConverter(languageID);
            locksMap.remove(languageID);
            converterManager.removeLanguageConverterFuture(languageID);
            contentManager.clearDownloadedContent(languageID);
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to remove the language from the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
            plugin.getLogger().log(Level.SEVERE, "Failed to remove the language from the database: " + e);
        }
    }

    public void updateLanguage(UUID languageID, String language, String languageName, String languageVersion, String languageDescription, String languageFile) {
        try {
            connectionManager.prepareStatement("UPDATE {table_prefix}LanguageList SET LanguageShort = '" + language + "', LanguageName = '" + languageName + "', " +
                    "LanguageVersion = '" + languageVersion + "', LanguageDescription = '" + languageDescription + "', LanguageFilePath = " +
                    "'" + languageFile + "' WHERE ID = '" + languageID.toString() + "'").executeUpdate();
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to update the language in the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
            plugin.getLogger().log(Level.SEVERE, "Failed to update the language in the database: " + e);
        }
    }

    public void updateLanguage(UUID languageID, Language language) {
        try {
            connectionManager.prepareStatement("UPDATE {table_prefix}LanguageList SET LanguageShort = '" + language.getLanguageShort() + "', " +
                    "LanguageName = '" + language.getLanguageName() + "', LanguageVersion = '" + language.getLanguageVersion() + "', " +
                    "LanguageDescription = '" + language.getLanguageDescription() + "', LanguageFilePath = '" + language.getLanguageFilePath() +
                    "' WHERE ID = '" + languageID.toString() + "'").executeUpdate();
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to update the language in the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
            plugin.getLogger().log(Level.SEVERE, "Failed to update the language in the database: " + e);
        }
    }

    public void createLanguageListTable() {
        connectionManager.createTable("CREATE TABLE IF NOT EXISTS {table_prefix}LanguageList (ID VARCHAR(255) PRIMARY KEY NOT NULL, " +
                "LanguageShort VARCHAR(255) UNIQUE , LanguageName VARCHAR(255) UNIQUE , LanguageVersion VARCHAR(30), " +
                "LanguageDescription TEXT, LanguageFilePath TEXT UNIQUE)");
    }

    public ConverterManager getConverterManager() {
        return converterManager;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }
}
