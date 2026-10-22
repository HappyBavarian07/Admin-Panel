# AdminPanel — Feature ideas (living document)

Proposed / interesting features. Not commitments. Keep the `adminpanel-guide` skill free of
these specifics; point to this file instead. When one ships, move it to the roadmap/changelog
and delete it here. Rechecked against the code on 2026-10-01; the old items about the DataService
(health dashboard, transaction helper, metrics, migration command) are gone because the DataService is.

## Stability & architecture

1. **Startup stages with results** — replace the long linear `onEnable` chain in `AdminPanelMain` with explicit
   stages that return success or failure, so a failed optional part (Vault, SuperVanish, bungee sync, addons, updater)
   disables only its own menus and commands instead of the whole plugin. Largest robustness gain; also the biggest
   change. Folds in the old "graceful degradation" idea (a capability registry the menus ask before showing a button).
2. **Config schema validation** — `config.yml` is checked at startup (`ConfigValidator`, shipped 2026-10-22). Still open:
   `database.properties`, which would have caught the old MySQL key mismatch.
3. **Async policy for the sync layer** — the proxy socket reader and packet handler still run on raw threads in
   `ConnectionHandler` and `PacketHandler`; give them an owner that stops them on disable.

## Data & diagnostics

4. **Admin action journal + export** — shipped 2026-10-22 (`journal` package, table `admin_actions`, `JournalMenu`, CSV
   export). Hooked so far: bans, unbans, kicks, warnings, op, whitelist, permissions, gamerules, plugin install, server
   stop and restart. Not hooked yet: troll toggles, world create/delete/import/settings, maintenance mode, plugin
   enable/disable/unload, language switches, data client actions, addon actions.
5. **Database migration command** — `/adminpanel migrate <sqlite|mysql>` with dry run and progress, built on the
   CoolStuffLib JPA layer (the old DataService migration util is gone).

## Permissions & moderation

6. **Permission groups with inheritance** — shipped in 9.0.0 as the permission manager (`permissions/` package, `/perms`,
   `menusystem/menu/permissionmanager`): groups with weights and parents, temporary and per-world entries, prefixes,
   explain, export and import, optional Vault provider. Still open: a context provider for regions, a web editor, and
   syncing groups between servers (the data client only syncs per-player permissions).
7. **Permission audit trail** — shipped: every change is a journal entry (`PERMISSION_*`, `GROUP_*`, `USER_*`), `/perms audit`
   and the audit menu show only those. Still open: the history of one node as its own view.

## UX

8. **Categorized navigation hub** — group the start menu into Player / System / World / Diagnostics hubs as the menu
    count grows.

## Sync & addons

9. **Multi-server state reconciliation** — conflict resolution (last write wins or versioned) for synced state, to
    avoid silent overwrites between servers.
10. **Addon capability model** — manifest-declared capabilities and safety checks before an addon is enabled.
11. **TFIDF search index persistence** — the Lucene index lives in memory (`ByteBuffersDirectory`) and is rebuilt on
    every start; persist it and rebuild incrementally.

## Dev experience

12. **Safe-test mode** — flags that simulate update, migration and permission flows on a test server.
13. **Broader tests** — menu workflows (a fake player harness like CoolStuffLibTest's), permission edge cases, the
    updater and the sync layer. The suite has 40 tests for the whole plugin.

Shipped and removed from this list: warning system (table, expiry, escalation, /warn, menu), danger zone confirmation (Stop, Restart and world Delete already ask first),
item-disable system and `/disableditems` (commands plan), declarative menus
(menus plan), contextual tab completion (typed arguments from CoolStuffLib 3), unified async policy for one-shot tasks
(cleanup plan).
