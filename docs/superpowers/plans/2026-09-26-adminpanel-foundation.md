# AdminPanel Foundation Migration (Plan 1 of 4) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move AdminPanel's infrastructure onto CoolStuffLib 3.0: the library's StartUpLogger, BackupManager and RepositoryManager, JPA tables instead of the home-grown DataService, and a one-time import of the old data, while fixing the database, startup and shutdown bugs found on the way.

**Architecture:** `AdminPanelMain` keeps its linear startup, but every infrastructure piece comes from the `CoolStuffLibBuilder` (backup manager, logging, repository manager with the connection from `database.properties`). Runtime data that the DataService held moves into three small JPA tables (`plugin_flags`, `plugin_descriptions`, `updater_cache`) that a new `AdminPanelData` class registers; per-player languages go back to CoolStuffLib's `PerPlayerLanguageHandler` (section in `data.yml`). A `LegacyDataImporter` reads the old `plugin_data.db` once and renames it.

**Tech Stack:** Java 17, Maven, Spigot API (compiled against 1.20.1, tested on 1.21.10), CoolStuffLib 3.0.0 (local install from `F:\InteliJ Programs\CoolStuffLib`, branch `newfeature`), SQLite (sqlite-jdbc, provided), JUnit 5 + Mockito.

**Spec:** No separate spec. Requirements come from the user's decisions on 2026-09-26: replace the DataService with JPA tables (with import), keep the class-based commands (Plan 2), replace AdminPanel's own BackupManager/StartUpLogger with CoolStuffLib's. Bug references (A1…A23) are in `.claude/issues/AUDIT.md`.

## The four plans

1. **Foundation (this plan):** logging, backups, database config, DataService → JPA, legacy import, startup/shutdown robustness.
2. **Commands:** modernize the 38 class-based sub commands (`arguments()`/`CommandArgs`, localized errors, cooldown/confirm), fix A22 (three addon commands named "enable") and A23 (`/disableditems` never registered).
3. **Menus:** move the remaining `isSimilar` handling and hand-written back/close buttons to `MenuAction` decorators, language-file buttons and `ListMenu`/`PaginatedMenu` zones (A18/A21).
4. **Cleanup:** `printStackTrace`/`System.out` → file logger (A11), unified async policy (A13), packaging (A14/A15), tests.

Plans 2–4 are written after this one lands, because they build on the new startup.

## Global Constraints

- Never write `config.yml` from code. Runtime data goes to `data.yml` sections or database tables, one per feature.
- Commits: small, one logical change each, **no `Co-Authored-By` or other AI attribution**.
- Minimal comments, no reformatting of untouched code.
- Before editing an AdminPanel symbol, run GitNexus impact: `npx --no-install gitnexus impact <symbol> --direction upstream --repo AdminPanel` and report HIGH/CRITICAL before continuing.
- CoolStuffLib changes: run `mvn -B test -Dgpg.skip` in `F:\InteliJ Programs\CoolStuffLib`, then `mvn -B -q install -DskipTests -Dgpg.skip` before building AdminPanel.
- AdminPanel build: `mvn -B -q package -DskipTests` (the pom copies the jar to the 1.20.2/1.21.1/1.21.5 test servers). Copy `target/Admin-Panel-<version>.jar` to `F:/Weitere Minecraft Test Server/Spigot 1.21.10/plugins/` for the smoke test.
- Test server: `F:\Weitere Minecraft Test Server\Spigot 1.21.10`, Java `F:\Weitere Minecraft Test Server\jdk-21.0.5\bin\java`.
- Docs are first person ("I"), never "we".

---

## File Structure

```
CoolStuffLib (F:\InteliJ Programs\CoolStuffLib)
  src/main/java/.../CoolStuffLibBuilder.java           Task 1: setBackupManager, public addConnection, MySQL with port
  src/main/java/.../jpa/utils/DatabaseProperties.java  Task 1: "mariadb" driver alias
  src/test/java/.../CoolStuffLibBuilderConnectionTest.java  Task 1

AdminPanel (C:\Users\quiri\IdeaProjects\AdminPanel) src/main/java/de/happybavarian07/adminpanel/
  utils/StartUpLogger.java                        Task 2: deleted (CoolStuffLib's is used)
  backupmanager/*                                 Task 3: deleted (CoolStuffLib's is used)
  main/DatabaseSettings.java                      Task 4: parses database.properties (A1, A2, A12)
  data/AdminPanelData.java                        Task 5: registers and exposes the repositories
  data/PluginFlag.java, PluginFlagRepository.java           Task 5
  data/PluginDescriptionEntry.java, PluginDescriptionRepository.java  Task 5
  data/UpdaterCacheEntry.java, UpdaterCacheRepository.java  Task 5
  utils/managers/PluginStateManager.java          Task 6
  utils/managers/PluginDescriptionManager.java    Task 7
  utils/NewUpdater.java, utils/managers/AutoUpdaterManager.java  Task 7
  utils/managers/AdminPanelPerPlayerLanguageHandler.java  Task 8: deleted
  data/LegacyDataImporter.java                    Task 9
  service/**                                      Task 10: deleted
  main/AdminPanelMain.java                        Tasks 2–11
  tools/run-adminpanel-smoke.sh                   Task 12
src/test/java/de/happybavarian07/adminpanel/
  main/DatabaseSettingsTest.java                  Task 4
  data/LegacyDataImporterTest.java                Task 9
```

---

### Task 1: CoolStuffLib builder additions for AdminPanel

**Files:**
- Modify: `F:/InteliJ Programs/CoolStuffLib/src/main/java/de/happybavarian07/coolstufflib/CoolStuffLibBuilder.java`
- Modify: `F:/InteliJ Programs/CoolStuffLib/src/main/java/de/happybavarian07/coolstufflib/jpa/utils/DatabaseProperties.java`
- Test: `F:/InteliJ Programs/CoolStuffLib/src/test/java/de/happybavarian07/coolstufflib/jpa/DatabasePropertiesTest.java`

**Interfaces:**
- Produces: `CoolStuffLibBuilder#setBackupManager(BackupManager)`; `RepositoryManagerBuilder#addConnection(String name, DatabaseProperties props)` (public, applies the builder prefix); `RepositoryManagerBuilder#addMySQLConnection(String name, String host, String port, String database, String username, String password)`; `DatabaseProperties` accepts driver `"mariadb"` as MySQL.

- [ ] **Step 1: Write the failing test**

```java
package de.happybavarian07.coolstufflib.jpa;

import de.happybavarian07.coolstufflib.jpa.utils.DatabaseProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabasePropertiesTest {
    @Test
    void mariadbUsesTheMysqlUrlWithItsPort() {
        DatabaseProperties props = new DatabaseProperties();
        props.setDriver("mariadb");
        props.setHost("db.example");
        props.setPort("3307");
        props.setDatabase("panel");
        assertEquals("jdbc:mysql://db.example:3307/panel?useSSL=false&allowPublicKeyRetrieval=true", props.getConnectionString());
    }
}
```

- [ ] **Step 2: Run it, expect FAIL** (`IllegalArgumentException: Unsupported database driver: mariadb`)

Run: `mvn -B -q test -Dtest=DatabasePropertiesTest -Dgpg.skip` in `F:\InteliJ Programs\CoolStuffLib`

- [ ] **Step 3: Implement**

