package de.happybavarian07.menusystem.menu.playermanager;

import de.happybavarian07.events.NotAPanelEventException;
import de.happybavarian07.events.player.GiveEffectToPlayerEvent;
import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.menusystem.PaginatedMenu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PotionMenu extends PaginatedMenu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final UUID targetUUID;

    public PotionMenu(PlayerMenuUtility playerMenuUtility, UUID targetUUID) {
        super(playerMenuUtility);
        this.targetUUID = targetUUID;
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Potions");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.PotionMenu", Bukkit.getPlayer(targetUUID));
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();
        List<PotionType> potionList = new ArrayList<>();
        Collections.addAll(potionList, PotionType.values());
        if (item.getType().equals(Material.POTION)) {
            PotionEffect effect = new PotionEffect(PotionType.valueOf(ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase()).getEffectType(),
                    5 * 60 * 20, 2, false, false, false);
            GiveEffectToPlayerEvent giveEffectToPlayerEvent = new GiveEffectToPlayerEvent(player, effect);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(giveEffectToPlayerEvent);
                if (!giveEffectToPlayerEvent.isCancelled()) {
                    try {
                        Bukkit.getPlayer(targetUUID).addPotionEffect(effect);
                    } catch (NullPointerException | IllegalArgumentException ignored) {
                    }
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("General.Close", null))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(lgm.getMessage("Player.General.NoPermissions", player));
                return;
            }
            new PlayerActionsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), targetUUID).open();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.ClearPotions", null))) {
            for (PotionEffect effect : Bukkit.getPlayer(targetUUID).getActivePotionEffects()) {
                Bukkit.getPlayer(targetUUID).removePotionEffect(effect.getType());
            }
        } else if (item.equals(lgm.getItem("General.Left", null)) ||
                item.equals(lgm.getItem("General.Right", null))) {
            if (item.equals(lgm.getItem("General.Left", null))) {
                if (page == 0) {
                    player.sendMessage(lgm.getMessage("Player.General.AlreadyOnFirstPage", player));
                } else {
                    page = page - 1;
                    super.open();
                }
            } else if (item.equals(lgm.getItem("General.Right", null))) {
                if (!((index + 1) >= potionList.size())) {
                    page = page + 1;
                    super.open();
                } else {
                    player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player));
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        inventory.setItem(getSlot("PlayerManager.ActionsMenu.ClearPotions", 47), lgm.getItem("PlayerManager.ActionsMenu.ClearPotions", null));

        List<PotionType> potionList = new ArrayList<>();
        Collections.addAll(potionList, PotionType.values());

        if (potionList != null && !potionList.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= potionList.size()) break;
                if (potionList.get(index) != null) {
                    ///////////////////////////

                    if (potionList.get(index).getEffectType() != null) {
                        ItemStack item = new ItemStack(Material.POTION);
                        ItemMeta meta = item.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a" + potionList.get(index).getEffectType().getName()));
                        item.setItemMeta(meta);
                        inventory.addItem(item);
                    }

                    ////////////////////////
                }
            }
        }
    }
}
