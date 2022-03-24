package de.happybavarian07.menusystem.menu.playermanager;

import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.menusystem.menu.playermanager.money.MoneyMenu;
import de.happybavarian07.menusystem.menu.playermanager.permissions.PermissionAction;
import de.happybavarian07.menusystem.menu.playermanager.permissions.PermissionActionSelectMenu;
import de.happybavarian07.menusystem.menu.playermanager.permissions.PermissionListMenu;
import de.happybavarian07.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerActionSelectMenu extends Menu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final UUID targetUUID;

    public PlayerActionSelectMenu(PlayerMenuUtility playerMenuUtility, UUID targetUUID) {
        super(playerMenuUtility);
        this.targetUUID = targetUUID;
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.SelectPlayerAction", Bukkit.getPlayer(targetUUID));
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Player target = Bukkit.getPlayer(targetUUID);
        ItemStack item = e.getCurrentItem();

        if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.ActionsItem", target))) {
            new PlayerActionsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), targetUUID).open();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.MoneyItem", target))) {
            new MoneyMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), targetUUID).open();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.BanItem", target))) {
            new PlayerBanMenu(playerMenuUtility, targetUUID).open();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.KickItem", target))) {
            new PlayerKickMenu(playerMenuUtility, targetUUID).open();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.PermissionItem", target))) {
            new PermissionActionSelectMenu(playerMenuUtility, targetUUID).open();
        } else if (item.equals(lgm.getItem("General.Close", null))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(lgm.getMessage("Player.General.NoPermissions", player));
                return;
            }
            new PlayerSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        }
    }

    @Override
    public void setMenuItems() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, super.FILLER);
        }
        String path = "PlayerManager.ActionsMenu.";
        Player target = Bukkit.getPlayer(targetUUID);
        inventory.setItem(getSlot(path + "ActionsItem", 10), lgm.getItem(path + "ActionsItem", target));
        if(Bukkit.getPluginManager().getPlugin("Vault") != null) {
            inventory.setItem(getSlot(path + "MoneyItem", 12), lgm.getItem(path + "MoneyItem", target));
        }
        inventory.setItem(getSlot(path + "BanItem", 14), lgm.getItem(path + "BanItem", target));
        inventory.setItem(getSlot(path + "KickItem", 16), lgm.getItem(path + "KickItem", target));
        inventory.setItem(getSlot(path + "PermissionItem", 4), lgm.getItem(path + "PermissionItem", target));
        inventory.setItem(getSlot("General.Close", 26), lgm.getItem("General.Close", target));
    }
}
