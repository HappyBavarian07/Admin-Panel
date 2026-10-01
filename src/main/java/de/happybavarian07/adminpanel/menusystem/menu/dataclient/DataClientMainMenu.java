package de.happybavarian07.adminpanel.menusystem.menu.dataclient;/*
 * @Author HappyBavarian07
 * @Date 28.09.2023 | 15:28
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.syncing.DataClient;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.io.IOException;

public class DataClientMainMenu extends Menu {
    private final DataClient dataClient;

    public DataClientMainMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        this.dataClient = AdminPanelMain.getPlugin().getDataClient();
        setOpeningPermission("AdminPanel.DataClient.Menu.Open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("DataClientMainMenu", playerMenuUtility.getOwner());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "DataClientMainMenu";
    }

    @Override
    public int getSlots() {
        return 45;
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
        String path = "DataClientMenu.MainMenu.";
        // Controls (Second Row)
        if (dataClient.isEnabled()) {
            registerButton(10, lgm.getItem(path + "Controls.Disconnect", player, false),
                    MenuAction.of((p, e) -> {
                        dataClient.disconnect(true);
                        super.open();
                    }).requires("AdminPanel.DataClient.Menu.Button.Disconnect"), null);
        } else {
            registerButton(10, lgm.getItem(path + "Controls.Connect", player, false),
                    MenuAction.of((p, e) -> {
                        try {
                            dataClient.connect();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        super.open();
                    }).requires("AdminPanel.DataClient.Menu.Button.Connect"), null);
        }
        registerButton(11, lgm.getItem(path + "Controls.Reconnect", player, false),
                MenuAction.of((p, e) -> {
                    try {
                        dataClient.reconnect(true);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    super.open();
                }).requires("AdminPanel.DataClient.Menu.Button.Reconnect"), null);
        registerButton(12, lgm.getItem(path + "Controls.PacketHandling." + dataClient.getPacketHandler().isPacketHandlingEnabled(), player, false),
                MenuAction.of((p, e) -> {
                    if (dataClient.getPacketHandler().isPacketHandlingEnabled()) {
                        dataClient.getPacketHandler().stopPacketHandlingThread();
                    } else {
                        dataClient.getPacketHandler().startPacketHandlingThread();
                    }
                    super.open();
                }).requires("AdminPanel.DataClient.Menu.Button.PacketHandling"), null);

        registerButton(36, lgm.getItem(path + "DataClientName", player, false),
                MenuAction.of((p, e) -> {
                    p.sendMessage(AdminPanelUtils.chat("&7DataClientName: &e" + dataClient.getConnectionHandler().getClientName()));
                }).requires("AdminPanel.DataClient.Menu.Button.DataClientName"), null);
        registerButton(39, lgm.getItem(path + "DataClientSettings", player, false),
                MenuAction.of((p, e) -> {
                    new DataClientSettingsMenu(playerMenuUtility).open();
                }).requires("AdminPanel.DataClient.Menu.Button.DataClientSettings"), null);
        registerButton(getSlot("General.Close", 44), lgm.getItem("General.Close", player, false),
                MenuAction.of((p, e) -> p.closeInventory()).requires("AdminPanel.Button.Close"), null);
    }
}
