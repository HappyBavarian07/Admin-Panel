package de.happybavarian07.adminpanel.menusystem.menu.permissionmanager;

import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import java.util.Comparator;
import java.util.List;

/** Every permission the server knows with its description and default; a click writes the name to the chat so it can be copied. */
public class PermissionBrowseMenu extends PaginatedMenu<Permission> {
    public PermissionBrowseMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission("AdminPanel.Permissions.Search");
        setSearchable(permission -> permission.getName() + " " + permission.getDescription());
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PermissionManager.Browse", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PermissionBrowseMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void preSetMenuItems() {
        List<Permission> permissions = Bukkit.getPluginManager().getPermissions().stream()
                .sorted(Comparator.comparing(Permission::getName)).toList();
        setPaginatedData(permissions, this::getPageItem, this::handleEntryClick);
        int perPage = getPaginatedItemSlots().length;
        if (perPage > 0) page = Math.min(page, Math.max(0, (paginatedData.size() - 1) / perPage));
    }

    private void handleEntryClick(Player staff, Permission permission, InventoryClickEvent event) {
        staff.sendMessage(lgm.getMessage("Permissions.Menu.BrowseName", staff, true).replace("%permission%", permission.getName()));
    }

    public ItemStack getPageItem(Permission permission) {
        lgm.addPlaceholder(PlaceholderType.ITEM, "%permission%", permission.getName(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%description%", permission.getDescription().isBlank() ? "-" : permission.getDescription(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%default%", permission.getDefault().name().toLowerCase(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%children%", permission.getChildren().size(), false);
        return lgm.getItem("PermissionManager.Browse.Entry", playerMenuUtility.getOwner(), true);
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
