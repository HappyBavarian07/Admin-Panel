package de.happybavarian07.adminpanel.menusystem;/*
 * @Author HappyBavarian07
 * @Date 22.12.2022 | 17:26
 */

import org.bukkit.event.inventory.InventoryClickEvent;

public abstract class MenuAddon {
    /**
     * Gets called when initializing a Menu Addon
     */
    public MenuAddon() {}

    public abstract Menu getMenu();

    public abstract String getName();

    public abstract void setMenuAddonItems();

    public abstract void handleMenu(InventoryClickEvent event);

    public abstract void onOpenEvent();

    public abstract void onCloseEvent();
}
