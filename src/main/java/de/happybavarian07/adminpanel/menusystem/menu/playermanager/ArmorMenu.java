package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class ArmorMenu extends Menu {

    public ArmorMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Inventoryview");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.ArmorView", playerMenuUtility.getTarget());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "ArmorMenu";
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {

    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player target = playerMenuUtility.getTarget();
        if (target == null || !target.isOnline()) return;
        if (target.getInventory().getArmorContents()[0] != null) {
            inventory.setItem(0, target.getInventory().getArmorContents()[0]);
        } else {
            inventory.setItem(0, lgm.getItem("General.EmptySlot", target, false));
        }
        if (target.getInventory().getArmorContents()[1] != null) {
            inventory.setItem(1, target.getInventory().getArmorContents()[1]);
        } else {
            inventory.setItem(1, lgm.getItem("General.EmptySlot", target, false));
        }
        if (target.getInventory().getArmorContents()[2] != null) {
            inventory.setItem(2, target.getInventory().getArmorContents()[2]);
        } else {
            inventory.setItem(2, lgm.getItem("General.EmptySlot", target, false));
        }
        if (target.getInventory().getArmorContents()[3] != null) {
            inventory.setItem(3, target.getInventory().getArmorContents()[3]);
        } else {
            inventory.setItem(3, lgm.getItem("General.EmptySlot", target, false));
        }
        registerButton(7, lgm.getItem("General.Refresh", target, false),
                MenuAction.of((p, e) -> super.open()).requires("AdminPanel.Button.refresh"), null);
        registerButton(8, lgm.getItem("General.Back", target, false),
                MenuAction.of((p, e) -> new PlayerActionsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open()).requires("AdminPanel.Button.Close"), null);
    }
}
