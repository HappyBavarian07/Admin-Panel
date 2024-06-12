package de.happybavarian07.adminpanel.language.mysql.controller;/*
 * @Author HappyBavarian07
 * @Date 08.05.2024 | 16:44
 */

import de.happybavarian07.adminpanel.language.mysql.Language;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefix;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ContentManager {
    private final AdminPanelMain plugin;
    private final InnerLanguageManager innerLanguageManager;
    private final ConnectionManager connectionManager;
    // Map of all language content (only used if offlineContentMap is true) (gets filled on startup) (the uuid is the language id) (the database table name is the language shortname + _content)
    private final Map<UUID, Map<String, Object>> languageContentMap = new HashMap<>();
    private final Map<UUID, Map<String, Object>> downloadedContentMap = new HashMap<>();

    private final ExecutorService executorService;

    public ContentManager(AdminPanelMain plugin, InnerLanguageManager innerLanguageManager, ExecutorService executorService, ConnectionManager connectionManager) {
        this.plugin = plugin;
        this.innerLanguageManager = innerLanguageManager;
        this.executorService = executorService;
        this.connectionManager = connectionManager;
    }

    public Map<UUID, Map<String, Object>> getLanguageContentMap() {
        try {
            String ids = languageContentMap.keySet().stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining("','", "'", "'"));
            ResultSet resultSet = connectionManager.prepareStatement("SELECT ID, LanguageShort FROM {table_prefix}LanguageList WHERE ID NOT IN (" + ids + ")").executeQuery();
            while (resultSet.next()) {
                UUID languageID = UUID.fromString(resultSet.getString("ID"));
                String languageShort = resultSet.getString("LanguageShort");
                ResultSet contentResultSet = connectionManager.prepareStatement("SELECT LanguageKey, LanguageValue FROM {table_prefix}" + languageShort + "_content").executeQuery();
                Map<String, Object> contentMap = new HashMap<>();
                while (contentResultSet.next()) {
                    String languageKey = contentResultSet.getString("LanguageKey");
                    byte[] languageValue = contentResultSet.getBytes("LanguageValue");
                    contentMap.put(languageKey, SerializationManager.deserialize(languageValue, Object.class, false));
                }
                languageContentMap.putIfAbsent(languageID, contentMap);
            }
        } catch (SQLException | IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return languageContentMap;
    }

    public boolean languageContentExists(UUID languageID, String languageKey) {
        if (!languageContentMap.containsKey(languageID) || !languageContentMap.get(languageID).containsKey(languageKey)) {
            // Query data from the database
            try {
                ResultSet resultSet = connectionManager.prepareStatement("SELECT * FROM {table_prefix}" + innerLanguageManager.getLanguageShortname(languageID) + "_content" +
                        "WHERE LanguageKey = '" + "Messages." + languageKey + "'" +
                        "OR LanguageKey = '" + "Items." + languageKey + "'" +
                        "OR LanguageKey = '" + "MenuTitles." + languageKey + "'" +
                        "OR LanguageKey = '" + languageKey + "'").executeQuery();
                return resultSet.next() && resultSet.getObject("LanguageValue") != null;
            } catch (SQLException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to check if the language content exists in the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                plugin.getLogger().log(Level.SEVERE, "Failed to check if the language content exists in the database: " + e);
            }
            return false;
        } else {
            return true;
        }
    }

    public Map<String, Object> getItemMapFromDatabase(UUID languageID, String pathToItem) {
        Map<String, Object> contentMap = new HashMap<>();

        // Check if the path exists in the downloadedContentMap
        if (downloadedContentMap.containsKey(languageID) && downloadedContentMap.get(languageID).containsKey("Items." + pathToItem)) {
            // If it does, get the content from the map
            contentMap = (Map<String, Object>) downloadedContentMap.get(languageID).get("Items." + pathToItem);
        } else {
            // If it doesn't, load it from the database
            try {
                ResultSet resultSet = connectionManager.prepareStatement(
                        "SELECT LanguageKey, LanguageValue FROM {table_prefix}" + innerLanguageManager.getLanguageShortname(languageID) + "_content WHERE LanguageKey " +
                                "= 'Items." + pathToItem + "'").executeQuery();
                while (resultSet.next()) {
                    byte[] languageValue = resultSet.getBytes("LanguageValue");
                    Map<String, Object> section = SerializationManager.deserialize(languageValue, Map.class, false);
                    assert section != null;
                    for (String key : section.keySet()) {
                        contentMap.put("Items." + pathToItem + "." + key, section.get(key));
                    }
                }
                // Add the loaded content to the downloadedContentMap
                if (!downloadedContentMap.containsKey(languageID)) {
                    downloadedContentMap.put(languageID, new HashMap<>());
                }
                downloadedContentMap.get(languageID).put("Items." + pathToItem, contentMap);
            } catch (SQLException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to get the item map from the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                plugin.getLogger().log(Level.SEVERE, "Failed to get the item map from the database: " + e);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return contentMap;
    }

    /**
     * Gets the content of a language from the database
     *
     * @param languageID The ID of the language to get the content from
     * @return Returns a map of the language content already converted to the language file format
     */
    public Map<String, Object> getLanguageContent(UUID languageID) {
        Map<String, Object> contentMap = new HashMap<>();

        // Check if the languageID exists in the downloadedContentMap
        if (downloadedContentMap.containsKey(languageID)) {
            // If it does, get the content from the map
            contentMap = downloadedContentMap.get(languageID);
        } else {
            // If it doesn't, load it from the database
            try {
                ResultSet resultSet = connectionManager.prepareStatement("SELECT LanguageKey, LanguageValue FROM {table_prefix}" + innerLanguageManager.getLanguageShortname(languageID) + "_content").executeQuery();
                while (resultSet.next()) {
                    String languageKey = resultSet.getString("LanguageKey");
                    byte[] languageValue = resultSet.getBytes("LanguageValue");
                    contentMap.put(languageKey, SerializationManager.deserialize(languageValue, Object.class, false));
                }
                // Add the loaded content to the downloadedContentMap
                downloadedContentMap.put(languageID, contentMap);
            } catch (SQLException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to get the language content from the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                plugin.getLogger().log(Level.SEVERE, "Failed to get the language content from the database: " + e);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return contentMap;
    }

    public boolean createLanguageContentTable(UUID languageID) {
        // Log
        //System.out.println("Creating language content table for language with ID " + languageID.toString());
        Language language = innerLanguageManager.getLanguage(languageID);
        /*System.out.println("Language: " + language.getLanguageName());
        System.out.println("Language Short: " + language.getLanguageShort());
        System.out.println("Language File: " + language.getLanguageFilePath());
        System.out.println("Language Version: " + language.getLanguageVersion());
        System.out.println("Language Description: " + language.getLanguageDescription());
        System.out.println("Language ID: " + languageID);
        System.out.println("Language Shortname: " + getLanguageShortname(languageID));*/

        try {
            connectionManager.prepareStatement("CREATE TABLE IF NOT EXISTS {table_prefix}" + innerLanguageManager.getLanguageShortname(languageID) + "_content " +
                    "(LanguageID VARCHAR(255), LanguageKey VARCHAR(255) PRIMARY KEY NOT NULL , LanguageValue BLOB)").executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to create the language content table for language with ID " + languageID.toString() + ": " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
            plugin.getLogger().log(Level.SEVERE, "Failed to create the language content table for language with ID " + languageID + ": " + e);
            return false;
        }
    }

    public void removeLanguageContentCache(UUID languageID) {
        downloadedContentMap.remove(languageID);
    }

    public boolean addLanguageContent(UUID languageID, String languageKey, Object languageValue) {
        try {
            String sql = "INSERT {ignore} INTO {table_prefix}" + innerLanguageManager.getLanguageShortname(languageID) + "_content (LanguageID, LanguageKey, LanguageValue) VALUES (?, ?, ?)";
            PreparedStatement pstmt = connectionManager.prepareStatement(sql);
            pstmt.setString(1, languageID.toString());
            pstmt.setString(2, languageKey);
            pstmt.setBytes(3, SerializationManager.serialize(languageValue));
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to add language content for language with ID " + languageID.toString() + ": " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
            plugin.getLogger().log(Level.SEVERE, "Failed to add language content for language with ID " + languageID + ": " + e);
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Boolean> addLanguageContentAsync(UUID languageID, Map<String, Object> contentMap) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        executorService.submit(() -> {
            ReentrantLock lock = innerLanguageManager.getLock(languageID);
            lock.lock();
            try {
                boolean result = addLanguageContent(languageID, contentMap);
                future.complete(result);
            } finally {
                lock.unlock();
            }
        });

        return future;
    }

    public boolean addLanguageContent(UUID languageID, Map<String, Object> contentMap) {
        String languageShort = innerLanguageManager.getLanguageShortname(languageID);
        try {
            // Get the start time
            long startTime = System.currentTimeMillis();

            int totalEntries = contentMap.size();
            int processedEntries = 0;
            int entriesPerBatch = 30; // Adjust this number based on your system's performance

            String sql = "INSERT {ignore} INTO {table_prefix}" + languageShort + "_content (LanguageID, LanguageKey, LanguageValue) VALUES (?, ?, ?)";
            PreparedStatement pstmt = connectionManager.prepareStatement(sql);

            for (Map.Entry<String, Object> entry : contentMap.entrySet()) {
                if (entry.getValue() == null) {
                    continue; // Skip if the value is null
                }
                pstmt.setString(1, languageID.toString());
                pstmt.setString(2, entry.getKey());
                pstmt.setBytes(3, SerializationManager.serialize(entry.getValue()));
                pstmt.addBatch(); // Add to batch
                pstmt.clearParameters(); // Clear parameters

                processedEntries++;

                // Execute batch every entriesPerBatch entries or when all entries have been processed
                if (processedEntries % entriesPerBatch == 0 || processedEntries >= totalEntries) {
                    pstmt.executeBatch();// Execute batch
                    pstmt.clearBatch(); // Clear batch

                    if (processedEntries % (entriesPerBatch * 10) == 0 || processedEntries >= totalEntries) {
                        // Log the status message every 10th batch or when all entries have been processed
                        double percentage = (double) processedEntries / totalEntries * 100;
                        plugin.getLogger().log(Level.INFO, "Processed " + processedEntries + "/" + totalEntries + " Language Content entries for Language '" + innerLanguageManager.getLanguageName(languageID) + "' (" + String.format("%.2f", percentage) + "%)");
                    }
                }
            }

            // Calculate the total execution time
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            // Log the execution time to the plugin's file logger and the normal logger
            plugin.getFileLogger().writeToLog(Level.INFO, "Execution time for adding language content (" + innerLanguageManager.getLanguageShortname(languageID) + "): " + executionTime + " ms.", LogPrefix.ACTIONSLOGGER_PLUGIN);
            plugin.getLogger().log(Level.INFO, "Execution time for adding language content: " + executionTime + " ms.");

            return true;
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to add language content for language with ID " + languageID.toString() + ": " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
            plugin.getLogger().log(Level.SEVERE, "Failed to add language content for language with ID " + languageID + ": " + e);
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeLanguageContent(UUID languageID, String languageKey) {
        String languageShort = innerLanguageManager.getLanguageShortname(languageID);
        try {
            downloadedContentMap.get(languageID).remove(languageKey);

            String key = languageKey;
            String query = "DELETE FROM {table_prefix}" + languageShort + "_content " +
                    "WHERE LanguageKey = '" + "Messages." + languageKey + "' " +
                    "OR LanguageKey = '" + "Items." + languageKey + "' " +
                    "OR LanguageKey = '" + "MenuTitles." + languageKey + "' " +
                    "OR LanguageKey = '" + key + "'";
            connectionManager.prepareStatement(query).executeUpdate();
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to remove the language content for language with ID " + languageID.toString() + ": " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
            plugin.getLogger().log(Level.SEVERE, "Failed to remove the language content for language with ID " + languageID + ": " + e);
        }
    }

    public void updateLanguageContent(UUID languageID, String languageKey, Object languageValue) {
        try {
            String query = "UPDATE {table_prefix}" + innerLanguageManager.getConverterManager().getLanguageConverter(languageID).getLanguageContentTableName() + " SET LanguageValue = ? " +
                    "WHERE LanguageKey = '" + "Messages." + languageKey + "' " +
                    "OR LanguageKey = '" + "Items." + languageKey + "' " +
                    "OR LanguageKey = '" + "MenuTitles." + languageKey + "' " +
                    "OR LanguageKey = '" + languageKey + "'";
            PreparedStatement prepStatement = connectionManager.prepareStatement(query);
            prepStatement.setBytes(1, SerializationManager.serialize(languageValue));
            prepStatement.executeUpdate();
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to update the language content for language with ID " + languageID.toString() + ": " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
            plugin.getLogger().log(Level.SEVERE, "Failed to update the language content for language with ID " + languageID + ": " + e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateLanguageContent(UUID languageID, Map<String, Object> contentMap, boolean softUpdate /* Soft means that it wont replace existing keys */) {
        String languageShort = innerLanguageManager.getLanguageShortname(languageID);
        try {
            int totalEntries = contentMap.size();
            int processedEntries = 0;
            int entriesPerBatch = 30; // Adjust this number based on your system's performance

            String sqlUpdate = "UPDATE {table_prefix}" + languageShort + "_content SET LanguageValue = ? WHERE LanguageKey = ?";
            String sqlInsert = "INSERT INTO {table_prefix}" + languageShort + "_content (LanguageID, LanguageKey, LanguageValue) VALUES (?, ?, ?)";
            PreparedStatement pstmtUpdate = connectionManager.prepareStatement(sqlUpdate);
            PreparedStatement pstmtInsert = connectionManager.prepareStatement(sqlInsert);

            pstmtInsert.setString(1, languageID.toString());

            for (Map.Entry<String, Object> entry : contentMap.entrySet()) {
                if (entry.getValue() == null) {
                    continue; // Skip if the value is null
                }

                // Check if the key exists
                ResultSet rs = connectionManager.prepareStatement("SELECT 1 FROM {table_prefix}" + languageShort + "_content WHERE LanguageKey = '" + entry.getKey() + "'").executeQuery();
                if (rs.next()) {
                    if (!softUpdate) {
                        // Key exists, update it
                        pstmtUpdate.setBytes(1, SerializationManager.serialize(entry.getValue()));
                        pstmtUpdate.setString(2, entry.getKey());
                        pstmtUpdate.addBatch(); // Add to batch
                    }
                } else {
                    // Key doesn't exist, insert it
                    pstmtInsert.setString(1, entry.getKey());
                    pstmtInsert.setBytes(2, SerializationManager.serialize(entry.getValue()));
                    pstmtInsert.addBatch(); // Add to batch
                }

                processedEntries++;

                // Execute batch every entriesPerBatch entries or when all entries have been processed
                if (processedEntries % entriesPerBatch == 0 || processedEntries >= totalEntries) {
                    pstmtUpdate.executeBatch();// Execute batch
                    pstmtInsert.executeBatch();// Execute batch
                    pstmtUpdate.clearBatch(); // Clear batch
                    pstmtInsert.clearBatch(); // Clear batch

                    // Log the status message every 10th batch or when all entries have been processed for both update and insert
                    if (processedEntries % (entriesPerBatch * 10) == 0 || processedEntries >= totalEntries) {
                        double percentage = (double) processedEntries / totalEntries * 100;
                        plugin.getLogger().log(Level.INFO, "Processed " + processedEntries + "/" + totalEntries + " Language Content entries for Language '" + innerLanguageManager.getLanguageName(languageID) + "' (" + String.format("%.2f", percentage) + "%)");
                    }
                }
            }

            return true;
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to update the language content for language with ID " + languageID.toString() + ": " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
            plugin.getLogger().log(Level.SEVERE, "Failed to update the language content for language with ID " + languageID + ": " + e);
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Boolean> updateLanguageContentAsync(UUID languageID, Map<String, Object> contentMap, boolean softUpdate /* Soft means that it wont replace existing keys */) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        executorService.submit(() -> {
            ReentrantLock lock = innerLanguageManager.getLock(languageID);
            lock.lock();
            try {
                boolean result = updateLanguageContent(languageID, contentMap, softUpdate);
                future.complete(result);
            } finally {
                lock.unlock();
            }
        });

        return future;
    }

    public String getContentValue(UUID languageID, String languageKey) {
        String key = languageKey;
        String query = "SELECT LanguageValue FROM {table_prefix}" + innerLanguageManager.getLanguageShortname(languageID) + "_content " +
                "WHERE LanguageKey = '" + "Messages." + languageKey + "' " +
                "OR LanguageKey = '" + "Items." + languageKey + "' " +
                "OR LanguageKey = '" + "MenuTitles." + languageKey + "' " +
                "OR LanguageKey = '" + key + "'";
        if (downloadedContentMap.containsKey(languageID) && downloadedContentMap.get(languageID).containsKey(languageKey)) {
            // If it does, get the content from the map
            return (String) downloadedContentMap.get(languageID).get(languageKey);
        } else {
            // If it doesn't, load it from the database
            try {
                ResultSet resultSet = connectionManager.prepareStatement(query).executeQuery();
                if (resultSet.next()) {
                    String result = SerializationManager.deserialize(resultSet.getBytes("LanguageValue"), String.class, false);
                    // Add the loaded content to the downloadedContentMap
                    downloadedContentMap.get(languageID).put(languageKey, result);
                    return result;
                }
            } catch (SQLException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to get the content value from the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                plugin.getLogger().log(Level.SEVERE, "Failed to get the content value from the database: " + e);
                return "-1";
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public <T> Object getCustomContentValue(UUID languageID, String languageKey, T defaultValue) throws IOException, ClassNotFoundException {
        if (downloadedContentMap.containsKey(languageID) && downloadedContentMap.get(languageID).containsKey(languageKey)) {
            // If it does, get the content from the map
            Object value = downloadedContentMap.get(languageID).get(languageKey);
            if (defaultValue instanceof Integer)
                return Integer.parseInt((String) value);
            if (defaultValue instanceof Boolean)
                return Boolean.parseBoolean((String) value);
            if (defaultValue instanceof Double)
                return Double.parseDouble((String) value);
            if (defaultValue instanceof String)
                return value;
            return SerializationManager.deserialize((String) value, defaultValue.getClass(), false);
        } else {
            // If it doesn't, load it from the database
            try {
                String query = "SELECT LanguageValue FROM {table_prefix}" + innerLanguageManager.getLanguageShortname(languageID) + "_content " +
                        "WHERE LanguageKey = '" + "Messages." + languageKey + "' " +
                        "OR LanguageKey = '" + "Items." + languageKey + "' " +
                        "OR LanguageKey = '" + "MenuTitles." + languageKey + "' " +
                        "OR LanguageKey = '" + languageKey + "'";
                ResultSet resultSet = connectionManager.prepareStatement(query).executeQuery();
                if (resultSet.next()) {
                    Object result = SerializationManager.deserialize(resultSet.getBytes("LanguageValue"), defaultValue.getClass(), false);
                    // Add the loaded content to the downloadedContentMap
                    downloadedContentMap.get(languageID).put(languageKey, result);
                    return result;
                }
            } catch (SQLException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to get the custom content value from the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                plugin.getLogger().log(Level.SEVERE, "Failed to get the custom content value from the database: " + e);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public void addDownloadedContent(UUID languageID, Map<String, Object> contentMap) {
        downloadedContentMap.put(languageID, contentMap);
    }

    public void addDownloadedContent(UUID languageID, String languageKey, Object languageValue) {
        if (!downloadedContentMap.containsKey(languageID)) {
            downloadedContentMap.put(languageID, new HashMap<>());
        }
        downloadedContentMap.get(languageID).put(languageKey, languageValue);
    }

    public void clearDownloadedContent() {
        downloadedContentMap.clear();
    }

    public void clearDownloadedContent(UUID languageID) {
        downloadedContentMap.remove(languageID);
    }

    public void removeLanguageContent(UUID languageID) {
        languageContentMap.remove(languageID);
        clearDownloadedContent(languageID);
    }
}
