package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;/*
 * @Author HappyBavarian07
 * @Date 17.06.2022 | 22:05
 */

import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.utils.PluginUtils;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class UpdateLangFilesCommand extends SubCommand {
    public UpdateLangFilesCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        player.sendMessage(Utils.chat("&7Starting Update of the Lang Files!"));
        try {
            lgm.updateLangFiles();
            lgm.reloadLanguages(player, true);
            player.sendMessage(Utils.chat("&7Update finished!"));
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(Utils.chat("&cAn Error occurred while updating the Lang Files: &6" + e.getMessage() + " &7(Please check the Console)."));
        }
        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        sender.sendMessage(Utils.chat("&7Starting Update of the Lang Files!"));
        try {
            lgm.updateLangFiles();
            lgm.reloadLanguages(sender, true);
            sender.sendMessage(Utils.chat("&7Update finished!"));
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(Utils.chat("&cAn Error occurred while updating the Lang Files: &6" + e.getMessage() + " &7(Please check the Console)."));
        }
        return true;
    }

    @Override
    public String name() {
        return "updatelangfiles";
    }

    @Override
    public String info() {
        return "The Command to update the Lang Files";
    }

    @Override
    public String[] aliases() {
        return new String[] {"updatelang"};
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        return new HashMap<>();
    }

    @Override
    public String syntax() {
        return "/apadmin updatelang";
    }

    @Override
    public String permissionAsString() {
        return "AdminPanel.AdminPanelAdminCommands.UpdateLangFiles";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }
}
