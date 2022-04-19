package de.happybavarian07.menusystem.menu.playermanager;

import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.PlaceholderType;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.utils.Utils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayerKickMenu extends Menu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    private final UUID targetUUID;
    private String reason = "";

    public PlayerKickMenu(PlayerMenuUtility playerMenuUtility, UUID targetUUID) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Kick");
        this.targetUUID = targetUUID;
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.KickMenu", null);
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String path = "PlayerManager.ActionsMenu.KickMenu.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%reason%", reason, true);
        if (item == null || !item.hasItemMeta()) return;
        if (item.getType().equals(lgm.getItem(path + "Reason", player, false).getType())) {
            player.setMetadata("KickPlayerSetNewReason", new FixedMetadataValue(plugin, true));
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%reason%", reason, true);
            player.sendMessage(lgm.getMessage("Player.PlayerManager.KickMenu.Reason.EnterNewReason", player, true));
            player.closeInventory();
        } else if (item.getType().equals(lgm.getItem(path + "Kick", player, false).getType())) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Kick")) {
                player.sendMessage(noPerms);
                return;
            }

            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%reason%", reason, true);
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
            if (plugin.getConfig().getStringList("Pman.Actions.ExemptPlayers").contains(target.getName())) {
                player.sendMessage(lgm.getMessage("Player.PlayerManager.KickMenu.NotKickable", player, true));
                return;
            }
            if (target.isOnline()) {
                target.getPlayer().kickPlayer(lgm.getMessage("Player.PlayerManager.KickMenu.TargetKickMessage", target.getPlayer(), false));
            }
            player.sendMessage(lgm.getMessage("Player.PlayerManager.KickMenu.SuccessfullyKicked", player, true));
            new PlayerSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem("General.Close", player, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new PlayerActionSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), targetUUID).open();
        }
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("KickPlayerSetNewReason")) {
            this.reason = format(player, event.getMessage());
            player.removeMetadata("KickPlayerSetNewReason", plugin);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%reason%", reason, true);
            player.sendMessage(lgm.getMessage("Player.PlayerManager.KickMenu.Reason.NewReasonSet", player, true));
            super.open();
            event.setCancelled(true);
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "PlayerManager.ActionsMenu.KickMenu.";
        lgm.addPlaceholder(PlaceholderType.ITEM, "%reason%", reason, false);
        // Reason
        inventory.setItem(getSlot(path + "Reason", 0), lgm.getItem(path + "Reason", player, false));

        // Ban
        inventory.setItem(getSlot(path + "Kick", 7), lgm.getItem(path + "Kick", player, false));

        // General
        inventory.setItem(getSlot("General.Close", 8), lgm.getItem("General.Close", player, false));
    }

    public String format(Player player, String message) {
        return Utils.format(player, message, AdminPanelMain.getPrefix());
    }
}
