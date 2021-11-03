package de.happybavarian07.main;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.happybavarian07.events.AdminPanelEvent;
import de.happybavarian07.events.NotAPanelEventException;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.utils.PluginUtils;
import de.happybavarian07.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

class LocalAdminPanelAPI implements AdminPanelAPI {

    private static final Map<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();
    private final AdminPanelMain plugin;
    private final LanguageManager lgm;
    private final PluginUtils pluginUtils;

    public LocalAdminPanelAPI(AdminPanelMain plugin) {
        this.plugin = plugin;
        this.lgm = plugin.getLanguageManager();
        this.pluginUtils = new PluginUtils();
    }

    @Override
    public Map<Player, PlayerMenuUtility> getPlayerMenuUtilityMap() {
        return playerMenuUtilityMap;
    }

    @Override
    public void openPanel(Menu menu) {
        menu.open();
    }

    @Override
    public PlayerMenuUtility getPlayerMenuUtility(Player player) {
        PlayerMenuUtility playerMenuUtility;
        if (!(playerMenuUtilityMap.containsKey(player))) { //See if the player has a playermenuutility "saved" for them

            //This player doesn't. Make one for them add add it to the hashmap
            playerMenuUtility = new PlayerMenuUtility(player);
            playerMenuUtilityMap.put(player, playerMenuUtility);

            return playerMenuUtility;
        } else {
            return playerMenuUtilityMap.get(player); //Return the object by using the provided player
        }
    }

    public ItemStack createSkull(String headTexture, String name) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1, (short) 3);
        if (headTexture.isEmpty()) return head;

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(Utils.chat(name));
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);

        profile.getProperties().put("textures", new Property("textures", headTexture));

        try {
            Field field = meta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(meta, profile);
        } catch (IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        head.setItemMeta(meta);
        return head;
    }

    public ItemStack createSkull(Head headTexture, String name) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1, (short) 3);
        if (headTexture.getFullTexture().isEmpty()) return head;

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(Utils.chat(name));
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);

        profile.getProperties().put("textures", new Property("textures", headTexture.getFullTexture()));

        try {
            Field field = meta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(meta, profile);
        } catch (IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        head.setItemMeta(meta);
        return head;
    }

    @Override
    public void loadPlugin(File pluginFile) throws InvalidPluginException, InvalidDescriptionException {
        pluginUtils.load(pluginFile);
    }

    @Override
    public void reloadPlugin(Plugin plugin) {
        pluginUtils.reload(plugin);
    }

    @Override
    public void reloadPlugin(String pluginName) {
        pluginUtils.reload(pluginUtils.getPluginByName(pluginName));
    }

    @Override
    public void unloadPlugin(Plugin plugin) {
        pluginUtils.unload(plugin);
    }

    @Override
    public void unloadPlugin(String pluginName) {
        pluginUtils.unload(pluginUtils.getPluginByName(pluginName));
    }

    @Override
    public List<String> getPluginNames(boolean fullName) {
        return pluginUtils.getPluginNames(fullName);
    }

    @Override
    public List<Plugin> getAllPlugins() {
        return pluginUtils.getAllPlugins();
    }

    @Override
    public Plugin getPluginByName(String pluginName) throws NullPointerException {
        return pluginUtils.getPluginByName(pluginName);
    }

    @Override
    public Plugin downloadPluginFromSpiget(int resourceID, String fileName, Boolean enableAfterInstall) throws IOException, InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
        return pluginUtils.downloadPluginFromSpiget(resourceID, fileName, enableAfterInstall);
    }

    @Override
    public void clearChat(int lines, boolean broadcastChatClear, Player player) {
        Utils.clearChat(lines, broadcastChatClear, player);
    }

    @Override
    public void reloadServer(int time) throws InterruptedException {
        Utils.serverReload(time);
    }

    @Override
    public void stopServer(int time, int time2) throws InterruptedException {
        Utils.serverStop(time, time2);
    }

    @Override
    public void addLanguage(LanguageFile languageFile, String languageName) {
        lgm.addLang(languageFile, languageName);
    }

    @Override
    public LanguageFile getLanguage(String name) throws NullPointerException {
        return lgm.getLang(name);
    }

    @Override
    public Map<String, LanguageFile> getRegisteredLanguages() {
        return lgm.getRegisteredLanguages();
    }

    @Override
    public void removeLanguage(String languageName) {
        lgm.removeLang(languageName);
    }

    @Override
    public void setCurrentLanguage(LanguageFile languageFile) throws NullPointerException {
        lgm.setCurrentLang(languageFile);
    }

    @Override
    public String getMessage(String path, Player player) {
        return lgm.getMessage(path, player);
    }

    @Override
    public String getMessage(String path, Player player, String langName) {
        return lgm.getMessage(path, player, langName);
    }

    @Override
    public ItemStack getItem(String path, Player player) {
        return lgm.getItem(path, player);
    }

    @Override
    public ItemStack getItem(String path, Player player, String langName) {
        return lgm.getItem(path, player, langName);
    }

    @Override
    public String getMenuTitle(String path, Player player) {
        return lgm.getMenuTitle(path, player);
    }

    @Override
    public String getMenuTitle(String path, Player player, String langName) {
        return lgm.getMenuTitle(path, player, langName);
    }

    @Override
    public void reloadConfigurationFiles(Player messageReceiver) {
        plugin.reloadConfig();
        messageReceiver.sendMessage(lgm.getMessage("Player.General.ReloadedConfig", messageReceiver));
        lgm.reloadLanguages(messageReceiver);
        plugin.getFileLogger().writeToLog(Level.CONFIG, "Reloaded the Configuration Files", "API");
    }

    // Events
    @Override
    public AdminPanelEvent callAdminPanelEvent(Event event) throws NotAPanelEventException {
        if (event instanceof AdminPanelEvent) {
            Bukkit.getPluginManager().callEvent(event);
            plugin.getFileLogger().writeToLog(Level.CONFIG, "Called the Admin-Panel Event " + event, "API");
            return (AdminPanelEvent) event;
        } else {
            throw new NotAPanelEventException("The Event: " + event + " is not an Admin-Panel Event!\nThis Error usually happens if a Plugin tryes to call a Normal Bukkit Event with the callAdminPanelEvent Method in the API!\nPlease contact the Developer of this Plugin!");
        }
    }
}
