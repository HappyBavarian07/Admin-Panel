# AdminPanel Commands Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move AdminPanel's 38 class-based sub commands to the CoolStuffLib 3.0 command API (`@SubCommandInfo`, `arguments()`, `execute(sender, CommandArgs)`), fix the command bugs found on the way (A22, A23 and the new ones below), and put every hard-coded chat text into the language files.

**Architecture:** Commands stay classes, as I decided on 2026-09-26. Groups of near-identical classes become one parameterized class: the 13 `/openpanel` sub commands and `/dataclient menu` become `MenuOpenCommand`, the 7 reload/update admin commands become `AdminActionCommand`. Every other class keeps its file but loses the overrides that `@SubCommandInfo` and `arguments()` replace. Messages come from `Messages.*` keys in `en.yml`/`de.yml`.

**Tech Stack:** Java 17, Maven, Spigot API, CoolStuffLib 3.0.0 (local install), JUnit 5 + Mockito.

**Spec:** Decisions from 2026-09-26 (keep the class-based commands, modernize them) and `.claude/issues/AUDIT.md` A22/A23. Foundation plan: `docs/superpowers/plans/2026-09-26-adminpanel-foundation.md`.

## Global Constraints

- Never write `config.yml` from code. Runtime data goes to `data.yml` sections or database tables.
- Commits: small, one logical change each, **no `Co-Authored-By` or other AI attribution**.
- Minimal comments, no reformatting of untouched code.
- Before editing an AdminPanel symbol, run GitNexus impact (`npx --no-install gitnexus impact <symbol> --direction upstream --repo AdminPanel`) and report HIGH/CRITICAL.
- Keep every existing sub command name, alias and permission node working, unless the task says otherwise (A22 changes three names on purpose).
- New language keys go into **both** `src/main/resources/languages/en.yml` and `de.yml`. The files have mixed line endings, so edit them with a script that keeps each file's line endings.
- AdminPanel build: `mvn -B -q package -DskipTests`; smoke test: `tools/run-adminpanel-smoke.sh`.
- Docs are first person ("I"), never "we".

## Bugs this plan fixes

| # | Where | Bug |
|---|---|---|
| A22 | `AddonDisable/Info/ReloadCommand` | all three are named `enable` with the enable permission, so `/addon disable|info|reload` don't exist |
| A23 | `DisabledItemsManager`, `DisabledItemsListener` | neither the command nor the listener is registered, so disabled items are never blocked |
| C1 | 13 panel commands, `MenuCommand` | `Bukkit.getPlayer` matches name prefixes, so `/openpanel X Ste` opens the menu for "Steve2"; anyone with the menu permission can open it for others (`/adminpanel` requires `AdminPanel.open.other`) |
| C2 | panel commands | `%target%` placeholder added with `true` and never removed; console without a player gets the error **and** the usage |
| C3 | `AddonDisableCommand` | uses the `Enable.AddonNotFound` key, `ALREADY_ENABLED` is reported as "already disabled", `DisabledMessage` text says "is already disabled" |
| C4 | `AddonInfoCommand` | reads `AddonCommand.Info.AddonInfo`, the key is `AddonInfoMessage`, so it prints nothing; `%addon_enabled%` is never replaced |
| C5 | `AddonReloadCommand` | only suggests disabled addons, but reloading needs enabled ones |
| C6 | `AutoUpdaterManager` | keeps a second in-memory copy of `data.yml` and saves it, overwriting per-player languages and disabled items written through the main copy (and the other way round); `/apadmin reloaddata` only reloads that copy |
| C7 | `BackupManagerCommand` | unknown backup name throws a `NullPointerException`; `load` with a non-number throws `NumberFormatException` |
| C8 | `BugReportCommand` | cooldown shows milliseconds as seconds, messages hard-coded in English |
| C9 | `PerPlayerLanguageCommand` | the usage message is built and never sent |
| C10 | `AdminPanelOpenCommand` | too many arguments sends the non-existent key `Player.ToManyArguments` |
| C11 | `PermissionsSyncCommand` | player without stored permissions returns `false` (usage) instead of saying so |
| C12 | `ClientListListCommand` | `System.out.println` debug output |
| C13 | admin/disabled-item commands | all messages hard-coded in English, not in the language files |

