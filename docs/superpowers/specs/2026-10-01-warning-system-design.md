# Warning system — design

Date: 2026-10-01. Approved in chat: edit reason and expire now, automatic expiry, escalation rules, `/warn` commands,
storage in a database table.

## Today

`WarningManager` keeps warnings in memory and writes them to `warnings.yml`. `PlayerWarningMenu` lists them; its Add
button inserts three hard-coded test warnings, a click prints English text, and edit is a TODO. Nothing expires,
nothing escalates, there are no commands. `Pman.Actions.WarningSystem` in `config.yml` switches the system on.

## Storage

New table `player_warnings` through the CoolStuffLib JPA layer, same database and prefix as `plugin_flags`
(see `data/AdminPanelData`, `PluginFlag`, `PluginFlagRepository`, and how `AdminPanelMain` registers them).

`PlayerWarning` entity: `id` (auto increment), `playerUuid`, `reason`, `issuer` (UUID text, or `CONSOLE`),
`createdAt`, `expiresAt` (`0` = never), `status` (`ACTIVE`, `EXPIRED`, `REMOVED`), `endedAt`, `endedBy`.
Rows are never deleted: expiring or removing a warning only changes `status`, so the history stays for the audit trail
later. A player's warning number is their position among all their rows ordered by `createdAt`, not a stored counter.

`warnings.yml` is imported once into the table (same pattern as `data/LegacyDataImporter`: import, then rename the file
to `warnings.yml.imported`). Imported warnings keep their dates; a warning whose `expirationDate` has passed becomes
`EXPIRED`.

## WarningManager

Rewritten on the repository, with a per-player cache loaded on first use so menus and commands do not hit the database
per click. Database access runs off the main thread where the caller is not already async; public methods return
immediately for cached data.

- `add(UUID player, String reason, UUID issuerOrNull, long durationMillis)` returns the new warning and runs the
  escalation check.
- `editReason(long id, String reason, UUID editor)`, `expireNow(long id, UUID by)`, `remove(long id, UUID by)`.
- `active(UUID)`, `all(UUID)`, `activeCount(UUID)`.
- An async sweep every minute marks warnings past `expiresAt` as `EXPIRED`; it also runs once at startup.
- The feature stays behind `Pman.Actions.WarningSystem`; with it off the manager is `null` and the menu, commands and
  buttons that need it say so instead of failing.

## Escalation

Rules are read from `config.yml` (read only, never written by code); add the defaults and comments to the bundled
`config.yml`:

```yaml
Warnings:
  DefaultExpiry: 30d        # 0 or never = does not expire; units: m, h, d
  Escalation:
    - count: 3              # active warnings after the new one
      action: KICK          # KICK, TEMPBAN or COMMAND
      reason: "Too many warnings"
    - count: 5
      action: TEMPBAN
      duration: 7d
      reason: "Too many warnings"
    - count: 7
      action: COMMAND
      command: "ban %player% Too many warnings"
```

After `add`, every rule whose `count` equals the new active count runs once. `%player%` is replaced in commands.
Invalid rules are logged and skipped; they never break adding a warning. Bans use the existing ban helpers in
`AdminPanelUtils` and whatever the Ban menu uses for temp bans.

## Commands

Three command managers registered like `DisabledItemsManager`, CoolStuffLib 3 style (`@SubCommandInfo`, `arguments()`,
`CommandArgs`, texts in the language files, tab completion for players):

- `/warn <player> <reason...>` permission `AdminPanel.PlayerManager.PlayerSettings.Warn`; optional duration as a first
  token such as `7d` is not parsed, the default expiry applies.
- `/warnings <player> [page]` permission `AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Warnings`: lists with
  number, reason, issuer, dates, status.
- `/unwarn <player> <number|all>` permission `AdminPanel.PlayerManager.PlayerSettings.Warn`: expires the warning
  (`expireNow`); `all` expires every active one.

Console may use all three.

## Menu

`PlayerWarningMenu` stays a `PaginatedMenu<PlayerWarning>` with per-entry click handlers and `MenuAction` decorators:

- Entry item shows number, reason, issuer name, created, expires, status; active ones look different from ended ones.
- Left click: edit the reason with `MenuAction.prompt`; right click: expire now; shift right click: remove, with
  `.confirm(...)`.
- Add button: asks for the reason with a prompt, uses `Warnings.DefaultExpiry`.
- A toggle button switches between active only and all.
- All texts and items from the language files (`PlayerManager.WarningMenu.*`), English and German; no hard-coded text.
- Fix the target name lookup so offline players work (`Bukkit.getPlayer(...)` may be null).

## Tests

Unit tests with an in-memory fake of `PlayerWarningRepository` (an interface; Mockito cannot mock classes on Java 21,
so use a hand-written fake): add, edit, expire, remove, expiry sweep, active count, escalation rule selection and
invalid-rule handling, duration parsing, legacy import. The smoke script gets `warn`, `warnings` and `unwarn` console
runs.
