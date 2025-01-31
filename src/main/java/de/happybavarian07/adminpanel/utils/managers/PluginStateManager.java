package de.happybavarian07.adminpanel.utils.managers;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
 * @Author HappyBavarian07
 * @Date September 10, 2024 | 16:40
 */
public class PluginStateManager {
    private final FileConfiguration config;
    private final AdminPanelMain plugin;
    private final Map<UUID, Boolean> hurtingWaterMap = new HashMap<>();
    private final Map<UUID, Boolean> chatMuteMap = new HashMap<>();
    private final Map<UUID, Boolean> villagerSoundsMap = new HashMap<>();
    private final Map<UUID, Boolean> blockBreakPreventMap = new HashMap<>();
    private final Map<UUID, Boolean> dupeMobsOnKillMap = new HashMap<>();
    private final Map<UUID, Boolean> freezePlayersMap = new HashMap<>();
    public boolean inMaintenanceMode = false;
    public boolean globalChatMuted = false;

    /**
     * Initializes the UtilityChecks module
     *
     * @param config Hopefully the Config of the Plugin
     */
    public PluginStateManager(FileConfiguration config, AdminPanelMain plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    // These Methods are used to get and set the Maintenance Mode and Chat Mute Status

    /**
     * Returns if the Plugin is in Maintenance Mode
     *
     * @return If the Plugin is in Maintenance Mode
     */
    public boolean isInMaintenanceMode() {
        return inMaintenanceMode;
    }

    public void setInMaintenanceMode(boolean inMaintenanceMode) {
        this.inMaintenanceMode = inMaintenanceMode;
    }

    /**
     * Returns if the Chat is Muted
     *
     * @return If the Chat is Muted
     */
    public boolean isGlobalChatMuted() {
        return globalChatMuted;
    }

    public void setGlobalChatMuted(boolean globalChatMuted) {
        this.globalChatMuted = globalChatMuted;
    }

    // These Methods are used to check for various conditions

    /**
     * Returns if the Addon System is enabled
     *
     * @return If the Addon System is enabled
     */
    public boolean isAddonSystemEnabled() {
        return config.getBoolean("Plugin.AddonSystem.enabled");
    }

    /**
     * Returns if the Updater is enabled
     *
     * @return If the Updater is enabled
     */
    public boolean isUpdaterEnabled() {
        return config.getBoolean("Plugin.Updater.checkForUpdates");
    }

    /**
     * Returns if the Plugin Updater is enabled
     *
     * @return If the Plugin Updater is enabled
     */
    public boolean isPluginUpdaterEnabled() {
        return config.getBoolean("Plugin.Updater.PluginUpdater.enabled");
    }

    /**
     * Returns if the Automatic Replace of the Plugin is enabled
     *
     * @return If the Automatic Replace of the Plugin is enabled
     */
    public boolean isUpdateReplacerEnabled() {
        return config.getBoolean("Plugin.Updater.automaticReplace");
    }

    public boolean isDataClientEnabled() {
        return config.getBoolean("Plugin.DataClient.enabled");
    }

    public boolean isWebUIEnabled() {
        return config.getBoolean("Plugin.WebUI.enabled");
    }

    public int getWebUIPort() {
        return config.getInt("Plugin.WebUI.port");
    }

    public boolean checkIfBungee(boolean sendLogOnDisabled) {
        if (Bukkit.getServer().spigot().getConfig().getConfigurationSection("settings").getBoolean("bungeecord", false) &&
                config.getBoolean("Plugin.BungeeSyncSystem.enabled")) {
            return true;
        } else {
            if (sendLogOnDisabled) {
                plugin.getLogger().severe("This Server is not BungeeCord.");
                plugin.getLogger().severe("If the Server is already hooked to BungeeCord, please enable it into your spigot.yml aswell.");
                plugin.getLogger().severe("Plugin - BungeeCord Connection Feature disabled!");
            }
            return false;
        }
    }

    public boolean checkIfCorruptionCheckIsDisabled() {
        return new File(plugin.getDataFolder(), "DisableConfigBackupCorruptionCheckIKnowWhatIAmDoingISwear").exists();
    }

    // These Methods are used to get the Maps of different Trolls and Settings

    public Map<UUID, Boolean> getHurtingWaterMap() {
        return hurtingWaterMap;
    }

    public Map<UUID, Boolean> getChatMuteMap() {
        return chatMuteMap;
    }

    public Map<UUID, Boolean> getVillagerSoundsMap() {
        return villagerSoundsMap;
    }

    public Map<UUID, Boolean> getBlockBreakPreventMap() {
        return blockBreakPreventMap;
    }

    public Map<UUID, Boolean> getDupeMobsOnKillMap() {
        return dupeMobsOnKillMap;
    }

    public Map<UUID, Boolean> getFreezePlayersMap() {
        return freezePlayersMap;
    }
}
