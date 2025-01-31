package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;/*
 * @Author HappyBavarian07
 * @Date 17.06.2022 | 22:05
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@CommandData(playerRequired = true)
public class BugReportCommand extends SubCommand {
    public BugReportCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        int response = AdminPanelMain.getAPI().reportBugToDiscord(player.getUniqueId(), args);
        if (response == -2) {
            long cooldownTimeInMillis = AdminPanelMain.getAPI().getCooldownTimeMap().get(player.getUniqueId()) - System.currentTimeMillis();
            player.sendMessage(Utils.chat("&cYou are still on Cooldown for %cooldowntime% Seconds")
                    .replace("%cooldowntime%", String.valueOf(TimeUnit.SECONDS.toSeconds(cooldownTimeInMillis))));
            return true;
        } else if (response == -1) {
            player.sendMessage(Utils.chat("&cError! Please look into the Console for more Information!"));
        } else {
            player.sendMessage(Utils.chat("&aSuccessfully send Report to the Developer. &7You can wait for the Bug Fix Update or just join my Discord."));
        }
        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        return false;
    }

    @Override
    public String name() {
        return "report";
    }

    @Override
    public String info() {
        return "The Command to report Bugs to the Developer.";
    }

    @Override
    public String[] aliases() {
        return new String[] {};
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        return new HashMap<>();
    }

    @Override
    public String syntax() {
        return "/apadmin report <Message>";
    }

    @Override
    public String permissionAsString() {
        return "AdminPanel.AdminPanelAdminCommands.ReportBug";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }
}
