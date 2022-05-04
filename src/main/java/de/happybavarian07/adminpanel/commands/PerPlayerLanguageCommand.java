package de.happybavarian07.adminpanel.commands;/*
 * @Author HappyBavarian07
 * @Date 26.04.2022 | 19:33
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.LanguageManager;
import de.happybavarian07.adminpanel.main.PlaceholderType;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PerPlayerLanguageCommand implements CommandExecutor, TabCompleter {
    private final LanguageManager lgm;
    private final List<String> completerArgs = new ArrayList<>();

    public PerPlayerLanguageCommand() {
        this.lgm = AdminPanelMain.getPlugin().getLanguageManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(lgm.getMessage("Console.ExecutesPlayerCommand", null, true));
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 1) {
            if (!sender.hasPermission("AdminPanel.PerPlayerLang")) {
                sender.sendMessage(lgm.getMessage("Player.General.NoPermissions", (Player) sender, true));
                return true;
            }
            UUID playerUUID = player.getUniqueId();
            String langName = args[0];
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%langname%", langName, true);
            if (langName.equals("default")) {
                lgm.getPlhandler().removePlayerLanguage(playerUUID);
                player.sendMessage(lgm.getMessage("Player.General.PerPlayerLangSet", player, true));
                return true;
            }
            if (lgm.getLang(langName, false) == null) {
                player.sendMessage(lgm.getMessage("Player.General.NotAValidLanguage", player, true));
                return true;
            }
            lgm.getPlhandler().setPlayerLanguage(playerUUID, langName);
            player.sendMessage(lgm.getMessage("Player.General.PerPlayerLangSet", player, true));
        } else if (args.length == 2) {
            if (!sender.hasPermission("AdminPanel.PerPlayerLangOther")) {
                sender.sendMessage(lgm.getMessage("Player.General.NoPermissions", (Player) sender, true));
                return true;
            }
            String langName = args[0];
            Player target = Bukkit.getPlayer(args[1]);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%langname%", langName, true);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", args[1], false);
            if (target == null) {
                player.sendMessage(lgm.getMessage("Player.General.TargetedPlayerIsNull", player, true));
                return true;
            }
            UUID playerUUID = target.getUniqueId();
            if (langName.equals("default")) {
                lgm.getPlhandler().removePlayerLanguage(playerUUID);
                player.sendMessage(lgm.getMessage("Player.General.PerPlayerLangSetOther", player, true));
                return true;
            }
            if (lgm.getLang(langName, false) == null) {
                player.sendMessage(lgm.getMessage("Player.General.NotAValidLanguage", player, true));
                return true;
            }
            lgm.getPlhandler().setPlayerLanguage(playerUUID, langName);
            player.sendMessage(lgm.getMessage("Player.General.PerPlayerLangSetOther", player, true));
        } else {
            Utils.chat(AdminPanelMain.getPrefix() + "&c Usage: &6" + command.getUsage());
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("perplayerlang")) {
            if (!sender.hasPermission("AdminPanel.PerPlayerLang")) return new ArrayList<>();
            if (completerArgs.isEmpty()) {
                completerArgs.add("default");
                completerArgs.addAll(lgm.getRegisteredLanguages().keySet());
            }

            List<String> result = new ArrayList<>();
            if (args.length == 1) {
                for (String a : completerArgs) {
                    if (a.toLowerCase().startsWith(args[0].toLowerCase()))
                        result.add(a);
                }
                return result;
            }

            if (args.length > 1) {
                return null;
            }
            return completerArgs;
        }
        return new ArrayList<>();
    }
}
