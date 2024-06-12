package de.happybavarian07.adminpanel.language.mysql.controller;/*
 * @Author HappyBavarian07
 * @Date 08.05.2024 | 19:22
 */

import de.happybavarian07.adminpanel.language.LanguageFile;
import de.happybavarian07.adminpanel.language.mysql.LanguageConverter;
import de.happybavarian07.adminpanel.main.AdminPanelMain;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ConverterManager {
    private final AdminPanelMain plugin;
    private final InnerLanguageManager innerLanguageManager;

    // Map of all the LanguageConverter Objects for each language (gets filled on startup)
    private final Map<UUID, LanguageConverter> languageConverterMap = new HashMap<>();
    private final Map<UUID, CompletableFuture<LanguageConverter>> languageConverterFutures = new HashMap<>();


    public ConverterManager(AdminPanelMain plugin, InnerLanguageManager innerLanguageManager) {
        this.plugin = plugin;
        this.innerLanguageManager = innerLanguageManager;
    }

    public LanguageConverter getLanguageConverter(UUID languageID) {
        return languageConverterMap.get(languageID);
    }

    public LanguageConverter createLanguageConverter(UUID ID, String languageShortNameOrEmpty) {
        if (languageConverterMap.containsKey(ID) && languageConverterMap.get(ID) != null)
            return languageConverterMap.get(ID);

        LanguageFile languageFile = getLanguageConverter(ID) != null ? getLanguageConverter(ID).getLanguageFile() : null;
        //LanguageFile languageFile = new LanguageFile(plugin, languageShortNameOrEmpty.isEmpty() ? getLanguageShortname(ID) : languageShortNameOrEmpty, false);
        if (languageFile == null) {
            languageFile = new LanguageFile(plugin, languageShortNameOrEmpty.isEmpty() ? innerLanguageManager.getLanguageShortname(ID) : languageShortNameOrEmpty, false);
        }

        // Get InputStream of Resource file and then create the LanguageConverter with InputStream and LanguageFile as constructor input
        InputStream inputStream = plugin.getResource("languages/" + languageFile.getLangFile().getName());

        LanguageConverter languageConverter = new LanguageConverter(
                inputStream,
                languageFile,
                languageShortNameOrEmpty.isEmpty() ? innerLanguageManager.getLanguageShortname(ID) : languageShortNameOrEmpty + "_content",
                ID
        );
        languageConverterMap.put(ID, languageConverter);
        return languageConverter;
    }

    public void addLanguageConverterFuture(UUID ID, CompletableFuture<LanguageConverter> future) {
        languageConverterFutures.put(ID, future);
    }

    public CompletableFuture<LanguageConverter> getLanguageConverterFuture(UUID ID) {
        return languageConverterFutures.get(ID);
    }

    public void removeLanguageConverterFuture(UUID ID) {
        languageConverterFutures.remove(ID);
    }

    public void removeLanguageConverter(UUID ID) {
        languageConverterMap.remove(ID);
    }

    public Map<UUID, LanguageConverter> getLanguageConverterMap() {
        return languageConverterMap;
    }

    public Map<UUID, CompletableFuture<LanguageConverter>> getLanguageConverterFutures() {
        return languageConverterFutures;
    }
}
