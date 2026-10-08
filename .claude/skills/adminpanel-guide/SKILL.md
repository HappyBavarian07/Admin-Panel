---
name: adminpanel-guide
description: Project infopoint for the AdminPanel Spigot/Paper plugin (io.github.happybavarian07:Admin-Panel). Use whenever working in this repository — answering "how does X work", locating a subsystem, explaining features or limitations, planning a change or new feature, or before editing plugin code. Knows the module map, startup pipeline, service/data layer, permission and menu systems, its dependency on CoolStuffLib, and where the current bug/feature backlog lives.
---

# AdminPanel — Project Guide

You are the resident expert on **AdminPanel**, a Minecraft server administration plugin
for Spigot/Paper by HappyBavarian07. This skill makes you an infopoint: answer questions
about the project, locate code, explain features and limitations, and help plan work.

## What this project is

- A Bukkit `JavaPlugin` (`de.happybavarian07.adminpanel.main.AdminPanelMain`) that
  consolidates many admin tools into one plugin: player management, world controls
  (gamerules, weather, time), plugin management, server controls, permissions, staff
  chat, warnings, trolls, backups, an auto-updater, BungeeCord/socket syncing, an addon
  loader, and multi-language support.
- GUI-driven: nearly every feature is an inventory menu under `menusystem/menu/...`.
- Maven project, Java 17, `groupId io.github.happybavarian07`, `artifactId Admin-Panel`,
  published to Maven Central. Current version lives in `pom.xml` (do not hardcode it in
  answers — read it).
- Hard dependency: **PlaceholderAPI**. Optional: SuperVanish, Vault. Bundled library
  dependency: **CoolStuffLib** (see below).

## Relationship to CoolStuffLib

AdminPanel is the flagship consumer of **CoolStuffLib** (`F:\InteliJ Programs\CoolStuffLib`,
`io.github.happybavarian07:CoolStuffLib`). CoolStuffLib supplies the reusable
infrastructure; AdminPanel supplies the Minecraft-admin features on top.

From CoolStuffLib AdminPanel uses: `CoolStuffLib` / `CoolStuffLibBuilder` bootstrap,
`LanguageManager`, `CommandManagerRegistry`, `MenuAddonManager` + menu system, the JPA
persistence layer (`RepositoryController`, `DatabaseProperties`), `ServiceRegistry`,
`BackupManager`, and utilities. When a symbol is in package `de.happybavarian07.coolstufflib.*`
it lives in the CoolStuffLib repo — switch to the coolstufflib-guide skill for it.

If you change a CoolStuffLib API that AdminPanel calls, both repos must move together and
the `pom.xml` CoolStuffLib version must match the library's released version.

## Module map (`src/main/java/de/happybavarian07/adminpanel`)

- `main/` — `AdminPanelMain` (bootstrap/God-class), `AdminPanelAPI`/`LocalAdminPanelAPI`,
  `InitMethods`, `Metrics`.
- `service/` — the DataService abstraction: `service/api` (`DataService`,
  `DataServiceType`, `DataServiceConfig`), `service/impl` (`FileDataService`,
  `SQLiteDataService`, `DataServiceFactory`, `DataServiceMigrationUtil`). Key/value JSON
  store, File↔SQLite migration.
- `permissions/` — `PermissionsManager`, `PlayerPermission(Repository)`; uses the
  CoolStuffLib JPA layer (intentionally separate from DataService).
- `menusystem/menu/...` — all GUIs (playermanager, pluginmanager, servermanager,
  worldmanager, dataclient, languagemigration, misc).
- `commands/` — `commands/managers` and `commands/subcommands/...` (panels, addon,
  adminpaneladmin, dataclient, disableditems).
- `syncing/` — Bungee + Java-socket cross-server sync (`DataClient`, crypto, packets,
  managers).
- `backupmanager/`, `addonloader/`, `configupdater/`, `hooks/`, `placeholders/`,
  `events/`, `listeners/`, `language/`, `bungee/`.
