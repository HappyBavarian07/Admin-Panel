package de.happybavarian07.adminpanel.menusystem.menu.playermanager.money;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.player.MoneySetEvent;
import de.happybavarian07.adminpanel.events.player.MoneyTakeEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.PlaceholderType;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.PlayerActionSelectMenu;
import de.happybavarian07.adminpanel.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;
import java.util.logging.Level;

public class MoneyMenu extends Menu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final UUID targetUUID;

    public MoneyMenu(PlayerMenuUtility playerMenuUtility, UUID targetUUID) {
        super(playerMenuUtility);
        this.targetUUID = targetUUID;
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Money");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.MoneyMenu", Bukkit.getPlayer(targetUUID));
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

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item == null || !item.hasItemMeta() || target == null || !target.isOnline()) return;
        if (item.equals(lgm.getItem("PlayerManager.MoneyMenu.Give", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Money.Give")) {
                player.sendMessage(noPerms);
                return;
            }
            player.setMetadata("moneyGiveMenuMetaData", new FixedMetadataValue(plugin, targetUUID));
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", target.getName(), true);
            player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.PleaseEnterAmount", player, true));
            player.closeInventory();
        } else if (item.equals(lgm.getItem("PlayerManager.MoneyMenu.Take", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Money.Take")) {
                player.sendMessage(noPerms);
                return;
            }
            player.setMetadata("moneyTakeMenuMetaData", new FixedMetadataValue(plugin, targetUUID));
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", target.getName(), true);
            player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.PleaseEnterAmount", player, true));
            player.closeInventory();
        } else if (item.equals(lgm.getItem("PlayerManager.MoneyMenu.Set", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Money.Set")) {
                player.sendMessage(noPerms);
                return;
            }
            player.setMetadata("moneySetMenuMetaData", new FixedMetadataValue(plugin, targetUUID));
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", target.getName(), true);
            player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.PleaseEnterAmount", player, true));
            player.closeInventory();
        } else if (item.equals(lgm.getItem("General.Close", target, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new PlayerActionSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), targetUUID).open();
        }
    }

    @Override
    public void setMenuItems() {
        Player target = Bukkit.getPlayer(targetUUID);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, super.FILLER);
        }
        inventory.setItem(getSlot("PlayerManager.MoneyMenu.Give", 11), lgm.getItem("PlayerManager.MoneyMenu.Give", target, false));
        inventory.setItem(getSlot("PlayerManager.MoneyMenu.Set", 13), lgm.getItem("PlayerManager.MoneyMenu.Set", target, false));
        inventory.setItem(getSlot("PlayerManager.MoneyMenu.Take", 15), lgm.getItem("PlayerManager.MoneyMenu.Take", target, false));
        inventory.setItem(getSlot("General.Close", 26), lgm.getItem("General.Close", target, false));
    }

    @EventHandler
    public void onAsync(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (player.hasMetadata("moneyGiveMenuMetaData")) {
            UUID targetUUIDEvent = UUID.fromString(player.getMetadata("moneyGiveMenuMetaData").get(0).asString());
            Player targetPlayer = Bukkit.getPlayer(targetUUIDEvent);
            Economy eco = Utils.getInstance().getEconomy();
            if (eco.hasAccount(Bukkit.getOfflinePlayer(targetUUIDEvent))) {
                try {
                    double amount = Double.parseDouble(message);
                    MoneyTakeEvent takeEvent = new MoneyTakeEvent(player, targetUUIDEvent, amount, eco.getBalance(targetPlayer));
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(takeEvent);
                        if (!takeEvent.isCancelled()) {
                            EconomyResponse response = eco.depositPlayer(targetPlayer, amount);
                            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%amount%", amount, true);
                            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", targetPlayer.getName(), true);
                            if (response.transactionSuccess()) {
                                plugin.getFileLogger().writeToLog(Level.INFO, player.getName() + " (" + player.getUniqueId() + ") " +
                                        "gave " + amount + " to " + targetPlayer.getName() + " (" + targetPlayer.getUniqueId() + ")", "[Vault - Money]");
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
            player.removeMetadata("moneyGiveMenuMetaData", plugin);
        }
        if (player.hasMetadata("moneyTakeMenuMetaData")) {
            UUID targetUUIDEvent = UUID.fromString(player.getMetadata("moneyTakeMenuMetaData").get(0).asString());
            Player targetPlayer = Bukkit.getPlayer(targetUUIDEvent);
            Economy eco = Utils.getInstance().getEconomy();
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
                                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", targetPlayer.getName(), true);
                                if (response.transactionSuccess()) {
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
            player.removeMetadata("moneyTakeMenuMetaData", plugin);
        }

        if (player.hasMetadata("moneySetMenuMetaData")) {
            UUID targetUUIDEvent = UUID.fromString(player.getMetadata("moneySetMenuMetaData").get(0).asString());
            Player targetPlayer = Bukkit.getPlayer(targetUUIDEvent);
            Economy eco = Utils.getInstance().getEconomy();
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
                            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", targetPlayer.getName(), true);
                            if (response.transactionSuccess()) {
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
            player.removeMetadata("moneySetMenuMetaData", plugin);
        }
    }
}
