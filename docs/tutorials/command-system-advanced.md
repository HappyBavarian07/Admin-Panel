# Command Manager System: Advanced Tutorial

This tutorial covers the advanced features of the Command Manager System in **Cool Stuff Lib**. Complete
the [Basic Tutorial](command-system-basic.md) first.

## Table of Contents

1. [Cool Stuff Lib Integration](#1-cool-stuff-lib-integration)
2. [Command Data Configuration](#2-command-data-configuration)
3. [Nested Subcommands](#3-nested-subcommands)
4. [Dynamic Tab Completion](#4-dynamic-tab-completion)
5. [Root Commands](#5-root-commands)
6. [Cooldowns and Confirmations](#6-cooldowns-and-confirmations)
7. [Async Commands](#7-async-commands)
8. [The Help Command](#8-the-help-command)
9. [Command Lifecycle Hooks](#9-command-lifecycle-hooks)
10. [Error Handling and Logging](#10-error-handling-and-logging)
11. [Permission Management](#11-permission-management)
12. [Sender-Specific Behavior](#12-sender-specific-behavior)
13. [Command Unregistration](#13-command-unregistration)
14. [Message Keys Reference](#14-message-keys-reference)
15. [Best Practices](#15-best-practices)

## Required Imports

```java
import de.happybavarian07.coolstufflib.CoolStuffLib;
import de.happybavarian07.coolstufflib.commandmanagement.*;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import java.time.Duration;
```

## 1. Cool Stuff Lib Integration

Inside a subcommand, `lib`, `lgm` and `registry` are available as fields once the command is registered:

```java
@SubCommandInfo(name = "greet")
public class GreetSubCommand extends SubCommand {
    public GreetSubCommand(String main) { super(main); }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        Player player = args.player();
        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%player%", player.getName(), false);
        player.sendMessage(lgm.getMessage("Commands.Greet.Success", player, true));
        return true;
    }
}
```

`getMessage(..., true)` resets the placeholders it used. `lgm.getMessageOrDefault(path, player, fallback, true)`
returns your fallback when the key is missing instead of a "null path" marker.

Opening a menu from a command:

```java
@SubCommandInfo(name = "menu")
@CommandData(playerRequired = true)
public class MenuSubCommand extends SubCommand {
    public MenuSubCommand(String main) { super(main); }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        Player player = args.player();
        new ConfigurationMenu(lib.getPlayerMenuUtility(player.getUniqueId())).open();
        return true;
    }
}
```

## 2. Command Data Configuration

`@SubCommandInfo` holds the metadata, `@CommandData` the sender and argument rules:

```java
@SubCommandInfo(name = "grant", aliases = "g", permission = "myplugin.user.grant",
        cooldownMillis = 2000, confirm = true, async = false)
@CommandData(
    playerRequired = true,                           // only players
    opRequired = true,                               // ops may use it without the permission
    minArgs = 2,                                     // overrides the count derived from arguments()
    maxArgs = 5,
    allowOnlySubCommandArgsThatFitToSubArgs = true,  // reject values not offered by tab completion
    senderTypeSpecificSubArgs = true                 // different completions for players/console
)
public class GrantSubCommand extends SubCommand { ... }
```

### Strict Argument Validation

With `allowOnlySubCommandArgsThatFitToSubArgs = true`, every argument position that has suggestions only
accepts one of them (positions without suggestions, like numbers, accept anything). Invalid values produce
`Player.Commands.CommandContainsInvalidArgs`.

### Sender-Specific Arguments

With `senderTypeSpecificSubArgs = true`, override `subArgs` and use `isPlayer` (1 player, 0 console):

```java
@Override
public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
    return isPlayer == 1
            ? Map.of(1, new String[]{"inventory", "location", "health"})
            : Map.of(1, new String[]{"broadcast", "reload"});
}
```

## 3. Nested Subcommands

Group related commands into a tree: `/myplugin user add <name>`, `/myplugin user remove <name>`.

```java
@Override
public void setup() {
    SubCommand user = subCommand("user")
            .sub(subCommand("add").arguments(Argument.player("name"))
                            .executes((sender, args) -> { addUser(args.string(0)); return true; }).build(),
                    subCommand("remove").arguments(Argument.player("name"))
                            .executes((sender, args) -> { removeUser(args.string(0)); return true; }).build())
            .build();
    registerSubCommand(user);
}
```

Class-based subcommands nest with `addChild`:

```java
UserSubCommand user = new UserSubCommand(getCommandName());
user.addChild(new UserAddSubCommand(getCommandName()));
registerSubCommand(user);
```

What nesting gives you:

- Dispatch and tab completion walk the tree (`/myplugin user <tab>` suggests `add`, `remove`).
- Paths drive the defaults: syntax `/myplugin user add <name>`, permission `myplugin.user.add`,
  language keys `Messages.Commands.myplugin.user.add.Info`.
- `/myplugin user` alone shows the usage `/myplugin user <add|remove>` if the parent has no own logic.
- Children get dependencies, permissions and help entries like top-level subcommands.

## 4. Dynamic Tab Completion

Most completion needs are covered by declared arguments (see the Basic Tutorial). For values that depend on
the server state, use a custom argument; it is evaluated on every tab press:

```java
@Override
public List<Argument> arguments() {
    return List.of(
            Argument.custom("warp", sender -> warpManager.getWarpNames()),
            Argument.enumOf("mode", GameMode.class).optional());
}
```

The sender passed to a completer can be `null` (for example while rendering the help page).

When a position depends on earlier arguments, override `subArgs` directly. Positions start at 1 for the first
argument after the subcommand:

```java
@Override
public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
    Map<Integer, String[]> map = new HashMap<>();
    map.put(1, new String[]{"player", "mob"});
    if (args.length > 0 && args[0].equalsIgnoreCase("mob")) {
        map.put(2, new String[]{"zombie", "skeleton", "creeper"});
    }
    return map;
}
```

Suggestions are filtered by what the player has typed and sorted automatically.

## 5. Root Commands

A command built with `registry.command(...)` can do something on its own and still have subcommands.
The root handler runs for `/warp` and for any first argument that is not a subcommand:

```java
SubCommand set = new SubCommandBuilder("warp", "set").arguments(Argument.word("name"))
        .player((player, args) -> warpManager.create(args.string(0), player.getLocation())).build();

registry.command("warp")
        .arguments(Argument.custom("name", sender -> warpManager.getWarpNames()))
        .player((player, args) -> warpManager.teleport(player, args.string(0)))
        .sub(set)
        .register();
```

Here `/warp spawn` teleports, `/warp set spawn` runs the `set` subcommand. In your own `CommandManager` use
`setRootCommand(subCommand)` in the constructor for the same behavior.

## 6. Cooldowns and Confirmations

```java
@SubCommandInfo(name = "reset", confirm = true, cooldownMillis = 60_000)
public class ResetSubCommand extends SubCommand { ... }
```

or with the builder:

```java
subCommand("reset").confirm().cooldown(Duration.ofMinutes(1)).executes(...).build();
```

- **Confirmation**: the first `/myplugin reset` only answers "Run the command again within 10s to confirm."
  Running the same command with the same arguments within `SubCommand.CONFIRMATION_WINDOW_MILLIS` executes it.
- **Cooldown**: per sender (players by UUID, others by name). The cooldown starts when the command actually
  runs; a blocked attempt answers "Please wait Xs before using this again."

## 7. Async Commands

Long-running work (database queries, web requests) should not block the server thread:

```java
@SubCommandInfo(name = "stats", async = true)
public class StatsSubCommand extends SubCommand { ... }
```

or `subCommand("stats").async()`. Permission, argument and cooldown checks still run on the main thread; only
`execute` runs asynchronously. Only use Bukkit API that is safe off the main thread there, and schedule back
with `Bukkit.getScheduler().runTask(plugin, ...)` to touch the world or players.

## 8. The Help Command

```java
registerSubCommand(new HelpCommand(getCommandName()));
```

- `/myplugin help [page]`: every command the sender may use (nested ones included), 10 per page. Commands
  the sender lacks permission for are hidden; player-only commands are hidden from the console.
- `/myplugin help user add`: detail view with syntax, description, aliases, permission, arguments,
  subcommands and notes (players only, cooldown, needs confirmation, runs asynchronously).
- Tab completion offers page numbers and the command tree.

Customize the look through the language file (`Messages.Player.Commands.HelpMessages.*` for the list,
`HelpDetails.*` for the detail view, see [section 14](#14-message-keys-reference)).

## 9. Command Lifecycle Hooks

```java
@Override
public void preInit() {
    // After the manager's setup(); lib, lgm and registry are set. Runs before permissions are registered.
}

@Override
public void postInit() {
    // After the whole command is registered.
    Bukkit.getScheduler().runTaskTimer(plugin, this::refreshCache, 0L, 1200L);
}
```

## 10. Error Handling and Logging

You rarely need `try/catch` in a command:

- Invalid input read through `CommandArgs` throws `CommandArgumentException`, which is turned into the matching
  localized message.
- Any other exception sends `Player.Commands.ErrorPerformingSubCommand` to the sender and is logged with its
  stack trace.

For your own messages, throw the same exception type:

```java
if (!warpManager.exists(name)) {
    throw new CommandArgumentException("Commands.Warp.Unknown", "%prefix% &cUnknown warp %value%.",
            Map.of("%value%", name));
}
```

Logging:

```java
CoolStuffLib.logError("Could not load warps", exception);          // file log + console, always safe
lib.writeToLog(Level.INFO, "Warp created", LogPrefix.COMMANDS, false); // file log only
```

`LogPrefix` is an enum; pick one of its constants (`COMMANDS`, `COOLSTUFFLIB_COMMANDS`, `ERROR`, ...).

## 11. Permission Management

- Default permission of a subcommand: `<main>.<path>` in lower case (`myplugin.user.add`).
- Auto-registered permissions default to OP, like any Bukkit permission without an explicit default.
- An empty permission (`permission = ""` or overriding `permissionAsString()` to return `""`) means everyone
  may use the command; the built-in `HelpCommand` works like that.
- `opRequired = true` lets operators use the command even without the permission.

For finer checks inside a command:

```java
if (!sender.hasPermission(permissionAsString() + ".others")) {
    sender.sendMessage(lgm.getMessage("Player.General.NoPermissions", null, true));
    return true;
}
```

## 12. Sender-Specific Behavior

`execute(sender, args)` handles every sender. To split by sender type, override the specific methods; they
all default to `execute`:

```java
@Override
public boolean onPlayerCommand(Player player, String[] args) { ... }

@Override
public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) { ... }

@Override
public boolean handleCommand(CommandSender sender, Player playerOrNull, String[] args) {
    // command blocks, RCON and other senders end up here
}
```

## 13. Command Unregistration

```java
CommandManager userManager = registry.getCommandManager("user");
if (userManager != null) registry.unregister(userManager);

registry.unregisterAll(); // e.g. in onDisable
```

`unregister` removes the command from the server's command map, removes its auto-registered permissions
(including those of nested subcommands) and refreshes the command list of online players. A new manager with
the same name can be registered afterwards.

## 14. Message Keys Reference

All keys live under `Messages.` in the language file. Keys marked "fallback" have an English default, so
older language files keep working.

| Key | Placeholders | Used for |
|---|---|---|
| `Player.Commands.InvalidSubCommand` | | unknown subcommand |
| `Player.Commands.UsageMessage` | `%usage%` | command returned `false` / wrong argument count |
| `Player.Commands.TooFewArguments` / `TooManyArguments` | | argument count |
| `Player.Commands.CommandContainsInvalidArgs` | `%invalidArgs%` | strict argument validation |
| `Player.Commands.ErrorPerformingSubCommand` | `%error%` | exception in a command |
| `Player.Commands.MissingArgument` (fallback) | `%argument%` | `CommandArgs` |
| `Player.Commands.NotANumber` | `%value%` | `CommandArgs` |
| `Player.Commands.NumberOutOfRange` (fallback) | `%value%`, `%min%`, `%max%` | `CommandArgs` |
| `Player.Commands.PlayerNotFound` / `WorldNotFound` (fallback) | `%value%` | `CommandArgs` |
| `Player.Commands.InvalidChoice` (fallback) | `%value%`, `%options%` | `CommandArgs` |
| `Player.Commands.OnCooldown` (fallback) | `%seconds%` | cooldowns |
| `Player.Commands.ConfirmCommand` (fallback) | `%seconds%` | confirmations |
| `Player.Commands.AliasFailed` (fallback) | `%command%` | command aliases |
| `Player.Commands.HelpMessages.Header/Format/Footer` | `%page%`, `%max_page%`, `%usage%`, `%description%` | help list |
| `Player.Commands.HelpPageDoesNotExist` | `%page%` | help list |
| `Player.Commands.HelpUnknownCommand` (fallback) | `%command%` | help details |
| `Player.Commands.HelpDetails.*` (fallback) | `%usage%`, `%description%`, `%aliases%`, `%permission%`, `%arguments%`, `%subcommands%`, `%notes%` | help details |
| `Console.ExecutesPlayerCommand` | | player-only command from console |
| `Player.General.NoPermissions` | `%permission%` | missing permission |

## 15. Best Practices

1. **Group commands**: `/myplugin user add` instead of `/adduser`. Nesting gives you sensible permissions and
   help for free.
2. **Declare arguments** instead of parsing `String[]` by hand; you get completion, usage and validation.
3. **Keep texts in the language file**: `Messages.Commands.<main>.<path>.Info` and `.Syntax` are picked up
   automatically.
4. **Guard destructive commands** with `confirm` and, where spamming hurts, a cooldown.
5. **Move slow work off the main thread** with `async`.
6. **Cache expensive completions**; custom completers run on every tab press.

For more information:
- [Command Aliases Tutorial](command-aliases-tutorial.md)
- [Cool Stuff Lib Tutorial](cool-stuff-lib-tutorial.md)
- [Language Manager Tutorial](LANGUAGE_MANAGER_TUTORIAL.md)
