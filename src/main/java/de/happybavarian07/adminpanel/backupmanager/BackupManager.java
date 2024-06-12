package de.happybavarian07.adminpanel.backupmanager;/*
 * @Author HappyBavarian07
 * @Date 28.01.2023 | 16:11
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BackupManager {
    private final AdminPanelMain plugin;
    // Relative to the Admin-Panel Plugin Folder
    private final String backupFolder;
    private Map<String, FileBackup> fileBackupList;
    private int numberOfBackUpsBeforeDeleting;

    public BackupManager(AdminPanelMain plugin, int numberOfBackUpsBeforeDeleting, String backupFolder) {
        this.plugin = plugin;
        this.fileBackupList = new HashMap<>();
        this.numberOfBackUpsBeforeDeleting = numberOfBackUpsBeforeDeleting;
        this.backupFolder = backupFolder;
    }

    public void addFileBackup(FileBackup backup) {
        File[] filesInBackupFolder = new File(plugin.getDataFolder() + File.separator + backupFolder).listFiles();
        if (filesInBackupFolder != null && filesInBackupFolder.length != 0) {
            for (File f : filesInBackupFolder) {
                if ((f.getParentFile().getName() + "/" + f.getName()).contains(backup.getDestinationPathToBackupToo() + "_"))
                    backup.addBackupDone(f);
            }
        }

        fileBackupList.put(backup.getIdentifier(), backup);
    }

    public void removeFileBackup(FileBackup backup) {
        fileBackupList.remove(backup.getIdentifier());
    }

    /**
     * Starts a backup
     *
     * @param identifier The Name of the Backup
     * @return Error Code (if Backup from identifier is null then -100)
     */
    public int startBackup(String identifier) {
        FileBackup backup = fileBackupList.get(identifier);
        if (numberOfBackUpsBeforeDeleting <= backup.getBackupsDone().size()) {
            backup.removeOldestBackup();
        }
        return backup.backup(numberOfBackUpsBeforeDeleting);
    }

    /**
     * Loads a file backup
     *
     * @param identifier   name of the backup
     * @param backupNumber number of the backup (-1 = newest)
     * @return Error Code (if Backup from identifier is null then -100)
     */
    public int loadBackup(String identifier, int backupNumber) {
        FileBackup backup = fileBackupList.get(identifier);
        if (backup == null) return -100;
        return backup.loadBackup(backupNumber == -1 ? backup.getNewestBackupFile() : backup.getBackupFileFromNumber(backupNumber));
    }

    public FileBackup getFileBackup(String identifier) {
        return fileBackupList.get(identifier);
    }

    public void backupAllFileBackups() {
        fileBackupList.keySet().forEach(this::startBackup);
    }

    public Map<String, FileBackup> getFileBackupList() {
        return fileBackupList;
    }

    public void setFileBackupList(Map<String, FileBackup> fileBackupList) {
        this.fileBackupList = fileBackupList;
    }

    public int getNumberOfBackUpsBeforeDeleting() {
        return numberOfBackUpsBeforeDeleting;
    }

    public void setNumberOfBackUpsBeforeDeleting(int numberOfBackUpsBeforeDeleting) {
        this.numberOfBackUpsBeforeDeleting = numberOfBackUpsBeforeDeleting;
    }

    public String getBackupFolder() {
        return backupFolder;
    }

    /**
     * Loads a file backup
     *
     * @param identifier   name of the backup
     * @param backupFile number or name of the backup (-1 = newest)
     * @return Error Code (if Backup from identifier is null then -100)
     */
    public int deleteBackupFile(String identifier, String backupFile) {
        FileBackup backup = fileBackupList.get(identifier);
        if (backup == null) return -100;
        try {
            int backupFileInt = Integer.parseInt(backupFile) - 1;
            return backup.deleteZipBackup(backupFileInt);
        } catch (NumberFormatException e) {
            return backup.deleteZipBackup(backup.getBackupFromFileName(backupFile));
        }
    }
}
