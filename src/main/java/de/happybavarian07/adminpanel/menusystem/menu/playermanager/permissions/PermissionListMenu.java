package de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.menusystem.PaginatedMenu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.utils.Utils;
import de.happybavarian07.adminpanel.utils.tfidfsearch.TFIDFSearch;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class PermissionListMenu extends PaginatedMenu implements Listener {
    private final PlayerMenuUtility playerMenuUtility;
    private final PermissionAction action;
    private final UUID targetUUID;
    private List<Permission> permissions = new ArrayList<>();
    private PermissionListMode mode;
    private String sortQuery;

    public PermissionListMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        this.playerMenuUtility = playerMenuUtility;
        this.action = playerMenuUtility.getData("PermissionAction", PermissionAction.class);
        this.targetUUID = playerMenuUtility.getTargetUUID();
        if (mode == null) {
            this.mode = PermissionListMode.ALL;
        } else {
            this.mode = playerMenuUtility.getData("PermissionListMode", PermissionListMode.class);
        }
        this.sortQuery = playerMenuUtility.getData("SortQuery", String.class);
        resetPermList();
        if (!sortQuery.isEmpty()) {
            tfidfSortPermissions(sortQuery);
        }
        if (action == PermissionAction.ADD) {
            setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.Add");
        } else if (action == PermissionAction.REMOVE) {
            setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.Remove");
        } else if (action == PermissionAction.INFO) {
            setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.Info");
        } else if (action == PermissionAction.LIST) {
            setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.List");
        }
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.Permissions.ListMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PermissionListMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        InventoryAction action2 = e.getAction();
        String path = "PlayerManager.ActionsMenu.Permissions.ListMenu";
        Player player = (Player) e.getWhoClicked();
        Player target = Bukkit.getPlayer(targetUUID);
        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);
        ItemStack item = e.getCurrentItem();
        if (item != null) {
            if (item.equals(lgm.getItem(path + ".Sort", player, false))) {
                if (action == PermissionAction.LIST) {
                    playerMenuUtility.addData("PermissionsSortMetaData-List", true);
                } else if (action == PermissionAction.ADD) {
                    playerMenuUtility.addData("PermissionsSortMetaData-Add", true);
                } else if (action == PermissionAction.REMOVE) {
                    playerMenuUtility.addData("PermissionsSortMetaData-Remove", true);
                } else if (action == PermissionAction.INFO) {
                    playerMenuUtility.addData("PermissionsSortMetaData-Info", true);
                }
                player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.ListMenu.EnterSort", player, true));
                player.closeInventory();
            } else if (item.equals(lgm.getItem(path + ".Mode." + mode.getName(), player, false))) {
                this.mode = PermissionListMode.getNext(mode);
                resetPermList();
                playerMenuUtility.setData("PermissionListMode", mode, true);
                playerMenuUtility.setData("SortQuery", sortQuery, true);
                playerMenuUtility.setData("PermissionAction", action, true);
                new PermissionListMenu(playerMenuUtility).open();
            } else if (item.equals(lgm.getItem("General.Close", null, false))) {
                if (!player.hasPermission("AdminPanel.Button.Close")) {
                    player.sendMessage(noPerms);
                    return;
                }
                new PermissionActionSelectMenu(playerMenuUtility).open();
            } else if (item.equals(lgm.getItem("General.Left", null, false))) {
                if (!player.hasPermission("AdminPanel.Button.pageleft")) {
                    player.sendMessage(noPerms);
                    return;
                }
                if (page == 0) {
                    player.sendMessage(lgm.getMessage("Player.General.AlreadyOnFirstPage", player, true));
                } else {
                    page = page - 1;
                    playerMenuUtility.setData("PermissionListMode", mode, true);
                    playerMenuUtility.setData("SortQuery", sortQuery, true);
                    playerMenuUtility.setData("PermissionAction", action, true);
                    this.open();
                }
            } else if (item.equals(lgm.getItem("General.Right", null, false))) {
                if (!player.hasPermission("AdminPanel.Button.pageright")) {
                    player.sendMessage(noPerms);
                    return;
                }
                if (!((index + 1) >= permissions.size())) {
                    page = page + 1;
                    playerMenuUtility.setData("PermissionListMode", mode, true);
                    playerMenuUtility.setData("SortQuery", sortQuery, true);
                    playerMenuUtility.setData("PermissionAction", action, true);
                    this.open();
                } else {
                    player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player, true));
                }
            } else if (item.equals(lgm.getItem("General.Refresh", null, false))) {
                if (!player.hasPermission("AdminPanel.Button.refresh")) {
                    player.sendMessage(noPerms);
                    return;
                }
                playerMenuUtility.setData("PermissionListMode", mode, true);
                playerMenuUtility.setData("SortQuery", sortQuery, true);
                playerMenuUtility.setData("PermissionAction", action, true);
                new PermissionListMenu(playerMenuUtility).open();
            } else if (item.getType().equals(Material.WRITABLE_BOOK)) {
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", target.getName(), true);
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%permission%", item.getItemMeta().getDisplayName(), false);
                if (action.equals(PermissionAction.ADD)) {
                    if (action2.equals(InventoryAction.PICKUP_ALL)) {
                        plugin.getPlayerPermissions().get(target.getUniqueId()).put(item.getItemMeta().getDisplayName(), true);
                        plugin.getPlayerPermissionsAttachments().get(target.getUniqueId()).setPermission(item.getItemMeta().getDisplayName(),
                                true);
                    }
                    if (action2.equals(InventoryAction.PICKUP_HALF)) {
                        plugin.getPlayerPermissions().get(target.getUniqueId()).put(item.getItemMeta().getDisplayName(), false);
                        plugin.getPlayerPermissionsAttachments().get(target.getUniqueId()).setPermission(item.getItemMeta().getDisplayName(),
                                false);
                    }
                    plugin.savePerms();
                    plugin.reloadPerms(target);
                    if (target.hasPermission(tfidfSearchPermissions(item.getItemMeta().getDisplayName()))) {
                        player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.AddedPermission", player, true));
                    }
                } else if (action.equals(PermissionAction.REMOVE)) {
                    this.mode = PermissionListMode.getNext(mode);
                    plugin.getPlayerPermissions().get(target.getUniqueId()).remove(tfidfSearchPermissions(item.getItemMeta().getDisplayName()).getName());
                    plugin.getPlayerPermissionsAttachments().get(target.getUniqueId()).unsetPermission(item.getItemMeta().getDisplayName());
                    plugin.savePerms();
                    plugin.reloadPerms(target);
                    if (!target.hasPermission(tfidfSearchPermissions(item.getItemMeta().getDisplayName()))) {
                        player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.RemovedPermission", player, true));
                    }
                } else if (action.equals(PermissionAction.INFO)) {
                    Permission current = tfidfSearchPermissions(item.getItemMeta().getDisplayName());
                    player.sendMessage(Utils.format(player, "&aName: &6" + current.getName() + "\n" +
                            "&aDescription: &6" + current.getDescription() + "\n" +
                            "&aPermissibles: &6" + current.getPermissibles() + "\n" +
                            "&aChildren: &6" + current.getChildren() + "\n" +
                            "&aDefault: &6" + current.getDefault() + "\n", AdminPanelMain.getPrefix()));
                }
                resetPermList();
                playerMenuUtility.setData("PermissionListMode", mode, true);
                playerMenuUtility.setData("SortQuery", sortQuery, true);
                playerMenuUtility.setData("PermissionAction", action, true);
                new PermissionListMenu(playerMenuUtility).open();
            }
        }
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
        // Handle open menu event if needed
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
        // Handle close menu event if needed
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();
        String path = "PlayerManager.ActionsMenu.Permissions.ListMenu";
        Player player = playerMenuUtility.getOwner();
        inventory.setItem(47, lgm.getItem(path + ".Sort", player, false));
        if (action == PermissionAction.INFO || action == PermissionAction.LIST) {
            inventory.setItem(52, lgm.getItem(path + ".Mode." + mode.getName(), player, false));
        }

        ///////////////////////////////////// Pagination loop template
        if (permissions != null && !permissions.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= permissions.size()) break;
                if (permissions.get(index) != null) {
                    ///////////////////////////

                    Permission current = permissions.get(index);
                    if (mode == PermissionListMode.PLAYER) {
                        if (!Bukkit.getPlayer(targetUUID).hasPermission(current)) {
                            continue;
                        }
                    }
                    ItemStack item = new ItemStack(Material.WRITABLE_BOOK, 1);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(current.getName());
                    if (action == PermissionAction.REMOVE || action == PermissionAction.LIST || action == PermissionAction.INFO) {
                        if (current.getDefault().equals(PermissionDefault.TRUE)) {
                            meta.setLore(Arrays.asList(Utils.format(player,
                                            "&aDefault: &6true", AdminPanelMain.getPrefix()),
                                    Utils.format(player,
                                            "&aValue: &6" + Bukkit.getPlayer(targetUUID).hasPermission(current.getName()), AdminPanelMain.getPrefix())));
                        } else {
                            meta.setLore(Arrays.asList(Utils.format(player,
                                            "&aDefault: &6false", AdminPanelMain.getPrefix()),
                                    Utils.format(player,
                                            "&aValue: &6" + Bukkit.getPlayer(targetUUID).hasPermission(current.getName()), AdminPanelMain.getPrefix())));
                        }
                    } else {
                        if (current.getDefault().equals(PermissionDefault.TRUE)) {
                            meta.setLore(Collections.singletonList(Utils.format(player,
                                    "&aDefault: &6true", AdminPanelMain.getPrefix())));
                        } else {
                            meta.setLore(Collections.singletonList(Utils.format(player,
                                    "&aDefault: &6false", AdminPanelMain.getPrefix())));
                        }
                    }
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

        if (playerMenuUtility.hasData("PermissionsSortMetaData-List")) {
            this.sortQuery = ChatColor.stripColor(event.getMessage());
            tfidfSortPermissions(sortQuery);
            playerMenuUtility.removeData("PermissionsSortMetaData-List");
            player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.ListMenu.SortResult", player, true));
            playerMenuUtility.setData("PermissionListMode", mode, true);
            playerMenuUtility.setData("SortQuery", sortQuery, true);
            playerMenuUtility.setData("PermissionAction", PermissionAction.LIST, true);
            new PermissionListMenu(playerMenuUtility).open();
            event.setCancelled(true);
        } else if (playerMenuUtility.hasData("PermissionsSortMetaData-Add")) {
            this.sortQuery = ChatColor.stripColor(event.getMessage());
            tfidfSortPermissions(sortQuery);
            playerMenuUtility.removeData("PermissionsSortMetaData-Add");
            player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.ListMenu.SortResult", player, true));
            playerMenuUtility.setData("PermissionListMode", mode, true);
            playerMenuUtility.setData("SortQuery", sortQuery, true);
            playerMenuUtility.setData("PermissionAction", PermissionAction.ADD, true);
            new PermissionListMenu(playerMenuUtility).open();
            event.setCancelled(true);
        } else if (playerMenuUtility.hasData("PermissionsSortMetaData-Remove")) {
            this.sortQuery = ChatColor.stripColor(event.getMessage());
            tfidfSortPermissions(sortQuery);
            playerMenuUtility.removeData("PermissionsSortMetaData-Remove");
            player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.ListMenu.SortResult", player, true));
            playerMenuUtility.setData("PermissionListMode", mode, true);
            playerMenuUtility.setData("SortQuery", sortQuery, true);
            playerMenuUtility.setData("PermissionAction", PermissionAction.REMOVE, true);
            new PermissionListMenu(playerMenuUtility).open();
            event.setCancelled(true);
        } else if (playerMenuUtility.hasData("PermissionsSortMetaData-Info")) {
            this.sortQuery = ChatColor.stripColor(event.getMessage());
            tfidfSortPermissions(sortQuery);
            playerMenuUtility.removeData("PermissionsSortMetaData-Info");
            player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.ListMenu.SortResult", player, true));
            playerMenuUtility.setData("PermissionListMode", mode, true);
            playerMenuUtility.setData("SortQuery", sortQuery, true);
            playerMenuUtility.setData("PermissionAction", PermissionAction.INFO, true);
            new PermissionListMenu(playerMenuUtility).open();
            event.setCancelled(true);
        }
    }

    private void resetPermList() {
        permissions.clear();
        for (Permission perm : Bukkit.getPluginManager().getPermissions()) {
            if (mode == PermissionListMode.PLAYER) {
                if (!Bukkit.getPlayer(targetUUID).hasPermission(perm)) {
                    continue;
                }
            }
            permissions.add(perm);
        }
    }

    private void tfidfSortPermissions(String query) {
        if (!query.isEmpty() && !query.isBlank()) {
            try {
                // Use the search method to get a list of permissions sorted by relevance
                List<TFIDFSearch.Item> searchResults = plugin.getPermissionSearcher().search(query);

                // Clear the permissions list and add the search results to it
                permissions.clear();
                for (TFIDFSearch.Item resultItem : searchResults) {
                    Permission permission = Bukkit.getPluginManager().getPermission(resultItem.getFieldValue("permissionName"));
                    if (permission != null) {
                        permissions.add(permission);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                playerMenuUtility.getOwner().sendMessage("The Query contained Errors. Please fix them: " + e.getMessage());
                permissions.clear();
            }
        } else {
            permissions = new ArrayList<>(Bukkit.getPluginManager().getPermissions());
        }
    }

    private Permission tfidfSearchPermissions(String query) {
        try {
            // Use the search method to get the most relevant permission
            List<TFIDFSearch.Item> searchResults = plugin.getPermissionSearcher().search(query);

            if (!searchResults.isEmpty()) {
                TFIDFSearch.Item resultItem = searchResults.get(0);
                return Bukkit.getPluginManager().getPermission(resultItem.getFieldValue("permissionName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            playerMenuUtility.getOwner().sendMessage("The Query contained Errors. Please fix them: " + e.getMessage());
        }
        return null;
    }
}
