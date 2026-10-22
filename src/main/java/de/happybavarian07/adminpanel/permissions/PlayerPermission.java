package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.coolstufflib.jpa.annotations.Column;
import de.happybavarian07.coolstufflib.jpa.annotations.Entity;
import de.happybavarian07.coolstufflib.jpa.annotations.Id;
import de.happybavarian07.coolstufflib.jpa.annotations.Table;

import java.util.UUID;

@Entity
@Table(name = "player_permissions")
public class PlayerPermission {
    @Id
    @Column(name = "entry_id", primaryKey = true)
    private String entryID;

    @Column(name = "player_uuid")
    private UUID playerUUID;

    @Column(name = "permission")
    private String permission;

    @Column(name = "value")
    private boolean value;

    /** When the entry ends in milliseconds since the epoch, 0 for never. */
    @Column(name = "expires_at")
    private long expiresAt;

    /** Where the entry applies as {@code key=value;key=value}, empty for everywhere. */
    @Column(name = "context")
    private String context;

    public String getEntryID() {
        return entryID;
    }

    public void setEntryID(String entryID) {
        this.entryID = entryID;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getContext() {
        return context == null ? "" : context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
