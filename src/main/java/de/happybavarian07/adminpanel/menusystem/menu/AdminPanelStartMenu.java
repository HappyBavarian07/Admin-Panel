package de.happybavarian07.adminpanel.menusystem.menu;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.PlayerSelectMenu;
import de.happybavarian07.adminpanel.menusystem.menu.pluginmanager.PluginSelectMenu;
import de.happybavarian07.adminpanel.menusystem.menu.servermanager.DangerZoneMenu;
import de.happybavarian07.adminpanel.menusystem.menu.servermanager.ServerManagerMenu;
import de.happybavarian07.adminpanel.menusystem.menu.worldmanager.WorldSelectMenu;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.PermissionAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class AdminPanelStartMenu extends Menu {

    public AdminPanelStartMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("StartMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "AdminPanelStartMenu";
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
        String path = "StartMenu.";

        registerButton(getSlot(path + "ReloadConfig", 18), 
            lgm.getItem(path + "ReloadConfig", player, false), 
            new PermissionAction("AdminPanel.ReloadConfig", (p, e) -> {
                AdminPanelMain.getAPI().reloadConfigurationFiles(p);
                this.open();
            }, lgm), null);

        registerButton(getSlot(path + "DangerZone", 4), 
            lgm.getItem(path + "DangerZone", player, false), 
            new PermissionAction("AdminPanel.DangerZone.open", (p, e) -> {
                new DangerZoneMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), this).open();
            }, lgm), null);

        registerButton(getSlot(path + "WorldManager", 10), 
            lgm.getItem(path + "WorldManager", player, false), 
            new PermissionAction("AdminPanel.WorldManager.open", (p, e) -> {
                new WorldSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), this).open();
            }, lgm), null);

        registerButton(getSlot(path + "PlayerManager", 12), 
            lgm.getItem(path + "PlayerManager", player, false), 
            new PermissionAction("AdminPanel.PlayerManager.open", (p, e) -> {
                new PlayerSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), this).open();
            }, lgm), null);

        registerButton(getSlot(path + "PluginManager", 13), 
            lgm.getItem(path + "PluginManager", player, false), 
            new PermissionAction("AdminPanel.PluginManager.open", (p, e) -> {
                new PluginSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), this).open();
            }, lgm), null);

        registerButton(getSlot(path + "ServerManager", 16), 
            lgm.getItem(path + "ServerManager", player, false), 
            new PermissionAction("AdminPanel.ServerManagment.Open", (p, e) -> {
                new ServerManagerMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open();
            }, lgm), null);

        registerButton(getSlot(path + "SwitchLanguageMenuItem", 26), 
            lgm.getItem(path + "SwitchLanguageMenuItem", player, false), 
            new PermissionAction("AdminPanel.SwitchLanguage", (p, e) -> {
                new TempLanguageSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), this).open();
            }, lgm), null);

        inventory.setItem(getSlot(path + "HintItem", 22), lgm.getItem(path + "HintItem", player, false));
    }
}
