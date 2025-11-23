package de.happybavarian07.adminpanel.menusystem.menu.languagemigration;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.languagemanager.LanguageFile;
import de.happybavarian07.coolstufflib.languagemanager.LanguageFileMigrator;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/*
 * @Author HappyBavarian07
 * @Date Juli 14, 2025 | 20:03
 */
public class LanguageMigrationMenu extends PaginatedMenu<LanguageFileMigrator.MigrationEntry> {
    private final LanguageFile languageFile;
    private final InputStream inputStream;
    private final NamespacedKey migrationItemKey = new NamespacedKey(AdminPanelMain.getPlugin(), "migrationItem");
    private LanguageFileMigrator migrator;
    private List<LanguageFileMigrator.MigrationEntry> migrationEntries;
    private List<LanguageFileMigrator.MigrationEntry> filteredMigrationEntries;
    private List<LanguageFileMigrator.MigrationEntry> actuallyAppliedMigrationEntries;
    private StatusFilterMode statusFilterMode = StatusFilterMode.ALL;

    public LanguageMigrationMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        this.languageFile = playerMenuUtility.getData("selected_language_file", LanguageFile.class);
        this.inputStream = AdminPanelMain.getPlugin().getResource(playerMenuUtility.getData("selected_language_resource", String.class));
        if (languageFile == null || inputStream == null) {
            playerMenuUtility.getOwner().sendMessage(lgm.getMessage("LanguageMigration.Error.NoLanguageFileSelected", playerMenuUtility.getOwner(), false));
            closeAndReturnOrClose();
            return;
        }
        this.migrator = new LanguageFileMigrator(languageFile.getLangFile(), inputStream);
        this.migrationEntries = migrator.getMigrationEntries();
        setOpeningPermission("AdminPanel.LanguageMigration.Open");
        statusFilterMode = playerMenuUtility.getData("language_migration_status_filter", StatusFilterMode.class, StatusFilterMode.ALL);
        filterMigrationEntries(statusFilterMode);
    }

    private void setPaginatedDataFiltered(List<LanguageFileMigrator.MigrationEntry> migrationEntries) {
        setPaginatedData(migrationEntries.stream()
                .filter(entry -> statusFilterMode.test(entry.getStatus()))
                .toList(), this::createPageItem);
    }

    private ItemStack createPageItem(LanguageFileMigrator.MigrationEntry migrationEntry) {
        lgm.addPlaceholder(PlaceholderType.ITEM, "%key%", migrationEntry.getKey(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%user_value%", migrationEntry.getUserValue(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%resource_value%", migrationEntry.getResourceValue(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%migration_status%", migrationEntry.getStatus().name(), false);
        ItemStack item = lgm.getItem("LanguageMigration.Migration.MigrationItem", playerMenuUtility.getOwner(), true);
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(migrationItemKey, org.bukkit.persistence.PersistentDataType.STRING, migrationEntry.toString());
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void preSetMenuItems() {

    }

    @Override
    public void postSetMenuItems() {
        lgm.addPlaceholder(PlaceholderType.ITEM, "%selected_all_filter% ", statusFilterMode == StatusFilterMode.ALL ? "&a > ALL" : "&7 ALL", false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%selected_changed_only_filter% ", statusFilterMode == StatusFilterMode.CHANGED_ONLY ? "&a > CHANGED ONLY" : "&7 CHANGED ONLY", false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%selected_unchanged_only_filter% ", statusFilterMode == StatusFilterMode.UNCHANGED_ONLY ? "&a > UNCHANGED ONLY" : "&7 UNCHANGED ONLY", false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%selected_missing_only_filter% ", statusFilterMode == StatusFilterMode.MISSING_ONLY ? "&a > MISSING ONLY" : "&7 MISSING ONLY", false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%status_filter_mode%", statusFilterMode.name(), false);
        inventory.setItem(getSlot("LanguageMigration.Migration.FilterItem", getSlots() - 7), lgm.getItem("LanguageMigration.Migration.FilterItem", playerMenuUtility.getOwner(), true));
        lgm.addPlaceholder(PlaceholderType.ITEM, "%total_migration_count%", String.valueOf(migrationEntries.size()), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%actually_applied_migration_count%", String.valueOf(actuallyAppliedMigrationEntries != null ? actuallyAppliedMigrationEntries.size() : 0), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%filtered_migration_count%", String.valueOf(filteredMigrationEntries != null ? filteredMigrationEntries.size() : 0), false);
        inventory.setItem(getSlot("LanguageMigration.Migration.ApplyChanges", getSlots() - 8), lgm.getItem("LanguageMigration.Migration.ApplyChanges", playerMenuUtility.getOwner(), false));
    }

    @Override
    protected void handlePageItemClick(int slot, ItemStack itemStack, InventoryClickEvent inventoryClickEvent) {
        if (itemStack == null || itemStack.getItemMeta() == null) return;
        String migrationEntryString = itemStack.getItemMeta().getPersistentDataContainer().get(migrationItemKey, org.bukkit.persistence.PersistentDataType.STRING);
        if (migrationEntryString == null) return;

        LanguageFileMigrator.MigrationEntry migrationEntry = LanguageFileMigrator.MigrationEntry.fromString(migrationEntryString);

        playerMenuUtility.setData("selected_migration_entry", migrationEntry, true);
        new MigrationEntryEditMenu(playerMenuUtility, this).open();
    }

    @Override
    protected void handleCustomItemClick(int slot, ItemStack itemStack, InventoryClickEvent inventoryClickEvent) {
        if (itemStack == null) return;
        Player player = (Player) inventoryClickEvent.getWhoClicked();

        ItemStack filterItem = lgm.getItem("LanguageMigration.Migration.FilterItem", playerMenuUtility.getOwner(), true);
        if (filterItem != null && itemStack.isSimilar(filterItem)) {
            StatusFilterMode newMode = getNextFilterMode(statusFilterMode);
            filterMigrationEntries(newMode);
            super.open();
            return;
        }

        ItemStack applyItem = lgm.getItem("LanguageMigration.Migration.ApplyChanges", playerMenuUtility.getOwner(), false);
        if (applyItem != null && itemStack.isSimilar(applyItem)) {
            List<LanguageFileMigrator.MigrationEntry> selected = migrationEntries.stream()
                    .filter(LanguageFileMigrator.MigrationEntry::isSelectedForMigration)
                    .toList();
            if (selected.isEmpty()) {
                player.sendMessage(lgm.getMessage("LanguageMigration.Message.NoEntriesSelected", player, true));
                return;
            }
            try {
                migrator.migrateSelected();
            } catch (Exception e) {
                player.sendMessage(lgm.getMessage("LanguageMigration.Message.MigrationFailed", player, true));
                return;
            }
            actuallyAppliedMigrationEntries = new ArrayList<>(selected);
            migrationEntries = migrator.getMigrationEntries();
            filterMigrationEntries(statusFilterMode);
            player.sendMessage(lgm.getMessage("LanguageMigration.Message.MigrationApplied", player, true));
        }
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("LanguageMigration.Migration", playerMenuUtility.getOwner());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "LanguageMigrationMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent inventoryOpenEvent) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent inventoryCloseEvent) {

    }

    public void openWithMigrationEntry(LanguageFileMigrator.MigrationEntry migrationEntry) {
        // add the migration entry to the applied migration entries list and remove it from the migration entries list
        // this is used when the user applies a migration entry from the MigrationEntryEditMenu.
        if (migrationEntry == null) return;
        if (migrationEntries.contains(migrationEntry)) {
            migrationEntries.remove(migrationEntry);
            if (actuallyAppliedMigrationEntries == null) {
                actuallyAppliedMigrationEntries = new ArrayList<>();
            }
            actuallyAppliedMigrationEntries.add(migrationEntry);
        }
        filterMigrationEntries(statusFilterMode);
        super.open();
    }

    public void filterMigrationEntries(StatusFilterMode statusFilterMode) {
        this.statusFilterMode = statusFilterMode;
        playerMenuUtility.setData("language_migration_status_filter", statusFilterMode, true);
        if (migrationEntries == null) {
            migrationEntries = migrator.getMigrationEntries();
        }
        filteredMigrationEntries = migrationEntries.stream()
                .filter(entry -> statusFilterMode.test(entry.getStatus()))
                .toList();
        setPaginatedDataFiltered(filteredMigrationEntries);
    }

    private StatusFilterMode getNextFilterMode(StatusFilterMode current) {
        StatusFilterMode[] modes = StatusFilterMode.values();
        int currentIndex = current.ordinal();
        int nextIndex = (currentIndex + 1) % modes.length;
        return modes[nextIndex];
    }

    public enum StatusFilterMode {
        ALL(status -> true),
        CHANGED_ONLY(status -> status == LanguageFileMigrator.MigrationStatus.DIFFERENT_VALUE),
        UNCHANGED_ONLY(status -> status == LanguageFileMigrator.MigrationStatus.UNCHANGED),
        MISSING_ONLY(status -> status == LanguageFileMigrator.MigrationStatus.MISSING_IN_USER || status == LanguageFileMigrator.MigrationStatus.MISSING_IN_RESOURCE);

        private final Predicate<LanguageFileMigrator.MigrationStatus> filterPredicate;

        StatusFilterMode(Predicate<LanguageFileMigrator.MigrationStatus> filterPredicate) {
            this.filterPredicate = filterPredicate;
        }

        public static StatusFilterMode fromString(String name) {
            for (StatusFilterMode mode : values()) {
                if (mode.name().equalsIgnoreCase(name)) {
                    return mode;
                }
            }
            return ALL;
        }

        public boolean test(LanguageFileMigrator.MigrationStatus status) {
            return filterPredicate.test(status);
        }
    }
}
