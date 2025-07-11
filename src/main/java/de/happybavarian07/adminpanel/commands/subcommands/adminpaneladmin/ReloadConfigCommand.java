package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;/*
 * @Author HappyBavarian07
 * @Date 17.06.2022 | 22:05
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ReloadConfigCommand extends SubCommand {
    public ReloadConfigCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        player.sendMessage(AdminPanelUtils.chat("&7Starting Reload of the Config!"));
        try {
            AdminPanelMain.getPlugin().reloadConfig();
            player.sendMessage(AdminPanelUtils.chat("&7Reload finished!"));
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(AdminPanelUtils.chat("&cAn Error occurred while reloading the Config: &6" + e.getMessage() + " &7(Please check the Console)."));
        }
        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        sender.sendMessage(AdminPanelUtils.chat("&7Starting Reload of the Config!"));
        try {
            AdminPanelMain.getPlugin().reloadConfig();
            sender.sendMessage(AdminPanelUtils.chat("&7Reload finished!"));
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(AdminPanelUtils.chat("&cAn Error occurred while reloading the Config: &6" + e.getMessage() + " &7(Please check the Console)."));
        }
        return true;
    }

    @Override
    public String name() {
        return "reloadconfig";
    }

    @Override
    public String info() {
        return "The Command to reload the Config";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        return new HashMap<>();
    }

    @Override
    public String syntax() {
        return "/apadmin reloadconfig";
    }

    @Override
    public String permissionAsString() {
        return "AdminPanel.AdminPanelAdminCommands.ReloadConfig";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }
}
