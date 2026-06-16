package de.happybavarian07.adminpanel.menusystem.menu.playermanager.money;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.player.MoneySetEvent;
import de.happybavarian07.adminpanel.events.player.MoneyTakeEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.PlayerActionSelectMenu;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.adminpanel.utils.LogPrefixExtension;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.PermissionAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.UUID;
import java.util.logging.Level;

public class MoneyMenu extends Menu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    public MoneyMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Money");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.MoneyMenu", playerMenuUtility.getTarget());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "MoneyMenu";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        // Logic is now handled by registered MenuActions in setMenuItems
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {

    }

    @Override
    public void setMenuItems() {
        Player target = Bukkit.getPlayer(playerMenuUtility.getTargetUUID());
        if (target == null || !target.isOnline()) return;
        setFillerGlass();

        registerButton(getSlot("PlayerManager.MoneyMenu.Give", 11), lgm.getItem("PlayerManager.MoneyMenu.Give", target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Money.Give", (p, e) -> {
                    playerMenuUtility.addData("moneyGiveMenuMetaData", playerMenuUtility.getTargetUUID());
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", target.getName(), true);
                    p.sendMessage(lgm.getMessage("Player.PlayerManager.Money.PleaseEnterAmount", p, true));
                    p.closeInventory();
                }, lgm), null);
        registerButton(getSlot("PlayerManager.MoneyMenu.Set", 13), lgm.getItem("PlayerManager.MoneyMenu.Set", target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Money.Set", (p, e) -> {
                    playerMenuUtility.addData("moneySetMenuMetaData", playerMenuUtility.getTargetUUID());
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", target.getName(), true);
                    p.sendMessage(lgm.getMessage("Player.PlayerManager.Money.PleaseEnterAmount", p, true));
                    p.closeInventory();
                }, lgm), null);
        registerButton(getSlot("PlayerManager.MoneyMenu.Take", 15), lgm.getItem("PlayerManager.MoneyMenu.Take", target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Money.Take", (p, e) -> {
                    playerMenuUtility.addData("moneyTakeMenuMetaData", playerMenuUtility.getTargetUUID());
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", target.getName(), true);
                    p.sendMessage(lgm.getMessage("Player.PlayerManager.Money.PleaseEnterAmount", p, true));
                    p.closeInventory();
                }, lgm), null);
        registerButton(getSlot("General.Close", 26), lgm.getItem("General.Close", target, false),
                new PermissionAction("AdminPanel.Button.Close", (p, e) -> {
                    new PlayerActionSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open();
                }, lgm), null);
    }

    @EventHandler
    public void onAsync(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (playerMenuUtility.hasData("moneyGiveMenuMetaData")) {
            UUID targetUUIDEvent = UUID.fromString(String.valueOf(playerMenuUtility.getData("moneyGiveMenuMetaData")));
            Player targetPlayer = Bukkit.getPlayer(targetUUIDEvent);
            Economy eco = AdminPanelUtils.getInstance().getEconomy();
            if (eco.hasAccount(Bukkit.getOfflinePlayer(targetUUIDEvent))) {
                try {
                    double amount = Double.parseDouble(message);
                    MoneyTakeEvent takeEvent = new MoneyTakeEvent(player, targetUUIDEvent, amount, eco.getBalance(targetPlayer));
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(takeEvent);
                        if (!takeEvent.isCancelled()) {
                            EconomyResponse response = eco.depositPlayer(targetPlayer, amount);
                            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%amount%", amount, true);
                            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", targetPlayer.getName(), false);
                            if (response.transactionSuccess()) {
                                plugin.getFileLogger().writeToLog(Level.INFO, player.getName() + " (" + player.getUniqueId() + ") " +
                                        "gave " + amount + " to " + targetPlayer.getName() + " (" + targetPlayer.getUniqueId() + ")", LogPrefixExtension.ACTIONSLOGGER_PLUGIN);
                                player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.GiveMoney", player, true));
                            } else {
                                player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.TransactionError", player, true));
                            }
                        }
                    } catch (NotAPanelEventException notAPanelEventException) {
                        notAPanelEventException.printStackTrace();
                    }
                } catch (NumberFormatException ex) {
                    player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.NotANumber", player, true));
                }
            }
            event.setCancelled(true);
            super.open();
            playerMenuUtility.removeData("moneyGiveMenuMetaData");
        }
        if (playerMenuUtility.hasData("moneyTakeMenuMetaData")) {
            UUID targetUUIDEvent = UUID.fromString(String.valueOf(playerMenuUtility.getData("moneyTakeMenuMetaData")));
            Player targetPlayer = Bukkit.getPlayer(targetUUIDEvent);
            Economy eco = AdminPanelUtils.getInstance().getEconomy();
            if (eco.hasAccount(Bukkit.getOfflinePlayer(targetUUIDEvent))) {
                try {
                    double amount = Double.parseDouble(message);
                    MoneyTakeEvent takeEvent = new MoneyTakeEvent(player, targetUUIDEvent, amount, eco.getBalance(Bukkit.getOfflinePlayer(targetUUIDEvent)));
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(takeEvent);
                        if (!takeEvent.isCancelled()) {
                            if (eco.has(Bukkit.getOfflinePlayer(targetUUIDEvent), amount)) {
                                EconomyResponse response = eco.withdrawPlayer(Bukkit.getOfflinePlayer(targetUUIDEvent), amount);
                                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%amount%", amount, true);
                                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", targetPlayer.getName(), false);
                                if (response.transactionSuccess()) {
                                    plugin.getFileLogger().writeToLog(Level.INFO, player.getName() + " (" + player.getUniqueId() + ") " +
                                            "took " + amount + " from " + targetPlayer.getName() + " (" + targetPlayer.getUniqueId() + ")", LogPrefixExtension.VAULT_MONEY);
                                    player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.TakeMoney", player, true));
                                } else {
                                    player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.TransactionError", player, true));
                                }
                            } else {
                                player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.NotEnoughMoney", player, true));
                            }
                        }
                    } catch (NotAPanelEventException notAPanelEventException) {
                        notAPanelEventException.printStackTrace();
                    }
                } catch (NumberFormatException ex) {
                    player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.NotANumber", player, true));
                }
            }
            event.setCancelled(true);
            super.open();
            playerMenuUtility.removeData("moneyTakeMenuMetaData");
        }

        if (playerMenuUtility.hasData("moneySetMenuMetaData")) {
            UUID targetUUIDEvent = UUID.fromString(String.valueOf(playerMenuUtility.getData("moneySetMenuMetaData")));
            Player targetPlayer = Bukkit.getPlayer(targetUUIDEvent);
            Economy eco = AdminPanelUtils.getInstance().getEconomy();
            if (eco.hasAccount(Bukkit.getOfflinePlayer(targetUUIDEvent))) {
                try {
                    double amount = Double.parseDouble(message);
                    MoneySetEvent setEvent = new MoneySetEvent(player, Bukkit.getOfflinePlayer(targetUUIDEvent), amount);
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(setEvent);
                        if (!setEvent.isCancelled()) {
                            eco.withdrawPlayer(Bukkit.getOfflinePlayer(targetUUIDEvent), eco.getBalance(Bukkit.getOfflinePlayer(targetUUIDEvent)));
                            EconomyResponse response = eco.depositPlayer(Bukkit.getOfflinePlayer(targetUUIDEvent), amount);
                            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%amount%", amount, true);
                            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", targetPlayer.getName(), false);
                            if (response.transactionSuccess()) {
                                plugin.getFileLogger().writeToLog(Level.INFO, player.getName() + " (" + player.getUniqueId() + ") " +
                                        "set the Money to " + amount + " from the Player " + targetPlayer.getName() + " (" + targetPlayer.getUniqueId() + ")", LogPrefixExtension.ACTIONSLOGGER_PLUGIN);
                                player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.SetMoney", player, true));
                            } else {
                                player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.TransactionError", player, true));
                            }
                        }
                    } catch (NotAPanelEventException notAPanelEventException) {
                        notAPanelEventException.printStackTrace();
                    }
                } catch (NumberFormatException ex) {
                    player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.NotANumber", player, true));
                }
            }
            event.setCancelled(true);
            super.open();
            playerMenuUtility.removeData("moneySetMenuMetaData");
        }
    }
}
