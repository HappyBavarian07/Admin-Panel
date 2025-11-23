package de.happybavarian07.adminpanel.menusystem.menu.languagemigration;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.languagemanager.LanguageFileMigrator;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/*
 * @Author HappyBavarian07
 * @Date Juli 14, 2025 | 20:15
 */
public class MigrationEntryEditMenu extends Menu implements Listener {
    private final LanguageFileMigrator.MigrationEntry migrationEntry;
    private final LanguageMigrationMenu languageMigrationMenu;
    private Object finalValue = "N/A";

    public MigrationEntryEditMenu(PlayerMenuUtility playerMenuUtility, LanguageMigrationMenu languageMigrationMenu) {
        super(playerMenuUtility);
        this.languageMigrationMenu = languageMigrationMenu;
        this.migrationEntry = playerMenuUtility.getData("selected_migration_entry", LanguageFileMigrator.MigrationEntry.class);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("LanguageMigrator.EntryEditMenu", playerMenuUtility.getOwner());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "MigrationEntryEditMenu";
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent inventoryClickEvent) {
        Player player = (Player) inventoryClickEvent.getWhoClicked();
        ItemStack clickedItem = inventoryClickEvent.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }
        if (clickedItem.isSimilar(lgm.getItem("LanguageMigrator.EntryEditMenu.KeepUserValue", player, false))) {
            finalValue = migrationEntry.getUserValue();

            if (inventoryClickEvent.isShiftClick()) {
                migrationEntry.setSelectedForMigration(true);
                migrationEntry.setUserValue(finalValue);
                languageMigrationMenu.openWithMigrationEntry(migrationEntry);
                return;
            }
        } else if (clickedItem.isSimilar(lgm.getItem("LanguageMigrator.EntryEditMenu.KeepResourceValue", player, false))) {
            finalValue = migrationEntry.getResourceValue();

            if (inventoryClickEvent.isShiftClick()) {
                migrationEntry.setSelectedForMigration(true);
                migrationEntry.setUserValue(finalValue);
                languageMigrationMenu.openWithMigrationEntry(migrationEntry);
                return;
            }
        } else if (clickedItem.isSimilar(lgm.getItem("LanguageMigrator.EntryEditMenu.DeleteEntryFromFile", player, false))) {
            finalValue = null;

            if (inventoryClickEvent.isShiftClick()) {
                migrationEntry.setSelectedForMigration(false);
                migrationEntry.setUserValue(null);
                languageMigrationMenu.openWithMigrationEntry(migrationEntry);
                return;
            }
        } else if (clickedItem.isSimilar(lgm.getItem("LanguageMigrator.EntryEditMenu.KeepRemovedFromFile", player, false))) {
            finalValue = null;

            if (inventoryClickEvent.isShiftClick()) {
                migrationEntry.setSelectedForMigration(true);
                migrationEntry.setUserValue(null);
                languageMigrationMenu.openWithMigrationEntry(migrationEntry);
                return;
            }
        } else if (clickedItem.isSimilar(lgm.getItem("LanguageMigrator.EntryEditMenu.EditFinalValue", player, false))) {
            player.closeInventory();
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%current_value%", String.valueOf(finalValue), false);
            player.sendMessage(lgm.getMessage("LanguageMigrator.EntryEditMenu.EnterFinalValue", player, true));
            player.sendMessage(lgm.getMessage("LanguageMigrator.EntryEditMenu.EnterFinalValueFormat", player, true));
            playerMenuUtility.setData("final_value_input", true, true);
            return;
        } else if (clickedItem.isSimilar(lgm.getItem("LanguageMigrator.EntryEditMenu.Close", player, false))) {
            if (finalValue == null) {
                migrationEntry.setSelectedForMigration(true);
                migrationEntry.setUserValue(null);
            } else {
                migrationEntry.setUserValue(finalValue);
                migrationEntry.setSelectedForMigration(true);
            }
            languageMigrationMenu.openWithMigrationEntry(migrationEntry);
            return;
        }
        super.open();
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent inventoryOpenEvent) {
        AdminPanelMain.getPlugin().getServer().getPluginManager().registerEvents(this, AdminPanelMain.getPlugin());
        playerMenuUtility.setData("final_value_input", false, true);
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent inventoryCloseEvent) {
        HandlerList.unregisterAll(this);
        languageMigrationMenu.open();
    }

    @Override
    public void setMenuItems() {
        Player player = playerMenuUtility.getOwner();
        LanguageFileMigrator.MigrationStatus status = migrationEntry.getStatus();
        lgm.addPlaceholder(PlaceholderType.ITEM, "%migration_entry_name%", migrationEntry.getKey(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%migration_entry_status%", status.name(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%migration_entry_user_value%", migrationEntry.getUserValue(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%migration_entry_resource_value%", migrationEntry.getResourceValue(), false);

        inventory.setItem(getSlot("LanguageMigrator.EntryEditMenu.Name", 0), lgm.getItem("LanguageMigrator.EntryEditMenu.Name", player, false));

        // items Keep User Val, Keep Resource Val when status is DIFFERENT_VALUE
        // items Keep User Val, Delete Entry From File when status is MISSING_IN_RESOURCE
        // items Keep Resource Val, Keep Removed From File when status is MISSING_IN_FILE
        // default items for all statuses: edit final value, close (9th slot), final value display (8th slot)
        if (status == LanguageFileMigrator.MigrationStatus.DIFFERENT_VALUE) {
            inventory.setItem(getSlot("LanguageMigrator.EntryEditMenu.KeepUserValue", 1), lgm.getItem("LanguageMigrator.EntryEditMenu.KeepUserValue", player, false));
            inventory.setItem(getSlot("LanguageMigrator.EntryEditMenu.KeepResourceValue", 2), lgm.getItem("LanguageMigrator.EntryEditMenu.KeepResourceValue", player, false));
        } else if (status == LanguageFileMigrator.MigrationStatus.MISSING_IN_RESOURCE) {
            inventory.setItem(getSlot("LanguageMigrator.EntryEditMenu.KeepUserValue", 1), lgm.getItem("LanguageMigrator.EntryEditMenu.KeepUserValue", player, false));
            inventory.setItem(getSlot("LanguageMigrator.EntryEditMenu.DeleteEntryFromFile", 2), lgm.getItem("LanguageMigrator.EntryEditMenu.DeleteEntryFromFile", player, false));
        } else if (status == LanguageFileMigrator.MigrationStatus.MISSING_IN_USER) {
            inventory.setItem(getSlot("LanguageMigrator.EntryEditMenu.KeepResourceValue", 1), lgm.getItem("LanguageMigrator.EntryEditMenu.KeepResourceValue", player, false));
            inventory.setItem(getSlot("LanguageMigrator.EntryEditMenu.KeepRemovedFromFile", 2), lgm.getItem("LanguageMigrator.EntryEditMenu.KeepRemovedFromFile", player, false));
        }
        inventory.setItem(getSlot("LanguageMigrator.EntryEditMenu.EditFinalValue", 3), lgm.getItem("LanguageMigrator.EntryEditMenu.EditFinalValue", player, false));
        lgm.addPlaceholder(PlaceholderType.ITEM, "%migration_entry_final_value%", finalValue, false);
        inventory.setItem(getSlot("LanguageMigrator.EntryEditMenu.FinalValueDisplay", 7), lgm.getItem("LanguageMigrator.EntryEditMenu.FinalValueDisplay", player, true));
        // with hint to close menu via Esc to not save changes
        inventory.setItem(getSlot("LanguageMigrator.EntryEditMenu.Confirm", 8), lgm.getItem("LanguageMigrator.EntryEditMenu.Close", player, false));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!playerMenuUtility.getData("final_value_input", Boolean.class, false)) return;
        if (!player.equals(playerMenuUtility.getOwner())) return;

        String message = event.getMessage();
        event.setCancelled(true);
        playerMenuUtility.setData("final_value_input", false, true);

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(lgm.getMessage("LanguageMigrator.EntryEditMenu.EditCancelled", player, true));
            super.open();
            return;
        }

        try {
            if (message.matches("-?\\d+")) {
                finalValue = Integer.parseInt(message);
            } else if (message.matches("-?\\d+\\.\\d+")) {
                finalValue = Double.parseDouble(message);
            } else if (message.startsWith("[") && message.endsWith("]")) {
                String content = message.substring(1, message.length() - 1);
                if (content.isEmpty()) {
                    finalValue = List.of();
                } else {
                    finalValue = Arrays.asList(content.split("\\s*,\\s*"));
                }
            } else if (message.equalsIgnoreCase("true") || message.equalsIgnoreCase("false")) {
                finalValue = Boolean.parseBoolean(message);
            } else {
                finalValue = message;
            }

            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%final_value%", String.valueOf(finalValue), false);
            player.sendMessage(lgm.getMessage("LanguageMigrator.EntryEditMenu.FinalValueSet", player, true));
        } catch (Exception e) {
            player.sendMessage(lgm.getMessage("LanguageMigrator.EntryEditMenu.InvalidInput", player, true));
            finalValue = message;
        }

        super.open();
    }
}
