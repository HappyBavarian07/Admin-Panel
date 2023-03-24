package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;/*
 * @Author HappyBavarian07
 * @Date 17.06.2022 | 22:05
 */

import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ReloadCommandManagersCommand extends SubCommand {
    public ReloadCommandManagersCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        player.sendMessage(Utils.chat("&7Starting Reload of the Command Managers!"));
        try {
            plugin.getCommandManagerRegistry().unregisterAll();
            plugin.getInitMethods().initCommandManagers();
            player.sendMessage(Utils.chat("&7Reload finished!"));
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(Utils.chat("&cAn Error occurred while reloading the Command Managers: &6" + e.getMessage() + " &7(Please check the Console)."));
        }
        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        sender.sendMessage(Utils.chat("&7Starting Reload of the Command Managers!"));
        try {
            plugin.getCommandManagerRegistry().unregisterAll();
            plugin.getInitMethods().initCommandManagers();
            sender.sendMessage(Utils.chat("&7Reload finished!"));
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(Utils.chat("&cAn Error occurred while reloading the Command Managers: &6" + e.getMessage() + " &7(Please check the Console)."));
        }
        return true;
    }

    @Override
    public String name() {
        return "reloadcommandmanagers";
    }

    @Override
    public String info() {
        return "The Command to reload the Command Managers";
    }

    @Override
    public String[] aliases() {
        return new String[] {"reloadcm"};
    }

    @Override
    public Map<Integer, String[]> subArgs() {
        return new HashMap<>();
    }

    @Override
    public String syntax() {
        return "/apadmin reloadcm";
    }

    @Override
    public String permission() {
        return "AdminPanel.AdminPanelAdminCommands.ReloadCommandManagers";
    }
}
