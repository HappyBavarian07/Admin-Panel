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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListCommand extends SubCommand {
    public ListCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        int page = 1;
        if (args.length > 1) return false;
        if (args.length == 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                player.sendMessage(AdminPanelUtils.format(player, "%prefix% &9> &cInvalid page: &e" + args[0], de.happybavarian07.adminpanel.main.AdminPanelMain.getPrefix()));
                return true;
            }
        }
        sendDisabledItemsList(player, page);
        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        int page = 1;
        if (args.length > 1) return false;
        if (args.length == 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                sender.sendMessage("[Admin-Panel] Invalid page: " + args[0]);
                return true;
            }
        }
        sendDisabledItemsList(sender, page);
        return true;
    }

    @Override
    public String name() {
        return "list";
    }

    @Override
    public String info() {
        return "The List Command";
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
        return "/ditems list <Page>";
    }

    @Override
    public String permissionAsString() {
        return "AdminPanel.DisabledItems.List";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }

    private void sendDisabledItemsList(CommandSender sender, int page) {
        List<String> items = new ArrayList<>();
        for (Material material : AdminPanelUtils.getDisabledItems()) {
            items.add(material.name());
        }
        items.sort(Comparator.naturalOrder());

        int pageSize = 10;
        int maxPage = Math.max(1, (int) Math.ceil(items.size() / (double) pageSize));
        if (page < 1 || page > maxPage) {
            sender.sendMessage("[Admin-Panel] Page " + page + " does not exist. Valid range: 1-" + maxPage);
            return;
        }

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, items.size());
        sender.sendMessage("[Admin-Panel] Disabled items (" + page + "/" + maxPage + "), total: " + items.size());
        if (items.isEmpty()) {
            sender.sendMessage("[Admin-Panel] No disabled items configured.");
            return;
        }
        for (int i = start; i < end; i++) {
            sender.sendMessage(" - " + items.get(i));
        }
    }
}
