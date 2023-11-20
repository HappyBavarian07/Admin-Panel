package de.happybavarian07.adminpanel.commands.subcommands.disableditems;/*
 * @Author HappyBavarian07
 * @Date 14.05.2022 | 20:52
 */

import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.language.PlaceholderType;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class EnableCommand extends SubCommand {
    public EnableCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        if(args.length != 1) {
            return false;
        }
        Material material = Material.matchMaterial(args[0]);
        if(material == null) {
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%material%", args[0], false);
            player.sendMessage(lgm.getMessage("Player.Commands.", player, true));
            // TODO Finish
        }
        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        return false;
    }

    @Override
    public String name() {
        return "enable";
    }

    @Override
    public String info() {
        return "The Enable Command";
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
        return "/ditems enable <Material>";
    }

    @Override
    public String permissionAsString() {
        return "AdminPanel.DisabledItems.Enable";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }
}
