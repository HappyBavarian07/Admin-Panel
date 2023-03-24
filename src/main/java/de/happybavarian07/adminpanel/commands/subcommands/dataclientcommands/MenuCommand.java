package de.happybavarian07.adminpanel.commands.subcommands.dataclientcommands;/*
 * @Author HappyBavarian07
 * @Date 23.11.2022 | 16:53
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import org.bukkit.Bukkit;
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
    public boolean onPlayerCommand(Player player, String[] args) {
        return false;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        return false;
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
    public String permission() {
        return "";
    }
}
