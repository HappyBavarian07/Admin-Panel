package de.happybavarian07.menusystem.menu.playermanager;

import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.menusystem.PaginatedMenu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.menusystem.menu.playermanager.money.PlayerActionSelectMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class PlayerSelectMenu extends PaginatedMenu {
    private final Main plugin = Main.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();

    public PlayerSelectMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.PlayerSelector", null);
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

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if(item.getType().equals(lgm.getItem("PlayerManager.PlayerHead", null).getType())) {
            if(player.equals(Bukkit.getOfflinePlayer(item.getItemMeta().getDisplayName()))) {
                player.sendMessage(lgm.getMessage("Player.PlayerManager.ChooseYourself", player));
                return;
            }
            new PlayerActionSelectMenu(playerMenuUtility, Bukkit.getOfflinePlayer(item.getItemMeta().getDisplayName()).getUniqueId()).open();
        } else if (item.equals(lgm.getItem("General.Close", null))) {
            if(!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new AdminPanelStartMenu(Main.getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem("General.Left", null))) {
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
        } else if (item.equals(lgm.getItem("General.Refresh", null))) {
            super.open();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.BannedPlayers", null))) {
            new BannedPlayersMenu(playerMenuUtility).open();
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();
        inventory.setItem(47, lgm.getItem("PlayerManager.ActionsMenu.BannedPlayers", null));

        //The thing you will be looping through to place items
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());

        ///////////////////////////////////// Pagination loop template
        if(players != null && !players.isEmpty()) {
            for(int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if(index >= players.size()) break;
                if (players.get(index) != null){
                    ///////////////////////////

                    ItemStack head = Main.getPlugin().getLanguageManager().getItem("PlayerManager.PlayerHead", players.get(index));
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
