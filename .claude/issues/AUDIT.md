# AdminPanel — Audit (living document)

Audited: 2026-08-19 against branch `newfeature`. This file is the current backlog of bugs,
discrepancies and tech debt. Update it as items are fixed; do not copy volatile items into
the `adminpanel-guide` skill.

Note: the root-level `PROJECT_AUDIT.md` / `ARCHITECT_AUDIT.md` are older and partly stale —
several items they list are already fixed in code (see "Already fixed" at the bottom).

## Correctness bugs

## Discrepancies / configuration

### A9 — Roadmap/version drift
- `UNIFIED_ROADMAP.md` header says 9.0.0; `pom.xml` is at a later version. Root audit docs
  predate current code. Keep one source of truth; verify versions from `pom.xml`.

## Code quality / robustness

### A16 — Repo hygiene
- Committed/oversized artifacts: `test.db` (~7.5MB, gitignored now), `pom.xml.releaseBackup`,
  `pom.xml.versionsBackup`, `General Updates.pdf`. Consider pruning from the working tree.

## Already fixed (verified in current code — remove from older audit docs)

Foundation migration, 2026-09-26 (plan `docs/superpowers/plans/2026-09-26-adminpanel-foundation.md`, verified with
`tools/run-adminpanel-smoke.sh` on Spigot 1.21.10 incl. `/reload`):
- A1/A2/A12: `database.properties` is parsed by `DatabaseSettings` (reads the `mysql_*` keys, defaults, port check,
  relative SQLite path under the plugin folder). `dbtype=mariadb` used to throw "Unsupported database driver" in
  CoolStuffLib; it is now an alias for MySQL.
- A3/A4/A7/A8: the DataService, its second service registry and `plugin_data.db` in the server root are gone.
  Runtime data lives in `plugin_flags`, `plugin_descriptions` and `updater_cache` (CoolStuffLib JPA, same database
  and prefix as the permissions). Old `plugin_data.db` files are imported once and renamed to `*.imported`.
- A5: a failing on-load addon is logged and skipped. A6: null-safe bungee/data-client shutdown. A10: plain getters.
- A19: AdminPanel uses CoolStuffLib's `StartUpLogger` (`create(JavaPlugin)` works in `onLoad`).
- A20: built and tested against the current CoolStuffLib `newfeature`.
- BackupManager: CoolStuffLib's implementation; CoolStuffLib starts/stops the scheduler.
- Found on the way: the updater cache used one set of keys for every plugin and never expired (now per resource id,
  trusted for one hour); maintenance/chat-mute were read from `data.yml` before it was loaded (now a table);
  per-player languages were stored twice (now only CoolStuffLib's handler in `data.yml`, sharing AdminPanel's
  `data.yml` object).

Commands migration, 2026-09-27 (plan `docs/superpowers/plans/2026-09-27-adminpanel-commands.md`, console commands
checked by `tools/run-adminpanel-smoke.sh`):
- A22: `/addon disable|reload|info` have their own names and permissions (`AdminPanel.AddonManageCMD.<Action>`).
- A23: `/disableditems` (alias `/ditems`) and `DisabledItemsListener` are registered.
- All sub commands use `@SubCommandInfo`, `arguments()` and `CommandArgs`; the 13 `/openpanel` classes and `/dataclient
  menu` are one `MenuOpenCommand`, the 7 reload/update admin commands one `AdminActionCommand`. Hard-coded English
  texts moved to `Messages.AdminPanelAdmin`, `Messages.DisabledItems`, `Messages.DataClient`, etc.
- Found on the way: `/openpanel` crashed on CoolStuffLib 3 (menus built with a `null` player utility to read their
  permission); `/openpanel <menu> <name>` matched name prefixes and needed no `AdminPanel.open.other`;
  `AutoUpdaterManager` kept a second copy of `data.yml` and overwrote other data when saving; the German addon messages
  sat outside `Messages`; `/addon info` read a non-existent key; `/apadmin backup` threw NPEs for unknown backups;
  `/apadmin report` showed milliseconds as seconds; `/perplayerlang` never sent its usage; `getCustomObject` callers
  need the full `Messages.` path.
- CoolStuffLib: `ConfigUpdater` wrote list entries containing `"` unescaped, which broke the whole language file on the
  server after an update (fixed there, with a test).

Cleanup, 2026-10-01 (smoke test and 40 unit tests green):
- A11: `printStackTrace` and `System.out` calls now use `AdminPanelMain.logError` or the plugin file logger (left on
  purpose: the Bungee test command, the addon sample, and `DependencyLoader`, which only builds a trace string).
- A13: permission setup, the addon loader and the auto updater are Bukkit async tasks; the permission wait stops when the
  plugin is disabled. Still raw threads: the proxy socket reader and packet handler threads and the permission indexer
  (named daemon threads).
- A14: CoolStuffLib and classgraph are kept whole by the jar minimizer. A15: the test server copies run in the
  `copy-to-test-servers` profile, active only where `F:\Weitere Minecraft Test Server` exists. A17: the unused
  `log4j-core` dependency is gone.

Menus migration, 2026-10-01 (smoke test and 40 unit tests green; clicks not yet tried in game):
- A18/A21: all menus use `registerButton`, `setPaginatedData` with per-entry click handlers, `MenuAction` decorators
  (`requires`, `then`, `thenClose`) and `MenuAction.prompt`. List menus acted on the wrong entry on page 2 and later
  (page-relative index used on the whole list). Chat input in `PluginInstallMenu`, `WorldCreateMenu` and the permission
  search used `Listener` menus whose handlers `MenuListener` unregisters on close; they use the chat input service now.
- Open in game: banned players on page 2, plugin install, world create, permission list (search, modes), player troll
  toggles, language select menus.

CoolStuffLib catch-up, 2026-10-21 (131 unit tests and the smoke test green against the current CoolStuffLib):
- `PluginInstallMenu` reset `fileName` to `null` after an install, so a reused menu would NPE on the next render, and it
  reported "successfully installed" after a failed download. It resets to the default name and reports success only on success.
- `AdminPanelMain.updateConfig()` runs `ConfigUpdater` on every start. CoolStuffLib's updater wrote the template's values over
  the owner's since the language rewrite; it keeps the owner's values again (checked on the test server: a changed
  `StaffChat.ChatPrefix` under the plugin section survived two starts).

- SQLite transaction rollback/finally: present (`SQLiteDataService.java:162-167,235-239`).
- `DataServiceConfig.getInt` null-safe unbox: fixed (`:37-40`).
- `onLoad` OnLoad-section null check: present (`AdminPanelMain.java:277`).
- `FileDataService.listKeys` returns a defensive copy: fixed (`:106`).
- `onDisable` guards `pluginStateManager`/`warningManager` null: present.
