# Command Manager System: Basic Tutorial

This tutorial will guide you through the basics of using the Command Manager System to create and manage commands in your Bukkit/Spigot plugin using the **Cool Stuff Lib**.

## Table of Contents

1. [Introduction](#introduction)
2. [Setting Up with Cool Stuff Lib (Recommended)](#setting-up-with-cool-stuff-lib-recommended)
3. [Manual Setup (Alternative)](#manual-setup-alternative)
4. [Creating Your First Command Manager](#creating-your-first-command-manager)
5. [Creating a Simple Subcommand](#creating-a-simple-subcommand)
6. [Registering Commands](#registering-commands)
7. [Adding a Help Command](#adding-a-help-command)
8. [Basic Tab Completion](#basic-tab-completion)
9. [Testing Your Commands](#testing-your-commands)

## Introduction

The Command Manager System is a framework that simplifies the creation and management of commands in Bukkit/Spigot plugins. It provides a structured approach to command handling with features like subcommands, permission management, and tab completion.

Key benefits of using this system:

- Organized command structure with main commands and subcommands
- Automatic permission handling
- Built-in tab completion
- Separation of player and console command handling
- Reduced boilerplate code

## Required Imports

For the examples in this tutorial, you'll need these imports:

```java
import de.happybavarian07.coolstufflib.CoolStuffLib;
import de.happybavarian07.coolstufflib.CoolStuffLibBuilder;
import de.happybavarian07.coolstufflib.commandmanagement.CommandManager;
import de.happybavarian07.coolstufflib.commandmanagement.CommandManagerRegistry;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import de.happybavarian07.coolstufflib.commandmanagement.CommandData;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
```

## Setting Up with Cool Stuff Lib (Recommended)

The **Cool Stuff Lib** provides a streamlined way to set up the Command Manager Registry using the Builder pattern. This is the preferred approach as it handles initialization automatically and provides additional features.

```java
public class MyPlugin extends JavaPlugin {
    private CoolStuffLib coolStuffLib;
    
    @Override
    public void onEnable() {
        // Create data file for persistent storage
        File dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Initialize Cool Stuff Lib using the Builder pattern
        File langFolder = new File(getDataFolder(), "lang");
        coolStuffLib = new CoolStuffLibBuilder(this)
                .setCommandManagerRegistry(new CommandManagerRegistry(this))
                .setLanguageManager(new LanguageManager(this, langFolder, "lang", "&7[MyPlugin] "))
                .setDataFile(dataFile)
                .setUsePlayerLangHandler(true)
                .setSendSyntaxOnZeroArgs(true)
                .createCoolStuffLib();
        
        // Setup the library (this initializes all components)
        coolStuffLib.setup();
        
        // Register your commands after setup
        registerCommands();
    }
    
    @Override
    public void onDisable() {
        // Unregister all commands when the plugin is disabled
        if (coolStuffLib != null && coolStuffLib.getCommandManagerRegistry() != null) {
            coolStuffLib.getCommandManagerRegistry().unregisterAll();
        }
    }
    
    private void registerCommands() {
        // We'll implement this method later
        coolStuffLib.getCommandManagerRegistry().register(new ExampleCommandManager());
    }
    
    public CoolStuffLib getCoolStuffLib() {
        return coolStuffLib;
    }
    
    public CommandManagerRegistry getCommandRegistry() {
        return coolStuffLib.getCommandManagerRegistry();
    }
}
```

## Manual Setup (Alternative)

If you prefer not to use the Cool Stuff Lib Builder, you can still set up the Command Manager Registry manually:

```java
public class MyPlugin extends JavaPlugin {
    private CommandManagerRegistry commandRegistry;
    
    @Override
    public void onEnable() {
        // Initialize the CommandManagerRegistry
        commandRegistry = new CommandManagerRegistry(this);
        
        // Set the language manager (if you're using one)
        File langFolder = new File(getDataFolder(), "lang");
        commandRegistry.setLanguageManager(new LanguageManager(this, langFolder, "lang", "&7[MyPlugin] "));
        
        // Mark the registry as ready
        commandRegistry.setCommandManagerRegistryReady(true);
        
        // Register commands (we'll do this later)
        
        // Store the registry for later use
        // You might want to create a getter method for this
    }
    
    @Override
    public void onDisable() {
        // Unregister all commands when the plugin is disabled
        if (commandRegistry != null) {
            commandRegistry.unregisterAll();
        }
    }
    
    public CommandManagerRegistry getCommandRegistry() {
        return commandRegistry;
    }
}
```

## Creating Your First Command Manager

Next, create a class that extends `CommandManager` to handle your main command:

```java
public class ExampleCommandManager extends CommandManager {
    @Override
    public String getCommandName() {
        return "example";  // The main command name
    }

    @Override
    public String getCommandUsage() {
        return "/example <subcommand> [args]";  // How to use the command
    }

    @Override
    public String getCommandInfo() {
        return "Example command for demonstration";  // Description
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return MyPlugin.getInstance();  // Your plugin instance
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("ex", "exmp");  // Command aliases
    }

    @Override
    public String getCommandPermissionAsString() {
        return "myplugin.command.example";  // Permission node
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;  // Automatically register the permission
    }

    @Override
    public void setup() {
        // We'll register subcommands here later
    }
}
```

## Creating a Simple Subcommand

Now, create a subcommand by extending the `SubCommand` class:

```java
@CommandData(
    playerRequired = false,  // Can be used by console
    opRequired = false,      // Doesn't require op
    minArgs = 0,             // Minimum arguments
    maxArgs = 1              // Maximum arguments
)
public class InfoSubCommand extends SubCommand {
    public InfoSubCommand(String mainCommandName) {
        super(mainCommandName);  // Pass the main command name
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        // Handle player command execution
        player.sendMessage("This is an example command!");
        
        if (args.length > 0) {
            player.sendMessage("You provided: " + args[0]);
        }
        
        return true;  // Command executed successfully
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        // Handle console command execution
        sender.sendMessage("This is an example command!");
        
        if (args.length > 0) {
            sender.sendMessage("You provided: " + args[0]);
        }
        
        return true;  // Command executed successfully
    }

    @Override
    public String name() {
        return "info";  // Subcommand name
    }

    @Override
    public String info() {
        return "Shows information about the plugin";  // Description
    }

    @Override
    public String[] aliases() {
        return new String[]{"i", "about"};  // Subcommand aliases
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        // Define valid arguments for tab completion
        Map<Integer, String[]> map = new HashMap<>();
        map.put(1, new String[]{"version", "author", "website"});
        return map;
    }

    @Override
    public String syntax() {
        return "/" + mainCommandName + " info [type]";  // Command syntax
    }

    @Override
    public String permissionAsString() {
        return "myplugin.command.example.info";  // Permission node
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;  // Automatically register the permission
    }
}
```

## Registering Commands

Now, update your `CommandManager` to register the subcommand:

```java
@Override
public void setup() {
    // Register the info subcommand
    registerSubCommand(new InfoSubCommand(getCommandName()));
}
```

And register the command manager in your plugin's `registerCommands` method (if using Cool Stuff Lib) or `onEnable` method (if using manual setup):

**With Cool Stuff Lib:**
```java
private void registerCommands() {
    // Register command managers
    coolStuffLib.getCommandManagerRegistry().register(new ExampleCommandManager());
}
```

**With Manual Setup:**
```java
@Override
public void onEnable() {
    // ... previous code ...
    
    // Register command managers
    commandRegistry.register(new ExampleCommandManager());
}
```

## Adding a Help Command

The system includes a built-in `HelpCommand` that displays all available subcommands. Add it to your command manager:

```java
@Override
public void setup() {
    // Register the info subcommand
    registerSubCommand(new InfoSubCommand(getCommandName()));
    
    // Register the help command
    registerSubCommand(new HelpCommand(getCommandName()));
}
```

The `HelpCommand` will automatically list all subcommands that the user has permission to use, with pagination support.

## Basic Tab Completion

Tab completion is handled automatically based on the `subArgs` method in your subcommands. The system will suggest:

1. Subcommand names when the user types the main command
2. Arguments defined in the `subArgs` method when the user types a subcommand

For example, with our `InfoSubCommand`, typing `/example info <tab>` will suggest "version", "author", and "website".

## Testing Your Commands

After implementing your commands, you can test them in-game:

1. Build and deploy your plugin
2. Start your server
3. Try your commands:
   - `/example` should show a usage message
   - `/example help` should list available subcommands
   - `/example info` should show the info message
   - `/example info version` should show the version info

If you encounter issues:

1. Check the console for error messages
2. Verify that permissions are set correctly
3. Ensure that the command registry is properly initialized
4. Check that subcommands are registered in the `setup()` method

## Benefits of Using Cool Stuff Lib

By using the Cool Stuff Lib Builder approach, you get several additional benefits:

- **Automatic Language Manager Integration**: Multi-language support out of the box
- **Per-Player Language Handling**: Different languages for different players
- **Integrated Logging**: Built-in file logging capabilities
- **Menu System Integration**: Seamless integration with the menu system
- **Simplified Setup**: Builder pattern reduces boilerplate code
- **Centralized Configuration**: All components configured in one place

## Next Steps

Now that you've created a basic command with the Command Manager System, you can:

1. Add more subcommands to expand functionality
2. Customize the help command
3. Implement more complex argument handling
4. Add sender-specific behavior
5. Explore the full Cool Stuff Lib features

For advanced features, check out the [Advanced Tutorial](command-system-advanced.md).
For a comprehensive guide to Cool Stuff Lib, see the [Cool Stuff Lib Tutorial](cool-stuff-lib-tutorial.md).