package de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin;/*
 * @Author HappyBavarian07
 * @Date 29.01.2023 | 12:14
 */

import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.PaginatedList;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import de.happybavarian07.adminpanel.language.Placeholder;
import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.backupmanager.BackupManager;
import de.happybavarian07.adminpanel.backupmanager.FileBackup;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

@CommandData
public class BackupManagerCommand extends SubCommand {
    private final BackupManager backupManager;

    public BackupManagerCommand(String mainCommandName) {
        super(mainCommandName);
        this.backupManager = plugin.getBackupManager();
    }

    @Override
    public boolean handleCommand(CommandSender sender, Player playerOrNull, String[] args) {
        if (args.length == 2) {
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%backupName%", args[1], false);
            // Handle Commands with one Arg
            if (args[0].equalsIgnoreCase("start")) {
                // Start Backup
                int response = backupManager.startBackup(args[1]);
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%success%", response == 0 ? "&aSuccess" : "&cError (Code: " + response + ")", false);
                sender.sendMessage(lgm.getMessage("BackupSystem.SubCommands.Start", playerOrNull, true));
            } else if (args[0].equalsIgnoreCase("getNewestBackupFile")) {
                // Get Newest Backup File
                File newestBackupFile = backupManager.getFileBackup(args[1]).getNewestBackupFile();
                sender.sendMessage(formatStringWithFile(lgm.getMessage("BackupSystem.SubCommands.GetNewestBackupFile", playerOrNull, false), newestBackupFile));
            } else if (args[0].equalsIgnoreCase("listBackups")) {
                // List Backup Classes
                try {
                    int page = Integer.parseInt(args[1]);
                    List<FileBackup> listOfFiles = new ArrayList<>(backupManager.getFileBackupList().values());
                    PaginatedList<FileBackup> messages = new PaginatedList<>(listOfFiles);
                    messages.maxItemsPerPage(4).sort("bubble", false);
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%page%", page, false);
                    if (!messages.containsPage(page)) {
                        sender.sendMessage(lgm.getMessage("BackupSystem.SubCommands.ListBackups.PageDoesNotExist", playerOrNull, true));
                        return true;
                    }
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%max_page%", messages.getMaxPage(), false);
                    sender.sendMessage(lgm.getMessage("BackupSystem.SubCommands.ListBackups.Header", playerOrNull, false));
                    for (FileBackup f : messages.getPage(page)) {
                        sender.sendMessage(formatStringWithFileBackup(lgm.getMessage("BackupSystem.SubCommands.ListBackups.Format", playerOrNull, false), f));
                    }
                    sender.sendMessage(lgm.getMessage("BackupSystem.SubCommands.ListBackups.Footer", playerOrNull, true));
                } catch (NumberFormatException e) {
                    sender.sendMessage(lgm.getMessage("Player.Commands.NotANumber", playerOrNull, true));
                    return true;
                } catch (PaginatedList.ListNotSortedException e2) {
                    e2.printStackTrace();
                    return true;
                }
            } else return false;
            return true;
        } else if (args.length == 3) {
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%backupName%", args[1], false);
            // Handle Commands with one Arg
            if (args[0].equalsIgnoreCase("listFiles")) {
                // List Files that get a backup
                try {
                    int page = Integer.parseInt(args[2]);
                    List<File> listOfFiles = new ArrayList<>(Arrays.asList(backupManager.getFileBackup(args[1]).getFilesToBackup()));
                    PaginatedList<File> messages = new PaginatedList<>(listOfFiles);
                    messages.maxItemsPerPage(4).sort("bubble", false);
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%page%", page, false);
                    if (!messages.containsPage(page)) {
                        sender.sendMessage(lgm.getMessage("BackupSystem.SubCommands.ListFilesToBackup.PageDoesNotExist", playerOrNull, true));
                        return true;
                    }
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%max_page%", messages.getMaxPage(), false);
                    sender.sendMessage(lgm.getMessage("BackupSystem.SubCommands.ListFilesToBackup.Header", playerOrNull, false));
                    for (File f : messages.getPage(page)) {
                        sender.sendMessage(formatStringWithFile(lgm.getMessage("BackupSystem.SubCommands.ListFilesToBackup.Format", playerOrNull, false), f));
                    }
                    sender.sendMessage(lgm.getMessage("BackupSystem.SubCommands.ListFilesToBackup.Footer", playerOrNull, true));
                } catch (NumberFormatException e) {
                    sender.sendMessage(lgm.getMessage("Player.Commands.NotANumber", playerOrNull, true));
                    return true;
                } catch (PaginatedList.ListNotSortedException e2) {
                    e2.printStackTrace();
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("listBackupsDone")) {
                // List Backups Done
                try {
                    int page = Integer.parseInt(args[2]);
                    List<File> listOfBackupsDone = new ArrayList<>(backupManager.getFileBackup(args[1]).getBackupsDone());
                    PaginatedList<File> messages = new PaginatedList<>(listOfBackupsDone);
                    messages.maxItemsPerPage(4).sort("bubble", false);
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%page%", page, false);
                    if (!messages.containsPage(page)) {
                        sender.sendMessage(lgm.getMessage("BackupSystem.SubCommands.ListBackupsDone.PageDoesNotExist", playerOrNull, true));
                        return true;
                    }
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%max_page%", messages.getMaxPage(), false);
                    sender.sendMessage(lgm.getMessage("BackupSystem.SubCommands.ListBackupsDone.Header", playerOrNull, false));
                    for (File f : messages.getPage(page)) {
                        sender.sendMessage(formatStringWithFile(
                                lgm.getMessage("BackupSystem.SubCommands.ListBackupsDone.Format", playerOrNull, false)
                                        .replace("%backupsDoneNumber%", "" + listOfBackupsDone.indexOf(f)), f));
                    }
                    sender.sendMessage(lgm.getMessage("BackupSystem.SubCommands.ListBackupsDone.Footer", playerOrNull, true));
                } catch (NumberFormatException e) {
                    sender.sendMessage(lgm.getMessage("Player.Commands.NotANumber", playerOrNull, true));
                    return true;
                } catch (PaginatedList.ListNotSortedException e2) {
                    e2.printStackTrace();
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("load")) {
                // Load Backup (First Arg = String, Second Arg = Int (-1 equals newest backup))
                int response = backupManager.loadBackup(args[1], Integer.parseInt(args[2]));
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%success%", response == 0 ? "&aSuccess" : "&cError (Code: " + response + ")", false);
                sender.sendMessage(lgm.getMessage("BackupSystem.SubCommands.Load", playerOrNull, true));
            } else if (args[0].equalsIgnoreCase("delete")) {
                // Delete Backup File
                int response = backupManager.deleteBackupFile(args[1], args[2]);
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%backupFileNumberOrName%", args[2], false);
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%success%", response == 0 ? "&aSuccess" : "&cError (Code: " + response + ")", false);
                sender.sendMessage(lgm.getMessage("BackupSystem.SubCommands.DeleteBackup", playerOrNull, true));
            } else return false;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        return handleCommand(player, player, args);
    }

    @Override
    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        return handleCommand(sender, null, args);
    }

    @Override
    public String name() {
        return "backup";
    }

    @Override
    public String info() {
        return "The Command to control the Config Backup System";
    }

    @Override
    public String[] aliases() {
        return new String[]{};
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        Map<Integer, String[]> subArgs = new HashMap<>();
        subArgs.put(1, new String[]{"start", "load", "delete", "listBackups", "listFiles", "listBackupsDone", "getNewestBackupFile"});
        subArgs.put(2, plugin.getBackupManager().getFileBackupList().keySet().toArray(new String[0]));
        subArgs.put(3, new String[]{"BackupFileNumber(0-Infinity)", "Newest(-1)", "<Page>"});
        return subArgs;
    }

    @Override
    public String syntax() {
        return "/apadmin backup <start|load|delete|listBackups|listFiles|listBackupsDone|getNewestBackupFile> [BackupName] [BackupFileNumber(0-Infinity)|Newest(-1)|Page]";
    }

    @Override
    public String permissionAsString() {
        return "AdminPanel.AdminPanelAdminCommands.BackupManager";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }

    private String formatStringWithFile(String in, File f) {
        Map<String, Placeholder> placeholders = new HashMap<>();
        placeholders.put("%fileName%", new Placeholder("%fileName%", f.getName(), PlaceholderType.ALL));
        placeholders.put("%absolutePath%", new Placeholder("%absolutePath%", f.getAbsolutePath(), PlaceholderType.ALL));
        placeholders.put("%normalPath%", new Placeholder("%normalPath%", f.getPath(), PlaceholderType.ALL));
        placeholders.put("%ParentFile%", new Placeholder("%parentFile%", f.getParent(), PlaceholderType.ALL));
        placeholders.put("%exists%", new Placeholder("%exists%", f.exists() ? "&aFile Exists" : "&cFile Doesn't Exist", PlaceholderType.ALL));

        return lgm.replacePlaceholders(in, placeholders);
    }

    private String formatStringWithFileBackup(String in, FileBackup fileBackup) {
        Map<String, Placeholder> placeholders = new HashMap<>();
        placeholders.put("%filesToBackup%", new Placeholder("%filesToBackup%", Arrays.toString(fileBackup.getFilesToBackup()), PlaceholderType.ALL));
        placeholders.put("%filesToBackupSize%", new Placeholder("%filesToBackupSize%", "" + fileBackup.getFilesToBackup().length, PlaceholderType.ALL));
        placeholders.put("%backupsDone%", new Placeholder("%backupsDone%", fileBackup.getBackupsDone().toString(), PlaceholderType.ALL));
        placeholders.put("%backupsDoneSize%", new Placeholder("%backupsDoneSize%", "" + fileBackup.getBackupsDone().size(), PlaceholderType.ALL));
        placeholders.put("%identifier%", new Placeholder("%identifier%", fileBackup.getIdentifier(), PlaceholderType.ALL));
        placeholders.put("%backupDest%", new Placeholder("%backupDest%", fileBackup.getDestinationPathToBackupToo(), PlaceholderType.ALL));
        placeholders.put("%newestBackupFile%", new Placeholder("%newestBackupFile%", fileBackup.getNewestBackupFile(), PlaceholderType.ALL));

        return lgm.replacePlaceholders(in, placeholders);
    }
}