In `DatabaseProperties.getConnectionString()` replace
```java
        } else if ("mysql".equalsIgnoreCase(driver)) {
```
with
```java
        } else if ("mysql".equalsIgnoreCase(driver) || "mariadb".equalsIgnoreCase(driver)) {
            // The MySQL driver that Spigot ships also talks to MariaDB
```

In `CoolStuffLibBuilder`, after `setPluginFileLogger(...)`:
```java
    /** Uses a backup manager that was created earlier, e.g. in {@code onLoad}; CoolStuffLib starts and stops it. */
    public CoolStuffLibBuilder setBackupManager(BackupManager backupManager) {
        this.backupManager = backupManager;
        return this;
    }
```

In `RepositoryManagerBuilder`, after `addMySQLConnection(String, String, String, String, String)`:
```java
        public RepositoryManagerBuilder addMySQLConnection(String name, String host, String port, String database, String username, String password) {
            DatabaseProperties props = new DatabaseProperties();
            props.setHost(host);
            props.setPort(port);
            props.setDatabase(database);
            props.setUsername(username);
            props.setPassword(password);
            props.setDriver("mysql");
            return addConnection(name, props);
        }
```
and change the private `addConnection` to:
```java
        /** Adds a connection with properties built by the caller; the builder's table prefix is applied. */
        public RepositoryManagerBuilder addConnection(String name, DatabaseProperties props) {
            props.setDatabasePrefix(databasePrefix);
            connections.add(props);
            repositoryManager.addConnection(name, props);
            return this;
        }
```
(the two existing callers ignore the return value, no other change needed).

- [ ] **Step 4: Run all CoolStuffLib tests, expect PASS**, then install

Run: `mvn -B test -Dgpg.skip` → `Tests run: 446, Failures: 0`, then `mvn -B -q install -DskipTests -Dgpg.skip`.

- [ ] **Step 5: Commit** (in CoolStuffLib)

```bash
git add src/main/java/de/happybavarian07/coolstufflib/CoolStuffLibBuilder.java src/main/java/de/happybavarian07/coolstufflib/jpa/utils/DatabaseProperties.java src/test/java/de/happybavarian07/coolstufflib/jpa/DatabasePropertiesTest.java
git commit -m "feat(builder): accept an existing backup manager, custom connection properties and MariaDB"
```

---

### Task 2: Use CoolStuffLib's StartUpLogger (A19)

**Files:**
- Delete: `src/main/java/de/happybavarian07/adminpanel/utils/StartUpLogger.java`
- Modify: every file that references the type `StartUpLogger` (7 explicit imports plus same-package/wildcard users; the script finds them)

The two classes have the same public API (checked 2026-09-26), so only imports change. The AdminPanel copy crashes on Java 20+ (`Thread.suspend/resume`).

- [ ] **Step 1: Impact check**

Run: `npx --no-install gitnexus impact StartUpLogger --direction upstream --repo AdminPanel` and note the risk level.

- [ ] **Step 2: Replace the imports and delete the copy**

Save as a scratch script and run from the repo root:
```python
import os, re
root = "src/main/java/de/happybavarian07/adminpanel"
old = "import de.happybavarian07.adminpanel.utils.StartUpLogger;"
new = "import de.happybavarian07.coolstufflib.utils.StartUpLogger;"
for dp, _, files in os.walk(root):
    for f in files:
        if not f.endswith(".java") or f == "StartUpLogger.java":
            continue
        p = os.path.join(dp, f)
        s = open(p, encoding="utf-8").read()
        uses_type = re.search(r"(?<![A-Za-z])StartUpLogger\b", s.replace("getStartUpLogger", ""))
        if old in s:
            s = s.replace(old, new)
        elif uses_type and new not in s:
            s = re.sub(r"(package [^;]+;\n)", r"\1\n" + new + "\n", s, count=1)
        else:
            continue
        open(p, "w", encoding="utf-8", newline="").write(s)
        print("updated", p)
os.remove(os.path.join(root, "utils", "StartUpLogger.java"))
```

- [ ] **Step 3: Build**

Run: `mvn -B -q package -DskipTests`
Expected: exit 0. If the compiler reports an unused/duplicate import in a file, remove it there.

- [ ] **Step 4: Commit**

```bash
git add -A src/main/java
git commit -m "refactor: use CoolStuffLib's StartUpLogger instead of the copy that fails on Java 20+"
```

---

### Task 3: Use CoolStuffLib's BackupManager

**Files:**
- Delete: `src/main/java/de/happybavarian07/adminpanel/backupmanager/` (BackupManager, FileBackup, BackupExecuteRunnable)
- Modify: `main/AdminPanelMain.java` (`setupBackupManager`, `getTimeInTicks`, `onEnable` builder, `executeBackupManager`, imports)
- Modify: `utils/managers/FileCorruptionManager.java`, `commands/subcommands/adminpaneladmin/BackupManagerCommand.java` (imports; `%backupDest%` uses `getPath()`)

**Interfaces:**
- Consumes: `CoolStuffLibBuilder#setBackupManager` (Task 1), CoolStuffLib `BackupManager(int retention, long intervalSeconds)`, `FileBackup(String id, List<RegexFileFilter> include, List<RegexFileFilter> exclude, File destination, File root)`, `de.happybavarian07.coolstufflib.backupmanager.RegexFileFilter`.
- Produces: `AdminPanelMain#getBackupManager()` now returns `de.happybavarian07.coolstufflib.backupmanager.BackupManager`.

- [ ] **Step 1: Impact check** for `BackupManager` and `FileBackup` (`--repo AdminPanel`), report the level.

- [ ] **Step 2: Rewrite `setupBackupManager` and the interval helper**

Replace `setupBackupManager()` and `getTimeInTicks()` with:
```java
    private void setupBackupManager() {
        new File(this.getDataFolder() + "/languages").mkdir();
        String backupFolderConfig = getConfig().getString("Plugin.BackupManager.BackupFolder", "backups");
        int numberOfBackupsBeforeDeleting = getConfig().getInt("Plugin.BackupManager.NumberOfBackupsAtOnce", 5);
        backupManager = new BackupManager(numberOfBackupsBeforeDeleting, getBackupIntervalSeconds());
        List<RegexFileFilter> filters = List.of(new RegexFileFilter(".*\\.(yml|db|properties)"));
        List<RegexFileFilter> excludeFilters = List.of(new RegexFileFilter(".*\\.txt"), new RegexFileFilter("addons/.*\\.jar"));
        File destination = new File(new File(getDataFolder(), backupFolderConfig), "ConfigBackup");
        destination.mkdirs();
        backupManager.addFileBackup(new FileBackup("ConfigBackup", filters, excludeFilters, destination, getDataFolder()));
    }

    /** "24" = hours, or a number with s/m/h/d, from Plugin.BackupManager.BackupTime. */
    private long getBackupIntervalSeconds() {
        String backupTime = getConfig().getString("Plugin.BackupManager.BackupTime", "24");
        if (backupTime.isEmpty()) {
            throw new IllegalArgumentException("Backup time is not configured properly.");
        }
        char unit = backupTime.charAt(backupTime.length() - 1);
        String number = Character.isDigit(unit) ? backupTime : backupTime.substring(0, backupTime.length() - 1);
        long value;
        try {
            value = Long.parseLong(number);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid time value: " + number);
        }
        return switch (unit) {
            case 's' -> value;
            case 'm' -> value * 60;
            case 'd' -> value * 60 * 60 * 24;
            default -> value * 60 * 60;
        };
    }
```
Imports: remove `de.happybavarian07.adminpanel.backupmanager.*` and `org.apache.commons.io.filefilter.RegexFileFilter`; add `de.happybavarian07.coolstufflib.backupmanager.BackupManager`, `de.happybavarian07.coolstufflib.backupmanager.FileBackup`, `de.happybavarian07.coolstufflib.backupmanager.RegexFileFilter`.

