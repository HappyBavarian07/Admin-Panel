package de.happybavarian07.menusystem.menu.playermanager;

import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.menusystem.PaginatedMenu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class BannedPlayersMenu extends PaginatedMenu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();

    public BannedPlayersMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.BannedPlayers");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.BannedPlayers", null);
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        List<OfflinePlayer> updatedPlayers = new ArrayList<>();

        for (OfflinePlayer current : getServer().getOfflinePlayers()) {
            if (current.isBanned() || plugin.getBanConfig().getBoolean(current.getUniqueId().toString()) && !current.isOnline()) {
                updatedPlayers.add(current);
            }
        }

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta()) return;
        if (item.getType().equals(lgm.getItem("PlayerManager.PlayerHead", null).getType())) {
            if (!player.hasPermission("AdminPanel.PlayerManager.BannedPlayers")) {
                player.sendMessage(noPerms);
                return;
            }
            Utils.unban(player, Bukkit.getOfflinePlayer(item.getItemMeta().getDisplayName()));
            inventory.setItem(e.getSlot(), null);
        } else if (item.equals(lgm.getItem("General.Close", null))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new PlayerSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.getType().equals(Material.DARK_OAK_BUTTON)) {
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
                if (!((index + 1) >= updatedPlayers.size())) {
                    page = page + 1;
                    super.open();
                } else {
                    player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player));
                }
            }
        } else if (item.equals(lgm.getItem("General.Refresh", null))) {
            if (!player.hasPermission("AdminPanel.Button.refresh")) {
                player.sendMessage(noPerms);
                return;
            }
            super.open();
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        //The thing you will be looping through to place items
        List<OfflinePlayer> updatedPlayers = new ArrayList<>();

        for (OfflinePlayer current : getServer().getOfflinePlayers()) {
            if (current.isBanned() || plugin.getBanConfig().getBoolean(current.getUniqueId().toString()) && !current.isOnline()) {
                updatedPlayers.add(current);
            }
        }

        ///////////////////////////////////// Pagination loop template
        if (updatedPlayers != null && !updatedPlayers.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= updatedPlayers.size()) break;
                if (updatedPlayers.get(index) != null) {
                    ///////////////////////////

                    ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
                    SkullMeta meta = (SkullMeta) head.getItemMeta();
                    meta.setOwningPlayer(updatedPlayers.get(index));
                    meta.setDisplayName(updatedPlayers.get(index).getName());
                    head.setItemMeta(meta);
                    inventory.addItem(head);

                    ////////////////////////
                }
            }
        }
        ////////////////////////
    }
}
