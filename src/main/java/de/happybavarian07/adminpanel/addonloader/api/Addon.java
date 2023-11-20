package de.happybavarian07.adminpanel.addonloader.api;/*
 * @Author HappyBavarian07
 * @Date 12.01.2022 | 17:23
 */

import java.util.ArrayList;
import java.util.List;

/**
 * The Addon Class
 */
public abstract class Addon {
    /**
     * Returns the Name of the Addon
     * @return The Name of the Addon
     */
    public abstract String getName();

    /**
     * Returns the Version of the Addon
     * @return The Version of the Addon
     */
    public abstract String getVersion();

    /**
     * Returns the Author of the Addon
     * @return The Author of the Addon
     */
    public abstract String getDescription();

    /**
     * Executed when Plugin enables
     */
    public void onEnable() {}

    /**
     * Executed when Plugin disables
     */
    public void onDisable() {}

    /**
     * Returns the Dependencies of the Addon
     * @return The Dependencies of the Addon
     */
    public List<Dependency> getDependencies() { return new ArrayList<>(); }
}
