package de.happybavarian07.addonloader.api;/*
 * @Author HappyBavarian07
 * @Date 12.01.2022 | 17:29
 */

import org.jetbrains.annotations.Nullable;

public class Dependency {
    private String name;
    private @Nullable String link;

    public Dependency(String name) {
        this.name = name;
        this.link = null;
    }

    public Dependency(String name, @Nullable String link) {
        this.name = name;
        this.link = link;
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
}
