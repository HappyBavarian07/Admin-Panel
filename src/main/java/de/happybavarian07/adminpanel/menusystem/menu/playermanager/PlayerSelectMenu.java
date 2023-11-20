package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.player.SelectPlayerEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.PaginatedMenu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class PlayerSelectMenu extends PaginatedMenu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    public PlayerSelectMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.PlayerSelector", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PlayerSelectMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item.getType().equals(lgm.getItem("PlayerManager.PlayerHead", null, false).getType())) {
            UUID target = Bukkit.getOfflinePlayer(item.getItemMeta().getDisplayName()).getUniqueId();
            SelectPlayerEvent selectPlayerEvent = new SelectPlayerEvent(player, target);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(selectPlayerEvent);
                if (!selectPlayerEvent.isCancelled()) {
                    if (player.getName().equals(item.getItemMeta().getDisplayName()) &&
                    !plugin.getConfig().getBoolean("Pman.SelfSelect")) {
                        player.sendMessage(lgm.getMessage("Player.PlayerManager.ChooseYourself", player, true));
                        return;
                    }
                    playerMenuUtility.setTargetUUID(target);
                    new PlayerActionSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("General.Close", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem("General.Left", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.pageleft")) {
                player.sendMessage(noPerms);
                return;
            }
            if (page == 0) {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnFirstPage", player, true));
            } else {
                page = page - 1;
                super.open();
            }
        } else if (item.equals(lgm.getItem("General.Right", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.pageright")) {
                player.sendMessage(noPerms);
                return;
            }
            if (!((index + 1) >= players.size())) {
                page = page + 1;
                super.open();
            } else {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player, true));
            }
        } else if (item.equals(lgm.getItem("General.Refresh", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.refresh")) {
                player.sendMessage(noPerms);
                return;
            }
            super.open();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.BannedPlayers", null, false))) {
            new BannedPlayersMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
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
        addMenuBorder();
        inventory.setItem(getSlot("PlayerManager.ActionsMenu.BannedPlayers", 47), lgm.getItem("PlayerManager.ActionsMenu.BannedPlayers", null, false));

        //The thing you will be looping through to place items
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());

        ///////////////////////////////////// Pagination loop template
        if (!players.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= players.size()) break;
                if (players.get(index) != null) {
                    ///////////////////////////

                    ItemStack head = AdminPanelMain.getPlugin().getLanguageManager().getItem("PlayerManager.PlayerHead", players.get(index), false);
                    SkullMeta meta = (SkullMeta) head.getItemMeta();
                    meta.setOwningPlayer(players.get(index));
                    head.setItemMeta(meta);
                    inventory.addItem(head);

                    ////////////////////////
                }
            }
        }
        ////////////////////////
    }
}