## File map

- Create `commands/subcommands/panels/MenuOpenCommand.java` (replaces 13 files in `panels/**` and `dataclientcommands/MenuCommand.java`).
- Create `commands/subcommands/adminpaneladmin/AdminActionCommand.java` (replaces `ReloadConfig`, `ReloadData`, `ReloadLang`, `ReloadPlugin`, `ReloadCommandManagers`, `UpdateConfig`, `UpdateLangFiles` commands).
- Modify every remaining sub command, the 5 managers, `InitMethods.initCommandManagers()`/`initEvents()`, `AutoUpdaterManager`, `AdminPanelMain` (`reloadDataYML()`), `ConfigReloadMenu`, the 3 buggy top-level executors, `en.yml`, `de.yml`, `plugin.yml`.
- Test: `src/test/java/de/happybavarian07/adminpanel/commands/MenuOpenCommandTest.java`, `AddonCommandNamesTest.java`.

---

### Task 1: One data.yml in memory (C6)

**Files:** `main/AdminPanelMain.java`, `utils/managers/AutoUpdaterManager.java`, `menusystem/menu/ConfigReloadMenu.java`

**Produces:** `AdminPanelMain#reloadDataYML()` returns `boolean` (false on load error, logged).

- [ ] Impact check `AutoUpdaterManager`, `getDataYML`.
- [ ] Add to `AdminPanelMain`:

```java
public boolean reloadDataYML() {
    try {
        dataYML.load(new File(getDataFolder(), "data.yml"));
        return true;
    } catch (IOException | InvalidConfigurationException e) {
        fileLogger.writeToLog(Level.SEVERE, "Could not reload data.yml: " + e.getMessage(), LogPrefixExtension.ADMINPANEL_MAIN, true);
        return false;
    }
}
```

`load` refills the same object, so `PerPlayerLanguageHandler` and `AdminPanelUtils` keep seeing it.

- [ ] `AutoUpdaterManager`: drop the `dataFile`/`dataYML` fields; read `plugin.getDataYML()` everywhere; save with `plugin.getDataYML().save(new File(plugin.getDataFolder(), "data.yml"))`; `reloadData()` becomes `plugin.reloadDataYML()`. `setup()` must not NPE when `PluginsToUpdate` is missing: use `getConfigurationSection(...)` and return when `null`. Note `AutoUpdaterManager` is created before `setupLanguageHandler()` loads `dataYML`; check the order in `onEnable` and move `dataYML` loading to right after `coolStuffLib.setup()` if needed, keeping `setupLanguageHandler()` using the same object.
- [ ] `ConfigReloadMenu` "Reload Data" button calls `plugin.reloadDataYML()`.
- [ ] Build, commit `fix: keep one copy of data.yml in memory so the auto updater no longer overwrites other data`.

### Task 2: `MenuOpenCommand` for `/openpanel` and `/dataclient menu` (C1, C2)

**Files:** create `commands/subcommands/panels/MenuOpenCommand.java`; delete the 13 panel command files and `MenuCommand.java`; modify `PanelOpenManager`, `DataClientCommandManager`; test `MenuOpenCommandTest`.

**Produces:** `new MenuOpenCommand(String main, String name, Function<PlayerMenuUtility, Menu> factory)` and an overload with an explicit permission.

