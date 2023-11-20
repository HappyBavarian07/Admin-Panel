package de.happybavarian07.adminpanel.commands.managers;/*
 * @Author HappyBavarian07
 * @Date 14.05.2022 | 20:49
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandManager;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.commandmanagement.HelpCommand;
import de.happybavarian07.adminpanel.commands.subcommands.disableditems.DisableCommand;
import de.happybavarian07.adminpanel.commands.subcommands.disableditems.EnableCommand;
import de.happybavarian07.adminpanel.commands.subcommands.disableditems.ListCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class DisabledItemsManager extends CommandManager {
    @Override
    public String getCommandName() {
        return "disableditems";
    }

    @Override
    public String getCommandUsage() {
        return "/disableditems <enable|disable|list> [Material]";
    }

    @Override
    public String getCommandInfo() {
        return "The Disable Item Command";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return adminpanel;
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("ditems");
        return aliases;
    }

    @Override
    public String getCommandPermissionAsString() {
        return "";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }

    @Override
    public boolean onCommand(Player player, String[] args) {
        return super.onCommand(player, args);
    }

    @Override
    public boolean onCommand(ConsoleCommandSender player, String[] args) {
        return super.onCommand(player, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return super.onTabComplete(sender, command, label, args);
    }

    @Override
    public void setup() {
        commands.add(new HelpCommand(getCommandName()));

        commands.add(new EnableCommand(getCommandName()));
        commands.add(new DisableCommand(getCommandName()));
        commands.add(new ListCommand(getCommandName()));
    }

    @Override
    public List<SubCommand> getSubCommands() {
        return commands;
    }
}
