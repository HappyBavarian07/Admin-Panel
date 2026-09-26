package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;

import de.happybavarian07.adminpanel.listeners.StaffChatHandler;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.CommandData;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SubCommandInfo(name = "staffchat", aliases = {"sc", "staffc"},
        info = "This Command is used to toggle the StaffChat on or off and send messages.",
        syntax = "/adminpaneladmin staffchat <toggle [Player]|disableSC|message...>",
        permission = "AdminPanel.AdminPanelAdminCommands.StaffChat", autoRegisterPermission = false)
@CommandData(minArgs = 1)
public class StaffChatCommand extends SubCommand {
    public StaffChatCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(
                Argument.custom("toggle|disableSC|message", sender -> List.of("toggle", "disableSC")),
                Argument.custom("player|message", sender -> {
                    List<String> names = new ArrayList<>();
                    Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
                    return names;
                }).optional());
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        StaffChatHandler staffChat = AdminPanelMain.getPlugin().getStaffChatHandler();
        String first = args.string(0);
        if (first.equalsIgnoreCase("toggle")) {
            Player target = args.has(1) ? args.onlinePlayer(1) : args.player();
            String key = staffChat.toggleStaffChatForPlayer(target) ? "Player.StaffChat.ToggleOn" : "Player.StaffChat.ToggleOff";
            sender.sendMessage(lgm.getMessage(key, target, false));
            return true;
        }
        Player player = args.player();
        if (first.equalsIgnoreCase("disableSC")) {
            String key = staffChat.toggleDisableStaffChatForPlayer(player) ? "Player.StaffChat.Disabled" : "Player.StaffChat.Enabled";
            player.sendMessage(lgm.getMessage(key, player, false));
            return true;
        }
        staffChat.sendMessageInStaffChat(args.joined(0), player, true);
        return true;
    }
}
