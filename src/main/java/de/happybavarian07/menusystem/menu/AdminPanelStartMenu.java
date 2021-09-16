package de.happybavarian07.menusystem.menu;

import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.menusystem.menu.playermanager.PlayerSelectMenu;
import de.happybavarian07.menusystem.menu.servermanager.ServerManagerMenu;
import de.happybavarian07.menusystem.menu.worldmanager.WorldSelectMenu;
import de.happybavarian07.menusystem.menu.pluginmanager.PluginSelectMenu;
import de.happybavarian07.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AdminPanelStartMenu extends Menu {
    private final Main plugin = Main.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();

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

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item.equals(lgm.getItem(path + "ServerReload", player))) {
            if (!player.hasPermission("AdminPanel.ServerReload")) {
                player.sendMessage(noPerms);
                return;
            }
            try {
                Utils.getInstance().serverReload(1000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "ServerStop", player))) {
            if (!player.hasPermission("AdminPanel.ServerStop")) {
                player.sendMessage(noPerms);
                return;
            }
            try {
                Utils.getInstance().serverStop(1000, 2000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "WorldManager", player))) {
            if (!player.hasPermission("AdminPanel.ServerStop")) {
                player.sendMessage(noPerms);
                return;
            }
            new WorldSelectMenu(Main.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem(path + "PlayerManager", player))) {
            if (!player.hasPermission("AdminPanel.ServerStop")) {
                player.sendMessage(noPerms);
                return;
            }
            new PlayerSelectMenu(Main.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem(path + "PluginManager", player))) {
            if (!player.hasPermission("AdminPanel.PluginManager.open")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginSelectMenu(Main.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem(path + "ServerManager", player))) {
            if (!player.hasPermission("AdminPanel.ServerManagment.Open")) {
                player.sendMessage(noPerms);
                return;
            }
            new ServerManagerMenu(Main.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem(path + "ReloadConfig", player))) {
            if (!player.hasPermission("AdminPanel.ReloadConfig")) {
                player.sendMessage(noPerms);
                return;
            }
            Main.getAPI().reloadConfigurationFiles(player);
            super.open();
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "StartMenu.";

        inventory.setItem(18, lgm.getItem(path + "ReloadConfig", player));
        inventory.setItem(4, lgm.getItem(path + "ServerReload", player));
        inventory.setItem(10, lgm.getItem(path + "WorldManager", player));
        inventory.setItem(12, lgm.getItem(path + "PlayerManager", player));
        inventory.setItem(13, lgm.getItem(path + "PluginManager", player));
        inventory.setItem(14, lgm.getItem(path + "ServerStop", player));
        inventory.setItem(16, lgm.getItem(path + "ServerManager", player));
        inventory.setItem(22, lgm.getItem(path + "HintItem", player));
    }
}
