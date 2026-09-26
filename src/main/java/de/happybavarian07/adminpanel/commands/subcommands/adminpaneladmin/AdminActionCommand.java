package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefixExtension;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/** A /adminpaneladmin sub command that runs one reload or update action and reports how it went. */
public class AdminActionCommand extends SubCommand {
    @FunctionalInterface
    public interface Action {
        void run(CommandSender sender) throws Exception;
    }

    private final String name;
    private final String[] aliases;
    private final String permission;
    private final String actionKey;
    private final boolean confirm;
    private final Action action;

    public AdminActionCommand(String mainCommandName, String name, String[] aliases, String permission,
                              String actionKey, boolean confirm, Action action) {
        super(mainCommandName);
        this.name = name;
        this.aliases = aliases;
        this.permission = permission;
        this.actionKey = actionKey;
        this.confirm = confirm;
        this.action = action;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String[] aliases() {
        return aliases;
    }

    @Override
    public String info() {
        return "Runs: " + actionKey;
    }

    @Override
    public String permissionAsString() {
        return permission;
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }

    @Override
    public boolean requiresConfirmation() {
        return confirm;
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        Player player = sender instanceof Player p ? p : null;
        String actionName = lgm.getMessage("AdminPanelAdmin.Actions." + actionKey, player, false);
        sender.sendMessage(lgm.getMessage("AdminPanelAdmin.ActionStarted", player, false).replace("%action%", actionName));
        try {
            action.run(sender);
            sender.sendMessage(lgm.getMessage("AdminPanelAdmin.ActionFinished", player, false).replace("%action%", actionName));
        } catch (Exception e) {
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE, "Admin action '" + name + "' failed: " + e, LogPrefixExtension.ADMINPANEL_MAIN, true);
            sender.sendMessage(lgm.getMessage("AdminPanelAdmin.ActionFailed", player, false)
                    .replace("%action%", actionName).replace("%error%", String.valueOf(e.getMessage())));
        }
        return true;
    }
}
