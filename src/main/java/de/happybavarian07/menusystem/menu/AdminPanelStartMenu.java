package de.happybavarian07.menusystem.menu;

import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.menusystem.menu.playermanager.PlayerSelectMenu;
import de.happybavarian07.menusystem.menu.pluginmanager.PluginSelectMenu;
import de.happybavarian07.menusystem.menu.servermanager.ServerManagerMenu;
import de.happybavarian07.menusystem.menu.worldmanager.WorldSelectMenu;
import de.happybavarian07.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AdminPanelStartMenu extends Menu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    public AdminPanelStartMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("StartMenu", null);
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();
        String path = "StartMenu.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item != null) {
            if (item.equals(lgm.getItem(path + "ServerRestart", player, false))) {
                if (!player.hasPermission("AdminPanel.ServerRestart")) {
                    player.sendMessage(noPerms);
                    return;
                }
                try {
                    Utils.serverRestart(20, 60);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            } else if (item.equals(lgm.getItem(path + "ServerStop", player, false))) {
                if (!player.hasPermission("AdminPanel.ServerStop")) {
                    player.sendMessage(noPerms);
                    return;
                }
                try {
                    Utils.serverStop(1000, 2000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            } else if (item.equals(lgm.getItem(path + "WorldManager", player, false))) {
                if (!player.hasPermission("AdminPanel.ServerStop")) {
                    player.sendMessage(noPerms);
                    return;
                }
                new WorldSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
            } else if (item.equals(lgm.getItem(path + "PlayerManager", player, false))) {
                if (!player.hasPermission("AdminPanel.ServerStop")) {
                    player.sendMessage(noPerms);
                    return;
                }
                new PlayerSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
            } else if (item.equals(lgm.getItem(path + "PluginManager", player, false))) {
                if (!player.hasPermission("AdminPanel.PluginManager.open")) {
                    player.sendMessage(noPerms);
                    return;
                }
                new PluginSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
            } else if (item.equals(lgm.getItem(path + "ServerManager", player, false))) {
                if (!player.hasPermission("AdminPanel.ServerManagment.Open")) {
                    player.sendMessage(noPerms);
                    return;
                }
                new ServerManagerMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
            } else if (item.equals(lgm.getItem(path + "ReloadConfig", player, false))) {
                if (!player.hasPermission("AdminPanel.ReloadConfig")) {
                    player.sendMessage(noPerms);
                    return;
                }
                AdminPanelMain.getAPI().reloadConfigurationFiles(player);
                super.open();
            } else if (item.equals(lgm.getItem(path + "SwitchLanguageMenuItem", player, false))) {
                if (!player.hasPermission("AdminPanel.SwitchLanguage")) {
                    player.sendMessage(noPerms);
                    return;
                }
                new TempLanguageSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
            }
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "StartMenu.";

        inventory.setItem(getSlot(path + "ReloadConfig", 18), lgm.getItem(path + "ReloadConfig", player, false));
        inventory.setItem(getSlot(path + "ServerRestart", 4), lgm.getItem(path + "ServerRestart", player, false));
        inventory.setItem(getSlot(path + "WorldManager", 10), lgm.getItem(path + "WorldManager", player, false));
        inventory.setItem(getSlot(path + "PlayerManager", 12), lgm.getItem(path + "PlayerManager", player, false));
        inventory.setItem(getSlot(path + "PluginManager", 13), lgm.getItem(path + "PluginManager", player, false));
        inventory.setItem(getSlot(path + "ServerStop", 14), lgm.getItem(path + "ServerStop", player, false));
        inventory.setItem(getSlot(path + "ServerManager", 16), lgm.getItem(path + "ServerManager", player, false));
        inventory.setItem(getSlot(path + "HintItem", 22), lgm.getItem(path + "HintItem", player, false));
        inventory.setItem(getSlot(path + "SwitchLanguageMenuItem", 26), lgm.getItem(path + "SwitchLanguageMenuItem", player, false));
    }
}
