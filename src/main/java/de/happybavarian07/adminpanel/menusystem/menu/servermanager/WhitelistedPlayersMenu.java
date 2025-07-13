package de.happybavarian07.adminpanel.menusystem.menu.servermanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class WhitelistedPlayersMenu extends PaginatedMenu<OfflinePlayer> {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    public WhitelistedPlayersMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission("AdminPanel.ServerManagment.ManageWhitelist");
        List<OfflinePlayer> players = new ArrayList<>();
        Collections.addAll(players, getServer().getOfflinePlayers());
        setPaginatedData(players, this::getPageItem);
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
    public void preSetMenuItems() {
    }

    @Override
    public void postSetMenuItems() {
    }

    @Override
    protected void handlePageItemClick(int indexOnPage, ItemStack item, InventoryClickEvent e) {
    }

    @Override
    protected void handleCustomItemClick(int slot, ItemStack item, InventoryClickEvent e) {
    }

    public ItemStack getPageItem(OfflinePlayer offlinePlayer) {
        ItemStack item = Utils.createSkull(offlinePlayer.getName() == null ? "MHF_Notfound" : offlinePlayer.getName(), offlinePlayer.getName(), false);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        Player player = Bukkit.getServer().getPlayer(offlinePlayer.getUniqueId());
        lore.add(AdminPanelUtils.format(player, "&6Online: &f" + (player != null && player.isOnline() ? "Yes" : "No"), ""));
        lore.add(AdminPanelUtils.format(player, "&6Banned: &f" + (offlinePlayer.isBanned() ? "Yes" : "No"), ""));
        lore.add(AdminPanelUtils.format(player, "&6Operator: &f" + (offlinePlayer.isOp() ? "Yes" : "No"), ""));
        lore.add(AdminPanelUtils.format(player, "&6UUID: &f" + offlinePlayer.getUniqueId(), ""));
        lore.add(AdminPanelUtils.format(player, "&6Last Seen: &f" + (offlinePlayer.getLastPlayed() > 0 ? getTimeAgo(offlinePlayer.getLastPlayed()) : "Never"), ""));
        lore.add(AdminPanelUtils.format(player, "&6Has played before: " + (offlinePlayer.hasPlayedBefore() ? "Yes" : "No"), ""));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String getTimeAgo(long lastPlayed) {
        long timeAgo = System.currentTimeMillis() - lastPlayed;
        long seconds = timeAgo / 1000;
        if (seconds < 60) return seconds + " seconds ago";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " minutes ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + " hours ago";
        long days = hours / 24;
        return days + " days ago";
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
