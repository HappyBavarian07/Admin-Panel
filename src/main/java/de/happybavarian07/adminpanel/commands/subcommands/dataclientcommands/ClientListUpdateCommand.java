package de.happybavarian07.adminpanel.commands.subcommands.dataclientcommands;/*
 * @Author HappyBavarian07
 * @Date 29.11.2022 | 17:42
 */

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import org.bukkit.command.CommandSender;

@SubCommandInfo(name = "UpdateClientList", info = "Requests a Client List Update from the Server",
        permission = "AdminPanel.DataClient.SubCommands.UpdateClients", autoRegisterPermission = false)
public class ClientListUpdateCommand extends DataClientSubCommand {
    public ClientListUpdateCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        client().getPacketHandler().requestClientListUpdate();
        CommandText.send(lgm, sender, "DataClient.RequestedUpdateMessage");
        return true;
    }
}
