package de.happybavarian07.adminpanel.commands.managers;/*
 * @Author HappyBavarian07
 * @Date 27.04.2022 | 17:07
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.CommandManager;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.commandmanagement.HelpCommand;
import de.happybavarian07.adminpanel.commands.subcommands.panels.AdminPanelStartMenuCommand;
import de.happybavarian07.adminpanel.commands.subcommands.panels.TempLanguageSelectMenuCommand;
import de.happybavarian07.adminpanel.commands.subcommands.panels.playermanager.BannedPlayersMenuCommand;
import de.happybavarian07.adminpanel.commands.subcommands.panels.playermanager.PlayerSelectMenuCommand;
import de.happybavarian07.adminpanel.commands.subcommands.panels.pluginmanager.PluginAutoUpdaterMenuCommand;
import de.happybavarian07.adminpanel.commands.subcommands.panels.pluginmanager.PluginInstallMenuCommand;
import de.happybavarian07.adminpanel.commands.subcommands.panels.pluginmanager.PluginSelectMenuCommand;
import de.happybavarian07.adminpanel.commands.subcommands.panels.servermanager.ChatManagerMenuCommand;
import de.happybavarian07.adminpanel.commands.subcommands.panels.servermanager.ServerManagerMenuCommand;
import de.happybavarian07.adminpanel.commands.subcommands.panels.servermanager.WhitelistManagerMenuCommand;
import de.happybavarian07.adminpanel.commands.subcommands.panels.servermanager.WhitelistedPlayersMenuCommand;
import de.happybavarian07.adminpanel.commands.subcommands.panels.worldmanager.WorldCreateMenuCommand;
import de.happybavarian07.adminpanel.commands.subcommands.panels.worldmanager.WorldSelectMenuCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

@CommandData
public class PanelOpenManager extends CommandManager {
    @Override
    public String getCommandName() {
        return "openpanel";
    }

    @Override
    public String getCommandUsage() {
        /*
         */
        return "/openpanel <SubCommand> (/openpanel help <Page>)";
    }

    @Override
    public String getCommandInfo() {
        return "Open specific Panels for you or other Players";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return plugin;
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.emptyList();
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

        // Panels without Args
        registerSubCommand(new BannedPlayersMenuCommand(getCommandName()));
        registerSubCommand(new PlayerSelectMenuCommand(getCommandName()));
        registerSubCommand(new PluginAutoUpdaterMenuCommand(getCommandName()));
        registerSubCommand(new PluginInstallMenuCommand(getCommandName()));
        registerSubCommand(new PluginSelectMenuCommand(getCommandName()));
        registerSubCommand(new ChatManagerMenuCommand(getCommandName()));
        registerSubCommand(new ServerManagerMenuCommand(getCommandName()));
        registerSubCommand(new WhitelistedPlayersMenuCommand(getCommandName()));
        registerSubCommand(new WhitelistManagerMenuCommand(getCommandName()));
        registerSubCommand(new WorldCreateMenuCommand(getCommandName()));
        registerSubCommand(new WorldSelectMenuCommand(getCommandName()));
        registerSubCommand(new AdminPanelStartMenuCommand(getCommandName()));
        registerSubCommand(new TempLanguageSelectMenuCommand(getCommandName()));

        // Panels with Args
        // etc
    }
}
