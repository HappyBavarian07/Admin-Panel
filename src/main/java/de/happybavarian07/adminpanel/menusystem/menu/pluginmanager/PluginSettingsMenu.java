package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

/*
 * @Author HappyBavarian07
 * @Date 02.09.2021
 */

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.plugins.*;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.utils.PluginUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
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
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String path = "PluginManager.Settings.";
        boolean enabled = currentPlugin.isEnabled();

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item == null || !item.hasItemMeta()) return;
        if (item.equals(lgm.getItem(path + "Enable", player, false))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Enable")) {
                player.sendMessage(noPerms);
                return;
            }
            PluginEnableEvent enableEvent = new PluginEnableEvent(player, currentPlugin);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(enableEvent);
                if (!enableEvent.isCancelled()) {
                    if (!enabled)
                        Bukkit.getPluginManager().enablePlugin(currentPlugin);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem( "PluginManager.PluginDescription", player, false))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.PluginDescription")) {
                player.sendMessage(noPerms);
                return;
            }
            // Open Plugin Description Menu and put current plugin in the playerdata
            playerMenuUtility.addData("CurrentSelectedPlugin", currentPlugin);
            new PluginDescriptionMenu(playerMenuUtility).open();
        } else if (item.equals(lgm.getItem(path + "Disable", player, false))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Disable")) {
                player.sendMessage(noPerms);
                return;
            }
            PluginDisableEvent disableEvent = new PluginDisableEvent(player, currentPlugin);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(disableEvent);
                if (!disableEvent.isCancelled()) {
                    if (enabled)
                        Bukkit.getPluginManager().disablePlugin(currentPlugin);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "Reload", player, false))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Reload")) {
                player.sendMessage(noPerms);
                return;
            }
            PluginReloadEvent reloadEvent = new PluginReloadEvent(player, currentPlugin);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(reloadEvent);
                if (!reloadEvent.isCancelled()) {
                    if (enabled)
                        pluginUtils.reload(currentPlugin);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "Restart", player, false))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Restart")) {
                player.sendMessage(noPerms);
                return;
            }
            PluginRestartEvent restartEvent = new PluginRestartEvent(player, currentPlugin);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(restartEvent);
                if (!restartEvent.isCancelled()) {
                    if (enabled) {
                        Bukkit.getPluginManager().disablePlugin(currentPlugin);
                        Bukkit.getPluginManager().enablePlugin(currentPlugin);
                    }
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "Unload", player, false))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Unload")) {
                player.sendMessage(noPerms);
                return;
            }
            PluginUnloadEvent unloadEvent = new PluginUnloadEvent(player, currentPlugin);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(unloadEvent);
                if (!unloadEvent.isCancelled()) {
                    if (pluginUtils.getPluginByName(currentPlugin.getName()) != null)
                        pluginUtils.unload(currentPlugin);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "Load", player, false))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Load")) {
                player.sendMessage(noPerms);
                return;
            }
            PluginLoadEvent loadEvent = new PluginLoadEvent(player, currentPlugin);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(loadEvent);
                if (!loadEvent.isCancelled()) {
                    if (pluginUtils.getPluginByName(currentPlugin.getName()) == null)
                        pluginUtils.load(currentPlugin);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "Commands.Item", player, false))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Permissions")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginCommandsListMenu(playerMenuUtility, currentPlugin).open();
        } else if (item.equals(lgm.getItem(path + "Permissions.Item", player, false))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Commands")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginPermissionsListMenu(playerMenuUtility, currentPlugin).open();
        } else if (item.equals(lgm.getItem("General.Close", player, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
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
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "PluginManager.Settings.";

        // Items
        inventory.setItem(getSlot("PluginManager.PluginDescription", 5), lgm.getItem("PluginManager.PluginDescription", player, false));
        inventory.setItem(getSlot(path + "Enable", 10), lgm.getItem(path + "Enable", player, false));
        inventory.setItem(getSlot(path + "Disable", 11), lgm.getItem(path + "Disable", player, false));
        inventory.setItem(getSlot(path + "Reload", 12), lgm.getItem(path + "Reload", player, false));
        inventory.setItem(getSlot(path + "Restart", 3), lgm.getItem(path + "Restart", player, false));
        inventory.setItem(getSlot(path + "Unload", 13), lgm.getItem(path + "Unload", player, false));
        inventory.setItem(getSlot(path + "Unload", 14), lgm.getItem(path + "Load", player, false));
        inventory.setItem(getSlot(path + "Commands.Item", 15), lgm.getItem(path + "Commands.Item", player, false));
        inventory.setItem(getSlot(path + "Permissions.Item", 16), lgm.getItem(path + "Permissions.Item", player, false));

        inventory.setItem(getSlot("General.Close", 26), lgm.getItem("General.Close", player, false));
    }
}
