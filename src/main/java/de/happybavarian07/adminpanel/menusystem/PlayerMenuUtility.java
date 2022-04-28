package de.happybavarian07.adminpanel.menusystem;

import org.bukkit.entity.Player;

/*
Companion class to all menus. This is needed to pass information across the entire
 menu system no matter how many inventories are opened or closed.

 Each player has one of these objects, and only one.
 */

public class PlayerMenuUtility {

    private final Player owner;

    public PlayerMenuUtility(Player p) {
        this.owner = p;
    }

    public Player getOwner() {
        return owner;
    }
}