```java
public class MenuOpenCommand extends SubCommand {
    private final String name;
    private final String permission;
    private final Function<PlayerMenuUtility, ? extends Menu> factory;

    public MenuOpenCommand(String mainCommandName, String name, Function<PlayerMenuUtility, ? extends Menu> factory) {
        this(mainCommandName, name, factory.apply(null).getOpeningPermission(), factory);
    }

    public MenuOpenCommand(String mainCommandName, String name, String permission, Function<PlayerMenuUtility, ? extends Menu> factory) {
        super(mainCommandName);
        this.name = name;
        this.permission = permission;
        this.factory = factory;
    }

    @Override public String name() { return name; }
    @Override public String info() { return "Opens the " + name; }
    @Override public String permissionAsString() { return permission; }
    @Override public boolean autoRegisterPermission() { return false; }
    @Override public List<Argument> arguments() { return List.of(Argument.player("player").optional()); }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        Player target = args.has(0) ? args.onlinePlayer(0) : args.player();
        Player self = sender instanceof Player p ? p : null;
        if (target != self && !sender.hasPermission("AdminPanel.open.other")) {
            sender.sendMessage(lgm.getMessage("Player.General.NoPermissions", self, true));
            return true;
        }
        factory.apply(CoolStuffLib.getLib().getPlayerMenuUtility(target.getUniqueId())).open();
        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", target.getName(), false);
        sender.sendMessage(lgm.getMessage(target == self ? "Player.General.OpeningMessageSelf" : "Player.General.OpeningMessageSelfForOther", self, true));
        return true;
    }
}
```

`factory.apply(null)` works because every menu constructor accepts a `null` utility today (the old classes did `new X(null).getOpeningPermission()`); menus whose constructor has a second argument get `pmu -> new X(pmu, null)`.

- [ ] Test (Mockito, no server): name, permission from the factory, `arguments()` is one optional player.
- [ ] `PanelOpenManager.setup()`: `HelpCommand` plus 13 `new MenuOpenCommand(getCommandName(), "<OldName>", <Menu>::new)` lines with the old names (`BannedPlayersMenu`, `PlayerSelectMenu`, `PluginAutoUpdaterMenu`, `PluginInstallMenu`, `PluginSelectMenu`, `ChatManagerMenu`, `ServerManagerMenu`, `WhitelistedPlayersMenu`, `WhitelistManagerMenu`, `WorldCreateMenu`, `WorldSelectMenu`, `AdminPanelStartMenu`, `TempLanguageSelectMenu`).
- [ ] `DataClientCommandManager`: `new MenuOpenCommand(getCommandName(), "menu", "AdminPanel.DataClient.Menu.Open", DataClientMainMenu::new)`.
- [ ] Delete the 14 old files, build, run tests, commit `refactor: one MenuOpenCommand for all /openpanel sub commands, exact player names and open.other`.

### Task 3: Addon commands (A22, C3, C4, C5)

**Files:** the 5 `addoncommands/*` classes, `en.yml`, `de.yml`, `plugin.yml`; test `AddonCommandNamesTest`.

