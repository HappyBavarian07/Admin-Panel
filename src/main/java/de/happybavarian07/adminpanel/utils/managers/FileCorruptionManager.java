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
import java.util.regex.Pattern;

/**
 * <p>FileCorruptionManager provides integrity checking, structure validation, and backup restoration for plugin files.</p>
 * <ul>
 *   <li>Checks files and directories for corruption, missing resources, and format errors.</li>
 *   <li>Supports pattern-based validation using regexes for dynamic content.</li>
 *   <li>Restores corrupted files from backup if available.</li>
 *   <li>Integrates with the plugin's resource and backup systems.</li>
 * </ul>
 * <pre><code>FileCorruptionManager manager = new FileCorruptionManager(plugin, backup);
 * CorruptionCheckResult result = manager.checkFileForCorruption(file);
 * </code></pre>
 */
public class FileCorruptionManager {
    private final AdminPanelMain plugin;
    private final FileBackup configBackup;

    /**
     * <p>Constructs a new FileCorruptionManager for managing file integrity and backup operations.</p>
     * <pre><code>FileCorruptionManager manager = new FileCorruptionManager(plugin, backup);</code></pre>
     *
     * @param plugin       the plugin instance
     * @param configBackup the backup manager
     */
    public FileCorruptionManager(AdminPanelMain plugin, FileBackup configBackup) {
        this.plugin = plugin;
        this.configBackup = configBackup;
    }

