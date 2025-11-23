package de.happybavarian07.adminpanel.menusystem.menu.languagemigration;

import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

/*
 * @Author HappyBavarian07
 * @Date Juli 14, 2025 | 19:43
 */
public class LanguageMigratorMainMenu extends Menu {
    public LanguageMigratorMainMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("LanguageMigrator.MainMenu", playerMenuUtility.getOwner());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "LanguageMigratorMainMenu";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent inventoryClickEvent) {
        Player player = (Player) inventoryClickEvent.getWhoClicked();
        ItemStack clickedItem = inventoryClickEvent.getCurrentItem();

        if (clickedItem.isSimilar(lgm.getItem("LanguageMigrator.NormalMigrate", player, false))) {
            new NormalLanguageSelectMenu(playerMenuUtility, this).open();
        } else if (clickedItem.isSimilar(lgm.getItem("LanguageMigrator.CustomMigrate", player, false))) {
            //new CustomLanguageSelectMenu(playerMenuUtility, this).open();
        }
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent inventoryOpenEvent) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent inventoryCloseEvent) {

    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        // just for normal migrations where the player has to select a file which has a resource file
        inventory.setItem(getSlot("LanguageMigrator.NormalMigrate", 11), lgm.getItem("LanguageMigrator.NormalMigrate", playerMenuUtility.getOwner(), false));
        // the player can select both real and resource file thus creating custom matches.
        inventory.setItem(getSlot("LanguageMigrator.CustomMigrate", 15), lgm.getItem("LanguageMigrator.CustomMigrate", playerMenuUtility.getOwner(), false));
        // TODO might work on an option to create entirely new files using a translation service, but for that we would have to add an api key to the config, because i aint paying xd.
    }
}
