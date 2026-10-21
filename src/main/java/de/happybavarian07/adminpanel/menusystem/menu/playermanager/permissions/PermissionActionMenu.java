package de.happybavarian07.adminpanel.menusystem.menu.playermanager.permissions;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.PlayerActionSelectMenu;
import de.happybavarian07.adminpanel.permissions.PermissionsManager;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;


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
        ItemStack closeButton = lgm.getItem("General.Back", player, false);

        inventory.setItem(getSlot(path + "InfoItem", 4), infoItem);
        registerButton(getSlot(path + "SetTrue", 10), trueButton, (p, e) -> {
            permissionsManager.addPermission(targetUUID, permissionFullName, true, true);
            sendSet(p, true);
            open();
        }, null);
        registerButton(getSlot(path + "SetFalse", 12), falseButton, (p, e) -> {
            permissionsManager.addPermission(targetUUID, permissionFullName, false, true);
            sendSet(p, false);
            open();
        }, null);
        registerButton(getSlot(path + "Remove", 14), removeButton, (p, e) -> {
            permissionsManager.removePermission(targetUUID, permissionFullName, true);
            CommandText.send(lgm, p, "Player.PlayerManager.Permissions.PermissionRemoved", "%permission%", permissionFullName,
                    "%target%", targetName());
            new PermissionListMenu(playerMenuUtility, new PlayerActionSelectMenu(playerMenuUtility)).open();
        }, null);
        registerButton(getSlot(path + "Info", 16), infoButton, (p, e) -> {
            Permission perm = Bukkit.getPluginManager().getPermission(permissionFullName);
            if (perm != null) {
                Player target = Bukkit.getPlayer(targetUUID);
                String none = lgm.getMessage("Player.PlayerManager.Permissions.PermissionInfoNone", p, true);
                CommandText.send(lgm, p, "Player.PlayerManager.Permissions.PermissionInfo",
                        "%permission%", perm.getName(),
                        "%description%", perm.getDescription().isEmpty() ? none : perm.getDescription(),
                        "%default%", perm.getDefault(),
                        "%value%", target != null && target.hasPermission(permissionFullName),
                        "%target%", targetName(),
                        "%children%", perm.getChildren().isEmpty() ? none : perm.getChildren().toString());
            } else {
                p.sendMessage(lgm.getMessage("Player.PlayerManager.Permissions.GroupMenu.PermissionNotFound", p, true));
            }
        }, null);
        registerButton(getSlot("General.Close", 22), closeButton,
                MenuAction.of((p, e) ->
                        new PermissionListMenu(playerMenuUtility, new PlayerActionSelectMenu(playerMenuUtility)).open()).requires("AdminPanel.Button.Close"), null);
    }

    private String targetName() {
        Player target = Bukkit.getPlayer(targetUUID);
        return target != null ? target.getName() : targetUUID.toString();
    }

    private void sendSet(Player staff, boolean value) {
        CommandText.send(lgm, staff, "Player.PlayerManager.Permissions.PermissionSet", "%permission%", permissionFullName,
                "%value%", value, "%target%", targetName());
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
