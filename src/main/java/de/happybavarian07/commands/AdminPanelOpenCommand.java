package de.happybavarian07.commands;

import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.menusystem.menu.AdminPanelStartMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class AdminPanelOpenCommand implements CommandExecutor {
    private final AdminPanelMain plugin;
    private final LanguageManager lgm;

    public AdminPanelOpenCommand() {
        this.plugin = AdminPanelMain.getPlugin();
        this.lgm = AdminPanelMain.getPlugin().getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("adminpanel")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
                    plugin.getFileLogger().writeToLog(Level.INFO, "Opened Admin-Panel for " + player.getName() + "(UUID: " + player.getUniqueId() + ")", "ActionsLogger - Player");
                } else {
                    sender.sendMessage(lgm.getMessage("Console.ExecutesPlayerCommand", null));
                }
            } else if (args.length == 1) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Player target = Bukkit.getPlayerExact(args[0]);
                    String targetPlayerIsNull = lgm.getMessage("Player.General.TargetedPlayerIsNull", player);
                    String openingMessageSelfOpenedForOther = lgm.getMessage("Player.General.OpeningMessageSelfOpenedForOther", target);
                    if (player.hasPermission("AdminPanel.open.other")) {
                        try {
                            new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(target)).open();
                            player.sendMessage(openingMessageSelfOpenedForOther);
                            plugin.getFileLogger().writeToLog(Level.INFO, player.getName() + "(UUID: " + player.getUniqueId() + ")" + " opened Admin-Panel for " + target.getName() + "(UUID: " + target.getUniqueId() + ")", "ActionsLogger - Player");
                        } catch (NullPointerException e) {
                            player.sendMessage(targetPlayerIsNull);
                        }
                    } else {
                        player.sendMessage(lgm.getMessage("Player.General.NoPermissions", player));
                    }
                }
                if (sender instanceof ConsoleCommandSender) {
                    ConsoleCommandSender console = (ConsoleCommandSender) sender;
                    Player target = Bukkit.getPlayerExact(args[0]);
                    String targetplayerisnull = lgm.getMessage("Player.General.TargetedPlayerIsNull", null);
                    String openingMessageSelfOpenedForOther = lgm.getMessage("Player.General.OpeningMessageSelfOpenedForOther", target);
                    try {
                        new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(target)).open();
                        console.sendMessage(openingMessageSelfOpenedForOther);
                        plugin.getFileLogger().writeToLog(Level.INFO, "Console opened Admin-Panel for " + target.getName() + "(UUID: " + target.getUniqueId() + ")", "ActionsLogger - Player");
                    } catch (NullPointerException e) {
                        console.sendMessage(targetplayerisnull);
                    }
                }
            } else {
                sender.sendMessage(lgm.getMessage("Player.ToManyArguments", null));
            }
        }
        return true;
    }
}
