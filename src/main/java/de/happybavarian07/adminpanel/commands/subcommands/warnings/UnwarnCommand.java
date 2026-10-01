package de.happybavarian07.adminpanel.commands.subcommands.warnings;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.data.PlayerWarning;
import de.happybavarian07.adminpanel.warnings.WarningManager;
import de.happybavarian07.adminpanel.warnings.WarningStatus;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@SubCommandInfo(name = "unwarn", permission = "AdminPanel.PlayerManager.PlayerSettings.Warn", autoRegisterPermission = false)
public class UnwarnCommand extends WarningSubCommand {
    private static final String ALL = "all";

    public UnwarnCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(playerArgument(), Argument.custom("number|all", sender -> List.of(ALL)));
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        WarningManager manager = manager(sender);
        if (manager == null) return true;
        OfflinePlayer target = target(sender, args.string(0));
        if (target == null) return true;
        String selection = args.string(1);
        UUID id = target.getUniqueId();
        String name = target.getName();
        if (selection.equalsIgnoreCase(ALL)) {
            manager.load(id)
                    .thenCompose(loaded -> manager.expireAll(id, issuer(sender)))
                    .thenAccept(ended -> CommandText.send(lgm, sender, KEY + "ExpiredAll", "%target%", name, "%count%", ended))
                    .exceptionally(failed(sender));
            return true;
        }
        int number;
        try {
            number = Integer.parseInt(selection);
        } catch (NumberFormatException notANumber) {
            CommandText.send(lgm, sender, KEY + "InvalidNumber", "%value%", selection);
            return true;
        }
        manager.load(id)
                .thenCompose(loaded -> expire(sender, manager, id, name, number))
                .exceptionally(failed(sender));
        return true;
    }

    private CompletionStage<Void> expire(CommandSender sender, WarningManager manager, UUID id, String name, int number) {
        PlayerWarning warning = manager.byNumber(id, number);
        if (warning == null) {
            CommandText.send(lgm, sender, KEY + "NoSuchWarning", "%target%", name, "%number%", number);
            return CompletableFuture.completedFuture(null);
        }
        if (warning.getStatus() != WarningStatus.ACTIVE) {
            CommandText.send(lgm, sender, KEY + "NotActive", "%target%", name, "%number%", number);
            return CompletableFuture.completedFuture(null);
        }
        return manager.expireNow(warning.getId(), issuer(sender))
                .thenAccept(done -> CommandText.send(lgm, sender, KEY + (done ? "Expired" : "NotActive"),
                        "%target%", name, "%number%", number));
    }
}
