package de.happybavarian07.adminpanel.commandmanagement;/*
 * @Author HappyBavarian07
 * @Date 09.11.2021 | 14:52
 */

import de.happybavarian07.adminpanel.language.LanguageManager;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefix;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

/**
 * <p>The {@code CommandManagerRegistry} class provides functionality for managing
 * and registering command managers and commands within a JavaPlugin environment.
 * It allows for commands to be registered, unregistered, and queried, as well
 * as maintaining associations between commands and their respective managers.</p>
 *
 * <p>This class offers reflection-based utilities for interacting with the internal
 * structures of the Bukkit PluginManager and the command system, such as
 * accessing private fields and handling known commands.</p>
 *
 * <p>Key Features:</p>
 * <ul>
 *   <li>Registers and unregisters {@link CommandManager}s and associated commands.</li>
 *   <li>Allows for query methods to check properties of commands and their managers,
 *       such as permission requirements, argument constraints, and subcommands.</li>
 *   <li>Supports auto-registration of permissions and subcommands for {@code CommandManager}s.</li>
 *   <li>Provides methods for handling command execution and tab completion efficiently.</li>
 *   <li>Ensures the integrity of the registry by allowing toggling and checks of the
 *       registry's ready state.</li>
 * </ul>
 *
 * <p>Usage of the {@code CommandManagerRegistry} requires initializing the class
 * with a {@link JavaPlugin} instance. Ensure that the registry is marked as ready
 * before performing registration or query operations to avoid runtime exceptions.</p>
 *
 * <p>Designed for advanced handling, this class assumes familiarity with
 * Bukkit's Plugin and Command APIs, as well as Java's reflection mechanisms.</p>
 */
public class CommandManagerRegistry implements CommandExecutor, TabCompleter {
    private final AdminPanelMain plugin;
    private final Map<CommandManager, CommandData> commandManagers;
    private LanguageManager lgm;
    private boolean commandManagerRegistryReady = false;

    /**
     * Constructs a new {@code CommandManagerRegistry} instance.
     *
     * <p>This constructor initializes the registry for managing command managers
     * specific to the provided plugin instance.</p>
     *
     * @param plugin The instance of the {@link JavaPlugin} this registry is associated with.
     */
    public CommandManagerRegistry(AdminPanelMain plugin) {
        this.plugin = plugin;
        this.commandManagers = new LinkedHashMap<>();
    }

