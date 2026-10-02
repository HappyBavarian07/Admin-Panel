package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.Material;
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
        setPaginatedData(permissions, this::getPageItem, (player, current, event) ->
                CommandText.send(lgm, player, "PluginPermissionsList.Info", "%permission%", current.getName(),
                        "%default%", current.getDefault(), "%description%", String.valueOf(current.getDescription()),
                        "%children%", current.getChildren(), "%permissibles%", current.getPermissibles()));
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

    public ItemStack getPageItem(Permission permission) {
        lgm.addPlaceholder(PlaceholderType.ITEM, "%permission%", permission.getName(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%default%", permission.getDefault(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%description%",
                permission.getDescription() == null || permission.getDescription().isEmpty() ? "None" : permission.getDescription(), false);
        return lgm.getItem("PluginManager.PermissionsList.Entry", playerMenuUtility.getOwner(), true);
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
