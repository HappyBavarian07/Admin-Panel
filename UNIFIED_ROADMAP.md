# AdminPanel Unified Roadmap

**Last Updated:** 2025-11-23  
**Current Version:** 9.0.0

This roadmap consolidates all features, TODOs, and implementation plans for the AdminPanel plugin. It replaces all
previous roadmap files and serves as the single source of truth for development priorities.

---

## Summary of Completed Work

### ✅ Data Service API (Complete)

- [x] `service/api` interfaces (DataService, DataServiceType, DataServiceConfig)
- [x] `FileDataService` implementation (YAML-backed with JSON serialization)
- [x] `SQLiteDataService` implementation (key-value table with JSON strings)
- [x] `DataServiceMigrationUtil` with batched migration support
- [x] Typed generic CRUD API (`<T> save/load/saveAll/loadAll`)
- [x] ServiceFactory and registry integration
- [x] Unit and integration tests (6 passing tests)
- [x] Migration tests (File↔SQLite bidirectional)

### ✅ Pattern Generation System (Complete)

- [x] `PatternGenerator` with pluggable `TokenAnalyzer` interface
- [x] Built-in analyzers: number, UUID, word detection
- [x] `PatternRunner` auto-generates missing patterns
- [x] Pattern validation integrated into `PatternValidator`
- [x] Configurable token analysis for custom data formats

### ✅ Test Suite Improvements

- [x] Fixed PaginatedList tests (16 passing)
- [x] Fixed VersionComparator tests (7 passing)
- [x] Fixed UpdaterVersionChecker spam output
- [x] DataService tests passing (6 tests)
- [x] Total: 32 passing tests, 1 remaining error (ExpressionEngine)

### ✅ Permission System

- [x] PermissionsManager using JPA repositories (kept intentionally)
- [x] Full permission hierarchy in plugin.yml
- [x] Language migration permissions added

---

## High Priority (Next Update - 9.1.0)

### 1. Language Migration UI ✅ COMPLETE

**Status:** Fully implemented and functional

#### Completed

- [x] `LanguageMigrationMenu` base structure
- [x] `LanguageMigratorMainMenu` scaffolding
- [x] `MigrationEntryEditMenu` fully implemented
- [x] Status filtering (ALL, CHANGED_ONLY, UNCHANGED_ONLY, MISSING_ONLY)
- [x] Permission nodes in plugin.yml
- [x] Chat-based value editing with `AsyncPlayerChatEvent`
- [x] Chat input validation and error handling (supports int, double, boolean, list, string)
- [x] Filter menu item click wiring to cycle through modes
- [x] Cancel support for chat editing
- [x] All TODOs completed and removed

**Implementation Complete:** 2025-11-23

### 2. Per-Player Language Persistence ✅ COMPLETE

**Status:** Already implemented in codebase

#### Implementation Details

- [x] `AdminPanelPerPlayerLanguageHandler` exists in `utils/managers/`
- [x] Extends CoolStuffLib's `PerPlayerLanguageHandler`
- [x] Wired into `LanguageManager.setPLHandler()` at startup (line 582 of AdminPanelMain)
- [x] Uses DataService for persistence (`language.players` key)
- [x] Stores as `Map<String, String>` with UUID→language mappings
- [x] Loads existing data from DataService on startup
- [x] Async persistence on every language change
- [x] Synchronous flush support via `flushSync()`

**Already Complete:** Pre-existing implementation

### 3. Config Reload Menu ✅ COMPLETE

**Status:** Fully implemented

**Location:** `menusystem/menu/ConfigReloadMenu.java`

#### Implemented Features

- [x] Full menu layout with 27 slots
- [x] Reload Config button (calls `reloadConfig()`)
- [x] Reload Languages button (calls `lgm.reloadLanguages()`)
- [x] Reload Data button (calls `DataService.reload()`)
- [x] Backup All button (calls `BackupManager.backupAllFileBackups()`)
- [x] Reload Plugin button (disable + enable)
- [x] Permission check: `AdminPanel.ReloadConfig`
- [x] Localized messages for all actions
- [x] Filler glass for visual polish

**Implementation Complete:** 2025-11-23

---

## Medium Priority (Version 9.2.0)

### 4. DataService Enhancements ✅ CORE COMPLETE

**Status:** Essential features implemented, advanced features deferred

#### Health Checks and Monitoring ✅ COMPLETE

- [x] Add `isHealthy()` method to DataService interface (already existed)
- [x] Add `getHealthStatus()` method for detailed status
- [x] Implement health checks for File (disk access validation)
- [x] Implement health checks for SQLite (connection test)
- [ ] Add periodic health monitoring in ServiceRegistry (deferred to 9.2.0)
- [ ] Log warnings when DataService becomes unhealthy (deferred to 9.2.0)
- [ ] Add admin notification on health degradation (deferred to 9.2.0)

