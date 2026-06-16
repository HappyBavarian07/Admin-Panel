# Cool Stuff Lib: Complete Tutorial

This comprehensive tutorial covers everything you need to know about using the Cool Stuff Lib in your Bukkit/Spigot plugins. The Cool Stuff Lib provides a unified framework for command management, language support, menu systems, and more.

## Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [Core Components](#core-components)
4. [Setting Up Cool Stuff Lib](#setting-up-cool-stuff-lib)
5. [Command Manager Integration](#command-manager-integration)
6. [Language Manager Integration](#language-manager-integration)
7. [Menu System Integration](#menu-system-integration)
8. [Logging System](#logging-system)
9. [Advanced Configuration](#advanced-configuration)
10. [Best Practices](#best-practices)
11. [Troubleshooting](#troubleshooting)

## Introduction

Cool Stuff Lib is a comprehensive library that simplifies plugin development by providing:

- **Command Manager System**: Structured command handling with subcommands and permissions
- **Language Manager**: Multi-language support with per-player language preferences
- **Menu System**: Interactive GUI menus with easy-to-use API
- **Plugin File Logger**: Centralized logging with file output
- **PlaceholderAPI Integration**: Automatic detection and support
- **Builder Pattern**: Simplified setup and configuration

## Getting Started

### Prerequisites

- Java 8 or higher
- Bukkit/Spigot/Paper server
- Basic knowledge of Java and Bukkit plugin development

### Adding Cool Stuff Lib to Your Project

Add the Cool Stuff Lib dependency to your project:

**Maven:**
```xml
<dependency>
    <groupId>de.happybavarian07</groupId>
    <artifactId>coolstufflib</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```gradle
dependencies {
    implementation 'de.happybavarian07:coolstufflib:1.0.0'
}
```

### Required Imports

When using Cool Stuff Lib features, you'll need these common imports:

```java
import de.happybavarian07.coolstufflib.CoolStuffLib;
import de.happybavarian07.coolstufflib.CoolStuffLibBuilder;
import de.happybavarian07.coolstufflib.commandmanagement.CommandManager;
import de.happybavarian07.coolstufflib.commandmanagement.CommandManagerRegistry;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import de.happybavarian07.coolstufflib.commandmanagement.CommandData;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.MenuAddonManager;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.utils.LogPrefix;
import de.happybavarian07.coolstufflib.utils.PluginFileLogger;
```

## Core Components

### CoolStuffLib Class

The main class that coordinates all components and provides access to managers.

### CoolStuffLibBuilder

A builder class that simplifies the setup process using the builder pattern.

### CommandManagerRegistry

Manages command registration, execution, and tab completion.

### LanguageManager

Handles multi-language support and message management.

### MenuAddonManager

Manages interactive GUI menus and player menu utilities.

### PluginFileLogger

Provides file-based logging capabilities.

## Setting Up Cool Stuff Lib

### Basic Setup

Here's how to set up Cool Stuff Lib in your plugin's main class:

```java
public class MyPlugin extends JavaPlugin {
    private CoolStuffLib coolStuffLib;
    
    @Override
    public void onEnable() {
        // Initialize Cool Stuff Lib
        setupCoolStuffLib();
        
        // Register your components
        registerCommands();
        registerMenus();
        
        getLogger().info("Plugin enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        // Clean shutdown
        if (coolStuffLib != null) {
            if (coolStuffLib.getCommandManagerRegistry() != null) {
                coolStuffLib.getCommandManagerRegistry().unregisterAll();
            }
        }
        getLogger().info("Plugin disabled successfully!");
    }
    
    private void setupCoolStuffLib() {
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
        
        // Build Cool Stuff Lib with all components
        File langFolder = new File(getDataFolder(), "lang");
        coolStuffLib = new CoolStuffLibBuilder(this)
                .setCommandManagerRegistry(new CommandManagerRegistry(this))
                .setLanguageManager(new LanguageManager(this, langFolder, "lang", "&7[MyPlugin] "))
                .setMenuAddonManager(new MenuAddonManager())
                .setPluginFileLogger(new PluginFileLogger(this))
                .setDataFile(dataFile)
                .setUsePlayerLangHandler(true)
                .setSendSyntaxOnZeroArgs(true)
                .createCoolStuffLib();
        
        // Initialize all components
        coolStuffLib.setup();
    }
    
    private void registerCommands() {
        // Register your command managers here
        coolStuffLib.getCommandManagerRegistry().register(new MainCommandManager());
        coolStuffLib.getCommandManagerRegistry().register(new AdminCommandManager());
    }
    
    private void registerMenus() {
        // Register your menus here if needed
        // Menu registration is typically done when creating menu instances
    }
    
    public CoolStuffLib getCoolStuffLib() {
        return coolStuffLib;
    }
    
    public static MyPlugin getInstance() {
        return getPlugin(MyPlugin.class);
    }
}
```

### Minimal Setup

If you only need specific components, you can create a minimal setup:

```java
private void setupMinimalCoolStuffLib() {
    File dataFile = new File(getDataFolder(), "data.yml");
    
    File langFolder = new File(getDataFolder(), "lang");
    coolStuffLib = new CoolStuffLibBuilder(this)
            .setCommandManagerRegistry(new CommandManagerRegistry(this))
            .setLanguageManager(new LanguageManager(this, langFolder, "lang", "&7[MyPlugin] "))
            .setDataFile(dataFile)
            .createCoolStuffLib();
    
    coolStuffLib.setup();
}
```

## Command Manager Integration

### Creating a Command Manager

```java
public class MainCommandManager extends CommandManager {
    @Override
    public String getCommandName() {
        return "myplugin";
    }

    @Override
    public String getCommandUsage() {
        return "/myplugin <subcommand> [args]";
    }

    @Override
    public String getCommandInfo() {
        return "Main command for MyPlugin";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return MyPlugin.getInstance();
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("mp", "myp");
    }

    @Override
    public String getCommandPermissionAsString() {
        return "myplugin.command.main";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }

    @Override
    public void setup() {
        // Register subcommands
        registerSubCommand(new InfoSubCommand(getCommandName()));
        registerSubCommand(new ReloadSubCommand(getCommandName()));
        registerSubCommand(new HelpCommand(getCommandName()));
    }
}
```

### Creating Subcommands

```java
@CommandData(
    playerRequired = false,
    opRequired = false,
    minArgs = 0,
    maxArgs = 0
)
public class InfoSubCommand extends SubCommand {
    public InfoSubCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        // Get language manager from Cool Stuff Lib
        LanguageManager langManager = CoolStuffLib.getLib().getLanguageManager();
        
        // Send localized message
        String message = langManager.getMessage("commands.info.message", player, true);
        player.sendMessage(message);
        
        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        sender.sendMessage("Plugin Info: MyPlugin v1.0.0");
        return true;
    }

    @Override
    public String name() {
        return "info";
    }

    @Override
    public String info() {
        return "Shows plugin information";
    }

    @Override
    public String[] aliases() {
        return new String[]{"i", "about"};
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        return new HashMap<>();
    }

    @Override
    public String syntax() {
        return "/" + mainCommandName + " info";
    }

    @Override
    public String permissionAsString() {
        return "myplugin.command.info";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }
}
```

## Language Manager Integration

### Setting Up Language Files

Create language files in your plugin's `lang` folder:

**lang/en.yml:**
```yaml
commands:
  info:
    message: "&aPlugin Info: &7MyPlugin v1.0.0"
  reload:
    success: "&aPlugin reloaded successfully!"
    error: "&cError reloading plugin: {error}"
  
messages:
  no-permission: "&cYou don't have permission to use this command!"
  player-only: "&cThis command can only be used by players!"
  invalid-args: "&cInvalid arguments! Usage: {usage}"
```

**lang/de.yml:**
```yaml
commands:
  info:
    message: "&aPlugin Info: &7MyPlugin v1.0.0"
  reload:
    success: "&aPlugin erfolgreich neu geladen!"
    error: "&cFehler beim Neuladen des Plugins: {error}"
  
messages:
  no-permission: "&cDu hast keine Berechtigung für diesen Befehl!"
  player-only: "&cDieser Befehl kann nur von Spielern verwendet werden!"
  invalid-args: "&cUngültige Argumente! Verwendung: {usage}"
```

### Using Language Manager in Commands

```java
@Override
public boolean onPlayerCommand(Player player, String[] args) {
    LanguageManager langManager = CoolStuffLib.getLib().getLanguageManager();
    
    // Get localized message for the player
    String message = langManager.getMessage("commands.info.message", player, true);
    player.sendMessage(message);
    
    // Use placeholders (note: placeholders are set separately, not passed to getMessage)
    langManager.addPlaceholder(PlaceholderType.MESSAGE, "%usage%", syntax(), false);
    String errorMessage = langManager.getMessage("messages.invalid-args", player, true);
    langManager.resetPlaceholders(PlaceholderType.MESSAGE, null); // Reset after use
    
    return true;
}
```

### Per-Player Language Support

With `setUsePlayerLangHandler(true)`, players can have individual language preferences:

```java
LanguageManager langManager = CoolStuffLib.getLib().getLanguageManager();
langManager.getPLHandler().setPlayerLanguage(player.getUniqueId(), "de");

// Get player's language
String playerLang = langManager.getPLHandler().getPlayerLanguageName(player.getUniqueId());
```

## Menu System Integration

### Creating a Menu

```java
public class MainMenu extends Menu {
    public MainMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return "Main Menu";
    }

    @Override
    public int getSlots() {
        return 27; // 3 rows
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        
        switch (e.getCurrentItem().getType()) {
            case DIAMOND:
                // Handle diamond click
                player.sendMessage("You clicked the diamond!");
                break;
            case EMERALD:
                // Open another menu
                new SettingsMenu(CoolStuffLib.getLib().getPlayerMenuUtility(player.getUniqueId())).open();
                break;
            case BARRIER:
                // Close menu
                player.closeInventory();
                break;
        }
    }

    @Override
    public void setMenuItems() {
        // Set menu items
        ItemStack info = new ItemStack(Material.DIAMOND);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§aPlugin Info");
        infoMeta.setLore(Arrays.asList("§7Click to view plugin information"));
        info.setItemMeta(infoMeta);
        inventory.setItem(10, info);
        
        ItemStack settings = new ItemStack(Material.EMERALD);
        ItemMeta settingsMeta = settings.getItemMeta();
        settingsMeta.setDisplayName("§bSettings");
        settingsMeta.setLore(Arrays.asList("§7Click to open settings"));
        settings.setItemMeta(settingsMeta);
        inventory.setItem(12, settings);
        
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§cClose");
        close.setItemMeta(closeMeta);
        inventory.setItem(16, close);
    }
}
```

### Opening Menus

```java
// In a command or event handler
Player player = // get player
PlayerMenuUtility playerMenuUtility = CoolStuffLib.getLib().getPlayerMenuUtility(player.getUniqueId());
new MainMenu(playerMenuUtility).open();
```

## Logging System

### Using the Plugin File Logger

```java
public class MyPlugin extends JavaPlugin {
    
    public void logInfo(String message) {
        CoolStuffLib.getLib().writeToLog(Level.INFO, message, LogPrefix.INFO, true);
    }
    
    public void logError(String message) {
        CoolStuffLib.getLib().writeToLog(Level.SEVERE, message, LogPrefix.ERROR, true);
    }
    
    public void logDebug(String message) {
        CoolStuffLib.getLib().writeToLog(Level.FINE, message, LogPrefix.DEBUG, false);
    }
}
```

### Using Log Prefixes

```java
// Use predefined log prefixes
CoolStuffLib.getLib().writeToLog(Level.INFO, "Info message", LogPrefix.INFO, true);
CoolStuffLib.getLib().writeToLog(Level.SEVERE, "Error message", LogPrefix.ERROR, true);
CoolStuffLib.getLib().writeToLog(Level.WARNING, "Warning message", LogPrefix.WARNING, true);
CoolStuffLib.getLib().writeToLog(Level.FINE, "Debug message", LogPrefix.DEBUG, false);
```

## Advanced Configuration

### Custom Starting Methods

You can provide custom initialization logic for each component:

```java
private void setupAdvancedCoolStuffLib() {
    // Custom language manager initialization
    Consumer<Object[]> customLangInit = (args) -> {
        LanguageManager langManager = (LanguageManager) args[0];
        JavaPlugin plugin = (JavaPlugin) args[1];
        
        // Custom language setup logic
        langManager.addLanguagesToList(true);
        // Add custom languages, set default language, etc.
    };
    
    // Custom command manager initialization
    Consumer<Object[]> customCmdInit = (args) -> {
        CommandManagerRegistry cmdRegistry = (CommandManagerRegistry) args[0];
        LanguageManager langManager = (LanguageManager) args[1];
        
        // Custom command setup logic
        cmdRegistry.setLanguageManager(langManager);
        cmdRegistry.setCommandManagerRegistryReady(true);
        // Additional custom setup
    };
    
    File langFolder = new File(getDataFolder(), "lang");
    coolStuffLib = new CoolStuffLibBuilder(this)
            .setCommandManagerRegistry(new CommandManagerRegistry(this))
            .setLanguageManager(new LanguageManager(this, langFolder, "lang", "&7[MyPlugin] "))
            .setLanguageManagerStartingMethod(customLangInit)
            .setCommandManagerRegistryStartingMethod(customCmdInit)
            .setDataFile(new File(getDataFolder(), "data.yml"))
            .createCoolStuffLib();
    
    coolStuffLib.setup();
}
```

### Accessing Components

```java
public class MyPlugin extends JavaPlugin {
    
    public CommandManagerRegistry getCommandRegistry() {
        return coolStuffLib.getCommandManagerRegistry();
    }
    
    public LanguageManager getLanguageManager() {
        return coolStuffLib.getLanguageManager();
    }
    
    public MenuAddonManager getMenuManager() {
        return coolStuffLib.getMenuAddonManager();
    }
    
    public PluginFileLogger getFileLogger() {
        return coolStuffLib.getPluginFileLogger();
    }
    
    public boolean isPlaceholderAPIEnabled() {
        return coolStuffLib.isPlaceholderAPIEnabled();
    }
}
```

## Best Practices

### 1. Initialization Order

Always call `coolStuffLib.setup()` before registering commands or using any components:

```java
@Override
public void onEnable() {
    setupCoolStuffLib();      // First
    coolStuffLib.setup();     // Second
    registerCommands();       // Third
    registerEvents();         // Fourth
}
```

### 2. Error Handling

Implement proper error handling for component initialization:

```java
private void setupCoolStuffLib() {
    try {
        File dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            dataFile.createNewFile();
        }
        
        coolStuffLib = new CoolStuffLibBuilder(this)
                .setCommandManagerRegistry(new CommandManagerRegistry(this))
                .setLanguageManager(new LanguageManager(this))
                .setDataFile(dataFile)
                .createCoolStuffLib();
        
        coolStuffLib.setup();
        
    } catch (Exception e) {
        getLogger().severe("Failed to initialize Cool Stuff Lib: " + e.getMessage());
        e.printStackTrace();
        Bukkit.getPluginManager().disablePlugin(this);
    }
}
```

### 3. Resource Management

Properly clean up resources in `onDisable()`:

```java
@Override
public void onDisable() {
    if (coolStuffLib != null) {
        // Unregister commands
        if (coolStuffLib.getCommandManagerRegistry() != null) {
            coolStuffLib.getCommandManagerRegistry().unregisterAll();
        }
        
        // Clear player menu utilities
        coolStuffLib.getPlayerMenuUtilityMap().clear();
    }
}
```

### 4. Singleton Access

Use the static accessor for global access:

```java
// In any class
CoolStuffLib lib = CoolStuffLib.getLib();
if (lib != null) {
    LanguageManager langManager = lib.getLanguageManager();
    // Use language manager
}
```

### 5. Component Checks

Always check if components are enabled before using them:

```java
CoolStuffLib lib = CoolStuffLib.getLib();
if (lib != null && lib.isLanguageManagerEnabled()) {
    String message = lib.getLanguageManager().getMessage("key", player, true);
    player.sendMessage(message);
}
```

## Troubleshooting

### Common Issues

**1. "CommandManagerRegistry not ready" Error**
- Ensure you call `coolStuffLib.setup()` before registering commands
- Check that the CommandManagerRegistry is properly set in the builder

**2. Language files not loading**
- Verify language files are in the correct `lang/` folder
- Check file encoding (should be UTF-8)
- Ensure proper YAML syntax

**3. Menus not working**
- Verify MenuAddonManager is set in the builder
- Check that MenuListener is registered (done automatically by `setup()`)
- Ensure proper PlayerMenuUtility usage

**4. Permissions not working**
- Check that `autoRegisterPermission()` returns `true`
- Verify permission strings are correct
- Ensure permissions are registered after command registration

### Debug Mode

Enable debug logging to troubleshoot issues:

```java
coolStuffLib = new CoolStuffLibBuilder(this)
        .setPluginFileLogger(new PluginFileLogger(this))
        // ... other components
        .createCoolStuffLib();

// Enable debug logging
coolStuffLib.writeToLog(Level.INFO, "Cool Stuff Lib initialized", LogPrefix.INFO, true);
```

### Getting Help

If you encounter issues:

1. Check the console for error messages
2. Verify your setup matches the examples
3. Ensure all required components are properly configured
4. Check the [GitHub repository](https://github.com/HappyBavarian07/CoolStuffLib) for updates
5. Join the Discord server for community support

## Conclusion

Cool Stuff Lib provides a powerful and flexible framework for Bukkit plugin development. By following this tutorial and the best practices outlined, you can create robust plugins with minimal boilerplate code.

The library's modular design allows you to use only the components you need, while the builder pattern ensures easy setup and configuration. Whether you're building simple commands or complex menu systems, Cool Stuff Lib has the tools to help you succeed.

For more specific tutorials on individual components, check out:
- [Command System Basic Tutorial](command-system-basic.md)
- [Command System Advanced Tutorial](command-system-advanced.md)
- [Language Manager Tutorial](LANGUAGE_MANAGER_TUTORIAL.md)
- [Expression Engine Tutorial](EXPRESSION_ENGINE_TUTORIAL.md)