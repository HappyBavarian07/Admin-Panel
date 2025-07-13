package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.player.SelectPlayerEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class PlayerSelectMenu extends PaginatedMenu<Player> {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    public PlayerSelectMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission("AdminPanel.PlayerManager.open");
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
        setPaginatedData(players, this::getPageItem);
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
    public void preSetMenuItems() {
    }

    @Override
    public void postSetMenuItems() {
        Player player = playerMenuUtility.getOwner();
        inventory.setItem(45, lgm.getItem("PlayerManager.ActionsMenu.BannedPlayers", player, false));
    }

    @Override
    protected void handlePageItemClick(int slot, ItemStack item, InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
        int indexOnPage = slot - 10;
        if (indexOnPage < 0 || indexOnPage >= players.size()) return;
        Player targetPlayer = players.get(indexOnPage);
        SelectPlayerEvent selectPlayerEvent = new SelectPlayerEvent(player, targetPlayer.getUniqueId());
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(selectPlayerEvent);
            if (!selectPlayerEvent.isCancelled()) {
                if (player.getName().equals(targetPlayer.getName()) &&
                        !plugin.getConfig().getBoolean("Pman.SelfSelect")) {
                    player.sendMessage(lgm.getMessage("Player.PlayerManager.ChooseYourself", player, true));
                    return;
                }
                playerMenuUtility.setTargetUUID(targetPlayer.getUniqueId());
                new PlayerActionSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
            }
        } catch (NotAPanelEventException notAPanelEventException) {
            notAPanelEventException.printStackTrace();
        }
    }

    @Override
    protected void handleCustomItemClick(int slot, ItemStack item, InventoryClickEvent e) {
        if (item.isSimilar(lgm.getItem("PlayerManager.ActionsMenu.BannedPlayers", playerMenuUtility.getOwner(), false))) {
            new BannedPlayersMenu(playerMenuUtility, this).open();
        }
    }

    public ItemStack getPageItem(Player targetPlayer) {
        ItemStack item = lgm.getItem("PlayerManager.PlayerHead", targetPlayer, false);
        if (item.getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(targetPlayer);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
