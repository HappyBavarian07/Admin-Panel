package de.happybavarian07.adminpanel.backupmanager;/*
 * @Author HappyBavarian07
 * @Date 28.01.2023 | 16:08
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileBackup implements Comparable<FileBackup> {
    private String identifier;
    private File[] filesToBackup;
    // Relative to the Admin-Panel Plugin Folder
    private String destinationPathToBackupToo;
    private List<File> backupsDone;

    public FileBackup(String indetifier, File[] filesToBackup, String destinationPathToBackupToo) {
        this.identifier = indetifier;
        this.filesToBackup = filesToBackup;
        this.destinationPathToBackupToo = destinationPathToBackupToo;
        this.backupsDone = new ArrayList<>();
    }

    /**
     * Error Codes:
     * 0 = Success,
     * -1 = Files to Backup is null or there are none,
     * -2 = IO Exception,
     * -3 = There is no Destination to backup too,
     * -4 = The Backups exceed the maxBackups Number
     * @param maxBackups Number of Backups before deleting existing ones
     * @return Error Code
     */
    public int backup(int maxBackups) {
        if (filesToBackup == null || filesToBackup.length == 0) return -1;
        if (destinationPathToBackupToo == null || destinationPathToBackupToo.isEmpty()) return -3;
        if (backupsDone.size() >= maxBackups) return -4;
        try {
            File zipFile = new File(AdminPanelMain.getPlugin().getDataFolder() + File.separator + destinationPathToBackupToo + "_" + (backupsDone.size() + 1) + ".zip");
            if (backupsDone.contains(zipFile)) {
                for (int i = 1; i <= maxBackups; i++) {
                    if (!backupsDone.contains(new File(AdminPanelMain.getPlugin().getDataFolder() + File.separator + destinationPathToBackupToo + "_" + (i) + ".zip"))) {
                        zipFile = new File(AdminPanelMain.getPlugin().getDataFolder() + File.separator + destinationPathToBackupToo + "_" + (i) + ".zip");
                        break;
                    }
                }
            }
            Utils.zipFiles(filesToBackup, zipFile.getAbsolutePath());
            backupsDone.add(zipFile);
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return -2;
        }
    }

    public void removeLastBackup() {
        File fileToRemove = new File(AdminPanelMain.getPlugin().getDataFolder() + File.separator + destinationPathToBackupToo + "_" + backupsDone.size());
        if (fileToRemove.delete()) backupsDone.remove(fileToRemove);
    }

    public void removeOldestBackup() {
        Map<Long, File> backupFileDates = new HashMap<>();
        for (File backupFile : backupsDone) {
            backupFileDates.put(backupFile.lastModified(), backupFile);
        }
        long minBackupFileDate = Collections.min(backupFileDates.keySet());
        if (minBackupFileDate == 0) return;
        if (backupFileDates.get(minBackupFileDate).delete())
            backupsDone.remove(backupFileDates.get(minBackupFileDate));
    }

    public File getNewestBackupFile() {
        Map<Long, File> backupFileDates = new HashMap<>();
        for (File backupFile : backupsDone) {
            backupFileDates.put(backupFile.lastModified(), backupFile);
        }
        long maxBackupFileDate = Collections.max(backupFileDates.keySet());
        if (maxBackupFileDate == 0) return null;
        if (backupFileDates.get(maxBackupFileDate).exists())
            return backupFileDates.get(maxBackupFileDate);
        return null;
    }

    /**
     * Error Codes:
     * 0 = Success,
     * -1 = Files to Backup is null or there are none,
     * -2 = IO Exception,
     * -3 = Zip File is null or doesn't exist
     * @param zipFile The Zip File
     * @return Error Code
     */
    public int loadBackup(File zipFile) {
        // Create copy of the old configs

        if (filesToBackup == null || filesToBackup.length == 0) return -1;
        if(zipFile == null || !zipFile.exists()) return -3;
        try {
            File zipFileTemp = new File(AdminPanelMain.getPlugin().getDataFolder() + File.separator + "old_configs.zip");
            Utils.zipFiles(filesToBackup, zipFileTemp.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return -2;
        }
        // Delete the Old Configs after creating a copy of those files

        for (File f : filesToBackup) {
            new File(f.getAbsolutePath()).delete();
        }

        // Load new one

        Utils.unzipFiles(zipFile.getAbsolutePath(), AdminPanelMain.getPlugin().getDataFolder() + File.separator);
        return 0;
    }

    /**
     * Error Codes:
     * 0 = Success,
     * -1 = Files to Backup is null or there are none,
     * -2 = IO Exception,
     * -3 = Zip File is null or doesn't exist
     * @param number The Zip File Number from the BackupsDone List (-1 = newest)
     * @return Error Code
     */
    public int deleteZipBackup(int number) {
        File zipFile = number == -1 ? getNewestBackupFile() : getBackupFileFromNumber(number);

        if (filesToBackup == null || filesToBackup.length == 0) return -1;
        if(zipFile == null || !zipFile.exists()) return -3;

        if(zipFile.delete()) return 0;
        else return -2;
    }

    /**
     * Error Codes:
     * 0 = Success,
     * -1 = Files to Backup is null or there are none,
     * -2 = IO Exception,
     * -3 = Zip File is null or doesn't exist
     * @param zipFile The Zip File
     * @return Error Code
     */
    public int deleteZipBackup(File zipFile) {
        if (filesToBackup == null || filesToBackup.length == 0) return -1;
        if(zipFile == null || !zipFile.exists()) return -3;

        if(zipFile.delete()) return 0;
        else return -2;
    }

    public File getBackupFileFromNumber(int number) {
        return backupsDone.get(number);
    }

    public File getBackupFromFileName(String filename) {
        return backupsDone.stream().filter(f -> f.getName().equalsIgnoreCase(filename)).findFirst().get();
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public File[] getFilesToBackup() {
        return filesToBackup;
    }

    public void setFilesToBackup(File[] filesToBackup) {
        this.filesToBackup = filesToBackup;
    }

    public String getDestinationPathToBackupToo() {
        return destinationPathToBackupToo;
    }

    public void setDestinationPathToBackupToo(String destinationPathToBackupToo) {
        this.destinationPathToBackupToo = destinationPathToBackupToo;
    }

    public List<File> getBackupsDone() {
        return backupsDone;
    }

    public void setBackupsDone(List<File> backupsDone) {
        this.backupsDone = backupsDone;
    }

    public void addBackupDone(File backupDone) {
        backupsDone.add(backupDone);
    }

    @Override
    public int compareTo(@NotNull FileBackup o) {
        int identifierComparison = this.identifier.compareTo(o.identifier);
        if (identifierComparison != 0) {
            return identifierComparison;
        }

        int destinationPathComparison = this.destinationPathToBackupToo.compareTo(o.destinationPathToBackupToo);
        if (destinationPathComparison != 0) {
            return destinationPathComparison;
        }

        return this.filesToBackup.length - o.filesToBackup.length;
    }
}