- `utils/` — managers (`AutoUpdaterManager`, `WarningManager`, `PluginStateManager`,
  `FileCorruptionManager`, `PluginDescriptionManager`, `AdminPanelPerPlayerLanguageHandler`),
  `pattern/`, `tfidfsearch/` (Lucene), `mysql/`, dependency loading.
- Vendored (do not treat as project code): `dte/hooksystem/...` and `org/json/...`.

## Startup pipeline (read before touching lifecycle)

`AdminPanelMain.onLoad()` → backup manager, file logger, config-corruption check,
`saveDefaultConfig`, platform detection, core dependency loading, optional on-load addons.

`AdminPanelMain.onEnable()` runs a long linear sequence of `setup*`/`init*` methods:
CoolStuffLib builder → repository controller (JPA/`database.properties`) → metrics →
plugin-state manager → menu/language/command managers → `coolStuffLib.setup()` → service
registry (DataService) → API → utils → bungee channels → config files → warnings →
permissions → events → language handler → staff chat → updater → commands → addon loader
→ backups. This is a known God-class; any refactor here is high blast radius.

## Configuration surface

- `src/main/resources/config.yml` (~247 keys) — feature toggles and settings, read via
  `getConfig().getX("Plugin.…" / "Pman.…")`.
- `src/main/resources/database.properties` — JPA/RepositoryController DB config
  (`dbtype` = `sqlite` or `mariadb`). Read by `AdminPanelMain.setupRepositoryController()`.
- `src/main/resources/plugin.yml` — commands + full permission tree.
- Runtime data: `data.yml`, `warnings.yml`, `addons/onLoadExecution.yml`, plus the
  DataService store and the JPA database file.

## Build & test

- Build with Maven (`mvn package`) — **do not run builds unless the user asks.** The shade
  plugin uses `minimizeJar=true` and copies the jar to hardcoded local test-server paths.
- Tests: JUnit 5 + Mockito under `src/test`. Small suite relative to feature surface.
- `.gitnexus/` index powers the GitNexus MCP tools (see repo `CLAUDE.md`); prefer them for
  impact analysis before edits.

## Capabilities — what you can help do here

- Explain any subsystem and trace execution flows.
- Locate where a feature/menu/command/permission is implemented.
- Plan features and refactors (respect the startup-pipeline and API→Manager layering;
  the architecture direction is monolithic→modular, declarative menus, startup stages).
- Draft code changes, new menus/commands, config keys, permission nodes, and tests.
- Assess impact and migration risk before edits.

## Known limitations (stable, structural)

- `AdminPanelMain` is a centralized bootstrap; optional-system failures can affect startup.
- Menus largely use imperative `if-else` / `isSimilar` handling rather than the declarative
  `MenuAction` system. This is legacy tech debt targeted for migration, **not** a design
  choice — see `.claude/issues/AUDIT.md` (A18). Prefer MenuAction for new/edited menus.
- Mixed concurrency models (`new Thread`, Bukkit scheduler, `CompletableFuture`).
- Test coverage is narrow vs. feature breadth.
- Some features are intentionally frozen/partial (item-disable system, warning edit
  actions) — treat as planned, not broken, unless the user asks to finish them.

## Where the current backlog lives (do not hardcode volatile facts)

Specific bugs, discrepancies, and feature ideas change often. Do **not** bake them into this
skill. Instead consult, and keep updated, the living documents in `.claude/issues/`:

- `.claude/issues/AUDIT.md` — current bugs, discrepancies, tech debt, risk notes.
- `.claude/issues/FEATURES.md` — proposed/planned feature ideas with what/why/where.

Also present at repo root (may lag behind reality — verify against code): `PROJECT_AUDIT.md`,
`ARCHITECT_AUDIT.md`, `UNIFIED_ROADMAP.md`, `AGENTS.md`. When you confirm an item in
`.claude/issues/*` is fixed or shipped, update those files rather than this skill.

## Working rules in this repo

- Verify version numbers, config keys, and symbol names against the source before stating
  them — they drift.
- Run impact analysis (GitNexus) before editing shared symbols; warn on HIGH/CRITICAL.
- Don't run compilers/builds unless asked. Keep changes scoped; don't reformat untouched code.
