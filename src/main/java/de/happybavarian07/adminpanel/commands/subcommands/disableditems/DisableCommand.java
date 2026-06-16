package de.happybavarian07.adminpanel.commands.subcommands.disableditems;/*
 * @Author HappyBavarian07
 * @Date 14.05.2022 | 20:52
 */

import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DisableCommand extends SubCommand {
    public DisableCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        if (args.length != 1) return false;

        Material material = Material.matchMaterial(args[0]);
        if (material == null) {
            player.sendMessage(AdminPanelUtils.format(player, "%prefix% &9> &cInvalid material: &e" + args[0], de.happybavarian07.adminpanel.main.AdminPanelMain.getPrefix()));
            return true;
        }

        boolean changed = AdminPanelUtils.disableVanillaItem(new ItemStack(material));
        if (changed) {
            player.sendMessage(AdminPanelUtils.format(player, "%prefix% &9> &aDisabled item: &e" + material, de.happybavarian07.adminpanel.main.AdminPanelMain.getPrefix()));
        } else {
            player.sendMessage(AdminPanelUtils.format(player, "%prefix% &9> &eItem is already disabled: &6" + material, de.happybavarian07.adminpanel.main.AdminPanelMain.getPrefix()));
        }
        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        if (args.length != 1) return false;

        Material material = Material.matchMaterial(args[0]);
        if (material == null) {
            sender.sendMessage("[Admin-Panel] Invalid material: " + args[0]);
            return true;
        }

        boolean changed = AdminPanelUtils.disableVanillaItem(new ItemStack(material));
        if (changed) {
            sender.sendMessage("[Admin-Panel] Disabled item: " + material);
        } else {
            sender.sendMessage("[Admin-Panel] Item is already disabled: " + material);
        }
        return true;
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
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
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
    public String permissionAsString() {
        return "AdminPanel.DisabledItems.Disable";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }
}
