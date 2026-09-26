package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;/*
 * @Author HappyBavarian07
 * @Date 29.01.2023 | 12:14
 */

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.backupmanager.BackupManager;
import de.happybavarian07.coolstufflib.backupmanager.FileBackup;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.PaginatedList;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@SubCommandInfo(name = "backup", info = "The Command to control the Config Backup System",
        syntax = "/apadmin backup <start|load|delete|listBackups|listFiles|listBackupsDone|getNewestBackupFile> [BackupName] [BackupFileNumber(0-Infinity)|Newest(-1)|Page]",
        permission = "AdminPanel.AdminPanelAdminCommands.BackupManager", autoRegisterPermission = false)
public class BackupManagerCommand extends SubCommand {
    private static final int PAGE_SIZE = 4;
    private static final String KEY = "BackupSystem.SubCommands.";

    public BackupManagerCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(
                Argument.choice("action", "start", "load", "delete", "listBackups", "listFiles", "listBackupsDone", "getNewestBackupFile"),
                Argument.custom("backup", sender -> manager().getFileBackupList().keySet()).optional(),
                Argument.custom("number|page", sender -> List.of("-1", "0", "1")).optional());
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        switch (args.string(0).toLowerCase(Locale.ROOT)) {
            case "start" -> {
                FileBackup backup = backup(args, 1);
                if (backup == null) return true;
                int response = manager().startBackup(backup.getIdentifier());
                CommandText.send(lgm, sender, KEY + "Start", "%backupName%", backup.getIdentifier(), "%success%", result(response));
            }
            case "load" -> {
                FileBackup backup = backup(args, 1);
                if (backup == null) return true;
                int response = manager().loadBackup(backup.getIdentifier(), args.integer(2));
                CommandText.send(lgm, sender, KEY + "Load", "%backupName%", backup.getIdentifier(), "%success%", result(response));
            }
            case "delete" -> {
                FileBackup backup = backup(args, 1);
                if (backup == null) return true;
                String file = args.string(2);
                int response = manager().deleteBackupFile(backup.getIdentifier(), file);
                CommandText.send(lgm, sender, KEY + "DeleteBackup", "%backupName%", backup.getIdentifier(),
                        "%backupFileNumberOrName%", file, "%success%", result(response));
            }
            case "getnewestbackupfile" -> {
                FileBackup backup = backup(args, 1);
                if (backup == null) return true;
                File newest = backup.getNewestBackupFile();
                if (newest == null) {
                    CommandText.send(lgm, sender, KEY + "NoBackupFile", "%backupName%", backup.getIdentifier());
                    return true;
                }
                sender.sendMessage(withFile(CommandText.get(lgm, sender, KEY + "GetNewestBackupFile", "%backupName%", backup.getIdentifier()), newest));
            }
            case "listbackups" -> sendPage(sender, "ListBackups", new ArrayList<>(manager().getFileBackupList().values()),
                    args.integer(1, 1), (backup, line) -> withFileBackup(line, backup));
            case "listfiles" -> {
                FileBackup backup = backup(args, 1);
                if (backup == null) return true;
                sendPage(sender, "ListFilesToBackup", Arrays.asList(backup.getFilesToBackup()), args.integer(2, 1), (file, line) -> withFile(line, file));
            }
            case "listbackupsdone" -> {
                FileBackup backup = backup(args, 1);
                if (backup == null) return true;
                List<File> done = new ArrayList<>(backup.getBackupsDone());
                sendPage(sender, "ListBackupsDone", done, args.integer(2, 1),
                        (file, line) -> withFile(line.replace("%backupsDoneNumber%", String.valueOf(done.indexOf(file))), file));
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private static BackupManager manager() {
        return AdminPanelMain.getPlugin().getBackupManager();
    }

    private FileBackup backup(CommandArgs args, int index) {
        String name = args.string(index);
        FileBackup backup = manager().getFileBackup(name);
        if (backup == null) CommandText.send(lgm, args.sender(), KEY + "BackupNotFound", "%backupName%", name);
        return backup;
    }

    private static String result(int response) {
        return response == 0 ? "&aSuccess" : "&cError (Code: " + response + ")";
    }

    private <T extends Comparable<T>> void sendPage(CommandSender sender, String section, List<T> items, int page, LineFormatter<T> formatter) {
        PaginatedList<T> list = new PaginatedList<>(items);
        list.maxItemsPerPage(PAGE_SIZE).sort("bubble", false);
        try {
            if (!list.containsPage(page)) {
                CommandText.send(lgm, sender, KEY + section + ".PageDoesNotExist");
                return;
            }
            Object[] pages = {"%page%", page, "%max_page%", list.getMaxPage()};
            CommandText.send(lgm, sender, KEY + section + ".Header", pages);
            String format = CommandText.get(lgm, sender, KEY + section + ".Format");
            for (T item : list.getPage(page)) sender.sendMessage(formatter.format(item, format));
            CommandText.send(lgm, sender, KEY + section + ".Footer", pages);
        } catch (PaginatedList.ListNotSortedException e) {
            throw new IllegalStateException(e);
        }
    }

    @FunctionalInterface
    private interface LineFormatter<T> {
        String format(T item, String line);
    }

    private static String withFile(String line, File file) {
        return line.replace("%fileName%", file.getName())
                .replace("%absolutePath%", file.getAbsolutePath())
                .replace("%normalPath%", file.getPath())
                .replace("%parentFile%", String.valueOf(file.getParent()))
                .replace("%exists%", file.exists() ? "§aFile Exists" : "§cFile Doesn't Exist");
    }

    private static String withFileBackup(String line, FileBackup backup) {
        return line.replace("%filesToBackup%", Arrays.toString(backup.getFilesToBackup()))
                .replace("%filesToBackupSize%", String.valueOf(backup.getFilesToBackup().length))
                .replace("%backupsDone%", String.valueOf(backup.getBackupsDone()))
                .replace("%backupsDoneSize%", String.valueOf(backup.getBackupsDone().size()))
                .replace("%identifier%", backup.getIdentifier())
                .replace("%backupDest%", backup.getDestinationPathToBackupToo().getPath())
                .replace("%newestBackupFile%", String.valueOf(backup.getNewestBackupFile()));
    }
}
