package de.happybavarian07.adminpanel.data;

import de.happybavarian07.coolstufflib.repository.RepositoryManager;

/** AdminPanel's runtime data tables, registered on CoolStuffLib's repository manager. */
public final class AdminPanelData {
    private final PluginFlagRepository flags;
    private final PluginDescriptionRepository descriptions;
    private final UpdaterCacheRepository updaterCache;
    private final PlayerWarningRepository warnings;

    public AdminPanelData(RepositoryManager repositories) {
        this.flags = repositories.registerRepository(PluginFlagRepository.class, PluginFlag.class);
        this.descriptions = repositories.registerRepository(PluginDescriptionRepository.class, PluginDescriptionEntry.class);
        this.updaterCache = repositories.registerRepository(UpdaterCacheRepository.class, UpdaterCacheEntry.class);
        this.warnings = repositories.registerRepository(PlayerWarningRepository.class, PlayerWarning.class);
    }

    public PluginFlagRepository flags() {
        return flags;
    }

    public PluginDescriptionRepository descriptions() {
        return descriptions;
    }

    public UpdaterCacheRepository updaterCache() {
        return updaterCache;
    }

    public PlayerWarningRepository warnings() {
        return warnings;
    }

    public boolean getFlag(String name, boolean fallback) {
        return flags.findById(name).map(PluginFlag::isEnabled).orElse(fallback);
    }

    public void setFlag(String name, boolean value) {
        flags.save(new PluginFlag(name, value));
    }
}
