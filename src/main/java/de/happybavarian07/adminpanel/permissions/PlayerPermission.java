package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.adminpanel.mysql.annotations.Column;
import de.happybavarian07.adminpanel.mysql.annotations.Entity;
import de.happybavarian07.adminpanel.mysql.annotations.Id;
import de.happybavarian07.adminpanel.mysql.annotations.Table;

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
}
