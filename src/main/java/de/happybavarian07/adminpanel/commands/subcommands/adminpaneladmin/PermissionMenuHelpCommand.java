package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;/*
 * @Author HappyBavarian07
 * @Date 02.10.2023 | 17:23
 */

import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PermissionMenuHelpCommand extends SubCommand {
    public PermissionMenuHelpCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Permission Menu Help:");
        player.sendMessage(ChatColor.YELLOW + "Fields for Permission Search:");
        player.sendMessage(ChatColor.GRAY + " - permissionName");
        player.sendMessage(ChatColor.GRAY + " - permissionDescription");
        player.sendMessage(ChatColor.GRAY + " - permissionDefault");
        player.sendMessage(ChatColor.GRAY + " - permissionChildren");

        player.sendMessage(ChatColor.YELLOW + "TF-IDF Search Algorithm for Permissions:");
        player.sendMessage(ChatColor.GRAY + "1. Simple Term Query:");
        player.sendMessage(ChatColor.GRAY + "   Use a single term to search.");
        player.sendMessage(ChatColor.GRAY + "   Example: 'adminpanel'");

        player.sendMessage(ChatColor.YELLOW + "2. Boolean Query:");
        player.sendMessage(ChatColor.GRAY + "   Use 'AND', 'OR', and 'NOT' to combine terms.");
        player.sendMessage(ChatColor.GRAY + "   Example: 'adminpanel OR vault'");

        player.sendMessage(ChatColor.YELLOW + "3. Phrase Query:");
        player.sendMessage(ChatColor.GRAY + "   Use double quotes to search for exact phrases.");
        player.sendMessage(ChatColor.GRAY + "   Example: '\"AdminPanel.open\"'");

        player.sendMessage(ChatColor.YELLOW + "4. Wildcard Query:");
        player.sendMessage(ChatColor.GRAY + "   Use '*' or '?' to match a pattern.");
        player.sendMessage(ChatColor.GRAY + "   Example: 'permissionName:admin*'");

        player.sendMessage(ChatColor.YELLOW + "5. Fuzzy Query:");
        player.sendMessage(ChatColor.GRAY + "   Use '~' to perform a fuzzy search.");
        player.sendMessage(ChatColor.GRAY + "   Example: 'permissionDescription:chat~'");

        player.sendMessage(ChatColor.YELLOW + "6. Range Query:");
        player.sendMessage(ChatColor.GRAY + "   Use '[a TO f]' to search within a range.");
        player.sendMessage(ChatColor.GRAY + "   Example: 'permissionDefault:[a TO f]'");

        player.sendMessage(ChatColor.YELLOW + "7. More Complex Query:");
        player.sendMessage(ChatColor.GRAY + "   Combine terms with 'AND', 'OR', and 'NOT'.");
        player.sendMessage(ChatColor.GRAY + "   Example: 'permissionChildren:spigot AND permissionDefault:OP'");
        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        return false;
    }

    @Override
    public String name() {
        return "permissionmenuhelp";
    }

    @Override
    public String info() {
        return "Command to display help for the Permission Menu";
    }

    @Override
    public String[] aliases() {
        return new String[]{"pmh", "permhelp", "permissionhelp"};
    }

    @Override
    public Map<Integer, String[]> subArgs() {
        return new HashMap<>();
    }

    @Override
    public String syntax() {
        return "/permissionmenu help";
    }

    @Override
    public String permissionAsString() {
        return "AdminPanel.PlayerManager.PlayerSettings.Permissions.MenuHelp";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }
}
