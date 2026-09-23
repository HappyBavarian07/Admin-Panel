# Command Manager System: Overview

This page explains how the parts of the Command Manager System fit together and where each topic is covered
step by step.

## Table of Contents

1. Introduction
2. Architecture Overview
3. How a Command Is Executed
4. Setting Up the Command System
5. Three Ways to Write a Command
6. Feature Map
7. Troubleshooting & Tips
8. References

---

## 1. Introduction

The Command Manager System in CoolStuffLib gives Bukkit/Spigot plugins structured commands: a main command
with (nested) subcommands, permissions, typed arguments, tab completion, help pages, cooldowns,
confirmations, async execution and command aliases.

---

## 2. Architecture Overview

| Type | Role |
|---|---|
| `CommandManagerRegistry` | Registers managers with the server (plugin.yml or dynamically), routes execution and tab completion, unregisters them |
| `CommandManager` | One main command (`/example`); holds subcommands and an optional root command |
| `SubCommand` | One subcommand (`/example info`); can have child subcommands |
| `@SubCommandInfo` | Metadata of a subcommand: name, aliases, info, syntax, permission, cooldown, confirm, async |
| `@CommandData` | Sender and argument rules: player/op required, min/max arguments, strict arguments |
| `Argument` | Declared positional argument: drives completion, usage line and argument count |
| `CommandArgs` | Typed access to the arguments in `execute`; reports invalid input automatically |
| `SubCommandBuilder` / `CommandBuilder` | Build subcommands / whole commands from lambdas |
| `HelpCommand` | Built-in `/<main> help [page]` and `/<main> help <command>` |
| `CommandTemplate`, `CommandAliasManager`, `AliasStore` | Command aliases (shortcuts), also created at runtime |

---

## 3. How a Command Is Executed

For `/example user add Steve`:

1. The registry finds the manager for `example`.
2. The manager resolves the subcommand `user`, then walks into its child `add`. If no subcommand matches
   and a root command exists, the root command handles all arguments.
3. Checks, in order: permission (or op with `opRequired`), player-only, argument count (from `@CommandData`
   or the declared arguments), strict argument values, confirmation, cooldown.
4. `execute(sender, args)` runs, on the main thread or asynchronously if the command is `async`.
5. Returning `false` shows the usage line. Invalid input read through `CommandArgs` and unexpected exceptions
   are reported to the sender; exceptions are also logged.

---

## 4. Setting Up the Command System

```java
coolStuffLib = new CoolStuffLibBuilder(this)
        .withLanguageManager()
            .setLanguageFolder(new File(getDataFolder(), "languages"))
            .setResourceDirectory("languages")
            .setPrefix("&7[MyPlugin] ")
        .build()
        .withCommandManager()
            .enableSyntaxOnZeroArgs()
        .build()
        .createCoolStuffLib();
coolStuffLib.setup();

CommandManagerRegistry registry = coolStuffLib.getCommandManagerRegistry();
registry.register(new ExampleCommandManager(this));
```

Call `registry.unregisterAll()` in `onDisable()`. Manual setup without the builder is shown in the
[Basic Tutorial](command-system-basic.md#manual-setup-alternative).

---

## 5. Three Ways to Write a Command

**Annotated subcommand class** — for real logic:

```java
@SubCommandInfo(name = "info", aliases = "i")
public class InfoSubCommand extends SubCommand {
    public InfoSubCommand(String main) { super(main); }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        sender.sendMessage("Example plugin v1.0");
        return true;
    }
}
```

**Builder subcommand** — for small commands inside a `CommandManager.setup()`:

```java
registerSubCommand(subCommand("ping").executes((sender, args) -> { sender.sendMessage("Pong!"); return true; }).build());
```

**Builder command** — a whole command without any class:

```java
registry.command("heal").player((player, args) -> player.setHealth(20)).register();
```

The classic style (overriding `name()`, `info()`, `syntax()`, `onPlayerCommand(...)`, `subArgs(...)`, ...)
keeps working.

---

## 6. Feature Map

| Feature | Where |
|---|---|
| First command, subcommands, registration | [Basic Tutorial](command-system-basic.md) |
| Typed arguments and declared arguments | [Basic Tutorial](command-system-basic.md#reading-arguments) |
| Nested subcommands, root commands | [Advanced Tutorial](command-system-advanced.md#3-nested-subcommands) |
| Cooldowns, confirmations, async commands | [Advanced Tutorial](command-system-advanced.md#6-cooldowns-and-confirmations) |
| Help list and detail view | [Advanced Tutorial](command-system-advanced.md#8-the-help-command) |
| Message keys | [Advanced Tutorial](command-system-advanced.md#14-message-keys-reference) |
| Command aliases / shortcuts | [Command Aliases Tutorial](command-aliases-tutorial.md) |
| Running commands from menu buttons | [Menu Tutorial](menu-system-tutorial.md#7-navigation-chat-prompts-and-commands) |
| Full plugin examples | [Implementation Examples](command-system-examples.md) |

---

## 7. Troubleshooting & Tips

- **Command does nothing / unknown command**: make sure `coolStuffLib.setup()` ran before `register(...)`.
- **"You do not have access"**: check the permission shown by `/<main> help <command>`; auto-registered
  permissions default to OP.
- **Subcommand never runs**: it must be registered in the manager's `setup()` (or before registering the manager).
- **Tab completion missing**: declare `arguments()` or override `subArgs`; positions start at 1 in `subArgs`.
- **Messages show "null path: ..."**: add the key to your language file; see the message keys reference.

---

## 8. References

- [Basic Tutorial](command-system-basic.md)
- [Advanced Tutorial](command-system-advanced.md)
- [Command Aliases Tutorial](command-aliases-tutorial.md)
- [Implementation Examples](command-system-examples.md)
- [Cool Stuff Lib Tutorial](cool-stuff-lib-tutorial.md)
