# Advanced Module State Management

## Module State Extensions

The Advanced Config System now provides an extensible state management system for modules. This tutorial explains how to leverage this feature to expose additional module state data for monitoring, debugging, and integration purposes.

## Basic Usage

Both `AbstractBaseConfigModule` and `AbstractGroupConfigModule` classes now provide a standardized way to expose module state through the `getModuleState()` method, which returns a `Map<String, Object>` containing state information.

By default, the following state information is included:

### For Base Config Modules:
- `name` - Module name
- `description` - Module description
- `version` - Module version 
- `state` - Current module state (INITIALIZED, ENABLED, etc.)
- `configured` - Whether the module has been configured
- `dependencies` - List of module dependencies

### For Group Config Modules:
- `name` - Module name
- `description` - Module description
- `version` - Module version
- `enabled` - Whether the module is enabled

## Extending Module State

To add custom state information, override the `getAdditionalModuleState()` method in your module implementation:

```java
public class ValidationModule extends AbstractBaseConfigModule {
    
    private int validationRulesCount = 0;
    private LocalDateTime lastValidationTime;
    private Map<String, List<String>> validationErrors = new HashMap<>();
    
    // Constructor and other required methods...
    
    @Override
    protected Map<String, Object> getAdditionalModuleState() {
        Map<String, Object> state = new HashMap<>();
        
        state.put("rulesCount", validationRulesCount);
        state.put("lastValidationTime", lastValidationTime != null ? 
                lastValidationTime.toString() : null);
        state.put("hasErrors", !validationErrors.isEmpty());
        state.put("errorCount", validationErrors.values().stream()
                .mapToInt(List::size).sum());
        
        return state;
    }
}
```

## Real-world Example: AutoGenModule

The `AutoGenModule` can be extended to expose information about registered templates and other internal state:

```java
public class AutoGenModule extends AbstractBaseConfigModule {
    private final Group rootGroup = new DefaultGroup("", null);
    private final Map<UUID, AutoGenTemplate> templateRegistry = new HashMap<>();
    private final Map<String, UUID> nameToTemplateIdMap = new HashMap<>();
    private final Map<String, UUID> fileToTemplateIdMap = new HashMap<>();

    public AutoGenModule() {
        super("AutoGenModule", "Automatically generates config structure from templates", "1.0.0");
    }
    
    @Override
    protected Map<String, Object> getAdditionalModuleState() {
        Map<String, Object> state = new HashMap<>();
        
        // Add template info
        state.put("templatesCount", templateRegistry.size());
        state.put("registeredTemplateNames", new ArrayList<>(nameToTemplateIdMap.keySet()));
        
        // Add group info
        int totalKeys = countTotalKeys(rootGroup);
        state.put("totalKeys", totalKeys);
        state.put("hasGeneratedStructure", totalKeys > 0);
        
        return state;
    }
    
    private int countTotalKeys(Group group) {
        int count = group.getKeys().size();
        for (Group childGroup : group.getGroups()) {
            count += countTotalKeys(childGroup);
        }
        return count;
    }
}
```

## Accessing Module State

You can access the module state in various ways:

### 1. Direct Access

```java
AdvancedConfig config = manager.getConfig("myConfig");
BaseConfigModule module = config.getModuleByName("ValidationModule");
Map<String, Object> moduleState = module.getModuleState();

System.out.println("Module State: " + moduleState);
```

### 2. Creating a Monitoring UI

The standardized state system makes it easy to create monitoring interfaces:

```java
public void displayModuleState(BaseConfigModule module) {
    Map<String, Object> state = module.getModuleState();
    
    System.out.println("=== " + state.get("name") + " ===");
    System.out.println("Version: " + state.get("version"));
    System.out.println("State: " + state.get("state"));
    
    // Display custom state values
    state.forEach((key, value) -> {
        if (!List.of("name", "description", "version", "state").contains(key)) {
            System.out.println(key + ": " + value);
        }
    });
}
```

### 3. Serializing State for External Systems

```java
ObjectMapper mapper = new ObjectMapper();
String jsonState = mapper.writeValueAsString(module.getModuleState());

// Send to monitoring system, log, or store for later analysis
```

## Benefits

1. **Consistency**: Uniform structure for accessing state across different modules
2. **Extensibility**: Easy to add new state information without changing core interfaces
3. **Integration**: Simple to integrate with monitoring, debugging, or visualization tools
4. **Encapsulation**: Modules control what state information they expose

## Best Practices

1. Only include relevant information in the additional state
2. Use appropriate data types that can be easily serialized
3. Don't expose sensitive information
4. Consider performance impacts when calculating state values
5. Document the meaning of custom state values
6. Use consistent naming conventions for custom state keys
