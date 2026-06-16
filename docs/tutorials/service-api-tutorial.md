# CoolStuffLib Service API Tutorial (2025 Edition)

## Service Package Structure

The service system is organized into modular subpackages:
- **api**: Core interfaces (Service, ServiceRegistry, ServiceDescriptor, ServiceFactory, ServiceLifecycleListener, ServiceMetrics, ServiceManagementAPI, ServiceState).
- **impl**: DefaultServiceRegistry and scoped registry implementations.
- **annotation**: ServiceComponent annotation for auto-discovery and registration.
- **exception**: Exception types for robust error handling (ServiceRegistrationException, ServiceLifecycleException, ServiceDescriptorNotFoundException, ServiceDependencyCycleException, DuplicateServiceNameException).
- **event**: Spigot event hooks (ServiceInitEvent, ServiceShutdownEvent, ServiceReloadEvent, ServiceFailEvent).
- **util**: ServiceComponentScanner for annotation-based discovery.

## 1. Service Interface and Lifecycle

Each service implements the `Service` interface, providing a unique UUID, name, and asynchronous lifecycle methods. For field injection, do not declare the id or name fields as final, and do not require them in the constructor or via setter:

```java
public class MyService implements Service {
    private UUID id;
    private String name;
    public MyService() { }
    public UUID id() { return id; }
    public String serviceName() { return name; }
    public CompletableFuture<Void> init() { /* initialization logic */ }
    public CompletableFuture<Void> shutdown() { /* shutdown logic */ }
    public CompletableFuture<Void> onReload() { /* reload logic */ }
}
```
- The registry injects the id and name fields after construction using reflection, regardless of whether a setter exists.
- Constructor injection is still preferred for dependencies, but id/name should be injected as fields.
- `init()`, `shutdown()`, `onReload()`: Asynchronous lifecycle methods. `onReload()` is optional (default no-op).

## 2. ServiceDescriptor

Describes a service for registration, including UUID, name, dependencies, and timeouts:

```java
ServiceDescriptor desc = new ServiceDescriptor(
    UUID.randomUUID(),
    "my-service",
    List.of(), // dependencies (UUIDs)
    Duration.ofSeconds(10), // startup timeout
    Duration.ofSeconds(5)   // shutdown timeout
);
```

## 3. Service Registration and Factories

Register services or factories with the registry. For field injection, instantiate the service with a no-arg constructor and let the registry inject id/name:

```java
ServiceRegistry registry = ServiceRegistryFactory.builder().build();
MyService service = new MyService();
registry.register(desc, service, null); // id and name injected by registry
```
- Direct registration is suitable for simple services with no special construction logic.

### ServiceFactory: Lazy and Custom Instantiation

A `ServiceFactory` allows you to register services that require lazy instantiation, custom setup, or advanced dependency injection. The registry stores the factory and only calls its `create` method when the service is first needed.

```java
ServiceFactory<MyService> factory = registry -> {
    ConfigService config = registry.getByName("config-service").orElseThrow();
    MyService service = new MyService();
    service.setConfig(config); // or use constructor injection
    return CompletableFuture.completedFuture(service);
};
registry.registerFactory(desc, factory, null);
```
- Use factories for services with complex dependencies, custom setup, or when integrating with DI frameworks.
- Factories are also useful for testing, mocking, or deferred creation.
- For simple services, direct registration is preferred.

## 4. Dependency Injection

Services can declare dependencies by accepting them in their constructor. The registry resolves and injects dependencies by UUID or name.

```java
public class DataService implements Service {
    private final ConfigService config;
    public DataService(ConfigService config) { this.config = config; }
    // ...
}
```

## 5. Annotation-Based Discovery

Mark service classes with `@ServiceComponent` for auto-registration. The registry will inject id and name fields after instantiation:

```java
@ServiceComponent(serviceName = "data-service", dependsOn = {"config-service"})
public class DataService implements Service { /* ... */ }
```

Scan and register annotated services:

