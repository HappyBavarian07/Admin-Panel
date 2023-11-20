package de.happybavarian07.adminpanel.menusystem.menu.dataclient;/*
 * @Author HappyBavarian07
 * @Date 28.09.2023 | 15:28
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.syncing.DataClient;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class DataClientMainMenu extends Menu {
    private final DataClient dataClient;

    public DataClientMainMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        this.dataClient = plugin.getDataClient();
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
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();
        String path = "DataClientMenu.MainMenu.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item != null) {
            if (item.equals(lgm.getItem("General.Close", player, false))) {
                if (!player.hasPermission("AdminPanel.Button.Close")) {
                    player.sendMessage(noPerms);
                    return;
                }
                player.closeInventory();
            } else if (item.equals(lgm.getItem(path + "Controls.Disconnect", player, false))) {
                if (!player.hasPermission("AdminPanel.DataClient.Menu.Button.Disconnect")) {
                    player.sendMessage(noPerms);
                    return;
                }
                dataClient.disconnect(true);
                super.open();
            } else if (item.equals(lgm.getItem(path + "Controls.Connect", player, false))) {
                if (!player.hasPermission("AdminPanel.DataClient.Menu.Button.Connect")) {
                    player.sendMessage(noPerms);
                    return;
                }
                try {
                    dataClient.connect();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                super.open();
            } else if (item.equals(lgm.getItem(path + "Controls.Reconnect", player, false))) {
                if (!player.hasPermission("AdminPanel.DataClient.Menu.Button.Reconnect")) {
                    player.sendMessage(noPerms);
                    return;
                }
                try {
                    dataClient.reconnect(true);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                super.open();
            } else if (item.equals(lgm.getItem(path + "Controls.PacketHandling.true", player, false))) {
                if (!player.hasPermission("AdminPanel.DataClient.Menu.Button.PacketHandling")) {
                    player.sendMessage(noPerms);
                    return;
                }
                dataClient.getPacketHandler().stopPacketHandlingThread();
                super.open();
            } else if (item.equals(lgm.getItem(path + "Controls.PacketHandling.false", player, false))) {
                if (!player.hasPermission("AdminPanel.DataClient.Menu.Button.PacketHandling")) {
                    player.sendMessage(noPerms);
                    return;
                }
                dataClient.getPacketHandler().startPacketHandlingThread();
                super.open();
            } else if (item.equals(lgm.getItem(path + "DataClientName", player, false))) {
                if (!player.hasPermission("AdminPanel.DataClient.Menu.Button.DataClientName")) {
                    player.sendMessage(noPerms);
                    return;
                }
                player.sendMessage(Utils.chat("&7DataClientName: &e" + dataClient.getConnectionHandler().getClientName()));
            } else if (item.equals(lgm.getItem(path + "DataClientSettings", player, false))) {
                if (!player.hasPermission("AdminPanel.DataClient.Menu.Button.DataClientSettings")) {
                    player.sendMessage(noPerms);
                    return;
                }
                new DataClientSettingsMenu(playerMenuUtility).open();
            }
        }
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
            inventory.setItem(10, lgm.getItem(path + "Controls.Disconnect", player, false));
        } else {
            inventory.setItem(10, lgm.getItem(path + "Controls.Connect", player, false));
        }
        inventory.setItem(11, lgm.getItem(path + "Controls.Reconnect", player, false));
        inventory.setItem(12, lgm.getItem(path + "Controls.PacketHandling." + dataClient.getPacketHandler().isPacketHandlingEnabled(), player, false));

        // Controls (Sending Packets/Custom Payloads + Custom Actions on Receiving maybe (with defaults))
        // UNDER DEVELOPMENT

        // Fourth Row (Stats + Name + Settings Button)
        inventory.setItem(36, lgm.getItem(path + "DataClientName", player, false));
        inventory.setItem(36, lgm.getItem(path + "DataClientConnectedClients", player, false));
        inventory.setItem(36, lgm.getItem(path + "DataClientStats", player, false));
        inventory.setItem(36, lgm.getItem(path + "DataClientSettings", player, false));
    }
}
