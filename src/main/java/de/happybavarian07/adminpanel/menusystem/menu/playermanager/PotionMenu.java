package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.player.GiveEffectToPlayerEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.actions.PermissionAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PotionMenu extends PaginatedMenu<PotionEffectType> implements Listener {
    private int amplifier;
    private int duration;

    public PotionMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        amplifier = 1; // in Int
        duration = 60; // in Ticks
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Potions");
        List<PotionEffectType> potionList = new ArrayList<>();
        Collections.addAll(potionList, PotionEffectType.values());
        setPaginatedData(potionList, this::getPageItem);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.PotionMenu", playerMenuUtility.getTarget());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PotionMenu";
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
        Player player = playerMenuUtility.getOwner();
        registerButton(47, lgm.getItem("PlayerManager.ActionsMenu.ClearPotions", player, false),
                (p, e) -> {
                    for (PotionEffect effect : playerMenuUtility.getTarget().getActivePotionEffects()) {
                        playerMenuUtility.getTarget().removePotionEffect(effect.getType());
                    }
                    super.open();
                }, null);
        registerButton(45, lgm.getItem("PlayerManager.ActionsMenu.SetDuration", player, false),
                (p, e) -> {
                    playerMenuUtility.setData("SetDurationPotionMenu", true, true);
                    p.sendMessage(lgm.getMessage("PotionMenu.EnterDuration", p, true));
                    p.closeInventory();
                }, null);
        registerButton(46, lgm.getItem("PlayerManager.ActionsMenu.SetAmplifier", player, false),
                (p, e) -> {
                    playerMenuUtility.setData("SetAmplifierPotionMenu", true, true);
                    p.sendMessage(lgm.getMessage("PotionMenu.EnterAmplifier", p, true));
                    p.closeInventory();
                }, null);
        registerButton(getSlot("General.Close", 53), lgm.getItem("General.Close", player, false),
                new PermissionAction("AdminPanel.Button.Close", (p, e) -> {
                    new PlayerActionsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open();
                }, lgm), null);
    }

    @Override
    protected void handlePageItemClick(int indexOnPage, ItemStack item, InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        List<PotionEffectType> potionList = new ArrayList<>();
        Collections.addAll(potionList, PotionEffectType.values());
        if (indexOnPage < 0 || indexOnPage >= potionList.size()) return;
        PotionEffectType type = potionList.get(indexOnPage);
        lgm.addPlaceholder(PlaceholderType.ALL, "%target%", player.getName(), false);
        lgm.addPlaceholder(PlaceholderType.ALL, "%duration%", duration, false);
        lgm.addPlaceholder(PlaceholderType.ALL, "%amplifier%", amplifier, false);
        lgm.addPlaceholder(PlaceholderType.ALL, "%type%", type.getName(), false);
        if (event.isRightClick()) {
            if (player.hasPotionEffect(type)) {
                player.removePotionEffect(type);
                player.sendMessage(lgm.getMessage("PotionMenu.EffectRemoved", player, true));
            }
        } else if (event.isLeftClick()) {
            if (player.hasPotionEffect(type)) return;
            if (duration < 0 || amplifier < 1 || duration > 1000000 || amplifier > 255) return;
            PotionEffect effect = new PotionEffect(type, duration, amplifier - 1, false, false, false);
            GiveEffectToPlayerEvent giveEffectToPlayerEvent = new GiveEffectToPlayerEvent(player, effect);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(giveEffectToPlayerEvent);
                if (!giveEffectToPlayerEvent.isCancelled()) {
                    try {
                        playerMenuUtility.getTarget().addPotionEffect(effect);
                        player.sendMessage(lgm.getMessage("PotionMenu.EffectAdded", player, true));
                    } catch (NullPointerException | IllegalArgumentException ignored) {
                    }
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        }
    }

    @Override
    protected void handleCustomItemClick(int slot, ItemStack item, InventoryClickEvent event) {
        // Logic is now handled by registered MenuActions in postSetMenuItems
    }

    public ItemStack getPageItem(PotionEffectType type) {
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utils.chat("&a" + type.getName()));
        List<String> lore = new ArrayList<>();
        lore.add(Utils.chat("&7Left-Click &a- &7Add Effect"));
        lore.add(Utils.chat("&7Right-Click &c- &7Remove Effect"));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        item.setItemMeta(meta);
        return item;
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playerMenuUtility.getOwner() != player) return;

        if (playerMenuUtility.hasData("SetDurationPotionMenu")) {
            try {
                playerMenuUtility.removeData("SetDurationPotionMenu");
                duration = Integer.parseInt(event.getMessage());
                player.sendMessage(lgm.getMessage("PotionMenu.DurationSet", player, false));
            } catch (NumberFormatException e) {
                player.sendMessage(lgm.getMessage("PotionMenu.NotANumber", player, false));
            } finally {
                this.open();
                event.setCancelled(true);
            }
        } else if (playerMenuUtility.hasData("SetAmplifierPotionMenu")) {
            try {
                playerMenuUtility.removeData("SetAmplifierPotionMenu");
                amplifier = Integer.parseInt(event.getMessage());
                player.sendMessage(lgm.getMessage("PotionMenu.AmplifierSet", player, false));
            } catch (NumberFormatException e) {
                player.sendMessage(lgm.getMessage("PotionMenu.NotANumber", player, false));
            } finally {
                this.open();
                event.setCancelled(true);
            }
        }
    }
}
