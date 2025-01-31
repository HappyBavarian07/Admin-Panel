package de.happybavarian07.adminpanel.commandmanagement;
/*
 * @Author HappyBavarian07
 * @Date 05.10.2021 | 17:28
 */

import de.happybavarian07.adminpanel.language.LanguageManager;
import de.happybavarian07.adminpanel.language.Placeholder;
import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@CommandData
public abstract class SubCommand implements Comparable<SubCommand> {
    protected AdminPanelMain plugin = AdminPanelMain.getPlugin();
    protected LanguageManager lgm = plugin.getLanguageManager();
    protected CommandManagerRegistry registry = plugin.getCommandManagerRegistry();
    protected String mainCommandName = "";
    /*
    /<command> <subcommand> args[0] args[1]
     */

    public SubCommand(String mainCommandName) {
        this.mainCommandName = mainCommandName;
    }

    /**
     * <p>Called before the command is registered.</p>
     */
    public void preInit() {
    }

    /**
     * <p>Called after the command is registered.</p>
     * <p>Use this function to initialize Values in the SubCommand</p>
     */
    public void postInit() {
    }

    /**
     * <p>Determines whether the command requires a player.</p>
     *
     * @return True if the command requires a player, false otherwise.
     */
    public boolean isPlayerRequired() {
        if (!this.getClass().isAnnotationPresent(CommandData.class)) {
            return registry.isPlayerRequired(registry.getCommandManager(mainCommandName));
        }
        return this.getClass().getAnnotation(CommandData.class).playerRequired();
    }

    /**
     * <p>Determines whether the command requires OP status.</p>
     *
     * @return True if the command requires OP status, false otherwise.
     */
    public boolean isOpRequired() {
        if (!this.getClass().isAnnotationPresent(CommandData.class)) {
            return registry.isOpRequired(registry.getCommandManager(mainCommandName));
        }
        return this.getClass().getAnnotation(CommandData.class).opRequired();
    }

    /**
     * <p>Determines whether the command should only allow sub-command arguments that fit into the sub-command's argument list.</p>
     * <p>If set to true, entering invalid arguments for sub-commands is prevented.</p>
     *
     * <p>For example, if a command has two sub-commands: "sub" and "sub2," and each of those commands has its own argument lists, then this function will determine whether or not it is possible for a user to enter an invalid set of arguments.</p>
     *
     * <p>If this function returns true (which it does by default), then if a user enters "/mainCommandName sub arg0 arg2," where arg0 and arg2 are valid arguments for the mainCommandName command,
     * the system will check if "sub" accepts those arguments, preventing invalid inputs.</p>
     *
     * @return True if only valid sub-command arguments are allowed, false otherwise.
     */
    public boolean allowOnlySubCommandArgsThatFitToSubArgs() {
        if (!this.getClass().isAnnotationPresent(CommandData.class)) {
            return registry.allowOnlySubCommandArgsThatFitToSubArgs(registry.getCommandManager(mainCommandName));
        }
        return this.getClass().getAnnotation(CommandData.class).allowOnlySubCommandArgsThatFitToSubArgs();
    }

    public boolean senderTypeSpecificSubArgs() {
        if (!this.getClass().isAnnotationPresent(CommandData.class)) {
            return registry.senderTypeSpecificSubArgs(registry.getCommandManager(mainCommandName));
        }
        return this.getClass().getAnnotation(CommandData.class).senderTypeSpecificSubArgs();
    }

    /**
     * <p>Gets the minimum number of arguments for this command.</p>
     * <p>If the command does not have a minimum number of arguments, then 0 is returned.</p>
     * @return The minimum number of arguments for this command.
     */
    public int minArgs() {
        if (!this.getClass().isAnnotationPresent(CommandData.class)) {
            return registry.minArgs(registry.getCommandManager(mainCommandName));
        }
        return this.getClass().getAnnotation(CommandData.class).minArgs();
    }


    /**
     * <p>Gets the maximum number of arguments for this command.</p>
     * <p>If the command does not have a maximum number of arguments, then Integer.MAX_VALUE is returned.</p>
     *
     * @return The maximum number of arguments for this command.
     */
    public int maxArgs() {
        if (!this.getClass().isAnnotationPresent(CommandData.class)) {
            return registry.maxArgs(registry.getCommandManager(mainCommandName));
        }
        return this.getClass().getAnnotation(CommandData.class).maxArgs();
    }


