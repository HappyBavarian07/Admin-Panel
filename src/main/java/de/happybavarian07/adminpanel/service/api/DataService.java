package de.happybavarian07.adminpanel.service.api;

import de.happybavarian07.coolstufflib.service.api.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DataService extends Service {
    UUID id();

    String serviceName();

    CompletableFuture<Void> init();

    CompletableFuture<Void> shutdown();

    CompletableFuture<Void> onReload();

    CompletableFuture<Void> migrateTo(DataService target);

    boolean isHealthy();

    DataServiceType getType();

    <T> CompletableFuture<Void> save(String key, T value);

    <T> CompletableFuture<T> load(String key, Class<T> clazz);

    <T> CompletableFuture<Void> update(String key, T value);

    CompletableFuture<Void> delete(String key);

    CompletableFuture<Set<String>> listKeys();

    CompletableFuture<Boolean> exists(String key);

    <T> CompletableFuture<Void> saveAll(Map<String, T> entries);

    <T> CompletableFuture<Map<String, T>> loadAll(Class<T> clazz);

    CompletableFuture<Void> clear();

    CompletableFuture<Void> reload();

    CompletableFuture<Set<String>> listKeys(String prefix);

    CompletableFuture<Void> deleteAll(java.util.List<String> keys);

    CompletableFuture<Void> backup(String backupName);

    String getHealthStatus();

    default <V> CompletableFuture<Map<String, V>> loadMap(String key, Class<V> valueClass) {
        return load(key, Map.class).thenApply(m -> {
            if (m == null) return null;
            Map<?, ?> raw = (Map<?, ?>) m;
            Map<String, V> result = new HashMap<>();
            for (Map.Entry<?, ?> e : raw.entrySet()) {
                Object k = e.getKey();
                Object v = e.getValue();
                if (k == null) continue;
                String ks = String.valueOf(k);
                V vv = null;
                if (v != null) {
                    try {
                        vv = valueClass.cast(v);
                    } catch (ClassCastException ex) {
                    }
                }
                result.put(ks, vv);
            }
            return result;
        });
    }
}
