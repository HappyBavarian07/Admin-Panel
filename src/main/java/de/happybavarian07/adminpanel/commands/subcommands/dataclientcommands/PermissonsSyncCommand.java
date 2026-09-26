package de.happybavarian07.adminpanel.commands.subcommands.dataclientcommands;/*
 * @Author HappyBavarian07
 * @Date 23.11.2022 | 16:56
 */

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.permissions.PermissionsManager;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

@SubCommandInfo(name = "SyncPermissions", aliases = "SyncPerms",
        info = "Sync Permissions across Servers. Without a Player, all Players are sent.",
        permission = "AdminPanel.DataClient.SubCommands.SyncPermissions", autoRegisterPermission = false)
public class PermissonsSyncCommand extends DataClientSubCommand {
    private static final String KEY = "DataClient.PermissionsSync.";

    public PermissonsSyncCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(Argument.player("player").optional());
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean execute(CommandSender sender, CommandArgs args) {
        PermissionsManager permissions = AdminPanelMain.getPlugin().getPermissionsManager();
        if (!args.has(0)) {
            for (UUID holder : permissions.getAllPlayerPermissions().keySet()) {
                utils().sendPlayerPermissions(holder, permissions.getPlayerPermissions(holder), "null");
            }
            CommandText.send(lgm, sender, KEY + "PermissionsSendForAllPlayers");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args.string(0));
        if (!permissions.hasPermissions(target.getUniqueId())) {
            CommandText.send(lgm, sender, KEY + "NoPermissionsStored", "%target_player%", args.string(0));
            return true;
        }
        utils().sendPlayerPermissions(target.getUniqueId(), permissions.getPlayerPermissions(target.getUniqueId()), "null");
        CommandText.send(lgm, sender, KEY + "PermissionsSendForOnePlayer", "%target_player%", String.valueOf(target.getName()));
        return true;
    }
}
