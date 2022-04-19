package de.happybavarian07.main;

import de.happybavarian07.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;

public class LanguageManager {

    private final JavaPlugin plugin;
    private final File langFolder;
    private final Map<String, LanguageFile> registeredLanguages;
    private final Map<PlaceholderType, Map<String, Object>> placeholders;
    private String currentLangName;
    private LanguageFile currentLang;

    public LanguageManager(JavaPlugin plugin, File langFolder) {
        this.plugin = plugin;
        this.langFolder = langFolder;
        this.registeredLanguages = new LinkedHashMap<>();
        this.placeholders = new LinkedHashMap<>();
        placeholders.put(PlaceholderType.MESSAGE, new HashMap<>());
        placeholders.put(PlaceholderType.ITEM, new HashMap<>());
        placeholders.put(PlaceholderType.MENUTITLE, new HashMap<>());
    }

    public File getLangFolder() {
        return langFolder;
    }

    public Map<String, LanguageFile> getRegisteredLanguages() {
        return registeredLanguages;
    }

    public String getCurrentLangName() {
        return currentLangName;
    }

    public LanguageFile getCurrentLang() {
        return currentLang;
    }

    public void setCurrentLang(LanguageFile currentLang, boolean log) throws NullPointerException {
        if (currentLang == null) {
            List<Map.Entry<String, LanguageFile>> list = new ArrayList<>(registeredLanguages.entrySet());
            Map.Entry<String, LanguageFile> firstInsertedEntry = list.get(0);
            this.currentLang = firstInsertedEntry.getValue();
            this.currentLangName = firstInsertedEntry.getValue().getLangName();
            throw new NullPointerException("Language not found!");
        } else {
            this.currentLangName = currentLang.getLangName();
            this.currentLang = currentLang;
        }
        if (log)
            plugin.getLogger().log(Level.INFO, "Current Language: " + currentLangName);
    }

    public void addLanguagesToList(boolean log) {
        File[] fileArray = langFolder.listFiles();
        if (fileArray != null) {
            for (File file : fileArray) {
                LanguageFile languageFile = new LanguageFile(plugin, file.getName().replace(".yml", ""));
                if (!registeredLanguages.containsValue(languageFile))
                    this.registeredLanguages.put(languageFile.getLangName(), languageFile);
                if (log)
                    plugin.getLogger().log(Level.INFO, "Language: " + languageFile.getLangFile() + " successfully registered!");
            }
        }
    }

