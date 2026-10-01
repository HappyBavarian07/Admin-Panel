package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

/*
 * @Author HappyBavarian07
 * @Date 02.09.2021
 */

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.plugins.*;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.utils.PluginUtils;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.Plugin;

public class PluginSettingsMenu extends Menu {
    private final PluginUtils pluginUtils;
    private final Plugin currentPlugin;

    public PluginSettingsMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        this.pluginUtils = new PluginUtils();
        this.currentPlugin = playerMenuUtility.getData("CurrentSelectedPlugin", Plugin.class);
        setOpeningPermission("AdminPanel.PluginManager.PluginSettings.Open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PluginManager.Settings", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PluginSettingsMenu";
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
        String path = "PluginManager.Settings.";

        // Items
        registerButton(getSlot("PluginManager.PluginDescription", 5), lgm.getItem("PluginManager.PluginDescription", player, false),
                MenuAction.of((p, e) -> {
                    playerMenuUtility.addData("CurrentSelectedPlugin", currentPlugin);
                    new PluginDescriptionMenu(playerMenuUtility).open();
                }).requires("AdminPanel.PluginManager.PluginSettings.PluginDescription"), null);
        registerButton(getSlot(path + "Enable", 10), lgm.getItem(path + "Enable", player, false),
                MenuAction.of((p, e) -> {
                    PluginEnableEvent event = new PluginEnableEvent(p, currentPlugin);
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(event);
                        if (!event.isCancelled() && !currentPlugin.isEnabled()) Bukkit.getPluginManager().enablePlugin(currentPlugin);
                    } catch (NotAPanelEventException ex) { AdminPanelMain.logError("A plugin settings action was not a valid panel event", ex); }
                }).requires("AdminPanel.PluginManager.PluginSettings.Enable"), null);
        registerButton(getSlot(path + "Disable", 11), lgm.getItem(path + "Disable", player, false),
                MenuAction.of((p, e) -> {
                    PluginDisableEvent event = new PluginDisableEvent(p, currentPlugin);
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(event);
                        if (!event.isCancelled() && currentPlugin.isEnabled()) Bukkit.getPluginManager().disablePlugin(currentPlugin);
                    } catch (NotAPanelEventException ex) { AdminPanelMain.logError("A plugin settings action was not a valid panel event", ex); }
                }).requires("AdminPanel.PluginManager.PluginSettings.Disable"), null);
        registerButton(getSlot(path + "Reload", 12), lgm.getItem(path + "Reload", player, false),
                MenuAction.of((p, e) -> {
                    PluginReloadEvent event = new PluginReloadEvent(p, currentPlugin);
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(event);
                        if (!event.isCancelled() && currentPlugin.isEnabled()) pluginUtils.reload(currentPlugin);
                    } catch (NotAPanelEventException ex) { AdminPanelMain.logError("A plugin settings action was not a valid panel event", ex); }
                }).requires("AdminPanel.PluginManager.PluginSettings.Reload"), null);
        registerButton(getSlot(path + "Restart", 3), lgm.getItem(path + "Restart", player, false),
                MenuAction.of((p, e) -> {
                    PluginRestartEvent event = new PluginRestartEvent(p, currentPlugin);
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(event);
                        if (!event.isCancelled() && currentPlugin.isEnabled()) {
                            Bukkit.getPluginManager().disablePlugin(currentPlugin);
                            Bukkit.getPluginManager().enablePlugin(currentPlugin);
                        }
                    } catch (NotAPanelEventException ex) { AdminPanelMain.logError("A plugin settings action was not a valid panel event", ex); }
                }).requires("AdminPanel.PluginManager.PluginSettings.Restart"), null);
        registerButton(getSlot(path + "Unload", 13), lgm.getItem(path + "Unload", player, false),
                MenuAction.of((p, e) -> {
                    PluginUnloadEvent event = new PluginUnloadEvent(p, currentPlugin);
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(event);
                        if (!event.isCancelled() && pluginUtils.getPluginByName(currentPlugin.getName()) != null) pluginUtils.unload(currentPlugin);
                    } catch (NotAPanelEventException ex) { AdminPanelMain.logError("A plugin settings action was not a valid panel event", ex); }
                }).requires("AdminPanel.PluginManager.PluginSettings.Unload"), null);
        registerButton(getSlot(path + "Unload", 14), lgm.getItem(path + "Load", player, false),
                MenuAction.of((p, e) -> {
                    PluginLoadEvent event = new PluginLoadEvent(p, currentPlugin);
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(event);
                        if (!event.isCancelled() && pluginUtils.getPluginByName(currentPlugin.getName()) == null) pluginUtils.load(currentPlugin);
                    } catch (NotAPanelEventException ex) { AdminPanelMain.logError("A plugin settings action was not a valid panel event", ex); }
                }).requires("AdminPanel.PluginManager.PluginSettings.Load"), null);
        registerButton(getSlot(path + "Commands.Item", 15), lgm.getItem(path + "Commands.Item", player, false),
                MenuAction.of((p, e) ->
                        new PluginCommandsListMenu(playerMenuUtility, new PluginSettingsMenu(playerMenuUtility)).open()).requires("AdminPanel.PluginManager.PluginSettings.Permissions"), null);
        registerButton(getSlot(path + "Permissions.Item", 16), lgm.getItem(path + "Permissions.Item", player, false),
                MenuAction.of((p, e) ->
                        new PluginPermissionsListMenu(playerMenuUtility, this).open()).requires("AdminPanel.PluginManager.PluginSettings.Commands"), null);
        registerButton(getSlot("General.Close", 26), lgm.getItem("General.Close", player, false),
                MenuAction.of((p, e) ->
                        new PluginSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), new AdminPanelStartMenu(playerMenuUtility)).open()).requires("AdminPanel.Button.Close"), null);
    }
}
