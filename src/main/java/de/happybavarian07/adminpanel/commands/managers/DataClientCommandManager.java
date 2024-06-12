package de.happybavarian07.adminpanel.commands.managers;/*
 * @Author HappyBavarian07
 * @Date 23.11.2022 | 16:47
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.CommandManager;
import de.happybavarian07.adminpanel.commandmanagement.HelpCommand;
import de.happybavarian07.adminpanel.commands.subcommands.dataclientcommands.*;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

@CommandData
public class DataClientCommandManager extends CommandManager {
    @Override
    public String getCommandName() {
        return "dataclient";
    }

    @Override
    public String getCommandUsage() {
        return "/dataclient <SubCommand> (/dataclient help <Page>)";
    }

    @Override
    public String getCommandInfo() {
        return "The Command to manage the Java Socket Connection";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return AdminPanelMain.getPlugin();
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("dclient", "datac");
    }

    @Override
    public String getCommandPermissionAsString() {
        return "AdminPanel.DataClient.executeCommands";
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
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        return super.onConsoleCommand(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return super.onTabComplete(sender, command, label, args);
    }

    @Override
    public void setup() {
        commands.add(new HelpCommand(getCommandName()));

        commands.add(new PermissonsSyncCommand(getCommandName()));
        commands.add(new TrollsSyncCommand(getCommandName()));
        commands.add(new MenuCommand(getCommandName()));
        commands.add(new ClientListListCommand(getCommandName()));
        commands.add(new ClientListUpdateCommand(getCommandName()));
    }
}
