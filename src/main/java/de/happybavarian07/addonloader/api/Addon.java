package de.happybavarian07.addonloader.api;/*
 * @Author HappyBavarian07
 * @Date 12.01.2022 | 17:23
 */

import java.util.ArrayList;
import java.util.List;

public abstract class Addon {
    public abstract String getName();

    public abstract String getVersion();

    public abstract String getDescription();

    public void onEnable() {}

    public void onDisable() {}

    public List<Dependency> getDependencies() { return new ArrayList<>(); }
}
