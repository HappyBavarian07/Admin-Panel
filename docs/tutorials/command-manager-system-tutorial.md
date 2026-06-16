e th# Command Manager System: Complete Tutorial

## Table of Contents

1. Introduction
2. Architecture Overview
3. Setting Up the Command System
4. Creating Command Managers
5. Subcommands and CommandData
6. Registering Commands
7. Help Command and Pagination
8. Tab Completion
9. Advanced Features
10. Error Handling & Logging
11. Permission Management
12. Sender-Specific Behavior
13. Example Workflows
14. Troubleshooting & Tips
15. References

---

## 1. Introduction

The Command Manager System in CoolStuffLib provides a robust framework for creating, managing, and extending commands in
Bukkit/Spigot plugins. It supports subcommands, permission management, tab completion, sender-specific logic, and
advanced error handling.

---

## 2. Architecture Overview

- **CommandManager**: Abstract base for main commands.
- **SubCommand**: Abstract base for subcommands.
- **CommandManagerRegistry**: Registers, manages, and unregisters command managers and their commands.
- **HelpCommand**: Built-in paginated help for subcommands.
- **CommandData**: Annotation for subcommand configuration.

---

## 3. Setting Up the Command System

### With CoolStuffLib Builder

```java
public class MyPlugin extends JavaPlugin {
    private CoolStuffLib coolStuffLib;

    @Override
    public void onEnable() {
        File dataFile = new File(getDataFolder(), "data.yml");
        File langFolder = new File(getDataFolder(), "lang");
        coolStuffLib = new CoolStuffLibBuilder(this)
                .setCommandManagerRegistry(new CommandManagerRegistry(this))
                .setLanguageManager(new LanguageManager(this, langFolder, "lang", "&7[MyPlugin] "))
                .setDataFile(dataFile)
                .setUsePlayerLangHandler(true)
                .setSendSyntaxOnZeroArgs(true)
                .createCoolStuffLib();
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
        coolStuffLib.getCommandManagerRegistry().register(new ExampleCommandManager());
    }
}
```

### Manual Setup

```java
public class MyPlugin extends JavaPlugin {
    private CommandManagerRegistry commandRegistry;

    @Override
    public void onEnable() {
        commandRegistry = new CommandManagerRegistry(this);
        File langFolder = new File(getDataFolder(), "lang");
        commandRegistry.setLanguageManager(new LanguageManager(this, langFolder, "lang", "&7[MyPlugin] "));
        commandRegistry.setCommandManagerRegistryReady(true);
    }

    @Override
    public void onDisable() {
        if (commandRegistry != null) {
            commandRegistry.unregisterAll();
        }
    }
}
```

## 3. Initialization with CoolStuffLibBuilder

The recommended way to set up the command manager system is via the builder pattern. This ensures all command managers and language integration are properly configured.

```java
CoolStuffLib lib = new CoolStuffLibBuilder(this)
    .withCommandManager()
        .enableSyntaxOnZeroArgs()
    .build()
    .createCoolStuffLib();
```

You can also chain the language manager setup for full integration:

```java
CoolStuffLib lib = new CoolStuffLibBuilder(this)
    .withLanguageManager()
        .setLanguageFolder(langFolder)
        .setResourceDirectory("languages")
        .setPrefix("&7[Prefix] ")
        .enablePlayerLanguageHandler()
    .build()
    .withCommandManager()
        .enableSyntaxOnZeroArgs()
    .build()
    .createCoolStuffLib();
```

---

## 4. Creating Command Managers

Extend `CommandManager` for your main command:

```java
public class ExampleCommandManager extends CommandManager {
    public String getCommandName() {
        return "example";
    }

    public String getCommandUsage() {
        return "/example <subcommand> [args]";
    }

    public String getCommandInfo() {
        return "Example command for demonstration";
    }

    public JavaPlugin getJavaPlugin() {
        return MyPlugin.getInstance();
    }

    public List<String> getCommandAliases() {
        return Arrays.asList("ex", "exmp");
    }

    public String getCommandPermissionAsString() {
        return "myplugin.command.example";
    }

    public boolean autoRegisterPermission() {
        return true;
    }

    public void setup() {
        registerSubCommand(new InfoSubCommand(getCommandName()));
        registerSubCommand(new HelpCommand(getCommandName()));
    }
}
```

