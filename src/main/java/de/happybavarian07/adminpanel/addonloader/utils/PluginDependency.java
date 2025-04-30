package de.happybavarian07.adminpanel.addonloader.utils;/*
 * @Author HappyBavarian07
 * @Date 12.01.2022 | 17:29
 */


public class PluginDependency {
    // If it is an Addon Dependency, you need to enter the Jar File Name without the .jar at the end. If it is a normal Plugin Dependency, you need to enter the Plugin Name
    private String name;
    private String link;
    private boolean isOptional = false;
    private boolean isAddonDependency = false;

    public PluginDependency(String name) {
        this.name = name;
        this.link = null;
    }

    public PluginDependency(String name, String link, boolean isOptional, boolean isAddonDependency) {
        this.name = name;
        this.link = link;
        this.isOptional = isOptional;
        this.isAddonDependency = isAddonDependency;
    }

    public String name() {
        return name;
    }

    public String group() {
        return name.split(":")[0];
    }

    public String artifact() {
        return name.split(":")[1];
    }

    public String version() {
        return name.split(":")[2];
    }

    public String link() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean optional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    public boolean addonDependency() {
        return isAddonDependency;
    }

    public void setAddonDependency(boolean addonDependency) {
        isAddonDependency = addonDependency;
    }
}
