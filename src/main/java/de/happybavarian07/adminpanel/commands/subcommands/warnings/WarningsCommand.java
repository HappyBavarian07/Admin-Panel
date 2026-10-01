package de.happybavarian07.adminpanel.commands.subcommands.warnings;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.data.PlayerWarning;
import de.happybavarian07.adminpanel.warnings.WarningManager;
import de.happybavarian07.adminpanel.warnings.WarningText;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

@SubCommandInfo(name = "warnings", permission = "AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Warnings", autoRegisterPermission = false)
public class WarningsCommand extends WarningSubCommand {
    private static final int PAGE_SIZE = 8;

    public WarningsCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(playerArgument(), Argument.integer("page").optional());
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        WarningManager manager = manager(sender);
        if (manager == null) return true;
        OfflinePlayer target = target(sender, args.string(0));
        if (target == null) return true;
        int page = args.integer(1, 1);
        manager.load(target.getUniqueId())
                .thenAccept(warnings -> list(sender, manager, target.getName(), warnings, page))
                .exceptionally(failed(sender));
        return true;
    }

    private void list(CommandSender sender, WarningManager manager, String player, List<PlayerWarning> warnings, int page) {
        if (warnings.isEmpty()) {
            CommandText.send(lgm, sender, KEY + "List.Empty", "%target%", player);
            return;
        }
        int maxPage = (warnings.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        if (page < 1 || page > maxPage) {
            CommandText.send(lgm, sender, KEY + "List.PageDoesNotExist", "%page%", page, "%max_page%", maxPage);
            return;
        }
        Object[] header = {"%target%", player, "%page%", page, "%max_page%", maxPage, "%count%", warnings.size()};
        CommandText.send(lgm, sender, KEY + "List.Header", header);
        for (PlayerWarning warning : warnings.subList((page - 1) * PAGE_SIZE, Math.min(page * PAGE_SIZE, warnings.size()))) {
            CommandText.send(lgm, sender, KEY + "List.Format",
                    "%number%", manager.number(warning),
                    "%reason%", warning.getReason(),
                    "%issuer%", WarningText.actor(lgm, sender, warning.getIssuer()),
                    "%created%", WarningText.date(lgm, sender, warning.getCreatedAt()),
                    "%expires%", WarningText.expires(lgm, sender, warning),
                    "%status%", WarningText.status(lgm, sender, warning));
        }
        CommandText.send(lgm, sender, KEY + "List.Footer", header);
    }
}
