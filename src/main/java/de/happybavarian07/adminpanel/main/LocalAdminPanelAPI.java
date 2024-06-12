package de.happybavarian07.adminpanel.main;

import de.happybavarian07.adminpanel.commandmanagement.CommandManager;
import de.happybavarian07.adminpanel.events.AdminPanelEvent;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.language.LanguageFile;
import de.happybavarian07.adminpanel.language.LanguageManager;
import de.happybavarian07.adminpanel.language.Placeholder;
import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.utils.LogPrefix;
import de.happybavarian07.adminpanel.utils.PluginUtils;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

class LocalAdminPanelAPI implements AdminPanelAPI {

    private static final Map<UUID, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();
    private final AdminPanelMain plugin;
    private final LanguageManager lgm;
    private final PluginUtils pluginUtils;
    // PlayerUUID, Cooldown Time in Millis
    private final Map<UUID, Long> cooldownTimeMap = new HashMap<>();

    public LocalAdminPanelAPI(AdminPanelMain plugin) {
        this.plugin = plugin;
        this.lgm = plugin.getLanguageManager();
        this.pluginUtils = new PluginUtils();
    }

    @Override
    public Map<UUID, PlayerMenuUtility> getPlayerMenuUtilityMap() {
        return playerMenuUtilityMap;
    }

    @Override
    public void openPanel(Menu menu) {
        menu.open();
    }

    @Override
    public PlayerMenuUtility getPlayerMenuUtility(Player player) {
        PlayerMenuUtility playerMenuUtility;
        if (!(playerMenuUtilityMap.containsKey(player.getUniqueId()))) { //See if the player has a playermenuutility "saved" for them

            //This player doesn't. Make one for them add add it to the hashmap
            playerMenuUtility = new PlayerMenuUtility(player.getUniqueId());
            playerMenuUtilityMap.put(player.getUniqueId(), playerMenuUtility);

            return playerMenuUtility;
        } else {
            return playerMenuUtilityMap.get(player.getUniqueId()); //Return the object by using the provided player
        }
    }

    public ItemStack createSkull(String headTexture, String name) {
        ItemStack head = new ItemStack(legacyServer() ? Material.matchMaterial("SKULL_ITEM") : Material.PLAYER_HEAD, 1);
        if (headTexture.isEmpty()) return head;

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(Utils.chat(name));
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "CustomHead");

        try {
            profile.getTextures().setSkin(new URL("https://textures.minecraft.net/texture/" + headTexture));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        meta.setOwnerProfile(profile);

        head.setItemMeta(meta);
        return head;
    }

    public ItemStack createSkull(Head headTexture, String name) {
        ItemStack head = new ItemStack(legacyServer() ? Material.matchMaterial("SKULL_ITEM") : Material.PLAYER_HEAD, 1);
        if (headTexture.getTexture().isEmpty()) return head;

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(Utils.chat(name));
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "CustomHead");

