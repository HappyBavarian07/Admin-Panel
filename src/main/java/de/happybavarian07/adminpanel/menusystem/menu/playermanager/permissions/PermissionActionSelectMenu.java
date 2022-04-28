package de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions;/*
 * @Author HappyBavarian07
 * @Date 30.10.2021 | 15:42
 */

import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.PlayerActionSelectMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PermissionActionSelectMenu extends Menu {
    private final PlayerMenuUtility playerMenuUtility;
    private final UUID targetUUID;

    public PermissionActionSelectMenu(PlayerMenuUtility playerMenuUtility, UUID targetUUID) {
        super(playerMenuUtility);
        this.playerMenuUtility = playerMenuUtility;
        this.targetUUID = targetUUID;
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.Open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.Permissions.ActionSelectMenu", null);
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        InventoryAction action2 = e.getAction();
        String path = "PlayerManager.ActionsMenu.Permissions.";
        Player player = (Player) e.getWhoClicked();
        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);
        ItemStack item = e.getCurrentItem();
        if (item != null) {
            if (item.equals(lgm.getItem(path + "Add", player, false))) {
                if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.Add")) {
                    player.sendMessage(noPerms);
                    return;
                }
                new PermissionListMenu(playerMenuUtility, PermissionAction.ADD, PermissionListMode.ALL, "", targetUUID).open();
            } else if (item.equals(lgm.getItem(path + "Remove", player, false))) {
                if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.Remove")) {
                    player.sendMessage(noPerms);
                    return;
                }
                new PermissionListMenu(playerMenuUtility, PermissionAction.REMOVE, PermissionListMode.ALL, "", targetUUID).open();
            } else if (item.equals(lgm.getItem(path + "Info", player, false))) {
                if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.Info")) {
                    player.sendMessage(noPerms);
                    return;
                }
                new PermissionListMenu(playerMenuUtility, PermissionAction.INFO, PermissionListMode.ALL, "", targetUUID).open();
            } else if (item.equals(lgm.getItem(path + "List", player, false))) {
                if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.List")) {
                    player.sendMessage(noPerms);
                    return;
                }
                new PermissionListMenu(playerMenuUtility, PermissionAction.LIST, PermissionListMode.ALL, "", targetUUID).open();
            } else if (item.equals(lgm.getItem("General.Close", null, false))) {
                if (!player.hasPermission("AdminPanel.Button.Close")) {
                    player.sendMessage(noPerms);
                    return;
                }
                new PlayerActionSelectMenu(playerMenuUtility, targetUUID).open();
            }
        }
    }

    @Override
    public void setMenuItems() {
        String path = "PlayerManager.ActionsMenu.Permissions.";
        Player player = playerMenuUtility.getOwner();
        setFillerGlass();
        inventory.setItem(getSlot(path + "Add", 10), lgm.getItem(path + "Add", player, false));
        inventory.setItem(getSlot(path + "Info", 12), lgm.getItem(path + "Info", player, false));
        inventory.setItem(getSlot(path + "List", 14), lgm.getItem(path + "List", player, false));
        inventory.setItem(getSlot(path + "Remove", 16), lgm.getItem(path + "Remove", player, false));
        inventory.setItem(getSlot("General.Close", 26), lgm.getItem("General.Close", player, false));
    }
}
