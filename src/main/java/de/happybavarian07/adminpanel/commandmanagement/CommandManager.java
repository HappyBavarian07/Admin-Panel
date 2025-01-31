package de.happybavarian07.adminpanel.commandmanagement;/*
 * @Author HappyBavarian07
 * @Date 09.11.2021 | 14:43
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.language.LanguageManager;
import de.happybavarian07.adminpanel.language.Placeholder;
import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefix;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;

/**
 * The base class for creating command managers with subcommands.
 */
@CommandData
public abstract class CommandManager {
    private final ArrayList<SubCommand> commands = new ArrayList<>();
    protected final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    protected final LanguageManager lgm = plugin.getLanguageManager();
    protected List<String> commandArgs = new ArrayList<>();
    protected List<String> commandSubArgs = new ArrayList<>();

    /**
     * Gets the name of the main command.
     *
     * @return The name of the main command.
     */
    public abstract String getCommandName();

    /**
     * Gets the usage instructions for the main command.
     *
     * @return The usage instructions for the main command.
     */
    public abstract String getCommandUsage();

    /**
     * Gets a brief description of what the main command does.
     *
     * @return A brief description of the main command.
     */
    public abstract String getCommandInfo();

    /**
     * Gets the JavaPlugin associated with this command manager.
     *
     * @return The JavaPlugin associated with this command manager.
     */
    public abstract JavaPlugin getJavaPlugin();

    /**
     * Gets a list of aliases for the main command.
     *
     * @return A list of aliases for the main command.
     */
    public abstract List<String> getCommandAliases();

    /**
     * Gets the permission associated with the main command as a Permission object.
     *
     * @return The permission associated with the main command as a Permission object.
     */
    public Permission getCommandPermissionAsPermission() {
        return new Permission(getCommandPermissionAsString(), getCommandInfo());
    }

    /**
     * Gets the permission associated with the main command as a string.
     *
     * @return The permission associated with the main command as a string.
     */
    public abstract String getCommandPermissionAsString();

    /**
     * Determines whether permission should be automatically registered for the main command.
     *
     * @return True if permission should be automatically registered, false otherwise.
     */
    public abstract boolean autoRegisterPermission();

