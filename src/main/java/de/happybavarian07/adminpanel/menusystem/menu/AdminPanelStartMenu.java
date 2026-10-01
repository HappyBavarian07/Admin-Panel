package de.happybavarian07.adminpanel.menusystem.menu;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.PlayerSelectMenu;
import de.happybavarian07.adminpanel.menusystem.menu.pluginmanager.PluginSelectMenu;
import de.happybavarian07.adminpanel.menusystem.menu.servermanager.DangerZoneMenu;
import de.happybavarian07.adminpanel.menusystem.menu.servermanager.ServerManagerMenu;
import de.happybavarian07.adminpanel.menusystem.menu.worldmanager.WorldSelectMenu;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
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
            MenuAction.of((p, e) -> {
                AdminPanelMain.getAPI().reloadConfigurationFiles(p);
                this.open();
            }).requires("AdminPanel.ReloadConfig"), null);

        registerButton(getSlot(path + "DangerZone", 4), 
            lgm.getItem(path + "DangerZone", player, false), 
            MenuAction.of((p, e) -> {
                new DangerZoneMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), this).open();
            }).requires("AdminPanel.DangerZone.open"), null);

        registerButton(getSlot(path + "WorldManager", 10), 
            lgm.getItem(path + "WorldManager", player, false), 
            MenuAction.of((p, e) -> {
                new WorldSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), this).open();
            }).requires("AdminPanel.WorldManager.open"), null);

        registerButton(getSlot(path + "PlayerManager", 12), 
            lgm.getItem(path + "PlayerManager", player, false), 
            MenuAction.of((p, e) -> {
                new PlayerSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), this).open();
            }).requires("AdminPanel.PlayerManager.open"), null);

        registerButton(getSlot(path + "PluginManager", 13), 
            lgm.getItem(path + "PluginManager", player, false), 
            MenuAction.of((p, e) -> {
                new PluginSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), this).open();
            }).requires("AdminPanel.PluginManager.open"), null);

        registerButton(getSlot(path + "ServerManager", 16), 
            lgm.getItem(path + "ServerManager", player, false), 
            MenuAction.of((p, e) -> {
                new ServerManagerMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open();
            }).requires("AdminPanel.ServerManagment.Open"), null);

        registerButton(getSlot(path + "SwitchLanguageMenuItem", 26), 
            lgm.getItem(path + "SwitchLanguageMenuItem", player, false), 
            MenuAction.of((p, e) -> {
                new TempLanguageSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), this).open();
            }).requires("AdminPanel.SwitchLanguage"), null);

        inventory.setItem(getSlot(path + "HintItem", 22), lgm.getItem(path + "HintItem", player, false));
    }
}
