package de.happybavarian07.adminpanel.commands.managers;/*
 * @Author HappyBavarian07
 * @Date 17.06.2022 | 21:38
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.CommandManager;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.commandmanagement.HelpCommand;
import de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

@CommandData
public class AdminPanelAdminManager extends CommandManager {
    @Override
    public String getCommandName() {
        return "adminpaneladmin";
    }

    @Override
    public String getCommandUsage() {
        return "/adminpaneladmin <reload|reloadconfig|reloadlang|reloaddata>";
    }

    @Override
    public String getCommandInfo() {
        return "The Admin Command to reload Stuff";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return adminpanel;
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("apadmin");
        aliases.add("adminpadmin");
        aliases.add("apaneladmin");
        return aliases;
    }

    @Override
    public String getCommandPermission() {
        return "Adminpanel.AdminPanelAdmin";
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

        commands.add(new ReloadConfigCommand(getCommandName()));
        commands.add(new ReloadDataCommand(getCommandName()));
        commands.add(new ReloadLangCommand(getCommandName()));
        commands.add(new ReloadPluginCommand(getCommandName()));
        commands.add(new ReloadCommandManagersCommand(getCommandName()));
        commands.add(new UpdateConfigCommand(getCommandName()));
        commands.add(new UpdateLangFilesCommand(getCommandName()));
        commands.add(new StartUpLoggerControlCommand(getCommandName()));
        commands.add(new BugReportCommand(getCommandName()));
        commands.add(new BackupManagerCommand(getCommandName()));
    }

    @Override
    public List<SubCommand> getSubCommands() {
        return commands;
    }
}
