package de.happybavarian07.main;

import de.happybavarian07.utils.Utils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class LanguageManager {

    private final JavaPlugin plugin;
    private final File langFolder;
    private final Map<String, LanguageFile> registeredLanguages;
    private String currentLangName;
    private LanguageFile currentLang;

    public LanguageManager(JavaPlugin plugin, File langFolder) {
        this.plugin = plugin;
        this.langFolder = langFolder;
        this.registeredLanguages = new LinkedHashMap<>();
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

    public void reloadLanguages(Player messageReceiver, Boolean log) {
        addLanguagesToList(log);
        for (String langFiles : registeredLanguages.keySet()) {
            getLang(langFiles).getLangConfig().reloadConfig();
            if (messageReceiver != null)
                messageReceiver.sendMessage(getMessage("Player.General.ReloadedLanguageFile", messageReceiver)
                        .replace("%language%", "" + getLang(langFiles).getLangFile()));
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

    public String getMessage(String path, Player player) {
        LanguageFile langFile = getCurrentLang();
        LanguageConfig langConfig = langFile.getLangConfig();
        if (langConfig == null || langConfig.getConfig() == null)
            return "null config";
        if (langConfig.getConfig().getString("Messages." + path) == null || !langConfig.getConfig().contains("Messages." + path))
            return "null path: Messages." + path;

        return Utils.format(player, Objects.requireNonNull(langConfig.getConfig().getString("Messages." + path)), AdminPanelMain.getPrefix());
    }

    public String getMessage(String path, Player player, String langName) {
        LanguageFile langFile = getLang(langName);
        LanguageConfig langConfig = langFile.getLangConfig();
        if (langConfig == null || langConfig.getConfig() == null)
            return "null config";
        if (langConfig.getConfig().getString("Messages." + path) == null || !langConfig.getConfig().contains("Messages." + path))
            return "null path: Messages." + path;

        return Utils.format(player, Objects.requireNonNull(langConfig.getConfig().getString("Messages." + path)), AdminPanelMain.getPrefix());
    }

    public ItemStack getItem(String path, Player player) {
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
            loreWithPlaceholders.add(Utils.format(player, s, AdminPanelMain.getPrefix()));
        }
        assert meta != null;
        meta.setLore(loreWithPlaceholders);
        assert displayName != null;
        meta.setDisplayName(Utils.format(player, displayName, AdminPanelMain.getPrefix()));
        if (langConfig.getConfig().getBoolean("Items." + path + ".enchanted", false)) {
            meta.addEnchant(Enchantment.DURABILITY, 0, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getItem(String path, Player player, String langName) {
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
            loreWithPlaceholders.add(Utils.format(player, s, AdminPanelMain.getPrefix()));
        }
        assert meta != null;
        meta.setLore(loreWithPlaceholders);
        assert displayName != null;
        meta.setDisplayName(Utils.format(player, displayName, AdminPanelMain.getPrefix()));
        if (langConfig.getConfig().getBoolean("Items." + path + ".enchanted", false)) {
            meta.addEnchant(Enchantment.DURABILITY, 0, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    public String getMenuTitle(String path, Player player, String langName) {
        LanguageFile langFile = getLang(langName);
        LanguageConfig langConfig = langFile.getLangConfig();
        if (langConfig == null || langConfig.getConfig() == null)
            return "null config";
        if (langConfig.getConfig().getString("MenuTitles." + path) == null || !langConfig.getConfig().contains("MenuTitles." + path))
            return "null path: MenuTitles." + path;
        return Utils.format(player, Objects.requireNonNull(langConfig.getConfig().getString("MenuTitles." + path)), AdminPanelMain.getPrefix());
    }

    public String getMenuTitle(String path, Player player) {
        LanguageFile langFile = getCurrentLang();
        LanguageConfig langConfig = langFile.getLangConfig();
        if (langConfig == null || langConfig.getConfig() == null)
            return "null config";
        if (langConfig.getConfig().getString("MenuTitles." + path) == null || !langConfig.getConfig().contains("MenuTitles." + path))
            return "null path: MenuTitles." + path;
        return Utils.format(player, Objects.requireNonNull(langConfig.getConfig().getString("MenuTitles." + path)), AdminPanelMain.getPrefix());
    }
}