    /**
     * <p>Called when a player executes a command.</p>
     *
     * @param player The player who sent the command.
     * @param args   The command arguments.
     * @return A boolean value.
     */
    public boolean onPlayerCommand(Player player, String[] args) {
        return handleCommand(player, player, args);
    }

    /**
     * The onConsoleCommand function is called when a command is entered into the console.
     *
     * @param sender Send messages to the console
     * @param args   Get the arguments that were passed to the command
     * @return A boolean, which is used to determine whether the command was successful
     */
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        return handleCommand(sender, null, args);
    }

    /**
     * <p>Called when a command is entered into the console.</p>
     *
     * @param sender       The console command sender.
     * @param playerOrNull The player who sent the command, or null if the command was sent from the console.
     * @param args         The command arguments.
     * @return True if the command was handled successfully, false otherwise.
     */
    public boolean handleCommand(CommandSender sender, Player playerOrNull, String[] args) {
        return false;
    }

    public abstract String name();

    public abstract String info();

    public abstract String[] aliases();

    /**
     * <p>Gets the sub-arguments for this command.</p>
     * <p>Sub-arguments are arguments that are specific to a sub-command.</p>
     * <p>The Integer means the Number of the Argument, while the String[] contains the arguments</p>
     * <p>That means that if you want a command like this: /chat friend [Friend|Player] [Message]</p>
     * <p>Then you would return a Map with the following values:</p>
     * <p>1, new String[]{"Friend", "Player"}</p>
     * <p>2, new String[]{"Message"}</p>
     * The isPlayer can be used, but it doesn't need to be used.<br>
     * There is also a function called senderTypeSpecificSubArgs,<br>
     * which can be used to determine whether the sub-arguments are specific to the sender type.<br>
     * If this function returns true,<br>
     * then you can/wanted use the isPlayer variable to determine whether the sender is a player or not.<br>
     *
     * @param isPlayer Whether the sender is a player or not. -1 if not determined.
     * @param args     The arguments that were passed to the command.
     * @return A map containing the sub-arguments for this command.
     */
    public abstract Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args);

    public abstract String syntax();

    public Permission permissionAsPermission() {
        return new Permission(permissionAsString(), permissionAsString());
    }

    public abstract String permissionAsString();

    public abstract boolean autoRegisterPermission();

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
     * @param cmd Information about the command.
     * @return The string with the placeholders replaced.
     */
    protected String format(String in, SubCommand cmd) {
        Map<String, Placeholder> placeholders = new HashMap<>();
        placeholders.put("%usage%", new Placeholder("%usage%", cmd.syntax(), PlaceholderType.ALL));
        placeholders.put("%description%", new Placeholder("%description%", cmd.info(), PlaceholderType.ALL));
        placeholders.put("%name%", new Placeholder("%name%", cmd.name(), PlaceholderType.ALL));
        placeholders.put("%permission%", new Placeholder("%permission%", cmd.permissionAsPermission().getName(), PlaceholderType.ALL));
        placeholders.put("%aliases%", new Placeholder("%aliases%", cmd.aliases(), PlaceholderType.ALL));
        placeholders.put("%subArgs%", new Placeholder("%subArgs%", cmd.subArgs(null, -1, new String[0]).toString(), PlaceholderType.ALL));

        return lgm.replacePlaceholders(in, placeholders);
    }

    /**
     * <p>Compares this subcommand to another subcommand.</p>
     * <p>Subcommands are compared based on their name, info, aliases, syntax, and info.</p>
     * <p>Subcommands are sorted in ascending order based on the following criteria:</p>
     * <ol>
     *     <li>Name</li>
     *     <li>Info</li>
     *     <li>Aliases</li>
     *     <li>Syntax</li>
     * </ol>
     *
     * @param o The subcommand to compare to.
     * @return A negative integer, zero, or a positive integer as this subcommand is less than,
     * equal to, or greater than the specified subcommand.
     */
    @Override
    public int compareTo(@NotNull SubCommand o) {
        int nameComparison = this.name().compareTo(o.name());
        if (nameComparison != 0) {
            return nameComparison;
        }

        int infoComparison = this.info().compareTo(o.info());
        if (infoComparison != 0) {
            return infoComparison;
        }

        int aliasesComparison = Arrays.toString(this.aliases()).compareTo(Arrays.toString(o.aliases()));
        if (aliasesComparison != 0) {
            return aliasesComparison;
        }

        int syntaxComparison = this.syntax().compareTo(o.syntax());
        if (syntaxComparison != 0) {
            return syntaxComparison;
        }

        return this.info().compareTo(o.info());
    }
}