```java
registry.registerAnnotatedServices("de.myplugin.services");
```
- Uses `ServiceComponentScanner` for recursive package scanning and registry injects id/name fields.

## 6. Exception Handling

Robust error handling is provided via custom exceptions:
- `ServiceRegistrationException`: General registration errors.
- `ServiceLifecycleException`: Lifecycle failures.
- `ServiceDescriptorNotFoundException`: Descriptor missing.
- `ServiceDependencyCycleException`: Cyclic dependencies.
- `DuplicateServiceNameException`: Name conflicts.

Handle exceptions explicitly for reliable service management.

## 7. Event Hooks and Spigot Integration

Lifecycle events are exposed as Spigot events:
- `ServiceInitEvent`
- `ServiceShutdownEvent`
- `ServiceReloadEvent`
- `ServiceFailEvent`

Example listener:

```java
public class MyListener implements Listener {
    @EventHandler
    public void onServiceInit(ServiceInitEvent event) {
        // handle init
    }
}
```
- Events are cancellable (except fail).
- In non-Bukkit environments, event hooks are null-safe.

## 8. Metrics and Health Checks

Track service metrics and health:

```java
long startupMs = registry.getStartupTime(serviceId);
int failures = registry.getHealthCheckFailures(serviceId);

registry.registerHealthCheck("data-service", () -> CompletableFuture.completedFuture(true));
CompletableFuture<Boolean> healthy = registry.isHealthy("data-service");
```
- Metrics available for startup/reload times and health check failures.

## 9. Scoped Registries

Create isolated registries for worlds, players, or other scopes:

```java
ServiceRegistry worldRegistry = ServiceRegistryFactory.builder().build();
ServiceRegistry playerRegistry = ServiceRegistryFactory.builder().build();
```
- Services in one registry are not visible to others unless explicitly shared.

## 10. Advanced Features

- **ServiceLifecycleListener**: Listen for state changes and failures.
- **Health Checks**: Register per-service health checks.
- **Batch Operations**: Start, stop, reload all services in a registry.
- **Custom Annotation Processing**: Extend `ServiceComponentScanner` for advanced DI or metadata.

## 11. Migration Notes

- If your service previously used final fields for id or name, or required them in the constructor or via setter, refactor to use non-final fields and allow field injection. The registry will inject these fields automatically.
- Ensure all services implement the new lifecycle methods.
- Replace legacy event hooks with new event classes and listeners.
- Annotation scanning supports recursive search and duplicate prevention.
- Avoid direct Bukkit/Spigot API calls in service implementations for backend-agnostic usage.

## 12. Best Practices

- Use explicit UUIDs and names for services, injected via fields.
- Prefer annotation-based registration for modularity.
- Register health checks for critical services.
- Handle exceptions explicitly.
- Use event hooks for advanced control and monitoring.
- Isolate registries for per-world/per-player scenarios.

## 13. Backend-Agnostic Usage

The service API is designed for cross-platform compatibility. Use registry event hooks and listeners for platform-specific logic, but keep service implementations generic.

```java
public class UniversalServiceListener implements ServiceLifecycleListener {
    public void onStateChange(UUID serviceId, String serviceName, ServiceState oldState, ServiceState newState, Throwable failure) {
        if (newState == ServiceState.RUNNING) {
            // Platform-agnostic logic here
        }
    }
}
registry.addLifecycleListener(new UniversalServiceListener());
```

## 14. Advanced DI and Annotation Processing

For advanced dependency injection or annotation processing, you can extend the annotation scanner or integrate with third-party DI frameworks. The registry supports constructor injection for dependencies declared in service constructors. For more complex scenarios, consider:
- Custom annotation processors to handle additional metadata or lifecycle hooks.
- Integration with DI frameworks (e.g., Guice, Spring) by adapting the registry’s factory methods.
- Extending the annotation scanner to support custom annotations or multi-stage discovery.

---

For more advanced usage, see the roadmap and API reference in the docs folder.
