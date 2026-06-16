# CoolStuffLib JPA Repository System Tutorial

## Table of Contents

1. Introduction
2. Architecture Overview
3. Entity Mapping with Annotations
4. Repository Interfaces and Usage
5. RepositoryController: Central Management
6. SQLExecutor: Query Execution
7. Connection, Cache, and Transaction Management
8. Advanced Features
9. Example: Complete Workflow
10. Extending and Customizing
11. Troubleshooting & FAQ

---

## 1. Introduction

The CoolStuffLib JPA system provides a flexible, annotation-driven repository layer for Java applications. It supports entity mapping, CRUD operations, transaction management, and dynamic repository creation, similar to standard JPA frameworks.

---

## 2. Architecture Overview

- **Annotations**: Define entity structure and relationships.
- **Repository Interface**: Generic CRUD operations.
- **AsyncRepository**: Asynchronous data access.
- **RepositoryController**: Manages repositories and connections.
- **SQLExecutor**: Executes SQL queries and updates.
- **ConnectionPool**: Manages database connections.
- **EntityCache**: Caches entities for performance.
- **TransactionManager**: Handles transactions.
- **DatabaseProperties**: Stores DB configuration.
- **RepositoryProxy**: Dynamically implements repository interfaces.

---

## 2a. Initialization with CoolStuffLibBuilder

The recommended way to set up the JPA repository system is via the builder pattern. This ensures all connections and repository managers are properly configured.

```java
CoolStuffLib lib = new CoolStuffLibBuilder(plugin)
    .withRepositoryManager()
    .build()
    .createCoolStuffLib();
```

You can configure database connections (MySQL, SQLite, etc.) using the builder:

```java
CoolStuffLib lib = new CoolStuffLibBuilder(plugin)
    .withRepositoryManager()
        .addMySQLConnection("main", "localhost", "mydb", "user", "pass")
        .setDefaultConnection("main")
    .build()
    .createCoolStuffLib();
```

Obtain repository instances via the RepositoryManager:

```java
UserRepository repo = lib.getRepositoryManager().getRepository(UserRepository.class);
```

---

## 3. Entity Mapping with Annotations

Entities are POJOs annotated to describe their database mapping.

```java
import de.happybavarian07.coolstufflib.jpa.annotations.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private int id;

    @Column(name = "username")
    private String username;

    // Getters and setters
}
```

Supported annotations:
- `@Entity`, `@Table`, `@Id`, `@Column`, `@GeneratedValue`, `@JoinColumn`, `@ManyToOne`, `@OneToMany`, `@ManyToMany`
- Lifecycle: `@PrePersist`, `@PostLoad`, `@PreUpdate`, `@PostInit`
- Transaction: `@Transactional`
- Advanced: `@CacheConfig`, `@CascadeType`, `@FetchType`

---

## 4. Repository Interfaces and Usage

Repositories are interfaces that define CRUD and query methods for entities. The system auto-generates implementations using dynamic proxies.

Example:

```java
import de.happybavarian07.coolstufflib.jpa.repository.*;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    List<User> findAllByActive(boolean active);
    // New direct field access methods:
    void setUsername(int id, String username);
    String getUsername(int id);
    long countColumnsByActive(boolean active);
    User insertUser(User user);
    User updateUser(User user);
}
```

- **JpaRepository<T, ID>**: Base interface for CRUD operations.
- Custom methods (e.g., `findByUsername`, `setUsername`, `getUsername`, `countColumnsByActive`, `insertUser`, `updateUser`) are detected and implemented at runtime.

### How RepositoryProxy Works

The `RepositoryProxy` scans repository interfaces for method names matching patterns like `findBy`, `findAllBy`, `countBy`, `countColumnsBy`, `set<Field>`, `get<Field>`, `insert<Field>`, `update<Field>`, etc. It parses method names and parameters to generate SQL queries or field access dynamically.

