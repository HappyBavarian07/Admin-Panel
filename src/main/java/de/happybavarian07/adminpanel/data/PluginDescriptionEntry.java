package de.happybavarian07.adminpanel.data;

import de.happybavarian07.coolstufflib.jpa.annotations.Column;
import de.happybavarian07.coolstufflib.jpa.annotations.Entity;
import de.happybavarian07.coolstufflib.jpa.annotations.Id;
import de.happybavarian07.coolstufflib.jpa.annotations.Table;

@Entity
@Table(name = "plugin_descriptions")
public class PluginDescriptionEntry {
    @Id
    @Column(name = "plugin_name", primaryKey = true)
    private String pluginName;

    @Column(name = "description")
    private String description;

    @Column(name = "resource_id")
    private int resourceId = -1;

    public PluginDescriptionEntry() {
    }

    public PluginDescriptionEntry(String pluginName, String description, int resourceId) {
        this.pluginName = pluginName;
        this.description = description;
        this.resourceId = resourceId;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getDescription() {
        return description;
    }

    public int getResourceId() {
        return resourceId;
    }
}
