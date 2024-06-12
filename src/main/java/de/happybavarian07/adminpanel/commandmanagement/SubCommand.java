package de.happybavarian07.adminpanel.commandmanagement;
/*
 * @Author HappyBavarian07
 * @Date 05.10.2021 | 17:28
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.language.LanguageManager;
import de.happybavarian07.adminpanel.language.Placeholder;
import de.happybavarian07.adminpanel.language.PlaceholderType;
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

    public boolean isPlayerRequired() {
        if (!this.getClass().isAnnotationPresent(CommandData.class)) {
            return registry.isPlayerRequired(registry.getCommandManager(mainCommandName));
        }
        return this.getClass().getAnnotation(CommandData.class).playerRequired();
    }

    public boolean isOpRequired() {
        if (!this.getClass().isAnnotationPresent(CommandData.class)) {
            return registry.isOpRequired(registry.getCommandManager(mainCommandName));
        }
        return this.getClass().getAnnotation(CommandData.class).opRequired();
    }

    /**
     * Checks if the Sub Command can only be executed if the Player gives the Args given in the {subArgs()} Method
     *
     * @return boolean
     */
    public boolean allowOnlySubCommandArgsThatFitToSubArgs() {
        if (!this.getClass().isAnnotationPresent(CommandData.class)) {
            return registry.allowOnlySubCommandArgsThatFitToSubArgs(registry.getCommandManager(mainCommandName));
        }
        return this.getClass().getAnnotation(CommandData.class).allowOnlySubCommandArgsThatFitToSubArgs();
    }

    public boolean onPlayerCommand(Player player, String[] args) {
        return handleCommand(player, player, args);
    }

    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        return handleCommand(sender, null, args);
    }

    public boolean handleCommand(CommandSender sender, Player playerOrNull, String[] args) {
        return false;
    }

    public abstract String name();

    public abstract String info();

    public abstract String[] aliases();

    public abstract Map<Integer, String[]> subArgs();

    public abstract String syntax();

    public Permission permissionAsPermission() {
        return new Permission(permissionAsString(), permissionAsString());
    }
    public abstract String permissionAsString();
    public abstract boolean autoRegisterPermission();

    protected String format(String in, SubCommand cmd) {
        Map<String, Placeholder> placeholders = new HashMap<>();
        placeholders.put("%usage%", new Placeholder("%usage%", cmd.syntax(), PlaceholderType.ALL));
        placeholders.put("%description%", new Placeholder("%description%", cmd.info(), PlaceholderType.ALL));
        placeholders.put("%name%", new Placeholder("%name%", cmd.name(), PlaceholderType.ALL));
        placeholders.put("%permission%", new Placeholder("%permission%", cmd.permissionAsString(), PlaceholderType.ALL));
        placeholders.put("%aliases%", new Placeholder("%aliases%", cmd.aliases(), PlaceholderType.ALL));
        placeholders.put("%subArgs%", new Placeholder("%subArgs%", cmd.subArgs().toString(), PlaceholderType.ALL));

        return lgm.replacePlaceholders(in, placeholders);
    }

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