Supported patterns:
- `findBy<Field>`: Returns single entity by field value.
- `findAllBy<Field>`: Returns list of entities matching field value.
- `countBy<Field>`: Returns count of entities matching field value.
- `countColumnsBy<Field>`: Returns count of entities matching a column value.
- `set<Field>(id, value)`: Sets a field value for an entity by id.
- `get<Field>(id)`: Gets a field value for an entity by id.
- `insert<Field>(entity)`: Inserts a new entity.
- `update<Field>(entity)`: Updates an existing entity.
- `deleteBy<Field>`: Deletes entities matching field value.

#### Example: Dynamic Query Generation

```java
UserRepository repo = RepositoryFactory.create(UserRepository.class);
User user = repo.findByUsername("alice");
List<User> activeUsers = repo.findAllByActive(true);
int count = repo.countByActive(true);
repo.deleteByUsername("bob");
repo.setUsername(1, "newname");
String name = repo.getUsername(1);
long activeCount = repo.countColumnsByActive(true);
User inserted = repo.insertUser(new User(...));
User updated = repo.updateUser(existingUser);
```

- The proxy analyzes method names, maps them to entity fields, and builds SQL queries accordingly.
- Supports nested properties (e.g., `findByAddress_City`).
- Handles basic types, enums, and relationships.

### Custom Queries and Advanced Usage

For complex queries, annotate methods with `@Query`:

```java
@Query("SELECT u FROM User u WHERE u.lastLogin < :date")
List<User> findInactiveSince(Date date);
```

- Parameters are mapped by name or position.
- Supports native SQL and JPQL.

---

## 5. Transactions and Lifecycle Events

Use `@Transactional` to mark methods for transaction management:

```java
@Transactional
void updateUser(User user) {
    // ...
}
```

Lifecycle annotations:
- `@PrePersist`, `@PostLoad`, `@PreUpdate`, `@PostInit`
- Methods annotated are called at appropriate entity lifecycle stages.

---

## 6. Full Example: Putting It All Together

### Entity
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private int id;
    @Column(name = "username")
    private String username;
    @Column(name = "active")
    private boolean active;
    // Getters and setters
}
```

### Repository
```java
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    List<User> findAllByActive(boolean active);
    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findAllActive();
    void setUsername(int id, String username);
    String getUsername(int id);
    long countColumnsByActive(boolean active);
    User insertUser(User user);
    User updateUser(User user);
}
```

### Usage

```java
import java.util.List;

UserRepository repo = RepositoryFactory.create(UserRepository.class);
User user = repo.findByUsername("alice");
List<User> activeUsers = repo.findAllActive();
repo.setUsername(user.getId(), "newname");
String name = repo.getUsername(user.getId());
long activeCount = repo.countColumnsByActive(true);
User inserted = repo.insertUser(new User(...));
User updated = repo.updateUser(user);
```

---

## 7. Implementation Steps

1. Define your entity classes with JPA annotations.
2. Create repository interfaces extending `JpaRepository`.
3. Use method naming conventions for queries, or annotate with `@Query` for custom SQL.
4. Use direct field access and column count methods as needed.
5. Obtain repository instances via `RepositoryFactory.create()`.
6. Use repositories for CRUD and queries; transactions and lifecycle events are handled automatically.

---

## 8. Advanced Features

- **Caching**: Enable with `@CacheConfig` on entities.
- **Cascade Operations**: Use `@CascadeType` on relationships.
- **Fetch Strategies**: Control with `@FetchType`.
- **Custom Transaction Management**: Use `TransactionManager` directly for advanced scenarios.

---

## 9. Troubleshooting & Tips

- Ensure entity fields match database columns.
- Use correct method naming for auto-query generation and direct field access.
- Annotate complex queries with `@Query`.
- Check DB connection settings in `DatabaseProperties`.
- Use `EntityCache` for performance tuning.

---

## 10. References

- [JPA Specification](https://jakarta.ee/specifications/persistence/)
- [CoolStuffLib API Docs](../apidocs/index.html)

---

This tutorial covers all major aspects of the CoolStuffLib JPA repository system, including dynamic repository proxies, direct field access, and method detection. For further details, consult the API documentation or source code.
