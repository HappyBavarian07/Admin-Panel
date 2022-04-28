package de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions;/*
 * @Author HappyBavarian07
 * @Date 30.10.2021 | 12:09
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.PlaceholderType;
import de.happybavarian07.adminpanel.menusystem.PaginatedMenu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.*;

public class PermissionListMenu extends PaginatedMenu implements Listener {
    private final PlayerMenuUtility playerMenuUtility;
    private final PermissionAction action;
    private final UUID targetUUID;
    private List<Permission> permissions = new ArrayList<>();
    private PermissionListMode mode;
    private String sortQuery;

    public PermissionListMenu(PlayerMenuUtility playerMenuUtility, PermissionAction action, PermissionListMode mode, String sortQuery, UUID targetUUID) {
        super(playerMenuUtility);
        this.playerMenuUtility = playerMenuUtility;
        this.action = action;
        this.targetUUID = targetUUID;
        if(mode == null) {
            this.mode = PermissionListMode.ALL;
        } else {
            this.mode = mode;
        }
        this.sortQuery = sortQuery;
        resetPermList();
        if (!sortQuery.equals("")) {
            sortInPermList(sortQuery);
        }
        if(action == PermissionAction.ADD) {
            setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.Add");
        } else if(action == PermissionAction.REMOVE) {
            setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.Remove");
        } else if(action == PermissionAction.INFO) {
            setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.Info");
        } else if(action == PermissionAction.LIST) {
            setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.List");
        }
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.Permissions.ListMenu", null);
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        InventoryAction action2 = e.getAction();
        String path = "PlayerManager.ActionsMenu.Permissions.ListMenu.";
        Player player = (Player) e.getWhoClicked();
        Player target = Bukkit.getPlayer(targetUUID);
        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);
        ItemStack item = e.getCurrentItem();
        if (item != null) {
            if (item.equals(lgm.getItem(path + "Sort", player, false))) {
                if(action == PermissionAction.LIST) {
                    player.setMetadata("PermissionsSortMetaData-List", new FixedMetadataValue(plugin, true));
                } else if(action == PermissionAction.ADD) {
                    player.setMetadata("PermissionsSortMetaData-Add", new FixedMetadataValue(plugin, true));
                } else if(action == PermissionAction.REMOVE) {
                    player.setMetadata("PermissionsSortMetaData-Remove", new FixedMetadataValue(plugin, true));
                } else if(action == PermissionAction.INFO) {
                    player.setMetadata("PermissionsSortMetaData-Info", new FixedMetadataValue(plugin, true));
                }
                player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.ListMenu.EnterSort", player, true));
                player.closeInventory();
            } else if (item.equals(lgm.getItem(path + "Mode." + mode.getName(), player, false))) {
                this.mode = PermissionListMode.getNext(mode);
                resetPermList();
                new PermissionListMenu(playerMenuUtility, action, mode, sortQuery, targetUUID).open();
            } else if (item.equals(lgm.getItem("General.Close", null, false))) {
                if (!player.hasPermission("AdminPanel.Button.Close")) {
                    player.sendMessage(noPerms);
                    return;
                }
                new PermissionActionSelectMenu(playerMenuUtility, targetUUID).open();
            } else if (item.equals(lgm.getItem("General.Left", null, false))) {
                if (!player.hasPermission("AdminPanel.Button.pageleft")) {
                    player.sendMessage(noPerms);
                    return;
                }
                if (page == 0) {
                    player.sendMessage(lgm.getMessage("Player.General.AlreadyOnFirstPage", player, true));
                } else {
                    page = page - 1;
                    new PermissionListMenu(playerMenuUtility, action, mode, sortQuery, targetUUID).open();
                }
            } else if (item.equals(lgm.getItem("General.Right", null, false))) {
                if (!player.hasPermission("AdminPanel.Button.pageright")) {
                    player.sendMessage(noPerms);
                    return;
                }
                if (!((index + 1) >= permissions.size())) {
                    page = page + 1;
                    new PermissionListMenu(playerMenuUtility, action, mode, sortQuery, targetUUID).open();
                } else {
                    player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player, true));
                }
            } else if (item.equals(lgm.getItem("General.Refresh", null, false))) {
                if (!player.hasPermission("AdminPanel.Button.refresh")) {
                    player.sendMessage(noPerms);
                    return;
                }
                new PermissionListMenu(playerMenuUtility, action, mode, sortQuery, targetUUID).open();
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
                    if (target.hasPermission(searchInPermList(item.getItemMeta().getDisplayName()))) {
                        player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.AddedPermission", player, true));
                    }
                } else if (action.equals(PermissionAction.REMOVE)) {
                    this.mode = PermissionListMode.getNext(mode);
                    plugin.getPlayerPermissions().get(target.getUniqueId()).remove(searchInPermList(item.getItemMeta().getDisplayName()).getName());
                    plugin.getPlayerPermissionsAttachments().get(target.getUniqueId()).unsetPermission(item.getItemMeta().getDisplayName());
                    plugin.savePerms();
                    plugin.reloadPerms(target);
                    if (!target.hasPermission(searchInPermList(item.getItemMeta().getDisplayName()))) {
                        player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.RemovedPermission", player, true));
                    }
                } else if (action.equals(PermissionAction.INFO)) {
                    Permission current = searchInPermList(item.getItemMeta().getDisplayName());
                    player.sendMessage(Utils.format(player, "&aName: &6" + current.getName() + "\n" +
                            "&aDescription: &6" + current.getDescription() + "\n" +
                            "&aPermissibles: &6" + current.getPermissibles() + "\n" +
                            "&aChildrens: &6" + current.getChildren() + "\n" +
                            "&aDefault: &6" + current.getDefault() + "\n", AdminPanelMain.getPrefix()));
                }
                resetPermList();
                new PermissionListMenu(playerMenuUtility, action, mode, sortQuery, targetUUID).open();
            }
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();
        String path = "PlayerManager.ActionsMenu.Permissions.ListMenu.";
        Player player = playerMenuUtility.getOwner();
        inventory.setItem(47, lgm.getItem(path + "Sort", player, false));
        if (action == PermissionAction.INFO || action == PermissionAction.LIST) {
            inventory.setItem(52, lgm.getItem(path + "Mode." + mode.getName(), player, false));
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
                    if(action == PermissionAction.REMOVE || action == PermissionAction.LIST || action == PermissionAction.INFO) {
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
        if (player.hasMetadata("PermissionsSortMetaData-List")) {
            this.sortQuery = ChatColor.stripColor(event.getMessage());
            sortInPermList(sortQuery);
            player.removeMetadata("PermissionsSortMetaData-List", plugin);
            player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.ListMenu.SortResult", player, true));
            new PermissionListMenu(playerMenuUtility, PermissionAction.LIST, mode, sortQuery, targetUUID).open();
            event.setCancelled(true);
        } else if (player.hasMetadata("PermissionsSortMetaData-Add")) {
            this.sortQuery = ChatColor.stripColor(event.getMessage());
            sortInPermList(sortQuery);
            player.removeMetadata("PermissionsSortMetaData-Add", plugin);
            player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.ListMenu.SortResult", player, true));
            new PermissionListMenu(playerMenuUtility, PermissionAction.ADD, mode, sortQuery, targetUUID).open();
            event.setCancelled(true);
        } else if (player.hasMetadata("PermissionsSortMetaData-Remove")) {
            this.sortQuery = ChatColor.stripColor(event.getMessage());
            sortInPermList(sortQuery);
            player.removeMetadata("PermissionsSortMetaData-Remove", plugin);
            player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.ListMenu.SortResult", player, true));
            new PermissionListMenu(playerMenuUtility, PermissionAction.REMOVE, mode, sortQuery, targetUUID).open();
            event.setCancelled(true);
        } else if (player.hasMetadata("PermissionsSortMetaData-Info")) {
            this.sortQuery = ChatColor.stripColor(event.getMessage());
            sortInPermList(sortQuery);
            player.removeMetadata("PermissionsSortMetaData-Info", plugin);
            player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.ListMenu.SortResult", player, true));
            new PermissionListMenu(playerMenuUtility, PermissionAction.INFO, mode, sortQuery, targetUUID).open();
            event.setCancelled(true);
        }
    }

    private void resetPermList() {
        Player player = Bukkit.getPlayer(targetUUID);
        permissions.clear();

        if (action == PermissionAction.REMOVE) {
            for (Permission perm : Bukkit.getPluginManager().getPermissions()) {
                if (player.hasPermission(perm)) {
                    permissions.add(perm);
                }
            }
        } else if (action == PermissionAction.INFO || action == PermissionAction.LIST) {
            if (mode == PermissionListMode.ALL) {
                permissions = new ArrayList<>(Bukkit.getPluginManager().getPermissions());
            } else {
                for (Permission perm : Bukkit.getPluginManager().getPermissions()) {
                    if (player.hasPermission(perm)) {
                        permissions.add(perm);
                    }
                }
            }
        } else {
            for (Permission perm : Bukkit.getPluginManager().getPermissions()) {
                if (!player.hasPermission(perm)) {
                    permissions.add(perm);
                }
            }
        }
        if (!(this.inventory == null))
            setMenuItems();
    }

    private void sortInPermList(String sortQuery) {
        resetPermList();
        permissions.removeIf(perm -> !perm.getName().contains(sortQuery) && !perm.getName().startsWith(sortQuery) && !perm.getName().equalsIgnoreCase(sortQuery));
    }

    private Permission searchInPermList(String searchQuery) {
        permissions.removeIf(perm -> !perm.getName().contains(searchQuery) && !perm.getName().startsWith(searchQuery) && !perm.getName().equalsIgnoreCase(searchQuery));
        if (permissions.get(0) != null) {
            return permissions.get(0);
        }
        return null;
    }
}
