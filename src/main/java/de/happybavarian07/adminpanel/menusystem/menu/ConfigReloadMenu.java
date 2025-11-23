package de.happybavarian07.adminpanel.menusystem.menu;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class ConfigReloadMenu extends Menu {

    public ConfigReloadMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.ReloadConfig");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("ConfigReloadMenu", playerMenuUtility.getOwner());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "ConfigReloadMenu";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent inventoryClickEvent) {
        Player player = (Player) inventoryClickEvent.getWhoClicked();
        ItemStack clickedItem = inventoryClickEvent.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }

        if (clickedItem.isSimilar(lgm.getItem("ConfigReloadMenu.ReloadConfig", player, false))) {
            AdminPanelMain.getPlugin().reloadConfig();
            player.sendMessage(lgm.getMessage("ConfigReloadMenu.ConfigReloaded", player, true));
            super.open();
        } else if (clickedItem.isSimilar(lgm.getItem("ConfigReloadMenu.ReloadLanguages", player, false))) {
            lgm.reloadLanguages(player, true);
            player.sendMessage(lgm.getMessage("ConfigReloadMenu.LanguagesReloaded", player, true));
            super.open();
        } else if (clickedItem.isSimilar(lgm.getItem("ConfigReloadMenu.ReloadData", player, false))) {
            AdminPanelMain.getPlugin().getDataService().reload();
            player.sendMessage(lgm.getMessage("ConfigReloadMenu.DataReloaded", player, true));
            super.open();
        } else if (clickedItem.isSimilar(lgm.getItem("ConfigReloadMenu.BackupAll", player, false))) {
            player.closeInventory();
            player.sendMessage(lgm.getMessage("ConfigReloadMenu.CreatingBackup", player, true));
            AdminPanelMain.getPlugin().getBackupManager().backupAllFileBackups();
            player.sendMessage(lgm.getMessage("ConfigReloadMenu.BackupCreated", player, true));
        } else if (clickedItem.isSimilar(lgm.getItem("ConfigReloadMenu.ReloadPlugin", player, false))) {
            player.closeInventory();
            player.sendMessage(lgm.getMessage("ConfigReloadMenu.ReloadingPlugin", player, true));
            AdminPanelMain.getPlugin().getServer().getPluginManager().disablePlugin(AdminPanelMain.getPlugin());
            AdminPanelMain.getPlugin().getServer().getPluginManager().enablePlugin(AdminPanelMain.getPlugin());
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
        Player player = playerMenuUtility.getOwner();

        inventory.setItem(getSlot("ConfigReloadMenu.ReloadConfig", 10), lgm.getItem("ConfigReloadMenu.ReloadConfig", player, false));
        inventory.setItem(getSlot("ConfigReloadMenu.ReloadLanguages", 11), lgm.getItem("ConfigReloadMenu.ReloadLanguages", player, false));
        inventory.setItem(getSlot("ConfigReloadMenu.ReloadData", 12), lgm.getItem("ConfigReloadMenu.ReloadData", player, false));
        inventory.setItem(getSlot("ConfigReloadMenu.BackupAll", 14), lgm.getItem("ConfigReloadMenu.BackupAll", player, false));
        inventory.setItem(getSlot("ConfigReloadMenu.ReloadPlugin", 16), lgm.getItem("ConfigReloadMenu.ReloadPlugin", player, false));

        setFillerGlass();
    }
}
