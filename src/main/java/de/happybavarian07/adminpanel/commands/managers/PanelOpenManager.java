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
        return adminpanel;
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
    public boolean onPlayerCommand(Player player, String[] args) {
        return super.onPlayerCommand(player, args);
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender player, String[] args) {
        return super.onConsoleCommand(player, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return super.onTabComplete(sender, command, label, args);
    }

    @Override
    public void setup() {
        commands.add(new HelpCommand(getCommandName()));

        // Panels without Args
        commands.add(new BannedPlayersMenuCommand(getCommandName()));
        commands.add(new PlayerSelectMenuCommand(getCommandName()));
        commands.add(new PluginAutoUpdaterMenuCommand(getCommandName()));
        commands.add(new PluginInstallMenuCommand(getCommandName()));
        commands.add(new PluginSelectMenuCommand(getCommandName()));
        commands.add(new ChatManagerMenuCommand(getCommandName()));
        commands.add(new ServerManagerMenuCommand(getCommandName()));
        commands.add(new WhitelistedPlayersMenuCommand(getCommandName()));
        commands.add(new WhitelistManagerMenuCommand(getCommandName()));
        commands.add(new WorldCreateMenuCommand(getCommandName()));
        commands.add(new WorldSelectMenuCommand(getCommandName()));
        commands.add(new AdminPanelStartMenuCommand(getCommandName()));
        commands.add(new TempLanguageSelectMenuCommand(getCommandName()));

        // Panels with Args
        // etc
    }

    @Override
    public List<SubCommand> getSubCommands() {
        return commands;
    }
}
