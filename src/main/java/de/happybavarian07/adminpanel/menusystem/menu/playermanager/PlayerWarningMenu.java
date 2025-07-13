package de.happybavarian07.adminpanel.menusystem.menu.playermanager;/*
 * @Author HappyBavarian07
 * @Date 13.11.2022 | 12:49
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.adminpanel.utils.Warning;
import de.happybavarian07.adminpanel.utils.WarningManager;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerWarningMenu extends PaginatedMenu<Warning> {
    private final UUID targetUUID;
    private final WarningManager warningManager;
    private final Map<ItemStack, Warning> itemsToWarnings = new HashMap<>();

    public PlayerWarningMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        this.targetUUID = playerMenuUtility.getTargetUUID();
        this.warningManager = AdminPanelMain.getPlugin().getWarningManager();
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Warnings");
        List<Warning> warnings = new ArrayList<>(warningManager.getWarnings(targetUUID));
        setPaginatedData(warnings, this::getPageItem);
    }

    @Override
    public String getMenuName() {
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
    public void preSetMenuItems() {
    }

    @Override
    public void postSetMenuItems() {
    }

    @Override
    protected void handlePageItemClick(int indexOnPage, ItemStack item, InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Warn")) {
            player.sendMessage(lgm.getPermissionMessage(player, "AdminPanel.PlayerManager.PlayerSettings.Warn"));
            return;
        }
        List<Warning> warnings = new ArrayList<>(warningManager.getWarnings(targetUUID));
        if (indexOnPage < 0 || indexOnPage >= warnings.size()) return;
        Warning warning = itemsToWarnings.get(item);
        // TODO Handling (Remove/Info)
        if (e.isLeftClick()) {
            player.sendMessage(AdminPanelUtils.format(player, "%prefix% &9> &aThis is the &f#&6" + warning.getWarningCount() + "%a Warning of the Player &6" + Bukkit.getPlayer(targetUUID).getName() + "&a.", AdminPanelMain.getPrefix()));
            player.sendMessage(AdminPanelUtils.format(player, " &f- &3Reason: &6" + warning.getReason(), AdminPanelMain.getPrefix()));
            player.sendMessage(AdminPanelUtils.format(player, " &f- &3reation Date: &6" + longToFormattedDate(warning.getCreationDate(), "yyyy/MM/dd HH:mm:ss"), AdminPanelMain.getPrefix()));
            player.sendMessage(AdminPanelUtils.format(player, " &f- &3Expiration Date: &6" + longToFormattedDate(warning.getExpirationDate(), "yyyy/MM/dd HH:mm:ss"), AdminPanelMain.getPrefix()));
        } else if (e.isRightClick()) {
            warningManager.removeWarning(targetUUID, warning.getWarningCount(), true);
            player.sendMessage(AdminPanelUtils.format(player, "%prefix% &aYou have successfully removed the &f#&6" + warning.getWarningCount() + "%a Warning of the Player &6" + Bukkit.getPlayer(targetUUID).getName() + "&a.", AdminPanelMain.getPrefix()));
        }
        player.sendMessage("Warning: " + itemsToWarnings.getOrDefault(item, new Warning(player.getUniqueId(), "NULL", -1, -1, -12)).toString());
    }

    @Override
    protected void handleCustomItemClick(int slot, ItemStack item, InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (item.isSimilar(lgm.getItem("PlayerManager.WarningMenu.AddWarning", Bukkit.getPlayer(targetUUID), false))) {
            warningManager.addWarning(targetUUID, new Warning(
                    targetUUID,
                    "TestReason#1",
                    System.currentTimeMillis() + 60000,
                    System.currentTimeMillis(),
                    warningManager.getWarningCount(targetUUID, false)
            ), true);
            warningManager.addWarning(targetUUID, new Warning(
                    targetUUID,
                    "TestReason#2",
                    System.currentTimeMillis() + 120000,
                    System.currentTimeMillis(),
                    warningManager.getWarningCount(targetUUID, false)
            ), true);
            warningManager.addWarning(targetUUID, new Warning(
                    targetUUID,
                    "TestReason#2",
                    System.currentTimeMillis() + 180000,
                    System.currentTimeMillis(),
                    warningManager.getWarningCount(targetUUID, false)
            ), true);
        }
    }

    public ItemStack getPageItem(Warning warning) {
        lgm.addPlaceholder(PlaceholderType.ITEM, "%count%", String.valueOf(warning.getWarningCount()), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%reason%", warning.getReason(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%creationDate%", longToFormattedDate(warning.getCreationDate(), "yyyy/MM/dd HH:mm:ss"), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%expirationDate%", longToFormattedDate(warning.getExpirationDate(), "yyyy/MM/dd HH:mm:ss"), false);
        ItemStack item = lgm.getItem("PlayerManager.WarningMenu.WarningItem", Bukkit.getPlayer(targetUUID), true);
        itemsToWarnings.put(item, warning);
        return item;
    }

    private String longToFormattedDate(long date, String format) {
        return new SimpleDateFormat(format).format(new Date(date));
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
