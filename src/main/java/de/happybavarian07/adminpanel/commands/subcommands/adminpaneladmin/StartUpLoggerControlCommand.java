package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@SubCommandInfo(name = "StartUpLoggerControl", aliases = "SULControl", info = "Controls the Start Up Logger",
        permission = "AdminPanel.AdminPanelAdminCommands.StartUpLoggerControl", autoRegisterPermission = false)
public class StartUpLoggerControlCommand extends SubCommand {
    public StartUpLoggerControlCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(Argument.choice("state", "Enable", "Disable"));
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        String state = args.string(0);
        if (!state.equalsIgnoreCase("Enable") && !state.equalsIgnoreCase("Disable")) return false;
        boolean enable = state.equalsIgnoreCase("Enable");
        if (enable) AdminPanelMain.getPlugin().getStartUpLogger().enableMessageSystem();
        else AdminPanelMain.getPlugin().getStartUpLogger().disableMessageSystem();
        CommandText.send(lgm, sender, enable ? "AdminPanelAdmin.StartUpLogger.Enabled" : "AdminPanelAdmin.StartUpLogger.Disabled");
        return true;
    }
}
