# Player menus rework — design

Date: 2026-10-01. Approved in chat (answers to the questions): new trolls from the groups fake scares, movement and
fake TNT only; reversible only; fix potions and spawning with presets, spawn options and search; ban and kick menus
with presets, ban list with unban, IP ban and silent mode, bans recorded in the warning history.

Two parts, built one after the other because both edit the language files.

## Part A: trolls

### Today

`PlayerTrollMenu` has a fixed set of buttons: hurting water, block break/place prevention, villager sounds, mob
duplication on kill, chat mute, freeze, four fake kick messages, fake op and deop, drop inventory, TNT. State lives in
maps in `PluginStateManager`; the effects are enforced by listeners and tasks elsewhere. Adding a troll means a new
button, a new map, a new event, new permission and new language keys by hand.

### Troll registry

New package `trolls` with:

- `Troll`: `id()`, `permission()` (`AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.<Id>`), `kind()` (`TOGGLE` or
  `ONE_SHOT`), `isActive(UUID target)`, `start(Player staff, Player target)`, `stop(Player target)`.
- `TrollRegistry`: holds the trolls, knows which toggles are active per target, stops every active troll of a player
  when the target quits and every troll on plugin disable, and exposes `stopAll(target)`.
- A switch per troll in `config.yml` (`Pman.Actions.Trolls.<Id>: true`, read only, added to the bundled file with a
  comment). A disabled troll does not appear in the menu and its command path refuses.
- Item name and lore per troll from the language files (`PlayerManager.TrollMenu.More.<Id>` with `true`/`false` item
  variants for toggles, like the existing ones); no hard-coded text.
- Every start and stop fires the existing event style where one exists (`AdminPanelEvent`), so addons can cancel.

The existing trolls keep their behavior and their maps. They are not rewritten onto the registry; the registry only
adds new trolls. Their buttons stay in `PlayerTrollMenu`. A new button opens `PlayerTrollMoreMenu` (a
`PaginatedMenu<Troll>`, 54 slots) listing the registry trolls; the stop-all button sits in both menus.

### New trolls (all reversible, none touches items or blocks)

Fake scares, `ONE_SHOT`:
- `CreeperHiss`: creeper fuse sound at the target, then nothing.
- `FakeExplosion`: explosion sound and particles near the target, no damage, no block change.
- `Jumpscare`: elder guardian curse effect with sound, plus an optional title from the language file.
- `FakeBan`: shows the target a fake "You were banned" disconnect screen through a kick with the ban text of the
  language file, with a config switch that decides whether the player is really disconnected; document that a kick is
  not a ban. Skip this one if it duplicates the existing fake kick buttons.
- `DemoScreen`: the demo-mode welcome screen if the server API offers `showDemoScreen`; skip it otherwise and say so.

Fake TNT, `ONE_SHOT`:
- `FakeTnt`: spawns primed TNT near the target with a short fuse; when it ends the entity is removed and only the
  explosion sound and particles play. No damage, no block damage, no knockback. A persistent tag marks the entity and
  every explosion and damage event from a tagged TNT is cancelled as a second safety.

Movement:
- `Launch` (one shot): throws the target up and forward a little; fall damage is cancelled for this launch.
- `RandomHop` (one shot): teleports the target to a safe spot within 5 blocks.
- `SpinCamera` (one shot, 5 seconds): turns the target's yaw every tick, ends by itself.
- `NoJump` (toggle): jump strength reduced through an attribute modifier or an effect, removed on stop.
- `SlowMotion` (toggle): movement speed reduced, removed on stop.

Everything started over time (`SpinCamera`, `FakeTnt`) is cancelled on stop-all, on target quit and on plugin disable.
Effects use named attribute modifiers or tagged potion effects so stop only removes what the troll added.

### Tests

Unit tests for the registry (start, stop, stop-all, quit cleanup, disabled by config) with fake trolls, and for the
safe-spot search if it is a pure function. Bukkit-bound effects are checked on the smoke server where possible (the
test server has a console only), so list them for the in-game check.

## Part B: potion, spawning, ban and kick

### Potion menu

- Effects are applied to and removed from the target, not the clicker (today the checks use the clicker).
- The clicked effect comes from the clicked entry, not from a page-relative index.
- Duration and level are entered through `MenuAction.prompt`; durations accept `30s`, `5m`, `2h` or a plain number of
  seconds; the chat-event listener is removed.
- Effect names come from the registry key, not the deprecated `getName`.
- Presets, one click each, from the language file: Speed run, Night vision, Tank, Levitation prank, Blindness prank.
- Search through the same prompt and a category filter (beneficial, harmful, neutral) using the effect category.

### Spawning menu

- Spawn options kept per menu instance: count (1 to a config limit), baby, custom name, glowing, with armor, with a
  rider; each toggle or prompt is a button.
- A category filter (hostile, passive, water, boss) and a name search.
- The spawn location is the target's, and the limit prevents mass spawning (`Pman.Actions.Spawning.MaxCount`, default 10).

### Ban and kick menus

- Reason presets and duration buttons from `config.yml` (`Pman.Actions.Ban.Presets` with name and reason,
  `Pman.Actions.Ban.Durations`), a custom reason through the prompt.
- Optional IP ban and a silent flag (no broadcast to chat); both toggles in the menu, default from config.
- `BannedPlayersMenu` shows reason, who banned, when and the time left from the ban list entry, and unbans on click
  (the click acts on the clicked entry).
- Every ban and kick from these menus is written to the warning history as a record: new `WarningStatus.RECORD` that
  never counts as active, never fires escalation and is listed with a marker in `/warnings` and the warning menu.
  Banning through the warning escalation executor records the same way.
- Existing exempt players (`Pman.Actions.ExemptPlayers`) stay protected.

## Language and config

All texts and items in `en.yml` and `de.yml` (German in `de.yml`); config defaults with comments in the bundled
`config.yml`; nothing is written to `config.yml` by code.

## Out of scope

The world menus and the startup stages are separate designs.
