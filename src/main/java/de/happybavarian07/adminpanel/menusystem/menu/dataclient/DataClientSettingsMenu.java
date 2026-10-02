package de.happybavarian07.adminpanel.menusystem.menu.dataclient;/*
 * @Author HappyBavarian07
 * @Date 11.10.2023 | 15:58
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.syncing.managers.SettingsManager;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChatEvent;

public class DataClientSettingsMenu extends Menu {
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
    public void handleOpenMenu(InventoryOpenEvent e) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {

    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "DataClientMenu.MainMenu.Settings.";

        registerButton(10, lgm.getItem(path + "AutoCheckConnection." + settingsManager.isCheckConnection(), player, false),
                MenuAction.of((p, e) -> {
                    settingsManager.setCheckConnection(!settingsManager.isCheckConnection());
                    super.open();
                }).requires("AdminPanel.DataClient.Menu.Settings.AutoCheckConnection"), null);
        registerButton(11, lgm.getItem(path + "Debug." + settingsManager.isDebugEnabled(), player, false),
                MenuAction.of((p, e) -> {
                    settingsManager.setDebugEnabled(!settingsManager.isDebugEnabled());
                    super.open();
                }).requires("AdminPanel.DataClient.Menu.Settings.Debug"), null);
        registerButton(12, lgm.getItem(path + "OverwritePermissions." + settingsManager.isOverwritePermissionsEnabled(), player, false),
                MenuAction.of((p, e) -> {
                    settingsManager.setOverwritePermissionsEnabled(!settingsManager.isOverwritePermissionsEnabled());
                    super.open();
                }).requires("AdminPanel.DataClient.Menu.Settings.OverwritePermissions"), null);
        registerButton(13, lgm.getItem(path + "FileLogging." + settingsManager.isFileLogging(), player, false),
                MenuAction.of((p, e) -> {
                    settingsManager.setFileLogging(!settingsManager.isFileLogging());
                    super.open();
                }).requires("AdminPanel.DataClient.Menu.Settings.FileLogging"), null);

        registerButton(15, lgm.getItem(path + "AutoCheckConnectionTiming", player, false),
                MenuAction.prompt(lgm.getMessage("DataClient.Menu.AutoCheckConnectionTimingChat", player, true), (p, text) -> {
                    try {
                        settingsManager.setCheckConnectionTiming(Integer.parseInt(text.trim()));
                        p.sendMessage(lgm.getMessage("DataClient.Menu.AutoCheckConnectingTimingChatSuccess", p, true));
                    } catch (NumberFormatException invalid) {
                        p.sendMessage(lgm.getMessage("Player.Commands.NotANumber", p, true));
                    }
                    open();
                }).requires("AdminPanel.DataClient.Menu.Settings.AutoCheckConnectionTiming"), null);
        registerButton(16, lgm.getItem(path + "FileLoggingPrefix", player, false),
                MenuAction.prompt(lgm.getMessage("DataClient.Menu.FileLoggingPrefixChat", player, true), (p, text) -> {
                    settingsManager.setFileLoggingPrefix(text);
                    p.sendMessage(lgm.getMessage("DataClient.Menu.FileLoggingPrefixChatSuccess", p, true));
                    open();
                }).requires("AdminPanel.DataClient.Menu.Settings.FileLoggingPrefix"), null);

        registerButton(getSlot("General.Close", 26), lgm.getItem("General.Back", player, false),
                MenuAction.of((p, e) -> new DataClientMainMenu(playerMenuUtility).open()).requires("AdminPanel.Button.Close"), null);
    }
}
