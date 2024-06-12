package de.happybavarian07.adminpanel.menusystem.menu.servermanager;

import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;

public class WhitelistManagerMenu extends Menu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    public WhitelistManagerMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.ServerManagment.ManageWhitelist");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("ServerManager.WhitelistManager.Menu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "WhitelistManagerMenu";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String path = "ServerManager.WhitelistMenu.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item == null || !item.hasItemMeta()) return;
        if (item.equals(lgm.getItem(path + "AddPlayer", player, false))) {
            playerMenuUtility.addData("WhitelistManagerAddPlayer", true);
            player.sendMessage(lgm.getMessage("Player.ServerManager.WhitelistManager.PleaseEnterName", player, true));
            player.closeInventory();
        } else if (item.equals(lgm.getItem(path + "RemovePlayer", player, false))) {
            playerMenuUtility.addData("WhitelistManagerRemovePlayer", true);
            player.sendMessage(lgm.getMessage("Player.ServerManager.WhitelistManager.PleaseEnterName", player, true));
            player.closeInventory();
        } else if (item.equals(lgm.getItem(path + "ListPlayers", player, false))) {
            new WhitelistedPlayersMenu(playerMenuUtility).open();
        } else if (item.equals(lgm.getItem(path + "TurnOn", player, false))) {
            Bukkit.setWhitelist(true);
        } else if (item.equals(lgm.getItem(path + "TurnOff", player, false))) {
            Bukkit.setWhitelist(false);
        } else if (item.equals(lgm.getItem(path + "Reload", player, false))) {
            Bukkit.reloadWhitelist();
        } else if (item.equals(lgm.getItem("General.Close", player, false))) {
            new ServerManagerMenu(playerMenuUtility).open();
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
        String path = "ServerManager.WhitelistMenu.";

        inventory.setItem(getSlot(path + "AddPlayer", 11), lgm.getItem(path + "AddPlayer", player, false));
        inventory.setItem(getSlot(path + "AddPlayer", 13), lgm.getItem(path + "RemovePlayer", player, false));
        inventory.setItem(getSlot(path + "AddPlayer", 15), lgm.getItem(path + "ListPlayers", player, false));
        inventory.setItem(getSlot(path + "TurnOn", 3), lgm.getItem(path + "TurnOn", player, false));
        inventory.setItem(getSlot(path + "TurnOff", 5), lgm.getItem(path + "TurnOff", player, false));
        inventory.setItem(getSlot(path + "Reload", 4), lgm.getItem(path + "Reload", player, false));

        inventory.setItem(getSlot("General.Close", 26), lgm.getItem("General.Close", player, false));
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playerMenuUtility.getOwner() != player) return;

        if (playerMenuUtility.hasData("WhitelistManagerAddPlayer")) {
            event.setCancelled(true);
            String message = event.getMessage();
            OfflinePlayer playerToAdd = Bukkit.getOfflinePlayer(message);
            if (!playerToAdd.isWhitelisted()) {
                playerToAdd.setWhitelisted(true);
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", playerToAdd.getName(), true);
                player.sendMessage(lgm.getMessage("Player.ServerManager.WhitelistManager.AddedPlayer", player, true));
                playerMenuUtility.removeData("WhitelistManagerAddPlayer");
                super.open();
            }
        }
        if (playerMenuUtility.hasData("WhitelistManagerRemovePlayer")) {
            event.setCancelled(true);
            String message = event.getMessage();
            OfflinePlayer playerToRemove = Bukkit.getOfflinePlayer(message);
            if (playerToRemove.isWhitelisted()) {
                playerToRemove.setWhitelisted(false);
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", playerToRemove.getName(), true);
                player.sendMessage(lgm.getMessage("Player.ServerManager.WhitelistManager.RemovedPlayer", player, true));
                playerMenuUtility.removeData("WhitelistManagerRemovePlayer");
                super.open();
            }
        }
    }
}
