package de.happybavarian07.main;

import org.bukkit.inventory.ItemStack;

public enum Head {
    WORLD("YzY5MTk2YjMzMGM2Yjg5NjJmMjNhZDU2MjdmYjZlY2NlNDcyZWFmNWM5ZDQ0Zjc5MWY2NzA5YzdkMGY0ZGVjZSJ9fX0=", "World Item"),
    BLANK_GREEN("MzI5NmQzZTE0OTNmYTMyZDgyN2EzNjM1YTY4M2U1YmRlZDY0OTE0ZDc1ZTczYWFjZGNjYmE0NmQ4ZmQ5MCJ9fX0=", "GameRule ON"),
    BLANK_RED("M2NjNDcwYWUyNjMxZWZkZmFmOTY3YjM2OTQxM2JjMjQ1MWNkN2EzOTQ2NWRhNzgzNmE2YzdhMTRlODc3In19fQ==", "GameRule OFF");

    private final String id;
    private final String texture;
    private final String prefix = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv";

    Head(String texture, String id) {
        this.texture = texture;
        this.id = id;
    }

    public String getTexture() {
        return texture;
    }

    public String getTexturePrefix() {
        return prefix;
    }

    public String getId() {
        return id;
    }

    public String getFullTexture() {
        return prefix + texture;
    }

    public ItemStack getAsItem() {
        return AdminPanelMain.getAPI().createSkull(this, getId());
    }
}