#### Robustness Improvements ✅ ESSENTIAL COMPLETE

- [x] FileDataService: atomic disk writes (already implemented - temp file + rename)
- [ ] FileDataService: basic file locking for concurrent access (deferred - platform-specific)
- [ ] FileDataService: YAML parse error recovery with backup restore (deferred - complex)
- [ ] SQLiteDataService: connection pooling for concurrent requests (deferred - single connection sufficient)
- [x] SQLiteDataService: transaction batching for saveAll operations (already implemented)

#### API Additions ✅ COMPLETE

- [x] `reload()` method exists and implemented
- [x] `backup(String backupName)` method with timestamped backups
- [x] `listKeys(String prefix)` for filtered key listing
- [x] `deleteAll(List<String> keys)` for batch deletion
- [ ] Lifecycle events: init, ready, fail, reload (deferred - needs event bus)

**Implementation Complete:** 2025-11-23  
**Deferred Features:** Advanced concurrency, ServiceRegistry integration, lifecycle events

### 5. Migration Command and UI

**Status:** Not started

#### Tasks

- [ ] Create `/adminpanel migrate` command with subcommands
- [ ] Subcommands: `start <source> <target>`, `status`, `cancel`, `resume`
- [ ] Add progress tracking (keys migrated, percentage complete)
- [ ] Add progress bar in admin menu
- [ ] Support selective migration (key prefix filter)
- [ ] Add dry-run mode to preview changes
- [ ] Log migration events for audit trail

**Estimated Time:** 2 days

### 6. Expression Engine Test Fixes ✅ COMPLETE

**Status:** All tests passing

**Outcome:** Test suite now passes cleanly (BUILD SUCCESS)

**Analysis:**

- Test was already correctly implemented
- Issue resolved during compilation and dependency fixes
- All LanguageFunctionManager tests passing (6/6)

**Resolution Complete:** 2025-11-23

---

## Low Priority (Future Versions)

### 7. JPA-Based DataService (Optional)

**Status:** Deferred

Currently not needed; FileDataService and SQLiteDataService cover most use cases. If complex queries or relational data
is required, implement:

- [ ] Design schema for generic key-value with metadata
- [ ] Create JPA repositories for DataService operations
- [ ] Implement `JPADataService` with query support
- [ ] Add migration path from File/SQLite to JPA
- [ ] Consider migrating PermissionsManager to use DataService pattern

**Estimated Time:** 3-5 days (when needed)

### 8. TFIDFSearch Index Persistence

**Status:** Planned, not urgent

**Location:** CoolStuffLib integration

#### Current State

- Lucene index is in-memory, rebuilt on startup
- No persistence of index data

#### Tasks (when implemented)

- [ ] Store TFIDFSearch index metadata in DataService
- [ ] Save indexed document IDs and timestamps
- [ ] Implement incremental index updates
- [ ] Add index rebuild command for admins
- [ ] Test with large plugin description datasets

**Estimated Time:** 1-2 days

### 9. AutoUpdaterManager Plugin List Persistence

**Status:** Not started

**Location:** `utils/AutoUpdaterManager.java`

#### Tasks

- [ ] Store `autoUpdaterPlugins` Map in DataService
- [ ] Persist plugin update check results
- [ ] Add admin UI to manage auto-update settings per plugin
- [ ] Add bulk enable/disable for auto-updates
- [ ] Persist update notification preferences

**Estimated Time:** 1 day

### 10. Warning System (Planned Feature)

**Status:** Deferred per user request

**Location:** `utils/WarningManager.java`

**TODO:** Line 96 - Edit methods (setReason, setExpirationDate, etc.)

This feature is planned but not being implemented in the near term. Mark as frozen until user request.

### 11. Item Disable System (Planned Feature)

**Status:** Partially implemented, frozen

**Locations:**

- `commands/subcommands/disableditems/ListCommand.java`
- `commands/subcommands/disableditems/EnableCommand.java`
- `commands/subcommands/disableditems/DisableCommand.java`

**TODOs:** Multiple "Finish" markers

This feature is planned but not being implemented in the near term. Mark as frozen until user request.

---

## Technical Debt and Maintenance

### Code TODOs (Non-Critical)

#### Already Documented (Keep as-is)

- `LiteBansHook.java` line 23: Integration planned but complex
- `DependencyManager.java` line 43: Dependencies not loading correctly (investigate)
- `TrollsSyncCommand.java` lines 27, 38: Player-specific troll data sync
- `BungeeUtils.java` line 63: Enhanced BungeeCord logging
- `PermissionsManager.java` line 33: Player groups system (future enhancement)
- `PermissionActionMenu.java` line 116: Op permission removal issue
- `FileCorruptionManager.java` line 193: Document backup check disable option

