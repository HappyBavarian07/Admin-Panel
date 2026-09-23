# CoolStuffLib Service API Tutorial

## Service Package Structure

The service system is organized into modular subpackages:
- **api**: Core interfaces (Service, ServiceRegistry, ServiceDescriptor, ServiceFactory, ServiceLifecycleListener,
  ServiceMetrics, ServiceManagementAPI, ServiceState, ServiceRegistryFactory).
- **impl**: DefaultServiceRegistry, scoped registries and built-in services such as ChatInputService.
- **annotation**: ServiceComponent annotation for auto-discovery and registration.
- **exception**: ServiceRegistrationException, ServiceLifecycleException, ServiceDescriptorNotFoundException,
  ServiceDependencyCycleException, DuplicateServiceNameException, ServiceIdInjectionException.
- **event**: Spigot event hooks (ServiceInitEvent, ServiceShutdownEvent, ServiceReloadEvent, ServiceFailEvent).
- **util**: ServiceComponentScanner for annotation-based discovery.

## 1. Service Interface and Lifecycle

Each service implements `Service`: an id, a name and asynchronous lifecycle methods.

```java
public class MyService implements Service {
    private final UUID serviceId = UUID.randomUUID();

    public UUID id() { return serviceId; }
    public String serviceName() { return "my-service"; }
    public CompletableFuture<Void> init() { return CompletableFuture.completedFuture(null); }
    public CompletableFuture<Void> shutdown() { return CompletableFuture.completedFuture(null); }
    public CompletableFuture<Void> onReload() { return CompletableFuture.completedFuture(null); } // optional
}
```

Id and name handling on registration:

- If the class (or a superclass) has a field literally named `id` or `name` that is still `null`, the
  registry fills it in. Existing values are never overwritten.
- Otherwise `id()` must return a non-null value, as above. Only a service without any id is rejected with
  `ServiceIdInjectionException`.
- The registry itself looks services up by the key they were registered under (see step 3), not by `id()`.

## 2. ServiceDescriptor

Describes a service for registration. The quick form uses the name for a stable id, no dependencies and
5 second timeouts:

```java
ServiceDescriptor desc = ServiceDescriptor.of("my-service");
```

The full form:

```java
ServiceDescriptor desc = new ServiceDescriptor(
    UUID.randomUUID(),
    "my-service",
    List.of(),               // dependencies (UUIDs)
    Duration.ofSeconds(10),  // startup timeout
    Duration.ofSeconds(5)    // shutdown timeout
);
```

## 3. Service Registration and Factories

```java
ServiceRegistry registry = ServiceRegistryFactory.builder().build();
UUID key = registry.register(ServiceDescriptor.of("my-service"), new MyService(), null).second();
registry.start(key).join();
```

- With `null` as the last argument the service is stored under `descriptor.id()`; pass a UUID to choose the key.
- `registry.getIdByName("my-service")` returns the key, `registry.getAsByName("my-service", MyService.class)` the
  service.
- Names are unique; registering the same name twice throws `DuplicateServiceNameException`.

### ServiceFactory: Lazy and Custom Instantiation

```java
ServiceFactory<MyService> factory = reg -> {
    ConfigService config = reg.getAsByName("config-service", ConfigService.class).orElseThrow();
    return CompletableFuture.completedFuture(new MyService(config));
};
registry.registerFactory(ServiceDescriptor.of("my-service"), factory, null);
```

Use factories for services with dependencies or custom setup; direct registration for simple services.

## 4. Services Inside CoolStuffLib

`CoolStuffLib.setup()` registers and starts the core services (language manager, command registry, menu addon
manager, repository manager, cache manager, backup manager, chat input service) with
`ServiceDescriptor.of(<name>)`. If any of them fails to start, `setup()` throws an `IllegalStateException`
that lists every failed service.

To get a service anywhere in your plugin:

```java
ChatInputService chat = CoolStuffLib.getLib().requireService("chat-input-service", ChatInputService.class);
```

`requireService(name, type)`:
- returns the running service, starting it first if it is registered but not running;
- if nothing is registered under the name, creates the type with its no-arg constructor, registers and
  starts it;
- throws `IllegalStateException` if the name belongs to a service of another type, or the type has no no-arg
  constructor (register such services yourself first).

It blocks until the service is started, bounded by the descriptor's start timeout.

## 5. Annotation-Based Discovery

```java
@ServiceComponent(serviceName = "data-service", dependsOn = {"config-service"})
public class DataService implements Service { /* ... */ }
```

```java
registry.registerAnnotatedServices("de.myplugin.services", config);
```

`ServiceComponentScanner` searches the package recursively; `config` is passed to constructors that accept one.

## 6. Exception Handling

- `ServiceRegistrationException`: general registration errors.
- `ServiceIdInjectionException`: the service has no id at all.
- `ServiceLifecycleException`: lifecycle failures.
- `ServiceDescriptorNotFoundException`: descriptor missing.
- `ServiceDependencyCycleException`: cyclic dependencies.
- `DuplicateServiceNameException`: name conflicts.

Failures during start/stop mark the service `FAILED` and complete the returned future exceptionally.

## 7. Event Hooks and Spigot Integration

Lifecycle events are exposed as Spigot events: `ServiceInitEvent`, `ServiceShutdownEvent`,
`ServiceReloadEvent`, `ServiceFailEvent`.

```java
public class MyListener implements Listener {
    @EventHandler
    public void onServiceInit(ServiceInitEvent event) {
        // handle init
    }
}
```

- Events are cancellable (except fail). Cancelling init/shutdown/reload marks the service `FAILED`.
- Without a running Bukkit server (e.g. in unit tests) no events are fired.

## 8. Metrics and Health Checks

```java
UUID id = registry.getIdByName("data-service");
long startupMs = registry.getStartupTime(id);
int failures = registry.getHealthCheckFailures(id);

registry.registerHealthCheck(id, () -> CompletableFuture.completedFuture(true));
CompletableFuture<Boolean> healthy = registry.isHealthy(id);
```

## 9. Scoped Registries

Create isolated registries for worlds, players, or other scopes:

```java
ServiceRegistry worldRegistry = ServiceRegistryFactory.builder().build();
ServiceRegistry playerRegistry = ServiceRegistryFactory.builder().enforceUniqueNames(false).build();
```

Services in one registry are not visible to others unless explicitly shared.

## 10. Lifecycle Listeners

```java
registry.addListener((serviceId, serviceName, from, to, failure) -> {
    if (to == ServiceState.FAILED) getLogger().warning(serviceName + " failed: " + failure);
});
```

## 11. Migration Notes

- Services may keep their own final id (e.g. `private final UUID serviceId`) and return it from `id()`; a field
  named `id` is no longer required.
- `register(desc, service, null)` keys the service by `desc.id()` instead of a new random UUID.
- Prefer `ServiceDescriptor.of(name)` over hand-written descriptors for simple services.
- `CoolStuffLib.getLib()` throws if the library is not initialized yet instead of returning `null`.

## 12. Best Practices

- Give every service a stable name; look it up by name, not by `id()`.
- Register health checks for critical services.
- Keep `init()`/`shutdown()` short; they run within the descriptor's timeouts.
- Keep service implementations free of Bukkit calls where possible so they stay testable.
