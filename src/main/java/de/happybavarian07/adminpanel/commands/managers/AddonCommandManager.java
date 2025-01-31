package de.happybavarian07.adminpanel.commands.managers;/*
 * @Author HappyBavarian07
 * @Date 17.06.2022 | 21:38
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.CommandManager;
import de.happybavarian07.adminpanel.commandmanagement.HelpCommand;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.commands.subcommands.addoncommands.*;
import de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

@CommandData
public class AddonCommandManager extends CommandManager {
    @Override
    public String getCommandName() {
        return "addon";
    }

    @Override
    public String getCommandUsage() {
        return "/addon <SubCommand> [args]";
    }

    @Override
    public String getCommandInfo() {
        return "The Addon Command to manage the Addons of the AdminPanel.";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return plugin;
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<>();
    }

    @Override
    public String getCommandPermissionAsString() {
        return "Adminpanel.AddonManageCMD";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        return super.onCommand(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return super.onTabComplete(sender, command, label, args);
    }

    @Override
    public void setup() {
        registerSubCommand(new HelpCommand(getCommandName()));

        registerSubCommand(new AddonListCommand(getCommandName()));
        registerSubCommand(new AddonEnableCommand(getCommandName()));
        registerSubCommand(new AddonDisableCommand(getCommandName()));
        registerSubCommand(new AddonReloadCommand(getCommandName()));
        registerSubCommand(new AddonInfoCommand(getCommandName()));
        // Add more commands here maybe
    }
}
