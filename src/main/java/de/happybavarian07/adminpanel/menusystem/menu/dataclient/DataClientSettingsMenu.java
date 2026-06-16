package de.happybavarian07.adminpanel.menusystem.menu.dataclient;/*
 * @Author HappyBavarian07
 * @Date 11.10.2023 | 15:58
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.syncing.managers.SettingsManager;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.PermissionAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChatEvent;

public class DataClientSettingsMenu extends Menu implements Listener {
    private final SettingsManager settingsManager;

    public DataClientSettingsMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        this.settingsManager = AdminPanelMain.getPlugin().getDataClient().getSettingsManager();
        setOpeningPermission("AdminPanel.DataClient.Menu.Settings.Open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("DataClientSettingsMenu", playerMenuUtility.getOwner());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "DataClientSettingsMenu";
    }

    @Override
    public int getSlots() {
        return 27;
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

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playerMenuUtility.getOwner() != player) return;

        if (playerMenuUtility.getData("AutoCheckConnectionTiming") != null) {
            event.setCancelled(true);
            String message = event.getMessage();
            try {
                int time = Integer.parseInt(message);
                settingsManager.setCheckConnectionTiming(time);
                playerMenuUtility.removeData("AutoCheckConnectionTiming");
                player.sendMessage(lgm.getMessage("DataClient.Menu.AutoCheckConnectingTimingChatSuccess", player, true));
                super.open();
            } catch (NumberFormatException e) {
                player.sendMessage(lgm.getMessage("Player.Commands.NotANumber", player, true));
            }
        } else if (playerMenuUtility.getData("FileLoggingPrefix") != null) {
            event.setCancelled(true);
            String message = event.getMessage();
            settingsManager.setFileLoggingPrefix(message);
            playerMenuUtility.removeData("FileLoggingPrefix");
            player.sendMessage(lgm.getMessage("DataClient.Menu.FileLoggingPrefixChatSuccess", player, true));
            super.open();
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "DataClientMenu.MainMenu.Settings.";

        registerButton(10, lgm.getItem(path + "AutoCheckConnection." + settingsManager.isCheckConnection(), player, false),
                new PermissionAction("AdminPanel.DataClient.Menu.Settings.AutoCheckConnection", (p, e) -> {
                    settingsManager.setCheckConnection(!settingsManager.isCheckConnection());
                    super.open();
                }, lgm), null);
        registerButton(11, lgm.getItem(path + "Debug." + settingsManager.isDebugEnabled(), player, false),
                new PermissionAction("AdminPanel.DataClient.Menu.Settings.Debug", (p, e) -> {
                    settingsManager.setDebugEnabled(!settingsManager.isDebugEnabled());
                    super.open();
                }, lgm), null);
        registerButton(12, lgm.getItem(path + "OverwritePermissions." + settingsManager.isOverwritePermissionsEnabled(), player, false),
                new PermissionAction("AdminPanel.DataClient.Menu.Settings.OverwritePermissions", (p, e) -> {
                    settingsManager.setOverwritePermissionsEnabled(!settingsManager.isOverwritePermissionsEnabled());
                    super.open();
                }, lgm), null);
        registerButton(13, lgm.getItem(path + "FileLogging." + settingsManager.isFileLogging(), player, false),
                new PermissionAction("AdminPanel.DataClient.Menu.Settings.FileLogging", (p, e) -> {
                    settingsManager.setFileLogging(!settingsManager.isFileLogging());
                    super.open();
                }, lgm), null);

        registerButton(15, lgm.getItem(path + "AutoCheckConnectionTiming", player, false),
                new PermissionAction("AdminPanel.DataClient.Menu.Settings.AutoCheckConnectionTiming", (p, e) -> {
                    p.closeInventory();
                    p.sendMessage(lgm.getMessage("DataClient.Menu.AutoCheckConnectionTimingChat", p, true));
                    playerMenuUtility.addData("AutoCheckConnectionTiming", settingsManager.getCheckConnectionTiming());
                }, lgm), null);
        registerButton(16, lgm.getItem(path + "FileLoggingPrefix", player, false),
                new PermissionAction("AdminPanel.DataClient.Menu.Settings.FileLoggingPrefix", (p, e) -> {
                    p.closeInventory();
                    p.sendMessage(lgm.getMessage("DataClient.Menu.FileLoggingPrefixChat", p, true));
                    playerMenuUtility.addData("FileLoggingPrefix", settingsManager.getFileLoggingPrefix());
                }, lgm), null);

        registerButton(getSlot("General.Close", 26), lgm.getItem("General.Close", player, false),
                new PermissionAction("AdminPanel.Button.Close", (p, e) -> new DataClientMainMenu(playerMenuUtility).open(), lgm), null);
    }
}
