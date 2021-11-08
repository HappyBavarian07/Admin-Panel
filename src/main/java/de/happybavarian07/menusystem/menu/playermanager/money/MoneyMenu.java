package de.happybavarian07.menusystem.menu.playermanager.money;

import de.happybavarian07.events.NotAPanelEventException;
import de.happybavarian07.events.player.MoneyTakeEvent;
import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.menusystem.menu.playermanager.PlayerActionSelectMenu;
import de.happybavarian07.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

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

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta() || target == null || !target.isOnline()) return;
        if (item.equals(lgm.getItem("PlayerManager.MoneyMenu.Give", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Money.Give")) {
                player.sendMessage(noPerms);
                return;
            }
            player.setMetadata("moneyGiveMenuMetaData", new FixedMetadataValue(plugin, targetUUID));
            player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.PleaseEnterAmount", target));
            player.closeInventory();
        } else if (item.equals(lgm.getItem("PlayerManager.MoneyMenu.Take", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Money.Take")) {
                player.sendMessage(noPerms);
                return;
            }
            player.setMetadata("moneyTakeMenuMetaData", new FixedMetadataValue(plugin, targetUUID));
            player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.PleaseEnterAmount", target));
            player.closeInventory();
        } else if (item.equals(lgm.getItem("General.Close", target))) {
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
        inventory.setItem(getSlot("PlayerManager.MoneyMenu.Give", 11), lgm.getItem("PlayerManager.MoneyMenu.Give", target));
        inventory.setItem(getSlot("PlayerManager.MoneyMenu.Take", 15), lgm.getItem("PlayerManager.MoneyMenu.Take", target));
        inventory.setItem(getSlot("General.Close", 26), lgm.getItem("General.Close", target));
    }

    @EventHandler
    public void onAsync(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (player.hasMetadata("moneyGiveMenuMetaData")) {
            UUID targetUUIDEvent = UUID.fromString(player.getMetadata("moneyGiveMenuMetaData").get(0).asString());
            Economy eco = Utils.getInstance().getEconomy();
            if (eco.hasAccount(Bukkit.getOfflinePlayer(targetUUIDEvent))) {
                try {
                    double amount = Double.parseDouble(message);
                    MoneyTakeEvent takeEvent = new MoneyTakeEvent(player, targetUUIDEvent, amount, eco.getBalance(Bukkit.getOfflinePlayer(targetUUIDEvent)));
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(takeEvent);
                        if (!takeEvent.isCancelled()) {
                            eco.depositPlayer(Bukkit.getOfflinePlayer(targetUUIDEvent), amount);
                            player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.GiveMoney", Bukkit.getPlayer(targetUUIDEvent)).replace("%amount%", String.valueOf(amount)));
                        }
                    } catch (NotAPanelEventException notAPanelEventException) {
                        notAPanelEventException.printStackTrace();
                    }
                } catch (NumberFormatException ex) {
                    player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.NotANumber", Bukkit.getPlayer(targetUUIDEvent)));
                }
            }
            event.setCancelled(true);
            super.open();
            player.removeMetadata("moneyGiveMenuMetaData", plugin);
        }
        if (player.hasMetadata("moneyTakeMenuMetaData")) {
            UUID targetUUIDEvent = UUID.fromString(player.getMetadata("moneyTakeMenuMetaData").get(0).asString());
            Economy eco = Utils.getInstance().getEconomy();
            if (eco.hasAccount(Bukkit.getOfflinePlayer(targetUUIDEvent))) {
                try {
                    double amount = Double.parseDouble(message);
                    MoneyTakeEvent takeEvent = new MoneyTakeEvent(player, targetUUIDEvent, amount, eco.getBalance(Bukkit.getOfflinePlayer(targetUUIDEvent)));
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(takeEvent);
                        if (!takeEvent.isCancelled()) {
                            if (eco.has(Bukkit.getOfflinePlayer(targetUUIDEvent), amount)) {
                                eco.withdrawPlayer(Bukkit.getOfflinePlayer(targetUUIDEvent), amount);
                                player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.TakeMoney", Bukkit.getPlayer(targetUUIDEvent)).replace("%amount%", String.valueOf(amount)));
                            } else {
                                player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.NotEnoughMoney", Bukkit.getPlayer(targetUUIDEvent)));
                            }
                        }
                    } catch (NotAPanelEventException notAPanelEventException) {
                        notAPanelEventException.printStackTrace();
                    }
                } catch (NumberFormatException ex) {
                    player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.NotANumber", player));
                }
            }
            event.setCancelled(true);
            super.open();
            player.removeMetadata("moneyTakeMenuMetaData", plugin);
        }
    }
}
