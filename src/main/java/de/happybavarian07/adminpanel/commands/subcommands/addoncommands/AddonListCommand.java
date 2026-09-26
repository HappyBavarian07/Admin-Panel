package de.happybavarian07.adminpanel.commands.subcommands.addoncommands;

import de.happybavarian07.adminpanel.addonloader.api.Addon;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.PaginatedList;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/*
 * @Author HappyBavarian07
 * @Date September 07, 2024 | 23:59
 */
@SubCommandInfo(name = "listaddons", aliases = "list", info = "Lists all Addons that are installed on the Server.",
        permission = "AdminPanel.AddonManageCMD.List")
public class AddonListCommand extends AddonSubCommand {
    private static final int MAX_ITEMS_PER_PAGE = 10;

    public AddonListCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(Argument.integer("page").optional());
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        int page = args.integer(0, 1);
        PaginatedList<Addon> addons = new PaginatedList<>(loader().getLoadedAddons());
        addons.maxItemsPerPage(MAX_ITEMS_PER_PAGE).sort("bubble", false);
        try {
            return sendPage(sender, addons, page);
        } catch (PaginatedList.ListNotSortedException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean sendPage(CommandSender sender, PaginatedList<Addon> addons, int page) throws PaginatedList.ListNotSortedException {
        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%page%", page, false);
        if (!addons.containsPage(page)) {
            sender.sendMessage(message(sender, "AddonCommand.ListMessage.PageDoesNotExist"));
            return true;
        }
        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%max_page%", addons.getMaxPage(), false);
        sender.sendMessage(message(sender, "AddonCommand.ListMessage.Header"));
        for (Addon addon : addons.getPage(page)) send(sender, "AddonCommand.ListMessage.Format", addon);
        sender.sendMessage(lgm.getMessage("AddonCommand.ListMessage.Footer", sender instanceof Player p ? p : null, true));
        return true;
    }
}
