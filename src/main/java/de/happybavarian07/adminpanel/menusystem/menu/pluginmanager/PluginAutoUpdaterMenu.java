package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;/*
 * @Author HappyBavarian07
 * @Date 18.04.2022 | 13:29
 */

import de.happybavarian07.adminpanel.main.PlaceholderType;
import de.happybavarian07.adminpanel.menusystem.PaginatedMenu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.utils.NewUpdater;
import de.happybavarian07.adminpanel.utils.PluginUtils;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class PluginAutoUpdaterMenu extends PaginatedMenu implements Listener {
    private int spigotID = -1;
    private String fileName = "";

    public PluginAutoUpdaterMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PluginManager.AutoUpdateMenu");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PluginManager.AutoUpdateMenu", playerMenuUtility.getOwner());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PluginAutoUpdaterMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        Plugin currentPlugin = new PluginUtils().getPluginByName(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
        Map<String, NewUpdater> pluginsToUpdate = new HashMap<>(plugin.getAutoUpdaterPlugins());
        ItemStack pluginItem = null;
        if (currentPlugin != null) {
            lgm.addPlaceholder(PlaceholderType.ITEM, "%spigotID%", pluginsToUpdate.get(currentPlugin.getName()).getResourceID(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%fileName%", pluginsToUpdate.get(currentPlugin.getName()).getFileName(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%onSpigot%", pluginsToUpdate.get(currentPlugin.getName()).resourceIsOnSpigot(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%externalFile%", pluginsToUpdate.get(currentPlugin.getName()).isExternalFile(), false);
            pluginItem = lgm.getItem("PluginManager.AutoUpdateMenu.PluginItem", playerMenuUtility.getOwner(), false);
            ItemMeta itemMeta = pluginItem.getItemMeta();
            itemMeta.setDisplayName(Utils.format(playerMenuUtility.getOwner(), "&a" + currentPlugin.getName(), ""));
            pluginItem.setItemMeta(itemMeta);
        }
        //System.out.println("Item: " + pluginItem);
        if (item.equals(pluginItem)) {
            // Update Plugin on Click
            NewUpdater updater = pluginsToUpdate.get(currentPlugin.getName());
            if (!updater.resourceIsOnSpigot()) {
                player.sendMessage(lgm.getMessage("Player.PluginManager.ResourceIsNotOnSpigot", player, true));
                return;
            }
            if (updater.isExternalFile() && updater.getLinkToFile().equals("") && !updater.bypassExternalURL()) {
                player.sendMessage(lgm.getMessage("Player.PluginManager.DownloadFileIsExternal", player, true));
                return;
            }
            updater.downloadLatestUpdate(updater.updateAvailable(), true, true);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%name%", currentPlugin.getName(), false);
            player.sendMessage(lgm.getMessage("Player.PluginManager.UpdatedPlugin", player, true));
        } else if (item.equals(lgm.getItem("PluginManager.AutoUpdateMenu.AddPlugin", player, false))) {
            // Add Plugin
            player.setMetadata("AddPluginMetaData", new FixedMetadataValue(plugin, true));
            player.closeInventory();
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.SelectFileName", player, false));
        } else if (item.equals(lgm.getItem("PluginManager.AutoUpdateMenu.RemovePlugin", player, false))) {
            // Remove Plugin
            player.setMetadata("RemovePluginSelectPluginMetaData", new FixedMetadataValue(plugin, true));
            player.closeInventory();
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.SelectPluginToRemove", player, false));
        } else if (item.equals(lgm.getItem("General.Close", player, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginSelectMenu(playerMenuUtility).open();
        } else if (item.equals(lgm.getItem("General.Left", null, false))) {
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
        } else if (item.equals(lgm.getItem("General.Right", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.pageright")) {
                player.sendMessage(noPerms);
                return;
            }
            if (!((index + 1) >= pluginsToUpdate.size())) {
                page = page + 1;
                super.open();
            } else {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player, true));
            }
        } else if (item.equals(lgm.getItem("General.Refresh", player, false))) {
            if (!player.hasPermission("AdminPanel.Button.refresh")) {
                player.sendMessage(noPerms);
                return;
            }
            super.open();
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();
        String path = "PluginManager.AutoUpdateMenu.";

        inventory.setItem(getSlot(path + "AddPlugin", 46), lgm.getItem(path + "AddPlugin", playerMenuUtility.getOwner(), false));
        inventory.setItem(getSlot(path + "RemovePlugin", 47), lgm.getItem(path + "RemovePlugin", playerMenuUtility.getOwner(), false));

        Map<String, NewUpdater> pluginsToUpdate = plugin.getAutoUpdaterPlugins();
        System.out.println("Plugins to Update: " + pluginsToUpdate);
        System.out.println("Plugins to Update: " + plugin.getAutoUpdaterPlugins());
        ///////////////////////////////////// Pagination loop template
        if (pluginsToUpdate != null && !pluginsToUpdate.isEmpty()) {
            int i = 0;
            for (String pluginName : pluginsToUpdate.keySet()) {
                index = super.maxItemsPerPage * page + i;
                if (index >= pluginsToUpdate.size()) break;
                if (pluginsToUpdate.get(pluginName) != null) {
                    ///////////////////////////

                    NewUpdater currentUpdater = pluginsToUpdate.get(pluginName);
                    if (currentUpdater == null) continue;
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%spigotID%", currentUpdater.getResourceID(), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%fileName%", currentUpdater.getFileName(), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%onSpigot%", currentUpdater.resourceIsOnSpigot(), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%externalFile%", currentUpdater.isExternalFile(), false);
                    ItemStack pluginItem = lgm.getItem("PluginManager.AutoUpdateMenu.PluginItem", playerMenuUtility.getOwner(), true);
                    ItemMeta itemMeta = pluginItem.getItemMeta();
                    itemMeta.setDisplayName(Utils.format(playerMenuUtility.getOwner(), "&a" + currentUpdater.getPluginToUpdate().getName(), ""));
                    pluginItem.setItemMeta(itemMeta);
                    inventory.addItem(pluginItem);


                    ////////////////////////
                }
                i++;
            }
        }
        ////////////////////////
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        Plugin selectedPlugin;
        if (player.hasMetadata("AddPluginMetaData")) {
            event.setCancelled(true);
            String message = event.getMessage().replace(" ", "-");
            if (!message.endsWith(".jar")) return;
            this.fileName = message;
            player.removeMetadata("AddPluginMetaData", plugin);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%fileName%", message, true);
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.FileNameSelected", player, false));
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.SelectSpigotID", player, true));
            player.setMetadata("AddPluginSpigotIDMetaData", new FixedMetadataValue(plugin, true));
        } else if (player.hasMetadata("AddPluginSpigotIDMetaData")) {
            event.setCancelled(true);
            int message;
            try {
                message = Integer.parseInt(event.getMessage().replace(" ", ""));
            } catch (NumberFormatException e) {
                player.sendMessage(lgm.getMessage("Player.General.PlayerManager.Money.NotANumber", player, true));
                return;
            }
            this.spigotID = message;
            player.removeMetadata("AddPluginSpigotIDMetaData", plugin);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%spigotID%", message, true);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%fileName%", fileName, false);
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.SpigotIDSelected", player, false));
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.SelectPlugin", player, true));
            player.setMetadata("AddPluginSelectPluginMetaData", new FixedMetadataValue(plugin, true));
        } else if (player.hasMetadata("AddPluginSelectPluginMetaData")) {
            event.setCancelled(true);
            String message = event.getMessage().replace(" ", "-");
            selectedPlugin = new PluginUtils().getPluginByName(message);
            if (selectedPlugin == null) {
                return;
            }
            player.removeMetadata("AddPluginSelectPluginMetaData", plugin);
            plugin.addPluginToUpdater(selectedPlugin, spigotID, fileName);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%name%", message, true);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%spigotID%", spigotID, false);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%fileName%", fileName, false);
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.PluginSelected", player, false));
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.AddedPlugin", player, true));
            super.open();
        } else if (player.hasMetadata("RemovePluginSelectPluginMetaData")) {
            event.setCancelled(true);
            String message = event.getMessage().replace(" ", "-");
            selectedPlugin = new PluginUtils().getPluginByName(message);
            if (selectedPlugin == null) {
                return;
            }
            player.removeMetadata("RemovePluginSelectPluginMetaData", plugin);
            plugin.removePluginFromUpdater(selectedPlugin);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%name%", message, true);
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.RemovedPlugin", player, true));
            super.open();
        }
    }
}