### Test Coverage Improvements

- [ ] Add tests for LanguageMigrationMenu workflow
- [ ] Add tests for PatternGenerator edge cases (empty files, binary files)
- [ ] Add tests for DataService concurrent access
- [ ] Add integration tests for migration with large datasets
- [ ] Add tests for permission system edge cases

### Documentation Updates

- [ ] Expand `service-api-tutorial.md` with typed examples
- [ ] Add migration recipes and common patterns
- [ ] Document PatternGenerator analyzer API
- [ ] Add admin guide for language migration workflow
- [ ] Document per-player language storage format

---

## Release Plan

### Version 9.1.0 (Next Release)

**Target Date:** TBD  
**Focus:** Language Migration & Per-Player Language Persistence

#### Must-Have

- ✅ Pattern generation system (complete)
- ✅ DataService tests passing (complete)
- ⚠️ Language Migration UI completion
- ⚠️ Per-player language persistence
- ⚠️ Config Reload Menu
- ⚠️ Expression Engine test fix

#### Nice-to-Have

- DataService health checks
- Migration progress tracking
- Atomic file writes for FileDataService

#### Release Criteria

- All tests passing (32+ tests)
- Language migration workflow tested end-to-end
- Per-player language data migrated successfully
- No regressions in existing features
- Updated documentation

### Version 9.2.0 (Future)

**Focus:** DataService Enhancements & Robustness

- DataService health monitoring
- Migration command and UI
- Atomic writes and file locking
- Advanced DataService API features

### Version 10.0.0 (Future)

**Focus:** Major Features & Refactoring

- Optional JPA DataService implementation
- TFIDFSearch persistence
- Player groups in permission system
- Warning system completion (if requested)

---

## Implementation Sequence (Recommended)

### Immediate (This Week)

1. ✅ Pattern generator system (DONE)
2. Complete `MigrationEntryEditMenu` TODOs
3. Wire filter menu clicks in `LanguageMigrationMenu`
4. Fix ExpressionEngine test

### Next Week

1. Implement `AdminPanelPerPlayerLanguageHandler`
2. Migrate per-player language data to DataService
3. Complete ConfigReloadMenu
4. Test language migration end-to-end

### Following Week

1. Add DataService health checks
2. Implement atomic writes for FileDataService
3. Add migration progress tracking
4. Write comprehensive tests

### Before Release

1. Run full test suite (target: 40+ passing tests)
2. Manual QA: test all menus and workflows
3. Update all documentation
4. Create release notes and changelog

---

## Known Issues and Limitations

### Current Limitations

- **FileDataService:** Not thread-safe for concurrent writes (will be fixed with file locking)
- **SQLiteDataService:** No connection pooling (single connection per instance)
- **LanguageMigrationMenu:** Filter menu not fully wired
- **MigrationEntryEditMenu:** Chat editing not implemented
- **ExpressionEngine:** One test failing (type mismatch)

### Non-Issues (By Design)

- PermissionsManager uses JPA (separate from DataService pattern)
- Warning system and item disable system are frozen features
- TFIDFSearch index is rebuilt on startup (persistence planned for future)

---

## Metrics and Quality Goals

### Test Coverage Target

- **Current:** 32 passing tests, 1 error
- **Target for 9.1.0:** 40+ passing tests, 0 errors
- **Target for 9.2.0:** 50+ passing tests, integration test suite

### Performance Goals

- DataService operations: < 10ms for single key operations
- Migration throughput: > 1000 keys/second for File↔SQLite
- Pattern validation: < 50ms per file (cached)

### Code Quality

- Zero compiler warnings (except sun.misc.Unsafe in ClassAppender)
- All public APIs documented with Javadoc
- No TODO markers in released code paths
- Permission nodes documented in plugin.yml

---

## Migration Notes (Consolidated Roadmaps)

This unified roadmap replaces:

- `ROADMAP_CONSOLIDATED.md` (now superseded)
- `service-data-roadmap.md` (archived)
- `src/main/java/de/happybavarian07/adminpanel/service/ROADMAP.md` (archived)
- `PatternFileGenerationRoadmap.md` (completed, archived)
- `LanguageMigrationMenuRoadmap.md` (merged here)

All content from previous roadmaps has been reviewed, consolidated, and organized by priority in this document.

---

## Contact and Contributions

- **Author:** HappyBavarian07
- **Repository:** [AdminPanel](https://github.com/happybavarian07/Admin-Panel)
- **License:** See LICENSE file
- **Dependencies:** PlaceholderAPI (required), CoolStuffLib (bundled), SuperVanish & Vault (optional)

For feature requests, bug reports, or questions, refer to the project repository.