- [ ] **Step 3: Hand the manager to CoolStuffLib**

In `onEnable`, change the builder line to:
```java
        CoolStuffLibBuilder coolStuffLibBuilder = new CoolStuffLibBuilder(this)
                .setDataFile(new File(getDataFolder(), "data.yml"))
                .setBackupManager(backupManager)
                .withLogging().create(fileLogger).build();
```
CoolStuffLib now starts the scheduler in `setup()` and stops it in `shutdown()`. `executeBackupManager()` keeps its single `backupManager.startBackup("ConfigBackup")` call (one backup per start).

- [ ] **Step 4: Fix the other users**

- `FileCorruptionManager`, `BackupManagerCommand`, `BungeeTestCommand`, `ConfigReloadMenu`: replace `de.happybavarian07.adminpanel.backupmanager.` imports with `de.happybavarian07.coolstufflib.backupmanager.`.
- `BackupManagerCommand#formatStringWithFileBackup`: `fileBackup.getDestinationPathToBackupToo()` now returns a `File`; use `fileBackup.getDestinationPathToBackupToo().getPath()`.

- [ ] **Step 5: Delete the old package and build**

```bash
git rm -r -q src/main/java/de/happybavarian07/adminpanel/backupmanager
mvn -B -q package -DskipTests
```
Expected: exit 0.

- [ ] **Step 6: Commit**

```bash
git add -A src/main/java
git commit -m "refactor: use CoolStuffLib's BackupManager and let CoolStuffLib run its scheduler"
```

---

### Task 4: Database settings and CoolStuffLib's RepositoryManager (A1, A2, A12)

**Files:**
- Create: `src/main/java/de/happybavarian07/adminpanel/main/DatabaseSettings.java`
- Create: `src/test/java/de/happybavarian07/adminpanel/main/DatabaseSettingsTest.java`
- Modify: `main/AdminPanelMain.java` (`setupRepositoryController` → `configureDatabase(builder)`, `getRepositoryController`, `setupPermissions`), `permissions/PermissionsManager.java:36`

**Interfaces:**
- Consumes: `RepositoryManagerBuilder#addConnection(String, DatabaseProperties)` (Task 1).
- Produces: `DatabaseSettings.parse(Properties, File dataFolder)` returning `DatabaseProperties`; `AdminPanelMain#getRepositoryManager()` (`de.happybavarian07.coolstufflib.repository.RepositoryManager`); constant `AdminPanelMain.DB_CONNECTION = "main"`.

`database.properties` keys (as shipped): `dbtype`, `sqlite_file_path`, `mysql_host`, `mysql_port`, `mysql_user`, `mysql_password`, `mysql_database`, `database_prefix`. Verify with `cat src/main/resources/database.properties` before writing the test; if a key differs, use the file's spelling in both the test and the parser.

- [ ] **Step 1: Write the failing test**

```java
package de.happybavarian07.adminpanel.main;

import de.happybavarian07.coolstufflib.jpa.utils.DatabaseProperties;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseSettingsTest {
    private final File dataFolder = new File("plugins/Admin-Panel").getAbsoluteFile();

    @Test
    void missingValuesFallBackToSqliteInTheDataFolder() {
        DatabaseProperties props = DatabaseSettings.parse(new Properties(), dataFolder);
        assertEquals("sqlite", props.getDriver());
        assertEquals(new File(dataFolder, "AdminPanelDatabase.db").getAbsolutePath(), props.getDatabase());
        assertEquals("", props.getDatabasePrefix());
    }

    @Test
    void relativeSqlitePathIsResolvedAgainstTheDataFolder() {
        Properties in = new Properties();
        in.setProperty("dbtype", "sqlite");
        in.setProperty("sqlite_file_path", "data/panel.db");
        assertEquals(new File(dataFolder, "data/panel.db").getAbsolutePath(), DatabaseSettings.parse(in, dataFolder).getDatabase());
    }

    @Test
    void mysqlKeysAreRead() {
        Properties in = new Properties();
        in.setProperty("dbtype", "mariadb");
        in.setProperty("mysql_host", "db.example");
        in.setProperty("mysql_port", "3307");
        in.setProperty("mysql_user", "panel");
        in.setProperty("mysql_password", "secret");
        in.setProperty("mysql_database", "adminpanel");
        in.setProperty("database_prefix", "ap_");
        DatabaseProperties props = DatabaseSettings.parse(in, dataFolder);
        assertEquals("mariadb", props.getDriver());
        assertEquals("db.example", props.getHost());
        assertEquals("3307", props.getPort());
        assertEquals("panel", props.getUsername());
        assertEquals("secret", props.getPassword());
        assertEquals("adminpanel", props.getDatabase());
        assertEquals("ap_", props.getDatabasePrefix());
    }

    @Test
    void invalidPortFallsBackToTheDefault() {
        Properties in = new Properties();
        in.setProperty("dbtype", "mysql");
        in.setProperty("mysql_port", "abc");
        assertEquals("3306", DatabaseSettings.parse(in, dataFolder).getPort());
    }
}
```

- [ ] **Step 2: Run it, expect FAIL** (`DatabaseSettings` does not exist)

Run: `mvn -B -q test -Dtest=DatabaseSettingsTest`

- [ ] **Step 3: Implement `DatabaseSettings`**

```java
package de.happybavarian07.adminpanel.main;

import de.happybavarian07.coolstufflib.jpa.utils.DatabaseProperties;

import java.io.File;
import java.util.Locale;
import java.util.Properties;

/** Turns database.properties into connection properties for CoolStuffLib. */
public final class DatabaseSettings {
    private DatabaseSettings() {
    }

    public static DatabaseProperties parse(Properties in, File dataFolder) {
        DatabaseProperties props = new DatabaseProperties();
        String driver = in.getProperty("dbtype", "").trim().toLowerCase(Locale.ROOT);
        props.setDatabasePrefix(in.getProperty("database_prefix", "").trim());
        if (driver.equals("mysql") || driver.equals("mariadb")) {
            props.setDriver(driver);
            props.setHost(in.getProperty("mysql_host", "localhost").trim());
            props.setPort(validPort(in.getProperty("mysql_port", "3306")));
            props.setUsername(in.getProperty("mysql_user", "root"));
            props.setPassword(in.getProperty("mysql_password", ""));
            props.setDatabase(in.getProperty("mysql_database", "adminpanel").trim());
            return props;
        }
        props.setDriver("sqlite");
        String path = in.getProperty("sqlite_file_path", "").trim();
        File file = new File(path.isEmpty() ? "AdminPanelDatabase.db" : path);
        if (!file.isAbsolute()) file = new File(dataFolder, file.getPath());
        props.setDatabase(file.getAbsolutePath());
        return props;
    }

    private static String validPort(String port) {
        try {
            int value = Integer.parseInt(port.trim());
            return value > 0 && value <= 65535 ? String.valueOf(value) : "3306";
        } catch (NumberFormatException e) {
            return "3306";
        }
    }
}
```

- [ ] **Step 4: Run the test, expect PASS**

