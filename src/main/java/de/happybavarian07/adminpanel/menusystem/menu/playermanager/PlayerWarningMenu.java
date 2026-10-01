package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.data.PlayerWarning;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.warnings.WarningManager;
import de.happybavarian07.adminpanel.warnings.WarningText;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class PlayerWarningMenu extends PaginatedMenu<PlayerWarning> {
    private static final String WARN_PERMISSION = "AdminPanel.PlayerManager.PlayerSettings.Warn";
    private static final String PATH = "PlayerManager.WarningMenu.";
    private static final String MESSAGE_PATH = "Player.PlayerManager.WarningMenu.";

    private final UUID targetUUID;
    private final String targetName;
    private final WarningManager warningManager;
    private boolean showAll;

    public PlayerWarningMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Warnings");
        this.targetUUID = playerMenuUtility.getTargetUUID();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        this.targetName = target.getName() != null ? target.getName() : targetUUID.toString();
        this.warningManager = AdminPanelMain.getPlugin().getWarningManager();
    }

    @Override
    public String getMenuName() {
        lgm.addPlaceholder(PlaceholderType.MENUTITLE, "%target%", targetName, false);
        return lgm.getMenuTitle("PlayerManager.WarningMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PlayerWarningMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void open() {
        Player owner = playerMenuUtility.getOwner();
        if (warningManager == null) {
            CommandText.send(lgm, owner, "Warnings.Disabled");
            return;
        }
        warningManager.load(targetUUID).thenAccept(loaded -> super.open()).exceptionally(failed(owner));
    }

    @Override
    public void preSetMenuItems() {
        List<PlayerWarning> entries = showAll ? warningManager.all(targetUUID) : warningManager.current(targetUUID);
        setPaginatedData(entries, this::getPageItem, this::handleEntryClick);
        int perPage = getPaginatedItemSlots().length;
        if (perPage > 0) page = Math.min(page, Math.max(0, (entries.size() - 1) / perPage));
    }

    @Override
    public void postSetMenuItems() {
        Player owner = playerMenuUtility.getOwner();
        MenuAction add = MenuAction.prompt(CommandText.get(lgm, owner, MESSAGE_PATH + "EnterReason", "%target%", targetName),
                (p, reason) -> warningManager.add(targetUUID, reason, p.getUniqueId())
                        .thenAccept(warning -> {
                            CommandText.send(lgm, p, "Warnings.Added", "%target%", targetName,
                                    "%number%", warningManager.number(warning),
                                    "%count%", warningManager.activeCount(targetUUID), "%reason%", reason);
                            open();
                        }).exceptionally(failed(p))).requires(WARN_PERMISSION);
        registerButton(getSlot(PATH + "AddWarning", 45), lgm.getItem(PATH + "AddWarning", owner, false), add);

        String filterPath = PATH + (showAll ? "Filter.All" : "Filter.Active");
        registerButton(getSlot(filterPath, 46), lgm.getItem(filterPath, owner, false),
                MenuAction.of((p, e) -> {
                    showAll = !showAll;
                    page = 0;
                }).then((p, e) -> refresh()));
    }

    private void handleEntryClick(Player player, PlayerWarning warning, InventoryClickEvent event) {
        MenuAction action;
        if (event.isRightClick()) {
            action = event.isShiftClick() ? removeAction(player, warning) : expireAction(warning);
        } else if (event.isLeftClick()) {
            action = editAction(player, warning);
        } else {
            return;
        }
        action.requires(WARN_PERMISSION).execute(player, event);
    }

    private MenuAction editAction(Player player, PlayerWarning warning) {
        int number = warningManager.number(warning);
        return MenuAction.prompt(CommandText.get(lgm, player, MESSAGE_PATH + "EnterNewReason", "%number%", number, "%target%", targetName),
                (p, reason) -> warningManager.editReason(warning.getId(), reason, p.getUniqueId())
                        .thenAccept(changed -> {
                            CommandText.send(lgm, p, "Warnings." + (changed ? "Edited" : "NotPossible"),
                                    "%target%", targetName, "%number%", number);
                            open();
                        }).exceptionally(failed(p)));
    }

    private MenuAction expireAction(PlayerWarning warning) {
        int number = warningManager.number(warning);
        return (p, e) -> warningManager.expireNow(warning.getId(), p.getUniqueId())
                .thenAccept(ended -> {
                    CommandText.send(lgm, p, "Warnings." + (ended ? "Expired" : "NotActive"), "%target%", targetName, "%number%", number);
                    refresh();
                }).exceptionally(failed(p));
    }

    private MenuAction removeAction(Player player, PlayerWarning warning) {
        int number = warningManager.number(warning);
        return MenuAction.of((p, e) -> warningManager.remove(warning.getId(), p.getUniqueId())
                        .thenAccept(removed -> {
                            CommandText.send(lgm, p, "Warnings." + (removed ? "Removed" : "NotPossible"), "%target%", targetName, "%number%", number);
                            refresh();
                        }).exceptionally(failed(p)))
                .confirm(CommandText.get(lgm, player, MESSAGE_PATH + "ConfirmRemove", "%number%", number, "%target%", targetName));
    }

    public ItemStack getPageItem(PlayerWarning warning) {
        Player viewer = playerMenuUtility.getOwner();
        String status = warning.getStatus().name();
        lgm.addPlaceholder(PlaceholderType.ITEM, "%number%", warningManager.number(warning), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%reason%", warning.getReason(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%issuer%", WarningText.actor(lgm, viewer, warning.getIssuer()), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%created%", WarningText.date(lgm, viewer, warning.getCreatedAt()), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%expires%", WarningText.expires(lgm, viewer, warning), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%status%", WarningText.status(lgm, viewer, warning), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%ended%", WarningText.date(lgm, viewer, warning.getEndedAt()), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%ended_by%", WarningText.actor(lgm, viewer, warning.getEndedBy()), false);
        return lgm.getItem(PATH + "WarningItem." + status.charAt(0) + status.substring(1).toLowerCase(), viewer, true);
    }

    private Function<Throwable, Void> failed(Player player) {
        return error -> {
            CommandText.send(lgm, player, "Warnings.Error");
            AdminPanelMain.logError("The warning menu failed", error);
            return null;
        };
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
