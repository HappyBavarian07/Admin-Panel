package de.happybavarian07.menusystem.menu.playermanager;

import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.main.LanguageManager;
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

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta()) return;
        if (item.getType().equals(lgm.getItem(path + "Reason", player).getType()) &&
                item.getItemMeta().getDisplayName().equals(format(player, lgm.getItem(path + "Reason", player).getItemMeta().getDisplayName()))) {
            player.setMetadata("KickPlayerSetNewReason", new FixedMetadataValue(plugin, true));
            player.sendMessage(lgm.getMessage("Player.PlayerManager.KickMenu.Reason.EnterNewReason", player));
            player.closeInventory();
        } else if (item.getType().equals(lgm.getItem(path + "Kick", player).getType()) &&
                item.getItemMeta().getDisplayName().equals(format(player, lgm.getItem(path + "Kick", player).getItemMeta().getDisplayName()))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Kick")) {
                player.sendMessage(noPerms);
                return;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
            if (plugin.getConfig().getStringList("Pman.Actions.ExemptPlayers").contains(target.getName())) {
                player.sendMessage(lgm.getMessage("Player.PlayerManager.KickMenu.NotKickable", player));
                return;
            }
            if (target.isOnline()) {
                target.getPlayer().kickPlayer(format(target.getPlayer(), lgm.getMessage("Player.PlayerManager.KickMenu.TargetKickMessage", target.getPlayer())));
            }
            player.sendMessage(lgm.getMessage("Player.PlayerManager.KickMenu.SuccessfullyKicked", player));
            new PlayerSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem("General.Close", player))) {
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
            player.sendMessage(lgm.getMessage("Player.PlayerManager.KickMenu.Reason.NewReasonSet", player));
            super.open();
            event.setCancelled(true);
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "PlayerManager.ActionsMenu.KickMenu.";
        ItemStack stack;
        ItemMeta meta;
        List<String> updatedLore = new ArrayList<>();

        // Reason
        stack = lgm.getItem(path + "Reason", player);
        meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(format(player, meta.getDisplayName()));
        for (String s : Objects.requireNonNull(meta.getLore())) {
            updatedLore.add(format(player, s));
        }
        meta.setLore(updatedLore);
        stack.setItemMeta(meta);
        inventory.setItem(0, stack);

        // Ban
        stack = lgm.getItem(path + "Kick", player);
        meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(format(player, meta.getDisplayName()));
        updatedLore.clear();
        for (String s : Objects.requireNonNull(meta.getLore())) {
            updatedLore.add(format(player, s));
        }
        meta.setLore(updatedLore);
        stack.setItemMeta(meta);
        inventory.setItem(7, stack);

        // General
        inventory.setItem(8, lgm.getItem("General.Close", player));
    }

    public String format(Player player, String message) {
        return Utils.format(player, message, AdminPanelMain.getPrefix()).replace("%reason%", reason);
    }
}
