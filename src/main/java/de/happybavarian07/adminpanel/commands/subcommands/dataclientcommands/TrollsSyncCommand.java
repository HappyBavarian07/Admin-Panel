package de.happybavarian07.adminpanel.commands.subcommands.dataclientcommands;/*
 * @Author HappyBavarian07
 * @Date 26.11.2022 | 17:42
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.main.PlaceholderType;
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
    public Map<Integer, String[]> subArgs() {
        Map<Integer, String[]> args = new HashMap<>();
        args.put(1, new String[]{"HurtingWaterMap", "ChatMuteMap", "VillagerSoundsMap", "BlockBreakPreventMap", "DupeMobsOnKillMap", "FreezePlayerMap"});
        return args;
    }

    @Override
    public String syntax() {
        return "/dataclient SyncTrolls <Troll>";
    }

    @Override
    public String permission() {
        return "AdminPanel.DataClient.SubCommands.SyncTrolls";
    }
}
