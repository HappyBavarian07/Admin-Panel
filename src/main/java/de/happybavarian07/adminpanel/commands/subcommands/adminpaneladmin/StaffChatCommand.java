package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;

import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.listeners.StaffChatHandler;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffChatCommand extends SubCommand {
    private final StaffChatHandler staffChatHandler;

    public StaffChatCommand(String mainCommandName) {
        super(mainCommandName);
        this.staffChatHandler = this.plugin.getStaffChatHandler();
    }

    public boolean handleCommand(CommandSender sender, Player playerOrNull, String[] args) {
        if (args.length == 0) {
            return false;
        } else if (args[0].equalsIgnoreCase("toggle")) {
            if (args.length == 2) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(this.lgm.getMessage("Player.General.TargetedPlayerIsNull", playerOrNull, false));
                    return false;
                } else {
                    if (this.staffChatHandler.toggleStaffChatForPlayer(target)) {
                        sender.sendMessage(this.lgm.getMessage("Player.StaffChat.ToggleOn", target, false));
                    } else {
                        sender.sendMessage(this.lgm.getMessage("Player.StaffChat.ToggleOff", target, false));
                    }

                    return true;
                }
            } else if (playerOrNull == null) {
                sender.sendMessage(this.lgm.getMessage("Console.ExecutesPlayerCommand", (Player)null, false));
                return false;
            } else {
                if (this.staffChatHandler.toggleStaffChatForPlayer(playerOrNull)) {
                    playerOrNull.sendMessage(this.lgm.getMessage("Player.StaffChat.ToggleOn", playerOrNull, false));
                } else {
                    playerOrNull.sendMessage(this.lgm.getMessage("Player.StaffChat.ToggleOff", playerOrNull, false));
                }

                return true;
            }
        } else if (args[0].equalsIgnoreCase("disableSC")) {
            if (playerOrNull == null) {
                sender.sendMessage(this.lgm.getMessage("Console.ExecutesPlayerCommand", (Player)null, false));
                return false;
            } else {
                if (this.staffChatHandler.toggleDisableStaffChatForPlayer(playerOrNull)) {
                    playerOrNull.sendMessage(this.lgm.getMessage("Player.StaffChat.Disabled", playerOrNull, false));
                } else {
                    playerOrNull.sendMessage(this.lgm.getMessage("Player.StaffChat.Enabled", playerOrNull, false));
                }

                return true;
            }
        } else if (playerOrNull == null) {
            sender.sendMessage(this.lgm.getMessage("Console.ExecutesPlayerCommand", (Player)null, false));
            return false;
        } else {
            String messageFromArgs = String.join(" ", args);
            this.staffChatHandler.sendMessageInStaffChat(messageFromArgs, playerOrNull, true);
            return true;
        }
    }

    public String name() {
        return "staffchat";
    }

    public String info() {
        return "This Command is used to toggle the StaffChat on or off and send messages.";
    }

    public String[] aliases() {
        return new String[]{"sc", "staffc"};
    }

    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        Map<Integer, String[]> subArgs = new HashMap();
        subArgs.put(1, new String[]{"toggle", "disableSC", "[message...]"});
        return subArgs;
    }

    public String syntax() {
        return "/adminpaneladmin staffchat <toggle|[message...]>";
    }

    public String permissionAsString() {
        return "AdminPanel.AdminPanelAdminCommands.StaffChat";
    }

    public boolean autoRegisterPermission() {
        return true;
    }
}