package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.journal.AdminJournal;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * /adminpaneladmin journalexport: writes the whole journal as CSV to {@code journal-<date>.csv} in the plugin folder.
 */
public class JournalExportCommand extends SubCommand {
    public static final String PERMISSION = "AdminPanel.Journal.Export";

    public JournalExportCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public String name() {
        return "journalexport";
    }

    @Override
    public String info() {
        return "Exports the admin journal to a CSV file in the plugin folder";
    }

    @Override
    public String permissionAsString() {
        return PERMISSION;
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        AdminPanelMain plugin = AdminPanelMain.getPlugin();
        AdminJournal journal = plugin.getJournal();
        if (journal == null) {
            CommandText.send(lgm, sender, "Journal.NotEnabled");
            return true;
        }
        File file = new File(plugin.getDataFolder(), "journal-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".csv");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int count = journal.export(file.toPath());
                Bukkit.getScheduler().runTask(plugin, () ->
                        CommandText.send(lgm, sender, "Journal.Exported", "%count%", count, "%file%", file.getName()));
            } catch (IOException | RuntimeException e) {
                AdminPanelMain.logError("Could not export the admin journal", e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        CommandText.send(lgm, sender, "Journal.ExportFailed", "%error%", String.valueOf(e.getMessage())));
            }
        });
        return true;
    }
}
