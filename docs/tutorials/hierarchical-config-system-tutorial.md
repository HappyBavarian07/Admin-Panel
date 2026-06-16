# Hierarchical Configuration System Tutorial

This tutorial explains how to use CoolStuffLib's enhanced hierarchical section-based configuration system.

## Introduction

The hierarchical configuration system provides a robust way to organize configuration data in a tree-like structure, with support for type safety, event notifications, specialized section types, and metadata including comments. This structure makes it easier to manage complex configurations with nested data.

## Core Components

### Configuration Objects

There are two main types of configuration objects:

1. **AdvancedPersistentConfig**: Stores configuration in a file with support for various formats
2. **AdvancedInMemoryConfig**: Holds configuration in memory only (no persistence)

### Section Types

1. **BaseConfigSection**: Standard section for hierarchical key-value data
2. **ListSection**: Section optimized for ordered collections
3. **MapSection**: Section optimized for key-value maps

### Event System

The configuration system includes a comprehensive event system that notifies listeners of configuration changes:

- Value changes (set/get/remove)
- Section operations (create/remove)
- Configuration lifecycle events (load/save/reload/clear)
- Module lifecycle events (register/unregister/enable/disable)

## Getting Started

### Creating and Managing Configurations

```java
// Create a configuration manager
AdvancedConfigManager manager = new AdvancedConfigManager();

// Create a persistent configuration (stored in a file)
AdvancedConfig persConfig = manager.createPersistentConfig(
    "myConfig",                                     // Configuration name
    new File("path/to/config.yml"),                // File location
    ConfigFileType.YAML                            // File format
);

// Create an in-memory configuration (not persisted)
AdvancedConfig memConfig = manager.createInMemoryConfig("memoryOnlyConfig");

// Save a configuration
persConfig.save();

// Reload a configuration from its source
persConfig.reload();

// Clear all values in a configuration
persConfig.clear();
```

### Working with Basic Values

```java
// Setting values
config.set("server.port", 8080);
config.set("server.host", "localhost");
config.set("app.name", "MyApp");
config.set("app.version", "1.0.0");
config.set("debug", true);

// Getting values with type safety
int port = config.getInt("server.port");
String host = config.getString("server.host");
String appName = config.getString("app.name");
boolean isDebug = config.getBoolean("debug");

// Using default values
int timeout = config.getInt("timeout", 30);
String defaultEnv = config.getString("environment", "production");

// Checking if a key exists
if (config.contains("database.url")) {
    // Use the database URL
}

// Removing values
config.remove("temporary.key");
```

### Working with Sections

```java
// Creating sections
ConfigSection serverSection = config.createSection("server");
serverSection.set("host", "localhost");
serverSection.set("port", 8080);

ConfigSection dbSection = config.createSection("database");
dbSection.set("url", "jdbc:mysql://localhost:3306/mydb");
dbSection.set("username", "user");
dbSection.set("password", "pass");

// Accessing sections
ConfigSection serverConfig = config.getSection("server");
String host = serverConfig.getString("host");
int port = serverConfig.getInt("port");

// Check if a section exists
if (config.hasSection("logging")) {
    // Configure logging options
}

// Removing sections
config.removeSection("temporary.section");

// Getting section keys
Set<String> serverKeys = serverSection.getKeys(false); // Just direct keys
Set<String> allKeys = serverSection.getKeys(true);     // All keys including nested ones

// Converting sections to maps
Map<String, Object> serverMap = serverSection.toMap();
```

### Working with Specialized Sections

```java
// List Section - for ordered collections
ListSection permissionsList = config.createCustomSection("permissions", ListSection.class);
permissionsList.add("READ");
permissionsList.add("WRITE");
permissionsList.add("ADMIN");

// Access items by index
String firstPermission = permissionsList.get(0, String.class);  // "READ"
boolean hasAdmin = permissionsList.contains("ADMIN");          // true

// Add multiple items
permissionsList.addAll(Arrays.asList("DELETE", "CREATE"));

// Map Section - for key-value maps
MapSection userAttributes = config.createCustomSection("user.attributes", MapSection.class);
userAttributes.put("level", 5);
userAttributes.put("experience", 1024);
userAttributes.put("lastLogin", System.currentTimeMillis());

// Access values by key with type safety
int level = userAttributes.getValue("level", Integer.class);
long lastLogin = userAttributes.getValue("lastLogin", Long.class);
```

### Working with Comments

```java
// Adding comments to configuration keys
config.set("server.port", 8080);
config.setComment("server.port", "The port on which the server will listen");

config.set("database.url", "jdbc:mysql://localhost:3306/mydb");
config.setComment("database.url", "MySQL database connection string");

// Adding comments to sections
ConfigSection loggingSection = config.createSection("logging");
config.setComment("logging", "Logging configuration settings");
loggingSection.set("level", "INFO");
loggingSection.setComment("level", "Log level: DEBUG, INFO, WARN, ERROR");

// Retrieving comments
String portComment = config.getComment("server.port"); // "The port on which the server will listen"

// Checking if a comment exists
if (config.hasComment("database.url")) {
    // Comment exists
}

// Removing comments
config.removeComment("temporary.setting");

// Getting all comments
Map<String, String> allComments = config.getComments();
```

