package de.happybavarian07.menusystem.menu.servermanager;

import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.menusystem.PaginatedMenu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class WhitelistedPlayersMenu extends PaginatedMenu {
    private final Main plugin = Main.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();

    public WhitelistedPlayersMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.ServerManagment.ManageWhitelist");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("ServerManager.WhitelistManager.Menu", null);
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

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta()) return;
        if (item.equals(lgm.getItem("General.Left", null))) {
            if (!player.hasPermission("AdminPanel.Button.pageleft")) {
                player.sendMessage(noPerms);
                return;
            }
            if (page == 0) {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnFirstPage", player));
            } else {
                page = page - 1;
                super.open();
            }
        } else if (item.equals(lgm.getItem("General.Right", null))) {
            if (!player.hasPermission("AdminPanel.Button.pageright")) {
                player.sendMessage(noPerms);
                return;
            }
            if (!((index + 1) >= players.size())) {
                page = page + 1;
                super.open();
            } else {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player));
            }
        } else if (item.equals(lgm.getItem("General.Close", player))) {
            new WhitelistManagerMenu(playerMenuUtility).open();
        } else if (item.equals(lgm.getItem("General.Refresh", player))) {
            super.open();
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();
        //The thing you will be looping through to place items
        List<OfflinePlayer> players = new ArrayList<>();
        Collections.addAll(players, getServer().getOfflinePlayers());

        ///////////////////////////////////// Pagination loop template
        if(players != null && !players.isEmpty()) {
            for(int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if(index >= players.size()) break;
                if (players.get(index) != null){
                    ///////////////////////////

                    OfflinePlayer current = players.get(index);
                    if(!current.isWhitelisted()) continue;
                    ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
                    SkullMeta meta = (SkullMeta) head.getItemMeta();
                    meta.setDisplayName(current.getName());
                    meta.setOwningPlayer(current);
                    List<String> lore = new ArrayList<>();
                    lore.add("&6Online: &a" + current.isOnline());
                    lore.add("&6Banned: &a" + current.isBanned());
                    lore.add("&6Op: &a" + current.isOp());
                    lore.add("&6UUID: &a" + current.getUniqueId());
                    lore.add("&6Last-Played: &a" + current.getLastPlayed());
                    lore.add("&6Played-Before: &a" + current.hasPlayedBefore());
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
