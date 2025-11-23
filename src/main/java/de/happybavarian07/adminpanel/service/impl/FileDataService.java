package de.happybavarian07.adminpanel.service.impl;

import de.happybavarian07.adminpanel.service.api.DataService;
import de.happybavarian07.adminpanel.service.api.DataServiceType;
import de.happybavarian07.adminpanel.utils.Serialization;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FileDataService implements DataService {
    private final UUID id = UUID.randomUUID();
    private final String name = "FileDataService";
    private final String storagePath;
    private final Yaml yaml;
    private Map<String, String> dataMap;

    public FileDataService(String storagePath) {
        this.storagePath = storagePath;
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(options);
        dataMap = new HashMap<>();
    }

    public UUID id() {
        return id;
    }

    public String serviceName() {
        return name;
    }

    public CompletableFuture<Void> init() {
        return CompletableFuture.runAsync(this::reloadFromDisk);
    }

    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(this::saveToDisk);
    }

    public CompletableFuture<Void> onReload() {
        return CompletableFuture.runAsync(this::reloadFromDisk);
    }

    public CompletableFuture<Void> migrateTo(DataService target) {
        return DataServiceMigrationUtil.migrate(this, target, String.class);
    }

    public boolean isHealthy() {
        return getHealthStatus().equals("HEALTHY");
    }

    public DataServiceType getType() {
        return DataServiceType.FILE_SYSTEM;
    }

    public <T> CompletableFuture<Void> save(String key, T value) {
        return CompletableFuture.runAsync(() -> {
            synchronized (this) {
                String serialized = Serialization.serialize(value);
                dataMap.put(key, serialized);
                saveToDisk();
            }
        });
    }

    public <T> CompletableFuture<T> load(String key, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (this) {
                String json = dataMap.get(key);
                if (json == null) return null;
                Object des = Serialization.deserialize(json, clazz);
                return (T) des;
            }
        });
    }

    public <T> CompletableFuture<Void> update(String key, T value) {
        return save(key, value);
    }

    public CompletableFuture<Void> delete(String key) {
        return CompletableFuture.runAsync(() -> {
            synchronized (this) {
                dataMap.remove(key);
                saveToDisk();
            }
        });
    }

    public CompletableFuture<Set<String>> listKeys() {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (this) {
                return dataMap.keySet();
            }
        });
    }

    public CompletableFuture<Boolean> exists(String key) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (this) {
                return dataMap.containsKey(key);
            }
        });
    }

    public <T> CompletableFuture<Void> saveAll(Map<String, T> entries) {
        return CompletableFuture.runAsync(() -> {
            synchronized (this) {
                for (Map.Entry<String, T> e : entries.entrySet()) {
                    dataMap.put(e.getKey(), Serialization.serialize(e.getValue()));
                }
                saveToDisk();
            }
        });
    }

    public <T> CompletableFuture<Map<String, T>> loadAll(Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (this) {
                Map<String, T> result = new HashMap<>();
                for (Map.Entry<String, String> e : dataMap.entrySet()) {
                    Object des = Serialization.deserialize(e.getValue(), clazz);
                    result.put(e.getKey(), (T) des);
                }
                return result;
            }
        });
    }

    public CompletableFuture<Void> clear() {
        return CompletableFuture.runAsync(() -> {
            synchronized (this) {
                dataMap.clear();
                saveToDisk();
            }
        });
    }

    public CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(this::reloadFromDisk);
    }

    public CompletableFuture<Set<String>> listKeys(String prefix) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (this) {
                return dataMap.keySet().stream()
                        .filter(k -> k.startsWith(prefix))
                        .collect(java.util.stream.Collectors.toSet());
            }
        });
    }

    public CompletableFuture<Void> deleteAll(java.util.List<String> keys) {
        return CompletableFuture.runAsync(() -> {
            synchronized (this) {
                for (String key : keys) {
                    dataMap.remove(key);
                }
                saveToDisk();
            }
        });
    }

    public CompletableFuture<Void> backup(String backupName) {
        return CompletableFuture.runAsync(() -> {
            try {
                Path source = Path.of(storagePath);
                if (!Files.exists(source)) return;

                String timestamp = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                Path backupPath = source.getParent().resolve("backups");
                Files.createDirectories(backupPath);

                Path backup = backupPath.resolve(backupName + "_" + timestamp + ".yml");
                Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create backup", e);
            }
        });
    }

    public String getHealthStatus() {
        try {
            File file = new File(storagePath);
            if (!file.getParentFile().exists()) {
                return "UNHEALTHY: Parent directory does not exist";
            }
            if (!file.getParentFile().canWrite()) {
                return "UNHEALTHY: Cannot write to storage directory";
            }
            if (file.exists() && !file.canRead()) {
                return "UNHEALTHY: Cannot read storage file";
            }
            return "HEALTHY";
        } catch (Exception e) {
            return "UNHEALTHY: " + e.getMessage();
        }
    }

    private void saveToDisk() {
        try {
            Path target = Path.of(storagePath);
            Path tmp = Path.of(storagePath + ".tmp");
            try (FileWriter writer = new FileWriter(tmp.toFile())) {
                yaml.dump(dataMap, writer);
            }
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save data to disk", e);
        }
    }

    private void reloadFromDisk() {
        File file = new File(storagePath);
        if (!file.exists()) {
            dataMap = new HashMap<>();
            return;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            Object loaded = yaml.load(fis);
            if (loaded instanceof Map<?, ?> raw) {
                Map<String, String> converted = new HashMap<>();
                for (Map.Entry<?, ?> entry : raw.entrySet()) {
                    converted.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                }
                dataMap = converted;
            } else {
                dataMap = new HashMap<>();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load data from disk", e);
        }
    }
}
