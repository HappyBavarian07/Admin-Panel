package de.happybavarian07.adminpanel.commands.subcommands.warnings;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.warnings.WarningManager;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

@SubCommandInfo(name = "warn", permission = "AdminPanel.PlayerManager.PlayerSettings.Warn", autoRegisterPermission = false)
public class WarnCommand extends WarningSubCommand {
    public WarnCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(playerArgument(), Argument.text("reason"));
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        WarningManager manager = manager(sender);
        if (manager == null) return true;
        OfflinePlayer target = target(sender, args.string(0));
        if (target == null) return true;
        String reason = args.joined(1);
        UUID id = target.getUniqueId();
        manager.add(id, reason, issuer(sender))
                .thenAccept(warning -> CommandText.send(lgm, sender, KEY + "Added", "%target%", target.getName(),
                        "%number%", manager.number(warning), "%count%", manager.activeCount(id), "%reason%", reason))
                .exceptionally(failed(sender));
        return true;
    }
}
