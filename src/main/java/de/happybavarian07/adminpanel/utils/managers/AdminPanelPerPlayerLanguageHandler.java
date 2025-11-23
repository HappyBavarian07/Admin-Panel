package de.happybavarian07.adminpanel.utils.managers;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.service.api.DataService;
import de.happybavarian07.adminpanel.utils.LogPrefixExtension;
import de.happybavarian07.coolstufflib.languagemanager.LanguageFile;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import de.happybavarian07.coolstufflib.languagemanager.PerPlayerLanguageHandler;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class AdminPanelPerPlayerLanguageHandler extends PerPlayerLanguageHandler {
    private final Map<String, String> persistedPlayerLangs = new ConcurrentHashMap<>();

    public AdminPanelPerPlayerLanguageHandler(LanguageManager languageManager, File dataFile, FileConfiguration dataYml) {
        super(languageManager, dataFile, dataYml);
        try {
            DataService ds = AdminPanelMain.getPlugin().getDataService();
            if (ds != null) {
                Map<String, String> saved = ds.loadMap("language.players", String.class).join();
                if (saved != null && !saved.isEmpty()) {
                    persistedPlayerLangs.putAll(saved);
                    for (Map.Entry<String, String> e : saved.entrySet()) {
                        try {
                            UUID id = UUID.fromString(e.getKey());
                            String lang = e.getValue();
                            if (lang != null && languageManager.getLang(lang, false) != null) {
                                super.setPlayerLanguage(id, lang);
                            }
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void flushSync() {
        try {
            DataService ds = AdminPanelMain.getPlugin().getDataService();
            if (ds == null) return;
            Map<String, String> copy = new HashMap<>(persistedPlayerLangs);
            try {
                ds.save("language.players", copy).join();
            } catch (Exception ex) {
                AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.WARNING, "Failed to flush per-player languages: " + ex.getMessage(), LogPrefixExtension.ADMINPANEL_MAIN);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setPlayerLanguage(UUID playerUUID, String languageName) {
        super.setPlayerLanguage(playerUUID, languageName);
        if (playerUUID == null) return;
        if (languageName == null) {
            persistedPlayerLangs.remove(playerUUID.toString());
        } else {
            persistedPlayerLangs.put(playerUUID.toString(), languageName);
        }
        persistAsync();
    }

    @Override
    public void removePlayerLanguage(UUID playerUUID) {
        super.removePlayerLanguage(playerUUID);
        if (playerUUID == null) return;
        persistedPlayerLangs.remove(playerUUID.toString());
        persistAsync();
    }

    private void persistAsync() {
        try {
            DataService ds = AdminPanelMain.getPlugin().getDataService();
            if (ds == null) return;
            Map<String, String> copy = new HashMap<>(persistedPlayerLangs);
            CompletableFuture<Void> f = ds.save("language.players", copy);
            f.exceptionally(ex -> null);
        } catch (Exception ignored) {
        }
    }

    @Override
    public Map<UUID, LanguageFile> getPlayerLanguages() {
        return super.getPlayerLanguages();
    }
}
