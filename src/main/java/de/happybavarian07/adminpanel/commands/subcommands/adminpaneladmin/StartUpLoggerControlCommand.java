package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;/*
 * @Author HappyBavarian07
 * @Date 17.12.2022 | 20:43
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@CommandData()
public class StartUpLoggerControlCommand extends SubCommand {
    public StartUpLoggerControlCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        if (args.length != 1) return false;
        if (args[0].equalsIgnoreCase("Enable")) {
            plugin.getStartUpLogger().enableMessageSystem();
            player.sendMessage("StartUpLogger/ConsoleLogger System enabled!");
        } else if (args[0].equalsIgnoreCase("Disable")) {
            plugin.getStartUpLogger().disableMessageSystem();
            player.sendMessage("StartUpLogger/ConsoleLogger System disabled!");
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        if (args.length != 1) return false;
        if (args[0].equalsIgnoreCase("Enable")) {
            plugin.getStartUpLogger().enableMessageSystem();
            sender.sendMessage("StartUpLogger/ConsoleLogger System enabled!");
        } else if (args[0].equalsIgnoreCase("Disable")) {
            plugin.getStartUpLogger().disableMessageSystem();
            sender.sendMessage("StartUpLogger/ConsoleLogger System disabled!");
        } else {
            return false;
        }
        return true;
    }

    @Override
    public String name() {
        return "StartUpLoggerControl";
    }

    @Override
    public String info() {
        return "Controls the Start Up Logger";
    }

    @Override
    public String[] aliases() {
        return new String[]{"SULControl"};
    }

    @Override
    public Map<Integer, String[]> subArgs() {
        Map<Integer, String[]> args = new HashMap<>();
        args.put(1, new String[] {"Enable", "Disable"});
        return args;
    }

    @Override
    public String syntax() {
        return "/apadmin SULControl <Enable|Disable>";
    }

    @Override
    public String permissionAsString() {
        return "AdminPanel.AdminPanelAdminCommands.StartUpLoggerControl";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }
}
