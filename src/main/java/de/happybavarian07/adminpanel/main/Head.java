package de.happybavarian07.adminpanel.main;

import org.bukkit.inventory.ItemStack;

public enum Head {
    WORLD("1289d5b178626ea23d0b0c3d2df5c085e8375056bf685b5ed5bb477fe8472d94", "World Item"),
    BLANK_GREEN("a3e9f4dbadde0f727c5803d75d8bb378fb9fcb4b60d33bec19092a3a2e7b07a9", "GameRule ON"),
    BLANK_RED("c65f3bae0d203ba16fe1dc3d1307a86a638be924471f23e82abd9d78f8a3fca", "GameRule OFF");

    private final String id;
    private final String texture;

    Head(String texture, String id) {
        this.texture = texture;
        this.id = id;
    }

    public String getTexture() {
        return texture;
    }

    public String getId() {
        return id;
    }

    public ItemStack getAsItem() {
        return AdminPanelMain.getAPI().createSkull(this, getId());
    }
}
