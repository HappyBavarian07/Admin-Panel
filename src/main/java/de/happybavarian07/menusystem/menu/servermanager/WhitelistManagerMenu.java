package de.happybavarian07.menusystem.menu.servermanager;

import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.PlaceholderType;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

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
            player.setMetadata("WhitelistManagerAddPlayer", new FixedMetadataValue(plugin, true));
            player.sendMessage(lgm.getMessage("Player.ServerManager.WhitelistManager.PleaseEnterName", player, true));
            player.closeInventory();
        } else if (item.equals(lgm.getItem(path + "RemovePlayer", player, false))) {
            player.setMetadata("WhitelistManagerRemovePlayer", new FixedMetadataValue(plugin, true));
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
        if (player.hasMetadata("WhitelistManagerAddPlayer")) {
            event.setCancelled(true);
            String message = event.getMessage();
            OfflinePlayer playerToAdd = Bukkit.getOfflinePlayer(message);
            if (!playerToAdd.isWhitelisted()) {
                playerToAdd.setWhitelisted(true);
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", playerToAdd.getName(), true);
                player.sendMessage(lgm.getMessage("Player.ServerManager.WhitelistManager.AddedPlayer", player, true));
                player.removeMetadata("WhitelistManagerAddPlayer", plugin);
                super.open();
            }
        }
        if (player.hasMetadata("WhitelistManagerRemovePlayer")) {
            event.setCancelled(true);
            String message = event.getMessage();
            OfflinePlayer playerToRemove = Bukkit.getOfflinePlayer(message);
            if (playerToRemove.isWhitelisted()) {
                playerToRemove.setWhitelisted(false);
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", playerToRemove.getName(), true);
                player.sendMessage(lgm.getMessage("Player.ServerManager.WhitelistManager.RemovedPlayer", player, true));
                player.removeMetadata("WhitelistManagerRemovePlayer", plugin);
                super.open();
            }
        }
    }
}
