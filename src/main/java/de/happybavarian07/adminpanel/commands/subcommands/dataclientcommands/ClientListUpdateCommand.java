package de.happybavarian07.adminpanel.commands.subcommands.dataclientcommands;/*
 * @Author HappyBavarian07
 * @Date 29.11.2022 | 17:42
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;

@CommandData
public class ClientListUpdateCommand extends SubCommand {
    public ClientListUpdateCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        plugin.getDataClient().getPacketHandler().requestClientListUpdate();
        player.sendMessage(lgm.getMessage("DataClient.RequestedUpdateMessage", player, false));
        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        plugin.getDataClient().getPacketHandler().requestClientListUpdate();
        sender.sendMessage(lgm.getMessage("DataClient.RequestedUpdateMessage", null, false));
        return true;
    }

    @Override
    public String name() {
        return "UpdateClientList";
    }

    @Override
    public String info() {
        return "Requests a Client List Update from the Server";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public Map<Integer, String[]> subArgs() {
        return Collections.emptyMap();
    }

    @Override
    public String syntax() {
        return "/dataclient UpdateClientList";
    }

    @Override
    public String permissionAsString() {
        return "AdminPanel.DataClient.SubCommands.UpdateClients";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }
}
