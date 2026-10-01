# World menus rework — design

Date: 2026-10-02. Scope chosen in chat: unloaded worlds in the list, teleport and player list, duplicate / rename /
import, difficulty and spawn flags and seed, one combined time and weather menu, gamerule search and categories and
prompts, world creation with seed and presets.

## Bugs in the current menus

- `GameRuleMenu.changeGameRule` casts `world.getGameRuleValue(rule)` (a `Boolean` or `Integer`) to `String`: every click
  throws a `ClassCastException`.
- `WorldSelectMenu` only lists loaded worlds, so the Load button in `WorldSettingsMenu` can never be reached. It also
  maps a click to `worlds.get(indexOnPage)`, which is the wrong world on page 2.
- `loadWorld` and `saveWorld` build paths with `"\\data\\raids.dat"` (only works on Windows) and `saveWorld` creates
  `raids.dat` as a directory first. Both swallow errors.
- `deleteMinecraftWorld` kicks every player and accepts the main world, which breaks the server.
- `TimeChangeMenu` looks every slot up under the `SUNRISE` key.
- Create menu: no seed, no difficulty, no check whether the world already exists, the name prompt accepts anything.

## Logic (package `worlds`, pure Java, unit-tested)

- `WorldNames`: `isValid(name)` (`[A-Za-z0-9_.-]{1,64}`, no leading dot, not a reserved Windows name),
  `normalize(input)` (spaces to `-`).
- `WorldFolders`: `unloaded(File container, Collection<String> loadedNames)` returns the folders that hold a
  `level.dat` and are not loaded; `copy(File from, File to)` copies a world folder without `uid.dat` and
  `session.lock`; `rename` is a folder move.
- `GameRuleCategory` (`MOBS`, `PLAYER`, `WORLD`, `DAMAGE`, `OTHER`) with `of(String ruleName)`.
- `GameRuleEdit`: `next(Object current, ClickKind kind)` for booleans (toggle) and integers (`+1`, `-1`, `+10`,
  `-10`, never below 0); `parse(Object current, String typed)` for the prompt, returns `null` when the text does not
  fit the rule's type.
- `TimeEdit`: `add(long time, long delta)` wraps into `0..23999`; `parse(String)` accepts ticks, `6000`, `12h`, `6:30`
  (24-hour clock, 0:00 is tick 18000).
- `WorldPreset` (`NORMAL`, `FLAT`, `VOID`, `AMPLIFIED`, `LARGE_BIOMES`): `type()` and `generatorSettings()` (VOID uses a
  flat generator with one air layer and the void biome).

## Operations (`worlds/WorldOperations`, Bukkit calls, no unit tests)

- `load(name)`: `Bukkit.createWorld(new WorldCreator(name))`, no raids.dat tricks.
- `unload(world, save)`, `save(world)`: refuse the main world (`Bukkit.getWorlds().get(0)`) for unload, rename and
  delete; players in the world are teleported to the main world spawn first, never kicked.
- `duplicate(world, newName)`: save, copy folder off the main thread, then load the copy on the main thread.
- `rename(world, newName)`: move players out, unload with save, move the folder, load under the new name.
- `delete(world)`: move players out, unload without save, delete the folder off the main thread.
- `importWorld(name)`: folder must exist directly in the server container and hold a `level.dat`, then `load`.
- `create(...)`: `WorldCreator` with name, preset type and generator settings, environment, seed, structures, hardcore;
  difficulty is set on the new world afterwards. The existing `WorldCreateEvent` stays and gets the seed as an extra
  constructor argument and getter/setter (old constructor stays).
- Every operation returns a result enum (`OK`, `EXISTS`, `MAIN_WORLD`, `INVALID`, `MISSING`, `FAILED`) that the menu turns
  into a language message. Errors go to `AdminPanelMain.logError`, never swallowed.

## Menus

- `WorldSelectMenu` (54): entries are loaded worlds and unloaded folders. Item shows environment, players, state;
  unloaded ones show a different item. Click on loaded: settings. Right click on loaded: teleport. Click on unloaded:
  load. Buttons: create, import (prompt), filter (all / loaded / unloaded), search (prompt, right click clears).
- `WorldSettingsMenu` (54): PVP, autosave, difficulty (cycle), monster spawning, animal spawning, keep inventory,
  time and weather, gamerules, teleport here, teleport to spawn, set spawn to my position, players, seed (click sends
  the seed to chat), border size (prompt, 0 resets), save, unload, duplicate, rename, delete (confirm), back, refresh.
  Load is gone from here (a loaded world is loaded).
- `WorldPlayersMenu` (`PaginatedMenu<Player>`): heads of the players in the world; click teleports staff to the player,
  right click sends the player to the main world spawn.
- `WorldTimeWeatherMenu` (27 or 36 slots): presets from `Time`, `+1h` and `-1h`, tick prompt, freeze toggle
  (`doDaylightCycle`), clear / rain / thunder with a duration prompt. Replaces `TimeChangeMenu` and
  `WeatherChangeMenu`; the `Time` and `Weather` enums stay. The menu fires `TimeChangeEvent` and `WeatherChangeEvent`.
- `GameRuleMenu` (54): search prompt, category filter, left / right for ±1, shift for ±10, middle click for a typed
  value, drop key (Q) resets to the default. Rules that differ from the default are marked.
- `WorldCreateMenu` (36): name, seed, preset, environment, difficulty, structures, hardcore, teleport after creation,
  create. Create refuses an invalid name and an existing folder.

Texts: all item and message texts from `en.yml` and `de.yml` under `WorldManager.*`; messages through `CommandText`.

## Permissions

New under `AdminPanel.WorldManager`: `Load`, `Unload`, `Delete`, `Duplicate`, `Rename`, `Import`, `Teleport`, `Players`.
Existing ones keep their meaning (`Settings` also covers difficulty, spawn flags, set spawn and border). All are
children of `AdminPanel.WorldManager.*`.

## Not in this change

View distance, world border damage settings, per-world gamerule presets, generator plugins, nether/end linking.
