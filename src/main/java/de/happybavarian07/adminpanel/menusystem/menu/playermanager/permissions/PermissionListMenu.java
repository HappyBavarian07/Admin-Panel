package de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions;

import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.menusystem.PaginatedMenu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.PlayerActionSelectMenu;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions.utils.CustomPermission;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions.utils.PermissionGroup;
import de.happybavarian07.adminpanel.permissions.PermissionsManager;
import de.happybavarian07.adminpanel.utils.tfidfsearch.TFIDFSearch;
import org.bukkit.Bukkit;
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
import org.bukkit.permissions.Permission;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class PermissionListMenu extends PaginatedMenu implements Listener {

    private final PlayerMenuUtility playerMenuUtility;
    private final PermissionsManager permissionsManager;
    private final java.util.UUID targetUUID;
    private PermissionGroup rootGroup;
    private PermissionGroup currentGroup; // current group being viewed
    private String sortQuery;
    private static List<ItemStack> menuItemsCache = null;
    private static PermissionGroup lastCachedGroup = null;
    private static java.util.UUID lastCachedTargetUUID = null;
    private int filteredResultCount = 0;

    public PermissionListMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.Open");
        this.playerMenuUtility = playerMenuUtility;
        this.permissionsManager = plugin.getPermissionsManager();
        this.targetUUID = playerMenuUtility.getTargetUUID();
        this.sortQuery = playerMenuUtility.getData("SortQuery", String.class);
        // Build the permission tree from Bukkit's registered permissions.
        buildPermissionTree(new ArrayList<>());
        // If there is a current group stored, load it; otherwise, start from the root.
        this.currentGroup = playerMenuUtility.getData("CurrentPermissionGroup", PermissionGroup.class);
        if (this.currentGroup == null) {
            this.currentGroup = rootGroup;
        }
    }

    @Override
    public String getMenuName() {
        Player target = Bukkit.getPlayer(targetUUID);
        lgm.addPlaceholder(PlaceholderType.MENUTITLE, "%target%", (target != null ? target.getName() : "Unknown"), false);
        lgm.addPlaceholder(PlaceholderType.MENUTITLE, "%group%", currentGroup.getFullName(), false);
        return lgm.getMenuTitle("PlayerManager.Permissions.GroupMenu", null);
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();
        Player player = playerMenuUtility.getOwner();
        String path = "PlayerManager.ActionsMenu.PermissionListMenu.";

        // --- Info Item at the top ----------------------
        // Get dynamic data: full group info, parent chain, sub-group and permission counts.
        String groupFullName = currentGroup.getFullName();
        String parentGroups = (currentGroup.getParent() != null)
                ? currentGroup.getParent().getFullName() : "None";
        int subGroupCount = currentGroup.getSubGroups().size();
        int subPermissionCount = currentGroup.getPermissions().size();
        // Set dynamic placeholders (the placeholder type used here is arbitrary; adjust as needed):
        lgm.addPlaceholder(PlaceholderType.ITEM, "%group%", groupFullName, false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%parent_groups%", parentGroups, false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%sub_group_count%", String.valueOf(subGroupCount), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%sub_permission_count%", String.valueOf(subPermissionCount), false);
        // ------------------------------------------------

        // Navigation: Back and Sort items from your language manager.
        if (!currentGroup.getName().equals("root")) {
            inventory.setItem(getSlot("General.Back", 45), lgm.getItem("General.Back", player, false));
            inventory.setItem(getSlot(path + "ToTheTop", 46), lgm.getItem(path + "ToTheTop", player, false));
        }
        inventory.setItem(getSlot(path + "GroupMenu.Sort", 47), lgm.getItem(path + "GroupMenu.Sort", player, false));

        // If a sort query is active, display a flat list from TFIDF search.
        if (sortQuery != null && !sortQuery.isEmpty()) {
            buildPermissionTree(tfidfSearchPermissions(sortQuery));
        }

        lgm.addPlaceholder(PlaceholderType.ITEM, "%filtered_result_count%", (filteredResultCount == 0) ? "Not filtered." : filteredResultCount, false);
        // Retrieve the info item from the language manager (must be defined in your config)
        ItemStack infoItem = lgm.getItem(path + "InfoItem", player, false);
        // Place the info item in a fixed slot (e.g., slot 4) so that it’s always visible.
        inventory.setItem(getSlot(path + "InfoItem", 4), infoItem);

        // Build a list of items from the tree structure.
        List<ItemStack> menuItems;

        // Nur neu generieren, wenn sich etwas geändert hat oder der Cache leer ist
        if (menuItemsCache == null || lastCachedGroup != currentGroup || !lastCachedTargetUUID.equals(targetUUID)) {
            menuItemsCache = new ArrayList<>();

            // Untergruppen als CHEST-Items hinzufügen
            for (PermissionGroup group : currentGroup.getSubGroups().values()) {
                try {
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%group_name%", group.getFullName(), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%group_item%", group.getFullName(), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%value%", Bukkit.getPlayer(targetUUID).hasPermission(group.getFullName()), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%sub_group_count%", String.valueOf(group.getSubGroups().size()), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%sub_permission_count%", String.valueOf(group.getPermissions().size()), false);

                    ItemStack groupItem = lgm.getItem(path + "GroupMenu.GroupItem", player, false);
                    ItemMeta meta = groupItem.getItemMeta();
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(plugin, "Group"),
                            PersistentDataType.STRING, group.getName());
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(plugin, "PermissionGroupItem"),
                            PersistentDataType.BOOLEAN, true);
                    meta.setLocalizedName(group.getName());
                    groupItem.setItemMeta(meta);
                    menuItemsCache.add(groupItem);
                } catch (Exception e) {
                    // Bei Fehlern Cache leeren und erneut versuchen
                    menuItemsCache = null;
                    player.sendMessage("§cFehler beim Laden der Gruppeninformationen: " + e.getMessage());
                    break;
                }
            }

            // Wenn der Cache gültig ist, Berechtigungen als ENCHANTED_BOOK-Items hinzufügen
            if (menuItemsCache != null) {
                for (CustomPermission cp : currentGroup.getPermissions()) {
                    try {
                        lgm.addPlaceholder(PlaceholderType.ITEM, "%permission_name%", cp.getFinalPermissionPart(), false);
                        lgm.addPlaceholder(PlaceholderType.ITEM, "%full_permission_name%", cp.getFullPermission(), false);
                        lgm.addPlaceholder(PlaceholderType.ITEM, "%value%", Bukkit.getPlayer(targetUUID).hasPermission(cp.getFullPermission()), false);

                        ItemStack permItem = lgm.getItem(path + "GroupMenu.PermissionItem", player, false);
                        ItemMeta meta = permItem.getItemMeta();
                        meta.getPersistentDataContainer().set(
                                new NamespacedKey(plugin, "Permission"),
                                PersistentDataType.STRING, cp.getFullPermission());
                        meta.getPersistentDataContainer().set(
                                new NamespacedKey(plugin, "CustomPermissionItem"),
                                PersistentDataType.BOOLEAN, true);
                        meta.setLocalizedName(cp.getFullPermission());
                        permItem.setItemMeta(meta);
                        menuItemsCache.add(permItem);
                    } catch (Exception e) {
                        // Bei Fehlern Cache leeren und aufgeben
                        menuItemsCache = null;
                        player.sendMessage("§cFehler beim Laden der Berechtigungsinformationen: " + e.getMessage());
                        break;
                    }
                }
            }

            // Aktuelle Gruppe und Ziel-UUID im Cache speichern
            if (menuItemsCache != null) {
                lastCachedGroup = currentGroup;
                lastCachedTargetUUID = targetUUID;
            }
        }

        // Wenn der Cache gültig ist, ihn verwenden, sonst leere Liste
        menuItems = (menuItemsCache != null) ? menuItemsCache : new ArrayList<>();

        // TODO Maybe implement a new Menu which gives you control over the Permission Database itself instead of only players

        /////////////////////////////////////
        // Old-style pagination loop:
        if (!menuItems.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= menuItems.size()) break;

                inventory.addItem(menuItems.get(index));
            }
        }
        /////////////////////////////////////
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        String path = "PlayerManager.ActionsMenu.PermissionListMenu.";
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null) return;

        if (clickedItem.isSimilar(lgm.getItem("General.Close", player, false))) {
            if (currentGroup.getParent() != null) {
                playerMenuUtility.setData("CurrentPermissionGroup", currentGroup.getParent(), true);
            }
            new PlayerActionSelectMenu(playerMenuUtility).open();
        } else if (clickedItem.isSimilar(lgm.getItem(path + "GroupMenu.Sort", player, false))) {
            if (sortQuery == null) sortQuery = "";
            player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.GroupMenu.EnterSort", player, true));
            playerMenuUtility.addData("PermissionsSortMetaData", true);
            player.closeInventory();
        } else if (clickedItem.isSimilar(lgm.getItem("General.Back", player, false))) {
            if (currentGroup.getParent() != null) {
                playerMenuUtility.setData("CurrentPermissionGroup", currentGroup.getParent(), true);
                new PermissionListMenu(playerMenuUtility).open();
            } else {
                player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.GroupMenu.NoParent", player, true));
            }
        } else if (clickedItem.isSimilar(lgm.getItem(path + "ToTheTop", player, false))) {
            playerMenuUtility.setData("CurrentPermissionGroup", rootGroup, true);
            new PermissionListMenu(playerMenuUtility).open();
        } else if (clickedItem.getItemMeta() != null && clickedItem.hasItemMeta() &&
                clickedItem.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "CustomPermissionItem"), PersistentDataType.BOOLEAN)) {
            String permFullName = clickedItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "Permission"), PersistentDataType.STRING);
            playerMenuUtility.setData("SelectedPermission", permFullName, true);
            new PermissionActionMenu(playerMenuUtility).open();
        } else if (clickedItem.getItemMeta() != null && clickedItem.hasItemMeta() &&
                clickedItem.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "PermissionGroupItem"), PersistentDataType.BOOLEAN)) {
            String groupName = clickedItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "Group"), PersistentDataType.STRING);
            PermissionGroup nextGroup = currentGroup.getSubGroups().get(groupName);
            if (nextGroup != null) {
                playerMenuUtility.setData("CurrentPermissionGroup", nextGroup, true);
                new PermissionListMenu(playerMenuUtility).open();
            }
        }
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
        // Reset the Menu Item Cache when the menu is closed
        // and the player doesn't have the data tag 'PermissionsSortMetaData'
        Player player = (Player) e.getPlayer();
        if (!playerMenuUtility.getOwner().equals(player)) return;
        if (!playerMenuUtility.hasData("PermissionsSortMetaData")) {
            menuItemsCache = null;
            lastCachedGroup = null;
            lastCachedTargetUUID = null;
        }
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!playerMenuUtility.getOwner().equals(player)) return;
        if (playerMenuUtility.hasData("PermissionsSortMetaData")) {
            this.sortQuery = ChatColor.stripColor(event.getMessage());
            playerMenuUtility.setData("SortQuery", sortQuery, true);
            playerMenuUtility.removeData("PermissionsSortMetaData");
            player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.GroupMenu.SortResult", player, true));
            new PermissionListMenu(playerMenuUtility).open();
            event.setCancelled(true);
        }
    }

    // Build the hierarchical permission tree by splitting on dots.
    private void buildPermissionTree(List<String> permissions) {
        rootGroup = new PermissionGroup("root", null);
        if (permissions != null && !permissions.isEmpty()) {
            for (String permission : permissions) {
                String[] parts = permission.split("\\.");
                PermissionGroup current = rootGroup;

                if (permission.endsWith(".*")) {
                    // Bei Wildcards (.*) den letzten Teil als "*" behalten
                    String[] partsWithoutWildcard = permission.substring(0, permission.length() - 2).split("\\.");
                    for (String s : partsWithoutWildcard) {
                        current = current.getOrCreateSubGroup(s);
                    }
                    current.addPermission(new CustomPermission("*", current));
                } else {
                    // Normale Berechtigungen wie zuvor verarbeiten
                    for (int i = 0; i < parts.length - 1; i++) {
                        current = current.getOrCreateSubGroup(parts[i]);
                    }
                    String finalPart = parts[parts.length - 1];
                    current.addPermission(new CustomPermission(finalPart, current));
                }
            }
        } else {
            for (Permission perm : Bukkit.getPluginManager().getPermissions()) {
                String[] parts = perm.getName().split("\\.");
                PermissionGroup current = rootGroup;

                if (perm.getName().endsWith(".*")) {
                    // Bei Wildcards (.*) den letzten Teil als "*" behalten
                    String[] partsWithoutWildcard = perm.getName().substring(0, perm.getName().length() - 2).split("\\.");
                    for (String s : partsWithoutWildcard) {
                        current = current.getOrCreateSubGroup(s);
                    }
                    current.addPermission(new CustomPermission("*", current));
                } else {
                    // Normale Berechtigungen wie zuvor verarbeiten
                    for (int i = 0; i < parts.length - 1; i++) {
                        current = current.getOrCreateSubGroup(parts[i]);
                    }
                    String finalPart = parts[parts.length - 1];
                    current.addPermission(new CustomPermission(finalPart, current));
                }
            }
        }
    }

    // Use TFIDF search to return a list of full permission strings.
    private List<String> tfidfSearchPermissions(String query) {
        List<String> results = new ArrayList<>();
        try {
            List<TFIDFSearch.Item> searchResults = permissionsManager.getPermissionSearcher().search(query);
            for (TFIDFSearch.Item item : searchResults) {
                String permissionName = item.getFieldValue("permissionName");
                results.add(permissionName);
            }
            this.filteredResultCount = results.size();
        } catch (Exception e) {
            e.printStackTrace();
            playerMenuUtility.getOwner().sendMessage("The Query contained errors. Please fix them: " + e.getMessage());
        }
        return results;
    }
}
