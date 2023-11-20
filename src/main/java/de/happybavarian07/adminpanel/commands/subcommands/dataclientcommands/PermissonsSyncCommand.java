package de.happybavarian07.adminpanel.commands.subcommands.dataclientcommands;/*
 * @Author HappyBavarian07
 * @Date 26.11.2022 | 17:42
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.language.PlaceholderType;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CommandData
public class PermissonsSyncCommand extends SubCommand {
    public PermissonsSyncCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        if (args.length == 0) {
            for (UUID permissionHolders : plugin.getPlayerPermissions().keySet()) {
                plugin.getDataClientUtils().sendPlayerPermissions(
                        permissionHolders,
                        plugin.getPlayerPermissions().get(permissionHolders),
                        "null"
                );
            }
            player.sendMessage(lgm.getMessage("DataClient.PermissionsSync.PermissionsSendForAllPlayers", player, false));
            return true;
        } else if (args.length == 1) {
            UUID permissionHolder = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
            System.out.println("UUID: " + permissionHolder);
            if (!plugin.getPlayerPermissions().containsKey(permissionHolder)) return false;
            plugin.getDataClientUtils().sendPlayerPermissions(
                    permissionHolder,
                    plugin.getPlayerPermissions().get(permissionHolder),
                    "null"
            );
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target_player%", Bukkit.getOfflinePlayer(permissionHolder).getName(), false);
            player.sendMessage(lgm.getMessage("DataClient.PermissionsSync.PermissionsSendForOnePlayer", player, true));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        if (args.length == 0) {
            for (UUID permissionHolders : plugin.getPlayerPermissions().keySet()) {
                plugin.getDataClientUtils().sendPlayerPermissions(
                        permissionHolders,
                        plugin.getPlayerPermissions().get(permissionHolders),
                        "null"
                );
            }
            sender.sendMessage(lgm.getMessage("DataClient.PermissionsSync.PermissionsSendForAllPlayers", null, false));
            return true;
        }
        if (args.length == 1) {
            UUID permissionHolder = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
            System.out.println("UUID: " + permissionHolder);
            if (!plugin.getPlayerPermissions().containsKey(permissionHolder)) return false;
            plugin.getDataClientUtils().sendPlayerPermissions(
                    permissionHolder,
                    plugin.getPlayerPermissions().get(permissionHolder),
                    "null"
            );
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target_player%", Bukkit.getOfflinePlayer(permissionHolder).getName(), false);
            sender.sendMessage(lgm.getMessage("DataClient.PermissionsSync.PermissionsSendForOnePlayer", null, true));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String name() {
        return "SyncPermissions";
    }

    @Override
    public String info() {
        return "Sync Permissions accross Servers. If [Player] = null, then All Players get selected.";
    }

    @Override
    public String[] aliases() {
        return new String[]{"SyncPerms"};
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
        return "/dataclient SyncPerms [Player]";
    }

    @Override
    public String permissionAsString() {
        return "AdminPanel.DataClient.SubCommands.SyncPermissions";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }
}
