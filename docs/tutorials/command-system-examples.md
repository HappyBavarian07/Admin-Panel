# Command Manager System: Implementation Examples

This document provides practical examples of implementing the Command Manager System in different scenarios.

## Table of Contents

1. [Simple Plugin Command](#simple-plugin-command)
2. [Admin Command with Subcommands](#admin-command-with-subcommands)
3. [Player Management Commands](#player-management-commands)
4. [Economy Plugin Commands](#economy-plugin-commands)
5. [World Management Commands](#world-management-commands)

## Simple Plugin Command

This example shows a simple plugin with a single main command and a few basic subcommands.

### Main Plugin Class

```java
public class SimplePlugin extends JavaPlugin {
    private CommandManagerRegistry commandRegistry;
    
    @Override
    public void onEnable() {
        // Initialize the CommandManagerRegistry
        commandRegistry = new CommandManagerRegistry(this);
        commandRegistry.setLanguageManager(new SimpleLanguageManager(this));
        commandRegistry.setCommandManagerRegistryReady(true);
        
        // Register commands
        commandRegistry.register(new SimpleCommandManager());
        
        getLogger().info("SimplePlugin enabled with commands registered!");
    }
    
    @Override
    public void onDisable() {
        // Unregister all commands
        if (commandRegistry != null) {
            commandRegistry.unregisterAll();
        }
    }
    
    public static SimplePlugin getInstance() {
        return JavaPlugin.getPlugin(SimplePlugin.class);
    }
}
```

### Command Manager

```java
public class SimpleCommandManager extends CommandManager {
    @Override
    public String getCommandName() {
        return "simple";
    }

    @Override
    public String getCommandUsage() {
        return "/simple <info|reload|help>";
    }

    @Override
    public String getCommandInfo() {
        return "Main command for SimplePlugin";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return SimplePlugin.getInstance();
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("sp", "simpleplugin");
    }

    @Override
    public String getCommandPermissionAsString() {
        return "simpleplugin.command";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }

    @Override
    public void setup() {
        registerSubCommand(new InfoSubCommand(getCommandName()));
        registerSubCommand(new ReloadSubCommand(getCommandName()));
        registerSubCommand(new HelpCommand(getCommandName()));
    }
}
```

### Subcommands

```java
@CommandData(playerRequired = false, minArgs = 0, maxArgs = 0)
public class InfoSubCommand extends SubCommand {
    public InfoSubCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        player.sendMessage(ChatColor.GREEN + "SimplePlugin v1.0");
        player.sendMessage(ChatColor.GREEN + "Created by YourName");
        player.sendMessage(ChatColor.GREEN + "Type /" + mainCommandName + " help for more information");
        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        sender.sendMessage("SimplePlugin v1.0");
        sender.sendMessage("Created by YourName");
        sender.sendMessage("Type /" + mainCommandName + " help for more information");
        return true;
    }

    @Override
    public String name() {
        return "info";
    }

    @Override
    public String info() {
        return "Shows information about the plugin";
    }

    @Override
    public String[] aliases() {
        return new String[]{"i", "about", "version"};
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        return new HashMap<>();  // No subarguments
    }

    @Override
    public String syntax() {
        return "/" + mainCommandName + " info";
    }

    @Override
    public String permissionAsString() {
        return "simpleplugin.command.info";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }
}

@CommandData(playerRequired = false, minArgs = 0, maxArgs = 0)
public class ReloadSubCommand extends SubCommand {
    public ReloadSubCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        player.sendMessage(ChatColor.YELLOW + "Reloading SimplePlugin...");
        
        // Reload logic
        SimplePlugin.getInstance().reloadConfig();
        
        player.sendMessage(ChatColor.GREEN + "SimplePlugin reloaded successfully!");
        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        sender.sendMessage("Reloading SimplePlugin...");
        
        // Reload logic
        SimplePlugin.getInstance().reloadConfig();
        
        sender.sendMessage("SimplePlugin reloaded successfully!");
        return true;
    }

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public String info() {
        return "Reloads the plugin configuration";
    }

    @Override
    public String[] aliases() {
        return new String[]{"r", "refresh"};
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        return new HashMap<>();  // No subarguments
    }

    @Override
    public String syntax() {
        return "/" + mainCommandName + " reload";
    }

    @Override
    public String permissionAsString() {
        return "simpleplugin.command.reload";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }
}
```

## Admin Command with Subcommands

This example demonstrates an admin command with various administrative subcommands.

### Command Manager

```java
public class AdminCommandManager extends CommandManager {
    @Override
    public String getCommandName() {
        return "admin";
    }

    @Override
    public String getCommandUsage() {
        return "/admin <subcommand> [args]";
    }

    @Override
    public String getCommandInfo() {
        return "Administrative commands for server management";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return MyPlugin.getInstance();
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("a", "administrator");
    }

    @Override
    public String getCommandPermissionAsString() {
        return "myplugin.admin";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }

    @Override
    public void setup() {
        registerSubCommand(new KickAllSubCommand(getCommandName()));
        registerSubCommand(new BroadcastSubCommand(getCommandName()));
        registerSubCommand(new ServerStatusSubCommand(getCommandName()));
        registerSubCommand(new MaintenanceSubCommand(getCommandName()));
        registerSubCommand(new HelpCommand(getCommandName()));
    }
}
```

### Example Subcommand

```java
@CommandData(playerRequired = false, minArgs = 0, maxArgs = 1)
public class MaintenanceSubCommand extends SubCommand {
    public MaintenanceSubCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        return handleCommand(player, args);
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        return handleCommand(sender, args);
    }
    
    private boolean handleCommand(CommandSender sender, String[] args) {
        MaintenanceManager manager = MyPlugin.getInstance().getMaintenanceManager();
        
        if (args.length == 0) {
            // Toggle maintenance mode
            boolean newState = !manager.isMaintenanceMode();
            manager.setMaintenanceMode(newState);
            
            sender.sendMessage(ChatColor.YELLOW + "Maintenance mode " + 
                              (newState ? "enabled" : "disabled"));
            
            if (newState) {
                // Kick non-admin players
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("myplugin.admin.bypass")) {
                        player.kickPlayer(ChatColor.RED + "Server is now in maintenance mode");
                    }
                }
            }
            
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("enable")) {
                manager.setMaintenanceMode(true);
                sender.sendMessage(ChatColor.YELLOW + "Maintenance mode enabled");
                
                // Kick non-admin players
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("myplugin.admin.bypass")) {
                        player.kickPlayer(ChatColor.RED + "Server is now in maintenance mode");
                    }
                }
                
                return true;
            } else if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("disable")) {
                manager.setMaintenanceMode(false);
                sender.sendMessage(ChatColor.YELLOW + "Maintenance mode disabled");
                return true;
            } else if (args[0].equalsIgnoreCase("status")) {
                boolean status = manager.isMaintenanceMode();
                sender.sendMessage(ChatColor.YELLOW + "Maintenance mode is currently " + 
                                  (status ? "enabled" : "disabled"));
                return true;
            }
        }
        
        sender.sendMessage(ChatColor.RED + "Usage: " + syntax());
        return false;
    }

    @Override
    public String name() {
        return "maintenance";
    }

    @Override
    public String info() {
        return "Toggles server maintenance mode";
    }

    @Override
    public String[] aliases() {
        return new String[]{"maint", "m"};
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        Map<Integer, String[]> map = new HashMap<>();
        map.put(1, new String[]{"on", "off", "status"});
        return map;
    }

    @Override
    public String syntax() {
        return "/" + mainCommandName + " maintenance [on|off|status]";
    }

    @Override
    public String permissionAsString() {
        return "myplugin.admin.maintenance";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }
}
```

## Player Management Commands

This example shows a player management command system with various player-related operations.

### Command Manager

```java
public class PlayerCommandManager extends CommandManager {
    @Override
    public String getCommandName() {
        return "player";
    }

    @Override
    public String getCommandUsage() {
        return "/player <subcommand> [args]";
    }

    @Override
    public String getCommandInfo() {
        return "Commands for player management";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return MyPlugin.getInstance();
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("p", "plr");
    }

    @Override
    public String getCommandPermissionAsString() {
        return "myplugin.player";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }

    @Override
    public void setup() {
        registerSubCommand(new PlayerInfoSubCommand(getCommandName()));
        registerSubCommand(new PlayerTeleportSubCommand(getCommandName()));
        registerSubCommand(new PlayerInventorySubCommand(getCommandName()));
        registerSubCommand(new PlayerHealSubCommand(getCommandName()));
        registerSubCommand(new PlayerFeedSubCommand(getCommandName()));
        registerSubCommand(new HelpCommand(getCommandName()));
    }
}
```

### Example Subcommand

```java
@CommandData(playerRequired = false, minArgs = 1, maxArgs = 2)
public class PlayerTeleportSubCommand extends SubCommand {
    public PlayerTeleportSubCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        // Self teleport to another player
        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
                return false;
            }
            
            player.teleport(target.getLocation());
            player.sendMessage(ChatColor.GREEN + "Teleported to " + target.getName());
            return true;
        }
        
        // Teleport one player to another
        if (args.length == 2) {
            if (!player.hasPermission(permissionAsString() + ".others")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to teleport other players");
                return false;
            }
            
            Player from = Bukkit.getPlayer(args[0]);
            if (from == null) {
                player.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
                return false;
            }
            
            Player to = Bukkit.getPlayer(args[1]);
            if (to == null) {
                player.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return false;
            }
            
            from.teleport(to.getLocation());
            from.sendMessage(ChatColor.GREEN + "You were teleported to " + to.getName());
            player.sendMessage(ChatColor.GREEN + "Teleported " + from.getName() + " to " + to.getName());
            return true;
        }
        
        player.sendMessage(ChatColor.RED + "Usage: " + syntax());
        return false;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("Usage: " + syntax());
            sender.sendMessage("Note: Console must specify both players");
            return false;
        }
        
        Player from = Bukkit.getPlayer(args[0]);
        if (from == null) {
            sender.sendMessage("Player not found: " + args[0]);
            return false;
        }
        
        Player to = Bukkit.getPlayer(args[1]);
        if (to == null) {
            sender.sendMessage("Player not found: " + args[1]);
            return false;
        }
        
        from.teleport(to.getLocation());
        from.sendMessage(ChatColor.GREEN + "You were teleported to " + to.getName());
        sender.sendMessage("Teleported " + from.getName() + " to " + to.getName());
        return true;
    }

    @Override
    public String name() {
        return "teleport";
    }

    @Override
    public String info() {
        return "Teleports players";
    }

    @Override
    public String[] aliases() {
        return new String[]{"tp", "tele"};
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        Map<Integer, String[]> map = new HashMap<>();
        
        // Get online player names
        List<String> playerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerNames.add(player.getName());
        }
        String[] players = playerNames.toArray(new String[0]);
        
        // First argument is always a player name
        map.put(1, players);
        
        // Second argument is also a player name
        map.put(2, players);
        
        return map;
    }

    @Override
    public String syntax() {
        return "/" + mainCommandName + " teleport <player> [target]";
    }

    @Override
    public String permissionAsString() {
        return "myplugin.player.teleport";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }
}
```

## Economy Plugin Commands

This example demonstrates commands for an economy plugin.

### Command Manager

```java
public class EconomyCommandManager extends CommandManager {
    @Override
    public String getCommandName() {
        return "economy";
    }

    @Override
    public String getCommandUsage() {
        return "/economy <subcommand> [args]";
    }

    @Override
    public String getCommandInfo() {
        return "Economy management commands";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return EconomyPlugin.getInstance();
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("eco", "econ");
    }

    @Override
    public String getCommandPermissionAsString() {
        return "economyplugin.command";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }

    @Override
    public void setup() {
        registerSubCommand(new BalanceSubCommand(getCommandName()));
        registerSubCommand(new PaySubCommand(getCommandName()));
        registerSubCommand(new GiveSubCommand(getCommandName()));
        registerSubCommand(new TakeSubCommand(getCommandName()));
        registerSubCommand(new SetSubCommand(getCommandName()));
        registerSubCommand(new TopSubCommand(getCommandName()));
        registerSubCommand(new HelpCommand(getCommandName()));
    }
}
```

### Example Subcommand

```java
@CommandData(playerRequired = false, minArgs = 0, maxArgs = 1)
public class BalanceSubCommand extends SubCommand {
    public BalanceSubCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        EconomyManager economy = EconomyPlugin.getInstance().getEconomyManager();
        
        if (args.length == 0) {
            // Check own balance
            double balance = economy.getBalance(player.getUniqueId());
            String formatted = economy.formatCurrency(balance);
            
            player.sendMessage(ChatColor.GREEN + "Your balance: " + ChatColor.GOLD + formatted);
            return true;
        } else {
            // Check another player's balance
            if (!player.hasPermission(permissionAsString() + ".others")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to check others' balances");
                return false;
            }
            
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                // Try offline player
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                if (!offlinePlayer.hasPlayedBefore()) {
                    player.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
                    return false;
                }
                
                double balance = economy.getBalance(offlinePlayer.getUniqueId());
                String formatted = economy.formatCurrency(balance);
                
                player.sendMessage(ChatColor.GREEN + offlinePlayer.getName() + "'s balance: " + 
                                  ChatColor.GOLD + formatted);
                return true;
            }
            
            double balance = economy.getBalance(target.getUniqueId());
            String formatted = economy.formatCurrency(balance);
            
            player.sendMessage(ChatColor.GREEN + target.getName() + "'s balance: " + 
                              ChatColor.GOLD + formatted);
            return true;
        }
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        EconomyManager economy = EconomyPlugin.getInstance().getEconomyManager();
        
        if (args.length == 0) {
            sender.sendMessage("Console must specify a player name");
            return false;
        }
        
        // Check player's balance
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            // Try offline player
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
            if (!offlinePlayer.hasPlayedBefore()) {
                sender.sendMessage("Player not found: " + args[0]);
                return false;
            }
            
            double balance = economy.getBalance(offlinePlayer.getUniqueId());
            String formatted = economy.formatCurrency(balance);
            
            sender.sendMessage(offlinePlayer.getName() + "'s balance: " + formatted);
            return true;
        }
        
        double balance = economy.getBalance(target.getUniqueId());
        String formatted = economy.formatCurrency(balance);
        
        sender.sendMessage(target.getName() + "'s balance: " + formatted);
        return true;
    }

    @Override
    public String name() {
        return "balance";
    }

    @Override
    public String info() {
        return "Checks a player's balance";
    }

    @Override
    public String[] aliases() {
        return new String[]{"bal", "money"};
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        Map<Integer, String[]> map = new HashMap<>();
        
        // Get online player names
        List<String> playerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerNames.add(player.getName());
        }
        
        map.put(1, playerNames.toArray(new String[0]));
        
        return map;
    }

    @Override
    public String syntax() {
        return "/" + mainCommandName + " balance [player]";
    }

    @Override
    public String permissionAsString() {
        return "economyplugin.balance";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }
}
```

## World Management Commands

This example shows commands for managing Minecraft worlds.

### Command Manager

```java
public class WorldCommandManager extends CommandManager {
    @Override
    public String getCommandName() {
        return "world";
    }

    @Override
    public String getCommandUsage() {
        return "/world <subcommand> [args]";
    }

    @Override
    public String getCommandInfo() {
        return "World management commands";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return WorldManagerPlugin.getInstance();
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("wm", "worlds");
    }

    @Override
    public String getCommandPermissionAsString() {
        return "worldmanager.command";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }

    @Override
    public void setup() {
        registerSubCommand(new WorldListSubCommand(getCommandName()));
        registerSubCommand(new WorldCreateSubCommand(getCommandName()));
        registerSubCommand(new WorldDeleteSubCommand(getCommandName()));
        registerSubCommand(new WorldTeleportSubCommand(getCommandName()));
        registerSubCommand(new WorldInfoSubCommand(getCommandName()));
        registerSubCommand(new WorldSetSpawnSubCommand(getCommandName()));
        registerSubCommand(new WorldTimeSubCommand(getCommandName()));
        registerSubCommand(new WorldWeatherSubCommand(getCommandName()));
        registerSubCommand(new HelpCommand(getCommandName()));
    }
}
```

### Example Subcommand

```java
@CommandData(playerRequired = false, minArgs = 1, maxArgs = 3)
public class WorldCreateSubCommand extends SubCommand {
    public WorldCreateSubCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        return handleCommand(player, args);
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        return handleCommand(sender, args);
    }
    
    private boolean handleCommand(CommandSender sender, String[] args) {
        WorldManager worldManager = WorldManagerPlugin.getInstance().getWorldManager();
        
        String worldName = args[0];
        
        // Check if world already exists
        if (Bukkit.getWorld(worldName) != null) {
            sender.sendMessage(ChatColor.RED + "World already exists: " + worldName);
            return false;
        }
        
        // Default values
        WorldType worldType = WorldType.NORMAL;
        boolean generateStructures = true;
        
        // Parse world type
        if (args.length > 1) {
            try {
                worldType = WorldType.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Invalid world type: " + args[1]);
                sender.sendMessage(ChatColor.RED + "Valid types: NORMAL, FLAT, AMPLIFIED, LARGE_BIOMES");
                return false;
            }
        }
        
        // Parse generate structures flag
        if (args.length > 2) {
            if (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("yes")) {
                generateStructures = true;
            } else if (args[2].equalsIgnoreCase("false") || args[2].equalsIgnoreCase("no")) {
                generateStructures = false;
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid value for generateStructures: " + args[2]);
                sender.sendMessage(ChatColor.RED + "Use 'true' or 'false'");
                return false;
            }
        }
        
        sender.sendMessage(ChatColor.YELLOW + "Creating world '" + worldName + "'...");
        
        // Create the world asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(WorldManagerPlugin.getInstance(), () -> {
            try {
                World world = worldManager.createWorld(worldName, worldType, generateStructures);
                
                Bukkit.getScheduler().runTask(WorldManagerPlugin.getInstance(), () -> {
                    sender.sendMessage(ChatColor.GREEN + "World '" + world.getName() + "' created successfully!");
                    
                    // Teleport player to the new world if sender is a player
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        player.teleport(world.getSpawnLocation());
                        player.sendMessage(ChatColor.GREEN + "Teleported to " + world.getName());
                    }
                });
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(WorldManagerPlugin.getInstance(), () -> {
                    sender.sendMessage(ChatColor.RED + "Failed to create world: " + e.getMessage());
                    WorldManagerPlugin.getInstance().getLogger().severe("Error creating world: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
        
        return true;
    }

    @Override
    public String name() {
        return "create";
    }

    @Override
    public String info() {
        return "Creates a new world";
    }

    @Override
    public String[] aliases() {
        return new String[]{"new", "generate"};
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        Map<Integer, String[]> map = new HashMap<>();
        
        // World types for the second argument
        map.put(2, new String[]{"NORMAL", "FLAT", "AMPLIFIED", "LARGE_BIOMES"});
        
        // Generate structures for the third argument
        map.put(3, new String[]{"true", "false"});
        
        return map;
    }

    @Override
    public String syntax() {
        return "/" + mainCommandName + " create <name> [type] [generateStructures]";
    }

    @Override
    public String permissionAsString() {
        return "worldmanager.create";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }
}
```

These examples demonstrate how to implement the Command Manager System in various plugin scenarios. You can adapt these patterns to fit your specific plugin needs.