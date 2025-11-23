package de.happybavarian07.adminpanel.service.impl;

import de.happybavarian07.adminpanel.service.api.DataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class DataServiceMigrationUtil {
    private static final int DEFAULT_BATCH_SIZE = 200;

    public static <T> CompletableFuture<Void> migrate(DataService source, DataService target, Class<T> clazz) {
        return migrate(source, target, clazz, DEFAULT_BATCH_SIZE);
    }

    public static <T> CompletableFuture<Void> migrate(DataService source, DataService target, Class<T> clazz, int batchSize) {
        return source.listKeys().thenCompose(keys -> {
            if (keys == null || keys.isEmpty()) return CompletableFuture.completedFuture(null);
            List<String> allKeys = new ArrayList<>(keys);
            AtomicInteger index = new AtomicInteger(0);
            CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
            while (index.get() < allKeys.size()) {
                int start = index.getAndAdd(batchSize);
                int end = Math.min(allKeys.size(), start + batchSize);
                List<String> batchKeys = allKeys.subList(start, end);
                chain = chain.thenCompose(ignored -> {
                    List<CompletableFuture<Void>> loads = new ArrayList<>();
                    Map<String, T> batchMap = new HashMap<>();
                    for (String k : batchKeys) {
                        CompletableFuture<Void> f = source.load(k, clazz).thenAccept(v -> {
                            if (v != null) batchMap.put(k, v);
                        }).exceptionally(ex -> {
                            return null;
                        });
                        loads.add(f);
                    }
                    return CompletableFuture.allOf(loads.toArray(new CompletableFuture[0]))
                            .thenCompose(x -> {
                                if (batchMap.isEmpty()) return CompletableFuture.completedFuture(null);
                                return target.saveAll(batchMap);
                            });
                });
            }
            return chain;
        });
    }
}