        try {
            profile.getTextures().setSkin(new URL("https://textures.minecraft.net/texture/" + headTexture.getTexture()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        meta.setOwnerProfile(profile);

        head.setItemMeta(meta);
        return head;
    }

    private boolean legacyServer() {
        String serverVersion = Bukkit.getServer().getVersion();
        return serverVersion.contains("1.12") ||
                serverVersion.contains("1.11") ||
                serverVersion.contains("1.10") ||
                serverVersion.contains("1.9") ||
                serverVersion.contains("1.8") ||
                serverVersion.contains("1.7");
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
    public void restartServer(int timeBeforeRestart) {
        Utils.serverRestart(timeBeforeRestart);
    }

    @Override
    public void stopServer(int timeBeforeStop) {
        Utils.serverStop(timeBeforeStop);
    }

    @Override
    public int reportBugToDiscord(UUID playerUUID, String reportMessage) {
        long playerTime = cooldownTimeMap.getOrDefault(playerUUID, -1L);
        if (cooldownTimeMap.containsKey(playerUUID) && playerTime > System.currentTimeMillis()) return -2;
        if (cooldownTimeMap.containsKey(playerUUID) && playerTime <= System.currentTimeMillis()) {
            cooldownTimeMap.remove(playerUUID);
        }
        Player player = Bukkit.getPlayer(playerUUID);
        boolean response = Utils.sendMessageToWebhook(
                "**Admin-Panel-Report** from Player '" + (player != null ? "**" + player.getName() + "**" : "**Not found**") + " (UUID: **" + playerUUID + "**)':\n" +
                reportMessage
        );
        if (response) {
            cooldownTimeMap.put(playerUUID, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5));
            return 0;
        }
        return -1;
    }

    @Override
    public int reportBugToDiscord(UUID playerUUID, List<String> reportMessageArrayList) {
        return reportBugToDiscord(playerUUID, String.join(" ", reportMessageArrayList));
    }

    @Override
    public int reportBugToDiscord(UUID playerUUID, String[] reportMessageArray) {
        return reportBugToDiscord(playerUUID, String.join(" ", reportMessageArray));
    }

    @Override
    public void addLanguage(LanguageFile languageFile, String languageName) {
        lgm.addLang(languageFile, languageName);
    }

    @Override
    public LanguageFile getLanguage(String name, boolean throwException) throws NullPointerException {
        return lgm.getLang(name, throwException);
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
        lgm.setCurrentLang(languageFile, true);
    }

    @Override
    public String getMessage(String path, Player player, boolean resetAfter) {
        return lgm.getMessage(path, player, resetAfter);
    }

    @Override
    public String getMessage(String path, Player player, String langName, boolean resetAfter) {
        return lgm.getMessage(path, player, langName, resetAfter);
    }

    @Override
    public void addPlaceholder(PlaceholderType type, String key, Object value, boolean resetBefore) {
        lgm.addPlaceholder(type, key, value, resetBefore);
    }

    @Override
    public void addPlaceholders(Map<String, Placeholder> placeholders, boolean resetBefore) {
        lgm.addPlaceholders(placeholders, resetBefore);
    }

    @Override
    public void removePlaceholder(PlaceholderType type, String key) {
        lgm.removePlaceholder(type, key);
    }

    @Override
    public void removePlaceholders(PlaceholderType type, List<String> keys) {
        lgm.removePlaceholders(type, keys);
    }

    @Override
    public void resetPlaceholders(PlaceholderType type, @Nullable List<String> excludeKeys) {
        lgm.resetPlaceholders(type, excludeKeys);
    }

    @Override
    public void resetSpecificPlaceholders(PlaceholderType type, @Nullable List<String> includeKeys) {
        lgm.resetSpecificPlaceholders(type, includeKeys);
    }

    @Override
    public Map<String, Placeholder> getPlaceholders() {
        return lgm.getPlaceholders();
    }

    @Override
    public String replacePlaceholders(PlaceholderType type, String message) {
        return lgm.replacePlaceholders(type, message);
    }

    @Override
    public String replacePlaceholders(String message, Map<String, Placeholder> placeholders) {
        return lgm.replacePlaceholders(message, placeholders);
    }

    @Override
    public Map<String, Placeholder> getNewPlaceholderMap() {
        return lgm.getNewPlaceholderMap();
    }

    @Override
    public ItemStack getItem(String path, Player player, boolean resetAfter) {
        return lgm.getItem(path, player, resetAfter);
    }

    @Override
    public ItemStack getItem(String path, Player player, String langName, boolean resetAfter) {
        return lgm.getItem(path, player, langName, resetAfter);
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
    public <T> T getCustomObject(String path, Player player, T defaultValue, boolean resetAfter) {
        return lgm.getCustomObject(path, player, defaultValue, resetAfter);
    }

    @Override
    public <T> T getCustomObject(String path, Player player, String langName, T defaultValue, boolean resetAfter) {
        return lgm.getCustomObject(path, player, langName, defaultValue, resetAfter);
    }

    @Override
    public void reloadConfigurationFiles(CommandSender messageReceiver) {
        plugin.reloadConfig();
        messageReceiver.sendMessage(lgm.getMessage("Player.General.ReloadedConfig", null, true));
        lgm.reloadLanguages(messageReceiver, true);
        plugin.getFileLogger().writeToLog(Level.CONFIG, "Reloaded the Configuration Files", LogPrefix.API);
    }

    // Events
    @Override
    public AdminPanelEvent callAdminPanelEvent(Event event) throws NotAPanelEventException {
        if (event instanceof AdminPanelEvent) {
            Bukkit.getPluginManager().callEvent(event);
            plugin.getFileLogger().writeToLog(Level.CONFIG, "Called the Admin-Panel Event " + event, LogPrefix.API);
            return (AdminPanelEvent) event;
        } else {
            throw new NotAPanelEventException("The Event: " + event + " is not an Admin-Panel Event!\nThis Error usually happens if a Plugin tryes to call a Normal Bukkit Event with the callAdminPanelEvent Method in the API!\nPlease contact the Developer of this Plugin!");
        }
    }

    @Override
    public boolean registerCommandManager(CommandManager commandManager) {
        return plugin.getCommandManagerRegistry().register(commandManager);
    }

    @Override
    public Map<UUID, Long> getCooldownTimeMap() {
        return cooldownTimeMap;
    }
}
