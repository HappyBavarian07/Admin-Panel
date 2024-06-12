package de.happybavarian07.adminpanel.language.mysql;/*
 * @Author HappyBavarian07
 * @Date 20.02.2024 | 19:45
 */

import de.happybavarian07.adminpanel.language.LanguageFile;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This class is used to convert the language files from the language files in the plugin to the mysql database.
 * It can also be used or has to be used to convert the keys from language file format to mysql format.
 */
public class LanguageConverter {
    private static LanguageConverter instance;
    private InputStream fileStream;
    private final LanguageFile languageFile;
    private final String languageContentTableName;
    private final UUID languageID;

    public LanguageConverter(LanguageFile languageFile, String languageContentTableName, UUID languageID) {
        instance = this;
        this.languageFile = languageFile;
        this.fileStream = null;
        this.languageContentTableName = languageContentTableName;
        this.languageID = languageID;
    }

    public LanguageConverter(InputStream fileStream, String languageContentTableName, UUID languageID) {
        instance = this;
        this.fileStream = fileStream;
        this.languageFile = null;
        this.languageContentTableName = languageContentTableName;
        this.languageID = languageID;
    }

    public LanguageConverter(InputStream fileStream, LanguageFile languageFile, String languageContentTableName, UUID languageID) {
        instance = this;
        this.fileStream = fileStream;
        this.languageFile = languageFile;
        this.languageContentTableName = languageContentTableName;
        this.languageID = languageID;
    }

    public static LanguageConverter getInstance() {
        return instance;
    }

    public LanguageFile getLanguageFile() {
        return languageFile;
    }

    public Map<String, Object> convertLanguageFileToDatabaseFormat(LanguageFile tempLanguageFile) {
        LanguageFile languageFile = tempLanguageFile != null ? tempLanguageFile : this.languageFile;
        languageFile.getLangConfig().saveDefaultConfig();
        languageFile.getLangConfig().reloadConfig();
        Map<String, Object> languageFileContent = languageFile.getLangConfig().getConfig().getValues(true);
        // Filter out the configuration sections using the .removeIf method
        languageFileContent.entrySet().removeIf(entry -> {
            if (entry.getValue() instanceof ConfigurationSection) {
                ConfigurationSection section = (ConfigurationSection) entry.getValue();

                /*if(section.getClass().isAssignableFrom(MemorySection.class)) {
                    //System.out.println("MemorySection: " + entry.getKey() + " | " + entry.getValue());
                }*/
                // Check if section contains values other then Configuration Sections (if it does, it's a needed Configuration Section)
                return section.getKeys(false).stream().allMatch(key -> section.get(key) instanceof ConfigurationSection);
            }
            return false;
        });
        // Convert the keys to the database format (it should be a unique key for each value, but not the entire key path, maybe encode the key path to a string and use that as the key)
        languageFileContent = languageFileContent.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().replace(".", "_"), Map.Entry::getValue));
        return languageFileContent;
    }

    public Map<String, Object> convertInputStreamToDatabaseFormat(InputStream tempInputStream) {
        // Load the InputStream into a FileConfiguration object
        if (!isInputStreamConversionSupported() && tempInputStream == null) return new LinkedHashMap<>();

        assert fileStream != null;
        // Check if input stream is closed and if yes then create a new one from the old one if possible
        if (tempInputStream != null && tempInputStream.markSupported()) {
            try {
                tempInputStream.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            tempInputStream = null;
            // Check if file stream available bytes is greater than 0 and if no create a new one from the language file
            try {
                if (fileStream.available() <= 0) {
                    if (languageFile != null) {
                        fileStream = new FileInputStream(languageFile.getLangFile());
                    } else {
                        return new LinkedHashMap<>();
                    }
                }
            } catch (IOException e) {
                if (languageFile != null) {
                    try {
                        fileStream = new FileInputStream(languageFile.getLangFile());
                    } catch (FileNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    return new LinkedHashMap<>();
                }
            }
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(tempInputStream != null ? tempInputStream : fileStream));

        // Get all the values from the FileConfiguration object
        Map<String, Object> content = config.getValues(true);

        // Filter out the configuration sections using the .removeIf method
        content.entrySet().removeIf(entry -> {
            if (entry.getValue() instanceof ConfigurationSection) {
                ConfigurationSection section = (ConfigurationSection) entry.getValue();

                /*if (section.getClass().isAssignableFrom(MemorySection.class)) {
                    System.out.println("MemorySection: " + entry.getKey() + " | " + entry.getValue());
                }*/
                // Check if section contains values other then Configuration Sections (if it does, it's a needed Configuration Section)
                return section.getKeys(false).stream().allMatch(key -> section.get(key) instanceof ConfigurationSection);
            }
            return false;
        });

        // Convert the keys to the database format and return the converted map
        return content;
        //return content.entrySet().stream()
        //        .collect(Collectors.toMap(entry -> convertKeyToDatabaseFormat(entry.getKey()), Map.Entry::getValue));
    }

    public String getLanguageContentTableName() {
        return languageContentTableName;
    }

    public boolean isInputStreamConversionSupported() {
        return fileStream != null;
    }

    public boolean isLanguageFileConversionSupported() {
        return languageFile != null;
    }

    public String convertKeyToDatabaseFormat(String key) {
        return Arrays.toString(Base64.encodeBase64(key.replace(".", "_").getBytes(StandardCharsets.UTF_8)));
    }

    public String convertDatabaseKeyToLanguageFormat(String key) {
        return new String(Base64.decodeBase64(key), StandardCharsets.UTF_8).replace("_", ".");
    }

    public Map<String, Object> convertDatabaseFormatToLanguageFile(Map<String, Object> databaseContent) {
        return databaseContent.entrySet().stream().collect(Collectors.toMap(entry -> convertDatabaseKeyToLanguageFormat(entry.getKey()), Map.Entry::getValue));
    }

    public Map<String, Object> convertLanguageToDatabaseFormat(boolean inputStream, InputStream tempInputStream, boolean languageFile, LanguageFile tempLanguageFile) {
        if (isInputStreamConversionSupported() || (inputStream && tempInputStream != null)) return convertInputStreamToDatabaseFormat(tempInputStream);
        if (isLanguageFileConversionSupported() || (languageFile && tempLanguageFile != null)) return convertLanguageFileToDatabaseFormat(tempLanguageFile);
        return new LinkedHashMap<>();
    }
}