    public void updateLangFiles() {
        /*for (LanguageFile langFiles : getRegisteredLanguages().values()) {
            try {
                String resourceName = "languages/" + langFiles.getLangFile().getName();
                if (plugin.getResource(resourceName) == null) {
                    if (plugin.getResource("languages/" + plugin.getConfig().getString("Plugin.languageForUpdates") + ".yml") != null) {
                        resourceName = "languages/" + plugin.getConfig().getString("Plugin.languageForUpdates") + ".yml";
                    } else {
                        resourceName = "languages/en.yml";
                    }
                }
                ConfigUpdater.update(plugin, resourceName, langFiles.getLangFile(), Arrays.asList("Items.PlayerManager.TrollMenu.VillagerSounds.true.Options"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        for (LanguageFile langFiles : getRegisteredLanguages().values()) {
            String resourceName = "languages/" + langFiles.getLangFile().getName();
            boolean nonDefaultLang = false;
            if (plugin.getResource(resourceName) == null) {
                if (plugin.getResource("languages/" + plugin.getConfig().getString("Plugin.languageForUpdates") + ".yml") != null) {
                    resourceName = "languages/" + plugin.getConfig().getString("Plugin.languageForUpdates") + ".yml";
                } else {
                    resourceName = "languages/en.yml";
                }
                nonDefaultLang = true;
            }
            File oldFile = langFiles.getLangFile();
            File newFile = new File(langFiles.getLangFile().getParentFile().getPath() + "/" + langFiles.getLangName() + "-new.yml");
            YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(newFile);
            InputStream defaultStream;
            defaultStream = plugin.getResource(resourceName);
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
                newConfig.setDefaults(defaultConfig);
                newConfig.options().header(defaultConfig.options().header());
            }
            newConfig.options().copyDefaults(true);
            try {
                newConfig.save(newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ((AdminPanelMain) plugin).getLangFileUpdater().updateFile(oldFile, newConfig, langFiles.getLangName(), nonDefaultLang);
            newFile.delete();
        }
    }

    public void reloadLanguages(Player messageReceiver, Boolean log) {
        addLanguagesToList(log);
        updateLangFiles();
        for (String langFiles : registeredLanguages.keySet()) {
            getLang(langFiles).getLangConfig().reloadConfig();
            if (messageReceiver != null) {
                addPlaceholder(PlaceholderType.MESSAGE, "%language%", getLang(langFiles).getLangFile(), true);
                messageReceiver.sendMessage(getMessage("Player.General.ReloadedLanguageFile", messageReceiver, true));
            }
        }
        setCurrentLang(getLang(plugin.getConfig().getString("Plugin.language")), log);
    }

    public void addLang(LanguageFile langFile, String langName) {
        if (registeredLanguages.containsKey(langName))
            return;
        registeredLanguages.put(langName, langFile);
        plugin.getLogger().log(Level.INFO, "Language: " + langFile.getLangFile() + " successfully registered!");
    }

    public LanguageFile getLang(String langName) throws NullPointerException {
        if (!registeredLanguages.containsKey(langName))
            throw new NullPointerException("Language: " + langName + " not found!");
        return registeredLanguages.get(langName);
    }

    public void removeLang(String langName) {
        if (!registeredLanguages.containsKey(langName))
            return;
        registeredLanguages.remove(langName);
    }

    public void addPlaceholder(PlaceholderType type, String key, Object value, boolean resetBefore) {
        if (resetBefore) resetPlaceholders(type);
        if(!placeholders.containsKey(type)) placeholders.put(type, new HashMap<>());
        if (!placeholders.get(type).containsKey(key))
            placeholders.get(type).put(key, value);
    }

    public void addPlaceholders(Map<PlaceholderType, Map<String, Object>> placeholders, boolean resetBefore) {
        if (resetBefore) placeholders.clear();
        this.placeholders.putAll(placeholders);
    }

    public void removePlaceholder(PlaceholderType type, String key) {
        placeholders.get(type).remove(key);
    }

    public void removePlaceholders(PlaceholderType type, List<String> keys) {
        for (String key : keys) {
            this.placeholders.get(type).remove(key);
        }
    }

    public void resetPlaceholders(PlaceholderType type) {
        if(!placeholders.containsKey(type)) placeholders.put(type, new HashMap<>());
        placeholders.get(type).clear();
    }

    public Map<PlaceholderType, Map<String, Object>> getPlaceholders() {
        return placeholders;
    }

    public String getMessage(String path, Player player, boolean resetAfter) {
        LanguageFile langFile = getCurrentLang();
        LanguageConfig langConfig = langFile.getLangConfig();
        if (langConfig == null || langConfig.getConfig() == null)
            return "null config";
        if (langConfig.getConfig().getString("Messages." + path) == null || !langConfig.getConfig().contains("Messages." + path))
            return "null path: Messages." + path;

        String message = Utils.format(player, langConfig.getConfig().getString("Messages." + path), AdminPanelMain.getPrefix());
        if (placeholders.isEmpty()) return message;

        message = replacePlaceholders(PlaceholderType.MESSAGE, message);
        if (resetAfter) resetPlaceholders(PlaceholderType.MESSAGE);
        return message;
    }

    public String replacePlaceholders(PlaceholderType type, String message) {
        for (String key : placeholders.get(type).keySet()) {
            message = message.replace(key, placeholders.get(type).get(key).toString());
        }
        return message;
    }

    public String replacePlaceholders(String message, Map<String, Object> placeholders) {
        for (String key : placeholders.keySet()) {
            message = message.replace(key, placeholders.get(key).toString());
        }
        return message;
    }

    public Map<String, Object> getNewPlaceholderMap() {
        return new HashMap<>();
    }

    public String getMessage(String path, Player player, String langName, boolean resetAfter) {
        LanguageFile langFile = getLang(langName);
        LanguageConfig langConfig = langFile.getLangConfig();
        if (langConfig == null || langConfig.getConfig() == null)
            return "null config";
        if (langConfig.getConfig().getString("Messages." + path) == null || !langConfig.getConfig().contains("Messages." + path))
            return "null path: Messages." + path;

        String message = Utils.format(player, langConfig.getConfig().getString("Messages." + path), AdminPanelMain.getPrefix());
        if (placeholders.isEmpty()) return message;

        message = replacePlaceholders(PlaceholderType.MESSAGE, message);
        if (resetAfter) resetPlaceholders(PlaceholderType.MESSAGE);
        return message;
    }

    public String getPermissionMessage(Player player, String permission) {
        addPlaceholder(PlaceholderType.MESSAGE, "%permission%", permission, true);
        return getMessage("Player.General.NoPermissions", player, true);
    }

    public ItemStack getItem(String path, Player player, boolean resetAfter) {
        LanguageFile langFile = getCurrentLang();
        LanguageConfig langConfig = langFile.getLangConfig();
        ItemStack error = new ItemStack(Material.BARRIER);
        ItemMeta errorMeta = error.getItemMeta();
        if (langConfig == null || langConfig.getConfig() == null) {
            assert errorMeta != null;
            errorMeta.setDisplayName("Language Config not found!");
            errorMeta.setLore(Arrays.asList("If this happens often,", "please report to the Discord"));
            error.setItemMeta(errorMeta);
            return error;
        }
        if (langConfig.getConfig().getString("Items." + path) == null || !langConfig.getConfig().contains("Items." + path)) {
            assert errorMeta != null;
            errorMeta.setDisplayName("Config Path not found!");
            errorMeta.setLore(Arrays.asList("If this happens often,", "please report to the Discord", "Path: Items." + path));
            error.setItemMeta(errorMeta);
            return error;
        }
        if (langConfig.getConfig().getBoolean("Items." + path + ".disabled", false) &&
                !Objects.equals(path, "General.DisabledItem")) {
            return this.getItem("General.DisabledItem", player, false);
        }
        ItemStack item;
        Material material = Material.matchMaterial(langConfig.getConfig().getString("Items." + path + ".material"));
        if (material == null) {
            assert errorMeta != null;
            errorMeta.setDisplayName("Material not found! (" + langConfig.getConfig().getString("Items." + path + ".material") + ")");
            errorMeta.setLore(Arrays.asList("If this happens,", "please change the Material from this Item", "to something existing", "Path: Items." + path + ".material"));
            error.setItemMeta(errorMeta);
            return error;
        }
        String displayName = langConfig.getConfig().getString("Items." + path + ".displayName");
        List<String> lore = langConfig.getConfig().getStringList("Items." + path + ".lore");
        List<String> loreWithPlaceholders = new ArrayList<>();
        item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        //System.out.println("Placeholders: " + placeholders);
        //System.out.println("Lore: " + lore);
        for (String s : lore) {
            String temp = replacePlaceholders(PlaceholderType.ITEM, s);
            loreWithPlaceholders.add(Utils.format(player, temp, AdminPanelMain.getPrefix()));
        }
        //System.out.println("Lore with Placeholders: " + loreWithPlaceholders);
        assert meta != null;
        meta.setLore(loreWithPlaceholders);
        assert displayName != null;
        meta.setDisplayName(replacePlaceholders(PlaceholderType.ITEM, Utils.format(player, displayName, AdminPanelMain.getPrefix())));
        if (langConfig.getConfig().getBoolean("Items." + path + ".enchanted", false)) {
            meta.addEnchant(Enchantment.DURABILITY, 0, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        if (resetAfter) resetPlaceholders(PlaceholderType.ITEM);
        return item;
    }

    public ItemStack getItem(String path, Player player, String langName, boolean resetAfter) {
        LanguageFile langFile = getLang(langName);
        LanguageConfig langConfig = langFile.getLangConfig();
        ItemStack error = new ItemStack(Material.BARRIER);
        ItemMeta errorMeta = error.getItemMeta();
        if (langConfig == null || langConfig.getConfig() == null) {
            assert errorMeta != null;
            errorMeta.setDisplayName("Language Config not found!");
            errorMeta.setLore(Arrays.asList("If this happens often,", "please report to the Discord"));
            error.setItemMeta(errorMeta);
            return error;
        }
        if (langConfig.getConfig().getString("Items." + path) == null || !langConfig.getConfig().contains("Items." + path)) {
            assert errorMeta != null;
            errorMeta.setDisplayName("Config Path not found!");
            errorMeta.setLore(Arrays.asList("If this happens often,", "please report to the Discord", "Path: Items." + path));
            error.setItemMeta(errorMeta);
            return error;
        }
        if (langConfig.getConfig().getBoolean("Items." + path + ".disabled", false) &&
                !Objects.equals(path, "General.DisabledItem")) {
            return this.getItem("General.DisabledItem", player, false);
        }
        ItemStack item;
        Material material = Material.matchMaterial(langConfig.getConfig().getString("Items." + path + ".material"));
        if (material == null) {
            assert errorMeta != null;
            errorMeta.setDisplayName("Material not found! (" + langConfig.getConfig().getString("Items." + path + ".material") + ")");
            errorMeta.setLore(Arrays.asList("If this happens,", "please change the Material from this Item", "to something existing", "Path: Items." + path + ".material"));
            error.setItemMeta(errorMeta);
            return error;
        }
        String displayName = langConfig.getConfig().getString("Items." + path + ".displayName");
        List<String> lore = langConfig.getConfig().getStringList("Items." + path + ".lore");
        List<String> loreWithPlaceholders = new ArrayList<>();
        item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        for (String s : lore) {
            String temp = replacePlaceholders(PlaceholderType.ITEM, s);
            loreWithPlaceholders.add(Utils.format(player, temp, AdminPanelMain.getPrefix()));
        }
        assert meta != null;
        meta.setLore(loreWithPlaceholders);
        assert displayName != null;
        meta.setDisplayName(replacePlaceholders(PlaceholderType.ITEM, Utils.format(player, displayName, AdminPanelMain.getPrefix())));
        if (langConfig.getConfig().getBoolean("Items." + path + ".enchanted", false)) {
            meta.addEnchant(Enchantment.DURABILITY, 0, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        if (resetAfter) resetPlaceholders(PlaceholderType.ITEM);
        return item;
    }

    public String getMenuTitle(String path, Player player, String langName) {
        LanguageFile langFile = getLang(langName);
        LanguageConfig langConfig = langFile.getLangConfig();
        if (langConfig == null || langConfig.getConfig() == null)
            return "null config";
        if (langConfig.getConfig().getString("MenuTitles." + path) == null || !langConfig.getConfig().contains("MenuTitles." + path))
            return "null path: MenuTitles." + path;
        resetPlaceholders(PlaceholderType.MENUTITLE);
        return Utils.format(player, replacePlaceholders(PlaceholderType.MENUTITLE, Objects.requireNonNull(langConfig.getConfig().getString("MenuTitles." + path))), AdminPanelMain.getPrefix());
    }

    public String getMenuTitle(String path, Player player) {
        LanguageFile langFile = getCurrentLang();
        LanguageConfig langConfig = langFile.getLangConfig();
        if (langConfig == null || langConfig.getConfig() == null)
            return "null config";
        if (langConfig.getConfig().getString("MenuTitles." + path) == null || !langConfig.getConfig().contains("MenuTitles." + path))
            return "null path: MenuTitles." + path;
        resetPlaceholders(PlaceholderType.MENUTITLE);
        return Utils.format(player, replacePlaceholders(PlaceholderType.MENUTITLE, Objects.requireNonNull(langConfig.getConfig().getString("MenuTitles." + path))), AdminPanelMain.getPrefix());
    }
}
