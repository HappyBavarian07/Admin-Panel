# CoolStuffLib Cache System Documentation

## Overview

The CoolStuffLib cache system provides a unified, thread-safe caching solution that supports both in-memory and persistent storage backends. The system is designed with flexibility and performance in mind, offering specialized integration with the Expression Engine for optimal performance.

## Architecture

### Core Components

- **Cache<K, V>**: Generic cache interface defining standard operations
- **InMemoryCache<K, V>**: High-performance in-memory implementation using ConcurrentHashMap
- **PersistentCache<K, V>**: Interface for persistent storage backends
- **PersistentCacheStub<K, V>**: Basic file-based implementation for development/testing
- **CacheManager**: Centralized management of cache instances

### Expression Engine Integration

- **ExpressionCacheKey**: Composite key combining expression strings with variable state
- **ExpressionCache<V>**: Specialized wrapper for expression-specific caching needs

## Basic Usage

### Creating and Using a Cache

```java
// Create an in-memory cache
Cache<String, Object> cache = new InMemoryCache<>();

// Store values
cache.put("user:123", userObject);
cache.put("config:app", configData);

// Retrieve values
Object user = cache.get("user:123");
boolean exists = cache.containsKey("config:app");

// Remove and clear
cache.remove("user:123");
cache.clear();
```

### Using the Cache Manager

```java
// Register caches with the manager
CacheManager manager = new CacheManager();
manager.registerCache("users", new InMemoryCache<String, User>());
manager.registerCache("sessions", new InMemoryCache<String, Session>());

// Retrieve registered caches
Cache<String, User> userCache = manager.getCache("users");
Cache<String, Session> sessionCache = manager.getCache("sessions");

// List all registered cache names
Set<String> cacheNames = manager.getCacheNames();
```

## Expression Engine Caching

The cache system provides specialized support for the Expression Engine, automatically caching both parsed expressions and evaluation results.

### Automatic Caching

```java
ExpressionEngine engine = new ExpressionEngine();

// Parse caching - subsequent parses of the same expression are cached
Object result1 = engine.evaluate("player.health + 10", variables);
Object result2 = engine.evaluate("player.health + 10", variables); // Uses cached parse

// Evaluation caching - results cached based on expression + variable state
Map<String, Object> vars1 = Map.of("player.health", 100);
Object result3 = engine.evaluate("player.health + 10", vars1); // 110

Map<String, Object> vars2 = Map.of("player.health", 100);
Object result4 = engine.evaluate("player.health + 10", vars2); // Uses cached result
```

### Cache Key Generation

The expression cache uses intelligent key generation that considers:
- The expression string itself
- The hash of relevant variable values used in the expression
- Variable types and names

```java
// These will use different cache entries due to different variable values
engine.evaluate("x + y", Map.of("x", 1, "y", 2)); // Cache key: expr + hash(x=1, y=2)
engine.evaluate("x + y", Map.of("x", 2, "y", 1)); // Cache key: expr + hash(x=2, y=1)

// This will use the same cache entry as the first call
engine.evaluate("x + y", Map.of("x", 1, "y", 2, "z", 999)); // 'z' ignored if not used
```

## Advanced Configuration

### Cache Sizing and Limits

```java
// Create cache with size limit (when implemented)
Cache<String, Object> limitedCache = new InMemoryCache<>(1000); // Max 1000 entries

// Monitor cache performance
InMemoryCache<String, Object> cache = new InMemoryCache<>();
int size = cache.size();
cache.clear();
```

### Persistent Storage

```java
// Use persistent cache for data that should survive application restarts
PersistentCache<String, Object> persistentCache = new PersistentCacheStub<>();
persistentCache.put("setting:theme", "dark");

// Data persists across application restarts
Object theme = persistentCache.get("setting:theme"); // Still available after restart
```

## Performance Considerations

### Thread Safety

All cache implementations are thread-safe and optimized for concurrent access:
- **InMemoryCache**: Uses ConcurrentHashMap for lock-free reads and minimal contention writes
- **PersistentCacheStub**: Synchronizes access to prevent file corruption
- **ExpressionCache**: Thread-safe wrapper around underlying cache implementations

### Best Practices

1. **Cache Granularity**: Cache at appropriate levels - not too fine-grained to avoid overhead
2. **Variable State**: Expression caching automatically handles variable state changes
3. **Memory Management**: Monitor cache sizes in production environments
4. **Cache Warming**: Pre-populate caches with frequently used expressions for better startup performance

```java
// Example: Cache warming for common expressions
ExpressionEngine engine = new ExpressionEngine();
Map<String, Object> commonVars = getCommonVariables();

// Pre-evaluate common expressions to warm the cache
engine.evaluate("player.level * 10", commonVars);
engine.evaluate("item.durability / item.maxDurability", commonVars);
engine.evaluate("Math.min(player.health, 100)", commonVars);
```

## Migration Guide

### For Existing Expression Engine Users

The cache system is automatically enabled and requires no code changes for basic usage. Existing code will immediately benefit from improved performance.

**Before (still works):**
```java
ExpressionEngine engine = new ExpressionEngine();
Object result = engine.evaluate("complex.expression", variables);
```

**After (same code, better performance):**
```java
ExpressionEngine engine = new ExpressionEngine();
Object result = engine.evaluate("complex.expression", variables); // Now cached automatically
```

### Custom Cache Configuration

For applications requiring specific cache behavior:

```java
// Create custom cache manager
CacheManager customManager = new CacheManager();
customManager.registerCache("expressions", new InMemoryCache<>());

// Configure expression engine to use custom cache
ExpressionEngine engine = new ExpressionEngine();
// (Custom cache configuration would be added here in future versions)
```

## Error Handling

The cache system provides robust error handling:

```java
try {
    Object value = cache.get("key");
    if (value == null) {
        // Key not found or null value stored
    }
} catch (Exception e) {
    // Handle cache access errors
    logger.error("Cache access failed", e);
}
```

## Future Extensions

The cache system is designed for extensibility:

### Planned Persistent Backends
- Redis integration for distributed caching
- Database-backed persistent storage
- File-based caching with compression
- Cloud storage integration (AWS S3, etc.)

### Planned Features
- LRU/TTL eviction policies
- Cache metrics and monitoring
- Configuration-driven cache selection
- Cluster-aware distributed caching

## Troubleshooting

### Common Issues

**Cache Miss on Expected Hit:**
- Verify variable values haven't changed
- Check if expression contains non-deterministic functions
- Ensure thread safety in variable access

**Memory Usage:**
- Monitor cache sizes in production
- Consider implementing size limits
- Use persistent caching for large datasets

**Performance:**
- Warm caches during application startup
- Profile cache hit/miss ratios
- Consider cache granularity adjustments

### Debugging Cache Behavior

```java
// Enable detailed logging (when implemented)
cache.setLoggingEnabled(true);

// Monitor cache statistics
int hitCount = cache.getHitCount();
int missCount = cache.getMissCount();
double hitRatio = (double) hitCount / (hitCount + missCount);
```

## API Reference

See the Javadoc documentation for complete API details of all cache interfaces and implementations.
