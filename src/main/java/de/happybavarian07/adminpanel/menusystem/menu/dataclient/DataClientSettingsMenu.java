package de.happybavarian07.adminpanel.menusystem.menu.dataclient;/*
 * @Author HappyBavarian07
 * @Date 11.10.2023 | 15:58
 */

import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.syncing.managers.SettingsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;

public class DataClientSettingsMenu extends Menu implements Listener {
    private final SettingsManager settingsManager;

    public DataClientSettingsMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        this.settingsManager = plugin.getDataClient().getSettingsManager();
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
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();
        String path = "DataClientMenu.MainMenu.Settings.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item != null) {
            if (item.equals(lgm.getItem(path + "AutoCheckConnection." + settingsManager.isCheckConnection(), player, false))) {
                if (!player.hasPermission("AdminPanel.DataClient.Menu.Settings.AutoCheckConnection")) {
                    player.sendMessage(noPerms);
                    return;
                }
                settingsManager.setCheckConnection(!settingsManager.isCheckConnection());
                super.open();
            } else if (item.equals(lgm.getItem(path + "Debug." + settingsManager.isDebugEnabled(), player, false))) {
                if (!player.hasPermission("AdminPanel.DataClient.Menu.Settings.Debug")) {
                    player.sendMessage(noPerms);
                    return;
                }
                settingsManager.setDebugEnabled(!settingsManager.isDebugEnabled());
                super.open();
            } else if (item.equals(lgm.getItem(path + "OverwritePermissions." + settingsManager.isOverwritePermissionsEnabled(), player, false))) {
                if (!player.hasPermission("AdminPanel.DataClient.Menu.Settings.OverwritePermissions")) {
                    player.sendMessage(noPerms);
                    return;
                }
                settingsManager.setOverwritePermissionsEnabled(!settingsManager.isOverwritePermissionsEnabled());
                super.open();
            } else if (item.equals(lgm.getItem(path + "FileLogging." + settingsManager.isFileLogging(), player, false))) {
                if (!player.hasPermission("AdminPanel.DataClient.Menu.Settings.FileLogging")) {
                    player.sendMessage(noPerms);
                    return;
                }
                settingsManager.setFileLogging(!settingsManager.isFileLogging());
                super.open();
            } else if (item.equals(lgm.getItem(path + ".AutoCheckConnectionTiming", player, false))) {
                if (!player.hasPermission("AdminPanel.DataClient.Menu.Settings.AutoCheckConnectionTiming")) {
                    player.sendMessage(noPerms);
                    return;
                }
                player.closeInventory();
                player.sendMessage(lgm.getMessage("DataClient.Menu.AutoCheckConnectionTimingChat", player, true));
                playerMenuUtility.addData("AutoCheckConnectionTiming", settingsManager.getCheckConnectionTiming());
            } else if (item.equals(lgm.getItem(path + ".FileLoggingPrefix", player, false))) {
                if (!player.hasPermission("AdminPanel.DataClient.Menu.Settings.FileLoggingPrefix")) {
                    player.sendMessage(noPerms);
                    return;
                }
                player.closeInventory();
                player.sendMessage(lgm.getMessage("DataClient.Menu.FileLoggingPrefixChat", player, true));
                playerMenuUtility.addData("FileLoggingPrefix", settingsManager.getFileLoggingPrefix());
            } else if (item.equals(lgm.getItem("General.Close", player, false))) {
                if (!player.hasPermission("AdminPanel.Button.Close")) {
                    player.sendMessage(noPerms);
                    return;
                }
                new DataClientMainMenu(playerMenuUtility).open();
            }
        }
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

        inventory.setItem(10, lgm.getItem(path + "AutoCheckConnection." + settingsManager.isCheckConnection(), player, false));
        inventory.setItem(11, lgm.getItem(path + "Debug." + settingsManager.isDebugEnabled(), player, false));
        inventory.setItem(12, lgm.getItem(path + "OverwritePermissions." + settingsManager.isOverwritePermissionsEnabled(), player, false));
        inventory.setItem(13, lgm.getItem(path + "FileLogging." + settingsManager.isFileLogging(), player, false));

        inventory.setItem(15, lgm.getItem(path + "AutoCheckConnectionTiming", player, false));
        inventory.setItem(16, lgm.getItem(path + "FileLoggingPrefix", player, false));

        inventory.setItem(26, lgm.getItem("General.Close", player, false));
    }
}
