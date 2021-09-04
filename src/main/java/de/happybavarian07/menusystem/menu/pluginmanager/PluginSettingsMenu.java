package de.happybavarian07.menusystem.menu.pluginmanager;

/**
 * @Author HappyBavarian07
 * @Date 02.09.2021
 */

import de.happybavarian07.events.NotAPanelEventException;
import de.happybavarian07.events.plugins.*;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.utils.PluginUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class PluginSettingsMenu extends Menu {
    private final Main plugin = Main.getPlugin();
    private final PluginUtils pluginUtils;
    private final LanguageManager lgm = plugin.getLanguageManager();
    private final Plugin currentPlugin;

    public PluginSettingsMenu(PlayerMenuUtility playerMenuUtility, Plugin currentPlugin) {
        super(playerMenuUtility);
        this.pluginUtils = new PluginUtils();
        this.currentPlugin = currentPlugin;
        setOpeningPermission("AdminPanel.PluginManager.PluginSettings.Open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PluginManager.Settings", null);
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

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta()) return;
        if (item.equals(lgm.getItem(path + "Enable", player))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Enable")) {
                player.sendMessage(noPerms);
                return;
            }
            PluginEnableEvent enableEvent = new PluginEnableEvent(player, currentPlugin);
            try {
                Main.getAPI().callAdminPanelEvent(enableEvent);
                if(!enableEvent.isCancelled()) {
                    if(!enabled)
                        Bukkit.getPluginManager().enablePlugin(currentPlugin);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "Disable", player))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Disable")) {
                player.sendMessage(noPerms);
                return;
            }
            PluginDisableEvent disableEvent = new PluginDisableEvent(player, currentPlugin);
            try {
                Main.getAPI().callAdminPanelEvent(disableEvent);
                if(!disableEvent.isCancelled()) {
                    if(enabled)
                        Bukkit.getPluginManager().disablePlugin(currentPlugin);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "Reload", player))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Restart")) {
                player.sendMessage(noPerms);
                return;
            }
            PluginReloadEvent reloadEvent = new PluginReloadEvent(player, currentPlugin);
            try {
                Main.getAPI().callAdminPanelEvent(reloadEvent);
                if(!reloadEvent.isCancelled()) {
                    if(enabled)
                        pluginUtils.reload(currentPlugin);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "Unload", player))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Unload")) {
                player.sendMessage(noPerms);
                return;
            }
            PluginUnloadEvent unloadEvent = new PluginUnloadEvent(player, currentPlugin);
            try {
                Main.getAPI().callAdminPanelEvent(unloadEvent);
                if(!unloadEvent.isCancelled()) {
                    if(pluginUtils.getPluginByName(currentPlugin.getName()) != null)
                        pluginUtils.unload(currentPlugin);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "Load", player))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Load")) {
                player.sendMessage(noPerms);
                return;
            }
            PluginLoadEvent loadEvent = new PluginLoadEvent(player, currentPlugin);
            try {
                Main.getAPI().callAdminPanelEvent(loadEvent);
                if(!loadEvent.isCancelled()) {
                    if(pluginUtils.getPluginByName(currentPlugin.getName()) == null)
                        pluginUtils.load(currentPlugin);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "Commands.Item", player))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Permissions")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginCommandsListMenu(playerMenuUtility, currentPlugin).open();
        } else if (item.equals(lgm.getItem(path + "Permissions.Item", player))) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Commands")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginPermissionsListMenu(playerMenuUtility, currentPlugin).open();
        } else if (item.equals(lgm.getItem("General.Close", player))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginSelectMenu(Main.getAPI().getPlayerMenuUtility(player)).open();
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "PluginManager.Settings.";

        // Items
        inventory.setItem(10, lgm.getItem(path + "Enable", player));
        inventory.setItem(11, lgm.getItem(path + "Disable", player));
        inventory.setItem(12, lgm.getItem(path + "Reload", player));
        inventory.setItem(13, lgm.getItem(path + "Unload", player));
        inventory.setItem(14, lgm.getItem(path + "Load", player));
        inventory.setItem(15, lgm.getItem(path + "Commands.Item", player));
        inventory.setItem(16, lgm.getItem(path + "Permissions.Item", player));

        inventory.setItem(26, lgm.getItem("General.Close", player));
    }
}
