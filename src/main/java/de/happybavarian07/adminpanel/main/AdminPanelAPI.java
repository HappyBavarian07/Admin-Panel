package de.happybavarian07.adminpanel.main;

import de.happybavarian07.adminpanel.commandmanagement.CommandManager;
import de.happybavarian07.adminpanel.events.AdminPanelEvent;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AdminPanelAPI {
    // Basic

    /**
     * Offnet das angegebene Menü für den in der PlayerMenuUtility angegebenen Spieler
     * Und um von dem Menu eine Instance zu bilden braucht man die {@code getPlayerMenuUtility(Player player)} Methode
     *
     * @param menu Ist das Menü das man öffnen will
     */
    void openPanel(Menu menu);

    /**
     * Gibt die PlayerMenuUtility Map zurück
     * Sie wird dafür benutzt um Informationen über die Spieler,
     * die das Panel offen haben überall nutzen zu können!
     *
     * @return Die Map
     */
    Map<Player, PlayerMenuUtility> getPlayerMenuUtilityMap();

    /**
     * Diese Methode sucht nach der PlayerMenuUtility von dem Spieler
     * und gibt sie zurück falls gefunden sonst erstellt sie eine neue!
     *
     * @param player der Spieler
     * @return Gibt die PlayerMenuUtility zurück
     */
    PlayerMenuUtility getPlayerMenuUtility(Player player);

    /**
     * Diese Methode nimmt ein Head Texture String entgegen und setzt diesen als Texture ein
     * und baut davon einen ItemStack!
     * Köpfe können auf https://minecraft-heads.com/ gefunden werden,
     * Dort muss man sich das Value kopieren und als der headTexture Parameter einfügen!
     * Dieser ist mit Base64 Encoded und muss nicht mehr Decoded werden
     * Da er so von minecraft verwendet wird!
     *
     * @param headTexture Das Value der Texture
     * @param name        Der Name des Items
     * @return Gibt einen Kopf zurück
     */
    ItemStack createSkull(String headTexture, String name);

    /**
     * Diese Methode nimmt ein Head Objekt entgegen nimmt sich daraus die Full Texture
     * und baut davon einen ItemStack mit der Kopf Texture
     *
     * @param headTexture Der Kopf den man möchte
     * @param name        Der Name des Items
     * @return Gibt einen Kopf zurück
     */
    ItemStack createSkull(Head headTexture, String name);

    // Plugin Manager

    /**
     * Lädt ein Plugin (wenn gefunden)
     * Der Parameter pluginName ist der Name des Plugins
     * Und dieses Plugin wird dann geladen!
     * Aber Nicht aktiviert!!!
     *
     * @param pluginName der Plugin Name
     * @throws InvalidPluginException      Wenn das Plugin kein Plugin ist
     * @throws InvalidDescriptionException Wenn das Plugin keine plugin.yml hat
     */
    void loadPlugin(File pluginName) throws InvalidPluginException, InvalidDescriptionException;

    /**
     * Reloaded ein Plugin (wenn gefunden)
     * Der Parameter plugin ist das Plugin
     * Und dieses Plugin wird dann geladen!
     *
     * @param plugin das Plugin
     */
    void reloadPlugin(Plugin plugin);


    /**
     * Reloaded ein Plugin (wenn gefunden)
     * Der Parameter pluginName ist der Name des Plugins
     * Und dieses Plugin wird dann geladen!
     *
     * @param pluginName der Plugin Name
     */
    void reloadPlugin(String pluginName);

    /**
     * Entlädt ein Plugin komplett
     * Alle Commands werden entladen,
     * Alle Listener werden entladen,
     * Und es wird komplett aus der Liste der Bukkit Plugins gelöscht
     * Und wird erst wieder da sein nach einem Reload/Restart
     *
     * @param plugin Das Plugin
     */
    void unloadPlugin(Plugin plugin);

    /**
     * Entlädt ein Plugin komplett
     * Alle Commands werden entladen,
     * Alle Listener werden entladen,
     * Und es wird komplett aus der Liste der Bukkit Plugins gelöscht
     * Und wird erst wieder da sein nach einem Reload/Restart
     *
     * @param pluginName Der Plugin Name
     */
    void unloadPlugin(String pluginName);

    /**
     * Gibt eine Liste aller Plugin Name zurück
     * (Nur wenn sie geladen sind)
     *
     * @param fullName Gibt ob es der vollständige Name sein soll oder nicht
     * @return die Plugin Namen
     */
    List<String> getPluginNames(boolean fullName);

    /**
     * Gibt eine Liste aller Plugins zurück
     * (Nur wenn sie geladen sind)
     *
     * @return Eine Liste mit Plugins
     */
    List<Plugin> getAllPlugins();

    /**
     * Gibt ein Plugin vom Namen zurück
     *
     * @param pluginName Der Name
     * @return das Plugin
     * @throws NullPointerException wenn das Plugin null ist
     */
    Plugin getPluginByName(String pluginName) throws NullPointerException;

    /**
     * Diese Methode downloaded ein Plugin von Spigot/Spiget!
     * <p>
     * Infos:
     * Die {@code resourceID} bekommt man dadurch das man auf Spigot
     * in der URL hinter dem Namen des Plugins die Zahl kopiert!
     * Der {@code fileName} ist der Name mit die Datei erstellt werden soll!
     * Und {@code enableAfterStart} macht, dass das Plugin entweder
     * Automatisch aktiviert werden soll oder nicht!
     *
     * @param resourceID         Resource Id des Plugins
     * @param fileName           Datei Name
     * @param enableAfterInstall Automatic Start
     * @return Das heruntergeladene Plugin
     * @throws IOException                 Wenn irgendwas mit der Website nicht geht
     * @throws InvalidPluginException      Wenn das Plugin kein Plugin ist
     * @throws InvalidDescriptionException Wenn das Plugin keine plugin.yml hat
     * @throws UnknownDependencyException  Wenn die Dependencies des Plugins fehlen
     */
    Plugin downloadPluginFromSpiget(int resourceID, String fileName, Boolean enableAfterInstall) throws IOException, InvalidPluginException, InvalidDescriptionException, UnknownDependencyException;

    //void downloadPluginUpdateFromSpiget(int resourceID, String fileName, boolean replaceOldVersion);

    // Utils

    /**
     * Cleart den Chat
     *
     * @param lines              Linien die gecleart werden sollen
     * @param broadcastChatClear Ob geBroadcastet werden soll bei Chat Clear
     * @param player             Der Spieler der dann genannt wird wenn {@code broadcastChatClear}
     */
    void clearChat(int lines, boolean broadcastChatClear, Player player);

    /**
     * Restartet den Server
     *
     * @param time Zeit zwischen den Aktionen und Nachrichten
     * @param time2 Zeit zwischen dem Restart und Player Kick
     * @throws InterruptedException Wenn der Restart unterbrochen wird
     */
    void restartServer(int time, int time2) throws InterruptedException;

    /**
     * Stoppt den Server
     *
     * @param time  Zeit zwischen den Aktionen und Nachrichten
     * @param time2 Zeit bevor Spieler gekickt werden und der Server gestoppt wird
     * @throws InterruptedException Wenn es unterbrochen wird
     */
    void stopServer(int time, int time2) throws InterruptedException;

    /**
     * Sends A formatted Report Message to my Discord via a Webhook
     * @param playerUUID The Player that reported
     * @param reportMessage The Report Message with no format
     * @return If successful
     */
    int reportBugToDiscord(UUID playerUUID, String reportMessage);

    /**
     * Sends a formatted Report message with the normal reportBugToDiscord Method
     * but first turns the Array into a String
     * @param playerUUID The Player that reported
     * @param reportMessageArray The Report Message Array with no format
     * @return If successful
     */
    int reportBugToDiscord(UUID playerUUID, String[] reportMessageArray);

    /**
     * Sends a formatted Report message with the normal reportBugToDiscord Method
     * but first turns the ArrayList into a String
     * @param playerUUID The Player that reported
     * @param reportMessageArrayList The Report Message Array List with no format
     * @return If successful
     */
    int reportBugToDiscord(UUID playerUUID, List<String> reportMessageArrayList);

    // Language System

    /**
     * Registriert eine Sprache im Plugin
     *
     * @param languageFile Der Language File mit den Einträgen drinnen
     * @param languageName Der Sprachname unter der die Sprache registriert werden soll
     */
    void addLanguage(LanguageFile languageFile, String languageName);

    /**
     * Gibt eine Registrierte Sprache zurück
     * wenn gefunden
     *
     * @param name Der Name Der Sprache
     * @param throwException wenn true wird eine Exception geworfen wenn die Sprache null ist
     * @return Die Sprache wenn vorhanden
     * @throws NullPointerException Wenn die Sprache nicht gefunden wurde
     */
    LanguageFile getLanguage(String name, boolean throwException) throws NullPointerException;

    /**
     * Gibt die Liste aller registrierten Sprachen zurück
     *
     * @return Liste aller registrierten Sprachen
     */
    Map<String, LanguageFile> getRegisteredLanguages();

    /**
     * Entfernt eine Registrierte Sprache
     * aus der Liste
     *
     * @param languageFile der Name der Sprache
     */
    void removeLanguage(String languageFile);

    /**
     * Setzt die Sprache die von dem System benutzt wird!
     *
     * @param languageFile Der Language File
     * @throws NullPointerException Wenn die Sprache {@code null} ist
     */
    void setCurrentLanguage(LanguageFile languageFile) throws NullPointerException;

    /**
     * Gibt dir eine Nachricht mit Placeholders zurück,
     * wenn gefunden aus der Sprache die gerade eingestellt ist!
     *
     * @param path   Der Pfad zu der Nachricht in der Config
     * @param player Der Spieler für die Placeholders
     * @param resetAfter setzt die Placeholder zurück nach dem formattieren
     * @return Die Nachricht (wenn gefunden) mit Placeholders
     */
    String getMessage(String path, Player player, boolean resetAfter);

    /**
     * Gibt dir eine Nachricht mit Placeholders zurück,
     * wenn gefunden aus der Sprache die angegeben ist (sie muss im System vorhanden sein, also erst {@code addLanguage()})!
     *
     * @param path     Der Pfad zu der Nachricht in der Config
     * @param player   Der Spieler für die Placeholders
     * @param langName Der Sprachen Name falls gewollt!
     * @param resetAfter setzt die Placeholder zurück nach dem formattieren
     * @return Die Nachricht (wenn gefunden) mit Placeholders
     */
    String getMessage(String path, Player player, String langName, boolean resetAfter);

    /**
     * Mit dieser Methode fügt man neue Placeholders einzeln temporär hinzu
     * sobald eine Methode (getMessage) resetAfter auf true hat wird die gesamte
     * Liste der Placeholder zurückgesetzt
     * @param type der Typ des Placeholders
     * @param key der Placeholder (z.B.: %target%)
     * @param value der Wert des Placeholder
     * @param resetBefore ob davor die anderen zurückgesetzt werden
     */
    void addPlaceholder(PlaceholderType type, String key, Object value, boolean resetBefore);

    /**
     * Mit dieser Methode fügt man neue Placeholders in einer Map temporär hinzu
     * sobald eine Methode (getMessage) resetAfter auf true hat wird die gesamte
     * Liste der Placeholder zurückgesetzt
     * @param placeholders die Liste der Placeholders
     * @param resetBefore ob davor die anderen zurückgesetzt werden
     */
    void addPlaceholders(Map<String, Placeholder> placeholders, boolean resetBefore);

    /**
     * Diese Methode entfernt einen bestimmten Placeholder
     * @param type der Typ des Placeholders
     * @param key der Placeholder (z.B.: %target%)
     */
    void removePlaceholder(PlaceholderType type, String key);

    /**
     * Diese Methode entfernt eine liste bestimmter Placeholder
     * @param type der Typ der Placeholders
     * @param keys die Liste der Placeholder Keys
     */
    void removePlaceholders(PlaceholderType type, List<String> keys);

    /**
     * Diese Methode setzt die gesamte Liste der Placeholders
     * zurück und löscht sie alle!
     * @param type der Typ des Placeholders
     * @param excludeKeys die Liste aller Keys die nicht resettet werden sollen
     */
    void resetPlaceholders(PlaceholderType type, @Nullable List<String> excludeKeys);

    /**
     * Diese Methode setzt die gesamte Liste der Placeholders
     * zurück und löscht sie alle!
     * @param type der Typ des Placeholders
     * @param includeKeys die Liste aller Keys die resettet werden sollen
     */
    void resetSpecificPlaceholders(PlaceholderType type, @Nullable List<String> includeKeys);

    /**
     * Gibt die Liste der aktiven Placeholder zurück
     * @return die Liste der aktiven Placeholder
     */
    Map<String, Placeholder> getPlaceholders();

    /**
     * Ersetzt alle aktiven Placeholder, wenn vorhanden, in einer Nachricht
     * @param type der Typ des Placeholders
     * @param message die Nachricht
     * @return die formattierte Nachricht
     */
    String replacePlaceholders(PlaceholderType type, String message);

    /**
     * Ersetzt alle Placeholder aus der angegebenen Liste, wenn vorhanden, in der Nachricht
     * @param message die Nachricht
     * @param placeholders die Placeholder Liste
     * @return die formattierte Nachricht
     */
    String replacePlaceholders(String message, Map<String, Placeholder> placeholders);

    /**
     * Gibt eine neue Liste im Placeholder Listen Format zurück
     * @return die Placeholder Liste
     */
    Map<String, Placeholder> getNewPlaceholderMap();

    /**
     * Gibt dir ein Item mit Placeholders im Namen und Lore zurück,
     * wenn gefunden aus der Sprache die gerade eingestellt ist!
     *
     * @param resetAfter Ob die Item Placeholder nach der Methode gecleart werden sollen
     * @param path   Der Pfad zum Item in der Config
     * @param player Der Spieler für die Placeholders
     * @return Das Item (wenn gefunden) mit Placeholders im Namen und Lore
     */
    ItemStack getItem(String path, Player player, boolean resetAfter);

    /**
     * Gibt dir ein Item mit Placeholders im Namen und Lore zurück,
     * wenn gefunden aus der Sprache die angegeben ist (sie muss im System vorhanden sein, also erst {@code addLanguage()})!
     *
     * @param resetAfter Ob die Item Placeholder nach der Methode gecleart werden sollen
     * @param path     Der Pfad zum Item in der Config
     * @param player   Der Spieler für die Placeholders
     * @param langName Der Sprachen Name falls gewollt!
     * @return Das Item (wenn gefunden) mit Placeholders im Namen und Lore
     */
    ItemStack getItem(String path, Player player, String langName, boolean resetAfter);

    /**
     * Gibt dir ein Item mit Placeholders im Namen und Lore zurück,
     * wenn gefunden aus der Sprache die gerade eingestellt ist!
     *
     * @param path   Der Pfad zum Titel in der Config
     * @param player Der Spieler für die Placeholders
     * @return Den Menu Title mit Placeholders
     */
    String getMenuTitle(String path, Player player);

    /**
     * Gibt dir einen Menu Title mit Placeholders zurück,
     * wenn gefunden aus der Sprache die angegeben ist (sie muss im System vorhanden sein, also erst {@code addLanguage()})!
     *
     * @param path     Der Pfad zum Titel in der Config
     * @param player   Der Spieler für die Placeholders
     * @param langName Der Sprachen Name falls gewollt
     * @return Den Menu Title mit Placeholders
     */
    String getMenuTitle(String path, Player player, String langName);

    /**
     * Gibt dir ein Item mit Placeholders im Namen und Lore zurück,
     * wenn gefunden aus der Sprache die gerade eingestellt ist!
     *
     * @param path   Der Pfad zum Objekt in der Config
     * @param defaultValue Der Default Wert
     * @param player Der Spieler für die Placeholders
     * @return Das Objekt, falls es ein String ist mit Placeholders
     */
    <T> T getCustomObject(String path, Player player, T defaultValue, boolean resetAfter);

    /**
     * Gibt dir einen Menu Title mit Placeholders zurück,
     * wenn gefunden aus der Sprache die angegeben ist (sie muss im System vorhanden sein, also erst {@code addLanguage()})!
     *
     * @param path     Der Pfad zum Objekt in der Config
     * @param player   Der Spieler für die Placeholders
     * @param defaultValue Der Default Wert
     * @param langName Der Sprachen Name falls gewollt
     * @param resetAfter Ob die Custom Placeholder nachher resettet werden sollen
     * @return Das Objekt, falls es ein String ist mit Placeholders
     */
    <T> T getCustomObject(String path, Player player, String langName, T defaultValue, boolean resetAfter);

    /**
     * Reloaded die Config
     * (Sprach Files, Config)
     *
     * @param messageReceiver Der Spieler der Nachrichten erhält
     *                        Der Speiler darf nicht null sein!
     */
    void reloadConfigurationFiles(CommandSender messageReceiver);

    // Events

    /**
     * Ein Admin Panel Event aufrufen!
     *
     * @param event Das Event das aufgerufen werden soll
     * @return Das Event
     * @throws NotAPanelEventException Wenn das Event nicht AdminPanelEvent extended
     */
    AdminPanelEvent callAdminPanelEvent(Event event) throws NotAPanelEventException;

    // Command Managers

    /**
     * Registriert einen neuen Command Manager
     * Weitere Infos im Wiki unter Command Manager API
     * @param commandManager der Command Manager
     */
    boolean registerCommandManager(CommandManager commandManager);


    /**
     * Returns the Cooldown Time Map
     * @return The Cooldown Time Map
     */
    public Map<UUID, Long> getCooldownTimeMap();
}
