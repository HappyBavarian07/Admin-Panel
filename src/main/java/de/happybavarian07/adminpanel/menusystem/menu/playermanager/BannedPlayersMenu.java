package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class BannedPlayersMenu extends PaginatedMenu<OfflinePlayer> {
    List<OfflinePlayer> updatedPlayers = new ArrayList<>();

    public BannedPlayersMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission("AdminPanel.PlayerManager.BannedPlayers");
        for (OfflinePlayer current : getServer().getOfflinePlayers()) {
            if (current.isBanned() && !current.isOnline()) {
                updatedPlayers.add(current);
            }
        }
        setPaginatedData(updatedPlayers, this::getPageItem);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.BannedPlayers", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "BannedPlayersMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void preSetMenuItems() {

    }

    @Override
    public void postSetMenuItems() {

    }

    @Override
    protected void handlePageItemClick(int indexOnPage, ItemStack item, InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (indexOnPage < 0 || indexOnPage >= updatedPlayers.size()) return;
        OfflinePlayer target = updatedPlayers.get(indexOnPage);
        AdminPanelUtils.unban(player, target);
    }

    @Override
    protected void handleCustomItemClick(int slot, ItemStack item, InventoryClickEvent e) {
    }

    public ItemStack getPageItem(OfflinePlayer offlinePlayer) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(offlinePlayer);
        meta.setDisplayName(offlinePlayer.getName());
        List<String> lore = new ArrayList<>();
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
