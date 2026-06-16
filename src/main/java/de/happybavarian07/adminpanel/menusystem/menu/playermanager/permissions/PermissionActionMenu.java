package de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.PlayerActionSelectMenu;
import de.happybavarian07.adminpanel.permissions.PermissionsManager;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.PermissionAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import java.util.Objects;

public class PermissionActionMenu extends Menu {

    private final PlayerMenuUtility playerMenuUtility;
    private final String permissionFullName;
    private final PermissionsManager permissionsManager;
    private final java.util.UUID targetUUID;

    public PermissionActionMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Permissions.Actions");
        this.playerMenuUtility = playerMenuUtility;
        this.permissionFullName = playerMenuUtility.getData("SelectedPermission", String.class);
        this.targetUUID = playerMenuUtility.getTargetUUID();
        this.permissionsManager = AdminPanelMain.getPlugin().getPermissionsManager();
    }

    @Override
    public String getMenuName() {
        return "Permission: " + permissionFullName;
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PermissionActionMenu";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "PlayerManager.ActionsMenu.PermissionListMenu.PermissionActions.";

        // General Info Item
        /*
            # %permission% = The Permission Name
            # %description% = The Permission Description
            # %default% = The Default Value
            # %value% = The Current Value
            # %target% = The Targeted Player*/
        lgm.addPlaceholder(PlaceholderType.ITEM, "%permission%", permissionFullName, false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%description%", Bukkit.getPluginManager().getPermission(permissionFullName).getDescription(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%default%", Bukkit.getPluginManager().getPermission(permissionFullName).getDefault().toString(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%value%", Bukkit.getPlayer(targetUUID).hasPermission(permissionFullName) ? "true" : "false", false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%target%", Bukkit.getPlayer(targetUUID).getName(), false);

        ItemStack infoItem = lgm.getItem(path + "InfoItem", player, false);

        ItemStack trueButton = lgm.getItem(path + "SetTrue", player, false);
        ItemStack falseButton = lgm.getItem(path + "SetFalse", player, false);
        ItemStack removeButton = lgm.getItem(path + "Remove", player, false);
        ItemStack infoButton = lgm.getItem(path + "Info", player, false);
        ItemStack closeButton = lgm.getItem("General.Close", player, false);

        inventory.setItem(getSlot(path + "InfoItem", 4), infoItem);
        registerButton(getSlot(path + "SetTrue", 10), trueButton, (p, e) -> {
            permissionsManager.addPermission(targetUUID, permissionFullName, true, true);
            if (Bukkit.getPlayer(targetUUID).hasPermission(permissionFullName)) {
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%value%", true, false);
                p.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.AddedPermission", p, true));
            }
            open();
        }, null);
        registerButton(getSlot(path + "SetFalse", 12), falseButton, (p, e) -> {
            permissionsManager.addPermission(targetUUID, permissionFullName, false, true);
            if (Bukkit.getPlayer(targetUUID).hasPermission(permissionFullName)) {
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%value%", false, false);
                p.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.AddedPermission", p, true));
            }
            open();
        }, null);
        registerButton(getSlot(path + "Remove", 14), removeButton, (p, e) -> {
            permissionsManager.removePermission(targetUUID, permissionFullName, true);
            if (!Bukkit.getPlayer(targetUUID).hasPermission(permissionFullName)) {
                p.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.RemovedPermission", p, true));
            }
            new PermissionListMenu(playerMenuUtility, new PlayerActionSelectMenu(playerMenuUtility)).open();
        }, null);
        registerButton(getSlot(path + "Info", 16), infoButton, (p, e) -> {
            Permission perm = Bukkit.getPluginManager().getPermission(permissionFullName);
            if (perm != null) {
                p.sendMessage(AdminPanelUtils.format(p,
                        "Info about Permission:" + "\n" +
                                "  - &aName: &6" + perm.getName() + "\n" +
                                "  - &aDescription: &6" + (perm.getDescription().isEmpty() ? perm.getDescription() : "None") + "\n" +
                                "  - &aDefault: &6" + perm.getDefault() + "\n" +
                                "  - &aValue: &6" + (Objects.requireNonNull(Bukkit.getPlayer(targetUUID)).hasPermission(permissionFullName) ? "true" : "false") + "\n" +
                                "  - &aTarget: &6" + Objects.requireNonNull(Bukkit.getPlayer(targetUUID)).getName() + "\n" +
                                "  - &aChildren: &6" + (perm.getChildren().isEmpty() ? perm.getChildren().toString() : "None") + "\n", AdminPanelMain.getPrefix()));
            } else {
                p.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.ListMenu.PermissionNotFound", p, true));
            }
        }, null);
        registerButton(getSlot("General.Close", 22), closeButton,
                new PermissionAction("AdminPanel.Button.Close", (p, e) ->
                        new PermissionListMenu(playerMenuUtility, new PlayerActionSelectMenu(playerMenuUtility)).open(), lgm), null);
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        // Logic is now handled by registered MenuActions in setMenuItems
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
