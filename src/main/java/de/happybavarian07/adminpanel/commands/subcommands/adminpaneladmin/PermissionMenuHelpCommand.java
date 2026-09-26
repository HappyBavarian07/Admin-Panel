package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SubCommandInfo(name = "permissionmenuhelp", aliases = {"pmh", "permhelp", "permissionhelp"},
        info = "Command to display help for the Permission Menu",
        permission = "AdminPanel.PlayerManager.PlayerSettings.Permissions.MenuHelp", autoRegisterPermission = false)
public class PermissionMenuHelpCommand extends SubCommand {
    public PermissionMenuHelpCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        Player player = sender instanceof Player p ? p : null;
        List<String> lines = lgm.getCustomObject("AdminPanelAdmin.PermissionMenuHelp", player, new ArrayList<>(), false);
        for (String line : lines) sender.sendMessage(AdminPanelUtils.format(player, line, AdminPanelMain.getPrefix()));
        return true;
    }
}
