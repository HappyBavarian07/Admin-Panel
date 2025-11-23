package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;/*
 * @Author HappyBavarian07
 * @Date 18.04.2022 | 13:29
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.adminpanel.utils.NewUpdater;
import de.happybavarian07.adminpanel.utils.PluginUtils;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginAutoUpdaterMenu extends PaginatedMenu<Plugin> implements Listener {
    private final List<Plugin> plugins;
    private final Map<String, NewUpdater> pluginsToUpdate;
    private int spigotID = -1;
    private String fileName = "";


    public PluginAutoUpdaterMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PluginManager.AutoUpdateMenu");
        pluginsToUpdate = new HashMap<>(AdminPanelMain.getPlugin().getAutoUpdaterManager().getAutoUpdaterPlugins());
        plugins = new ArrayList<>();
        for (String pluginName : pluginsToUpdate.keySet()) {
            Plugin plugin = new PluginUtils().getPluginByName(pluginName);
            if (plugin != null) plugins.add(plugin);
        }
        setPaginatedData(plugins, this::getPageItem);
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
    public void preSetMenuItems() {
    }

    @Override
    public void postSetMenuItems() {
        inventory.setItem(getSlot("PluginManager.AutoUpdateMenu.AddPlugin", 46), lgm.getItem("PluginManager.AutoUpdateMenu.AddPlugin", playerMenuUtility.getOwner(), false));
        inventory.setItem(getSlot("PluginManager.AutoUpdateMenu.RemovePlugin", 47), lgm.getItem("PluginManager.AutoUpdateMenu.RemovePlugin", playerMenuUtility.getOwner(), false));
    }

    @Override
    protected void handlePageItemClick(int indexOnPage, ItemStack item, InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (indexOnPage < 0 || indexOnPage >= plugins.size()) return;
        Plugin currentPlugin = plugins.get(indexOnPage);
        NewUpdater updater = pluginsToUpdate.get(currentPlugin.getName());
        if (!updater.resourceIsOnSpigot()) {
            player.sendMessage(lgm.getMessage("Player.PluginManager.ResourceIsNotOnSpigot", player, true));
            return;
        }
        if (updater.isExternalFile() && updater.getLinkToFile().isEmpty() && !updater.bypassExternalURL()) {
            player.sendMessage(lgm.getMessage("Player.PluginManager.DownloadFileIsExternal", player, true));
            return;
        }
        boolean replace = AdminPanelMain.getPlugin().getConfig().getBoolean("Plugin.Updater.PluginUpdater.automaticReplace");
        updater.checkForUpdatesAsync(false, (available, latest) -> {
            if (available) updater.downloadLatestUpdateAsync(replace, true, true, r -> {
            });
        });
        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%name%", currentPlugin.getName(), false);
        player.sendMessage(lgm.getMessage("Player.PluginManager.UpdatedPlugin", player, true));
    }

    @Override
    protected void handleCustomItemClick(int slot, ItemStack item, InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (item.isSimilar(lgm.getItem("PluginManager.AutoUpdateMenu.AddPlugin", player, false))) {
            playerMenuUtility.addData("AddPluginMetaData", true);
            player.closeInventory();
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.SelectFileName", player, false));
        } else if (item.isSimilar(lgm.getItem("PluginManager.AutoUpdateMenu.RemovePlugin", player, false))) {
            playerMenuUtility.addData("RemovePluginSelectMetaData", true);
            player.closeInventory();
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.SelectPluginToRemove", player, false));
        } else if (item.isSimilar(lgm.getItem("General.Close", player, false))) {
            new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        }
    }

    public ItemStack getPageItem(Plugin plugin) {
        Map<String, NewUpdater> pluginsToUpdate = new HashMap<>(AdminPanelMain.getPlugin().getAutoUpdaterManager().getAutoUpdaterPlugins());
        NewUpdater updater = pluginsToUpdate.get(plugin.getName());
        lgm.addPlaceholder(PlaceholderType.ITEM, "%spigotID%", updater.getResourceID(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%fileName%", updater.getFileName(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%onSpigot%", updater.resourceIsOnSpigot(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%externalFile%", updater.isExternalFile(), false);
        ItemStack item = lgm.getItem("PluginManager.AutoUpdateMenu.PluginItem", playerMenuUtility.getOwner(), true);
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(AdminPanelUtils.format(playerMenuUtility.getOwner(), "&a" + plugin.getName(), ""));
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Plugin selectedPlugin;
        if (playerMenuUtility.hasData("AddPluginMetaData")) {
            event.setCancelled(true);
            String message = event.getMessage().replace(" ", "-");
            if (!message.endsWith(".jar")) return;
            this.fileName = message;
            playerMenuUtility.removeData("AddPluginMetaData");
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%fileName%", message, true);
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.FileNameSelected", player, false));
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.SelectSpigotID", player, true));
            playerMenuUtility.addData("AddPluginSpigotIDMetaData", true);
        } else if (playerMenuUtility.hasData("AddPluginSpigotIDMetaData")) {
            event.setCancelled(true);
            int message;
            try {
                message = Integer.parseInt(event.getMessage().replace(" ", ""));
            } catch (NumberFormatException e) {
                player.sendMessage(lgm.getMessage("Player.General.PlayerManager.Money.NotANumber", player, true));
                return;
            }
            this.spigotID = message;
            playerMenuUtility.removeData("AddPluginSpigotIDMetaData");
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%spigotID%", message, true);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%fileName%", fileName, false);
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.SpigotIDSelected", player, false));
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.SelectPlugin", player, true));
            playerMenuUtility.addData("AddPluginSelectPluginMetaData", true);
        } else if (playerMenuUtility.hasData("AddPluginSelectPluginMetaData")) {
            event.setCancelled(true);
            String message = event.getMessage();
            selectedPlugin = new PluginUtils().getPluginByName(message);
            if (selectedPlugin == null) {
                return;
            }
            AdminPanelMain.getPlugin().getPluginDescriptionManager().addPluginDescription(selectedPlugin, "", spigotID);
            playerMenuUtility.removeData("AddPluginSelectPluginMetaData");
            AdminPanelMain.getPlugin().getAutoUpdaterManager().addPluginToUpdater(selectedPlugin, spigotID, fileName);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%name%", message, true);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%spigotID%", spigotID, false);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%fileName%", fileName, false);
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.PluginSelected", player, false));
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.AddedPlugin", player, true));
            super.open();
        } else if (playerMenuUtility.hasData("RemovePluginSelectMetaData")) {
            event.setCancelled(true);
            String message = event.getMessage().replace(" ", "-");
            selectedPlugin = new PluginUtils().getPluginByName(message);
            if (selectedPlugin == null) {
                return;
            }
            playerMenuUtility.removeData("RemovePluginSelectMetaData");
            AdminPanelMain.getPlugin().getAutoUpdaterManager().removePluginFromUpdater(selectedPlugin);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%name%", message, true);
            player.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.RemovedPlugin", player, true));
            super.open();
        }
    }
}