    /**
     * <p>Checks a file for corruption by validating its structure, integrity, and optionally matching it against a pattern file.</p>
     * <pre><code>CorruptionCheckResult result = manager.checkFileForCorruption(file);</code></pre>
     *
     * @param file the file to check
     * @return the result of the corruption check
     */
    public CorruptionCheckResult checkFileForCorruption(File file) {
        if (file.isDirectory()) {
            return CorruptionCheckResult.CHECK_FAILED_FILE_IS_DIRECTORY;
        }
        if (file.exists()) {
            String resourceName = file.getAbsolutePath().replace(plugin.getDataFolder().getAbsolutePath(), "").substring(1).replace(File.separator, "/");
            InputStream patternResourceIn = plugin.getResource(resourceName + ".pattern");
            if (patternResourceIn != null) {
                try {
                    if (!PatternFileValidator.matchesPattern(file, patternResourceIn)) {
                        return CorruptionCheckResult.PATTERN_MISMATCH;
                    }
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, "Pattern validation failed for file: " + file.getAbsolutePath(), ex);
                    return CorruptionCheckResult.CHECK_FAILED_IO_ERROR;
                }
            }
            InputStream resourceIn = plugin.getResource(resourceName);
            if (resourceIn == null) {
                return CorruptionCheckResult.CHECK_FAILED_MISSING_RESOURCE;
            }

            // Do the Length Check
            if (file.length() == 0) {
                return CorruptionCheckResult.EMPTY;
            }

            // Do the Integrity Check
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                reader.readLine();
            } catch (IOException ex) {
                return CorruptionCheckResult.INTEGRITY_ERROR;
            }

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
                    boolean found = false;
                    for (String resourceKey : resourceKeys) {
                        for (String fileKey : fileKeys) {
                            if (Pattern.matches(resourceKey, fileKey)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            return CorruptionCheckResult.DATA_ERROR;
                        }
                        found = false;
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
                    boolean found = false;
                    for (String resourceKey : resourceProperties.stringPropertyNames()) {
                        for (String fileKey : properties.stringPropertyNames()) {
                            if (Pattern.matches(resourceKey, fileKey)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            return CorruptionCheckResult.DATA_ERROR;
                        }
                        found = false;
                    }
                } catch (IOException ex) {
                    return CorruptionCheckResult.CORRUPTED;
                }
            } else {
                return CorruptionCheckResult.CHECK_FAILED_INVALID_FORMAT;
            }

            return CorruptionCheckResult.VALID;
        }
        return CorruptionCheckResult.FILE_MISSING;
    }

    /**
     * <p>Checks an array of files for corruption, including recursive directory traversal.</p>
     * {@code Map<File, CorruptionCheckResult> results = manager.checkFilesForCorruption(files);}
     *
     * @param files the files to check
     * @return a map of files to their corruption check results
     */
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

    /**
     * <p>Recursively checks a directory and its contents for corruption.</p>
     * {@code Map<File, CorruptionCheckResult> results = manager.checkDirectoryForCorruption(directory);}
     *
     * @param directory the directory to check
     * @return a map of files to their corruption check results
     */
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

    /**
     * <p>Runs the corruption check and attempts to restore corrupted files from backup if needed.</p>
     * <pre><code>manager.handleConfigBackupCheck();</code></pre>
     */
    public void handleConfigBackupCheck() {
        // TODO Tell the user about this way of disabling the Config Backup Check
        if (!new File(plugin.getDataFolder(), "DisableConfigBackupCorruptionCheckIKnowWhatIAmDoingISwear").exists()) {
            plugin.getLogger().info(ChatColor.YELLOW + "Starting File Corruption Check...");
            Map<File, CorruptionCheckResult> results = checkFilesForCorruption(configBackup.getFilesToBackup());
            for (Map.Entry<File, CorruptionCheckResult> entry : results.entrySet()) {
                String relativePath = entry.getKey().getAbsolutePath().replace(plugin.getDataFolder().getAbsolutePath(), "").substring(1).replace(File.separator, "/");
                String corruptedString = entry.getValue().isCorrupted() ? ChatColor.RED + "Corrupted" : ChatColor.GREEN + "Not Corrupted";
                int totalLength = 120;
                String message = "[Admin-Panel] Checked File '" + ChatColor.YELLOW + relativePath + ChatColor.WHITE + "' for corruption.";
                int messageLength = ChatColor.stripColor(message).length();
                int dashRepeatLength = totalLength - messageLength - 1;
                if (dashRepeatLength < 0) {
                    dashRepeatLength = 1;
                }
                String dashes = " " + "-".repeat(dashRepeatLength) + " ";
                Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + message + dashes + "Result: " + corruptedString + " (" + ChatColor.GOLD + entry.getValue().getReason() + ChatColor.WHITE + ")");
            }
            if (results.values().stream().anyMatch(CorruptionCheckResult::isCorrupted)) {
                plugin.getLogger().warning(ChatColor.RED + "Trying to replace corrupted files and back them up for checking...");
                int backupLoadResult = configBackup.loadSpecificFilesFromBackup(configBackup.getNewestBackupFile(), results.keySet().stream().filter(f -> results.get(f).isCorrupted()).toArray(File[]::new));
                if (backupLoadResult == 0) {
                    plugin.getLogger().info(ChatColor.GREEN + "Backup loaded successfully!");
                } else {
                    plugin.getLogger().severe(ChatColor.RED + "Backup could either not be loaded or only half loaded! Error Code: " + backupLoadResult);
                }
            }
            plugin.getLogger().info(ChatColor.YELLOW + "File Corruption Check finished!");
        }
    }

    /**
     * <p>Enum representing the result of a file corruption check.</p>
     * <pre><code>CorruptionCheckResult result = CorruptionCheckResult.VALID;</code></pre>
     */
    public enum CorruptionCheckResult {
        CHECK_FAILED_SECURITY_EXCEPTION(false, -5, "A security exception was encountered."),
        CHECK_FAILED_IO_ERROR(true, -4, "An I/O error occurred during the check."),
        CHECK_FAILED_INVALID_FORMAT(true, -3, "The file format is invalid."),
        CHECK_FAILED_FILE_IS_DIRECTORY(true, -2, "The file is a directory."),
        CHECK_FAILED_MISSING_RESOURCE(false, -1, "Resource file is missing."),
        VALID(false, 0, "The file is valid."),
        FILE_MISSING(false, 1, "The file could not be found or opened."),
        EMPTY(true, 2, "The file is empty."),
        INTEGRITY_ERROR(true, 3, "The file integrity check failed."),
        DATA_ERROR(true, 4, "The file contains data errors."),
        CORRUPTED(true, 5, "The file is corrupted, but the System couldn't find a reason."),
        PATTERN_MISMATCH(true, 6, "The file does not match the required pattern.");

        private final boolean corrupted;
        private final int errorCode;
        private final String reason;

        /**
         * Constructs a CorruptionCheckResult with the specified parameters.
         *
         * @param corrupted whether the file is corrupted
         * @param errorCode the error code associated with the corruption check
         * @param reason    the reason for the corruption check result
         */
        CorruptionCheckResult(boolean corrupted, int errorCode, String reason) {
            this.corrupted = corrupted;
            this.errorCode = errorCode;
            this.reason = reason;
        }

        /**
         * Returns whether the file is corrupted or not
         * If the file is corrupted, it will return true, otherwise false
         *
         * @return true if the file is corrupted, false otherwise
         */
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
