package de.happybavarian07.commands;

import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.menusystem.menu.AdminPanelStartMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminPanelOpenCommand implements CommandExecutor {
    private final LanguageManager lgm;

    public AdminPanelOpenCommand() {
        this.lgm = Main.getPlugin().getLanguageManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("adminpanel") || command.getName().equalsIgnoreCase("apanel") || command.getName().equalsIgnoreCase("adminp") || command.getName().equalsIgnoreCase("ap")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    new AdminPanelStartMenu(Main.getAPI().getPlayerMenuUtility(player)).open();
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
                            new AdminPanelStartMenu(Main.getAPI().getPlayerMenuUtility(target)).open();
                            player.sendMessage(openingMessageSelfOpenedForOther);
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
                        new AdminPanelStartMenu(Main.getAPI().getPlayerMenuUtility(target)).open();
                        console.sendMessage(openingMessageSelfOpenedForOther);
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
