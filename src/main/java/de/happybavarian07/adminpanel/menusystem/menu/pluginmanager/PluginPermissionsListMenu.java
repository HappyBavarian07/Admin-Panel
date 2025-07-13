package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginPermissionsListMenu extends PaginatedMenu<Permission> {
    private final List<Permission> permissions;

    public PluginPermissionsListMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        Plugin currentPlugin = playerMenuUtility.getData("CurrentSelectedPlugin", Plugin.class);
        setOpeningPermission("AdminPanel.PluginManager.PluginSettings.Permissions");
        permissions = currentPlugin.getDescription().getPermissions();
        setPaginatedData(permissions, this::getPageItem);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PluginManager.Permissions.Menu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PluginPermissionsListMenu";
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
    }

    @Override
    protected void handlePageItemClick(int indexOnPage, ItemStack item, InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (indexOnPage < 0 || indexOnPage >= permissions.size()) return;
        Permission current = permissions.get(indexOnPage);
        player.sendMessage(AdminPanelUtils.chat("&6Name: &a" + current.getName()));
        player.sendMessage(AdminPanelUtils.chat("&6Default: &a" + current.getDefault()));
        player.sendMessage(AdminPanelUtils.chat("&6Description: &a" + current.getDescription()));
        player.sendMessage(AdminPanelUtils.chat("&6Childrens: &a" + current.getChildren()));
        player.sendMessage(AdminPanelUtils.chat("&6Permissibles: &a" + current.getPermissibles()));
    }

    @Override
    protected void handleCustomItemClick(int slot, ItemStack item, InventoryClickEvent e) {
    }

    public ItemStack getPageItem(Permission permission) {
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(permission.getName());
        List<String> lore = new ArrayList<>();
        lore.add("&6Default: &7" + permission.getDefault());
        lore.add("&6Description: &7" + permission.getDescription());
        lore.add("&6Click for Infos.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
