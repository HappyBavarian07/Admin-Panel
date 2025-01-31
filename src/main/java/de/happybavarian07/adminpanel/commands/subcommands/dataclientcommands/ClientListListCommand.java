package de.happybavarian07.adminpanel.commands.subcommands.dataclientcommands;/*
 * @Author HappyBavarian07
 * @Date 29.11.2022 | 17:42
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.PaginatedList;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.language.PlaceholderType;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;

@CommandData
public class ClientListListCommand extends SubCommand {
    public ClientListListCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        if (args.length != 1) {
            return false;
        }
        try {
            int page = Integer.parseInt(args[0]);
            PaginatedList<String> messages = new PaginatedList<>(plugin.getDataClient().getConnectionHandler().getOtherConnectedClients());
            messages.maxItemsPerPage(10).sort("bubble", false);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%page%", page, false);
            if (!messages.containsPage(page)) {
                player.sendMessage(lgm.getMessage("DataClient.ClientListMessages.PageDoesNotExist", player, true));
                return true;
            }
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%max_page%", messages.getMaxPage(), false);
            player.sendMessage(lgm.getMessage("DataClient.ClientListMessages.Header", player, false));
            for (String s : messages.getPage(page)) {
                player.sendMessage(lgm.getMessage("DataClient.ClientListMessages.Format", player, false)
                        .replace("%count%", String.valueOf(messages.getListOfThings().indexOf(s)))
                        .replace("%dataClientName%", s));
            }
            player.sendMessage(lgm.getMessage("DataClient.ClientListMessages.Footer", player, true));
        } catch (NumberFormatException e) {
            player.sendMessage(lgm.getMessage("Player.Commands.NotANumber", player, true));
            return true;
        } catch (PaginatedList.ListNotSortedException e2) {
            e2.printStackTrace();
            return true;
        }
        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        if (args.length != 1) {
            return false;
        }
        try {
            int page = Integer.parseInt(args[0]);
            PaginatedList<String> messages = new PaginatedList<>(plugin.getDataClient().getConnectionHandler().getOtherConnectedClients());
            System.out.println("Other Connected Clients: " + plugin.getDataClient().getConnectionHandler().getOtherConnectedClients());
            messages.maxItemsPerPage(10).sort("bubble", false);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%page%", page, false);
            if (!messages.containsPage(page)) {
                sender.sendMessage(lgm.getMessage("DataClient.ClientListMessages.PageDoesNotExist", null, true));
                return true;
            }
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%max_page%", messages.getMaxPage(), false);
            sender.sendMessage(lgm.getMessage("DataClient.ClientListMessages.Header", null, false));
            for (String s : messages.getPage(page)) {
                sender.sendMessage(lgm.getMessage("DataClient.ClientListMessages.Format", null, false)
                        .replace("%count%", String.valueOf(messages.getListOfThings().indexOf(s)))
                        .replace("%dataClientName%", s));
            }
            sender.sendMessage(lgm.getMessage("DataClient.ClientListMessages.Footer", null, true));
        } catch (NumberFormatException e) {
            sender.sendMessage(lgm.getMessage("Player.Commands.NotANumber", null, true));
            return true;
        } catch (PaginatedList.ListNotSortedException e2) {
            e2.printStackTrace();
            return true;
        }
        return true;
    }

    @Override
    public String name() {
        return "ListClients";
    }

    @Override
    public String info() {
        return "Lists all Clients that are connected to the Server";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        return Collections.emptyMap();
    }

    @Override
    public String syntax() {
        return "/dataclient ListClients <Page>";
    }

    @Override
    public String permissionAsString() {
        return "AdminPanel.DataClient.SubCommands.ListClients";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }
}