- [ ] **Step 5: Use it in `AdminPanelMain`**

Replace `setupRepositoryController()` with:
```java
    public static final String DB_CONNECTION = "main";

    private void configureDatabase(CoolStuffLibBuilder builder) {
        saveResource("database.properties", false);
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(new File(getDataFolder(), "database.properties"))) {
            properties.load(in);
        } catch (IOException e) {
            fileLogger.writeToLog(Level.SEVERE, "Could not read database.properties, using SQLite defaults: " + e.getMessage(), LogPrefixExtension.ADMINPANEL_MAIN, true);
        }
        DatabaseProperties databaseProperties = DatabaseSettings.parse(properties, getDataFolder());
        fileLogger.writeToLog(Level.INFO, "Using Database: " + databaseProperties.getDriver() + " (" + databaseProperties.getDatabase() + ")", LogPrefixExtension.ADMINPANEL_MAIN, true);
        builder.withRepositoryManager()
                .setDatabasePrefix(databaseProperties.getDatabasePrefix())
                .addConnection(DB_CONNECTION, databaseProperties)
                .build();
    }
```
`saveResource(..., false)` logs a harmless warning when the file exists; wrap it in `if (!new File(getDataFolder(), "database.properties").exists())`.

In `onEnable`: replace `setupRepositoryController();` with `configureDatabase(coolStuffLibBuilder);`. Remove the field `repositoryController` and change the getter:
```java
    public RepositoryManager getRepositoryManager() {
        return coolStuffLib.getRepositoryManager();
    }
```
Delete `getRepositoryController()`. In `setupPermissions()` replace the `repositoryController.addRepositoryToRegistrationFile(...)` block with nothing (registration moves to Task 5's `AdminPanelData`). Move `setupPermissionsManager();` so it runs **after** `AdminPanelData` is created (Task 5, Step 5) — until then keep it where it is and register the permission repository right before it:
```java
    private void setupPermissionsManager() {
        getRepositoryManager().registerRepository(PlayerPermissionRepository.class, PlayerPermission.class);
        permissionsManager = new PermissionsManager(this, 5 * 60 * 20L);
    }
```
In `PermissionsManager` line 36: `plugin.getRepositoryController().getRepository(...)` → `plugin.getRepositoryManager().getRepository(...)`.

Imports in `AdminPanelMain`: remove `jpa.RepositoryController`; add `de.happybavarian07.coolstufflib.repository.RepositoryManager`.

- [ ] **Step 6: Build and commit**

Run: `mvn -B -q package -DskipTests` → exit 0.
```bash
git add src/main/java/de/happybavarian07/adminpanel/main src/main/java/de/happybavarian07/adminpanel/permissions/PermissionsManager.java src/test/java/de/happybavarian07/adminpanel/main/DatabaseSettingsTest.java
git commit -m "fix: read the MySQL keys from database.properties and use CoolStuffLib's repository manager"
```

---

### Task 5: JPA tables for AdminPanel's runtime data

**Files:**
- Create in `src/main/java/de/happybavarian07/adminpanel/data/`: `PluginFlag.java`, `PluginFlagRepository.java`, `PluginDescriptionEntry.java`, `PluginDescriptionRepository.java`, `UpdaterCacheEntry.java`, `UpdaterCacheRepository.java`, `AdminPanelData.java`
- Modify: `main/AdminPanelMain.java` (create `AdminPanelData` right after `coolStuffLib.setup()`, getter `getData()`)

**Interfaces:**
- Produces: `AdminPanelData#flags()`, `#descriptions()`, `#updaterCache()` returning the repositories; `AdminPanelData#getFlag(String name, boolean fallback)`, `#setFlag(String name, boolean value)`; entity getters/setters shown below. Table names (with the configured prefix): `plugin_flags(name PK, enabled)`, `plugin_descriptions(plugin_name PK, description, resource_id)`, `updater_cache(resource_id PK, latest_version, update_available, last_check_millis, message)`.

- [ ] **Step 1: Entities and repositories**

`PluginFlag.java`:
```java
package de.happybavarian07.adminpanel.data;

import de.happybavarian07.coolstufflib.jpa.annotations.Column;
import de.happybavarian07.coolstufflib.jpa.annotations.Entity;
import de.happybavarian07.coolstufflib.jpa.annotations.Id;
import de.happybavarian07.coolstufflib.jpa.annotations.Table;

@Entity
@Table(name = "plugin_flags")
public class PluginFlag {
    @Id
    @Column(name = "name", primaryKey = true)
    private String name;

    @Column(name = "enabled")
    private boolean enabled;

    public PluginFlag() {
    }

    public PluginFlag(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
```

`PluginFlagRepository.java`:
```java
package de.happybavarian07.adminpanel.data;

import de.happybavarian07.coolstufflib.jpa.repository.Repository;

public interface PluginFlagRepository extends Repository<PluginFlag, String> {
}
```

`PluginDescriptionEntry.java`:
```java
package de.happybavarian07.adminpanel.data;

import de.happybavarian07.coolstufflib.jpa.annotations.Column;
import de.happybavarian07.coolstufflib.jpa.annotations.Entity;
import de.happybavarian07.coolstufflib.jpa.annotations.Id;
import de.happybavarian07.coolstufflib.jpa.annotations.Table;

@Entity
@Table(name = "plugin_descriptions")
public class PluginDescriptionEntry {
    @Id
    @Column(name = "plugin_name", primaryKey = true)
    private String pluginName;

    @Column(name = "description")
    private String description;

    @Column(name = "resource_id")
    private int resourceId = -1;

    public PluginDescriptionEntry() {
    }

    public PluginDescriptionEntry(String pluginName, String description, int resourceId) {
        this.pluginName = pluginName;
        this.description = description;
        this.resourceId = resourceId;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getDescription() {
        return description;
    }

    public int getResourceId() {
        return resourceId;
    }
}
```

`PluginDescriptionRepository.java`:
```java
package de.happybavarian07.adminpanel.data;

import de.happybavarian07.coolstufflib.jpa.repository.Repository;

public interface PluginDescriptionRepository extends Repository<PluginDescriptionEntry, String> {
}
```

`UpdaterCacheEntry.java`:
```java
package de.happybavarian07.adminpanel.data;

import de.happybavarian07.coolstufflib.jpa.annotations.Column;
import de.happybavarian07.coolstufflib.jpa.annotations.Entity;
import de.happybavarian07.coolstufflib.jpa.annotations.Id;
import de.happybavarian07.coolstufflib.jpa.annotations.Table;

@Entity
@Table(name = "updater_cache")
public class UpdaterCacheEntry {
    @Id
    @Column(name = "resource_id", primaryKey = true)
    private int resourceId;

    @Column(name = "latest_version")
    private String latestVersion;

    @Column(name = "update_available")
    private boolean updateAvailable;

    @Column(name = "last_check_millis")
    private long lastCheckMillis;

    @Column(name = "message")
    private String message;

    public UpdaterCacheEntry() {
    }

    public UpdaterCacheEntry(int resourceId, String latestVersion, boolean updateAvailable, long lastCheckMillis, String message) {
        this.resourceId = resourceId;
        this.latestVersion = latestVersion;
        this.updateAvailable = updateAvailable;
        this.lastCheckMillis = lastCheckMillis;
        this.message = message;
    }

    public int getResourceId() {
        return resourceId;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public long getLastCheckMillis() {
        return lastCheckMillis;
    }

    public String getMessage() {
        return message;
    }
}
```

`UpdaterCacheRepository.java`:
```java
package de.happybavarian07.adminpanel.data;

import de.happybavarian07.coolstufflib.jpa.repository.Repository;

public interface UpdaterCacheRepository extends Repository<UpdaterCacheEntry, Integer> {
}
```

- [ ] **Step 2: `AdminPanelData`**

```java
package de.happybavarian07.adminpanel.data;

import de.happybavarian07.coolstufflib.repository.RepositoryManager;

/** AdminPanel's runtime data tables, registered on CoolStuffLib's repository manager. */
public final class AdminPanelData {
    private final PluginFlagRepository flags;
    private final PluginDescriptionRepository descriptions;
    private final UpdaterCacheRepository updaterCache;

    public AdminPanelData(RepositoryManager repositories) {
        this.flags = repositories.registerRepository(PluginFlagRepository.class, PluginFlag.class);
        this.descriptions = repositories.registerRepository(PluginDescriptionRepository.class, PluginDescriptionEntry.class);
        this.updaterCache = repositories.registerRepository(UpdaterCacheRepository.class, UpdaterCacheEntry.class);
    }

    public PluginFlagRepository flags() {
        return flags;
    }

    public PluginDescriptionRepository descriptions() {
        return descriptions;
    }

    public UpdaterCacheRepository updaterCache() {
        return updaterCache;
    }

    public boolean getFlag(String name, boolean fallback) {
        return flags.findById(name).map(PluginFlag::isEnabled).orElse(fallback);
    }

    public void setFlag(String name, boolean value) {
        flags.save(new PluginFlag(name, value));
    }
}
```

- [ ] **Step 3: Create it in `AdminPanelMain`**

Field `private AdminPanelData data;`, getter:
```java
    public AdminPanelData getData() {
        return data;
    }
```
Right after `coolStuffLib.setup();` add `data = new AdminPanelData(getRepositoryManager());`.

- [ ] **Step 4: Build and commit**

Run: `mvn -B -q package -DskipTests` → exit 0.
```bash
git add src/main/java/de/happybavarian07/adminpanel/data src/main/java/de/happybavarian07/adminpanel/main/AdminPanelMain.java
git commit -m "feat: JPA tables for plugin flags, plugin descriptions and the updater cache"
```

---

### Task 6: Plugin state flags from the database

**Files:**
- Modify: `utils/managers/PluginStateManager.java`, `main/AdminPanelMain.java` (`setupPluginStateManager` runs after `data` exists)

**Interfaces:**
- Consumes: `AdminPanelData#getFlag/setFlag` (Task 5).
- Produces: constructor `PluginStateManager(FileConfiguration config, AdminPanelData data, AdminPanelMain plugin)`; `saveAllState()` becomes a no-op kept for callers (every change is written immediately).

Today the manager is built before `data.yml` is loaded, so the maintenance/chat-mute flags were never read back from `data.yml` (the DataService copy was the only one that worked).

- [ ] **Step 1: Impact check** for `PluginStateManager` (upstream).

- [ ] **Step 2: Replace the DataService fields and constructors**

Remove the `dataYML` and `dataService` fields, both constructors, `setDataService`, `loadConfigValues`, `loadFromDataService` and `persistFlags`, and add:
```java
    static final String MAINTENANCE = "maintenance";
    static final String GLOBAL_CHAT_MUTED = "globalChatMuted";
    private final AdminPanelData data;

    public PluginStateManager(FileConfiguration config, AdminPanelData data, AdminPanelMain plugin) {
        this.config = config;
        this.data = data;
        this.plugin = plugin;
        inMaintenanceMode = data.getFlag(MAINTENANCE, false);
        globalChatMuted = data.getFlag(GLOBAL_CHAT_MUTED, false);
    }

    public void saveAllState() {
    }
```
In `setInMaintenanceMode` replace `persistFlags();` with `data.setFlag(MAINTENANCE, inMaintenanceMode);`, in `setGlobalChatMuted` with `data.setFlag(GLOBAL_CHAT_MUTED, globalChatMuted);`. Import `de.happybavarian07.adminpanel.data.AdminPanelData`; remove the `DataService` import.

- [ ] **Step 3: Construct it after the data tables**

`setupPluginStateManager()` becomes `pluginStateManager = new PluginStateManager(getConfig(), data, this);`. In `onEnable`, move the `setupPluginStateManager();` call to directly after `data = new AdminPanelData(...)`. Check with `grep -n "pluginStateManager\|getPluginStateManager" -r src/main/java` that nothing between the old and the new position uses it (in the current file: nothing does; `registerBungeeChannels` comes later).

- [ ] **Step 4: Build and commit**

```bash
mvn -B -q package -DskipTests
git add src/main/java/de/happybavarian07/adminpanel/utils/managers/PluginStateManager.java src/main/java/de/happybavarian07/adminpanel/main/AdminPanelMain.java
git commit -m "fix: store maintenance and chat-mute flags in the database so they survive a restart"
```

---

### Task 7: Plugin descriptions and updater cache from the database

**Files:**
- Modify: `utils/managers/PluginDescriptionManager.java`, `utils/NewUpdater.java`, `utils/managers/AutoUpdaterManager.java`

**Interfaces:**
- Consumes: `AdminPanelData#descriptions()`, `#updaterCache()` (Task 5).

`plugin_descriptions.yml` stays as the shipped list of defaults (it is a resource file); the table holds what the server added or changed and wins over the file. The updater cache is stored per `resourceID` (the old DataService keys were shared by every updater instance, so one plugin's version overwrote another's) and is only trusted for one hour, because the old code returned a cached version forever.

- [ ] **Step 1: Impact check** for `PluginDescriptionManager`, `NewUpdater`, `AutoUpdaterManager.setup`.

- [ ] **Step 2: PluginDescriptionManager**

Replace the DataService block in the constructor with:
```java
        for (PluginDescriptionEntry entry : plugin.getData().descriptions().findAll()) {
            pluginDescriptionMap.put(entry.getPluginName(), entry.getDescription());
        }
```
In `removePluginDescription(String)` replace the DataService block with `AdminPanelMain.getPlugin().getData().descriptions().deleteById(pluginName);`.
In `addPluginDescription(String, String, int)` replace the DataService block with:
```java
        AdminPanelMain.getPlugin().getData().descriptions()
                .save(new PluginDescriptionEntry(pluginName, pluginDescriptionMap.get(pluginName), resourceID));
```
`PluginDescriptionManager` is created at the start of `onEnable` (before the data tables): move `pluginDescriptionManager = new PluginDescriptionManager();` to right after `data = new AdminPanelData(...)` and check with grep that nothing uses it earlier. Imports: `de.happybavarian07.adminpanel.data.PluginDescriptionEntry`; remove `DataService`.

- [ ] **Step 3: NewUpdater**

Add `private static final long CACHE_TTL_MILLIS = 60 * 60 * 1000L;`. Replace the DataService block in the constructor with:
```java
        try {
            plugin.getData().updaterCache().findById(resourceID)
                    .filter(entry -> System.currentTimeMillis() - entry.getLastCheckMillis() < CACHE_TTL_MILLIS)
                    .ifPresent(entry -> {
                        cachedLatestVersionName = entry.getLatestVersion();
                        cachedUpdateAvailable = entry.isUpdateAvailable();
                        lastUpdateCheckMillis = entry.getLastCheckMillis();
                        cachedUpdateMessage = entry.getMessage();
                    });
        } catch (RuntimeException e) {
            plugin.getFileLogger().writeToLog(Level.WARNING, "Could not read the updater cache: " + e.getMessage(), LogPrefixExtension.UPDATER);
        }
```
Add a helper and use it at both save sites (after the latest version is fetched, and after an update check):
```java
    private void saveCache() {
        try {
            plugin.getData().updaterCache().save(new UpdaterCacheEntry(resourceID, cachedLatestVersionName,
                    Boolean.TRUE.equals(cachedUpdateAvailable), lastUpdateCheckMillis, cachedUpdateMessage));
        } catch (RuntimeException e) {
            plugin.getFileLogger().writeToLog(Level.WARNING, "Could not save the updater cache: " + e.getMessage(), LogPrefixExtension.UPDATER);
        }
    }
```
The first site (`getLatestVersionName`, today `ds.save("updater.cachedLatestVersionName", version)`) becomes `saveCache();` after `cachedLatestVersionName = version;`. The second site (the block that saves four keys) becomes `cachedUpdateMessage = finalMessageToCache;` (move the existing assignment from below up, if it is below) followed by `saveCache();`. Imports: `de.happybavarian07.adminpanel.data.UpdaterCacheEntry`; remove `DataService`.

- [ ] **Step 4: AutoUpdaterManager**

Delete the `DataService ds = null; try { ds = ...getDataService(); } catch ...` lines at the top of `setup` and the "Persist small metadata about this plugin updater" block (the per-resource cache in `NewUpdater` already stores it; nothing read `updater.meta.*`). Remove the `DataService` import and now-unused `HashMap`/`Map` imports.

- [ ] **Step 5: Build and commit**

```bash
mvn -B -q package -DskipTests
git add src/main/java/de/happybavarian07/adminpanel/utils
git commit -m "fix: keep plugin descriptions and a per-plugin updater cache in the database"
```

---

### Task 8: Per-player languages through CoolStuffLib's handler

**Files:**
- Delete: `utils/managers/AdminPanelPerPlayerLanguageHandler.java`
- Modify: `main/AdminPanelMain.java` (`setupLanguageManager`, `setupLanguageHandler`, `onDisable`)

CoolStuffLib's `PerPlayerLanguageHandler` already stores player languages in the `PlayerLanguages` section of `data.yml`. AdminPanel's subclass stored a second copy in the DataService. The handler must share AdminPanel's `dataYML` object, because `AutoUpdaterManager` and `AdminPanelUtils` also save that file and two in-memory copies would overwrite each other.

- [ ] **Step 1: Impact check** for `AdminPanelPerPlayerLanguageHandler`.

- [ ] **Step 2: Use the library handler**

In `setupLanguageManager`, remove `.enablePlayerLanguageHandler()` (it would create a second in-memory copy of `data.yml`). `setupLanguageHandler()` becomes:
```java
    private void setupLanguageHandler() {
        File dataFile = new File(getDataFolder(), "data.yml");
        dataYML = YamlConfiguration.loadConfiguration(dataFile);
        languageManager.setPLHandler(new PerPlayerLanguageHandler(languageManager, dataFile, dataYML));
    }
```
In `onDisable`, delete the `flushSync` try/catch. Import `de.happybavarian07.coolstufflib.languagemanager.PerPlayerLanguageHandler`. Delete the old class:
```bash
git rm -q src/main/java/de/happybavarian07/adminpanel/utils/managers/AdminPanelPerPlayerLanguageHandler.java
```
Search for other users: `grep -rn "AdminPanelPerPlayerLanguageHandler" src/main/java` must print nothing.

- [ ] **Step 3: Build and commit**

```bash
mvn -B -q package -DskipTests
git add -A src/main/java
git commit -m "refactor: store per-player languages only through CoolStuffLib's handler in data.yml"
```

---

### Task 9: Import the old DataService data once

**Files:**
- Create: `src/main/java/de/happybavarian07/adminpanel/data/LegacyDataImporter.java`
- Create: `src/test/java/de/happybavarian07/adminpanel/data/LegacyDataImporterTest.java`
- Modify: `main/AdminPanelMain.java` (run the import after the language handler and the data tables exist)

**Interfaces:**
- Produces: `LegacyDataImporter.read(File sqliteFile)` → `LegacyData` record `(Map<String, String> playerLanguages, Map<String, String> pluginDescriptions, Map<String, Boolean> flags)`; `LegacyDataImporter.findLegacyFile(File dataFolder)` → the first existing of `<dataFolder>/plugin_data.db` and `<server root>/plugin_data.db`, or null.

The old SQLite file has one table `data(id TEXT PRIMARY KEY, value TEXT)`; values are Gson JSON. Keys: `language.players` (map uuid → language), `plugin.descriptions` (map name → description), `plugin.state.maintenance`, `plugin.state.globalChatMuted` (booleans). `updater.*` is a cache and is skipped.

- [ ] **Step 1: Write the failing test**

```java
package de.happybavarian07.adminpanel.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyDataImporterTest {
    @TempDir
    Path dir;

    @Test
    void readsLanguagesDescriptionsAndFlags() throws Exception {
        File db = dir.resolve("plugin_data.db").toFile();
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath());
             Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE data (id TEXT PRIMARY KEY, value TEXT)");
            try (PreparedStatement p = c.prepareStatement("INSERT INTO data VALUES (?, ?)")) {
                insert(p, "language.players", "{\"6f1c0b1e-0000-0000-0000-000000000001\":\"de\"}");
                insert(p, "plugin.descriptions", "{\"Essentials\":\"Basics\"}");
                insert(p, "plugin.state.maintenance", "true");
                insert(p, "plugin.state.globalChatMuted", "false");
                insert(p, "updater.cachedLatestVersionName", "\"9.9.9\"");
            }
        }

        LegacyDataImporter.LegacyData data = LegacyDataImporter.read(db);

        assertEquals(Map.of("6f1c0b1e-0000-0000-0000-000000000001", "de"), data.playerLanguages());
        assertEquals(Map.of("Essentials", "Basics"), data.pluginDescriptions());
        assertEquals(Map.of("maintenance", true, "globalChatMuted", false), data.flags());
    }

    @Test
    void aMissingTableGivesEmptyData() throws Exception {
        File db = dir.resolve("empty.db").toFile();
        DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath()).close();
        LegacyDataImporter.LegacyData data = LegacyDataImporter.read(db);
        assertTrue(data.playerLanguages().isEmpty() && data.pluginDescriptions().isEmpty() && data.flags().isEmpty());
    }

    private static void insert(PreparedStatement p, String key, String json) throws Exception {
        p.setString(1, key);
        p.setString(2, json);
        p.executeUpdate();
    }
}
```

- [ ] **Step 2: Run it, expect FAIL** (`LegacyDataImporter` does not exist)

Run: `mvn -B -q test -Dtest=LegacyDataImporterTest`

- [ ] **Step 3: Implement**

```java
package de.happybavarian07.adminpanel.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/** Reads what the removed DataService stored in plugin_data.db, so it can be moved to the new tables once. */
public final class LegacyDataImporter {
    private static final Gson GSON = new Gson();
    private static final String STATE_PREFIX = "plugin.state.";

    private LegacyDataImporter() {
    }

    public record LegacyData(Map<String, String> playerLanguages, Map<String, String> pluginDescriptions,
                             Map<String, Boolean> flags) {
    }

    /** The old file was created relative to the server folder; check the plugin folder first. */
    public static File findLegacyFile(File dataFolder) {
        File inPluginFolder = new File(dataFolder, "plugin_data.db");
        if (inPluginFolder.isFile()) return inPluginFolder;
        File serverRoot = dataFolder.getAbsoluteFile().getParentFile().getParentFile();
        File inServerRoot = new File(serverRoot, "plugin_data.db");
        return inServerRoot.isFile() ? inServerRoot : null;
    }

    public static LegacyData read(File sqliteFile) throws SQLException {
        Map<String, String> languages = new HashMap<>();
        Map<String, String> descriptions = new HashMap<>();
        Map<String, Boolean> flags = new HashMap<>();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile.getAbsolutePath());
             Statement statement = connection.createStatement()) {
            ResultSet tables = connection.getMetaData().getTables(null, null, "data", null);
            if (!tables.next()) return new LegacyData(languages, descriptions, flags);
            try (ResultSet rows = statement.executeQuery("SELECT id, value FROM data")) {
                while (rows.next()) {
                    String key = rows.getString(1);
                    String json = rows.getString(2);
                    if (key == null || json == null || json.isEmpty()) continue;
                    if (key.equals("language.players")) {
                        languages.putAll(GSON.fromJson(json, new TypeToken<Map<String, String>>() {}.getType()));
                    } else if (key.equals("plugin.descriptions")) {
                        descriptions.putAll(GSON.fromJson(json, new TypeToken<Map<String, String>>() {}.getType()));
                    } else if (key.startsWith(STATE_PREFIX)) {
                        flags.put(key.substring(STATE_PREFIX.length()), GSON.fromJson(json, Boolean.class));
                    }
                }
            }
        }
        return new LegacyData(languages, descriptions, flags);
    }
}
```

- [ ] **Step 4: Run the test, expect PASS**

- [ ] **Step 5: Apply it on startup**

The tables and flags must be imported before `PluginStateManager` and `PluginDescriptionManager` read them (both are created right after the data tables, Tasks 6–7, because `registerBungeeChannels()` needs the state manager early). The languages need the language handler, which is created later. So the file is read once, in two steps.

Field: `private LegacyDataImporter.LegacyData legacyData;` and `private File legacyFile;`.

Directly after `data = new AdminPanelData(getRepositoryManager());` and **before** `setupPluginStateManager();`:
```java
        importLegacyTables();
```
Directly after `setupLanguageHandler();`:
```java
        importLegacyLanguages();
```
Methods:
```java
    private void importLegacyTables() {
        legacyFile = LegacyDataImporter.findLegacyFile(getDataFolder());
        if (legacyFile == null) return;
        try {
            legacyData = LegacyDataImporter.read(legacyFile);
        } catch (SQLException e) {
            fileLogger.writeToLog(Level.SEVERE, "Could not import " + legacyFile + ": " + e.getMessage(), LogPrefixExtension.ADMINPANEL_MAIN, true);
            legacyFile = null;
            return;
        }
        legacyData.pluginDescriptions().forEach((name, description) ->
                data.descriptions().save(new PluginDescriptionEntry(name, description, -1)));
        legacyData.flags().forEach(data::setFlag);
    }

    private void importLegacyLanguages() {
        if (legacyData == null) return;
        legacyData.playerLanguages().forEach((uuid, language) -> {
            try {
                if (languageManager.getLang(language, false) != null) {
                    languageManager.getPLHandler().setPlayerLanguage(UUID.fromString(uuid), language);
                }
            } catch (IllegalArgumentException ignored) {
                // not a UUID, skip the entry
            }
        });
        File done = new File(legacyFile.getParentFile(), legacyFile.getName() + ".imported");
        if (!legacyFile.renameTo(done)) {
            fileLogger.writeToLog(Level.WARNING, "Imported " + legacyFile + " but could not rename it; it will be imported again next start",
                    LogPrefixExtension.ADMINPANEL_MAIN, true);
        }
        fileLogger.writeToLog(Level.INFO, "Imported old data from " + legacyFile + ": " + legacyData.playerLanguages().size()
                + " player languages, " + legacyData.pluginDescriptions().size() + " plugin descriptions, "
                + legacyData.flags().size() + " flags", LogPrefixExtension.ADMINPANEL_MAIN, true);
        legacyData = null;
    }
```
Imports: `de.happybavarian07.adminpanel.data.LegacyDataImporter`, `de.happybavarian07.adminpanel.data.PluginDescriptionEntry`, `java.sql.SQLException`.

- [ ] **Step 6: Build and commit**

```bash
mvn -B -q package
git add src/main/java/de/happybavarian07/adminpanel/data/LegacyDataImporter.java src/test/java/de/happybavarian07/adminpanel/data/LegacyDataImporterTest.java src/main/java/de/happybavarian07/adminpanel/main/AdminPanelMain.java
git commit -m "feat: import the old DataService data once from plugin_data.db"
```

---

### Task 10: Remove the DataService (A7, A8)

**Files:**
- Delete: `src/main/java/de/happybavarian07/adminpanel/service/` (api + impl), `src/test/java/de/happybavarian07/adminpanel/service/`, `utils/Serialization.java` if nothing else uses it
- Modify: `main/AdminPanelMain.java` (fields `serviceRegistry`, `dataServiceUUID`, `setupServiceRegistry`, `getDataService`), `menusystem/menu/ConfigReloadMenu.java` (ReloadData button)

- [ ] **Step 1: Impact check** for `DataService`, `getDataService`.

- [ ] **Step 2: Remove the wiring**

In `AdminPanelMain`: delete `setupServiceRegistry()` and its call, the fields `serviceRegistry` and `dataServiceUUID`, `getDataService()` and the imports of `service.*`, `ServiceRegistry`, `DefaultServiceRegistry`. `grep -rn "getDataService\|service.api\|service.impl" src/main/java` must print nothing afterwards.

- [ ] **Step 3: ConfigReloadMenu "Reload Data"**

The button reloaded the DataService. It now reloads `data.yml` into the shared object (so edits made by hand while the server runs are picked up):
```java
            (p, e) -> {
                AdminPanelMain plugin = AdminPanelMain.getPlugin();
                try {
                    plugin.getDataYML().load(new File(plugin.getDataFolder(), "data.yml"));
                    p.sendMessage(lgm.getMessage("ConfigReloadMenu.DataReloaded", p, true));
                } catch (IOException | InvalidConfigurationException ex) {
                    plugin.getFileLogger().writeToLog(Level.SEVERE, "Could not reload data.yml: " + ex.getMessage(), LogPrefixExtension.ADMINPANEL_MAIN, true);
                }
                this.open();
            }
```
Imports: `java.io.File`, `java.io.IOException`, `org.bukkit.configuration.InvalidConfigurationException`, `java.util.logging.Level`, `de.happybavarian07.adminpanel.utils.LogPrefixExtension` (skip those already present).

- [ ] **Step 4: Delete the package and its tests, build with tests**

```bash
git rm -r -q src/main/java/de/happybavarian07/adminpanel/service src/test/java/de/happybavarian07/adminpanel/service
grep -rn "Serialization\." src/main/java || git rm -q src/main/java/de/happybavarian07/adminpanel/utils/Serialization.java
mvn -B -q package
```
Expected: exit 0, tests pass.

- [ ] **Step 5: Commit**

```bash
git add -A src
git commit -m "refactor: remove the DataService and its second service registry"
```

---

### Task 11: Startup and shutdown robustness (A5, A6, A10)

**Files:**
- Modify: `main/AdminPanelMain.java` (`onLoad` addon loop, `onDisable`, `getDataClient`/`getDataClientUtils`)

- [ ] **Step 1: A5 — one broken on-load addon must not stop the plugin**

Replace the body of `onLoadSection.getKeys(false).forEach(addon -> { ... })` with:
```java
                onLoadSection.getKeys(false).forEach(addon -> {
                    String path = "OnLoad." + addon + ".";
                    File addonFile = new File(this.getDataFolder() + "/addons/" + tempConfig.getString(path + "FileName"));
                    String addonName = tempConfig.getString(path + "Name");
                    try {
                        loader.loadAddon(addonFile);
                        loader.enableAddon(addonFile, new HashSet<>(), true);
                        logger.coloredMessage(ChatColor.GREEN, "Loaded Addon: " + addonName);
                    } catch (IOException | ClassNotFoundException | RuntimeException e) {
                        fileLogger.writeToLog(Level.SEVERE, "Could not load addon " + addonName + " (" + addonFile.getName() + "): " + e,
                                LogPrefixExtension.ADMINPANEL_MAIN, true);
                    }
                });
```

- [ ] **Step 2: A6 — null-safe shutdown of the sync channels**

In `onDisable`, replace the bungee block with:
```java
        if (pluginStateManager != null && pluginStateManager.checkIfBungee(false)) {
            if (bungeeUtils != null) bungeeUtils.closeBungeeChannel();
            if (dataClient != null) {
                dataClient.disconnect(true);
                dataClient.getStatsManager().saveStatsToFile();
            }
        }
```

- [ ] **Step 3: A10 — plain getters**

`getDataClient()` and `getDataClientUtils()` become `return dataClient;` / `return dataClientUtils;` (the `try/catch (CommandException)` around a field read cannot trigger). Remove the `CommandException` import if unused.

- [ ] **Step 4: Build and commit**

```bash
mvn -B -q package -DskipTests
git add src/main/java/de/happybavarian07/adminpanel/main/AdminPanelMain.java
git commit -m "fix: keep loading when an on-load addon fails and shut down sync channels null-safely"
```

---

### Task 12: Smoke test on the server, audit and docs

**Files:**
- Create: `tools/run-adminpanel-smoke.sh`
- Modify: `.claude/issues/AUDIT.md` (move A1, A2, A5, A6, A7, A8, A10, A12, A19 to "Already fixed"; add the updater-cache and plugin-state findings as fixed), `docs/` pages that mention the DataService or `plugin_data.db` (`grep -rln "DataService\|plugin_data" docs README.md`)

- [ ] **Step 1: Write the smoke script**

```bash
#!/usr/bin/env bash
# Builds AdminPanel, starts the 1.21.10 test server, reloads once, stops it and prints AdminPanel errors.
set -uo pipefail
SERVER="${AP_SERVER:-F:/Weitere Minecraft Test Server/Spigot 1.21.10}"
JAVA="${AP_JAVA:-F:/Weitere Minecraft Test Server/jdk-21.0.5/bin/java}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG="$SERVER/adminpanel-smoke.log"
CMDS="$SERVER/adminpanel-smoke-cmds.txt"

mvn -B -q -f "$ROOT/pom.xml" clean package -DskipTests || { echo "Build failed"; exit 1; }
VERSION=$(grep -m1 -oP '(?<=<version>)[^<]+' "$ROOT/pom.xml")
cp "$ROOT/target/Admin-Panel-$VERSION.jar" "$SERVER/plugins/Admin-Panel-$VERSION.jar"

wait_for() { for _ in $(seq 1 "$3"); do [ "$(grep -c -- "$1" "$LOG" 2>/dev/null || true)" -ge "$2" ] && return 0; sleep 1; done; echo "Timed out waiting for '$1'"; return 1; }

: > "$CMDS"; : > "$LOG"
(cd "$SERVER" && tail -f "$CMDS" | "$JAVA" -Xms1G -Xmx2G -jar spigot-1.21.10.jar nogui > "$LOG" 2>&1) &
status=0
wait_for "Done (" 1 180 || status=1
echo "reload confirm" >> "$CMDS"
wait_for "Enabling Admin-Panel" 2 180 || status=1
sleep 5
echo "stop" >> "$CMDS"
wait_for "All dimensions are saved" 1 120 || status=1
sleep 2; echo "" >> "$CMDS"

echo "===== AdminPanel lines ====="
grep -E "\[Admin-Panel\].*(Using Database|Imported old data|Could not|Error|Exception)" "$LOG"
if grep -E -A3 "(Error occurred while enabling|Could not load) .*Admin-Panel|Exception.*adminpanel" "$LOG" | grep -q .; then
    echo "AdminPanel reported errors (see above)"; status=1
fi
[ -f "$SERVER/plugin_data.db" ] && { echo "plugin_data.db still in the server root"; status=1; }
exit $status
```

- [ ] **Step 2: Run it**

Run: `bash tools/run-adminpanel-smoke.sh`
Expected: exit 0; the log shows `Using Database: sqlite (...plugins/Admin-Panel/AdminPanelDatabase.db)` and, on the first run with an old `plugin_data.db` in the server root, `Imported old data from ...`; afterwards `plugin_data.db.imported` exists and no new `plugin_data.db` is created. Check the tables:
```bash
python -c "import sqlite3;c=sqlite3.connect(r'F:\Weitere Minecraft Test Server\Spigot 1.21.10\plugins\Admin-Panel\AdminPanelDatabase.db');print([r[0] for r in c.execute(\"select name from sqlite_master where type='table'\")])"
```
Expected: contains `plugin_flags`, `plugin_descriptions`, `updater_cache` and the existing permission table.

- [ ] **Step 3: Manual check (with the user)**

Start the server, join, run `/adminpanel` → toggle maintenance mode in the server manager, restart, confirm it is still on. Switch the language in the start menu, `/reload confirm`, confirm it is kept.

- [ ] **Step 4: Update the audit and docs, commit**

```bash
git add tools/run-adminpanel-smoke.sh .claude/issues/AUDIT.md docs
git commit -m "chore: smoke test script and audit update for the foundation migration"
```

---

## Self-Review Notes

- **Decisions covered:** DataService → JPA tables + import (Tasks 5–10), CoolStuffLib StartUpLogger/BackupManager (Tasks 2–3), commands untouched here (Plan 2).
- **Audit items covered:** A1/A2/A12 (Task 4), A5/A6/A10 (Task 11), A7/A8 (Task 10), A19 (Task 2), A20 (rebuilt against the fixed CoolStuffLib throughout). A3/A4 disappear with the DataService.
- **New findings fixed on the way:** shared updater cache keys and a cache that never expired (Task 7); plugin state read from `data.yml` before it was loaded (Task 6); `dbtype=mariadb` unsupported by CoolStuffLib (Task 1); two in-memory copies of `data.yml` avoided (Task 8).
- **Order constraints in `onEnable` after this plan:** builder (backup, logging, database) → `coolStuffLib.setup()` → `data = new AdminPanelData(...)` → `importLegacyTables()` → `setupPluginStateManager()` → `pluginDescriptionManager = new PluginDescriptionManager()` → … → `registerBungeeChannels()` (uses `pluginStateManager`) → … → `setupLanguageHandler()` → `importLegacyLanguages()`.
