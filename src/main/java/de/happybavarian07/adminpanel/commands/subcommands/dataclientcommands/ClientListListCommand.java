package de.happybavarian07.adminpanel.commands.subcommands.dataclientcommands;/*
 * @Author HappyBavarian07
 * @Date 29.11.2022 | 17:42
 */

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.PaginatedList;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import org.bukkit.command.CommandSender;

import java.util.List;

@SubCommandInfo(name = "ListClients", info = "Lists all Clients that are connected to the Server",
        permission = "AdminPanel.DataClient.SubCommands.ListClients", autoRegisterPermission = false)
public class ClientListListCommand extends DataClientSubCommand {
    private static final String KEY = "DataClient.ClientListMessages.";

    public ClientListListCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(Argument.integer("page").optional());
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        int page = args.integer(0, 1);
        List<String> clients = client().getConnectionHandler().getOtherConnectedClients();
        PaginatedList<String> list = new PaginatedList<>(clients);
        list.maxItemsPerPage(10).sort("bubble", false);
        try {
            if (!list.containsPage(page)) {
                CommandText.send(lgm, sender, KEY + "PageDoesNotExist");
                return true;
            }
            Object[] pages = {"%page%", page, "%max_page%", list.getMaxPage()};
            CommandText.send(lgm, sender, KEY + "Header", pages);
            for (String client : list.getPage(page)) {
                CommandText.send(lgm, sender, KEY + "Format", "%count%", list.getListOfThings().indexOf(client), "%dataClientName%", client);
            }
            CommandText.send(lgm, sender, KEY + "Footer", pages);
        } catch (PaginatedList.ListNotSortedException e) {
            throw new IllegalStateException(e);
        }
        return true;
    }
}
