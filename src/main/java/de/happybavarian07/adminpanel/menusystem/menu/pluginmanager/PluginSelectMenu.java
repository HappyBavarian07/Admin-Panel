package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

/*
 * @Author HappyBavarian07
 * @Date 02.09.2021
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefixExtension;
import de.happybavarian07.coolstufflib.CoolStuffLib;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.actions.PermissionAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.ChatColor;
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
import java.util.logging.Level;

public class PluginSelectMenu extends PaginatedMenu<Plugin> implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final NamespacedKey pluginItemNamespacedKey = new NamespacedKey(plugin, "pluginItemName");
    private final List<Plugin> plugins;

    public PluginSelectMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission("AdminPanel.PluginManager.Open");
        plugins = new ArrayList<>(plugin.getPluginUtils().getAllPlugins());
        setPaginatedData(plugins, this::getPageItem);
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
    public void preSetMenuItems() {
    }

    @Override
    public void postSetMenuItems() {
        Player player = playerMenuUtility.getOwner();
        registerButton(getSlots() - 9, lgm.getItem("PluginManager.AutoUpdateMenu.OpenMenuItem", player, false),
                new PermissionAction("AdminPanel.PluginManager.AutoUpdateMenu", (p, e) -> {
                    if (plugin.getAutoUpdaterManager() == null) {
                        p.sendMessage(lgm.getMessage("Player.General.NullMenu", p, true));
                        plugin.getFileLogger().writeToLog(Level.WARNING, "AutoUpdaterManager is null, please check your config.yml and make sure the AutoUpdater is enabled.", LogPrefixExtension.ADMINPANEL_GUI, true);
                        return;
                    }
                    new PluginAutoUpdaterMenu(playerMenuUtility).open();
                }, lgm), null);
        registerButton(getSlots() - 8, lgm.getItem("PluginManager.Load", player, false),
                new PermissionAction("AdminPanel.PluginManager.LoadPlugins", (p, e) -> {
                    playerMenuUtility.addData("TypePluginFileNameToLoadInChat", true);
                    p.sendMessage(lgm.getMessage("Player.PluginManager.TypePluginFileNameToLoadInChat", p, true));
                    p.closeInventory();
                }, lgm), null);
        registerButton(getSlots() - 7, lgm.getItem("PluginManager.Install", player, false),
                new PermissionAction("AdminPanel.PluginManager.InstallPlugins", (p, e) -> new PluginInstallMenu(playerMenuUtility).open(), lgm), null);
    }

    @Override
    protected void handlePageItemClick(int indexOnPage, ItemStack item, InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Plugin selectedPlugin = plugins.get(item.getItemMeta().getPersistentDataContainer().get(pluginItemNamespacedKey, PersistentDataType.STRING) != null ?
                plugins.indexOf(plugin.getPluginUtils().getPluginByName(
                        item.getItemMeta().getPersistentDataContainer().get(pluginItemNamespacedKey, PersistentDataType.STRING)))
                : indexOnPage);
        playerMenuUtility.setData("CurrentSelectedPlugin", selectedPlugin, true);
        new PluginSettingsMenu(CoolStuffLib.getLib().getPlayerMenuUtility(player.getUniqueId())).open();
    }

    @Override
    protected void handleCustomItemClick(int slot, ItemStack item, InventoryClickEvent e) {
        // Logic is now handled by registered MenuActions in postSetMenuItems
    }

    public ItemStack getPageItem(Plugin plugin) {
        boolean enabled = plugin.isEnabled();
        lgm.setPathExpressionVariable(playerMenuUtility.getOwnerUUID().toString(), "PluginManager.PluginItem", "pluginEnabled", enabled, 1);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginName%", plugin.getName(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginVersion%", plugin.getDescription().getVersion(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginFullName%", plugin.getDescription().getFullName(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginAuthor%", String.join(", ", plugin.getDescription().getAuthors()), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginWebsite%", plugin.getDescription().getWebsite() != null ? plugin.getDescription().getWebsite() : "N/A", false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%plugimAPIVersion%", plugin.getDescription().getAPIVersion() != null ? plugin.getDescription().getAPIVersion() : "N/A", false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginDescription%", plugin.getDescription().getDescription() != null ? plugin.getDescription().getDescription() : "N/A", false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%pluginEnabled%", enabled ? "Yes" : "No", false);
        ItemStack item = lgm.getItem("PluginManager.PluginItem", playerMenuUtility.getOwner(), false);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(pluginItemNamespacedKey, PersistentDataType.STRING, plugin.getName());
        item.setItemMeta(meta);
        return item;
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playerMenuUtility.getOwner() != player) return;

        if (playerMenuUtility.hasData("TypePluginFileNameToLoadInChat")) {
            event.setCancelled(true);
            String message = ChatColor.stripColor(event.getMessage()).trim();
            if (message.equals("cancel") || message.equals("exit")) {
                playerMenuUtility.removeData("TypePluginFileNameToLoadInChat");
                super.open();
                return;
            }
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
