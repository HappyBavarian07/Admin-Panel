package de.happybavarian07.adminpanel.addonloader.utils;/*
 * @Author HappyBavarian07
 * @Date 12.01.2022 | 17:29
 */

import org.jetbrains.annotations.Nullable;

public class Dependency {
    // If it is an Addon Dependency, you need to enter the Jar File Name without the .jar at the end. If it is a normal Plugin Dependency, you need to enter the Plugin Name
    private String name;
    private @Nullable String link;
    private boolean isOptional = false;
    private boolean isAddonDependency = false;

    public Dependency(String name) {
        this.name = name;
        this.link = null;
    }

    public Dependency(String name, @Nullable String link, boolean isOptional, boolean isAddonDependency) {
        this.name = name;
        this.link = link;
        this.isOptional = isOptional;
        this.isAddonDependency = isAddonDependency;
    }

    public String getName() {
        return name;
    }

    public @Nullable String getLink() {
        return link;
    }

    public void setLink(@Nullable String link) {
        this.link = link;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    public boolean isAddonDependency() {
        return isAddonDependency;
    }

    public void setAddonDependency(boolean addonDependency) {
        isAddonDependency = addonDependency;
    }
}
