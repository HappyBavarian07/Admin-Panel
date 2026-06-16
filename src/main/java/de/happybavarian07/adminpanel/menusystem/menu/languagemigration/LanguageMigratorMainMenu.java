package de.happybavarian07.adminpanel.menusystem.menu.languagemigration;

import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.PermissionAction;
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
        // Logic is now handled by registered MenuActions in setMenuItems
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
            new PermissionAction("AdminPanel.LanguageMigrator.Open", (p, e) -> {
                new NormalLanguageSelectMenu(playerMenuUtility, this).open();
            }, lgm), null);

        registerButton(getSlot("LanguageMigrator.CustomMigrate", 15), 
            lgm.getItem("LanguageMigrator.CustomMigrate", player, false), 
            new PermissionAction("AdminPanel.LanguageMigrator.Open", (p, e) -> {
                // CustomLanguageSelectMenu not yet implemented
            }, lgm), null);
    }
}
