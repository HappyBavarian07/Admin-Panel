package de.happybavarian07.adminpanel.utils.managers;

import de.happybavarian07.adminpanel.backupmanager.FileBackup;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

/*
 * @Author HappyBavarian07
 * @Date September 11, 2024 | 16:10
 */
// This Class is used to manage the File Corruption of the Plugin
// If a File is corrupted, it will be backed up and the corrupted file will be replaced with the backup hopefully
// This is used to prevent data loss
// It will run by default on startup before the backup process happens, so that it can catch any corrupted files before they are backed up
// This is a very important class, as it can prevent data loss in case of a corrupted file
// You can disable it, along with the Config Backup inside the config.yml
public class FileCorruptionManager {
    private final AdminPanelMain plugin;
    private FileBackup configBackup;

    public FileCorruptionManager(AdminPanelMain plugin, FileBackup configBackup) {
        this.plugin = plugin;
        this.configBackup = configBackup;
    }

    // This Method is used to check if the Config is corrupted
    // This method will check one file against the hopefully correct stored File in the Plugin's Resources
    public CorruptionCheckResult checkFileForCorruption(File file) {
        if (file.isDirectory()) {
            return CorruptionCheckResult.CHECK_FAILED_FILE_IS_DIRECTORY;
        }
        if (file.exists()) {
            String resourceName = file.getAbsolutePath().replace(plugin.getDataFolder().getAbsolutePath(), "").substring(1).replace(File.separator, "/");
            InputStream resourceIn = plugin.getResource(resourceName);
            //System.out.println("ResourceIn: " + resourceIn);
            //System.out.println("ResourceName: " + resourceName);
            if (resourceIn == null) {
                return CorruptionCheckResult.CHECK_FAILED_MISSING_RESOURCE;
            }

            // Do the Length Check
            if (file.length() == 0) {
                return CorruptionCheckResult.EMPTY;
            }

            // Do the Integrity Check
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                reader.readLine();
            } catch (IOException ex) {
                return CorruptionCheckResult.INTEGRITY_ERROR;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        plugin.getLogger().log(Level.SEVERE, "An error occurred while closing the BufferedReader!", ex);
                    }
                }
            }

            // Check file type and handle accordingly
            String fileName = file.getName();
            if (fileName.endsWith(".yml")) {
                try {
                    YamlConfiguration config = new YamlConfiguration();
                    config.load(file);
                } catch (FileNotFoundException ex) {
                    return CorruptionCheckResult.CHECK_FAILED_INVALID_FORMAT;
                } catch (IOException ex) {
                    return CorruptionCheckResult.CORRUPTED;
                } catch (InvalidConfigurationException ex) {
                    return CorruptionCheckResult.INTEGRITY_ERROR;
                }

                try {
                    FileConfiguration resourceConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(resourceIn));
                    FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                    Set<String> resourceKeys = resourceConfig.getKeys(true);
                    Set<String> fileKeys = fileConfig.getKeys(true);
                    // Go through all keys in the resource file and check if they are in the file
                    for (String key : resourceKeys) {
                        if (!fileKeys.contains(key)) {
                            return CorruptionCheckResult.DATA_ERROR;
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    return CorruptionCheckResult.FILE_MISSING;
                }
            } else if (fileName.endsWith(".properties")) {
                try {
                    Properties properties = new Properties();
                    properties.load(new FileInputStream(file));
                    Properties resourceProperties = new Properties();
                    resourceProperties.load(resourceIn);
                    for (String key : resourceProperties.stringPropertyNames()) {
                        if (!properties.containsKey(key)) {
                            return CorruptionCheckResult.DATA_ERROR;
                        }
                    }
                } catch (IOException ex) {
                    return CorruptionCheckResult.CORRUPTED;
                }
            } else {
                return CorruptionCheckResult.CHECK_FAILED_INVALID_FORMAT;
            }

            // If the File is not corrupted, return VALID
            return CorruptionCheckResult.VALID;
        }
        return CorruptionCheckResult.FILE_MISSING;
    }

    public Map<File, CorruptionCheckResult> checkFilesForCorruption(File[] files) {
        Map<File, CorruptionCheckResult> results = new HashMap<>();
        for (File file : files) {
            if (file.isDirectory()) {
                results.putAll(checkDirectoryForCorruption(file));
            } else {
                results.put(file, checkFileForCorruption(file));
            }
        }
        return results;
    }

    public Map<File, CorruptionCheckResult> checkDirectoryForCorruption(File directory) {
        Map<File, CorruptionCheckResult> results = new HashMap<>();
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                results.putAll(checkDirectoryForCorruption(file));
            } else {
                results.put(file, checkFileForCorruption(file));
            }
        }
        return results;
    }

    public void handleConfigBackupCheck() {
        // TODO Tell the user about this way of disabling the Config Backup Check
        if (!new File(plugin.getDataFolder(), "DisableConfigBackupCorruptionCheckIKnowWhatIAmDoingISwear").exists()) {
            plugin.getLogger().info(ChatColor.YELLOW + "Starting File Corruption Check...");
            Map<File, CorruptionCheckResult> results = checkFilesForCorruption(configBackup.getFilesToBackup());
            for (Map.Entry<File, CorruptionCheckResult> entry : results.entrySet()) {
                String relativePath = entry.getKey().getAbsolutePath().replace(plugin.getDataFolder().getAbsolutePath(), "").substring(1).replace(File.separator, "/");
                String corruptedString = entry.getValue().isCorrupted() ? ChatColor.RED + "Corrupted" : ChatColor.GREEN + "Not Corrupted";
                int totalLength = 120; // Adjust this value as needed
                String message = "[Admin-Panel] Checked File '" + ChatColor.YELLOW + relativePath + ChatColor.WHITE + "' for corruption.";
                int messageLength = ChatColor.stripColor(message).length();
                int dashRepeatLength = totalLength - messageLength - 1;
                if(dashRepeatLength < 0) {
                    dashRepeatLength = 1;
                }
                String dashes = " " + "-".repeat(dashRepeatLength) + " ";
                Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + message + dashes + "Result: " + corruptedString + " (" + ChatColor.GOLD + entry.getValue().getReason() + ChatColor.WHITE + ")");
            }
            if (results.values().stream().anyMatch(CorruptionCheckResult::isCorrupted)) {
                plugin.getLogger().warning(ChatColor.RED + "Replacing corrupted files and backing them up for checking...");
                int backupLoadResult = configBackup.loadSpecificFilesFromBackup(configBackup.getNewestBackupFile(), results.keySet().stream().filter(f -> results.get(f).isCorrupted()).toArray(File[]::new));
                if (backupLoadResult == 0) {
                    plugin.getLogger().info(ChatColor.GREEN + "Backup loaded successfully!");
                } else {
                    plugin.getLogger().severe(ChatColor.RED + "Backup could not be loaded! Error Code: " + backupLoadResult);
                }
            }
            plugin.getLogger().info(ChatColor.YELLOW + "File Corruption Check finished!");
        }
    }

    public enum CorruptionCheckResult {
        CHECK_FAILED_SECURITY_EXCEPTION(false, -5, "A security exception was encountered."),
        CHECK_FAILED_IO_ERROR(false, -4, "An I/O error occurred during the check."),
        CHECK_FAILED_INVALID_FORMAT(true, -3, "The file format is invalid."),
        CHECK_FAILED_FILE_IS_DIRECTORY(false, -2, "The file is a directory."),
        CHECK_FAILED_MISSING_RESOURCE(false, -1, "Resource file is missing."),
        VALID(false, 0, "The file is valid."),
        FILE_MISSING(false, 1, "The file could not be found or opened."),
        EMPTY(true, 2, "The file is empty."),
        INTEGRITY_ERROR(true, 3, "The file integrity check failed."),
        DATA_ERROR(true, 4, "The file contains data errors."),
        CORRUPTED(true, 5, "The file is corrupted, but the System couldn't find a reason.");

        private final boolean corrupted;
        private final int errorCode;
        private final String reason;

        CorruptionCheckResult(boolean corrupted, int errorCode, String reason) {
            this.corrupted = corrupted;
            this.errorCode = errorCode;
            this.reason = reason;
        }

        public boolean isCorrupted() {
            return corrupted;
        }

        /**
         * Returns the Error Code of the Corruption Check
         * The Error Code is used to identify the Error and at the same time shows the Level of severity
         *
         * @return The Error Code of the Corruption Check
         */
        public int getErrorCode() {
            return errorCode;
        }

        /**
         * Returns the Reason of the Corruption Check
         * The Reason shows why the File is corrupted or why the check failed
         *
         * @return The Reason of the Corruption Check
         */
        public String getReason() {
            return reason;
        }
    }
}
