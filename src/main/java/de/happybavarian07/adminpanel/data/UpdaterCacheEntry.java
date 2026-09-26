package de.happybavarian07.adminpanel.data;

import de.happybavarian07.coolstufflib.jpa.annotations.Column;
import de.happybavarian07.coolstufflib.jpa.annotations.Entity;
import de.happybavarian07.coolstufflib.jpa.annotations.Id;
import de.happybavarian07.coolstufflib.jpa.annotations.Table;

@Entity
@Table(name = "updater_cache")
public class UpdaterCacheEntry {
    @Id
    @Column(name = "resource_id", primaryKey = true)
    private int resourceId;

    @Column(name = "latest_version")
    private String latestVersion;

    @Column(name = "update_available")
    private boolean updateAvailable;

    @Column(name = "last_check_millis")
    private long lastCheckMillis;

    @Column(name = "message")
    private String message;

    public UpdaterCacheEntry() {
    }

    public UpdaterCacheEntry(int resourceId, String latestVersion, boolean updateAvailable, long lastCheckMillis, String message) {
        this.resourceId = resourceId;
        this.latestVersion = latestVersion;
        this.updateAvailable = updateAvailable;
        this.lastCheckMillis = lastCheckMillis;
        this.message = message;
    }

    public int getResourceId() {
        return resourceId;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public long getLastCheckMillis() {
        return lastCheckMillis;
    }

    public String getMessage() {
        return message;
    }
}
