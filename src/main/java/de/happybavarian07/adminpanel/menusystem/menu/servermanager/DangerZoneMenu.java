package de.happybavarian07.adminpanel.menusystem.menu.servermanager;

import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.PermissionAction;
import de.happybavarian07.coolstufflib.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class DangerZoneMenu extends Menu {

    public DangerZoneMenu(PlayerMenuUtility playerMenuUtility) {
        this(playerMenuUtility, null);
    }

    public DangerZoneMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission("AdminPanel.DangerZone.open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("DangerZoneMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "DangerZoneMenu";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        // Logic is now handled by registered MenuActions in setMenuItems
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
        Player player = playerMenuUtility.getOwner();
        String path = "DangerZoneMenu.";

        registerButton(getSlot(path + "ServerStop", 11), lgm.getItem(path + "ServerStop", player, false), new PermissionAction("AdminPanel.ServerStop", (player1, event) -> {
            Utils.openConfirmationMenu(
                    lgm.getMessage("DangerZoneMenu.StopReason", player1, true),
                    (player2, e) -> AdminPanelUtils.serverStop(60),
                    player1,
                    this
            );
        }), null);
        registerButton(getSlot(path + "ServerRestart", 15), lgm.getItem(path + "ServerRestart", player, false), new PermissionAction("AdminPanel.ServerRestart", (player1, event) -> {
            Utils.openConfirmationMenu(
                    lgm.getMessage("DangerZoneMenu.RestartReason", player1, true),
                    (player2, e) -> AdminPanelUtils.serverRestart(60),
                    player1,
                    this
            );
        }), null);
        registerButton(getSlot(path + "BackToMain", 22), lgm.getItem(path + "BackToMain", player, false), (player1, event) -> {
            closeAndReturnOrClose();
        }, null);
    }
}
