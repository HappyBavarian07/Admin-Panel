package de.happybavarian07.adminpanel.commands;

import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.language.LanguageManager;
import de.happybavarian07.adminpanel.utils.LogPrefix;
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
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("adminpanel")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
                    plugin.getFileLogger().writeToLog(Level.INFO, "Opened Admin-Panel for " + player.getName() + "(UUID: " + player.getUniqueId() + ")", LogPrefix.ACTIONSLOGGER_PLAYER);
                } else {
                    sender.sendMessage(lgm.getMessage("Console.ExecutesPlayerCommand", null, true));
                }
            } else if (args.length == 1) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Player target = Bukkit.getPlayerExact(args[0]);
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", args[0], true);
                    String targetPlayerIsNull = lgm.getMessage("Player.General.TargetedPlayerIsNull", player, false);
                    String openingMessageSelfOpenedForOther = lgm.getMessage("Player.General.OpeningMessageSelfForOther", player, true);
                    if (player.hasPermission("AdminPanel.open.other")) {
                        try {
                            new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(target)).open();
                            player.sendMessage(openingMessageSelfOpenedForOther);
                            plugin.getFileLogger().writeToLog(Level.INFO, player.getName() + "(UUID: " + player.getUniqueId() + ")" + " opened Admin-Panel for " + target.getName() + "(UUID: " + target.getUniqueId() + ")", LogPrefix.ACTIONSLOGGER_PLAYER);
                        } catch (NullPointerException e) {
                            player.sendMessage(targetPlayerIsNull);
                        }
                    } else {
                        player.sendMessage(lgm.getMessage("Player.General.NoPermissions", player, true));
                    }
                }
                if (sender instanceof ConsoleCommandSender) {
                    ConsoleCommandSender console = (ConsoleCommandSender) sender;
                    Player target = Bukkit.getPlayerExact(args[0]);
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", args[0], true);
                    String targetplayerisnull = lgm.getMessage("Player.General.TargetedPlayerIsNull", null, false);
                    String openingMessageSelfOpenedForOther = lgm.getMessage("Player.General.OpeningMessageSelfForOther", target, true);
                    try {
                        new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(target)).open();
                        console.sendMessage(openingMessageSelfOpenedForOther);
                        plugin.getFileLogger().writeToLog(Level.INFO, "Console opened Admin-Panel for " + target.getName() + "(UUID: " + target.getUniqueId() + ")", LogPrefix.ACTIONSLOGGER_PLAYER);
                    } catch (NullPointerException e) {
                        console.sendMessage(targetplayerisnull);
                    }
                }
            } else {
                sender.sendMessage(lgm.getMessage("Player.ToManyArguments", null, true));
            }
        }
        return true;
    }
}
