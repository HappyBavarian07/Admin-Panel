package de.happybavarian07.adminpanel.menusystem.menu;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.languagemanager.LanguageFile;
import de.happybavarian07.coolstufflib.languagemanager.Placeholder;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TempLanguageSelectMenu extends PaginatedMenu<LanguageFile> {
    public TempLanguageSelectMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission("AdminPanel.SwitchLanguage");
        List<LanguageFile> languages = new ArrayList<>(lgm.getRegisteredLanguages().values());
        languages.removeIf(lang -> lang == lgm.getCurrentLang());
        setPaginatedData(languages, this::getPageItem);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("SwitchLanguageMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "TempLanguageSelectMenu";
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

    public void handlePageItemClick(int indexOnPage, ItemStack item, InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);
        List<LanguageFile> languages = new ArrayList<>(lgm.getRegisteredLanguages().values());
        languages.removeIf(lang -> lang == lgm.getCurrentLang());
        if (indexOnPage < 0 || indexOnPage >= languages.size()) return;
        LanguageFile langFile = languages.get(indexOnPage);
        if (!player.hasPermission("AdminPanel.SwitchLanguage")) {
            player.sendMessage(noPerms);
            return;
        }
        lgm.setCurrentLang(langFile, true);
        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%fullname%", langFile.getFullName() != null ? langFile.getFullName() : "NaN", true);
        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%shortname%", langFile.getLangName() != null ? langFile.getLangName() : "NaN", false);
        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%version%", langFile.getFileVersion() != null ? langFile.getFileVersion() : "NaN", false);
        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%path%", langFile.getLangFile().getPath(), false);
        player.sendMessage(lgm.getMessage("Player.General.SetCurrentLanguage", player, true));
        new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
    }

    @Override
    protected void handleCustomItemClick(int i, ItemStack itemStack, InventoryClickEvent inventoryClickEvent) {

    }

    public ItemStack getPageItem(LanguageFile langFile) {
        ItemStack item = lgm.getItem("StartMenu.SwitchLanguageMenu.LanguageItem", playerMenuUtility.getOwner(), false);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(format(meta.getDisplayName(), langFile));
        List<String> updatedLore = new ArrayList<>();
        for (String lore : meta.getLore()) {
            updatedLore.add(format(lore, langFile));
        }
        meta.setLore(updatedLore);
        item.setItemMeta(meta);
        return item;
    }

    public String format(String in, LanguageFile langFile) {
        Map<String, Placeholder> placeholders = lgm.getNewPlaceholderMap();
        placeholders.put("%fullname%", new Placeholder("%fullname%", langFile.getFullName() != null ? langFile.getFullName() : "NaN", PlaceholderType.ITEM));
        placeholders.put("%shortname%", new Placeholder("%shortname%", langFile.getLangName() != null ? langFile.getLangName() : "NaN", PlaceholderType.ITEM));
        placeholders.put("%version%", new Placeholder("%version%", langFile.getFileVersion() != null ? langFile.getFileVersion() : "NaN", PlaceholderType.ITEM));
        placeholders.put("%path%", new Placeholder("%path%", langFile.getLangFile().getPath(), PlaceholderType.ITEM));
        return lgm.replacePlaceholders(in, placeholders);
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
