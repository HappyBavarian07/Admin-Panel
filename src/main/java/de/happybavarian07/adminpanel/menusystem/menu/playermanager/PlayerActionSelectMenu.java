package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.money.MoneyMenu;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions.PermissionListMenu;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions.PermissionListMode;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class PlayerActionSelectMenu extends Menu {

    public PlayerActionSelectMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.SelectPlayerAction", playerMenuUtility.getTarget());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PlayerActionSelectMenu";
    }

    @Override
    public int getSlots() {
        return 27;
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
        String path = "PlayerManager.ActionsMenu.";
        Player target = Bukkit.getPlayer(playerMenuUtility.getTargetUUID());
        Player player = playerMenuUtility.getOwner();

        registerButton(getSlot(path + "ActionsItem", 10),
                lgm.getItem(path + "ActionsItem", target, false),
                (p, e) -> new PlayerActionsMenu(playerMenuUtility).open(), null);

        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            registerButton(getSlot(path + "MoneyItem", 12),
                    lgm.getItem(path + "MoneyItem", target, false),
                    (p, e) -> new MoneyMenu(playerMenuUtility).open(), null);
        }

        registerButton(getSlot(path + "BanItem", 14),
                lgm.getItem(path + "BanItem", target, false),
                (p, e) -> new PlayerBanMenu(playerMenuUtility).open(), null);

        registerButton(getSlot(path + "KickItem", 16),
                lgm.getItem(path + "KickItem", target, false),
                (p, e) -> new PlayerKickMenu(playerMenuUtility).open(), null);

        if (AdminPanelMain.getPlugin().getWarningManager() != null) {
            registerButton(getSlot("PlayerManager.WarningMenuItem", 24),
                    lgm.getItem("PlayerManager.WarningMenuItem", target, false),
                    (p, e) -> new PlayerWarningMenu(playerMenuUtility, this).open(), null);
        }

        registerButton(getSlot(path + "PermissionItem", 4),
                lgm.getItem(path + "PermissionItem", target, false),
                (p, e) -> {
                    playerMenuUtility.addData("PermissionListMode", PermissionListMode.ALL);
                    playerMenuUtility.addData("SortQuery", "");
                    new PermissionListMenu(playerMenuUtility, this).open();
                }, null);

        registerButton(getSlot("General.Close", 26),
                lgm.getItem("General.Close", target, false),
                MenuAction.of((p, e) -> {
                    new PlayerSelectMenu(playerMenuUtility, new AdminPanelStartMenu(playerMenuUtility)).open();
                }).requires("AdminPanel.Button.Close"), null);
    }
}
