package de.happybavarian07.menusystem.menu.playermanager;

import de.happybavarian07.events.NotAPanelEventException;
import de.happybavarian07.events.player.PlayerBanEvent;
import de.happybavarian07.events.player.PlayerKickEvent;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.menusystem.menu.playermanager.PlayerActionsMenu;
import de.happybavarian07.menusystem.menu.playermanager.PlayerSelectMenu;
import de.happybavarian07.menusystem.menu.playermanager.money.MoneyMenu;
import de.happybavarian07.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerActionSelectMenu extends Menu {
    private final Main plugin = Main.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();
    private final UUID targetUUID;

    public PlayerActionSelectMenu(PlayerMenuUtility playerMenuUtility, UUID targetUUID) {
        super(playerMenuUtility);
        this.targetUUID = targetUUID;
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.SelectPlayerAction", Bukkit.getPlayer(targetUUID));
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Player target = Bukkit.getPlayer(targetUUID);
        ItemStack item = e.getCurrentItem();

        if(item.equals(lgm.getItem("PlayerManager.ActionsMenu.ActionsItem", target))) {
            new PlayerActionsMenu(Main.getAPI().getPlayerMenuUtility(player), targetUUID).open();
        } else if(item.equals(lgm.getItem("PlayerManager.ActionsMenu.MoneyItem", target))) {
            new MoneyMenu(Main.getAPI().getPlayerMenuUtility(player), targetUUID).open();
        } else if(item.equals(lgm.getItem("PlayerManager.ActionsMenu.BanItem", target))) {
            String reason = lgm.getMessage("Player.PlayerManager.BanReason", target);
            String source = lgm.getMessage("Player.PlayerManager.BanSource", player);
            String targetName = target.getName();
            PlayerBanEvent banEvent = new PlayerBanEvent(player, targetName, reason, source);
            try {
                Main.getAPI().callAdminPanelEvent(banEvent);
                if (!banEvent.isCancelled()) {
                    Utils.getInstance().ban(player, targetName, reason, source);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if(item.equals(lgm.getItem("PlayerManager.ActionsMenu.KickItem", target))) {
            String reason = lgm.getMessage("Player.PlayerManager.KickReason", target);
            String source = lgm.getMessage("Player.PlayerManager.KickSource", player);
            String targetName = target.getName();
            PlayerKickEvent kickEvent = new PlayerKickEvent(player, targetName, reason, source);
            try {
                Main.getAPI().callAdminPanelEvent(kickEvent);
                if (!kickEvent.isCancelled()) {
                    Utils.getInstance().kick(player, targetName, reason, source);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("General.Close", null))) {
            if(!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(lgm.getMessage("Player.General.NoPermissions", player));
                return;
            }
            new PlayerSelectMenu(Main.getAPI().getPlayerMenuUtility(player)).open();
        }
    }

    @Override
    public void setMenuItems() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, super.FILLER);
        }
        Player target = Bukkit.getPlayer(targetUUID);
        inventory.setItem(10, lgm.getItem("PlayerManager.ActionsMenu.ActionsItem", target));
        inventory.setItem(12, lgm.getItem("PlayerManager.ActionsMenu.MoneyItem", target));
        inventory.setItem(14, lgm.getItem("PlayerManager.ActionsMenu.BanItem", target));
        inventory.setItem(16, lgm.getItem("PlayerManager.ActionsMenu.KickItem", target));
        inventory.setItem(26, lgm.getItem("General.Close", target));
    }
}
