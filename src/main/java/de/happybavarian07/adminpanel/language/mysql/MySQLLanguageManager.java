package de.happybavarian07.adminpanel.language.mysql;

import de.happybavarian07.adminpanel.language.LanguageFile;
import de.happybavarian07.adminpanel.language.LanguageManager;
import de.happybavarian07.adminpanel.language.Placeholder;
import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefix;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class MySQLLanguageManager extends LanguageManager {
    //private final JavaPlugin plugin;
    //private final File langFolder;
    //private final Map<String, LanguageFile> registeredLanguages;
    private final LanguageDatabaseController databaseController;

    //private LanguageFile currentLang;
    private final PerPlayerLanguageMySQLHandler plhandler;
    private final AdminPanelMain plugin;
    private final PlaceholderMySQLHandler placeholderMySQLHandler;

    public MySQLLanguageManager(AdminPanelMain plugin, File langFolder, String prefix) {
        super(plugin, langFolder, prefix);
        this.plugin = plugin;

        // Either get the properties file to the database or make a new one with the default values from embedded resources
        File propertiesFile = new File(plugin.getDataFolder(), "database.properties");
        if (!propertiesFile.exists()) {
            plugin.saveResource("database.properties", false);
        }
        // Make a Properties Instance and read which type it is
        Properties properties = Utils.getProperties(propertiesFile);
        // If the type is not set, set it to SQLite
        if (properties.getProperty("dbtype") == null) {
            properties.setProperty("dbtype", "sqlite");
            Utils.saveProperties(properties, propertiesFile);
        }

        String type = properties.getProperty("dbtype");
        if (type.equalsIgnoreCase("mysql") || type.equalsIgnoreCase("mariadb")) {
            this.databaseController = new LanguageDatabaseController(plugin, propertiesFile, true, type);
        } else if (type.equalsIgnoreCase("sqlite")) {
            // Read the SQLite file from Properties and create a new SQLite file if it doesn't exist
            File sqliteFile = new File(plugin.getDataFolder(), properties.getProperty("sqlite_path_language"));
            if (!sqliteFile.exists()) {
                sqliteFile.getParentFile().mkdirs();
                try {
                    sqliteFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.databaseController = new LanguageDatabaseController(plugin, sqliteFile, false, "sqlite");
        } else {
            File sqliteFile = new File(plugin.getDataFolder(), "language.db");
            if (!sqliteFile.exists()) {
                sqliteFile.getParentFile().mkdirs();
                try {
                    sqliteFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.databaseController = new LanguageDatabaseController(plugin, propertiesFile, false, "sqlite");
        }

        this.plhandler = new PerPlayerLanguageMySQLHandler(this, databaseController);
        this.placeholderMySQLHandler = new PlaceholderMySQLHandler(this, databaseController);
        plhandler.setupDatabaseTable();
        placeholderMySQLHandler.setupDatabaseTable();
    }

    @Override
    public PerPlayerLanguageMySQLHandler getPlhandler() {
        return plhandler;
    }

    public PlaceholderMySQLHandler getPlaceholderMySQLHandler() {
        return placeholderMySQLHandler;
    }

    public LanguageDatabaseController getDatabaseController() {
        return databaseController;
    }

    public Language getCurrentLanguage() {
        return databaseController.getCurrentLanguage();
    }

    /**
     * Sets the current language to the given language name
     *
     * @param languageShort The name of the language to set as the current language (can be only the short name rn)
     */
    public void setCurrentLanguage(String languageShort) {
        databaseController.setCurrentLanguage(languageShort);
    }

    @Override
    public void updateLangFiles() {
        databaseController.getInnerLanguageManager().getLanguageIDs().forEach(languageID -> {
            CompletableFuture<LanguageConverter> converterFuture = getLanguageConverterFuture(languageID);
            converterFuture.join(); // Wait for the CompletableFuture to complete
            CompletableFuture<Boolean> future = getContentFuture(languageID, converterFuture);
            handleFutureResult(future, languageID);
        });
    }

    private CompletableFuture<LanguageConverter> getLanguageConverterFuture(UUID languageID) {
        CompletableFuture<LanguageConverter> converterFuture = null;
        int attempts = 0;

        while (converterFuture == null && attempts < 10) { // Retry up to 10 times
            converterFuture = databaseController.getInnerLanguageManager().getConverterManager().getLanguageConverterFutures().get(languageID);
            attempts++;

            if (converterFuture == null) {
                try {
                    Thread.sleep(100); // Wait for 100 milliseconds before retrying
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (converterFuture == null) {
            throw new RuntimeException("Failed to get LanguageConverter for UUID: " + languageID);
        }

        converterFuture.join(); // Wait for the CompletableFuture to complete

        if (!converterFuture.isDone()) {
            throw new RuntimeException("LanguageConverter CompletableFuture is not done for UUID: " + languageID);
        }

        return converterFuture;
    }

    private CompletableFuture<Boolean> getContentFuture(UUID languageID, CompletableFuture<LanguageConverter> converterFuture) {
        if (databaseController.getInnerLanguageManager().getContentManager().getContentValue(languageID, "LanguageFullName") == null) {
            return databaseController.getInnerLanguageManager().getContentManager().addLanguageContentAsync(
                    languageID,
                    databaseController.getInnerLanguageManager().getConverterManager().getLanguageConverter(languageID).convertLanguageToDatabaseFormat(
                            true,
                            plugin.getResource("languages/" + databaseController.getInnerLanguageManager().getLanguageShortname(languageID) + ".yml"),
                            false,
                            null
                    )
            );
        } else {
            return databaseController.getInnerLanguageManager().getContentManager().updateLanguageContentAsync(
                    languageID,
                    databaseController.getInnerLanguageManager().getConverterManager().getLanguageConverter(languageID).convertLanguageToDatabaseFormat(
                            true,
                            plugin.getResource("languages/" + databaseController.getInnerLanguageManager().getLanguageShortname(languageID) + ".yml"),
                            false,
                            null
                    ),
                    false
            );
        }
    }

    private void handleFutureResult(CompletableFuture<Boolean> future, UUID languageID) {
        future.thenAccept(result -> {
            String action = databaseController.getInnerLanguageManager().getContentManager().getContentValue(languageID, "LanguageFullName") == null ? "added" : "updated";
            if (result) {
                plugin.getLogger().log(Level.INFO, "Language content " + action + " successfully.");
            } else {
                plugin.getLogger().log(Level.SEVERE, "Failed to " + action + " language content.");
            }
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Exception occurred while adding/updating language content.", ex);
            return null;
        });
    }

    @Override
    public LanguageFile getLangOrPlayerLang(boolean currentLang, String langName, @Nullable Player player) {
        return new LanguageFile(plugin, getLanguageOrPlayerLanguage(currentLang, langName, player).getLanguageShort(), false);
    }

    public Language getLanguageOrPlayerLanguage(boolean currentLang, String langName, @Nullable Player player) {
        if (player == null && currentLang) return getCurrentLanguage();
        if (player == null) return getLanguage(langName, true);
        if (langName == null && currentLang) return getCurrentLanguage();

        Language lang = plhandler.getPlayerLanguage(player.getUniqueId());
        if (lang == null) {
            if (currentLang) return getCurrentLanguage();
            else return getLanguage(langName, true);
        }
        return lang;
    }

    @Override
    public LanguageFile getLang(String langName, boolean throwException) throws NullPointerException {
        return new LanguageFile(plugin, getLanguage(langName, throwException).getLanguageShort(), false);
    }

    public Language getLanguage(String langName, boolean throwException) throws NullPointerException {
        Language language = databaseController.getInnerLanguageManager().getLanguage(databaseController.getInnerLanguageManager().getLanguageIDByShortname(langName));
        if (language == null && throwException) {
            throw new NullPointerException("Language " + langName + " not found!");
        }
        return language;
    }

    @Override
    public String getMessage(String path, Player player, boolean resetAfter) {
        return getMessage(path, player, getCurrentLanguage().getLanguageShort(), resetAfter);
    }

    @Override
    public String getMessage(String path, Player player, String langName, boolean resetAfter) {
        String databaseReturn = databaseController.getInnerLanguageManager().getContentManager().getContentValue(getLanguageOrPlayerLanguage(true, langName, player).getID(), path);
        if (databaseReturn == null)
            return "null config";
        if (databaseReturn.equals("-1"))
            return "null path: Messages." + path;

        String message = Utils.format(player, databaseReturn, getPrefix());
        if (getPlaceholders().isEmpty()) return message;

        List<String> includedKeys = new ArrayList<>(getPlaceholderKeysInMessage(message, PlaceholderType.MESSAGE));
        message = replacePlaceholders(PlaceholderType.MESSAGE, message);
        if (resetAfter) resetSpecificPlaceholders(PlaceholderType.MESSAGE, includedKeys);
        return message;
    }

    public ItemStack getItem(String path, Player player, boolean resetAfter) {
        return getItem(path, player, getCurrentLanguage().getLanguageShort(), resetAfter);
    }

    @Override
    public ItemStack getItem(String path, Player player, String langName, boolean resetAfter) {
        Map<String, Object> itemMapDatabaseReturn = databaseController.getInnerLanguageManager().getContentManager().getItemMapFromDatabase(getLanguageOrPlayerLanguage(true, langName, player).getID(), path);
        ItemStack error = new ItemStack(Material.BARRIER);
        ItemMeta errorMeta = error.getItemMeta();
        if (itemMapDatabaseReturn == null) {
            assert errorMeta != null;
            errorMeta.setDisplayName("Language Config not found!");
            errorMeta.setLore(Arrays.asList("If this happens often,", "please report to the Discord"));
            error.setItemMeta(errorMeta);
            return error;
        }
        if (itemMapDatabaseReturn.get("Items." + path + ".material") == null || !itemMapDatabaseReturn.containsKey("Items." + path + ".material")) {
            assert errorMeta != null;
            errorMeta.setDisplayName("Config Path not found!");
            errorMeta.setLore(Arrays.asList("If this happens often,", "please report to the Discord", "Path: Items." + path));
            error.setItemMeta(errorMeta);
            return error;
        }
        if (Boolean.parseBoolean((String) itemMapDatabaseReturn.get("Items." + path + ".disabled")) &&
                !Objects.equals(path, "General.DisabledItem")) {
            return this.getItem("General.DisabledItem", player, false);
        }
        ItemStack item;
        Material material = Material.matchMaterial((String) itemMapDatabaseReturn.get("Items." + path + ".material"));
        if (material == null) {
            assert errorMeta != null;
            errorMeta.setDisplayName("Material not found! (" + itemMapDatabaseReturn.get("Items." + path + ".material") + ")");
            errorMeta.setLore(Arrays.asList("If this happens,", "please change the Material from this Item", "to something existing", "Path: Items." + path + ".material"));
            error.setItemMeta(errorMeta);
            return error;
        }
        String displayName = (String) itemMapDatabaseReturn.get("Items." + path + ".displayName");
        // Read lore from database map and turn the string into a list
        List<String> lore = (List<String>) itemMapDatabaseReturn.get("Items." + path + ".lore");
        List<String> loreWithPlaceholders = new ArrayList<>();
        List<String> includedKeys = new ArrayList<>();
        item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        for (String s : lore) {
            includedKeys.addAll(getPlaceholderKeysInMessage(s, PlaceholderType.ITEM));
            String temp = replacePlaceholders(PlaceholderType.ITEM, s);
            loreWithPlaceholders.add(Utils.format(player, temp, getPrefix()));
        }
        assert meta != null;
        meta.setLore(loreWithPlaceholders);
        assert displayName != null;
        includedKeys.addAll(getPlaceholderKeysInMessage(Utils.format(player, displayName, getPrefix()), PlaceholderType.ITEM));
        meta.setDisplayName(replacePlaceholders(PlaceholderType.ITEM, Utils.format(player, displayName, getPrefix())));
        if (Boolean.parseBoolean((String) itemMapDatabaseReturn.get("Items." + path + ".enchanted"))) {
            meta.addEnchant(Enchantment.DURABILITY, 0, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        if (resetAfter) resetSpecificPlaceholders(PlaceholderType.ITEM, includedKeys);
        return item;
    }

    @Override
    public String getMenuTitle(String path, Player player) {
        return getMenuTitle(path, player, getCurrentLanguage().getLanguageShort());
    }

    @Override
    public String getMenuTitle(String path, Player player, String langName) {
        String databaseReturn = databaseController.getInnerLanguageManager().getContentManager().getContentValue(getLanguageOrPlayerLanguage(true, langName, player).getID(), path);
        if (databaseReturn == null)
            return "null config";
        if (databaseReturn.equals("-1"))
            return "null path: Messages." + path;

        List<String> includedKeys = new ArrayList<>(getPlaceholderKeysInMessage(databaseReturn, PlaceholderType.MENUTITLE));
        databaseReturn = replacePlaceholders(PlaceholderType.MENUTITLE, databaseReturn);
        resetSpecificPlaceholders(PlaceholderType.MENUTITLE, includedKeys);
        return Utils.format(player, databaseReturn, getPrefix());
    }

    @Override
    public <T> T getCustomObject(String path, @Nullable Player player, T defaultValue, boolean resetAfter) {
        return getCustomObject(path, player, getCurrentLanguage().getLanguageShort(), defaultValue, resetAfter);
    }

    @Override
    public <T> T getCustomObject(String path, @Nullable Player player, String langName, T defaultValue, boolean resetAfter) {
        LanguageDatabaseController databaseController = getDatabaseController();
        if (databaseController == null)
            return defaultValue;
        UUID languageID = databaseController.getInnerLanguageManager().getLanguageIDByShortname(langName);
        if (languageID == null)
            return defaultValue;
        Object value = null;
        try {
            value = databaseController.getInnerLanguageManager().getContentManager().getCustomContentValue(languageID, path, defaultValue);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (value == null)
            return defaultValue;

        T obj;
        try {
            obj = (T) value;
            if (obj instanceof String) {
                obj = (T) replacePlaceholders(PlaceholderType.CUSTOM, Utils.format(player, obj.toString(), getPrefix()));
                if (resetAfter)
                    resetPlaceholders(PlaceholderType.CUSTOM, null);
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
            return defaultValue;
        }
        if (obj == null) obj = defaultValue;
        return obj;
    }

    @Override
    public LanguageFile getCurrentLang() {
        return new LanguageFile(plugin, getCurrentLangName(), true);
    }

    @Override
    public String getCurrentLangName() {
        return getCurrentLanguage().getLanguageShort();
    }

    @Override
    public void setCurrentLang(LanguageFile currentLang, boolean log) throws NullPointerException {
        if (currentLang == null) throw new NullPointerException("LanguageFile cannot be null!");
        this.setCurrentLanguage(currentLang.getLangName());
        if (log) {
            plugin.getFileLogger().writeToLog(Level.INFO, "Language " + currentLang.getFullName() + " set as the current language.", LogPrefix.ACTIONSLOGGER_PLUGIN);
        }
    }


    @Override
    public String getPrefix() {
        return databaseController.getPrefix();
    }

    @Override
    public void setPrefix(String prefix) {
        databaseController.setPrefix(prefix);
    }

    @Override
    public void addLang(LanguageFile langFile, String langName) {
        addLang(UUID.randomUUID(), langFile, langName, false);
    }

    public boolean addLang(UUID uuid, LanguageFile langFile, String langName, boolean autoUploadContent) {

        // Arguments for the addLanguage method: UUID languageID, String language, String languageName, String languageVersion, String languageDescription, String languageFile
        CompletableFuture<Boolean> future = databaseController.getInnerLanguageManager().addLanguage(
                uuid,
                langName,
                langFile.getFullName(),
                langFile.getFileVersion(),
                langFile.getLanguageDescription(),
                langFile.getLangFile().getName(),
                autoUploadContent);

        // Use a callback to handle the result
        future.thenAccept(result -> {
            if (result) {
                plugin.getLogger().log(Level.INFO, "Language: " + langFile.getLangFile() + " successfully registered!");
            } else {
                plugin.getLogger().severe("Failed to add language " + langName + " to the database!");
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to add language " + langName + " to the database.", LogPrefix.ACTIONSLOGGER_PLUGIN);
            }
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Exception occurred while adding language " + langName + " to the database!", ex);
            return null;
        });

        // Return true instantly
        return true;
    }

    @Override
    public void removeLang(String langName) {
        databaseController.getInnerLanguageManager().removeLanguage(databaseController.getInnerLanguageManager().getLanguageIDByShortname(langName));
        plugin.getFileLogger().writeToLog(Level.INFO, "Language " + langName + " removed from the database.", LogPrefix.ACTIONSLOGGER_PLUGIN);
    }

    @Override
    public Map<String, LanguageFile> getRegisteredLanguages() {
        Map<String, LanguageFile> registeredLanguages = new LinkedHashMap<>();
        databaseController.getInnerLanguageManager().getLanguageIDs().forEach(languageID -> {
            Language lang = databaseController.getInnerLanguageManager().getLanguage(languageID);
            registeredLanguages.put(lang.getLanguageShort(), new LanguageFile(plugin, lang.getLanguageShort(), false));
        });
        return registeredLanguages;
    }

    @Override
    public void reloadLanguages(CommandSender messageReceiver, Boolean log) {
        addLanguagesToList(log);
        // Get the Language Converter Futures and check if all of them are done and only then continue
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(databaseController.getInnerLanguageManager().getConverterManager().getLanguageConverterFutures().values().toArray(new CompletableFuture[0]));
        try {
            allFutures.get();
        } catch (InterruptedException | ExecutionException e) {
            plugin.getLogger().log(Level.SEVERE, "Exception occurred while waiting for all LanguageConverter Futures to complete!", e);
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Exception occurred while waiting for all LanguageConverter Futures to complete!", LogPrefix.ACTIONSLOGGER_PLUGIN);
        }
        while (!allFutures.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        updateLangFiles();
        if (log) {
            plugin.getFileLogger().writeToLog(Level.INFO, "Languages reloaded.", LogPrefix.ACTIONSLOGGER_PLUGIN);
        }
        for (Language lang : databaseController.getInnerLanguageManager().getLanguageMap().values()) {
            databaseController.getInnerLanguageManager().getConverterManager().getLanguageConverter(lang.getID()).getLanguageFile().getLangConfig().reloadConfig();
            if (messageReceiver != null) {
                addPlaceholder(PlaceholderType.MESSAGE, "%language%", lang.getLanguageFilePath(), true);
                messageReceiver.sendMessage(getMessage("Player.General.ReloadedLanguageFile", null, true));
            }
        }
        setCurrentLang(getLang(plugin.getConfig().getString("Plugin.language"), true), log);
    }

    @Override
    public void addLanguagesToList(boolean log) {
        File[] fileArray = getLangFolder().listFiles();
        if (fileArray != null) {
            for (File file : fileArray) {
                if (file.isFile() && file.getName().endsWith(".yml")) {
                    LanguageFile languageFile = new LanguageFile(plugin, file.getName().replace(".yml", ""), false);
                    UUID uuid = languageFile.getLangUUID();
                    if (databaseController.getInnerLanguageManager().getLanguageIDByShortname(languageFile.getLangName()) == null) {
                        boolean result = addLang(uuid, languageFile, languageFile.getLangName(), false);
                        if (log && !languageFile.getLangName().equals("default") && result)
                            plugin.getLogger().log(Level.INFO, "Language: " + languageFile.getLangFile() + " successfully registered!");
                    }
                }
            }
        }
    }

    @Override
    public String getPermissionMessage(Player player, String permission) {
        return getMessage("Player.General.NoPermission", player, true).replace("%permission%", permission);
    }

    @Override
    public void addPlaceholder(PlaceholderType type, String key, Object value, boolean resetBefore) {
        placeholderMySQLHandler.addPlaceholder(type, key, value, resetBefore);
    }

    @Override
    public void addPlaceholders(Map<String, Placeholder> placeholders, boolean resetBefore) {
        placeholderMySQLHandler.addPlaceholders(placeholders, resetBefore);
    }

    @Override
    public void removePlaceholder(PlaceholderType type, String key) {
        placeholderMySQLHandler.removePlaceholder(type, key);
    }

    @Override
    public void removePlaceholders(PlaceholderType type, List<String> keys) {
        placeholderMySQLHandler.removePlaceholders(type, keys);
    }

    @Override
    public void resetPlaceholders(PlaceholderType type, @Nullable List<String> excludeKeys) {
        placeholderMySQLHandler.resetPlaceholders(type, excludeKeys);
    }

    @Override
    public void resetSpecificPlaceholders(PlaceholderType type, @Nullable List<String> includeKeys) {
        placeholderMySQLHandler.resetSpecificPlaceholders(type, includeKeys);
    }

    @Override
    public Map<String, Placeholder> getPlaceholders() {
        return placeholderMySQLHandler.getPlaceholders();
    }

    public Map<String, Placeholder> getPlaceholdersByType(PlaceholderType type) {
        return placeholderMySQLHandler.getPlaceholdersByType(type);
    }

    @Override
    public List<String> getPlaceholderKeysInMessage(String message, PlaceholderType type) {
        return placeholderMySQLHandler.getPlaceholderKeysInMessage(message, type);
    }

    @Override
    public String replacePlaceholders(PlaceholderType type, String message) {
        return placeholderMySQLHandler.replacePlaceholders(type, message);
    }

    @Override
    public ItemStack replacePlaceholders(Player player, ItemStack item) {
        return placeholderMySQLHandler.replacePlaceholders(player, item);
    }

    @Override
    public ItemStack replacePlaceholders(Player player, ItemStack item, Map<String, Placeholder> placeholders) {
        return placeholderMySQLHandler.replacePlaceholders(player, item, placeholders);
    }

    @Override
    public String replacePlaceholders(String message, Map<String, Placeholder> placeholders) {
        return placeholderMySQLHandler.replacePlaceholders(message, placeholders);
    }

    @Override
    public Map<String, Placeholder> getNewPlaceholderMap() {
        return placeholderMySQLHandler.getNewPlaceholderMap();
    }
}
