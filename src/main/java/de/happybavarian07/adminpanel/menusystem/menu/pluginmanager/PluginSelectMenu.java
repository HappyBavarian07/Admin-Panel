package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

/*
 * @Author HappyBavarian07
 * @Date 02.09.2021
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.Head;
import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.menusystem.PaginatedMenu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.utils.PluginUtils;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PluginSelectMenu extends PaginatedMenu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final PluginUtils pluginUtils;

    public PluginSelectMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        this.pluginUtils = new PluginUtils();
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
        List<Plugin> plugins = new ArrayList<>(pluginUtils.getAllPlugins());

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item == null || !item.hasItemMeta()) return;
        if (item.getType().equals(legacyServer() ? Material.matchMaterial("SKULL_ITEM") : Material.PLAYER_HEAD)) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Open")) {
                player.sendMessage(noPerms);
                return;
            }
            playerMenuUtility.addData("CurrentSelectedPlugin", Bukkit.getPluginManager().getPlugin(ChatColor.stripColor(item.getItemMeta().getDisplayName())));
            new PluginSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem(path + "Install", player, false))) {
            if (!player.hasPermission("AdminPanel.PluginManager.InstallPlugins")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginInstallMenu(playerMenuUtility).open();
        } else if (item.equals(lgm.getItem(path + "Load", player, false))) {
            if (!player.hasPermission("AdminPanel.PluginManager.LoadPlugins")) {
                player.sendMessage(noPerms);
                return;
            }
            playerMenuUtility.addData("TypePluginFileNameToLoadInChat", true);
            player.sendMessage(lgm.getMessage("Player.PluginManager.TypePluginFileNameToLoadInChat", player, true));
            player.closeInventory();
        } else if (item.equals(lgm.getItem("PluginManager.AutoUpdateMenu.OpenMenuItem", player, false))) {
            if (!player.hasPermission("AdminPanel.PluginManager.AutoUpdateMenu")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginAutoUpdaterMenu(playerMenuUtility).open();
        } else if (item.equals(lgm.getItem("General.Close", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
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
            if (!((index + 1) >= plugins.size())) {
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
        List<Plugin> plugins = new ArrayList<>(pluginUtils.getAllPlugins());

        ///////////////////////////////////// Pagination loop template
        if (!plugins.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= plugins.size()) break;
                if (plugins.get(index) != null) {
                    ///////////////////////////

                    Plugin currentPlugin = plugins.get(index);
                    boolean enabled = currentPlugin.isEnabled();
                    ItemStack item;
                    if (enabled) {
                        item = Head.BLANK_GREEN.getAsItem();
                    } else {
                        item = Head.BLANK_RED.getAsItem();
                    }
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(Utils.chat("&a" + currentPlugin.getName()));
                    List<String> lore = new ArrayList<>();
                    lore.add(Utils.chat("&6Description: &a" + plugin.getPluginDescriptionManager().getDescriptionFromPlugin(currentPlugin)));
                    lore.add(Utils.chat("&6Enabled: &a" + enabled));
                    lore.add(Utils.chat("&6Version: &a" + currentPlugin.getDescription().getVersion()));
                    if (currentPlugin.getDescription().getAuthors().size() == 1) {
                        lore.add(Utils.chat("&6Author: &a" + currentPlugin.getDescription().getAuthors().get(0)));
                    } else {
                        lore.add(Utils.chat("&6Authors: &a" + currentPlugin.getDescription().getAuthors()));
                    }
                    lore.add(Utils.chat("&6Website: &a" + currentPlugin.getDescription().getWebsite()));
                    lore.add(Utils.chat("&6API-Version: &a" + currentPlugin.getDescription().getAPIVersion()));
                    lore.add(Utils.chat("&6Full-Name: &a" + currentPlugin.getDescription().getFullName()));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
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
                pluginUtils.load(pluginFile);
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
