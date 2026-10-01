package de.happybavarian07.adminpanel.commands.managers;

import de.happybavarian07.adminpanel.commands.subcommands.warnings.WarningsCommand;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;

public class WarningsManager extends WarningCommandManager {
    @Override
    public String getCommandName() {
        return "warnings";
    }

    @Override
    public String getCommandUsage() {
        return "/warnings <player> [page]";
    }

    @Override
    public String getCommandPermissionAsString() {
        return "AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Warnings";
    }

    @Override
    SubCommand rootCommand() {
        return new WarningsCommand(getCommandName());
    }
}
