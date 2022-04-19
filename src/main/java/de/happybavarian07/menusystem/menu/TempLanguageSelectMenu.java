package de.happybavarian07.menusystem.menu;/*
 * @Author HappyBavarian07
 * @Date 16.11.2021 | 15:03
 */

import de.happybavarian07.events.NotAPanelEventException;
import de.happybavarian07.events.player.SelectPlayerEvent;
import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.main.LanguageFile;
import de.happybavarian07.main.PlaceholderType;
import de.happybavarian07.menusystem.PaginatedMenu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.menusystem.menu.playermanager.BannedPlayersMenu;
import de.happybavarian07.menusystem.menu.playermanager.PlayerActionSelectMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class TempLanguageSelectMenu extends PaginatedMenu {
    public TempLanguageSelectMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.SwitchLanguage");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("SwitchLanguageMenu", null);
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();
        List<LanguageFile> languages = new ArrayList<>(lgm.getRegisteredLanguages().values());
        languages.removeIf(lang -> lang.getPlugin() != plugin);
        languages.removeIf(lang -> lang == lgm.getCurrentLang());

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        assert item != null;
        if (item.equals(lgm.getItem("General.Close", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
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
            if (!((index + 1) >= languages.size())) {
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
        if (!languages.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= languages.size()) break;
                if (languages.get(index) != null) {
                    ///////////////////////////

                    LanguageFile current = languages.get(index);
                    ItemStack head = lgm.getItem("StartMenu.SwitchLanguageMenu.LanguageItem", playerMenuUtility.getOwner(), false);
                    ItemMeta meta = head.getItemMeta();
                    meta.setDisplayName(format(meta.getDisplayName(), current));
                    List<String> updatedLore = new ArrayList<>();
                    for(String lore : meta.getLore()) {
                        updatedLore.add(format(lore, current));
                    }
                    meta.setLore(updatedLore);
                    head.setItemMeta(meta);

                    if(item.equals(head)) {
                        lgm.setCurrentLang(languages.get(index), true);
                        LanguageFile langFile = languages.get(index);
                        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%fullname%", langFile.getFullName() != null ? langFile.getFullName() : "NaN", true);
                        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%shortname%", langFile.getLangName() != null ? langFile.getLangName() : "NaN", false);
                        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%version%", langFile.getFileVersion() != null ? langFile.getFileVersion() : "NaN", false);
                        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%path%", langFile.getLangFile().getPath(), false);
                        player.sendMessage(lgm.getMessage("Player.General.SetCurrentLanguage", player, true));
                        new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
                    } else {
                        if(index >= languages.size()) {
                            break;
                        }
                    }

                    ////////////////////////
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        //The thing you will be looping through to place items
        List<LanguageFile> languages = new ArrayList<>(lgm.getRegisteredLanguages().values());
        languages.removeIf(lang -> lang.getPlugin() != plugin);
        languages.removeIf(lang -> lang == lgm.getCurrentLang());

        ///////////////////////////////////// Pagination loop template
        if (!languages.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= languages.size()) break;
                if (languages.get(index) != null) {
                    ///////////////////////////

                    LanguageFile current = languages.get(index);
                    ItemStack head = AdminPanelMain.getPlugin().getLanguageManager().getItem("StartMenu.SwitchLanguageMenu.LanguageItem", playerMenuUtility.getOwner(), false);
                    ItemMeta meta = head.getItemMeta();
                    meta.setDisplayName(format(meta.getDisplayName(), current));
                    List<String> updatedLore = new ArrayList<>();
                    for(String lore : meta.getLore()) {
                        updatedLore.add(format(lore, current));
                    }
                    meta.setLore(updatedLore);
                    head.setItemMeta(meta);
                    inventory.addItem(head);

                    ////////////////////////
                }
            }
        }
        ////////////////////////
    }

    public String format(String in, LanguageFile langFile) {
        Map<String, Object> placeholders = lgm.getNewPlaceholderMap();
        placeholders.put("%fullname%", langFile.getFullName() != null ? langFile.getFullName() : "NaN");
        placeholders.put("%shortname%", langFile.getLangName() != null ? langFile.getLangName() : "NaN");
        placeholders.put("%version%", langFile.getFileVersion() != null ? langFile.getFileVersion() : "NaN");
        placeholders.put("%path%", langFile.getLangFile().getPath());
        return lgm.replacePlaceholders(in, placeholders);
    }
}
