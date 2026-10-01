package de.happybavarian07.adminpanel.menusystem.menu.languagemigration;

import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.entity.Player;
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
    public void handleOpenMenu(InventoryOpenEvent inventoryOpenEvent) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent inventoryCloseEvent) {

    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();

        registerButton(getSlot("LanguageMigrator.NormalMigrate", 11), 
            lgm.getItem("LanguageMigrator.NormalMigrate", player, false), 
            MenuAction.of((p, e) -> {
                new NormalLanguageSelectMenu(playerMenuUtility, this).open();
            }).requires("AdminPanel.LanguageMigrator.Open"), null);

        registerButton(getSlot("LanguageMigrator.CustomMigrate", 15), 
            lgm.getItem("LanguageMigrator.CustomMigrate", player, false), 
            MenuAction.of((p, e) -> {
                // CustomLanguageSelectMenu not yet implemented
            }).requires("AdminPanel.LanguageMigrator.Open"), null);
    }
}
