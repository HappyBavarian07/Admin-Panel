package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.coolstufflib.jpa.annotations.Column;
import de.happybavarian07.coolstufflib.jpa.annotations.Entity;
import de.happybavarian07.coolstufflib.jpa.annotations.Id;
import de.happybavarian07.coolstufflib.jpa.annotations.Table;

/** One entry of a permission group, including {@code group.<name>} entries for parents. */
@Entity
@Table(name = "permission_group_nodes")
public class GroupNodeEntity {
    @Id
    @Column(name = "entry_id", primaryKey = true)
    private String entryId;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "permission")
    private String permission;

    @Column(name = "value")
    private boolean value;

    @Column(name = "expires_at")
    private long expiresAt;

    @Column(name = "context")
    private String context;

    public GroupNodeEntity() {
    }

    public GroupNodeEntity(String entryId, String groupName, String permission, boolean value, long expiresAt, String context) {
        this.entryId = entryId;
        this.groupName = groupName;
        this.permission = permission;
        this.value = value;
        this.expiresAt = expiresAt;
        this.context = context;
    }

    public String getEntryId() {
        return entryId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isValue() {
        return value;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public String getContext() {
        return context == null ? "" : context;
    }
}
