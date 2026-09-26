package de.happybavarian07.adminpanel.menusystem.menu;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefixExtension;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

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
        Player player = playerMenuUtility.getOwner();

        registerButton(getSlot("ConfigReloadMenu.ReloadConfig", 10), 
            lgm.getItem("ConfigReloadMenu.ReloadConfig", player, false), 
            (p, e) -> {
                AdminPanelMain.getPlugin().reloadConfig();
                p.sendMessage(lgm.getMessage("ConfigReloadMenu.ConfigReloaded", p, true));
                this.open();
            }, null);

        registerButton(getSlot("ConfigReloadMenu.ReloadLanguages", 11), 
            lgm.getItem("ConfigReloadMenu.ReloadLanguages", player, false), 
            (p, e) -> {
                lgm.reloadLanguages(p, true);
                p.sendMessage(lgm.getMessage("ConfigReloadMenu.LanguagesReloaded", p, true));
                this.open();
            }, null);

        registerButton(getSlot("ConfigReloadMenu.ReloadData", 12), 
            lgm.getItem("ConfigReloadMenu.ReloadData", player, false), 
            (p, e) -> {
                AdminPanelMain plugin = AdminPanelMain.getPlugin();
                try {
                    plugin.getDataYML().load(new File(plugin.getDataFolder(), "data.yml"));
                    p.sendMessage(lgm.getMessage("ConfigReloadMenu.DataReloaded", p, true));
                } catch (IOException | InvalidConfigurationException ex) {
                    plugin.getFileLogger().writeToLog(Level.SEVERE, "Could not reload data.yml: " + ex.getMessage(), LogPrefixExtension.ADMINPANEL_MAIN, true);
                }
                this.open();
            }, null);

        registerButton(getSlot("ConfigReloadMenu.BackupAll", 14), 
            lgm.getItem("ConfigReloadMenu.BackupAll", player, false), 
            (p, e) -> {
                p.closeInventory();
                p.sendMessage(lgm.getMessage("ConfigReloadMenu.CreatingBackup", p, true));
                AdminPanelMain.getPlugin().getBackupManager().backupAllFileBackups();
                p.sendMessage(lgm.getMessage("ConfigReloadMenu.BackupCreated", p, true));
            }, null);

        registerButton(getSlot("ConfigReloadMenu.ReloadPlugin", 16), 
            lgm.getItem("ConfigReloadMenu.ReloadPlugin", player, false), 
            (p, e) -> {
                p.closeInventory();
                p.sendMessage(lgm.getMessage("ConfigReloadMenu.ReloadingPlugin", p, true));
                AdminPanelMain.getPlugin().getServer().getPluginManager().disablePlugin(AdminPanelMain.getPlugin());
                AdminPanelMain.getPlugin().getServer().getPluginManager().enablePlugin(AdminPanelMain.getPlugin());
            }, null);

        setFillerGlass();
    }
}