- [ ] Each class gets `@SubCommandInfo(name = ..., permission = ...)`: `listaddons` (alias `list`, `AdminPanel.AddonManageCMD.List`), `enable`, `disable`, `reload`, `info` with `AdminPanel.AddonManageCMD.<Enable|Disable|Reload|Info>`. Add `Disable`, `Reload`, `Info` to `plugin.yml` under `AdminPanel.AddonManageCMD`.
- [ ] `arguments()`: `Argument.custom("addon", s -> <names>)` — enable: disabled addons, disable/reload: enabled addons, info: all. `listaddons`: `Argument.integer("page").optional()`.
- [ ] `execute`: look the addon up once, send `AddonCommand.<Action>.AddonNotFound` with the right action; map every `EnableResult` to the key of the same name (`ALREADY_ENABLED` → `AddonAlreadyEnabled`, `NOT_ENABLED`/disable's "already disabled" case → `AddonAlreadyDisabled`); replace `IOException | ClassNotFoundException` → `RuntimeException` with the `Error…Addon` message and a file-log entry.
- [ ] Info reads `AddonCommand.Info.AddonInfoMessage` and replaces `%addon_enabled%`.
- [ ] Move the shared `replaceAddonPlaceholders` into a small package-private `AddonMessages` helper.
- [ ] Fix `Disable.DisabledMessage` text ("has been disabled!") in both language files.
- [ ] Test: the five names and permissions are distinct.
- [ ] Commit `fix: /addon disable, info and reload have their own names and permissions (A22)`.

### Task 4: `AdminActionCommand` for the reload/update commands (C6, C13)

**Files:** create `adminpaneladmin/AdminActionCommand.java`; delete the 7 old classes; modify `AdminPanelAdminManager`, language files.

```java
public class AdminActionCommand extends SubCommand {
    public interface Action { void run(CommandSender sender) throws Exception; }

    private final String name, permission, actionKey;
    private final String[] aliases;
    private final boolean confirm;
    private final Action action;
    // constructor sets all fields

    @Override public String name() { return name; }
    @Override public String[] aliases() { return aliases; }
    @Override public String permissionAsString() { return permission; }
    @Override public boolean autoRegisterPermission() { return false; }
    @Override public boolean requiresConfirmation() { return confirm; }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        Player player = sender instanceof Player p ? p : null;
        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%action%", lgm.getMessage("AdminPanelAdmin.Actions." + actionKey, player, false), false);
        sender.sendMessage(lgm.getMessage("AdminPanelAdmin.ActionStarted", player, false));
        try {
            action.run(sender);
            sender.sendMessage(lgm.getMessage("AdminPanelAdmin.ActionFinished", player, false));
        } catch (Exception e) {
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE, "Admin action " + name + " failed: " + e, LogPrefixExtension.ADMINPANEL_MAIN, true);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%error%", String.valueOf(e.getMessage()), false);
            sender.sendMessage(lgm.getMessage("AdminPanelAdmin.ActionFailed", player, false));
        }
        return true;
    }
}
```

Registrations (names, aliases and permissions unchanged):

| name | aliases | action | confirm |
|---|---|---|---|
| `reloadconfig` | – | `plugin.reloadConfig()` | no |
| `reloaddata` | – | `plugin.reloadDataYML()`, throw when false | no |
| `reloadlang` | – | `lgm.reloadLanguages(sender, true)` | no |
| `reload` | `reloadplugin` | `new PluginUtils().reload(plugin)` | **yes** |
| `reloadcommandmanagers` | `reloadcm` | `registry.unregisterAll(); initMethods.initCommandManagers()` | no |
| `updateconfig` | – | `plugin.updateConfig()` | no |
| `updatelangfiles` | `updatelang` | `lgm.updateLangFiles(); lgm.reloadLanguages(sender, true)` | no |

- [ ] Language keys `Messages.AdminPanelAdmin.ActionStarted/ActionFinished/ActionFailed` and `Actions.<Key>` names in en/de.
- [ ] Build, commit `refactor: one AdminActionCommand for the reload and update admin commands`.

### Task 5: Remaining admin commands (C7, C8, C13)

**Files:** `BackupManagerCommand`, `BugReportCommand`, `StaffChatCommand`, `StartUpLoggerControlCommand`, `PermissionMenuHelpCommand`, language files.

- [ ] `BackupManagerCommand`: `arguments()` = `choice("action", start, load, delete, listBackups, listFiles, listBackupsDone, getNewestBackupFile)`, `custom("backup", <backup ids>).optional()`, `word("number|page").optional()`. Unknown backup → `BackupSystem.SubCommands.BackupNotFound`; numbers through `args.integer(...)`; drop `printStackTrace`.
- [ ] `BugReportCommand`: `@SubCommandInfo(name = "report")`, `@CommandData(playerRequired = true)`, `Argument.text("message")`; seconds = `(remainingMillis + 999) / 1000`; messages `AdminPanelAdmin.BugReport.Cooldown/Error/Sent`.
- [ ] `StaffChatCommand`: `arguments()` = `custom("toggle|disableSC|message", ...)`, `player("player").optional()`; use `args.player()` and `args.onlinePlayer(1)`.
- [ ] `StartUpLoggerControlCommand`: `choice("state", "Enable", "Disable")`, messages `AdminPanelAdmin.StartUpLogger.Enabled/Disabled`.
- [ ] `PermissionMenuHelpCommand`: text as a list `AdminPanelAdmin.PermissionMenuHelp` (send each line), console allowed, syntax generated.
- [ ] Commit per class group: `refactor: backup, bug report, staff chat, logger and permission help commands use CommandArgs and language keys`.

### Task 6: Data client commands (C11, C12)

- [ ] `ClientListListCommand`: one `execute`, `integer("page").optional()` default 1, drop `System.out`.
- [ ] `ClientListUpdateCommand`: one `execute`.
- [ ] `PermissonsSyncCommand`: one `execute`; `custom("player", <online names>).optional()`; player without stored permissions → `DataClient.PermissionsSync.NoPermissionsStored`.
- [ ] `TrollsSyncCommand`: `choice("troll", HurtingWaterMap, ChatMuteMap, VillagerSoundsMap, BlockBreakPreventMap, DupeMobsOnKillMap, FreezePlayerMap)`; report `hasSucceeded()` (`TrollDataSendFailed` on false).
- [ ] Commit `refactor: data client commands use CommandArgs`.

### Task 7: `/disableditems` (A23, C13)

- [ ] `InitMethods.initCommandManagers()` registers `new DisabledItemsManager()`; `initEvents()` registers `new DisabledItemsListener()`. Check `plugin.yml` needs no command entry (the registry registers commands itself, like `/openpanel`); `DisabledItemsManager` gets the alias `ditems` that the syntax strings already use.
- [ ] `EnableCommand`/`DisableCommand`: `Argument.custom("material", s -> Material item names)`, one `execute`, messages `DisabledItems.InvalidMaterial/Disabled/AlreadyDisabled/Enabled/NotDisabled`.
- [ ] `ListCommand`: `integer("page").optional()`, `PaginatedList` like the other list commands, messages `DisabledItems.List.Header/Format/Footer/Empty/PageDoesNotExist`.
- [ ] Commit `fix: register /disableditems and its listener (A23)`.

### Task 8: Top-level command fixes (C9, C10)

- [ ] `PerPlayerLanguageCommand`: send the usage message; drop the `getOfflinePlayer(...) == null` check (never null) and use `hasPlayedBefore() || isOnline()` for "player not found".
- [ ] `AdminPanelOpenCommand`: `Player.Commands.TooManyArguments`; replace the `NullPointerException` catch with a `null` check.
- [ ] Commit `fix: /perplayerlang usage and /adminpanel argument messages`.

### Task 9: Verify

- [ ] `mvn -B test`, then `tools/run-adminpanel-smoke.sh` (extend it: after start, run `openpanel help`, `addon listaddons`, `addon info <none>`, `apadmin reloaddata`, `disableditems list`, `dataclient help` from the console and fail on `Config Path not found` or AdminPanel stack traces).
- [ ] In game with the user: `/openpanel ServerManagerMenu`, `/openpanel ServerManagerMenu <partial name>` (must fail), `/addon` tab completion, `/disableditems disable DIAMOND` then try to use a diamond, `/apadmin reload` (asks for confirmation).
- [ ] Update `.claude/issues/AUDIT.md` (A22, A23 fixed) and the admin-panel wiki `commands-and-permissions.md` (the A22 note, the new permissions, `/disableditems`).

## Self-Review Notes

- **Decisions covered:** commands stay classes (Tasks 2–7), only duplicates are merged.
- **Audit items:** A22 (Task 3), A23 (Task 7). New findings C1–C13 each have a task.
- **Kept on purpose:** all names, aliases and permission nodes; the four top-level executors stay plain Bukkit executors (they are registered through `plugin.yml`), only their bugs are fixed.
