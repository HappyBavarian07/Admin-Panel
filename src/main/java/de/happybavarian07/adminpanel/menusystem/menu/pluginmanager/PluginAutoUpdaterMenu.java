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
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class PluginAutoUpdaterMenu extends PaginatedMenu<Plugin> {
    private final List<Plugin> plugins;
    private final Map<String, NewUpdater> pluginsToUpdate;
    private int spigotID = -1;
    private String fileName = "";


    public PluginAutoUpdaterMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PluginManager.AutoUpdateMenu");
        setSavedMenu(new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(playerMenuUtility.getOwner())));
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
        Player player = playerMenuUtility.getOwner();
        registerButton(getSlot("PluginManager.AutoUpdateMenu.AddPlugin", 46),
                lgm.getItem("PluginManager.AutoUpdateMenu.AddPlugin", player, false),
                (p, e) -> promptFileName(p, lgm.getMessage("Player.PluginManager.AutoPluginUpdater.SelectFileName", p, false)), null);
        registerButton(getSlot("PluginManager.AutoUpdateMenu.RemovePlugin", 47),
                lgm.getItem("PluginManager.AutoUpdateMenu.RemovePlugin", player, false),
                (p, e) -> promptRemovePlugin(p, lgm.getMessage("Player.PluginManager.AutoPluginUpdater.SelectPluginToRemove", p, false)), null);
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
        // Logic is now handled by registered MenuActions in postSetMenuItems
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

    private void ask(Player player, String message, BiConsumer<Player, String> onInput) {
        MenuAction.prompt(message, onInput).execute(player, null);
    }

    private void promptFileName(Player player, String message) {
        ask(player, message, (p, text) -> {
            String name = text.replace(" ", "-");
            if (!name.endsWith(".jar")) {
                promptFileName(p, null);
                return;
            }
            fileName = name;
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%fileName%", name, true);
            p.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.FileNameSelected", p, false));
            promptSpigotId(p, lgm.getMessage("Player.PluginManager.AutoPluginUpdater.SelectSpigotID", p, true));
        });
    }

    private void promptSpigotId(Player player, String message) {
        ask(player, message, (p, text) -> {
            int id;
            try {
                id = Integer.parseInt(text.replace(" ", ""));
            } catch (NumberFormatException e) {
                p.sendMessage(lgm.getMessage("Player.PlayerManager.Money.NotANumber", p, true));
                promptSpigotId(p, null);
                return;
            }
            spigotID = id;
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%spigotID%", id, true);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%fileName%", fileName, false);
            p.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.SpigotIDSelected", p, false));
            promptPluginName(p, lgm.getMessage("Player.PluginManager.AutoPluginUpdater.SelectPlugin", p, true));
        });
    }

    private void promptPluginName(Player player, String message) {
        ask(player, message, (p, text) -> {
            Plugin selectedPlugin = new PluginUtils().getPluginByName(text);
            if (selectedPlugin == null) {
                promptPluginName(p, null);
                return;
            }
            AdminPanelMain.getPlugin().getPluginDescriptionManager().addPluginDescription(selectedPlugin, "", spigotID);
            AdminPanelMain.getPlugin().getAutoUpdaterManager().addPluginToUpdater(selectedPlugin, spigotID, fileName);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%name%", text, true);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%spigotID%", spigotID, false);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%fileName%", fileName, false);
            p.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.PluginSelected", p, false));
            p.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.AddedPlugin", p, true));
            open();
        });
    }

    private void promptRemovePlugin(Player player, String message) {
        ask(player, message, (p, text) -> {
            String name = text.replace(" ", "-");
            Plugin selectedPlugin = new PluginUtils().getPluginByName(name);
            if (selectedPlugin == null) {
                promptRemovePlugin(p, null);
                return;
            }
            AdminPanelMain.getPlugin().getAutoUpdaterManager().removePluginFromUpdater(selectedPlugin);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%name%", name, true);
            p.sendMessage(lgm.getMessage("Player.PluginManager.AutoPluginUpdater.RemovedPlugin", p, true));
            open();
        });
    }
}
