package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.money.MoneyMenu;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions.PermissionActionSelectMenu;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

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
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Player target = Bukkit.getPlayer(playerMenuUtility.getTargetUUID());
        ItemStack item = e.getCurrentItem();

        if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.ActionsItem", target, false))) {
            new PlayerActionsMenu(playerMenuUtility).open();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.MoneyItem", target, false))) {
            new MoneyMenu(playerMenuUtility).open();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.BanItem", target, false))) {
            new PlayerBanMenu(playerMenuUtility).open();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.KickItem", target, false))) {
            new PlayerKickMenu(playerMenuUtility).open();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.PermissionItem", target, false))) {
            new PermissionActionSelectMenu(playerMenuUtility).open();
        } else if (item.equals(lgm.getItem("PlayerManager.WarningMenuItem", target, false))) {
            //new PlayerWarningMenu(playerMenuUtility, targetUUID).open();
            player.sendMessage(Utils.chat("!!!UNDER DEVELOPMENT!!!"));
        } else if (item.equals(lgm.getItem("General.Close", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(lgm.getMessage("Player.General.NoPermissions", player, true));
                return;
            }
            new PlayerSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        }
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {

    }

    @Override
    public void setMenuItems() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, super.FILLER);
        }
        String path = "PlayerManager.ActionsMenu.";
        Player target = Bukkit.getPlayer(playerMenuUtility.getTargetUUID());
        inventory.setItem(getSlot(path + "ActionsItem", 10), lgm.getItem(path + "ActionsItem", target, false));
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            inventory.setItem(getSlot(path + "MoneyItem", 12), lgm.getItem(path + "MoneyItem", target, false));
        }
        inventory.setItem(getSlot(path + "BanItem", 14), lgm.getItem(path + "BanItem", target, false));
        inventory.setItem(getSlot(path + "KickItem", 16), lgm.getItem(path + "KickItem", target, false));
        if (plugin.getWarningManager() != null)
            inventory.setItem(getSlot("PlayerManager.WarningMenuItem", 24), lgm.getItem("PlayerManager.WarningMenuItem", target, false));
        inventory.setItem(getSlot(path + "PermissionItem", 4), lgm.getItem(path + "PermissionItem", target, false));
        inventory.setItem(getSlot("General.Close", 26), lgm.getItem("General.Close", target, false));
    }
}