    /**
     * Retrieves the value of a private field from a given object using reflection.
     * <p>
     * This method allows accessing private fields by temporarily making them accessible.
     * It handles exceptions such as {@link NoSuchFieldException} and {@link IllegalAccessException}.
     * The field is made accessible only during the retrieval process and its original accessibility is restored afterward.
     * </p>
     *
     * @param object The object from which the private field value is to be retrieved. Must not be null.
     * @param field  The name of the private field to retrieve. Must not be null or empty.
     * @return The value of the specified private field as an {@link Object}, or {@code null}
     */
    private static Object getPrivateField(Object object, String field) {
        Object result;
        try {
            Field objectField = object.getClass().getDeclaredField(field);
            objectField.setAccessible(true);
            result = objectField.get(object);
            objectField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    /**
     * Removes a registered command from the server's command map.
     *
     * <p>
     * This method unregisters a command from the Bukkit command map, effectively disabling it.
     * The command is also removed from related aliases, if they exist and are linked to the command.
     * This can be useful for dynamically managing commands in a plugin-enabled environment.
     *
     * <p>
     * Please note that this method uses reflection to access private fields and internal structures
     * of the Bukkit API. This approach might break with future updates or changes in the API and
     * should be used with caution.
     *
     * @param cmd the command to be unregistered. This must be a valid and previously registered
     *            command on the server's command map.
     */
    public static void unregisterCommand(Command cmd) {
        try {
            Object result = getPrivateField(Bukkit.getServer().getPluginManager(), "commandMap");
            SimpleCommandMap commandMap = (SimpleCommandMap) result;
            assert commandMap != null;
            /*Object map = getPrivateField(commandMap, "knownCommands");
            @SuppressWarnings("unchecked")
            HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
            assert knownCommands != null;
            knownCommands.remove(cmd.getName());
            for (String alias : cmd.getAliases()) {
                if (knownCommands.containsKey(alias) && knownCommands.get(alias).toString().contains(javaPlugin.getName())) {
                    knownCommands.remove(alias);
                }
            }*/
            cmd.unregister(commandMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers a {@link CommandManager} instance with the {@code CommandManagerRegistry}.
     * <p>
     * This method initializes the provided {@link CommandManager}, including its associated subcommands,
     * registers the necessary permissions, and associates the command manager with the internal registry.
     * It ensures that all required setup is complete and that the commands are available for execution.
     * </p>
     *
     * <p><strong>Note:</strong> This method will throw a {@link RuntimeException} if the registry is not ready for use.
     * Additionally, if a null {@code CommandManager} or a duplicate command manager is passed,
     * registration will fail, and {@code false} will be returned.</p>
     * <br>
     * <br>
     * <ul>
     * <li><strong>Pre-Init:</strong> The method calls {@link SubCommand#preInit()} for each subcommand
     * associated with the {@code CommandManager} before registration.</li>
     * <li><strong>Post-Init:</strong> After registration, {@link SubCommand#postInit()} is called for each subcommand.
     * This ensures that subcommands are correctly initialized and ready for use.</li>
     * <li><strong>Command Registration:</strong> The method registers the command with the server and sets the executor and tab completer.
     * If the command is already registered, the method will unregister it and re-register it.</li>
     * <li><strong>Permission Registration:</strong> If the command manager has auto-register permission enabled,
     * the method will check if the permission already exists and add it if not.</li>
     * <li><strong>Subcommand Setup:</strong> The method calls {@link CommandManager#setup()} to add subcommands to the command manager.</li>
     * <li><strong>Subcommand Permission Registration:</strong> If a subcommand has auto-register permission enabled,
     * the method will check if the permission already exists and add it if not.</li>
     * <li><strong>Subcommand List:</strong> The method clears the subcommand list after registration to ensure a clean state.</li>
     * <li><strong>Return:</strong> The method returns {@code true} if the registration is successful, {@code false} otherwise.</li>
     * </ul>
     *
     * @param cm the {@link CommandManager} instance to register. Must not be null.
     * @return {@code true} if the registration is successful, {@code false} otherwise.
     */
    public boolean register(CommandManager cm) {
        if (commandManagers.containsKey(cm) || cm == null) return false;
        if (!commandManagerRegistryReady)
            throw new RuntimeException("CommandManagerRegistry (CMR) not ready to use yet. The Start Method has not been called yet.");

        // Pre Init SubCommands
        for (SubCommand subCommand : cm.getSubCommands()) {
            subCommand.preInit();
        }

        // Checking if the Command Manager has CommandData

        CommandData data = cm.getClass().getAnnotation(CommandData.class);

        JavaPlugin javaPlugin = cm.getJavaPlugin();
        // Registering the Command on the Server
        if (javaPlugin.getCommand(cm.getCommandName()) != null) {
            Objects.requireNonNull(javaPlugin.getCommand(cm.getCommandName())).setExecutor(this);
            Objects.requireNonNull(javaPlugin.getCommand(cm.getCommandName())).setTabCompleter(this);
        } else {
            DCommand pluginCommand = new DCommand(cm.getCommandName(), javaPlugin);
            pluginCommand.setProperty("label", javaPlugin.getName().toLowerCase());
            pluginCommand.setProperty("aliases", cm.getCommandAliases());
            pluginCommand.setProperty("usage", cm.getCommandUsage());
            pluginCommand.setProperty("description", cm.getCommandInfo());
            pluginCommand.setProperty("permission", cm.getCommandPermissionAsString());
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
            pluginCommand.register();
        }
        if (cm.autoRegisterPermission()) {
            if (!permissionExistsAlready(cm.getCommandPermissionAsPermission())) {
                Bukkit.getPluginManager().addPermission(cm.getCommandPermissionAsPermission());
            }
        }
        // Calling setup() for Adding Sub Commands
        cm.setup();

        for (SubCommand subCommand : cm.getSubCommands()) {
            if (subCommand.autoRegisterPermission()) {
                if (!permissionExistsAlready(subCommand.permissionAsPermission())) {
                    Bukkit.getPluginManager().addPermission(subCommand.permissionAsPermission());
                }
            }
        }

        commandManagers.put(cm, data);

        // Post Init SubCommands
        for (SubCommand subCommand : cm.getSubCommands()) {
            subCommand.postInit();
        }
        return true;
    }

    /**
     * Checks if a given permission already exists in the set of permissions managed by Bukkit.
     *
     * <p>Compares the name of the given permission with the names of the permissions currently
     * registered with Bukkit's PluginManager. The comparison is case-insensitive.</p>
     *
     * @param permission The {@link Permission} object to check for existence.
     *                   Must not be null, and its name will be compared with existing permissions.
     * @return {@code true} if a permission with the same name already exists, {@code false} otherwise.
     */
    public boolean permissionExistsAlready(Permission permission) {
        // Check only after Name and not after any other value
        Set<Permission> bukkitPermissions = Bukkit.getPluginManager().getPermissions();
        for (Permission bukkitPermission : bukkitPermissions) {
            if (bukkitPermission.getName().equalsIgnoreCase(permission.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Unregisters a {@code CommandManager} from the {@code CommandManagerRegistry}.
     * This will remove the associated commands and permissions from the server,
     * as well as clear the sub-command list and the internal registry of the given {@code CommandManager}.
     *
     * <p><b>Note:</b> This method should only be used after ensuring the {@code CommandManagerRegistry} is ready
     * by calling the appropriate start method. If the registry is not ready, this method will throw a
     * {@code RuntimeException}.</p>
     *
     * @param cm the {@code CommandManager} to unregister. If {@code null} or not registered,
     *           the method will terminate without performing any action.
     *           <ul>
     *             <li>Commands registered by the {@code CommandManager} will be unregistered from the server.</li>
     *             <li>Permissions associated with the commands, both for main and sub-commands
     *           </ul>
     */
    public void unregister(CommandManager cm) {
        if (!commandManagers.containsKey(cm) || cm == null) return;
        if (!commandManagerRegistryReady)
            throw new RuntimeException("CommandManagerRegistry (CMR) not ready to use yet. The Start Method has not been called yet.");

        JavaPlugin javaPlugin = cm.getJavaPlugin();

        // Unregistering the Command on the Server
        if (javaPlugin.getCommand(cm.getCommandName()) != null) {
            unregisterCommand(javaPlugin.getCommand(cm.getCommandName()));
        } else {
            DCommand pluginCommand = new DCommand(cm.getCommandName(), javaPlugin);
            pluginCommand.setProperty("label", javaPlugin.getName().toLowerCase());
            pluginCommand.setProperty("aliases", cm.getCommandAliases());
            pluginCommand.setProperty("usage", cm.getCommandUsage());
            pluginCommand.setProperty("description", cm.getCommandInfo());
            pluginCommand.setProperty("permission", cm.getCommandPermissionAsString());
            unregisterCommand(pluginCommand);
        }
        if (cm.autoRegisterPermission()) {
            if (permissionExistsAlready(cm.getCommandPermissionAsPermission())) {
                Bukkit.getPluginManager().removePermission(cm.getCommandPermissionAsPermission());
            }
        }

        for (SubCommand subCommand : cm.getSubCommands()) {
            if (subCommand.autoRegisterPermission()) {
                if (permissionExistsAlready(subCommand.permissionAsPermission())) {
                    Bukkit.getPluginManager().removePermission(subCommand.permissionAsPermission());
                }
            }
        }

        // Call SubCommands.clear to Sub Command List
        cm.getSubCommands().clear();
        commandManagers.remove(cm);
    }

    /**
     * Unregisters all {@link CommandManager} instances currently registered in the registry.
     *
     * <p>This method iterates over all {@link CommandManager} instances maintained by the registry
     * and calls {@link #unregister(CommandManager)} for each instance. It ensures a clean removal
     * of all registered commands and their associated metadata.</p>
     *
     * <p><strong>Important:</strong> This method requires that the registry is in a ready state.
     * If the {@link CommandManagerRegistry} has not been initialized or the start method has not
     * been called, an exception will be thrown.</p>
     *
     * <p>Throws:</p>
     * <ul>
     *   <li>{@link RuntimeException} - if this method is invoked before the registry is ready for use.</li>
     * </ul>
     *
     * <p>Usage of this method is suitable for scenarios where a complete reset of the command registry
     * is needed, such as during plugin shutdown or reloading processes.</p>
     */
    public void unregisterAll() {
        if (!commandManagerRegistryReady)
            throw new RuntimeException("CommandManagerRegistry (CMR) not ready to use yet. The Start Method has not been called yet.");
        List<CommandManager> managers = new ArrayList<>(commandManagers.keySet());
        for (CommandManager manager : managers) {
            unregister(manager);
        }
    }

    /**
     * Retrieves a map of CommandManager instances paired with their associated CommandData.
     * <p>
     * This method provides access to the registered command managers and their corresponding data.
     * It ensures that the CommandManagerRegistry (CMR) is ready before returning the map.
     * If the CMR is not ready (i.e., the start method has not been called),
     * a {@code RuntimeException} will be thrown.
     *
     * @return A {@code Map<CommandManager, CommandData>} containing the registered command managers
     * and their associated command data.
     * @throws RuntimeException if the CommandManagerRegistry is not ready.
     */
    public Map<CommandManager, CommandData> getCommandManagers() {
        if (!commandManagerRegistryReady)
            throw new RuntimeException("CommandManagerRegistry (CMR) not ready to use yet. The Start Method has not been called yet.");
        return commandManagers;
    }

    /**
     * Retrieves the {@link CommandManager} associated with the given command name.
     * <p>
     * This method checks both the primary command name and any aliases
     * associated with each {@code CommandManager}. If a match is found, the
     * corresponding {@code CommandManager} is returned. If no match is found,
     * {@code null} is returned.
     * </p>
     *
     * @param commandName the name of the command or an alias to search for.
     *                    Must not be {@code null}.
     * @return the {@link CommandManager} associated with the provided command
     * name or {@code null} if no matching {@code CommandManager} is found.
     */
    public CommandManager getCommandManager(String commandName) {
        for (CommandManager cm : commandManagers.keySet()) {
            if (cm.getCommandName().equals(commandName)) {
                return cm;
            } else if (cm.getCommandAliases().contains(commandName)) {
                return cm;
            }
        }
        return null;
    }

    /**
     * Checks if the player is required to execute the commands managed by a specific {@link CommandManager}.
     *
     * <p>This method retrieves the {@link CommandData} associated with the provided {@link CommandManager}
     * and determines whether the command managed by the CommandManager requires a player as the sender.</p>
     *
     * @param commandManager the {@link CommandManager} for which the player requirement status is being checked.
     *                       Must not be null.
     * @return {@code true} if a player is required to execute the commands for the specified {@link CommandManager},
     * {@code false} otherwise.
     */
    public Boolean isPlayerRequired(CommandManager commandManager) {
        CommandData data = commandManagers.get(commandManager);
        if (data == null) return false;
        return data.playerRequired();
    }

    /**
     * Determines if the specified {@link CommandManager} requires operator (op) permissions.
     *
     * <p>This method checks if the given {@code CommandManager} instance has a registered
     * {@link CommandData} and evaluates whether it is flagged as requiring operator permissions.</p>
     *
     * @param commandManager the {@link CommandManager} instance to be checked.
     *                       Must not be {@code null}.
     * @return {@code true} if the given {@link CommandManager} requires operator (op) permissions;
     * {@code false} otherwise.
     */
    public Boolean isOpRequired(CommandManager commandManager) {
        CommandData data = commandManagers.get(commandManager);
        if (data == null) return false;
        return data.opRequired();
    }

    /**
     * Determines whether only sub-command arguments that match the defined sub-arguments
     * are allowed for a given {@link CommandManager}.
     *
     * <p>This method retrieves the {@link CommandData} associated with the specified
     * {@code CommandManager}. If the associated {@link CommandData} is not found,
     * this method returns {@code false}. Otherwise, it checks the setting for allowing only
     * sub-command arguments that fit the sub-argument definitions and returns the result.
     *
     * @param commandManager the {@link CommandManager} whose sub-command argument allowance setting
     *                       is to be retrieved.
     *                       <ul>
     *                         <li>Must be a valid and registered {@link CommandManager}.</li>
     *                         <li>Cannot be {@code null}.</li>
     *                       </ul>
     * @return {@code true} if only sub-command arguments fitting the sub-argument definitions
     * are allowed; {@code false} if otherwise or if no {@link CommandData} is associated
     */
    public Boolean allowOnlySubCommandArgsThatFitToSubArgs(CommandManager commandManager) {
        CommandData data = commandManagers.get(commandManager);
        if (data == null) return false;
        return data.allowOnlySubCommandArgsThatFitToSubArgs();
    }

    /**
     * Determines if the specified {@link CommandManager}'s sub-arguments are specific to the sender type.
     *
     * <p>This method retrieves the {@link CommandData} associated with the provided {@link CommandManager}.
     * If no such mapping exists, it returns {@code false}. Otherwise, it delegates to the
     * {@code senderTypeSpecificSubArgs()} method of the {@link CommandData} instance.
     *
     * @param commandManager the {@link CommandManager} whose sub-argument sender type specificity is being checked
     * @return {@code true} if sub-arguments are specific to the sender type; {@code false} otherwise
     */
    public boolean senderTypeSpecificSubArgs(CommandManager commandManager) {
        CommandData data = commandManagers.get(commandManager);
        if (data == null) return false;
        return data.senderTypeSpecificSubArgs();
    }

    /**
     * Retrieves the minimum number of arguments required for a specified {@link CommandManager}.
     * <p>
     * This method fetches the {@link CommandData} associated with the given {@link CommandManager}.
     * If no data exists for the provided {@link CommandManager}, the method will return 0.
     * Otherwise, it returns the minimum number of arguments needed as defined in the associated {@link CommandData}.
     * </p>
     *
     * @param commandManager the {@link CommandManager} instance whose minimum argument requirement is to be retrieved.
     *                       Must not be {@code null} to ensure reliable fetching.
     * @return the minimum number of arguments required for the given {@link CommandManager},
     * or 0 if no {@link CommandData} exists for the supplied {@link CommandManager}.
     */
    public int minArgs(CommandManager commandManager) {
        CommandData data = commandManagers.get(commandManager);
        if (data == null) return 0;
        return data.minArgs();
    }

    /**
     * Retrieves the maximum number of arguments that a command managed by the given {@link CommandManager} can support.
     * <p>
     * If the specified {@link CommandManager} is not registered or no associated data is found,
     * this method returns {@code Integer.MAX_VALUE}.
     *
     * @param commandManager the {@link CommandManager} whose maximum argument count is being queried
     * @return the maximum number of arguments the command can support, or {@code Integer.MAX_VALUE} if no data is associated with the provided {@link CommandManager}
     */
    public int maxArgs(CommandManager commandManager) {
        CommandData data = commandManagers.get(commandManager);
        if (data == null) return Integer.MAX_VALUE;
        return data.maxArgs();
    }

    /**
     * Retrieves the list of subcommands associated with the specified command name.
     * <p>
     * This method queries the relevant {@code CommandManager} for the given command name
     * and collects its subcommands.
     *
     * @param commandName the name of the command for which the subcommands are to be retrieved;
     *                    must not be {@code null}.
     * @return a {@code List} of {@code SubCommand} objects representing the subcommands
     * of the specified command; an empty list is returned if the command has no subcommands
     * or if no {@code CommandManager} matches the provided command name.
     */
    public List<SubCommand> getSubCommands(String commandName) {
        return getCommandManager(commandName).getSubCommands();
    }

    /**
     * Handles the execution of/**
     * Handles the execution of a command by processing the provided {@link Command}, sender, and arguments.
     * <p>
     * The method verifies the validity of the command, checks the argument count, sender type, and permissions
     * required to execute the command. It delegates the execution to the appropriate {@link CommandManager}
     * implementation and logs the result.
     * </p>
     *
     * <p>Key actions performed by the method:</p>
     * <ul>
     *     <li>Checks if the command name matches any registered {@link CommandManager} instances.</li>
     *     <li>Validates the argument count and sender's characteristics (e.g., being a player).</li>
     *     <li>Ensures that the sender has the required permissions to execute the command.</li>
     *     <li>Logs the success or failure of the command execution to the system log.</li>
     *     <li>Handles any exceptions that may occur during command execution by logging detailed error information.</li>
     * </ul>
     *
     * @param sender The entity issuing the command. This could be a {@link Player} or a console (non-player entity).
     * @param cmd    The {@link Command} being executed.
     * @param label  The command label or alias used by the sender.
     * @param args   An array of {@link String} representing the arguments passed along with the command.
     * @return {@code true} if the command execution was successful, {@code false} otherwise. Returning {@code true}
     * indicates that the command was handled and no further processing is required by the server.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        for (CommandManager cm : commandManagers.keySet()) {
            try {
                if (cm.getCommandName().equalsIgnoreCase(cmd.getName())) {
                    if (args.length == 0) {
                        if (AdminPanelMain.getPlugin().isSendSyntaxOnArgsZero()) {
                            Player playerTemp = null;
                            if (sender instanceof Player) {
                                playerTemp = (Player) sender;
                            }
                            sender.sendMessage(format(lgm.getMessage("Player.Commands.UsageMessage", playerTemp, true), cm));
                            return true;
                        }
                        sender.sendMessage(lgm.getMessage("Player.Commands.TooFewArguments", (sender instanceof Player) ? (Player) sender : null, true));
                        return true;
                    }
                    if (!(sender instanceof Player)) {
                        if (isPlayerRequired(cm)) {
                            sender.sendMessage(lgm.getMessage("Console.ExecutesPlayerCommand", null, true));
                            return true;
                        }
                    }
                    if (isOpRequired(cm)) {
                        if (!sender.isOp()) {
                            sender.sendMessage(lgm.getMessage("Player.Commands.NoPermission", (sender instanceof Player) ? (Player) sender : null, true));
                            return true;
                        }
                    }
                    if (args.length < minArgs(cm)) {
                        sender.sendMessage(lgm.getMessage("Player.Commands.TooFewArguments", (sender instanceof Player) ? (Player) sender : null, true));
                        return true;
                    }
                    if (args.length > maxArgs(cm)) {
                        sender.sendMessage(lgm.getMessage("Player.Commands.TooManyArguments", (sender instanceof Player) ? (Player) sender : null, true));
                        return true;
                    }

                    boolean commandResult = cm.onCommand(sender, args);

                    // Logging the command execution
                    String logMessage = "Command execution for command: " + cmd.getName() + ", Args: " + Arrays.toString(args) + ", Sender: " + sender.getName();

                    // Log the command result (success or failure)
                    if (commandResult) {
                        logMessage += " - Success";
                    } else {
                        logMessage += " - Failure";
                    }

                    AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, logMessage, LogPrefix.ADMINPANEL_COMMANDS, false);
                    return commandResult;
                }
            } catch (Exception e) {
                // Error occurred during command execution, log it.
                String logMessage = "Error during command execution for command: " + cmd.getName() + ", Args: " + Arrays.toString(args)
                        + ", Error: " + e.getMessage() + ", Stacktrace: " + Arrays.toString(e.getStackTrace());
                AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE, logMessage, LogPrefix.ERROR, true);
            }
        }
        return true;
    }

    /**
     * Formats a message by replacing placeholders with information from the provided {@link CommandManager}.
     *
     * <p>The following placeholders are replaced within the message:
     * <ul>
     *     <li><code>%command%</code>: Replaced with the name of the command.</li>
     *     <li><code>%usage%</code>: Replaced with the usage information of the command.</li>
     *     <li><code>%info%</code>: Replaced with the descriptive information of the command.</li>
     *     <li><code>%permission%</code>: Replaced with the command's required permission as a string.</li>
     * </ul>
     *
     * @param message The message string containing placeholders to be formatted.
     * @param cm      The {@link CommandManager} instance to supply values for placeholders.
     * @return The formatted message with all placeholders replaced by the corresponding values.
     */
    private String format(String message, CommandManager cm) {
        return message.replace("%command%", cm.getCommandName())
                .replace("%usage%", cm.getCommandUsage())
                .replace("%info%", cm.getCommandInfo())
                .replace("%permission%", cm.getCommandPermissionAsString());
    }

    /**
     * Handles tab-completion for commands executed by a {@link CommandSender}.
     * <p>
     * This method attempts to provide a list of valid tab-completion options based on the provided arguments
     * and the specific {@link CommandManager} registered for the command. If the sender does not meet certain
     * requirements (e.g., the sender is not a {@link Player} and the command requires a player), an empty list
     * will be returned.
     * <p>
     * In case of any internal errors during tab-completion, an appropriate message will be logged.
     *
     * @param sender the {@link CommandSender} who is attempting to execute the command.
     *               This can be a {@link Player}, the console, or another entity.
     * @param cmd    the {@link Command} object representing the executed command.
     * @param label  the alias of the command used by the sender.
     * @param args   an array of {@link String} representing the arguments provided by the sender.
     *               This may be used to determine the appropriate tab-completion suggestions.
     * @return a {@link List} of {@link String} containing possible tab-completion options based
     */
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        CommandManager cm = findCommandManager(cmd.getName());

        if (cm != null) {
            try {
                if (!(sender instanceof Player) && isPlayerRequired(cm)) {
                    return Collections.emptyList();
                }

                if (args.length == 0) {
                    return Collections.emptyList();
                }

                if (!sender.hasPermission(cm.getCommandPermissionAsPermission())) {
                    return Collections.emptyList();
                }

                return cm.onTabComplete(sender, cmd, label, args);
            } catch (NullPointerException e) {
                String logMessage = "Error during tab completion for command: " + cmd.getName() + ", Args: " + Arrays.toString(args)
                        + ", Error: " + e.getMessage() + ", Stacktrace: " + Arrays.toString(e.getStackTrace());
                AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE, logMessage, LogPrefix.ERROR, true);
            }
        }

        return null;
    }

    /**
     * Searches for a {@link CommandManager} based on the provided command name.
     * <p>
     * This method iterates through the registered {@link CommandManager} instances,
     * comparing their command names with the specified command name in a
     * case-insensitive manner. If a matching {@link CommandManager} is found,
     * it is returned; otherwise, {@code null} is returned.
     * </p>
     *
     * @param commandName the name of the command to search for; should not be null or empty
     * @return the {@link CommandManager} associated with the given command name,
     * or {@code null} if no match is found
     */
    @Nullable
    private CommandManager findCommandManager(String commandName) {
        for (CommandManager cm : commandManagers.keySet()) {
            if (cm.getCommandName().equalsIgnoreCase(commandName)) {
                return cm;
            }
        }
        return null;
    }

    /**
     * Checks whether the command manager registry is ready.
     *
     * <p>This method determines if the registry of command managers has been
     * properly set up and is in a ready state. It is essential for ensuring
     * that commands and their respective managers can function without issues.
     *
     * @return {@code true} if the command manager registry is ready;
     * {@code false} otherwise.
     */
    public boolean isCommandManagerRegistryReady() {
        return commandManagerRegistryReady;
    }

    /**
     * Sets the readiness status of the CommandManagerRegistry.
     * <p>
     * This method updates the {@code commandManagerRegistryReady} field to reflect whether
     * the CommandManagerRegistry is ready for use or not.
     * </p>
     *
     * @param cmrReady A boolean indicating whether the CommandManagerRegistry is ready.
     *                 <ul>
     *                   <li>If {@code true}, the registry is marked as ready.</li>
     *                   <li>If {@code false}, the registry is marked as not ready.</li>
     *                 </ul>
     */
    public void setCommandManagerRegistryReady(boolean cmrReady) {
        this.commandManagerRegistryReady = cmrReady;
    }

    /**
     * Sets the LanguageManager instance for this CommandManagerRegistry.
     *
     * <p>This method assigns the provided {@link LanguageManager} object to be used for handling language-related functionalities
     * in the CommandManagerRegistry. The LanguageManager is responsible for managing translations, messages, or other
     * localized content necessary for commands.
     *
     * <p>Calling this method allows customization of language management within this registry by setting the specific
     * LanguageManager implementation as needed.
     *
     * @param lgm the {@link LanguageManager} instance to be set. It should not be null and must be properly configured
     *            for managing desired language-related functionalities.
     */
    public void setLanguageManager(LanguageManager lgm) {
        this.lgm = lgm;
    }

    /**
     * Retrieves the instance of the {@link JavaPlugin} associated with the {@code CommandManagerRegistry}.
     *
     * <p>This method is useful to obtain the plugin instance that this class operates on.
     *
     * @return the {@link JavaPlugin} instance associated with the {@code CommandManagerRegistry}.
     */
    public AdminPanelMain getPlugin() {
        return plugin;
    }
}
