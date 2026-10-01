package de.happybavarian07.adminpanel.commands.managers;

import de.happybavarian07.adminpanel.commands.subcommands.warnings.WarnCommand;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;

public class WarnManager extends WarningCommandManager {
    @Override
    public String getCommandName() {
        return "warn";
    }

    @Override
    public String getCommandUsage() {
        return "/warn <player> <reason...>";
    }

    @Override
    public String getCommandPermissionAsString() {
        return "AdminPanel.PlayerManager.PlayerSettings.Warn";
    }

    @Override
    SubCommand rootCommand() {
        return new WarnCommand(getCommandName());
    }
}
