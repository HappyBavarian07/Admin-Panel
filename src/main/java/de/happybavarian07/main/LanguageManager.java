package de.happybavarian07.main;

import de.happybavarian07.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class LanguageManager {

    private final Main plugin;
    private final File langFolder;
    private final Map<String, LanguageFile> registeredLanguages;
    private String currentLangName;
    private LanguageFile currentLang;

    public LanguageManager(Main plugin, File langFolder) {
        this.plugin = plugin;
        this.langFolder = langFolder;
        this.registeredLanguages = new LinkedHashMap<>();

        addLanguagesToList();
    }

    public void setCurrentLang(LanguageFile currentLang) throws NullPointerException {
        if(currentLang == null) {
            List<Map.Entry<String, LanguageFile>> list = new ArrayList<>(registeredLanguages.entrySet());
            Map.Entry<String, LanguageFile> firstInsertedEntry = list.get(0);
            this.currentLang = firstInsertedEntry.getValue();
            this.currentLangName = firstInsertedEntry.getValue().getLangName();
            throw new NullPointerException("Language not found!");
        } else {
            this.currentLangName = currentLang.getLangName();
            this.currentLang = currentLang;
        }
        System.out.println("Current Language: " + currentLangName);
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

    public void addLanguagesToList() {
        File[] fileArray = langFolder.listFiles();
        if (fileArray != null) {
            for(File file : fileArray) {
                LanguageFile languageFile = new LanguageFile(plugin, file.getName().replace(".yml", ""));
                this.registeredLanguages.put(languageFile.getLangName(), languageFile);
                System.out.println("Language: " + languageFile.getLangFile() + " successfully registered!");
            }
        }
    }

    public void addLang(LanguageFile langFile, String langName) {
        if(registeredLanguages.containsKey(langName))
            return;
        registeredLanguages.put(langName, langFile);
        System.out.println("Language: " + langFile.getLangFile() + " successfully registered!");
    }

    public LanguageFile getLang(String langName) throws NullPointerException {
        if(!registeredLanguages.containsKey(langName))
            throw new NullPointerException("Language: " + langName + " not found!");
        return registeredLanguages.get(langName);
    }

    public void removeLang(String langName) {
        if(!registeredLanguages.containsKey(langName))
            return;
        registeredLanguages.remove(langName);
    }

    public String getMessage(String path, Player player) {
        LanguageFile langFile = getCurrentLang();
        LanguageConfig langConfig = langFile.getLangConfig();
        if(langConfig == null || langConfig.getConfig() == null)
            return "null config";
        if(!langConfig.getConfig().contains("Messages." + path) || langConfig.getConfig().getString("Messages." + path) == null)
            return "null path: Messages." + path;

        return Utils.getInstance().replacePlaceHolders(player, langConfig.getConfig().getString("Messages." + path), Main.getPrefix());
    }

    public String getMessage(String path, Player player, String langName) {
        LanguageFile langFile = getLang(langName);
        LanguageConfig langConfig = langFile.getLangConfig();
        if(langConfig == null || langConfig.getConfig() == null)
            return "null config";
        if(!langConfig.getConfig().contains("Messages." + path) || langConfig.getConfig().getString("Messages." + path) == null)
            return "null path: Messages." + path;

        return Utils.getInstance().replacePlaceHolders(player, langConfig.getConfig().getString("Messages." + path), Main.getPrefix());
    }

    public ItemStack getItem(String path, Player player) {
        LanguageFile langFile = getCurrentLang();
        LanguageConfig langConfig = langFile.getLangConfig();
        ItemStack error = new ItemStack(Material.BARRIER);
        ItemMeta errorMeta = error.getItemMeta();
        if(langConfig == null || langConfig.getConfig() == null) {
            errorMeta.setDisplayName("Language Config not found!");
            errorMeta.setLore(Arrays.asList("If this happens often,", "please report to the Discord"));
            error.setItemMeta(errorMeta);
            return error;
        }
        if(!langConfig.getConfig().contains("Items." + path)) {
            errorMeta.setDisplayName("Config Path not found!");
            errorMeta.setLore(Arrays.asList("If this happens often,", "please report to the Discord", "Path: Items." + path));
            error.setItemMeta(errorMeta);
            return error;
        }
        ItemStack item;
        Material material = Material.matchMaterial(langConfig.getConfig().getString("Items." + path + ".material"));
        String displayName = langConfig.getConfig().getString("Items." + path + ".displayName");
        List<String> lore = langConfig.getConfig().getStringList("Items." + path + ".lore");
        List<String> loreWithPlaceholders = new ArrayList<>();
        item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        for (String s : lore) {
            loreWithPlaceholders.add(Utils.getInstance().replacePlaceHolders(player, s, Main.getPrefix()));
        }
        meta.setLore(loreWithPlaceholders);
        meta.setDisplayName(Utils.getInstance().replacePlaceHolders(player, displayName, Main.getPrefix()));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getItem(String path, Player player, String langName) {
        LanguageFile langFile = getLang(langName);
        LanguageConfig langConfig = langFile.getLangConfig();
        if(langConfig == null || langConfig.getConfig() == null)
            return new ItemStack(Material.BARRIER);
        if(!langConfig.getConfig().contains("Items." + path))
            return new ItemStack(Material.BARRIER);
        ItemStack item;
        Material material = Material.matchMaterial(langConfig.getConfig().getString("Items." + path + ".material"));
        String displayName = langConfig.getConfig().getString("Items." + path + ".displayName");
        List<String> lore = langConfig.getConfig().getStringList("Items." + path + ".lore");
        List<String> loreWithPlaceholders = new ArrayList<>();
        item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        for (String s : lore) {
            loreWithPlaceholders.add(Utils.getInstance().replacePlaceHolders(player, s, Main.getPrefix()));
        }
        meta.setLore(loreWithPlaceholders);
        meta.setDisplayName(Utils.getInstance().replacePlaceHolders(player, displayName, Main.getPrefix()));
        item.setItemMeta(meta);
        return item;
    }

    public String getMenuTitle(String path, Player player, String langName) {
        LanguageFile langFile = getLang(langName);
        LanguageConfig langConfig = langFile.getLangConfig();
        if(langConfig == null || langConfig.getConfig() == null)
            return "null config";
        if(!langConfig.getConfig().contains("MenuTitles." + path) || langConfig.getConfig().getString("MenuTitles." + path) == null)
            return "null path: MenuTitles." + path;
        return Utils.getInstance().replacePlaceHolders(player, langConfig.getConfig().getString("MenuTitles." + path), Main.getPrefix());
    }

    public String getMenuTitle(String path, Player player) {
        LanguageFile langFile = getCurrentLang();
        LanguageConfig langConfig = langFile.getLangConfig();
        if(langConfig == null || langConfig.getConfig() == null)
            return "null config";
        if(!langConfig.getConfig().contains("MenuTitles." + path) || langConfig.getConfig().getString("MenuTitles." + path) == null)
            return "null path: MenuTitles." + path;
        return Utils.getInstance().replacePlaceHolders(player, langConfig.getConfig().getString("MenuTitles." + path), Main.getPrefix());
    }
}
