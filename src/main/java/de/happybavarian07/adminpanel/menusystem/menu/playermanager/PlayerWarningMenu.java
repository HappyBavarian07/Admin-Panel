package de.happybavarian07.adminpanel.menusystem.menu.playermanager;/*
 * @Author HappyBavarian07
 * @Date 13.11.2022 | 12:49
 */

import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.PaginatedMenu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.utils.Utils;
import de.happybavarian07.adminpanel.utils.Warning;
import de.happybavarian07.adminpanel.utils.WarningManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerWarningMenu extends PaginatedMenu {
    private final UUID targetUUID;
    private final WarningManager warningManager;
    private final Map<ItemStack, Warning> itemsToWarnings = new HashMap<>();

    public PlayerWarningMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        this.targetUUID = playerMenuUtility.getTargetUUID();
        this.warningManager = AdminPanelMain.getPlugin().getWarningManager();
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Warnings");
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

    /* Permissions:
                (Use the Warn Button)
              AdminPanel.PlayerManager.PlayerSettings.Warn:
                default: op
                (Open Warn Menu)
              AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Warnings:
                default: op
     */
    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();
        List<Warning> warnings = new ArrayList<>(warningManager.getWarnings(targetUUID));

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        assert item != null;
        if (item.equals(lgm.getItem("PlayerManager.WarningMenu.AddWarning", Bukkit.getPlayer(targetUUID), false))) {
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
        } else if (item.getType().equals(lgm.getItem("PlayerManager.WarningMenu.WarningItem", Bukkit.getPlayer(targetUUID), false).getType())) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Warn")) {
                player.sendMessage(noPerms);
                return;
            }
            // TODO Handling (Remove/Info)
            if (e.isLeftClick()) {
                // Info
                Warning warning = itemsToWarnings.get(item);
                // Print out Info about the Warning to the Player (e.g. Reason, Creation Date, Expiration Date)
                player.sendMessage(Utils.format(player, "%prefix% &9> &aThis is the &f#&6" + warning.getWarningCount() + "%a Warning of the Player &6" + Bukkit.getPlayer(targetUUID).getName() + "&a.", AdminPanelMain.getPrefix()));
                player.sendMessage(Utils.format(player, " &f- &3Reason: &6" + warning.getReason(), AdminPanelMain.getPrefix()));
                player.sendMessage(Utils.format(player, " &f- &3reation Date: &6" + longToFormattedDate(warning.getCreationDate(), "yyyy/MM/dd HH:mm:ss"), AdminPanelMain.getPrefix()));
                player.sendMessage(Utils.format(player, " &f- &3Expiration Date: &6" + longToFormattedDate(warning.getExpirationDate(), "yyyy/MM/dd HH:mm:ss"), AdminPanelMain.getPrefix()));
            } else if (e.isRightClick()) {
                // Remove
                Warning warning = itemsToWarnings.get(item);
                warningManager.removeWarning(targetUUID, warning.getWarningCount(), true);
                player.sendMessage(Utils.format(player, "%prefix% &aYou have successfully removed the &f#&6" + warning.getWarningCount() + "%a Warning of the Player &6" + Bukkit.getPlayer(targetUUID).getName() + "&a.", AdminPanelMain.getPrefix()));
            }
            player.sendMessage("Warning: " + itemsToWarnings.getOrDefault(item, new Warning(player.getUniqueId(), "NULL", -1, -1, -12)).toString());
        } else if (item.equals(lgm.getItem("General.Close", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new PlayerActionSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem("General.Left", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.pageleft")) {
                player.sendMessage(noPerms);
                return;
            }
            if (page == 0) {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnFirstPage", player, true));
            } else {
                page = page - 1;
                super.open();
            }
        } else if (item.equals(lgm.getItem("General.Right", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.pageright")) {
                player.sendMessage(noPerms);
                return;
            }
            if (!((index + 1) >= warnings.size())) {
                page = page + 1;
                super.open();
            } else {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player, true));
            }
        } else if (item.equals(lgm.getItem("General.Refresh", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.refresh")) {
                player.sendMessage(noPerms);
                return;
            }
            super.open();
        }
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {

    }

    @Override
    public void setMenuItems() {
        addMenuBorder();
        //The thing you will be looping through to place items
        List<Warning> warnings = new ArrayList<>(warningManager.getWarnings(targetUUID));

        ///////////////////////////////////// Pagination loop template
        if (!warnings.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= warnings.size()) break;
                if (warnings.get(index) != null) {
                    ///////////////////////////
                    Warning warning = warnings.get(index);
                    Player player = Bukkit.getPlayer(targetUUID);
                    try {
                        lgm.addPlaceholder(PlaceholderType.ITEM, "%count%", warning.getWarningCount(), false);
                        lgm.addPlaceholder(PlaceholderType.ITEM, "%reason%", warning.getReason(), false);
                        lgm.addPlaceholder(PlaceholderType.ITEM, "%expirationDate%", longToFormattedDate(warning.getExpirationDate(), "yyyy/MM/dd HH:mm:ss"), false);
                        lgm.addPlaceholder(PlaceholderType.ITEM, "%creationDate%", longToFormattedDate(warning.getCreationDate(), "yyyy/MM/dd HH:mm:ss"), false);
                        ItemStack item = lgm.getItem("PlayerManager.WarningMenu.WarningItem", player, false);
                        inventory.addItem(item);
                        itemsToWarnings.put(item, warning);
                    } catch (NumberFormatException e) {
                        System.out.println("Warning #" + warning.getWarningCount() + " from Player " + player.getName() + " threw a Error. Check if the Creation-/Expiration Date contains any characters that are not numbers!");
                    }
                    ////////////////////////
                }
            }
        }
        inventory.setItem(getSlot("PlayerManager.WarningMenu.AddWarning", 47), lgm.getItem("PlayerManager.WarningMenu.AddWarning", playerMenuUtility.getOwner(), false));
        ////////////////////////
    }

    public String longToFormattedDate(long dateInLongFormat, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date tempDate = new Date(dateInLongFormat);
        return sdf.format(tempDate);
    }
}
