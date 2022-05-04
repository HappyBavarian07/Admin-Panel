package de.happybavarian07.adminpanel.commands.managers;/*
 * @Author HappyBavarian07
 * @Date 27.04.2022 | 17:07
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.CommandManager;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.commands.subcommands.panelopencommands.HelpCommand;
import de.happybavarian07.adminpanel.commands.subcommands.panelopencommands.panels.playermanager.*;
import de.happybavarian07.adminpanel.commands.subcommands.panelopencommands.panels.pluginmanager.*;
import de.happybavarian07.adminpanel.commands.subcommands.panelopencommands.panels.servermanager.*;
import de.happybavarian07.adminpanel.commands.subcommands.panelopencommands.panels.worldmanager.*;
import de.happybavarian07.adminpanel.commands.subcommands.panelopencommands.panels.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

@CommandData(playerRequired = false)
public class PanelOpenManager extends CommandManager {
    @Override
    public String getCommandName() {
        return "openpanel";
    }

    @Override
    public String getCommandUsage() {
        /*
         */
        return "/openpanel <SubCommand> (openpanel help <Page>)";
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
    public String getCommandPermission() {
        return "";
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
        commands.add(new HelpCommand());

        // Panels without Args
        commands.add(new BannedPlayersMenuCommand());
        commands.add(new PlayerSelectMenuCommand());
        commands.add(new PluginAutoUpdaterMenuCommand());
        commands.add(new PluginInstallMenuCommand());
        commands.add(new PluginSelectMenuCommand());
        commands.add(new ChatManagerMenuCommand());
        commands.add(new ServerManagerMenuCommand());
        commands.add(new WhitelistedPlayersMenuCommand());
        commands.add(new WhitelistManagerMenuCommand());
        commands.add(new WorldCreateMenuCommand());
        commands.add(new WorldSelectMenuCommand());
        commands.add(new AdminPanelStartMenuCommand());
        commands.add(new TempLanguageSelectMenuCommand());

        // Panels with Args
        // etc
    }

    @Override
    public List<SubCommand> getSubCommands() {
        return commands;
    }
}
