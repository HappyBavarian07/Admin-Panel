package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PlayerBanMenu extends Menu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private String reason = "";
    private int seconds = 0;
    private int minutes = 0;
    private int hours = 0;
    private int days = 0;
    private int months = 0;
    private int years = 0;

    public PlayerBanMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Ban");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.BanMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PlayerBanMenu";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {

    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playerMenuUtility.getOwner() != player) return;

        if (playerMenuUtility.hasData("BanPlayerSetNewReason")) {
            this.reason = format(player, event.getMessage());
            playerMenuUtility.removeData("BanPlayerSetNewReason");
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%reason%", reason, true);
            player.sendMessage(lgm.getMessage("Player.PlayerManager.BanMenu.Reason.NewReasonSet", player, true));
            super.open();
            event.setCancelled(true);
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "PlayerManager.ActionsMenu.BanMenu.";

        // Reason
        registerButton(getSlot(path + "Reason", 13),
                lgm.getItem(path + "Reason", player, false),
                (p, e) -> {
                    playerMenuUtility.addData("BanPlayerSetNewReason", true);
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%reason%", reason, true);
                    lgm.addPlaceholder(PlaceholderType.ITEM, "%reason%", reason, false);
                    p.sendMessage(lgm.getMessage("Player.PlayerManager.BanMenu.Reason.EnterNewReason", p, true));
                    p.closeInventory();
                }, null);

        // Time Units (Right-click decrease, Left-click increase)
        registerButton(getSlot(path + "Years", 2), lgm.getItem(path + "Years", player, false),
                (p, e) -> {
                    if (e.getAction().equals(InventoryAction.PICKUP_ALL)) {
                        if (years < 1000) years++;
                    } else if (e.getAction().equals(InventoryAction.PICKUP_HALF)) {
                        if (years > 0) years--;
                    }
                    this.open();
                }, null);

        registerButton(getSlot(path + "Months", 3), lgm.getItem(path + "Months", player, false),
                (p, e) -> {
                    if (e.getAction().equals(InventoryAction.PICKUP_ALL)) {
                        if (months < 1000) months++;
                    } else if (e.getAction().equals(InventoryAction.PICKUP_HALF)) {
                        if (months > 0) months--;
                    }
                    this.open();
                }, null);

        registerButton(getSlot(path + "Days", 4), lgm.getItem(path + "Days", player, false),
                (p, e) -> {
                    if (e.getAction().equals(InventoryAction.PICKUP_ALL)) {
                        if (days < 1000) days++;
                    } else if (e.getAction().equals(InventoryAction.PICKUP_HALF)) {
                        if (days > 0) days--;
                    }
                    this.open();
                }, null);

        registerButton(getSlot(path + "Hours", 5), lgm.getItem(path + "Hours", player, false),
                (p, e) -> {
                    if (e.getAction().equals(InventoryAction.PICKUP_ALL)) {
                        if (hours < 1000) hours++;
                    } else if (e.getAction().equals(InventoryAction.PICKUP_HALF)) {
                        if (hours > 0) hours--;
                    }
                    this.open();
                }, null);

        registerButton(getSlot(path + "Minutes", 6), lgm.getItem(path + "Minutes", player, false),
                (p, e) -> {
                    if (e.getAction().equals(InventoryAction.PICKUP_ALL)) {
                        if (minutes < 1000) minutes += 5;
                    } else if (e.getAction().equals(InventoryAction.PICKUP_HALF)) {
                        if (minutes >= 5) minutes -= 5;
                    }
                    this.open();
                }, null);

        registerButton(getSlot(path + "Seconds", 7), lgm.getItem(path + "Seconds", player, false),
                (p, e) -> {
                    if (e.getAction().equals(InventoryAction.PICKUP_ALL)) {
                        if (seconds < 1000) seconds += 10;
                    } else if (e.getAction().equals(InventoryAction.PICKUP_HALF)) {
                        if (seconds >= 10) seconds -= 10;
                    }
                    this.open();
                }, null);

        // Ban Execution
        registerButton(getSlot(path + "Ban", 22), lgm.getItem(path + "Ban", player, false),
                MenuAction.of((p, e) -> {
                    Date banEnd = new Date();
                    banEnd.setTime(System.currentTimeMillis() +
                            TimeUnit.SECONDS.toMillis(seconds) +
                            TimeUnit.MINUTES.toMillis(minutes) +
                            TimeUnit.HOURS.toMillis(hours) +
                            TimeUnit.DAYS.toMillis(days) +
                            (months * 2628000000L) + (years * 31556952000L));

                    OfflinePlayer target = Bukkit.getOfflinePlayer(playerMenuUtility.getTargetUUID());
                    if (plugin.getConfig().getStringList("Pman.Actions.ExemptPlayers").contains(target.getName())) {
                        p.sendMessage(lgm.getMessage("Player.PlayerManager.BanMenu.NotBannable", p, true));
                        return;
                    }
                    if (target.isBanned()) {
                        p.sendMessage(lgm.getMessage("Player.PlayerManager.BanMenu.AlreadyBanned", p, true));
                        return;
                    }
                    if (target.isOnline()) {
                        target.getPlayer().kickPlayer(format(target.getPlayer(), lgm.getMessage("Player.PlayerManager.BanMenu.TargetKickMessage", target.getPlayer(), true)));
                    }
                    Bukkit.getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(target.getName()), reason, banEnd, p.getName());
                    years = 0;
                    months = 0;
                    days = 0;
                    hours = 0;
                    minutes = 0;
                    seconds = 0;
                    p.sendMessage(lgm.getMessage("Player.PlayerManager.BanMenu.SuccessfullyBanned", p, true));
                    new PlayerSelectMenu(playerMenuUtility, new AdminPanelStartMenu(playerMenuUtility)).open();
                }).requires("AdminPanel.PlayerManager.PlayerSettings.Ban"), null);

        // Close
        registerButton(getSlot("General.Close", 26), lgm.getItem("General.Close", player, false),
                MenuAction.of((p, e) -> {
                    new PlayerActionSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open();
                }).requires("AdminPanel.Button.Close"), null);
    }

    public String format(Player player, String message) {
        return AdminPanelUtils.format(player, message, AdminPanelMain.getPrefix()).replace("%reason%", reason)
                .replace("%time%", years + ":" + months + ":" + days + ":" + hours + ":" + minutes + ":" + seconds)
                .replace("%years%", String.valueOf(years))
                .replace("%months%", String.valueOf(months))
                .replace("%days%", String.valueOf(days))
                .replace("%hours%", String.valueOf(hours))
                .replace("%minutes%", String.valueOf(minutes))
                .replace("%seconds%", String.valueOf(seconds));
    }
}
