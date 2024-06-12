package de.happybavarian07.adminpanel.language;

import de.happybavarian07.adminpanel.configupdater.ConfigUpdater;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class LanguageManager {
    private final JavaPlugin plugin;
    private final File langFolder;
    private final Map<String, LanguageFile> registeredLanguages;
    private final Map<String, Placeholder> placeholders;
    private String prefix;
    private String currentLangName;
    private LanguageFile currentLang;
    private PerPlayerLanguageHandler plhandler;

    public LanguageManager(JavaPlugin plugin, File langFolder, String prefix) {
        this.prefix = prefix;
        this.plugin = plugin;
        this.langFolder = langFolder;
        this.registeredLanguages = new LinkedHashMap<>();
        this.placeholders = new LinkedHashMap<>();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public PerPlayerLanguageHandler getPlhandler() {
        return plhandler;
    }

    public void setPlhandler(PerPlayerLanguageHandler plhandler) {
        this.plhandler = plhandler;
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
                LanguageFile languageFile = new LanguageFile(plugin, file.getName().replace(".yml", ""), true);
                if (!registeredLanguages.containsValue(languageFile) && !languageFile.getLangName().equals("default"))
                    this.registeredLanguages.put(languageFile.getLangName(), languageFile);
                if (log && !languageFile.getLangName().equals("default"))
                    plugin.getLogger().log(Level.INFO, "Language: " + languageFile.getLangFile() + " successfully registered!");
            }
        }
    }

    public void updateLangFiles() {
        for (LanguageFile langFiles : getRegisteredLanguages().values()) {
            try {
                String resourceName = "languages/" + langFiles.getLangFile().getName();
                if (plugin.getResource(resourceName) == null) {
                    if (plugin.getResource("languages/" + plugin.getConfig().getString("Plugin.languageForUpdates") + ".yml") != null) {
                        resourceName = "languages/" + plugin.getConfig().getString("Plugin.languageForUpdates") + ".yml";
                    } else {
                        resourceName = "languages/en.yml";
                    }
                }
                // "Test.Options", "Items.PlayerManager.TrollMenu.VillagerSounds.true.Options"
                ConfigUpdater.update(plugin, resourceName, langFiles.getLangFile(), Collections.emptyList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*for (LanguageFile langFiles : getRegisteredLanguages().values()) {
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
        }*/
    }

    public void reloadLanguages(CommandSender messageReceiver, Boolean log) {
        addLanguagesToList(log);
        updateLangFiles();
        for (String langFiles : registeredLanguages.keySet()) {
            getLang(langFiles, true).getLangConfig().reloadConfig();
            if (messageReceiver != null) {
                addPlaceholder(PlaceholderType.MESSAGE, "%language%", getLang(langFiles, true).getLangFile(), true);
                messageReceiver.sendMessage(getMessage("Player.General.ReloadedLanguageFile", null, true));
            }
        }
        setCurrentLang(getLang(plugin.getConfig().getString("Plugin.language"), true), log);
    }

    public void addLang(LanguageFile langFile, String langName) {
        if (registeredLanguages.containsKey(langName) || langName.equals("default"))
            return;
        registeredLanguages.put(langName, langFile);
        plugin.getLogger().log(Level.INFO, "Language: " + langFile.getLangFile() + " successfully registered!");
    }

    public LanguageFile getLang(String langName, boolean throwException) throws NullPointerException {
        if (!registeredLanguages.containsKey(langName))
            if (throwException)
                throw new NullPointerException("Language: " + langName + " not found!");
            else
                return null;
        return registeredLanguages.get(langName);
    }

    public void removeLang(String langName) {
        if (!registeredLanguages.containsKey(langName))
            return;
        registeredLanguages.remove(langName);
    }

    public void addPlaceholder(PlaceholderType type, String key, Object value, boolean resetBefore) {
        if (resetBefore) resetSpecificPlaceholders(type, Collections.singletonList(key));
        if (!placeholders.containsKey(key))
            placeholders.put(key, new Placeholder(key, value, type));
        else
            placeholders.replace(key, placeholders.get(key), new Placeholder(key, value, type));
    }

    public void addPlaceholders(Map<String, Placeholder> placeholders, boolean resetBefore) {
        if (resetBefore) resetSpecificPlaceholders(PlaceholderType.ALL, new ArrayList<>(placeholders.keySet()));
        this.placeholders.putAll(placeholders);
    }

    public void removePlaceholder(PlaceholderType type, String key) {
        if (!placeholders.containsKey(key)) return;
        if (!placeholders.get(key).getType().equals(type) && !placeholders.get(key).getType().equals(PlaceholderType.ALL))
            return;

        placeholders.remove(key);
    }

    public void removePlaceholders(PlaceholderType type, List<String> keys) {
        for (String key : keys) {
            if (!placeholders.containsKey(key)) continue;
            if (!placeholders.get(key).getType().equals(type) && !placeholders.get(key).getType().equals(PlaceholderType.ALL))
                continue;

            this.placeholders.remove(key);
        }
    }

    public void resetPlaceholders(PlaceholderType type, @Nullable List<String> excludeKeys) {
        List<String> keysToRemove = new ArrayList<>();
        for (String key : placeholders.keySet()) {
            if (excludeKeys != null && excludeKeys.contains(key)) continue;
            if (!placeholders.get(key).getType().equals(type) && !placeholders.get(key).getType().equals(PlaceholderType.ALL))
                continue;

            keysToRemove.add(key);
        }
        removePlaceholders(type, keysToRemove);
    }

    public void resetSpecificPlaceholders(PlaceholderType type, @Nullable List<String> includeKeys) {
        List<String> keysToRemove = new ArrayList<>();
        for (String key : placeholders.keySet()) {
            if (includeKeys != null && !includeKeys.contains(key)) continue;
            if (!placeholders.get(key).getType().equals(type) &&
                    !placeholders.get(key).getType().equals(PlaceholderType.ALL) &&
                    !type.equals(PlaceholderType.ALL))
                continue;

            keysToRemove.add(key);
        }
        removePlaceholders(type, keysToRemove);
    }

    public Map<String, Placeholder> getPlaceholders() {
        return placeholders;
    }

    public List<String> getPlaceholderKeysInMessage(String message, PlaceholderType type) {
        List<String> keys = new ArrayList<>();
        for (String key : placeholders.keySet()) {
            if (!message.contains(key)) continue;
            if (!placeholders.get(key).getType().equals(type) && !placeholders.get(key).getType().equals(PlaceholderType.ALL))
                continue;

            keys.add(key);
        }
        return keys;
    }


    public String replacePlaceholders(PlaceholderType type, String message) {
        for (String key : placeholders.keySet()) {
            if (!placeholders.get(key).getType().equals(type) && !placeholders.get(key).getType().equals(PlaceholderType.ALL))
                continue;

            message = placeholders.get(key).replace(message);
        }
        return message;
    }

    public ItemStack replacePlaceholders(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        List<String> lore = meta.getLore();
        List<String> loreWithPlaceholders = new ArrayList<>();
        assert lore != null;
        for (String s : lore) {
            String temp = replacePlaceholders(PlaceholderType.ITEM, s);
            loreWithPlaceholders.add(Utils.format(player, temp, prefix));
        }
        meta.setLore(loreWithPlaceholders);
        meta.setDisplayName(replacePlaceholders(PlaceholderType.ITEM, Utils.format(player, meta.getDisplayName(), prefix)));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack replacePlaceholders(Player player, ItemStack item, Map<String, Placeholder> placeholders) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        List<String> lore = meta.getLore();
        List<String> loreWithPlaceholders = new ArrayList<>();
        assert lore != null;
        for (String s : lore) {
            String temp = replacePlaceholders(s, placeholders);
            loreWithPlaceholders.add(Utils.format(player, temp, prefix));
        }
        meta.setLore(loreWithPlaceholders);
        meta.setDisplayName(replacePlaceholders(Utils.format(player, meta.getDisplayName(), prefix), placeholders));
        item.setItemMeta(meta);
        return item;
    }

    public String replacePlaceholders(String message, Map<String, Placeholder> placeholders) {
        for (String key : placeholders.keySet()) {
            message = placeholders.get(key).replace(message);
        }
        return message;
    }

    public Map<String, Placeholder> getNewPlaceholderMap() {
        return new HashMap<>();
    }

    public LanguageFile getLangOrPlayerLang(boolean currentLang, String langName, @Nullable Player player) {
        if (player == null && currentLang) return getCurrentLang();
        if (player == null) return getLang(langName, true);
        if (langName == null && currentLang) return getCurrentLang();

        LanguageFile langFile = plhandler.getPlayerLanguageFile(player.getUniqueId());
        if (langFile == null) {
            if (currentLang) return getCurrentLang();
            else return getLang(langName, true);
        }
        return langFile;
    }

    public String getMessage(String path, Player player, boolean resetAfter) {
        return getMessage(path, player, getCurrentLangName(), resetAfter);
    }

    public String getMessage(String path, Player player, String langName, boolean resetAfter) {
        LanguageFile langFile = getLangOrPlayerLang(true, langName, player);
        LanguageConfig langConfig = langFile.getLangConfig();
        if (langConfig == null || langConfig.getConfig() == null)
            return "null config";
        if (langConfig.getConfig().getString("Messages." + path) == null || !langConfig.getConfig().contains("Messages." + path))
            return "null path: Messages." + path;

        String message = Utils.format(player, langConfig.getConfig().getString("Messages." + path), prefix);
        if (placeholders.isEmpty()) return message;

        List<String> includedKeys = new ArrayList<>(getPlaceholderKeysInMessage(message, PlaceholderType.MESSAGE));
        message = replacePlaceholders(PlaceholderType.MESSAGE, message);
        if (resetAfter) resetSpecificPlaceholders(PlaceholderType.MESSAGE, includedKeys);
        return message;
    }

    public String getPermissionMessage(Player player, String permission) {
        addPlaceholder(PlaceholderType.MESSAGE, "%permission%", permission, true);
        return getMessage("Player.General.NoPermissions", player, true);
    }

    public ItemStack getItem(String path, Player player, boolean resetAfter) {
        return getItem(path, player, getCurrentLangName(), resetAfter);
    }

    public ItemStack getItem(String path, Player player, String langName, boolean resetAfter) {
        LanguageFile langFile = getLangOrPlayerLang(false, langName, player);
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
        List<String> includedKeys = new ArrayList<>();
        item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        for (String s : lore) {
            includedKeys.addAll(getPlaceholderKeysInMessage(s, PlaceholderType.ITEM));
            String temp = replacePlaceholders(PlaceholderType.ITEM, s);
            loreWithPlaceholders.add(Utils.format(player, temp, prefix));
        }
        assert meta != null;
        meta.setLore(loreWithPlaceholders);
        assert displayName != null;
        includedKeys.addAll(getPlaceholderKeysInMessage(Utils.format(player, displayName, prefix), PlaceholderType.ITEM));
        meta.setDisplayName(replacePlaceholders(PlaceholderType.ITEM, Utils.format(player, displayName, prefix)));
        if (langConfig.getConfig().getBoolean("Items." + path + ".enchanted", false)) {
            meta.addEnchant(Enchantment.DURABILITY, 0, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        if (resetAfter) resetSpecificPlaceholders(PlaceholderType.ITEM, includedKeys);
        return item;
    }

    public String getMenuTitle(String path, Player player) {
        return getMenuTitle(path, player, getCurrentLangName());
    }

    public String getMenuTitle(String path, Player player, String langName) {
        LanguageFile langFile = getLangOrPlayerLang(false, langName, player);
        LanguageConfig langConfig = langFile.getLangConfig();
        if (langConfig == null || langConfig.getConfig() == null)
            return "null config";
        if (langConfig.getConfig().getString("MenuTitles." + path) == null || !langConfig.getConfig().contains("MenuTitles." + path))
            return "null path: MenuTitles." + path;
        String title = langConfig.getConfig().getString("MenuTitles." + path);
        List<String> includedKeys = new ArrayList<>(getPlaceholderKeysInMessage(title, PlaceholderType.MENUTITLE));
        title = replacePlaceholders(PlaceholderType.MENUTITLE, title);
        resetSpecificPlaceholders(PlaceholderType.MENUTITLE, includedKeys);
        return Utils.format(player, title, prefix);
    }

    public <T> T getCustomObject(String path, @Nullable Player player, T defaultValue, boolean resetAfter) {
        return getCustomObject(path, player, getCurrentLangName(), defaultValue, resetAfter);
    }

    public <T> T getCustomObject(String path, @Nullable Player player, String langName, T defaultValue, boolean resetAfter) {
        LanguageFile langFile = getLangOrPlayerLang(false, langName, player);
        LanguageConfig langConfig = langFile.getLangConfig();
        if (langConfig == null || langConfig.getConfig() == null)
            return defaultValue;
        if (langConfig.getConfig().get(path) == null || !langConfig.getConfig().contains(path))
            return defaultValue;
        if (langConfig.getConfig().get(path) == null) return defaultValue;

        T obj;
        try {
            obj = (T) langConfig.getConfig().get(path);
            if (obj instanceof String) {
                obj = (T) replacePlaceholders(PlaceholderType.CUSTOM, Utils.format(player, obj.toString(), prefix));
                if (resetAfter)
                    resetSpecificPlaceholders(PlaceholderType.CUSTOM,
                            getPlaceholderKeysInMessage((String) langConfig.getConfig().get(path), PlaceholderType.CUSTOM));
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
            return defaultValue;
        }
        if (obj == null) obj = defaultValue;
        return obj;
    }
}
