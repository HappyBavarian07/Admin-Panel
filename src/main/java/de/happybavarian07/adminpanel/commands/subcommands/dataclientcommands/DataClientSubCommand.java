package de.happybavarian07.adminpanel.commands.subcommands.dataclientcommands;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.syncing.DataClient;
import de.happybavarian07.adminpanel.syncing.DataClientUtils;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgumentException;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;

import java.util.Map;

/** Base for the /dataclient sub commands: fails with a message instead of an NPE when syncing is off. */
abstract class DataClientSubCommand extends SubCommand {
    DataClientSubCommand(String mainCommandName) {
        super(mainCommandName);
    }

    static DataClient client() {
        DataClient client = AdminPanelMain.getPlugin().getDataClient();
        if (client == null) throw disabled();
        return client;
    }

    static DataClientUtils utils() {
        DataClientUtils utils = AdminPanelMain.getPlugin().getDataClientUtils();
        if (utils == null) throw disabled();
        return utils;
    }

    private static CommandArgumentException disabled() {
        return new CommandArgumentException("DataClient.NotEnabled", "%prefix% &9> &cThe Data Client is not enabled!", Map.of());
    }
}
