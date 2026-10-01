package de.happybavarian07.adminpanel.commands.managers;

import de.happybavarian07.adminpanel.commands.subcommands.warnings.UnwarnCommand;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;

public class UnwarnManager extends WarningCommandManager {
    @Override
    public String getCommandName() {
        return "unwarn";
    }

    @Override
    public String getCommandUsage() {
        return "/unwarn <player> <number|all>";
    }

    @Override
    public String getCommandPermissionAsString() {
        return "AdminPanel.PlayerManager.PlayerSettings.Warn";
    }

    @Override
    SubCommand rootCommand() {
        return new UnwarnCommand(getCommandName());
    }
}
