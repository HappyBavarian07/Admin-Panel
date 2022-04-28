package de.happybavarian07.adminpanel.main;/*
 * @Author HappyBavarian07
 * @Date 15.11.2021 | 17:59
 */

import de.happybavarian07.adminpanel.configupdater.KeyBuilder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class LanguageFileUpdater {
    private static AdminPanelMain plugin;
    private final LanguageManager lgm;
    private static final char SEPARATOR = '.';

    public LanguageFileUpdater(AdminPanelMain plugin) {
        LanguageFileUpdater.plugin = plugin;
        this.lgm = plugin.getLanguageManager();
    }

    public static void update(String resourceName, File toUpdate) throws IOException {
        if (plugin.getResource(resourceName) == null) {
            if (plugin.getResource("languages/" + plugin.getConfig().getString("Plugin.languageForUpdates") + ".yml") != null) {
                resourceName = "languages/" + plugin.getConfig().getString("Plugin.languageForUpdates") + ".yml";
            } else {
                resourceName = "languages/en.yml";
            }
        }
        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(resourceName), StandardCharsets.UTF_8));
        FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(toUpdate);
        Map<String, String> comments = parseComments(plugin, resourceName, defaultConfig);
    }

    //Returns a map of key comment pairs. If a key doesn't have any comments it won't be included in the map.
    private static Map<String, String> parseComments(Plugin plugin, String resourceName, FileConfiguration defaultConfig) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(plugin.getResource(resourceName)));
        Map<String, String> comments = new LinkedHashMap<>();
        StringBuilder commentBuilder = new StringBuilder();
        KeyBuilder keyBuilder = new KeyBuilder(defaultConfig, SEPARATOR);

        String line;
        while ((line = reader.readLine()) != null) {
            String trimmedLine = line.trim();

            //Only getting comments for keys. A list/array element comment(s) not supported
            if (trimmedLine.startsWith("-")) {
                continue;
            }

            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {//Is blank line or is comment
                commentBuilder.append(trimmedLine).append("\n");
            } else {//is a valid yaml key
                keyBuilder.parseLine(trimmedLine);
                String key = keyBuilder.toString();

                //If there is a comment associated with the key it is added to comments map and the commentBuilder is reset
                if (commentBuilder.length() > 0) {
                    comments.put(key, commentBuilder.toString());
                    commentBuilder.setLength(0);
                }

                //Remove the last key from keyBuilder if current path isn't a config section or if it is empty to prepare for the next key
                if (!keyBuilder.isConfigSectionWithKeys()) {
                    keyBuilder.removeLastKey();
                }
            }
        }

        reader.close();

        if (commentBuilder.length() > 0)
            comments.put(null, commentBuilder.toString());

        return comments;
    }

    private static void writeCommentIfExists(Map<String, String> comments, BufferedWriter writer, String fullKey, String indents) throws IOException {
        String comment = comments.get(fullKey);

        //Comments always end with new line (\n)
        if (comment != null)
            //Replaces all '\n' with '\n' + indents except for the last one
            writer.write(indents + comment.substring(0, comment.length() - 1).replace("\n", "\n" + indents) + "\n");
    }

    //Input: 'key1.key2' Result: 'key1'
    private static void removeLastKey(StringBuilder keyBuilder) {
        if (keyBuilder.length() == 0)
            return;

        String keyString = keyBuilder.toString();
        //Must be enclosed in brackets in case a regex special character is the separator
        String[] split = keyString.split("[" + SEPARATOR + "]");
        //Makes sure begin index isn't < 0 (error). Occurs when there is only one key in the path
        int minIndex = Math.max(0, keyBuilder.length() - split[split.length - 1].length() - 1);
        keyBuilder.replace(minIndex, keyBuilder.length(), "");
    }

    private static void appendNewLine(StringBuilder builder) {
        if (builder.length() > 0)
            builder.append("\n");
    }

    /*
    Entweder default config erstellen und werte von alter Config einlesen und dort reinschreiben
    oder anders
     */

    /*public void updateFile(FileConfiguration oldConfig, String newConfigString, String langName) {
        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource(newConfigString))));
        for(String path : oldConfig) {

        }
    }*/
}
