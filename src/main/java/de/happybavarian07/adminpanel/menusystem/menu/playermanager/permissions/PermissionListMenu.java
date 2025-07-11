package de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.PlayerActionSelectMenu;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions.utils.CustomPermission;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions.utils.PermissionGroup;
import de.happybavarian07.adminpanel.permissions.PermissionsManager;
import de.happybavarian07.adminpanel.utils.tfidfsearch.TFIDFSearch;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
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

    private static List<ItemStack> menuItemsCache = null;
    private static PermissionGroup lastCachedGroup = null;
    private static java.util.UUID lastCachedTargetUUID = null;
    private static String lastProcessedSearchQuery = null;
    private final PlayerMenuUtility playerMenuUtility;
    private final PermissionsManager permissionsManager;
    private final java.util.UUID targetUUID;
    private PermissionGroup rootGroup;
    private PermissionGroup currentGroup;
    private String sortQuery;
    private int filteredResultCount = 0;
    private PermissionFilterMode filterMode;

    public PermissionListMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.Open");
        this.playerMenuUtility = playerMenuUtility;
        this.permissionsManager = AdminPanelMain.getPlugin().getPermissionsManager();
        this.targetUUID = playerMenuUtility.getTargetUUID();
        this.sortQuery = playerMenuUtility.getData("SortQuery", String.class);
        this.filterMode = playerMenuUtility.getData("PermissionFilterMode", PermissionFilterMode.class);
        if (this.filterMode == null) {
            this.filterMode = PermissionFilterMode.GLOBAL;
        }

        buildPermissionTree(new ArrayList<>());
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
    public String getConfigMenuAddonFeatureName() {
        return "PermissionListMenu";
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

        if (sortQuery != null && !sortQuery.isEmpty() && !sortQuery.equals(lastProcessedSearchQuery)) {
            List<String> searchResults = tfidfSearchPermissions(sortQuery);
            if (searchResults.isEmpty()) {
                buildPermissionTree(new ArrayList<>());
            } else {
                buildPermissionTree(searchResults);
            }
            this.currentGroup = rootGroup;
            playerMenuUtility.setData("CurrentPermissionGroup", currentGroup, true);
            lastProcessedSearchQuery = sortQuery;
            menuItemsCache = null;
        } else if (sortQuery == null || sortQuery.isEmpty()) {
            if (lastProcessedSearchQuery != null) {
                buildPermissionTree(new ArrayList<>());
                this.currentGroup = rootGroup;
                playerMenuUtility.setData("CurrentPermissionGroup", currentGroup, true);
                lastProcessedSearchQuery = null;
                menuItemsCache = null;
            }
        }

        String groupFullName = currentGroup.getFullName();
        String parentGroups = (currentGroup.getParent() != null)
                ? currentGroup.getParent().getFullName() : "None";
        int subGroupCount = currentGroup.getSubGroups().size();
        int subPermissionCount = currentGroup.getPermissions().size();
        lgm.addPlaceholder(PlaceholderType.ITEM, "%group%", groupFullName, false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%parent_groups%", parentGroups, false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%sub_group_count%", String.valueOf(subGroupCount), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%sub_permission_count%", String.valueOf(subPermissionCount), false);

        if (!currentGroup.getName().equals("root")) {
            inventory.setItem(getSlot("General.Back", 45), lgm.getItem("General.Back", player, false));
            inventory.setItem(getSlot(path + "ToTheTop", 46), lgm.getItem(path + "ToTheTop", player, false));
        }
        inventory.setItem(getSlot(path + "GroupMenu.Sort", 47), lgm.getItem(path + "GroupMenu.Sort", player, false));

        if (filterMode == PermissionFilterMode.GLOBAL) {
            inventory.setItem(getSlot(path + "GroupMenu.Mode.Global", 52), lgm.getItem(path + "GroupMenu.Mode.Global", player, false));
        } else if (filterMode == PermissionFilterMode.PLAYER) {
            inventory.setItem(getSlot(path + "GroupMenu.Mode.Player", 52), lgm.getItem(path + "GroupMenu.Mode.Player", player, false));
        }

        String filterStatus = "Not filtered.";
        if (sortQuery != null && !sortQuery.isEmpty()) {
            if (filteredResultCount == 0) {
                filterStatus = "No results found for: " + sortQuery;
            } else {
                filterStatus = "Showing " + filteredResultCount + " results for: " + sortQuery;
            }
        }
        lgm.addPlaceholder(PlaceholderType.ITEM, "%filtered_result_count%", filterStatus, false);
        ItemStack infoItem = lgm.getItem(path + "InfoItem", player, false);
        inventory.setItem(getSlot(path + "InfoItem", 4), infoItem);

        List<ItemStack> menuItems;

        boolean isSearchActive = sortQuery != null && !sortQuery.isEmpty();
        boolean shouldRebuildCache = menuItemsCache == null ||
                lastCachedGroup != currentGroup ||
                !targetUUID.equals(lastCachedTargetUUID);

        if (shouldRebuildCache) {
            menuItemsCache = new ArrayList<>();

            for (PermissionGroup group : currentGroup.getSubGroups().values()) {
                try {
                    if (filterMode == PermissionFilterMode.PLAYER) {
                        boolean groupHasPlayerPermissions = hasPlayerPermissionsInGroup(group, Bukkit.getPlayer(targetUUID));
                        if (!groupHasPlayerPermissions) {
                            continue;
                        }
                    }

                    lgm.addPlaceholder(PlaceholderType.ITEM, "%group_name%", group.getFullName(), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%group_item%", group.getFullName(), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%value%", Bukkit.getPlayer(targetUUID).hasPermission(group.getFullName()), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%sub_group_count%", String.valueOf(group.getSubGroups().size()), false);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%sub_permission_count%", String.valueOf(group.getPermissions().size()), false);

                    ItemStack groupItem = lgm.getItem(path + "GroupMenu.GroupItem", player, false);
                    ItemMeta meta = groupItem.getItemMeta();
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(AdminPanelMain.getPlugin(), "Group"),
                            PersistentDataType.STRING, group.getName());
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(AdminPanelMain.getPlugin(), "PermissionGroupItem"),
                            PersistentDataType.BOOLEAN, true);
                    meta.setLocalizedName(group.getName());
                    groupItem.setItemMeta(meta);
                    menuItemsCache.add(groupItem);
                } catch (Exception e) {
                    menuItemsCache = null;
                    player.sendMessage("§cFehler beim Laden der Gruppeninformationen: " + e.getMessage());
                    break;
                }
            }

            if (menuItemsCache != null) {
                for (CustomPermission cp : currentGroup.getPermissions()) {
                    try {
                        Player targetPlayer = Bukkit.getPlayer(targetUUID);
                        boolean playerHasPermission = targetPlayer != null && targetPlayer.hasPermission(cp.getFullPermission());

                        if (filterMode == PermissionFilterMode.PLAYER && !playerHasPermission) {
                            continue;
                        }

                        lgm.addPlaceholder(PlaceholderType.ITEM, "%permission_name%", cp.getFinalPermissionPart(), false);
                        lgm.addPlaceholder(PlaceholderType.ITEM, "%full_permission_name%", cp.getFullPermission(), false);
                        lgm.addPlaceholder(PlaceholderType.ITEM, "%value%", playerHasPermission, false);

                        ItemStack permItem = lgm.getItem(path + "GroupMenu.PermissionItem", player, false);
                        ItemMeta meta = permItem.getItemMeta();
                        assert meta != null;
                        meta.getPersistentDataContainer().set(
                                new NamespacedKey(AdminPanelMain.getPlugin(), "Permission"),
                                PersistentDataType.STRING, cp.getFullPermission());
                        meta.getPersistentDataContainer().set(
                                new NamespacedKey(AdminPanelMain.getPlugin(), "CustomPermissionItem"),
                                PersistentDataType.BOOLEAN, true);
                        meta.setLocalizedName(cp.getFullPermission());
                        permItem.setItemMeta(meta);
                        menuItemsCache.add(permItem);
                    } catch (Exception e) {
                        menuItemsCache = null;
                        player.sendMessage("§cFehler beim Laden der Berechtigungsinformationen: " + e.getMessage());
                        break;
                    }
                }
            }

            if (menuItemsCache != null && !isSearchActive) {
                lastCachedGroup = currentGroup;
                lastCachedTargetUUID = targetUUID;
            }
        }

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
            if (sortQuery != null && !sortQuery.isEmpty()) {
                List<String> searchResults = tfidfSearchPermissions(sortQuery);
                if (searchResults.isEmpty()) {
                    buildPermissionTree(new ArrayList<>());
                } else {
                    buildPermissionTree(searchResults);
                }
            } else {
                buildPermissionTree(new ArrayList<>());
            }
            playerMenuUtility.setData("CurrentPermissionGroup", rootGroup, true);
            this.currentGroup = rootGroup;
            new PermissionListMenu(playerMenuUtility).open();
        } else if (clickedItem.getItemMeta() != null && clickedItem.hasItemMeta() &&
                clickedItem.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(AdminPanelMain.getPlugin(), "CustomPermissionItem"), PersistentDataType.BOOLEAN)) {
            String permFullName = clickedItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(AdminPanelMain.getPlugin(), "Permission"), PersistentDataType.STRING);
            playerMenuUtility.setData("SelectedPermission", permFullName, true);
            new PermissionActionMenu(playerMenuUtility).open();
        } else if (clickedItem.getItemMeta() != null && clickedItem.hasItemMeta() &&
                clickedItem.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(AdminPanelMain.getPlugin(), "PermissionGroupItem"), PersistentDataType.BOOLEAN)) {
            String groupName = clickedItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(AdminPanelMain.getPlugin(), "Group"), PersistentDataType.STRING);
            PermissionGroup nextGroup = currentGroup.getSubGroups().get(groupName);
            if (nextGroup != null) {
                playerMenuUtility.setData("CurrentPermissionGroup", nextGroup, true);
                this.currentGroup = nextGroup;
                new PermissionListMenu(playerMenuUtility).open();
            }
        } else if (clickedItem.isSimilar(lgm.getItem("General.Left", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.pageleft")) {
                player.sendMessage(lgm.getPermissionMessage(player, "AdminPanel.Button.pageleft"));
                return;
            }
            if (page == 0) {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnFirstPage", player, true));
            } else {
                page = page - 1;
                new PermissionListMenu(playerMenuUtility).open();
            }
        } else if (clickedItem.isSimilar(lgm.getItem("General.Right", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.pageright")) {
                player.sendMessage(lgm.getPermissionMessage(player, "AdminPanel.Button.pageright"));
                return;
            }
            if (!((index + 1) >= menuItemsCache.size())) {
                page = page + 1;
                super.open();
            } else {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player, true));
            }
        } else if (clickedItem.isSimilar(lgm.getItem("General.Refresh", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.refresh")) {
                player.sendMessage(lgm.getPermissionMessage(player, "AdminPanel.Button.refresh"));
                return;
            }
            sortQuery = null;
            playerMenuUtility.removeData("SortQuery");
            playerMenuUtility.removeData("PermissionsSortMetaData");
            menuItemsCache = null;
            lastCachedGroup = null;
            lastCachedTargetUUID = null;
            buildPermissionTree(new ArrayList<>());
            this.currentGroup = rootGroup;
            playerMenuUtility.setData("CurrentPermissionGroup", currentGroup, true);
            new PermissionListMenu(playerMenuUtility).open();
        } else if (clickedItem.isSimilar(lgm.getItem(path + "GroupMenu.Mode.Player", player, false))) {
            filterMode = PermissionFilterMode.GLOBAL;
            playerMenuUtility.setData("PermissionFilterMode", filterMode, true);
            new PermissionListMenu(playerMenuUtility).open();
        } else if (clickedItem.isSimilar(lgm.getItem(path + "GroupMenu.Mode.Global", player, false))) {
            filterMode = PermissionFilterMode.PLAYER;
            playerMenuUtility.setData("PermissionFilterMode", filterMode, true);
            new PermissionListMenu(playerMenuUtility).open();
        }
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
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

            menuItemsCache = null;
            lastCachedGroup = null;
            lastCachedTargetUUID = null;
            lastProcessedSearchQuery = null;

            if (sortQuery != null && !sortQuery.isEmpty()) {
                List<String> searchResults = tfidfSearchPermissions(sortQuery);
                if (searchResults.isEmpty()) {
                    player.sendMessage("No permissions found matching: " + sortQuery);
                    buildPermissionTree(new ArrayList<>());
                } else {
                    player.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.GroupMenu.SortResult", player, true));
                    buildPermissionTree(searchResults);
                }
            } else {
                buildPermissionTree(new ArrayList<>());
                player.sendMessage("Search cleared - showing all permissions");
            }

            this.currentGroup = rootGroup;
            playerMenuUtility.setData("CurrentPermissionGroup", currentGroup, true);

            super.open();
            event.setCancelled(true);
        }
    }

    private void buildPermissionTree(List<String> permissions) {
        rootGroup = new PermissionGroup("root", null);
        if (permissions != null && !permissions.isEmpty()) {
            for (String permission : permissions) {
                String[] parts = permission.split("\\.");
                PermissionGroup current = rootGroup;

                if (permission.endsWith(".*")) {
                    String[] partsWithoutWildcard = permission.substring(0, permission.length() - 2).split("\\.");
                    for (String s : partsWithoutWildcard) {
                        current = current.getOrCreateSubGroup(s);
                    }
                    current.addPermission(new CustomPermission("*", current));
                } else {
                    for (int i = 0; i < parts.length - 1; i++) {
                        current = current.getOrCreateSubGroup(parts[i]);
                    }
                    String finalPart = parts[parts.length - 1];
                    current.addPermission(new CustomPermission(finalPart, current));
                }
            }
        } else if (sortQuery == null || sortQuery.isEmpty()) {
            for (Permission perm : Bukkit.getPluginManager().getPermissions()) {
                String[] parts = perm.getName().split("\\.");
                PermissionGroup current = rootGroup;

                if (perm.getName().endsWith(".*")) {
                    String[] partsWithoutWildcard = perm.getName().substring(0, perm.getName().length() - 2).split("\\.");
                    for (String s : partsWithoutWildcard) {
                        current = current.getOrCreateSubGroup(s);
                    }
                    current.addPermission(new CustomPermission("*", current));
                } else {
                    for (int i = 0; i < parts.length - 1; i++) {
                        current = current.getOrCreateSubGroup(parts[i]);
                    }
                    String finalPart = parts[parts.length - 1];
                    current.addPermission(new CustomPermission(finalPart, current));
                }
            }
        }
    }

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

    private boolean hasPlayerPermissionsInGroup(PermissionGroup group, Player player) {
        if (group == null || player == null) {
            return false;
        }

        for (CustomPermission permission : group.getPermissions()) {
            if (player.hasPermission(permission.getFullPermission())) {
                return true;
            }
        }

        return false;
    }
}
