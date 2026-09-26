package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.CommandData;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@SubCommandInfo(name = "report", info = "The Command to report Bugs to the Developer.",
        permission = "AdminPanel.AdminPanelAdminCommands.ReportBug", autoRegisterPermission = false)
@CommandData(playerRequired = true, minArgs = 1)
public class BugReportCommand extends SubCommand {
    public BugReportCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(Argument.text("message"));
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        Player player = args.player();
        int response = AdminPanelMain.getAPI().reportBugToDiscord(player.getUniqueId(), args.joined(0));
        if (response == -2) {
            long remaining = AdminPanelMain.getAPI().getCooldownTimeMap().get(player.getUniqueId()) - System.currentTimeMillis();
            CommandText.send(lgm, sender, "AdminPanelAdmin.BugReport.Cooldown", "%seconds%", (remaining + 999) / 1000);
        } else if (response == -1) {
            CommandText.send(lgm, sender, "AdminPanelAdmin.BugReport.Error");
        } else {
            CommandText.send(lgm, sender, "AdminPanelAdmin.BugReport.Sent");
        }
        return true;
    }
}
