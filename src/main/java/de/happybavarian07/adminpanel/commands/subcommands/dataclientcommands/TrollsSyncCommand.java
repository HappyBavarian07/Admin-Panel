package de.happybavarian07.adminpanel.commands.subcommands.dataclientcommands;/*
 * @Author HappyBavarian07
 * @Date 26.11.2022 | 17:42
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.language.PlaceholderType;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@CommandData(allowOnlySubCommandArgsThatFitToSubArgs = true)
public class TrollsSyncCommand extends SubCommand {
    public TrollsSyncCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        if (args.length != 1) return false;

        // TODO Maybe Send Player Specific troll Data
        boolean result = plugin.getDataClientUtils().sendCustomMap("null", args[0]).hasSucceeded();
        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%troll%", args[0], false);
        player.sendMessage(lgm.getMessage("DataClient.TrollsSync.TrollDataSendForAllPlayers", player, true));
        return true;
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        if (args.length == 0) return false;

        // TODO Maybe Send Player Specific troll Data
        boolean result = plugin.getDataClientUtils().sendCustomMap("null", args[0]).hasSucceeded();
        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%troll%", args[0], false);
        sender.sendMessage(lgm.getMessage("DataClient.TrollsSync.TrollDataSendForAllPlayers", null, true));
        return true;
    }

    @Override
    public String name() {
        return "SyncTrolls";
    }

    @Override
    public String info() {
        return "Sync specific Trolls with other Servers";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        Map<Integer, String[]> subArgs = new HashMap<>();
        subArgs.put(1, new String[]{"HurtingWaterMap", "ChatMuteMap", "VillagerSoundsMap", "BlockBreakPreventMap", "DupeMobsOnKillMap", "FreezePlayerMap"});
        return subArgs;
    }

    @Override
    public String syntax() {
        return "/dataclient SyncTrolls <Troll>";
    }

    @Override
    public String permissionAsString() {
        return "AdminPanel.DataClient.SubCommands.SyncTrolls";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }
}
