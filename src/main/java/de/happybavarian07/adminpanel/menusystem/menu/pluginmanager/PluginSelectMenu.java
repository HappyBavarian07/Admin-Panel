package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

/*
 * @Author HappyBavarian07
 * @Date 02.09.2021
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.utils.LogPrefixExtension;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class PluginSelectMenu extends PaginatedMenu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final NamespacedKey pluginItemNamespacedKey = new NamespacedKey(plugin, "pluginItemName");

    public PluginSelectMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PluginManager.Open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PluginManager.Selector", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PluginSelectMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String path = "PluginManager.";
        List<Plugin> plugins = new ArrayList<>(plugin.getPluginUtils().getAllPlugins());

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item == null || !item.hasItemMeta()) return;
        if (item.getItemMeta().getPersistentDataContainer().has(pluginItemNamespacedKey, PersistentDataType.STRING)) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Open")) {
                player.sendMessage(noPerms);
                return;
            }
            playerMenuUtility.addData("CurrentSelectedPlugin",
                    plugin.getPluginUtils().getPluginByName(item.getItemMeta().getPersistentDataContainer().get(pluginItemNamespacedKey, PersistentDataType.STRING)));
            new PluginSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        }

        ItemStack installItem = lgm.getItem(path + "Install", player, false);
        if (item.getItemMeta().getDisplayName().equals(installItem.getItemMeta().getDisplayName()) &&
                Objects.equals(item.getItemMeta().getLore(), installItem.getItemMeta().getLore()) &&
                item.getType().equals(installItem.getType())) {
            if (!player.hasPermission("AdminPanel.PluginManager.InstallPlugins")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginInstallMenu(playerMenuUtility).open();
        } else if (item.isSimilar(lgm.getItem(path + "Load", player, false))) {
            if (!player.hasPermission("AdminPanel.PluginManager.LoadPlugins")) {
                player.sendMessage(noPerms);
                return;
            }
            playerMenuUtility.addData("TypePluginFileNameToLoadInChat", true);
            player.sendMessage(lgm.getMessage("Player.PluginManager.TypePluginFileNameToLoadInChat", player, true));
            player.closeInventory();
        } else if (item.isSimilar(lgm.getItem("PluginManager.AutoUpdateMenu.OpenMenuItem", player, false))) {
            if (!player.hasPermission("AdminPanel.PluginManager.AutoUpdateMenu")) {
                player.sendMessage(noPerms);
                return;
            }
            if (plugin.getAutoUpdaterManager() == null) {
                player.sendMessage(lgm.getMessage("Player.General.NullMenu", player, true));
                plugin.getFileLogger().writeToLog(Level.WARNING, "AutoUpdaterManager is null, please check your config.yml and make sure the AutoUpdater is enabled.", LogPrefixExtension.ADMINPANEL_GUI, true);
                return;
            }
            new PluginAutoUpdaterMenu(playerMenuUtility).open();
        } else if (item.isSimilar(lgm.getItem("General.Close", player, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.isSimilar(lgm.getItem("General.Left", player, false))) {
            if (!player.hasPermission("AdminPanel.Button.pageleft")) {
                player.sendMessage(noPerms);
                return;
            }
            if (page == 0) {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnFirstPage", player, true));
            } else {
                page = page - 1;
                super.open();
            }
        } else if (item.isSimilar(lgm.getItem("General.Right", player, false))) {
            if (!player.hasPermission("AdminPanel.Button.pageright")) {
                player.sendMessage(noPerms);
                return;
            }
            if (!((index + 1) >= plugins.size())) {
                page = page + 1;
                super.open();
            } else {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player, true));
            }
        } else if (item.isSimilar(lgm.getItem("General.Refresh", player, false))) {
            if (!player.hasPermission("AdminPanel.Button.refresh")) {
                player.sendMessage(noPerms);
                return;
            }
            super.open();
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
        addMenuBorder();
        inventory.setItem(getSlot("PluginManager.AutoUpdateMenu.OpenMenuItem", 45), lgm.getItem("PluginManager.AutoUpdateMenu.OpenMenuItem", playerMenuUtility.getOwner(), false));
        inventory.setItem(getSlot("PluginManager.Load", 46), lgm.getItem("PluginManager.Load", playerMenuUtility.getOwner(), false));
        inventory.setItem(getSlot("PluginManager.Install", 47), lgm.getItem("PluginManager.Install", playerMenuUtility.getOwner(), false));
        List<Plugin> plugins = new ArrayList<>(plugin.getPluginUtils().getAllPlugins());

        ///////////////////////////////////// Pagination loop template
        if (!plugins.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= plugins.size()) break;
                if (plugins.get(index) != null) {
                    Plugin currentPlugin = plugins.get(index);
                    boolean enabled = currentPlugin.isEnabled();

                    lgm.setPathExpressionVariable(playerMenuUtility.getOwnerUUID().toString(), "PluginManager.PluginItem", "pluginEnabled", enabled);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginName%", currentPlugin.getName(), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginVersion%", currentPlugin.getDescription().getVersion(), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginFullName%", currentPlugin.getDescription().getFullName(), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginAuthor%", currentPlugin.getDescription().getAuthors().toString(), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginWebsite%", currentPlugin.getDescription().getWebsite() == null ? "null" : currentPlugin.getDescription().getWebsite(), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginAPIVersion%", currentPlugin.getDescription().getAPIVersion() == null ? "null" : currentPlugin.getDescription().getAPIVersion(), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginDescription%", plugin.getPluginDescriptionManager().getDescriptionFromPlugin(currentPlugin), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginFileName%", currentPlugin.getDescription().getFullName() + ".jar", false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginEnabled%", enabled ? "true" : "false", false);
                    ItemStack item = lgm.getItem("PluginManager.PluginItem", playerMenuUtility.getOwner(), false);
                    ItemMeta itemMeta = item.getItemMeta();
                    if (itemMeta != null) {
                        itemMeta.getPersistentDataContainer().set(pluginItemNamespacedKey, PersistentDataType.STRING, currentPlugin.getName());
                        item.setItemMeta(itemMeta);
                    }
                    inventory.addItem(item);

                    ////////////////////////
                }
            }
        }
        ////////////////////////
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playerMenuUtility.getOwner() != player) return;

        if (playerMenuUtility.hasData("TypePluginFileNameToLoadInChat")) {
            event.setCancelled(true);
            String message = event.getMessage().replace(" ", "-");
            File pluginFile = new File(plugin.getPluginFile().getParentFile(), message + ".jar");
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%filename%", message + ".jar", true);
            if (!pluginFile.exists()) {
                player.sendMessage(lgm.getMessage("Player.PluginManager.FileToLoadDoesNotExist", player, true));
                return;
            }
            try {
                plugin.getPluginUtils().load(pluginFile);
            } catch (Exception e) {
                player.sendMessage(lgm.getMessage("Player.PluginManager.LoadPluginError", player, true));
                return;
            }
            playerMenuUtility.removeData("TypePluginFileNameToLoadInChat");
            player.sendMessage(lgm.getMessage("Player.PluginManager.PluginFileNameSelected", player, true));
            super.open();
        }
    }
}
