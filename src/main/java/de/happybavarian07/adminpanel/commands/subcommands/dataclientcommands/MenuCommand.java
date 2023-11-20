package de.happybavarian07.adminpanel.commands.subcommands.dataclientcommands;/*
 * @Author HappyBavarian07
 * @Date 23.11.2022 | 16:53
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.dataclient.DataClientMainMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@CommandData(playerRequired = true)
public class MenuCommand extends SubCommand {
    public MenuCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Player playerOrNull, String[] args) {
        if(playerOrNull == null) {
            sender.sendMessage(lgm.getMessage("Console.ExecutesPlayerCommand", null, false));
            return false;
        }
        if(args.length == 0) {
            new DataClientMainMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(playerOrNull)).open();
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(target == null) {
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", args[0], false);
            sender.sendMessage(lgm.getMessage("General.TargetedPlayerIsNull", playerOrNull, true));
            return false;
        }

        new DataClientMainMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(target)).open();
        return true;
    }

    @Override
    public String name() {
        return "menu";
    }

    @Override
    public String info() {
        return "Opens the Menu of the Data Client";
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
        return "/dataclient menu [Player]";
    }

    @Override
    public String permissionAsString() {
        return "AdminPanel.DataClient.Menu.Open";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }
}
