package de.happybavarian07.menusystem;

import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.main.LanguageManager;

public abstract class PaginatedMenu extends Menu {

    protected int page = 0;

    // 28 empty slots per page
    protected int maxItemsPerPage = 28;

    protected int index = 0;

    public PaginatedMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    //Set the border and menu buttons for the menu
    public void addMenuBorder() {
        LanguageManager lgm = AdminPanelMain.getPlugin().getLanguageManager();
        inventory.setItem(getSlot("General.Left", 48), lgm.getItem("General.Left", null, false));

        inventory.setItem(getSlot("General.Close", 49), lgm.getItem("General.Close", null, false));

        inventory.setItem(getSlot("General.Right", 50), lgm.getItem("General.Right", null, false));

        inventory.setItem(getSlot("General.Refresh", 51), lgm.getItem("General.Refresh", null, false));

        for (int i = 0; i < 10; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, super.FILLER);
            }
        }

        inventory.setItem(17, super.FILLER);
        inventory.setItem(18, super.FILLER);
        inventory.setItem(26, super.FILLER);
        inventory.setItem(27, super.FILLER);
        inventory.setItem(35, super.FILLER);
        inventory.setItem(36, super.FILLER);

        for (int i = 44; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, super.FILLER);
            }
        }
    }

    @Override
    public void setMenuItems() {

    }
}
