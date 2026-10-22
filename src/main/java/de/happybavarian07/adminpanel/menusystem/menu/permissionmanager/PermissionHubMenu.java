package de.happybavarian07.adminpanel.menusystem.menu.permissionmanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

/** The start of the permission manager: players, groups, the audit log and a reload. */
public class PermissionHubMenu extends Menu {
    public static final String PERMISSION = "AdminPanel.Permissions.Menu";
    private static final String PATH = "PermissionManager.Hub.";

    public PermissionHubMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission(PERMISSION);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PermissionManager.Hub", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PermissionHubMenu";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        registerButton(getSlot(PATH + "Players", 10), lgm.getItem(PATH + "Players", player, false),
                MenuAction.of((p, e) -> new PermissionPlayersMenu(playerMenuUtility, this).open()).requires("AdminPanel.Permissions.User"), null);
        registerButton(getSlot(PATH + "Groups", 12), lgm.getItem(PATH + "Groups", player, false),
                MenuAction.of((p, e) -> new PermissionGroupsMenu(playerMenuUtility, this).open()).requires("AdminPanel.Permissions.Group"), null);
        registerButton(getSlot(PATH + "Browse", 14), lgm.getItem(PATH + "Browse", player, false),
                MenuAction.of((p, e) -> new PermissionBrowseMenu(playerMenuUtility, this).open()).requires("AdminPanel.Permissions.Search"), null);
        registerButton(getSlot(PATH + "Audit", 16), lgm.getItem(PATH + "Audit", player, false),
                MenuAction.of((p, e) -> new PermissionAuditMenu(playerMenuUtility, this).open()).requires("AdminPanel.Permissions.Audit"), null);
        registerButton(getSlot(PATH + "Reload", 22), lgm.getItem(PATH + "Reload", player, false),
                MenuAction.of((p, e) -> {
                    AdminPanelMain.getPlugin().getPermissionService().reload(p);
                    p.sendMessage(lgm.getMessage("Permissions.Command.Reloaded", p, true));
                }).requires("AdminPanel.Permissions.Reload"), null);
        registerButton(getSlot("General.Close", 26), lgm.getItem("General.Back", player, false),
                MenuAction.of((p, e) -> new AdminPanelStartMenu(playerMenuUtility).open()).requires("AdminPanel.Button.Close"), null);
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
