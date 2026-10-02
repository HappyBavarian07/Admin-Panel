package de.happybavarian07.adminpanel.menusystem.menu.playermanager.money;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.player.MoneyGiveEvent;
import de.happybavarian07.adminpanel.events.player.MoneySetEvent;
import de.happybavarian07.adminpanel.events.player.MoneyTakeEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.FeatureGate;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.PlayerActionSelectMenu;
import de.happybavarian07.adminpanel.utils.LogPrefixExtension;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.logging.Level;

/**
 * Gives, sets and takes the money of the targeted player through Vault. The amount is typed into the chat.
 */
public class MoneyMenu extends Menu {
    private static final String PATH = "PlayerManager.MoneyMenu.";
    private static final String PERMISSION = "AdminPanel.PlayerManager.PlayerSettings.Money.";

    private enum Kind {
        GIVE("Give", "Gave"), SET("Set", "SetTo"), TAKE("Take", "Took");

        private final String item;
        private final String message;

        Kind(String item, String message) {
            this.item = item;
            this.message = message;
        }
    }

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
        Player owner = playerMenuUtility.getOwner();
        int[] slots = {11, 13, 15};
        for (Kind kind : Kind.values()) {
            registerButton(getSlot(PATH + kind.item, slots[kind.ordinal()]), lgm.getItem(PATH + kind.item, target, false),
                    MenuAction.prompt(CommandText.get(lgm, owner, "MoneyMenu.EnterAmount", "%target%", target.getName()),
                            (p, text) -> apply(p, kind, text)).requires(PERMISSION + kind.item));
        }
        registerButton(getSlot("General.Close", 26), lgm.getItem("General.Back", target, false),
                MenuAction.of((p, e) -> new PlayerActionSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open())
                        .requires("AdminPanel.Button.Close"), null);
    }

    private void apply(Player staff, Kind kind, String text) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerMenuUtility.getTargetUUID());
        Economy eco = AdminPanelMain.getPlugin().eco;
        if (eco == null) {
            FeatureGate.sendUnavailable(staff, "vault");
        } else {
            run(staff, kind, text, target, eco);
        }
        open();
    }

    private void run(Player staff, Kind kind, String text, OfflinePlayer target, Economy eco) {
        double amount;
        try {
            amount = Double.parseDouble(text.trim());
        } catch (NumberFormatException invalid) {
            amount = -1;
        }
        if (!(amount >= 0) || Double.isInfinite(amount)) {
            CommandText.send(lgm, staff, "MoneyMenu.NotANumber");
            return;
        }
        if (!eco.hasAccount(target)) {
            CommandText.send(lgm, staff, "MoneyMenu.NoAccount", "%target%", target.getName());
            return;
        }
        double balance = eco.getBalance(target);
        if (kind == Kind.TAKE && !eco.has(target, amount)) {
            CommandText.send(lgm, staff, "MoneyMenu.NotEnough", "%target%", target.getName(), "%balance%", balance);
            return;
        }
        try {
            var event = switch (kind) {
                case GIVE -> new MoneyGiveEvent(staff, target, amount);
                case SET -> new MoneySetEvent(staff, target, amount);
                case TAKE -> new MoneyTakeEvent(staff, target.getUniqueId(), amount, balance);
            };
            AdminPanelMain.getAPI().callAdminPanelEvent(event);
            if (event.isCancelled()) return;
        } catch (NotAPanelEventException invalid) {
            AdminPanelMain.logError("A money action was not a valid panel event", invalid);
            return;
        }
        EconomyResponse response = switch (kind) {
            case GIVE -> eco.depositPlayer(target, amount);
            case TAKE -> eco.withdrawPlayer(target, amount);
            case SET -> amount >= balance ? eco.depositPlayer(target, amount - balance) : eco.withdrawPlayer(target, balance - amount);
        };
        if (!response.transactionSuccess()) {
            CommandText.send(lgm, staff, "MoneyMenu.Failed", "%target%", target.getName(), "%error%", response.errorMessage);
            return;
        }
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, staff.getName() + " (" + staff.getUniqueId() + ") "
                + kind.name().toLowerCase() + " " + amount + " for " + target.getName() + " (" + target.getUniqueId() + ")",
                LogPrefixExtension.ACTIONSLOGGER_PLUGIN);
        CommandText.send(lgm, staff, "MoneyMenu." + kind.message, "%amount%", amount, "%target%", target.getName(),
                "%balance%", eco.getBalance(target));
    }
}
