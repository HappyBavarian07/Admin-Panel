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

public class PlayerBanMenu extends Menu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();

    private final UUID targetUUID;
    private String reason = "";
    private int seconds = 0;
    private int minutes = 0;
    private int hours = 0;
    private int days = 0;
    private int months = 0;
    private int years = 0;

    public PlayerBanMenu(PlayerMenuUtility playerMenuUtility, UUID targetUUID) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Ban");
        this.targetUUID = targetUUID;
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.BanMenu", null);
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String path = "PlayerManager.ActionsMenu.BanMenu.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta()) return;
        if (item.getType().equals(lgm.getItem(path + "Years", player).getType()) &&
                item.getItemMeta().getDisplayName().equals(format(player, lgm.getItem(path + "Years", player).getItemMeta().getDisplayName()))) {
            if (e.getAction().equals(InventoryAction.PICKUP_ALL)) {
                if (years == 1000) return;
                years += 1;
            } else if (e.getAction().equals(InventoryAction.PICKUP_HALF)) {
                if (years == 0) return;
                years -= 1;
            }
            super.open();
        } else if (item.getType().equals(lgm.getItem(path + "Months", player).getType()) &&
                item.getItemMeta().getDisplayName().equals(format(player, lgm.getItem(path + "Months", player).getItemMeta().getDisplayName()))) {
            if (e.getAction().equals(InventoryAction.PICKUP_ALL)) {
                if (months == 1000) return;
                months += 1;
            } else if (e.getAction().equals(InventoryAction.PICKUP_HALF)) {
                if (months == 0) return;
                months -= 1;
            }
            super.open();
        } else if (item.getType().equals(lgm.getItem(path + "Days", player).getType()) &&
                item.getItemMeta().getDisplayName().equals(format(player, lgm.getItem(path + "Days", player).getItemMeta().getDisplayName()))) {
            if (e.getAction().equals(InventoryAction.PICKUP_ALL)) {
                if (days == 1000) return;
                days += 1;
            } else if (e.getAction().equals(InventoryAction.PICKUP_HALF)) {
                if (days == 0) return;
                days -= 1;
            }
            super.open();
        } else if (item.getType().equals(lgm.getItem(path + "Hours", player).getType()) &&
                item.getItemMeta().getDisplayName().equals(format(player, lgm.getItem(path + "Hours", player).getItemMeta().getDisplayName()))) {
            if (e.getAction().equals(InventoryAction.PICKUP_ALL)) {
                if (hours == 1000) return;
                hours += 1;
            } else if (e.getAction().equals(InventoryAction.PICKUP_HALF)) {
                if (hours == 0) return;
                hours -= 1;
            }
            super.open();
        } else if (item.getType().equals(lgm.getItem(path + "Minutes", player).getType()) &&
                item.getItemMeta().getDisplayName().equals(format(player, lgm.getItem(path + "Minutes", player).getItemMeta().getDisplayName()))) {
            if (e.getAction().equals(InventoryAction.PICKUP_ALL)) {
                if (minutes == 1000) return;
                minutes += 5;
            } else if (e.getAction().equals(InventoryAction.PICKUP_HALF)) {
                if (minutes == 0) return;
                minutes -= 5;
            }
            super.open();
        } else if (item.getType().equals(lgm.getItem(path + "Seconds", player).getType()) &&
                item.getItemMeta().getDisplayName().equals(format(player, lgm.getItem(path + "Seconds", player).getItemMeta().getDisplayName()))) {
            if (e.getAction().equals(InventoryAction.PICKUP_ALL)) {
                if (seconds == 1000) return;
                seconds += 10;
            } else if (e.getAction().equals(InventoryAction.PICKUP_HALF)) {
                if (seconds == 0) return;
                seconds -= 10;
            }
            super.open();
        } else if (item.getType().equals(lgm.getItem(path + "Reason", player).getType()) &&
                item.getItemMeta().getDisplayName().equals(format(player, lgm.getItem(path + "Reason", player).getItemMeta().getDisplayName()))) {
            player.setMetadata("BanPlayerSetNewReason", new FixedMetadataValue(plugin, true));
            player.sendMessage(lgm.getMessage("Player.PlayerManager.BanMenu.Reason.EnterNewReason", player));
            player.closeInventory();
        } else if (item.getType().equals(lgm.getItem(path + "Ban", player).getType()) &&
                item.getItemMeta().getDisplayName().equals(format(player, lgm.getItem(path + "Ban", player).getItemMeta().getDisplayName()))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Ban")) {
                player.sendMessage(noPerms);
                return;
            }


            Date banEnd = new Date();
            long secondsMillis = TimeUnit.SECONDS.toMillis(seconds);
            long minutesMillis = TimeUnit.MINUTES.toMillis(minutes);
            long hoursMillis = TimeUnit.HOURS.toMillis(hours);
            long daysMillis = TimeUnit.DAYS.toMillis(days);
            long monthsMillis = months * 2628000000L;
            long yearsMillis = years * 31556952000L;
            banEnd.setTime(System.currentTimeMillis() + secondsMillis + minutesMillis + hoursMillis + daysMillis + monthsMillis + yearsMillis);
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
            if (plugin.getConfig().getStringList("Pman.Actions.ExemptPlayers").contains(target.getName())) {
                player.sendMessage(lgm.getMessage("Player.PlayerManager.BanMenu.NotBannable", player));
                return;
            }
            if (target.isBanned()) {
                player.sendMessage(lgm.getMessage("Player.PlayerManager.BanMenu.AlreadyBanned", player));
                return;
            }
            if (target.isOnline()) {
                target.getPlayer().kickPlayer(format(target.getPlayer(), lgm.getMessage("Player.PlayerManager.BanMenu.TargetKickMessage", target.getPlayer())));
            }
            Bukkit.getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(Bukkit.getOfflinePlayer(targetUUID).getName()), reason, banEnd, player.getName());
            years = 0;
            months = 0;
            days = 0;
            hours = 0;
            minutes = 0;
            seconds = 0;
            player.sendMessage(lgm.getMessage("Player.PlayerManager.BanMenu.SuccessfullyBanned", player));
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
        if (player.hasMetadata("BanPlayerSetNewReason")) {
            this.reason = format(player, event.getMessage());
            player.removeMetadata("BanPlayerSetNewReason", plugin);
            player.sendMessage(lgm.getMessage("Player.PlayerManager.BanMenu.Reason.NewReasonSet", player));
            super.open();
            event.setCancelled(true);
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "PlayerManager.ActionsMenu.BanMenu.";
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
        inventory.setItem(13, stack);

        // Years
        stack = lgm.getItem(path + "Years", player);
        meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(format(player, meta.getDisplayName()));
        updatedLore.clear();
        for (String s : Objects.requireNonNull(meta.getLore())) {
            updatedLore.add(format(player, s));
        }
        meta.setLore(updatedLore);
        stack.setItemMeta(meta);
        inventory.setItem(2, stack);

        // Months
        stack = lgm.getItem(path + "Months", player);
        meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(format(player, meta.getDisplayName()));
        updatedLore.clear();
        for (String s : Objects.requireNonNull(meta.getLore())) {
            updatedLore.add(format(player, s));
        }
        meta.setLore(updatedLore);
        stack.setItemMeta(meta);
        inventory.setItem(3, stack);

        // Days
        stack = lgm.getItem(path + "Days", player);
        meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(format(player, meta.getDisplayName()));
        updatedLore.clear();
        for (String s : Objects.requireNonNull(meta.getLore())) {
            updatedLore.add(format(player, s));
        }
        meta.setLore(updatedLore);
        stack.setItemMeta(meta);
        inventory.setItem(4, stack);

        // Hours
        stack = lgm.getItem(path + "Hours", player);
        meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(format(player, meta.getDisplayName()));
        updatedLore.clear();
        for (String s : Objects.requireNonNull(meta.getLore())) {
            updatedLore.add(format(player, s));
        }
        meta.setLore(updatedLore);
        stack.setItemMeta(meta);
        inventory.setItem(5, stack);

        // Minutes
        stack = lgm.getItem(path + "Minutes", player);
        meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(format(player, meta.getDisplayName()));
        updatedLore.clear();
        for (String s : Objects.requireNonNull(meta.getLore())) {
            updatedLore.add(format(player, s));
        }
        meta.setLore(updatedLore);
        stack.setItemMeta(meta);
        inventory.setItem(6, stack);

        // Seconds
        stack = lgm.getItem(path + "Seconds", player);
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

        // Ban
        stack = lgm.getItem(path + "Ban", player);
        meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(format(player, meta.getDisplayName()));
        updatedLore.clear();
        for (String s : Objects.requireNonNull(meta.getLore())) {
            updatedLore.add(format(player, s));
        }
        meta.setLore(updatedLore);
        stack.setItemMeta(meta);
        inventory.setItem(22, stack);

        // General
        inventory.setItem(26, lgm.getItem("General.Close", player));
    }

    public String format(Player player, String message) {
        return Utils.format(player, message, AdminPanelMain.getPrefix()).replace("%reason%", reason)
                .replace("%time%", years + ":" + months + ":" + days + ":" + hours + ":" + minutes + ":" + seconds)
                .replace("%years%", String.valueOf(years))
                .replace("%months%", String.valueOf(months))
                .replace("%days%", String.valueOf(days))
                .replace("%hours%", String.valueOf(hours))
                .replace("%minutes%", String.valueOf(minutes))
                .replace("%seconds%", String.valueOf(seconds));
    }
}
