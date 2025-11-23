package de.happybavarian07.adminpanel.menusystem.menu.languagemigration;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.languagemanager.LanguageFile;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

/*
 * @Author HappyBavarian07
 * @Date Juli 14, 2025 | 19:52
 */
public class NormalLanguageSelectMenu extends PaginatedMenu<LanguageFile> {
    private final Map<String, LanguageFile> languageFiles; // only lang files with a resource file
    private final NamespacedKey languageItemKey = new NamespacedKey(AdminPanelMain.getPlugin(), "languageItem");

    public NormalLanguageSelectMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission("AdminPanel.LanguageMigrator.NormalMigrate.Open");
        this.languageFiles = new HashMap<>();
        lgm.getRegisteredLanguages().forEach((name, file) -> {
            if (this.getClass().getResource("languages/" + name + ".yml") != null) {
                languageFiles.put(file.getLangName(), file);
            }
        });
        setPaginatedData(languageFiles.values().stream().toList(), languageFile -> {
            lgm.addPlaceholder(PlaceholderType.ITEM, "%language_full_name%", languageFile.getFullName(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%language_short_name%", languageFile.getLangName(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%language_path%", languageFile.getLangFile().getPath(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%language_file_name%", languageFile.getLangFile().getName(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%language_version%", languageFile.getFileVersion(), false);
            ItemStack item = lgm.getItem("LanguageMigrator.NormalMigrate.LanguageItem", playerMenuUtility.getOwner(), true);
            if (item.getItemMeta() != null) {
                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(languageItemKey, PersistentDataType.STRING, languageFile.getLangName());
                item.setItemMeta(meta);
            }
            return item;
        });
    }

    @Override
    public void preSetMenuItems() {

    }

    @Override
    public void postSetMenuItems() {

    }

    @Override
    protected void handlePageItemClick(int slot, ItemStack itemStack, InventoryClickEvent inventoryClickEvent) {
        if (itemStack == null || itemStack.getItemMeta() == null) return;
        String langName = itemStack.getItemMeta().getPersistentDataContainer().get(languageItemKey, PersistentDataType.STRING);
        if (langName == null) return;
        LanguageFile languageFile = languageFiles.get(langName);
        if (languageFile != null) {
            playerMenuUtility.setData("selected_language_file", languageFile, true);
            playerMenuUtility.setData("selected_language_resource", "languages/" + languageFile.getLangName() + ".yml", true);
            new LanguageMigrationMenu(playerMenuUtility, savedMenu).open();
        }
    }

    @Override
    protected void handleCustomItemClick(int i, ItemStack itemStack, InventoryClickEvent inventoryClickEvent) {

    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("LanguageMigrator.NormalMigrate.Selector", playerMenuUtility.getOwner());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "NormalLanguageSelectMenu";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent inventoryOpenEvent) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent inventoryCloseEvent) {

    }
}
