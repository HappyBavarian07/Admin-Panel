package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.coolstufflib.jpa.annotations.Column;
import de.happybavarian07.coolstufflib.jpa.annotations.Entity;
import de.happybavarian07.coolstufflib.jpa.annotations.Id;
import de.happybavarian07.coolstufflib.jpa.annotations.Table;

/** The settings of a permission group; its entries are in {@link GroupNodeEntity}. */
@Entity
@Table(name = "permission_groups")
public class PermissionGroupEntity {
    @Id
    @Column(name = "name", primaryKey = true)
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "weight")
    private int weight;

    @Column(name = "prefix")
    private String prefix;

    @Column(name = "suffix")
    private String suffix;

    @Column(name = "created_at")
    private long createdAt;

    public PermissionGroupEntity() {
    }

    public PermissionGroupEntity(String name, String displayName, int weight, String prefix, String suffix, long createdAt) {
        this.name = name;
        this.displayName = displayName;
        this.weight = weight;
        this.prefix = prefix;
        this.suffix = suffix;
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName == null ? "" : displayName;
    }

    public int getWeight() {
        return weight;
    }

    public String getPrefix() {
        return prefix == null ? "" : prefix;
    }

    public String getSuffix() {
        return suffix == null ? "" : suffix;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
