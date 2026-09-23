# Command Manager System: Basic Tutorial

This tutorial walks you step by step through creating commands in your Bukkit/Spigot plugin with the
Command Manager System of **Cool Stuff Lib**.

## Table of Contents

1. [Introduction](#introduction)
2. [Setting Up with Cool Stuff Lib (Recommended)](#setting-up-with-cool-stuff-lib-recommended)
3. [Manual Setup (Alternative)](#manual-setup-alternative)
4. [Creating Your First Command Manager](#creating-your-first-command-manager)
5. [Creating a Simple Subcommand](#creating-a-simple-subcommand)
6. [Reading Arguments](#reading-arguments)
7. [Declaring Arguments for Tab Completion](#declaring-arguments-for-tab-completion)
8. [Registering Commands](#registering-commands)
9. [Adding a Help Command](#adding-a-help-command)
10. [The Quickest Way: Builder Commands](#the-quickest-way-builder-commands)
11. [Testing Your Commands](#testing-your-commands)

## Introduction

The Command Manager System gives your commands a structure: one main command (`/example`) with
subcommands (`/example info`), automatic permissions, argument checks, tab completion and a help page.

Key benefits:

- Organized command structure with main commands, subcommands and nested subcommands
- Automatic permission handling
- Typed argument parsing with localized error messages
- Tab completion and usage lines generated from declared arguments
- Much less boilerplate: a subcommand can be one annotation and one method

## Required Imports

```java
import de.happybavarian07.coolstufflib.CoolStuffLib;
import de.happybavarian07.coolstufflib.CoolStuffLibBuilder;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.CommandData;
import de.happybavarian07.coolstufflib.commandmanagement.CommandManager;
import de.happybavarian07.coolstufflib.commandmanagement.CommandManagerRegistry;
import de.happybavarian07.coolstufflib.commandmanagement.HelpCommand;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
```

## Setting Up with Cool Stuff Lib (Recommended)

The builder sets up the registry, language manager and data file in one place:

```java
public class MyPlugin extends JavaPlugin {
    private CoolStuffLib coolStuffLib;

    @Override
    public void onEnable() {
        File langFolder = new File(getDataFolder(), "lang");
        coolStuffLib = new CoolStuffLibBuilder(this)
                .setCommandManagerRegistry(new CommandManagerRegistry(this))
                .setLanguageManager(new LanguageManager(this, langFolder, "lang", "&7[MyPlugin] "))
                .setDataFile(new File(getDataFolder(), "data.yml"))
                .setUsePlayerLangHandler(true)
                .setSendSyntaxOnZeroArgs(true)
                .createCoolStuffLib();

        // Initializes all components. Throws with a list of failed services if something cannot start.
        coolStuffLib.setup();

        registerCommands();
    }

    @Override
    public void onDisable() {
        if (coolStuffLib != null && coolStuffLib.getCommandManagerRegistry() != null) {
            coolStuffLib.getCommandManagerRegistry().unregisterAll();
        }
    }

    private void registerCommands() {
        coolStuffLib.getCommandManagerRegistry().register(new ExampleCommandManager(this));
    }
}
```

## Manual Setup (Alternative)

Without the builder you create the registry yourself:

```java
commandRegistry = new CommandManagerRegistry(this);
commandRegistry.setLanguageManager(new LanguageManager(this, new File(getDataFolder(), "lang"), "lang", "&7[MyPlugin] "));
commandRegistry.setCommandManagerRegistryReady(true);
commandRegistry.register(new ExampleCommandManager(this));
```

Call `commandRegistry.unregisterAll()` in `onDisable()`.

## Creating Your First Command Manager

A `CommandManager` describes the main command and registers its subcommands in `setup()`:

```java
public class ExampleCommandManager extends CommandManager {
    private final JavaPlugin plugin;

    public ExampleCommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getCommandName() { return "example"; }

    @Override
    public String getCommandUsage() { return "/example <subcommand> [args]"; }

    @Override
    public String getCommandInfo() { return "Example command for demonstration"; }

    @Override
    public JavaPlugin getJavaPlugin() { return plugin; }

    @Override
    public List<String> getCommandAliases() { return List.of("ex", "exmp"); }

    @Override
    public String getCommandPermissionAsString() { return "myplugin.command.example"; }

    @Override
    public boolean autoRegisterPermission() { return true; }

    @Override
    public void setup() {
        // subcommands are registered here (step 8)
    }
}
```

The command does not need to be in `plugin.yml`; it is registered dynamically if it is missing there.

## Creating a Simple Subcommand

Put the metadata into `@SubCommandInfo` and implement only the logic:

```java
@SubCommandInfo(name = "info", aliases = {"i", "about"}, info = "Shows information about the plugin")
public class InfoSubCommand extends SubCommand {
    public InfoSubCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        sender.sendMessage("This is an example command!");
        return true; // false shows the usage message
    }
}
```

That is all. Everything you do not set is derived:

| Property | Default |
|---|---|
| Permission | `<main>.<name>` in lower case, e.g. `example.info` (auto-registered) |
| Description | `Messages.Commands.<main>.<name>.Info` from the language file, else empty |
| Syntax | `Messages.Commands.<main>.<name>.Syntax`, else generated, e.g. `/example info` |

Set `permission = "..."`, `syntax = "..."` or `autoRegisterPermission = false` in the annotation to
override. Sender and argument rules still come from `@CommandData`:

```java
@SubCommandInfo(name = "heal")
@CommandData(playerRequired = true)
public class HealSubCommand extends SubCommand { ... }
```

Overriding the old methods (`name()`, `info()`, `syntax()`, `onPlayerCommand(...)`, `subArgs(...)`, ...)
still works, so existing subcommands keep running unchanged.

## Reading Arguments

`CommandArgs` gives you typed access. Indexes start at 0 **after** the subcommand name:

```java
@Override
public boolean execute(CommandSender sender, CommandArgs args) {
    Player target = args.onlinePlayer(0);        // "Player Steve is not online!" if not found
    int amount = args.integer(1, 1, 1, 64);      // optional, default 1, must be 1..64
    GameMode mode = args.enumOf(2, GameMode.class);
    target.getInventory().addItem(new ItemStack(Material.DIAMOND, amount));
    return true;
}
```

Wrong input stops the command and sends a localized message (`Messages.Player.Commands.MissingArgument`,
`NotANumber`, `NumberOutOfRange`, `PlayerNotFound`, `WorldNotFound`, `InvalidChoice`). If your language
file does not have a key yet, an English default is used. No `try/catch` needed.

Other readers: `string(i)`, `string(i, default)`, `joined(from)`, `decimal(i)`, `bool(i)`, `world(i)`,
`has(i)`, `size()`, and `player()` for the sender as a player.

## Declaring Arguments for Tab Completion

Declare the arguments once; tab completion, the usage line and the argument count follow:

```java
@SubCommandInfo(name = "give")
public class GiveSubCommand extends SubCommand {
    public GiveSubCommand(String main) { super(main); }

    @Override
    public List<Argument> arguments() {
        return List.of(
                Argument.player("target"),
                Argument.choice("item", "diamond", "emerald"),
                Argument.integer("amount").optional());
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) { ... }
}
```

- `/example give <tab>` suggests online players, the next position `diamond`/`emerald`.
- The generated syntax is `/example give <target> <diamond|emerald> [amount]`: `<...>` required,
  `[...]` optional, short choice lists spelled out.
- At least 2 and at most 3 arguments are accepted (unless `@CommandData` sets `minArgs`/`maxArgs`).

Available argument types: `player`, `world`, `choice`, `enumOf`, `integer`, `decimal`, `word`,
`text` (all remaining words) and `custom(name, sender -> suggestions)`.

## Registering Commands

Register subcommands in the manager's `setup()`, then register the manager:

```java
@Override
public void setup() {
    registerSubCommand(new InfoSubCommand(getCommandName()));
    registerSubCommand(new GiveSubCommand(getCommandName()));
}
```

```java
coolStuffLib.getCommandManagerRegistry().register(new ExampleCommandManager(this));
```

## Adding a Help Command

```java
registerSubCommand(new HelpCommand(getCommandName()));
```

- `/example help [page]` lists every command the sender may use.
- `/example help give` shows the details of one command: syntax, description, aliases, permission,
  arguments, subcommands and notes (players only, cooldown, confirmation).

## The Quickest Way: Builder Commands

Small commands do not need classes at all:

```java
CommandManagerRegistry registry = coolStuffLib.getCommandManagerRegistry();

registry.command("heal")
        .permission("myplugin.heal")
        .arguments(Argument.player("target").optional())
        .player((player, args) -> {
            Player target = args.has(0) ? args.onlinePlayer(0) : player;
            target.setHealth(20);
        })
        .register();
```

Inside a `CommandManager`, `subCommand("name")` starts a builder for a subcommand:

```java
registerSubCommand(subCommand("ping")
        .executes((sender, args) -> { sender.sendMessage("Pong!"); return true; })
        .build());
```

## Testing Your Commands

1. Build and deploy your plugin, start the server.
2. Try:
   - `/example` shows the usage message
   - `/example help` lists the subcommands, `/example help info` shows details
   - `/example info` runs the subcommand
   - `/example give <your name> diamond abc` reports that `abc` is not a number

If something does not work:

1. Check the console for errors (command errors are logged with a stack trace).
2. Verify the permissions (`/example help info` shows the required one).
3. Make sure `coolStuffLib.setup()` ran before registering commands.
4. Check that subcommands are registered in `setup()`.

## Next Steps

- [Advanced Tutorial](command-system-advanced.md): nested subcommands, cooldowns, confirmations,
  async commands and more.
- [Command Aliases Tutorial](command-aliases-tutorial.md): command shortcuts, also created at runtime.
- [Cool Stuff Lib Tutorial](cool-stuff-lib-tutorial.md): the complete library.
