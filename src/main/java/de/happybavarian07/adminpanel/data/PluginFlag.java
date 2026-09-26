package de.happybavarian07.adminpanel.data;

import de.happybavarian07.coolstufflib.jpa.annotations.Column;
import de.happybavarian07.coolstufflib.jpa.annotations.Entity;
import de.happybavarian07.coolstufflib.jpa.annotations.Id;
import de.happybavarian07.coolstufflib.jpa.annotations.Table;

@Entity
@Table(name = "plugin_flags")
public class PluginFlag {
    @Id
    @Column(name = "name", primaryKey = true)
    private String name;

    @Column(name = "enabled")
    private boolean enabled;

    public PluginFlag() {
    }

    public PluginFlag(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
