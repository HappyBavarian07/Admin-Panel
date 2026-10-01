package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.FeatureGate;
import de.happybavarian07.adminpanel.menusystem.menu.StartupStatusMenu;
import de.happybavarian07.adminpanel.startup.Startup;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.bukkit.command.CommandSender;

/**
 * /adminpaneladmin status: every startup stage with its status and, for the ones that are not OK, the reason.
 */
public class StartupStatusCommand extends SubCommand {
    public StartupStatusCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public String name() {
        return "status";
    }

    @Override
    public String info() {
        return "Shows which parts of Admin-Panel started and which did not";
    }

    @Override
    public String permissionAsString() {
        return StartupStatusMenu.PERMISSION;
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        CommandText.send(lgm, sender, "StartupStatus.Header");
        for (Startup.Result result : AdminPanelMain.getPlugin().getStartup().results()) {
            CommandText.send(lgm, sender, "StartupStatus.Line",
                    "%status%", CommandText.get(lgm, sender, "StartupStatus.Status." + result.status().name()),
                    "%stage%", FeatureGate.featureName(lgm, sender, result.id()),
                    "%detail%", result.detail().isEmpty() ? "" : " &7- " + result.detail());
        }
        return true;
    }
}
