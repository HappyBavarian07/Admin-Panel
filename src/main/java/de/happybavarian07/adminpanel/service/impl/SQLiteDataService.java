package de.happybavarian07.adminpanel.service.impl;

import de.happybavarian07.adminpanel.service.api.DataService;
import de.happybavarian07.adminpanel.service.api.DataServiceType;
import de.happybavarian07.adminpanel.utils.Serialization;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SQLiteDataService implements DataService {
    private final UUID id = UUID.randomUUID();
    private final String name = "SQLiteDataService";
    private Connection connection;
    private final String dbPath;

    public SQLiteDataService(String dbPath) {
        this.dbPath = dbPath;
    }

    public UUID id() {
        return id;
    }

    public String serviceName() {
        return name;
    }

    public CompletableFuture<Void> init() {
        return CompletableFuture.runAsync(() -> {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS data (id TEXT PRIMARY KEY, value TEXT)");
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to initialize SQLite data service", e);
            }
        });
    }

    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to shutdown SQLite data service", e);
            }
        });
    }

    public CompletableFuture<Void> onReload() {
        return CompletableFuture.runAsync(() -> {
            // No-op for SQLite
        });
    }

    public CompletableFuture<Void> migrateTo(DataService target) {
        return DataServiceMigrationUtil.migrate(this, target, Object.class);
    }

    public boolean isHealthy() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public DataServiceType getType() {
        return DataServiceType.SQLITE;
    }

    public <T> CompletableFuture<Void> save(String key, T value) {
        String serialized = Serialization.serialize(value);
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement pstmt = connection.prepareStatement("INSERT OR REPLACE INTO data (id, value) VALUES (?, ?)")) {
                pstmt.setString(1, key);
                pstmt.setString(2, serialized);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save data object", e);
            }
        });
    }

    public <T> CompletableFuture<T> load(String key, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement pstmt = connection.prepareStatement("SELECT value FROM data WHERE id = ?")) {
                pstmt.setString(1, key);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String json = rs.getString("value");
                        Object des = Serialization.deserialize(json, clazz);
                        return (T) des;
                    }
                    return null;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load data object", e);
            }
        });
    }

    public <T> CompletableFuture<Void> update(String key, T value) {
        return save(key, value);
    }

    public CompletableFuture<Void> delete(String key) {
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM data WHERE id = ?")) {
                pstmt.setString(1, key);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete data object", e);
            }
        });
    }

    public CompletableFuture<Set<String>> listKeys() {
        return CompletableFuture.supplyAsync(() -> {
            Set<String> keys = new HashSet<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id FROM data")) {
                while (rs.next()) {
                    keys.add(rs.getString("id"));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to list keys", e);
            }
            return keys;
        });
    }

    public CompletableFuture<Boolean> exists(String key) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement pstmt = connection.prepareStatement("SELECT 1 FROM data WHERE id = ?")) {
                pstmt.setString(1, key);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to check existence", e);
            }
        });
    }

    public <T> CompletableFuture<Void> saveAll(Map<String, T> entries) {
        return CompletableFuture.runAsync(() -> {
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement pstmt = connection.prepareStatement("INSERT OR REPLACE INTO data (id, value) VALUES (?, ?)")) {
                    for (Map.Entry<String, T> entry : entries.entrySet()) {
                        pstmt.setString(1, entry.getKey());
                        pstmt.setString(2, Serialization.serialize(entry.getValue()));
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
                connection.commit();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to batch save data", e);
            }
        });
    }

    public <T> CompletableFuture<Map<String, T>> loadAll(Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, T> all = new HashMap<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, value FROM data")) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String json = rs.getString("value");
                    Object des = Serialization.deserialize(json, clazz);
                    all.put(id, (T) des);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load all data", e);
            }
            return all;
        });
    }

    public CompletableFuture<Void> clear() {
        return CompletableFuture.runAsync(() -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("DELETE FROM data");
            } catch (SQLException e) {
                throw new RuntimeException("Failed to clear all data", e);
            }
        });
    }

    public CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
        });
    }

    public CompletableFuture<Set<String>> listKeys(String prefix) {
        return CompletableFuture.supplyAsync(() -> {
            Set<String> keys = new java.util.HashSet<>();
            try (PreparedStatement pstmt = connection.prepareStatement("SELECT id FROM data WHERE id LIKE ?")) {
                pstmt.setString(1, prefix + "%");
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        keys.add(rs.getString("id"));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to list keys with prefix", e);
            }
            return keys;
        });
    }

    public CompletableFuture<Void> deleteAll(java.util.List<String> keys) {
        return CompletableFuture.runAsync(() -> {
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM data WHERE id = ?")) {
                    for (String key : keys) {
                        pstmt.setString(1, key);
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
                connection.commit();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to batch delete data", e);
            }
        });
    }

    public CompletableFuture<Void> backup(String backupName) {
        return CompletableFuture.runAsync(() -> {
            try {
                java.nio.file.Path dbPath = java.nio.file.Path.of(this.dbPath);
                if (!java.nio.file.Files.exists(dbPath)) return;

                String timestamp = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                java.nio.file.Path backupPath = dbPath.getParent().resolve("backups");
                java.nio.file.Files.createDirectories(backupPath);

                java.nio.file.Path backup = backupPath.resolve(backupName + "_" + timestamp + ".db");
                java.nio.file.Files.copy(dbPath, backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (java.io.IOException e) {
                throw new RuntimeException("Failed to create backup", e);
            }
        });
    }

    public String getHealthStatus() {
        try {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                if (rs.next()) {
                    return "HEALTHY";
                }
            }
            return "UNHEALTHY: Query test failed";
        } catch (SQLException e) {
            return "UNHEALTHY: " + e.getMessage();
        }
    }
}
