# Command Manager System: Advanced Tutorial

This tutorial covers advanced features and techniques for the Command Manager System using **Cool Stuff Lib**. Before proceeding, make sure you've completed the [Basic Tutorial](command-system-basic.md) and [Cool Stuff Lib Tutorial](cool-stuff-lib-tutorial.md).

## Table of Contents

1. [Cool Stuff Lib Integration](#cool-stuff-lib-integration)
2. [Advanced Command Data Configuration](#advanced-command-data-configuration)
3. [Complex Subcommand Structures](#complex-subcommand-structures)
4. [Dynamic Tab Completion](#dynamic-tab-completion)
5. [Custom Help Commands](#custom-help-commands)
6. [Command Lifecycle Hooks](#command-lifecycle-hooks)
7. [Error Handling and Logging](#error-handling-and-logging)
8. [Permission Management](#permission-management)
9. [Sender-Specific Behavior](#sender-specific-behavior)
10. [Command Unregistration](#command-unregistration)
11. [Best Practices](#best-practices)

## Required Imports

For the advanced examples in this tutorial, you'll need these imports:

```java
import de.happybavarian07.coolstufflib.CoolStuffLib;
import de.happybavarian07.coolstufflib.commandmanagement.CommandManager;
import de.happybavarian07.coolstufflib.commandmanagement.CommandManagerRegistry;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import de.happybavarian07.coolstufflib.commandmanagement.CommandData;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.utils.LogPrefix;
import java.util.logging.Level;
import java.util.Arrays;
```

## Cool Stuff Lib Integration

When using the Command Manager System with Cool Stuff Lib, you get additional benefits and simplified access to other components. Here's how to leverage these features in advanced scenarios:

### Accessing Cool Stuff Lib Components in Commands

```java
@Override
public boolean onPlayerCommand(Player player, String[] args) {
    CoolStuffLib lib = CoolStuffLib.getLib();
    
    // Access Language Manager for localized messages
    LanguageManager langManager = lib.getLanguageManager();
    String message = langManager.getMessage("commands.advanced.success", player, true);
    
    // Access Plugin File Logger for detailed logging
    lib.writeToLog(Level.INFO, "Player " + player.getName() + " executed advanced command", LogPrefix.INFO, false);
    
    // Check if PlaceholderAPI is available
    if (lib.isPlaceholderAPIEnabled()) {
        // Use PlaceholderAPI features
        message = PlaceholderAPI.setPlaceholders(player, message);
    }
    
    player.sendMessage(message);
    return true;
}
```

### Language-Aware Command Messages

```java
@CommandData(playerRequired = true, minArgs = 1, maxArgs = 2)
public class AdvancedSubCommand extends SubCommand {
    
    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        LanguageManager langManager = CoolStuffLib.getLib().getLanguageManager();
        
        // Use placeholders in localized messages
        langManager.addPlaceholder(PlaceholderType.MESSAGE, "%player%", player.getName(), false);
        langManager.addPlaceholder(PlaceholderType.MESSAGE, "%arg1%", args[0], false);
        
        String successMessage = langManager.getMessage("commands.advanced.executed", player, true);
        
        // Reset placeholders after use
        langManager.resetPlaceholders(PlaceholderType.MESSAGE, null);
        
        player.sendMessage(successMessage);
        return true;
    }
    
    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        // Console gets default language
        LanguageManager langManager = CoolStuffLib.getLib().getLanguageManager();
        String message = langManager.getMessage("commands.advanced.console-executed", null, true);
        sender.sendMessage(message);
        return true;
    }
}
```

### Menu Integration in Commands

```java
@Override
public boolean onPlayerCommand(Player player, String[] args) {
    if (args.length > 0 && args[0].equalsIgnoreCase("menu")) {
        // Open a menu using Cool Stuff Lib's menu system
        PlayerMenuUtility menuUtility = CoolStuffLib.getLib().getPlayerMenuUtility(player.getUniqueId());
        new AdvancedConfigMenu(menuUtility).open();
        return true;
    }
    
    // Regular command logic
    return false;
}
```

## Advanced Command Data Configuration

The `@CommandData` annotation provides several advanced configuration options:

```java
@CommandData(
    playerRequired = true,                         // Only players can use this command
    opRequired = true,                             // Requires operator status
    minArgs = 2,                                   // Minimum 2 arguments
    maxArgs = 5,                                   // Maximum 5 arguments
    allowOnlySubCommandArgsThatFitToSubArgs = true, // Strict argument validation
    senderTypeSpecificSubArgs = true               // Different tab completion for players/console
)
public class AdvancedSubCommand extends SubCommand {
    // Implementation
}
```

### Strict Argument Validation

When `allowOnlySubCommandArgsThatFitToSubArgs` is set to `true`, the system will only allow arguments that are defined in the `subArgs` method. This is useful for commands with a fixed set of valid arguments.

### Sender-Specific Arguments

When `senderTypeSpecificSubArgs` is set to `true`, the `subArgs` method can provide different tab completions based on whether the sender is a player or console:

```java
@Override
public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
    Map<Integer, String[]> map = new HashMap<>();
    
    if (isPlayer == 1) {
        // Player-specific options
        map.put(1, new String[]{"inventory", "location", "health"});
    } else {
        // Console-specific options
        map.put(1, new String[]{"broadcast", "reload", "stop"});
    }
    
    return map;
}
```

## Complex Subcommand Structures

For more complex commands, you can create a hierarchy of subcommands:

```java
public class UserCommandManager extends CommandManager {
    @Override
    public void setup() {
        registerSubCommand(new UserCreateSubCommand(getCommandName()));
        registerSubCommand(new UserDeleteSubCommand(getCommandName()));
        registerSubCommand(new UserInfoSubCommand(getCommandName()));
        registerSubCommand(new UserListSubCommand(getCommandName()));
        registerSubCommand(new UserPermissionSubCommand(getCommandName()));
        registerSubCommand(new HelpCommand(getCommandName()));
    }
    
    // Other required methods
}
```

Each subcommand can have its own complex argument structure:

```java
@CommandData(minArgs = 2, maxArgs = 3)
public class UserPermissionSubCommand extends SubCommand {
    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        Map<Integer, String[]> map = new HashMap<>();
        
        // First argument: action
        map.put(1, new String[]{"add", "remove", "check"});
        
        // Second argument: username (dynamic)
        if (args.length > 0) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            map.put(2, players.toArray(new String[0]));
        }
        
        // Third argument: permission (depends on first argument)
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                map.put(3, new String[]{"myplugin.user", "myplugin.admin", "myplugin.moderator"});
            }
        }
        
        return map;
    }
    
    // Other required methods
}
```

## Dynamic Tab Completion

For more advanced tab completion, you can dynamically generate suggestions based on the current state:

```java
@Override
public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
    Map<Integer, String[]> map = new HashMap<>();
    
    // First argument: entity type
    map.put(1, new String[]{"player", "mob", "item"});
    
    // Second argument depends on first argument
    if (args.length > 0) {
        if (args[0].equalsIgnoreCase("player")) {
            // List online players
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            map.put(2, players.toArray(new String[0]));
        } else if (args[0].equalsIgnoreCase("mob")) {
            // List mob types
            map.put(2, new String[]{"zombie", "skeleton", "creeper", "spider"});
        } else if (args[0].equalsIgnoreCase("item")) {
            // List item types
            map.put(2, new String[]{"diamond_sword", "iron_pickaxe", "golden_apple"});
        }
    }
    
    // Third argument depends on first and second arguments
    if (args.length > 1) {
        if (args[0].equalsIgnoreCase("player")) {
            map.put(3, new String[]{"teleport", "give", "heal", "kill"});
        } else if (args[0].equalsIgnoreCase("mob")) {
            map.put(3, new String[]{"spawn", "remove", "count"});
        } else if (args[0].equalsIgnoreCase("item")) {
            map.put(3, new String[]{"give", "drop", "count"});
        }
    }
    
    return map;
}
```

## Custom Help Commands

You can create a custom help command by extending the built-in `HelpCommand`:

```java
public class CustomHelpCommand extends HelpCommand {
    public CustomHelpCommand(String mainCommandName) {
        super(mainCommandName);
    }
    
    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        // Custom header
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "My Plugin Help" + ChatColor.GOLD + " ===");
        
        // Call the parent method to display the command list
        boolean result = super.onPlayerCommand(player, args);
        
        // Custom footer
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Page " + 
                           getCurrentPage() + "/" + getTotalPages() + ChatColor.GOLD + " ===");
        
        return result;
    }
    
    @Override
    public int getItemsPerPage() {
        return 5;  // Custom number of items per page
    }
    
    @Override
    public String getSortMethod() {
        return "subcommand";  // Sort by subcommand name
    }
}
```

Then register your custom help command instead of the default one:

```java
@Override
public void setup() {
    // Register other subcommands
    
    // Register custom help command
    registerSubCommand(new CustomHelpCommand(getCommandName()));
}
```

## Command Lifecycle Hooks

The `SubCommand` class provides lifecycle hooks that you can override:

```java
@Override
public void preInit() {
    // Called before the command is registered
    // Use this for initialization that must happen before registration
    getLogger().info("Initializing " + name() + " subcommand");
    
    // Load configuration
    loadConfig();
}

@Override
public void postInit() {
    // Called after the command is registered
    // Use this for initialization that depends on registration
    getLogger().info(name() + " subcommand registered with permission: " + permissionAsString());
    
    // Schedule tasks
    Bukkit.getScheduler().runTaskTimer(getPlugin(), this::updateCache, 0L, 1200L);
}
```

## Error Handling and Logging

Implement robust error handling in your commands using Cool Stuff Lib's logging system:

```java
@Override
public boolean onPlayerCommand(Player player, String[] args) {
    try {
        // Command logic
        if (args[0].equalsIgnoreCase("create")) {
            return handleCreate(player, args);
        } else if (args[0].equalsIgnoreCase("delete")) {
            return handleDelete(player, args);
        } else {
            // Use language manager for error messages
            LanguageManager langManager = CoolStuffLib.getLib().getLanguageManager();
            String errorMessage = langManager.getMessage("commands.unknown-action", player, true);
            player.sendMessage(errorMessage);
            return false;
        }
    } catch (Exception e) {
        // Log the error using Cool Stuff Lib's file logger
        CoolStuffLib.getLib().writeToLog(
            Level.SEVERE, 
            "Error executing command by " + player.getName() + ": " + e.getMessage(), 
            LogPrefix.ERROR, 
            true
        );
        e.printStackTrace();
        
        // Inform the player with localized message
        LanguageManager langManager = CoolStuffLib.getLib().getLanguageManager();
        String errorMessage = langManager.getMessage("commands.execution-error", player, true);
        player.sendMessage(errorMessage);
        
        return false;
    }
}

private boolean handleCreate(Player player, String[] args) {
    try {
        // Creation logic here
        
        // Log successful operation
        CoolStuffLib.getLib().writeToLog(
            Level.INFO, 
            "Player " + player.getName() + " created item: " + args[1], 
            LogPrefix.INFO, 
            false
        );
        
        return true;
    } catch (Exception e) {
        // Specific error handling for creation
        CoolStuffLib.getLib().writeToLog(
            Level.WARNING, 
            "Failed to create item for " + player.getName() + ": " + e.getMessage(), 
            LogPrefix.WARNING, 
            true
        );
        
        LanguageManager langManager = CoolStuffLib.getLib().getLanguageManager();
        String errorMessage = langManager.getMessage("commands.create.failed", player, true);
        player.sendMessage(errorMessage);
        
        return false;
    }
}
```

### Using Log Prefixes for Commands

```java
public class AdvancedSubCommand extends SubCommand {
    
    @Override
    public void preInit() {
        CoolStuffLib.getLib().writeToLog(
            Level.INFO, 
            "Initializing " + name() + " subcommand", 
            LogPrefix.COOLSTUFFLIB_COMMANDS, 
            false
        );
    }
    
    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        // Log command usage
        CoolStuffLib.getLib().writeToLog(
            Level.FINE, 
            "Player " + player.getName() + " used command: " + name() + " with args: " + Arrays.toString(args), 
            LogPrefix.COMMANDS, 
            false
        );
        
        // Command logic...
        return true;
    }
}
```

## Permission Management

For complex permission structures, you can implement custom permission checking:

```java
@Override
public boolean onPlayerCommand(Player player, String[] args) {
    // Basic permission check is done automatically by the system
    
    // Additional permission checks for specific actions
    if (args.length > 0) {
        String action = args[0].toLowerCase();
        String actionPermission = permissionAsString() + "." + action;
        
        if (!player.hasPermission(actionPermission)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to " + action);
            return true;
        }
    }
    
    // Command logic
    // ...
    
    return true;
}
```

You can also register these permissions automatically:

```java
@Override
public void postInit() {
    // Register action-specific permissions
    String[] actions = {"create", "delete", "modify", "view"};
    
    for (String action : actions) {
        String permName = permissionAsString() + "." + action;
        Permission perm = new Permission(
            permName,
            "Allows " + action + " access to " + name() + " command",
            PermissionDefault.OP
        );
        
        if (!permissionExistsAlready(perm)) {
            Bukkit.getPluginManager().addPermission(perm);
        }
    }
}

private boolean permissionExistsAlready(Permission permission) {
    for (Permission p : Bukkit.getPluginManager().getPermissions()) {
        if (p.getName().equalsIgnoreCase(permission.getName())) {
            return true;
        }
    }
    return false;
}
```

## Sender-Specific Behavior

For commands that behave differently based on the sender type:

```java
@Override
public boolean onPlayerCommand(Player player, String[] args) {
    // Player-specific implementation
    // Can access player-specific methods like getLocation()
    Location playerLocation = player.getLocation();
    
    // Command logic using player context
    // ...
    
    return true;
}

@Override
public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
    // Console-specific implementation
    // Cannot access player-specific methods
    
    if (args.length < 2) {
        sender.sendMessage("Console must specify a player name as the second argument");
        return false;
    }
    
    Player targetPlayer = Bukkit.getPlayer(args[1]);
    if (targetPlayer == null) {
        sender.sendMessage("Player not found: " + args[1]);
        return false;
    }
    
    // Command logic using console context
    // ...
    
    return true;
}
```

## Command Unregistration

For plugins that need to dynamically register and unregister commands:

```java
public void registerCommands() {
    commandRegistry.register(new UserCommandManager());
    commandRegistry.register(new AdminCommandManager());
}

public void unregisterUserCommands() {
    CommandManager userManager = commandRegistry.getCommandManager("user");
    if (userManager != null) {
        commandRegistry.unregister(userManager);
    }
}

public void reloadCommands() {
    // Unregister all commands
    commandRegistry.unregisterAll();
    
    // Register commands again
    registerCommands();
}
```

## Best Practices

### 1. Command Structure

Organize your commands logically:

```
/myplugin user add <username>
/myplugin user remove <username>
/myplugin user list
/myplugin admin reload
/myplugin admin debug
```

Instead of:

```
/adduser <username>
/removeuser <username>
/listusers
/reloadplugin
/debugplugin
```

### 2. Permission Naming

Use a consistent permission naming scheme:

```
myplugin.command.user
myplugin.command.user.add
myplugin.command.user.remove
myplugin.command.user.list
myplugin.command.admin
myplugin.command.admin.reload
myplugin.command.admin.debug
```

### 3. Error Messages

Provide clear, helpful error messages using the Language Manager:

```java
if (args.length < 2) {
    LanguageManager langManager = CoolStuffLib.getLib().getLanguageManager();
    langManager.addPlaceholder(PlaceholderType.MESSAGE, "%usage%", syntax(), false);
    
    String errorMessage = langManager.getMessage("commands.not-enough-args", 
        (sender instanceof Player) ? (Player) sender : null, 
        true);
    langManager.resetPlaceholders(PlaceholderType.MESSAGE, null);
    sender.sendMessage(errorMessage);
    return false;
}
```

### 4. Command Aliases

Provide intuitive aliases for common commands:

```java
@Override
public String[] aliases() {
    return new String[]{"u", "usr"};  // Aliases for "user" command
}
```

### 5. Documentation

Document your commands in-game and in external documentation:

```java
@Override
public String info() {
    return "Manages user accounts with various subcommands";
}

@Override
public String syntax() {
    return "/" + mainCommandName + " user <add|remove|list> [username]";
}
```

### 6. Performance

Be mindful of performance, especially in tab completion:

```java
private List<String> cachedUsernames = new ArrayList<>();
private long lastCacheUpdate = 0;

private List<String> getCachedUsernames() {
    long now = System.currentTimeMillis();
    
    // Update cache every 30 seconds
    if (now - lastCacheUpdate > 30000) {
        cachedUsernames.clear();
        // Populate cache
        lastCacheUpdate = now;
    }
    
    return cachedUsernames;
}

@Override
public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
    Map<Integer, String[]> map = new HashMap<>();
    
    // Use cached values for better performance
    map.put(2, getCachedUsernames().toArray(new String[0]));
    
    return map;
}
```

### 7. Cool Stuff Lib Integration Best Practices

When using Cool Stuff Lib, follow these additional best practices:

#### Always Check Component Availability

```java
@Override
public boolean onPlayerCommand(Player player, String[] args) {
    CoolStuffLib lib = CoolStuffLib.getLib();
    if (lib == null) {
        player.sendMessage("§cCool Stuff Lib not available!");
        return false;
    }
    
    if (lib.isLanguageManagerEnabled()) {
        // Use language manager
        String message = lib.getLanguageManager().getMessage("key", player, true);
        player.sendMessage(message);
    } else {
        // Fallback to hardcoded messages
        player.sendMessage("Language manager not available");
    }
    
    return true;
}
```

#### Use Centralized Logging

```java
public class MyCommandManager extends CommandManager {
    private static final LogPrefix CMD_PREFIX = new LogPrefix("CMD", "§e[COMMAND]§r");
    
    protected void logCommand(String action, Player player, String[] args) {
        CoolStuffLib.getLib().writeToLog(
            Level.INFO,
            String.format("Player %s executed %s with args: %s", 
                player.getName(), action, Arrays.toString(args)),
            CMD_PREFIX,
            false
        );
    }
}
```

#### Leverage Language Manager Placeholders

```java
@Override
public boolean onPlayerCommand(Player player, String[] args) {
    LanguageManager langManager = CoolStuffLib.getLib().getLanguageManager();
    
    // Set placeholders before getting the message
    langManager.addPlaceholder(PlaceholderType.MESSAGE, "%player%", player.getName(), false);
    langManager.addPlaceholder(PlaceholderType.MESSAGE, "%world%", player.getWorld().getName(), false);
    langManager.addPlaceholder(PlaceholderType.MESSAGE, "%time%", String.valueOf(System.currentTimeMillis()), false);
    
    String message = langManager.getMessage("commands.success", player, true);
    player.sendMessage(message);
    
    // Reset placeholders after use
    langManager.resetPlaceholders(PlaceholderType.MESSAGE, null);
    
    return true;
}
```

#### Integrate with Menu System

```java
@CommandData(playerRequired = true)
public class MenuSubCommand extends SubCommand {
    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        PlayerMenuUtility menuUtility = CoolStuffLib.getLib().getPlayerMenuUtility(player.getUniqueId());
        new ConfigurationMenu(menuUtility).open();
        return true;
    }
}
```

## Conclusion

The Command Manager System, when combined with Cool Stuff Lib, provides powerful tools for creating complex command structures in your Bukkit/Spigot plugins. By leveraging the advanced features covered in this tutorial, you can create a polished, user-friendly command interface with multi-language support, integrated logging, and seamless menu integration.

The Cool Stuff Lib approach reduces boilerplate code, provides consistent error handling, and offers a unified way to access all plugin components. This results in more maintainable and feature-rich plugins.

Remember to balance complexity with usability, and always provide clear documentation and feedback to your users.

For more information, check out:
- [Cool Stuff Lib Tutorial](cool-stuff-lib-tutorial.md) for comprehensive library usage
- [Command System Basic Tutorial](command-system-basic.md) for getting started
- [Language Manager Tutorial](LANGUAGE_MANAGER_TUTORIAL.md) for advanced language features