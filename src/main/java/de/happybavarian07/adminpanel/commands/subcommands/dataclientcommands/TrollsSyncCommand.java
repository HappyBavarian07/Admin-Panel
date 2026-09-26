package de.happybavarian07.adminpanel.commands.subcommands.dataclientcommands;/*
 * @Author HappyBavarian07
 * @Date 23.11.2022 | 16:56
 */

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import org.bukkit.command.CommandSender;

import java.util.List;

@SubCommandInfo(name = "SyncTrolls", info = "Sync specific Trolls with other Servers",
        permission = "AdminPanel.DataClient.SubCommands.SyncTrolls", autoRegisterPermission = false)
public class TrollsSyncCommand extends DataClientSubCommand {
    private static final List<String> TROLLS = List.of("HurtingWaterMap", "ChatMuteMap", "VillagerSoundsMap",
            "BlockBreakPreventMap", "DupeMobsOnKillMap", "FreezePlayerMap");

    public TrollsSyncCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(Argument.choice("troll", TROLLS.toArray(new String[0])));
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        String troll = TROLLS.stream().filter(t -> t.equalsIgnoreCase(args.string(0))).findFirst().orElse(null);
        if (troll == null) return false;
        boolean sent = utils().sendCustomMap("null", troll).hasSucceeded();
        CommandText.send(lgm, sender, sent ? "DataClient.TrollsSync.TrollDataSendForAllPlayers" : "DataClient.TrollsSync.TrollDataSendFailed", "%troll%", troll);
        return true;
    }
}
