package de.happybavarian07.adminpanel.menusystem.menu.servermanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.PaginatedMenu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class WhitelistedPlayersMenu extends PaginatedMenu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    public WhitelistedPlayersMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.ServerManagment.ManageWhitelist");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("ServerManager.WhitelistManager.PlayerList", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "WhitelistedPlayersMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        List<OfflinePlayer> players = new ArrayList<>();
        Collections.addAll(players, getServer().getOfflinePlayers());

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item == null || !item.hasItemMeta()) return;
        if (item.equals(lgm.getItem("General.Left", null, false))) {
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
        } else if (item.equals(lgm.getItem("General.Close", player, false))) {
            new WhitelistManagerMenu(playerMenuUtility).open();
        } else if (item.equals(lgm.getItem("General.Refresh", player, false))) {
            super.open();
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
        Player player = playerMenuUtility.getOwner();
        //The thing you will be looping through to place items
        List<OfflinePlayer> players = new ArrayList<>();
        Collections.addAll(players, getServer().getOfflinePlayers());

        ///////////////////////////////////// Pagination loop template
        if (!players.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= players.size()) break;
                if (players.get(index) != null) {
                    ///////////////////////////

                    OfflinePlayer current = players.get(index);
                    if (!current.isWhitelisted()) continue;
                    ItemStack head = new ItemStack(legacyServer() ? Material.matchMaterial("SKULL_ITEM") : Material.PLAYER_HEAD, 1);
                    SkullMeta meta = (SkullMeta) head.getItemMeta();
                    meta.setDisplayName(current.getName());
                    meta.setOwningPlayer(current);
                    List<String> lore = new ArrayList<>();
                    lore.add(Utils.format(player, "&6Online: &a" + current.isOnline(), ""));
                    lore.add(Utils.format(player, "&6Banned: &a" + current.isBanned(), ""));
                    lore.add(Utils.format(player, "&6Op: &a" + current.isOp(), ""));
                    lore.add(Utils.format(player, "&6UUID: &a" + current.getUniqueId(), ""));
                    lore.add(Utils.format(player, "&6Last-Played: &a" + current.getLastPlayed(), ""));
                    lore.add(Utils.format(player, "&6Played-Before: &a" + current.hasPlayedBefore(), ""));
                    meta.setLore(lore);
                    head.setItemMeta(meta);
                    inventory.addItem(head);

                    ////////////////////////
                }
            }
        }
        ////////////////////////
    }
}