    /**
     * Executes the main command or one of its subcommands.
     *
     * @param sender The sender of the command.
     * @param args   The arguments provided the command.
     * @return True if the command was handled successfully, false otherwise.
     */
    public boolean onCommand(CommandSender sender, String[] args) {
        SubCommand target = this.getSubCommand(args[0]);

        if (target == null) {
            sender.sendMessage(lgm.getMessage("Player.Commands.InvalidSubCommand", getPlayerForSender(sender), true));
            return true;
        }

        if (!hasPermission(sender, target)) {
            sender.sendMessage(format(lgm.getMessage("Player.General.NoPermissions", getPlayerForSender(sender), true), target));
            return true;
        }

        String[] updatedArgs = removeFirstArgument(args);

        if (target.isPlayerRequired() && !(sender instanceof Player)) {
            sender.sendMessage(lgm.getMessage("Console.ExecutesPlayerCommand", null, true));
            return true;
        }
        if (target.allowOnlySubCommandArgsThatFitToSubArgs()) {
            Map<Integer, String> invalidArgs = findInvalidArgs(updatedArgs, target, (sender instanceof Player) ? 1 : 0);
            if (!invalidArgs.isEmpty()) {
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%invalidArgs%", invalidArgs.toString(), false);
                sender.sendMessage(format(lgm.getMessage("Player.Commands.CommandContainsInvalidArgs", getPlayerForSender(sender), true), target));
                return false;
            }
        }

        try {
            boolean callResult = handleSubCommand(sender, target, updatedArgs);
            if (!callResult) {
                sender.sendMessage(format(lgm.getMessage("Player.Commands.UsageMessage", getPlayerForSender(sender), true), target));
            }
        } catch (Exception e) {
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%error%", e + ": " + e.getMessage(), false);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%stacktrace%", Arrays.toString(e.getStackTrace()), false);
            sender.sendMessage(format(lgm.getMessage("Player.Commands.ErrorPerformingSubCommand", getPlayerForSender(sender), true), target));
            String stacktraceWithLineBreaks = Arrays.toString(e.getStackTrace()).replace(", ", "\n");
            plugin.getFileLogger().writeToLog(Level.SEVERE,
                    "Error performing subcommand: " + target.name() +
                            "(Error: " + e + ": " + e.getLocalizedMessage() + ", Stacktrace: " + stacktraceWithLineBreaks + ")",
                    LogPrefix.ADMINPANEL_COMMANDS,
                    true);
        }
        return true;
    }

    /**
     * Finds invalid arguments in a given array of arguments for a given subcommand.
     *
     * @param args   The array of arguments to check.
     * @param target The subcommand to check against.
     * @return A map of invalid arguments, with the key being the index of the argument and the value being the argument itself.
     */
    private Map<Integer, String> findInvalidArgs(String[] args, SubCommand target, int isPlayer) {
        Map<Integer, String> invalidArgs = new HashMap<>();
        Map<Integer, String[]> subArgs = target.subArgs(null, isPlayer, args);
        if (subArgs == null || subArgs.isEmpty()) return invalidArgs;
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (!Arrays.asList(subArgs.get(i)).contains(arg)) {
                invalidArgs.put(i, arg);
            }
        }
        return invalidArgs;
    }

    /**
     * Retrieves the {@link Player} object associated with the given {@link CommandSender}.
     *
     * @param sender The {@link CommandSender} to retrieve the {@link Player} object for.
     * @return The {@link Player} object associated with the given {@link CommandSender}, or null if the sender is not a {@link Player}.
     */
    private Player getPlayerForSender(CommandSender sender) {
        return (sender instanceof Player) ? (Player) sender : null;
    }

    /**
     * Removes the first argument from the given array of arguments.
     *
     * @param args The array of arguments.
     * @return A new array of arguments with the first argument removed.
     */
    private String[] removeFirstArgument(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }

    /**
     * Checks if the given {@link CommandSender} has permission to execute the given {@link SubCommand}.
     *
     * @param sender The {@link CommandSender} to check permission for.
     * @param target The {@link SubCommand} to check permission for.
     * @return {@code true} if the {@link CommandSender} has permission to execute the {@link SubCommand}, {@code false} otherwise.
     */
    public boolean hasPermission(CommandSender sender, SubCommand target) {
        return sender.hasPermission(target.permissionAsPermission()) || (target.isOpRequired() && sender.isOp());
    }

    /**
     * Handles a subcommand for the given sender.
     *
     * @param sender The sender of the command.
     * @param target The subcommand to handle.
     * @param args   The arguments for the subcommand.
     * @return Whether the command was successfully handled.
     */
    public boolean handleSubCommand(CommandSender sender, SubCommand target, String[] args) {
        if (sender instanceof Player) {
            return target.onPlayerCommand((Player) sender, args);
        }
        return target.onConsoleCommand((ConsoleCommandSender) sender, args);
    }

    /**
     * Handles tab completion for the main command and its subcommands.
     *
     * @param sender  The {@link CommandSender} of the command.
     * @param command The main {@link Command}.
     * @param label   The label of the command.
     * @param args    The arguments provided for tab completion.
     * @return A list of possible tab completions.
     */
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        Set<String> result = new HashSet<>();
        List<String> subCommandOptions = new ArrayList<>();
        List<String> subCommandArgOptions = new ArrayList<>();

        for (SubCommand sub : this.getSubCommands()) {
            if (!sender.hasPermission(sub.permissionAsPermission()) && !(sub.isOpRequired() && sender.isOp())) {
                continue;
            }

            subCommandOptions.add(sub.name());
            subCommandOptions.addAll(Arrays.asList(sub.aliases()));

            if (args.length == 1) {
                for (String option : subCommandOptions) {
                    if (option.toLowerCase().startsWith(args[0].toLowerCase())) {
                        result.add(option);
                    }
                }
            } else if (args.length > 1) {
                if (sub.name().equals(args[0]) || Arrays.asList(sub.aliases()).contains(args[0])) {
                    Map<Integer, String[]> subArgs = sub.subArgs(sender, (sender instanceof Player) ? 1 : 0, Arrays.copyOfRange(args, 1, args.length));
                    if (subArgs != null && subArgs.containsKey(args.length - 1)) {
                        subCommandArgOptions.addAll(Arrays.asList(subArgs.get(args.length - 1)));
                    }

                    for (String option : subCommandArgOptions) {
                        if (option.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                            result.add(option);
                        }
                    }

                    return new ArrayList<>(result);
                }
            }
        }

        return new ArrayList<>(result);
    }

    /**
     * Sets up the main command and registers all subcommands.
     * <p> This will be called by the CMR on registration. </p>
     * <p> This is where you should register all subcommands using {@link #registerSubCommand(SubCommand)}. </p>
     * <p> This method should be overridden by the implementing class. </p>
     */
    public abstract void setup();

    /**
     * Retrieves the list of subcommands associated with the main command.
     *
     * @return A list of {@link SubCommand} objects associated with the main command.
     */
    public List<SubCommand> getSubCommands() {
        return commands;
    }

    /**
     * Gets a specific subcommand by its name or alias.
     *
     * @param name The name or alias of the subcommand to retrieve.
     * @return The {@link SubCommand} with the given name or alias, or null if not found.
     */
    protected SubCommand getSubCommand(String name) {
        for (SubCommand subCommand : getSubCommands()) {
            if (subCommand.name().equalsIgnoreCase(name) || Arrays.asList(subCommand.aliases()).contains(name)) {
                return subCommand;
            }
        }
        return null;
    }

    /**
     * Registers a subcommand with the main command.
     * <p> This method should be called in the {@link #setup()} method to register all subcommands.</p>
     * <p> If the subcommand is successfully registered, this method will return true.</p>
     * <p> If the subcommand is not successfully registered, this method will return false.</p>
     * <p> If the subcommand is already registered, this method will return false.</p>
     *
     * @param subCommand The subcommand to register.
     * @return True if the subcommand was successfully registered, false otherwise.
     */
    protected boolean registerSubCommand(SubCommand subCommand) {
        return commands.add(subCommand);
    }

    /**
     * <p>Formats the help message for a subcommand, replacing placeholders with their respective values.</p>
     * <p>Available placeholders:</p>
     * <ul>
     *     <li>%usage% - Replaced with the usage of this command</li>
     *     <li>%description% - Replaced with a description of this command</li>
     *     <li>%name% - Replaced with the name of this command</li>
     *     <li>%permission% - Replaced with the permission required for this command</li>
     *     <li>%aliases% - Replaced with the command's aliases</li>
     *     <li>%subArgs% - Replaced with the command's sub-arguments</li>
     * </ul>
     *
     * @param in  The message to format.
     * @param cmd The {@link SubCommand} associated with the message.
     * @return The string with the placeholders replaced.
     */
    private String format(String in, SubCommand cmd) {
        Map<String, Placeholder> placeholders = new HashMap<>();
        placeholders.put("%usage%", new Placeholder("%usage%", cmd.syntax(), PlaceholderType.ALL));
        placeholders.put("%description%", new Placeholder("%description%", cmd.info(), PlaceholderType.ALL));
        placeholders.put("%name%", new Placeholder("%name%", cmd.name(), PlaceholderType.ALL));
        placeholders.put("%permission%", new Placeholder("%permission%", cmd.permissionAsPermission().getName(), PlaceholderType.ALL));
        placeholders.put("%aliases%", new Placeholder("%aliases%", cmd.aliases(), PlaceholderType.ALL));
        placeholders.put("%subArgs%", new Placeholder("%subArgs%", cmd.subArgs(null, -1, new String[0]).toString(), PlaceholderType.ALL));

        return lgm.replacePlaceholders(in, placeholders);
    }
}