---

## 5. Subcommands and CommandData

Extend `SubCommand` for each subcommand. Use `@CommandData` for configuration:

```java

@CommandData(playerRequired = false, minArgs = 0, maxArgs = 1)
public class InfoSubCommand extends SubCommand {
    public InfoSubCommand(String mainCommandName) {
        super(mainCommandName);
    }

    public boolean onPlayerCommand(Player player, String[] args) {
        player.sendMessage("This is an example command!");
        if (args.length > 0) player.sendMessage("You provided: " + args[0]);
        return true;
    }

    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        sender.sendMessage("This is an example command!");
        if (args.length > 0) sender.sendMessage("You provided: " + args[0]);
        return true;
    }

    public String name() {
        return "info";
    }

    public String info() {
        return "Shows information about the plugin";
    }

    public String[] aliases() {
        return new String[]{"i", "about"};
    }

    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        Map<Integer, String[]> map = new HashMap<>();
        map.put(1, new String[]{"version", "author", "website"});
        return map;
    }

    public String syntax() {
        return "/" + mainCommandName + " info [type]";
    }

    public String permissionAsString() {
        return "myplugin.command.example.info";
    }

    public boolean autoRegisterPermission() {
        return true;
    }
}
```

---

## 6. Registering Commands

Register your command manager in your plugin:

```java
coolStuffLib.getCommandManagerRegistry().

register(new ExampleCommandManager());
```

---

## 7. Help Command and Pagination

Add the built-in `HelpCommand` to your command manager for paginated help:

```java
registerSubCommand(new HelpCommand(getCommandName()));
```

You can extend `HelpCommand` for custom formatting and pagination.

---

## 8. Tab Completion

Tab completion is automatic based on the `subArgs` method in each subcommand. Suggestions are context-aware and can be
sender-specific.

---

## 9. Advanced Features

- **Dynamic Tab Completion**: Generate suggestions based on arguments and sender type.
- **Complex Subcommand Hierarchies**: Organize commands with multiple levels and argument dependencies.
- **Menu Integration**: Open menus from commands using PlayerMenuUtility.
- **Language-Aware Messages**: Use LanguageManager for localized feedback and placeholders.
- **Lifecycle Hooks**: Use `preInit` and `postInit` in subcommands for setup and registration logic.

---

## 10. Error Handling & Logging

Use CoolStuffLib's logging system for robust error handling:

```java
import de.happybavarian07.coolstufflib.CoolStuffLib;

import java.util.logging.Level;
try{
        // Command logic
        }catch(Exception e){
        CoolStuffLib.

getLib().

writeToLog(Level.SEVERE, "Error executing command: "+e.getMessage(),LogPrefix.ERROR,true);
        player.

sendMessage(CoolStuffLib.getLib().

getLanguageManager().

getMessage("commands.execution-error",player, true));
        }
```

---

## 11. Permission Management

- Use `permissionAsString()` and `autoRegisterPermission()` for automatic permission registration.
- Register action-specific permissions in `postInit` if needed.

---

## 12. Sender-Specific Behavior

- Use `@CommandData(senderTypeSpecificSubArgs = true)` and logic in `subArgs` to provide different tab completions and
  behaviors for players vs. console.

---

## 13. Example Workflows

- Simple command manager with info and help subcommands
- Admin command manager with multiple administrative subcommands
- Player management commands with dynamic tab completion
- Economy and world management commands with hierarchical subcommands

---

## 14. Troubleshooting & Tips

- Ensure command registry is initialized and ready
- Register all subcommands in the `setup()` method
- Use the built-in help command for user guidance
- Check permissions and argument constraints
- Use lifecycle hooks for initialization and cleanup

---

## 15. References

- [CoolStuffLib API Docs](../apidocs/index.html)
- [Basic Tutorial](command-system-basic.md)
- [Advanced Tutorial](command-system-advanced.md)
- [Implementation Examples](command-system-examples.md)

---

This tutorial covers all major aspects of the CoolStuffLib Command Manager System, including setup, subcommands,
registration, help, tab completion, advanced features, error handling, and permission management. For further details,
consult the API documentation or source code.
