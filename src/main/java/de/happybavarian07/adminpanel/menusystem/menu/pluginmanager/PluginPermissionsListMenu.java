package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.PaginatedMenu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.utils.Utils;
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

public class PluginPermissionsListMenu extends PaginatedMenu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final Plugin currentPlugin;

    public PluginPermissionsListMenu(PlayerMenuUtility playerMenuUtility, Plugin currentPlugin) {
        super(playerMenuUtility);
        this.currentPlugin = currentPlugin;
        setOpeningPermission("AdminPanel.PluginManager.PluginSettings.Permissions");
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
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String path = "PluginManager.Permissions.";
        List<Permission> permissions = currentPlugin.getDescription().getPermissions();

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item == null || !item.hasItemMeta()) return;
        if (item.getType().equals(Material.WRITABLE_BOOK)) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Open")) {
                player.sendMessage(noPerms);
                return;
            }
            Permission current = null;
            for (Permission perm : permissions) {
                if (perm.getName().equals(item.getItemMeta().getDisplayName())) {
                    current = perm;
                }
            }
            Utils utils = Utils.getInstance();
            player.sendMessage(Utils.chat("&6Name: &a" + current.getName()));
            player.sendMessage(Utils.chat("&6Default: &a" + current.getDefault()));
            player.sendMessage(Utils.chat("&6Description: &a" + current.getDescription()));
            player.sendMessage(Utils.chat("&6Childrens: &a" + current.getChildren()));
            player.sendMessage(Utils.chat("&6Permissibles: &a" + current.getPermissibles()));
        } else if (item.equals(lgm.getItem("General.Close", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            playerMenuUtility.setData("CurrentSelectedPlugin", currentPlugin, true);
            new PluginSettingsMenu(playerMenuUtility).open();
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
            if (!((index + 1) >= permissions.size())) {
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

        List<Permission> permissions = currentPlugin.getDescription().getPermissions();
        ///////////////////////////////////// Pagination loop template
        if (!permissions.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= permissions.size()) break;
                if (permissions.get(index) != null) {
                    ///////////////////////////

                    Permission currentPermission = permissions.get(index);
                    if (currentPermission == null) continue;
                    ItemStack command = new ItemStack(Material.WRITABLE_BOOK, 1);
                    ItemMeta commandMeta = command.getItemMeta();
                    commandMeta.setDisplayName(currentPermission.getName());
                    List<String> lore = new ArrayList<>();
                    Utils utils = Utils.getInstance();
                    lore.add(Utils.chat("&6Click for Infos"));
                    commandMeta.setLore(lore);
                    command.setItemMeta(commandMeta);
                    inventory.addItem(command);

                    ////////////////////////
                }
            }
        }
        ////////////////////////
    }
}
