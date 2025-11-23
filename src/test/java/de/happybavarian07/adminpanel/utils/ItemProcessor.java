package de.happybavarian07.adminpanel.utils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ItemProcessor {

    public static class ProcessingStats {
        private final List<Double> individualTimes = new ArrayList<>();

        public void update(long elapsedTime) {
            double elapsedTimeInMs = (double) elapsedTime / 1_000_000; // Convert nanoseconds to milliseconds
            individualTimes.add(elapsedTimeInMs);
        }

        public double getMinTime() {
            return individualTimes.isEmpty() ? 0 : round(Collections.min(individualTimes), 5);
        }

        public double getMaxTime() {
            return individualTimes.isEmpty() ? 0 : round(Collections.max(individualTimes), 5);
        }

        public double getAverageTime() {
            double sum = individualTimes.stream().mapToDouble(Double::doubleValue).sum();
            return individualTimes.isEmpty() ? 0 : round((sum / individualTimes.size()), 5);
        }

        private double round(double value, int decimalPlaces) {
            double scale = Math.pow(10, decimalPlaces);
            return Math.round(value * scale) / scale;
        }
    }

    public static <R> List<R> processItemsSameType(List<R> items, int numThreads, Function<R, R> itemProcessor, boolean debug) {
        ProcessingStats stats = new ProcessingStats();
        return processItemsInternalR(items, numThreads, itemProcessor, stats, debug);
    }

    public static <T, R> List<R> processItemsDifferentType(List<T> items, int numThreads, Function<T, R> itemProcessor, boolean debug) {
        ProcessingStats stats = new ProcessingStats();
        return processItemsInternalTR(items, numThreads, itemProcessor, stats, debug);
    }

    public static <T, R> Map<T, R> processMapItems(Map<T, R> items, int numThreads, Function<R, R> itemProcessor, boolean debug) {
        ProcessingStats stats = new ProcessingStats();
        return processMapItemsInternal(items, numThreads, itemProcessor, stats, debug);
    }

    public static <T, R> Map<T, List<R>> processMapListItems(Map<T, List<R>> items, int numThreads, Function<R, R> itemProcessor, boolean debug) {
        ProcessingStats stats = new ProcessingStats();
        return processMapListItemsInternal(items, numThreads, itemProcessor, stats, debug);
    }

    private static <R> List<R> processItemsInternalR(List<R> items, int numThreads, Function<R, R> itemProcessor, ProcessingStats stats, boolean debug) {
        int itemsPerThread = items.size() / numThreads;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        List<R> processedItems = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            int start = i * itemsPerThread;
            int end = (i == numThreads - 1) ? items.size() : (i + 1) * itemsPerThread;
            List<R> sublist = items.subList(start, end);

            Runnable task = () -> {
                long startTime = System.nanoTime();

                for (R item : sublist) {
                    R processedItem = itemProcessor.apply(item);
                    synchronized (processedItems) {
                        processedItems.add(processedItem);
                    }
                }

                long endTime = System.nanoTime();
                long elapsedTime = endTime - startTime;

                if (debug) {
                    System.out.println("Thread " + Thread.currentThread().getId() + " took " + ((double) elapsedTime / 1_000_000) + " ms");
                }

                stats.update(elapsedTime);
            };

            executor.submit(task);
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // Handle the exception if necessary
        }

        if (debug) {
            System.out.println("Processing Stats - Min: " + stats.getMinTime() + " ms, Max: " + stats.getMaxTime() + " ms, Avg: " + stats.getAverageTime() + " ms");
        }

        return processedItems;
    }

    private static <T, R> List<R> processItemsInternalTR(List<T> items, int numThreads, Function<T, R> itemProcessor, ProcessingStats stats, boolean debug) {
        int itemsPerThread = items.size() / numThreads;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        List<R> processedItems = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            int start = i * itemsPerThread;
            int end = (i == numThreads - 1) ? items.size() : (i + 1) * itemsPerThread;
            List<T> sublist = items.subList(start, end);

            Runnable task = () -> {
                long startTime = System.nanoTime();

                for (T item : sublist) {
                    R processedItem = itemProcessor.apply(item);
                    synchronized (processedItems) {
                        processedItems.add(processedItem);
                    }
                }

                long endTime = System.nanoTime();
                long elapsedTime = endTime - startTime;

                if (debug) {
                    System.out.println("Thread " + Thread.currentThread().getId() + " took " + ((double) elapsedTime / 1_000_000) + " ms");
                }

                stats.update(elapsedTime);
            };

            executor.submit(task);
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // Handle the exception if necessary
        }

        if (debug) {
            System.out.println("Processing Stats - Min: " + stats.getMinTime() + " ms, Max: " + stats.getMaxTime() + " ms, Avg: " + stats.getAverageTime() + " ms");
        }

        return processedItems;
    }

    private static <T, R> Map<T, R> processMapItemsInternal(Map<T, R> items, int numThreads, Function<R, R> itemProcessor, ProcessingStats stats, boolean debug) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        Map<T, R> processedItems = new HashMap<>();

        for (T key : items.keySet()) {
            R value = items.get(key);

            Runnable task = () -> {
                long startTime = System.nanoTime();

                R processedItem = itemProcessor.apply(value);
                synchronized (processedItems) {
                    processedItems.put(key, processedItem);
                }

                long endTime = System.nanoTime();
                long elapsedTime = endTime - startTime;

                if (debug) {
                    System.out.println("Thread " + Thread.currentThread().getId() + " took " + ((double) elapsedTime / 1_000_000) + " ms");
                }

                stats.update(elapsedTime);
            };

            executor.submit(task);
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // Handle the exception if necessary
        }

        if (debug) {
            System.out.println("Processing Stats - Min: " + stats.getMinTime() + " ms, Max: " + stats.getMaxTime() + " ms, Avg: " + stats.getAverageTime() + " ms");
        }

        return processedItems;
    }

    private static <T, R> Map<T, List<R>> processMapListItemsInternal(Map<T, List<R>> items, int numThreads, Function<R, R> itemProcessor, ProcessingStats stats, boolean debug) {
        Map<T, List<R>> processedItems = new HashMap<>();

        for (T key : items.keySet()) {
            List<R> itemList = items.get(key);

            long startTime = System.nanoTime();

            List<R> processedItemList = processItemsInternalR(itemList, numThreads, itemProcessor, stats, debug);

            long endTime = System.nanoTime();
            long elapsedTime = endTime - startTime;

            if (debug) {
                System.out.println("Key " + key + " took " + ((double) elapsedTime / 1_000_000) + " ms");
            }

            processedItems.put(key, processedItemList);
            stats.update(elapsedTime);
        }

        return processedItems;
    }
}