package de.happybavarian07.adminpanel.commands.subcommands.addoncommands;

import de.happybavarian07.adminpanel.addonloader.api.Addon;
import de.happybavarian07.adminpanel.commandmanagement.PaginatedList;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.language.PlaceholderType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/*
 * @Author HappyBavarian07
 * @Date September 07, 2024 | 23:59
 */
public class AddonListCommand extends SubCommand {
    private final int MAX_ITEM_PER_PAGE = 10;

    public AddonListCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Player playerOrNull, String[] args) {
        int page = 1;
        if (args.length == 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(lgm.getMessage("Player.Commands.NotANumber", playerOrNull, true));
                return true;
            }
        }
        try {
            PaginatedList<Addon> messages = new PaginatedList<>(plugin.getAddonLoader().getLoadedAddons());
            messages.maxItemsPerPage(MAX_ITEM_PER_PAGE).sort("bubble", false);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%page%", page, false);
            if (!messages.containsPage(page)) {
                sender.sendMessage(lgm.getMessage("AddonCommand.ListMessage.HelpPageDoesNotExist", playerOrNull, true));
                return true;
            }
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%max_page%", messages.getMaxPage(), false);
            sender.sendMessage(lgm.getMessage("AddonCommand.ListMessage.Header", playerOrNull, false));
            for (Addon a : messages.getPage(page)) {
                sender.sendMessage(format(lgm.getMessage("AddonCommand.ListMessage.Format", playerOrNull, false), a));
            }
            sender.sendMessage(lgm.getMessage("AddonCommand.ListMessage.Footer", playerOrNull, true));
        } catch (PaginatedList.ListNotSortedException e2) {
            e2.printStackTrace();
            return true;
        }
        return true;
    }

    @Override
    public String name() {
        return "listaddons";
    }

    @Override
    public String info() {
        return "Lists all Addons that are installed on the Server.";
    }

    @Override
    public String[] aliases() {
        return new String[]{"list"};
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        int count = plugin.getAddonLoader().getLoadedAddons().size() / MAX_ITEM_PER_PAGE;
        if (plugin.getAddonLoader().getLoadedAddons().size() % MAX_ITEM_PER_PAGE != 0) {
            count++;
        }
        return Map.of(1, new String[]{String.valueOf(count)});
    }

    @Override
    public String syntax() {
        return "/" + mainCommandName + " " + name() + " [Page]";
    }

    @Override
    public String permissionAsString() {
        return "AdminPanel.AddonManageCMD.List";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }

    public String format(String format, Addon addon) {
        return format
                .replace("%addon_name%", addon.getName())
                .replace("%addon_version%", addon.getVersion())
                .replace("%addon_description%", addon.getDescription())
                .replace("%addon_dependencies%", addon.getDependencies().toString());
    }
}
