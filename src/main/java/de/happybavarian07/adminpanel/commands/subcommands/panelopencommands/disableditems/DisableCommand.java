package de.happybavarian07.adminpanel.commands.subcommands.panelopencommands.disableditems;/*
 * @Author HappyBavarian07
 * @Date 14.05.2022 | 20:52
 */

import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class DisableCommand extends SubCommand {
    public DisableCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        return false;
        // TODO Finish
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        return false;
    }

    @Override
    public String name() {
        return "disable";
    }

    @Override
    public String info() {
        return "The Disable Command";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public Map<Integer, String[]> subArgs() {
        Map<Integer, String[]> subArgs = new HashMap<>();
        String[] materials = new String[Material.values().length];
        int count = 0;
        for (Material material : Material.values()) {
            materials[count] = String.valueOf(material);
            count++;
        }
        subArgs.put(1, materials);
        return subArgs;
    }

    @Override
    public String syntax() {
        return "/ditems disable <Material>";
    }

    @Override
    public String permission() {
        return "AdminPanel.DisabledItems.Disable";
    }
}
