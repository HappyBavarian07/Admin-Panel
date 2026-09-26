package de.happybavarian07.adminpanel.commands.subcommands.disableditems;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.List;

@SubCommandInfo(name = "list", info = "Lists the disabled items.",
        permission = "AdminPanel.DisabledItems.List", autoRegisterPermission = false)
public class ListCommand extends DisabledItemsSubCommand {
    private static final int PAGE_SIZE = 10;

    public ListCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(Argument.integer("page").optional());
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        List<String> items = AdminPanelUtils.getDisabledItems().stream().map(Material::name).sorted().toList();
        if (items.isEmpty()) {
            CommandText.send(lgm, sender, KEY + "List.Empty");
            return true;
        }
        int maxPage = (items.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        int page = args.integer(0, 1);
        if (page < 1 || page > maxPage) {
            CommandText.send(lgm, sender, KEY + "List.PageDoesNotExist", "%page%", page, "%max_page%", maxPage);
            return true;
        }
        Object[] placeholders = {"%page%", page, "%max_page%", maxPage, "%count%", items.size()};
        CommandText.send(lgm, sender, KEY + "List.Header", placeholders);
        for (String item : items.subList((page - 1) * PAGE_SIZE, Math.min(page * PAGE_SIZE, items.size()))) {
            CommandText.send(lgm, sender, KEY + "List.Format", "%material%", item);
        }
        CommandText.send(lgm, sender, KEY + "List.Footer", placeholders);
        return true;
    }
}
