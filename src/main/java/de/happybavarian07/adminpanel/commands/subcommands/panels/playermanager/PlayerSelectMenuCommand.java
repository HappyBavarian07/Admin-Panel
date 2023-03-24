package de.happybavarian07.adminpanel.commands.subcommands.panels.playermanager;/*
 * @Author HappyBavarian07
 * @Date 27.04.2022 | 17:26
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.PlaceholderType;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.PlayerSelectMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@CommandData
public class PlayerSelectMenuCommand extends SubCommand {
    public PlayerSelectMenuCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        if (args.length == 0) {
            new PlayerSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
            player.sendMessage(lgm.getMessage("Player.General.OpeningMessageSelf", player, true));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", args[0], true);
        if (target == null) {
            player.sendMessage(lgm.getMessage("Player.General.TargetedPlayerIsNull", player, true));
            return true;
        }

        new PlayerSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(target)).open();
        player.sendMessage(lgm.getMessage("Player.General.OpeningMessageSelfForOther", player, true));

        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(lgm.getMessage("Console.ExecutesPlayerCommand", null, true));
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", args[0], true);
        if (target == null) {
            sender.sendMessage(lgm.getMessage("Player.General.TargetedPlayerIsNull", null, true));
            return true;
        }

        new PlayerSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(target)).open();
        sender.sendMessage(lgm.getMessage("Player.General.OpeningMessageSelfForOther", null, true));
        return true;
    }

    @Override
    public String name() {
        return "PlayerSelectMenu";
    }

    @Override
    public String info() {
        return "Opens the PlayerSelectMenu";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public Map<Integer, String[]> subArgs() {
        Map<Integer, String[]> subArgs = new HashMap<>();
        String[] playerNames = new String[Bukkit.getOnlinePlayers().size()];
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerNames[count] = player.getName();
            count++;
        }
        subArgs.put(1, playerNames);
        return subArgs;
    }

    @Override
    public String syntax() {
        return "/openpanel PlayerSelectMenu";
    }

    @Override
    public String permission() {
        return new PlayerSelectMenu(null).getOpeningPermission();
    }
}