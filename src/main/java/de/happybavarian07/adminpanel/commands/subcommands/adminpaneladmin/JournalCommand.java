package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.data.AdminAction;
import de.happybavarian07.adminpanel.journal.AdminJournal;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.JournalMenu;
import de.happybavarian07.adminpanel.warnings.WarningText;
import de.happybavarian07.coolstufflib.CoolStuffLib;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * /adminpaneladmin journal [page]: a player gets the journal menu, the console gets a page of text lines.
 */
public class JournalCommand extends SubCommand {
    private static final int LINES = 10;

    public JournalCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public String name() {
        return "journal";
    }

    @Override
    public String info() {
        return "Shows what admins did: bans, kicks, warnings, op and permission changes";
    }

    @Override
    public String permissionAsString() {
        return JournalMenu.PERMISSION;
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }

    @Override
    public List<Argument> arguments() {
        return List.of(Argument.integer("page").optional());
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        AdminJournal journal = AdminPanelMain.getPlugin().getJournal();
        if (journal == null) {
            CommandText.send(lgm, sender, "Journal.NotEnabled");
            return true;
        }
        if (sender instanceof Player player && !args.has(0)) {
            new JournalMenu(CoolStuffLib.getLib().getPlayerMenuUtility(player.getUniqueId())).open();
            return true;
        }
        int page = args.has(0) ? Math.max(1, args.integer(0)) : 1;
        long total = journal.count();
        if (total == 0) {
            CommandText.send(lgm, sender, "Journal.Empty");
            return true;
        }
        int pages = (int) ((total + LINES - 1) / LINES);
        CommandText.send(lgm, sender, "Journal.Header", "%page%", Math.min(page, pages), "%pages%", pages, "%total%", total);
        Player viewer = sender instanceof Player p ? p : null;
        for (AdminAction action : journal.page(Math.min(page, pages) - 1, LINES)) {
            CommandText.send(lgm, sender, "Journal.Line", "%id%", action.getId(), "%time%", WarningText.date(lgm, viewer, action.getCreatedAt()),
                    "%actor%", action.getActor(), "%action%", action.getAction(), "%target%", action.getTarget(), "%details%", action.getDetails());
        }
        return true;
    }
}
