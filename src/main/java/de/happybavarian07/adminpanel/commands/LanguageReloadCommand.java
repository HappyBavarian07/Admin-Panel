package de.happybavarian07.adminpanel.commands;/*
 * @Author HappyBavarian07
 * @Date 11.04.2022 | 16:21
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.language.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class LanguageReloadCommand implements CommandExecutor {
    private final LanguageManager lgm;

    public LanguageReloadCommand() {
        lgm = AdminPanelMain.getPlugin().getLanguageManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("reloadlang")) {
            if (!sender.hasPermission("AdminPanel.ReloadLanguages")) {
                sender.sendMessage(lgm.getMessage("Player.General.NoPermissions", null, true));
                return true;
            }
            lgm.reloadLanguages(sender, true);
        }
        return true;
    }
}
