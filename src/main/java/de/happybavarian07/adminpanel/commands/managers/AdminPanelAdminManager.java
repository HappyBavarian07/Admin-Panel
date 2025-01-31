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
        return plugin;
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
    public String getCommandPermissionAsString() {
        return "Adminpanel.AdminPanelAdmin";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
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

        registerSubCommand(new ReloadConfigCommand(getCommandName()));
        registerSubCommand(new ReloadDataCommand(getCommandName()));
        registerSubCommand(new ReloadLangCommand(getCommandName()));
        registerSubCommand(new ReloadPluginCommand(getCommandName()));
        registerSubCommand(new ReloadCommandManagersCommand(getCommandName()));
        registerSubCommand(new UpdateConfigCommand(getCommandName()));
        registerSubCommand(new UpdateLangFilesCommand(getCommandName()));
        registerSubCommand(new StartUpLoggerControlCommand(getCommandName()));
        registerSubCommand(new BugReportCommand(getCommandName()));
        registerSubCommand(new BackupManagerCommand(getCommandName()));
        registerSubCommand(new PermissionMenuHelpCommand(getCommandName()));
        registerSubCommand(new StaffChatCommand(getCommandName()));
    }
}
