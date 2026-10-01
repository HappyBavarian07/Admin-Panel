# Startup stages with fault isolation — design

Date: 2026-10-02. Approved in chat: a status menu, `/apadmin status` and unavailable buttons; a failing required stage
disables the plugin cleanly; optional stages only mark themselves broken. Lucene search, language, commands and the
permissions manager stay required. Optional: addon loader, bStats, trolls, warnings, Vault, SuperVanish,
Bungee/socket sync, updater, auto updater (and the config backup).

## Today

`onEnable` is one list of 30 calls. Any exception in the middle leaves a half started plugin; the exception ends up in
the server log only. `InitMethods.initPluginCheck` disables the plugin when PlaceholderAPI is missing and then returns,
so `onEnable` carries on initializing a disabled plugin. Vault, SuperVanish, Bungee, the updater and the addon loader
log their own problems in their own words, and nothing tells an admin in game that a feature is dead. A menu button of
a dead feature throws a `NullPointerException` on click.

## Model (package `startup`, pure Java, unit-tested)

- `StageStatus`: `OK`, `DISABLED` (expected off: config switch or plugin not installed, with a reason), `FAILED`,
  `SKIPPED` (a stage it depends on did not run OK).
- `StageResult(id, required, status, detail)`.
- `Startup`: `run(id, required, action, dependsOn...)` runs the action and records the result. An exception (any
  `Exception` or `LinkageError`) becomes `FAILED` with the exception text as detail and goes to the plugin log with its
  stack trace. A failed required stage throws `StartupAbortedException` after recording. `disabled(id, reason)` records
  an expected-off stage. `results()`, `result(id)`, `isOk(id)`, `hasFailures()` (counts `FAILED` and `SKIPPED`, not
  `DISABLED`) and `summary()` (one line for the log).

## onEnable

Becomes a list of `startup.run(...)` calls in the same order. The required block is: library and database setup,
data, plugin state, API, utils, config files, prefix, events, language handler and manager, config update,
permissions manager, commands. The optional stages are the ones above; each guards itself with its config switch and
records `disabled(id, reason)` when it is off. Stage ids are short English words (`placeholderapi`, `vault`,
`supervanish`, `bungee`, `warnings`, `trolls`, `bstats`, `updater`, `autoupdater`, `addons`, `backup`).

`onEnable` catches `StartupAbortedException`, logs one clear block (stage, cause, "AdminPanel is disabled") and calls
`disablePlugin(this)`. `onDisable` gets null checks for everything a stage creates.

`InitMethods.initPluginCheck` is split: PlaceholderAPI (required, throws when missing instead of disabling and
returning), SuperVanish (absent is `DISABLED` "not installed"), Vault (absent is `DISABLED`, a failing economy setup is
`FAILED`). The Bungee socket client no longer swallows its `IOException`; the stage records it.

At the end of `onEnable` one line with `summary()` goes to the log, and ops with `AdminPanel.AdminPanelAdminCommands.Status`
get a notice on join when `hasFailures()` is true (chat only, one line).

## Showing it

- `/apadmin status` (permission `AdminPanel.AdminPanelAdminCommands.Status`): every stage with a coloured status and
  its detail; works from the console.
- `StartupStatusMenu` (paginated, same permission, also `/openpanel StartupStatusMenu`): one item per stage; failed ones
  red with the error text. The start menu shows a red banner item that opens it when `hasFailures()` is true.
- Unavailable buttons: a small helper registers a button for a feature. When the feature's stage is not `OK` it shows
  a barrier item "unavailable: <reason>" from the language file and the click only says why. Used for warnings and the
  warning menu, trolls and the more-trolls menu, the money menu (vault), the data client / sync menus (bungee), and the
  auto updater menu. Commands of a dead feature answer with the same message.

## Texts and permissions

All texts from `en.yml` and `de.yml` (`StartupStatus.*`). New permission `AdminPanel.AdminPanelAdminCommands.Status`.
The language key test is extended to cover the new keys.

## Not in this change

Retrying a failed stage from the menu, hot reload of a stage, and moving the code of the stages out of
`AdminPanelMain` into classes of their own (the methods stay where they are; only the calls are wrapped).