### Using Metadata

```java
// Adding metadata to configuration
config.addMetadata("version", "1.0.0");
config.addMetadata("lastModified", System.currentTimeMillis());
config.addMetadata("createdBy", "admin");

// Retrieving metadata with type safety
String version = config.getMetadata("version", String.class);
Long lastModified = config.getMetadata("lastModified", Long.class);

// Checking if metadata exists
if (config.hasMetadata("createdBy")) {
    // Use creator information
}

// Getting all metadata
Map<String, Object> allMetadata = config.getMetadata();

// Removing metadata
config.removeMetadata("temporary.metadata");
```

### Event Handling

```java
// Subscribe to value changes
config.getEventBus().subscribe(ConfigValueEvent.class, event -> {
    if (event.getType() == ConfigValueEvent.Type.SET) {
        System.out.println("Value changed: " + event.getFullPath() +
                           " from " + event.getOldValue() + " to " + event.getNewValue());
    }
});

// Subscribe to section events
config.getEventBus().subscribe(ConfigSectionEvent.class, event -> {
    if (event.getType() == ConfigSectionEvent.Type.CREATED) {
        System.out.println("Section created: " + event.getFullPath());
    } else if (event.getType() == ConfigSectionEvent.Type.REMOVED) {
        System.out.println("Section removed: " + event.getFullPath());
    }
});

// Subscribe to lifecycle events
config.getEventBus().subscribe(ConfigLifecycleEvent.class, event -> {
    switch (event.getType()) {
        case SAVE:
            System.out.println("Config was saved: " + event.getConfigName());
            break;
        case RELOAD:
            System.out.println("Config was reloaded: " + event.getConfigName());
            break;
        case LOAD:
            System.out.println("Config was loaded: " + event.getConfigName());
            break;
    }
});

// Subscribe to events with priority
config.getEventBus().subscribe(ConfigValueEvent.class, event -> {
    // This will run first due to higher priority
    System.out.println("High priority handler");
}, ConfigEventBus.EventPriority.HIGHEST);

// Subscribe to events asynchronously
config.getEventBus().subscribe(ConfigLifecycleEvent.class, event -> {
    // This will run in a separate thread
    System.out.println("Processing asynchronously...");
}, ConfigEventBus.EventPriority.NORMAL, true);

// Cancellable events
config.getEventBus().subscribe(ConfigChangeEvent.class, event -> {
    if (event.getPath().equals("server.port")) {
        System.out.println("Preventing change to server port");
        event.setCancelled(true);
    }
});
```

## Advanced Features

### Modules

```java
// Enable the backup module to automatically back up configurations
PersistentBackupModule backupModule = new PersistentBackupModule(5);  // Keep 5 backups
config.registerModule(backupModule);
config.enableModule(backupModule.getName());

// Enable the change tracker module to keep a history of changes
ConfigChangeTrackerModule trackerModule = new ConfigChangeTrackerModule();
config.registerModule(trackerModule);
config.enableModule(trackerModule.getName());
List<String> changes = trackerModule.getChanges();

// Enable the corruption check module to detect and repair corrupted configs
CorruptionCheckModule corruptionModule = new CorruptionCheckModule();
config.registerModule(corruptionModule);
config.enableModule(corruptionModule.getName());
```

### Multiple File Types

```java
// JSON Configuration
AdvancedConfig jsonConfig = manager.createPersistentConfig(
    "jsonConfig", 
    new File("config.json"), 
    ConfigFileType.JSON
);

// YAML Configuration
AdvancedConfig yamlConfig = manager.createPersistentConfig(
    "yamlConfig", 
    new File("config.yml"), 
    ConfigFileType.YAML
);

// Properties Configuration
AdvancedConfig propsConfig = manager.createPersistentConfig(
    "propsConfig", 
    new File("config.properties"), 
    ConfigFileType.PROPERTIES
);

// INI Configuration
AdvancedConfig iniConfig = manager.createPersistentConfig(
    "iniConfig", 
    new File("config.ini"), 
    ConfigFileType.INI
);

// TOML Configuration
AdvancedConfig tomlConfig = manager.createPersistentConfig(
    "tomlConfig", 
    new File("config.toml"), 
    ConfigFileType.TOML
);
```

### Merging Configurations

```java
// Create two configurations
AdvancedConfig defaultConfig = new AdvancedInMemoryConfig("defaults");
defaultConfig.set("server.port", 8080);
defaultConfig.set("logging.level", "INFO");

AdvancedConfig userConfig = new AdvancedInMemoryConfig("user");
userConfig.set("server.port", 9090);  // Will override the default

// Merge defaultConfig into userConfig
// User values take precedence over defaults
userConfig.copyFrom(defaultConfig);

// Result: userConfig now has
// server.port = 9090 (from userConfig)
// logging.level = INFO (from defaultConfig)
```
