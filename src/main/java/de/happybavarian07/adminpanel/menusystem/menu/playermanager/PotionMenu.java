package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.player.GiveEffectToPlayerEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.PlaceholderType;
import de.happybavarian07.adminpanel.menusystem.PaginatedMenu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PotionMenu extends PaginatedMenu implements Listener {
    private final UUID targetUUID;
    private int amplifier;
    private int duration;

    public PotionMenu(PlayerMenuUtility playerMenuUtility, UUID targetUUID) {
        super(playerMenuUtility);
        this.targetUUID = targetUUID;
        amplifier = 1; // in Int
        duration = 60; // in Ticks
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Potions");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.PotionMenu", Bukkit.getPlayer(targetUUID));
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
    public void handleMenu(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();
        List<PotionEffectType> potionList = new ArrayList<>();
        Collections.addAll(potionList, PotionEffectType.values());
        lgm.addPlaceholder(PlaceholderType.ALL, "%target%", player.getName(), false);
        lgm.addPlaceholder(PlaceholderType.ALL, "%duration%", duration, false);
        lgm.addPlaceholder(PlaceholderType.ALL, "%amplifier%", amplifier, false);
        if (item.getType().equals(Material.POTION)) {
            lgm.addPlaceholder(PlaceholderType.ALL, "%type%", PotionEffectType.getByName(ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase()).getName(), false);
            if (event.isRightClick()) {
                if (player.hasPotionEffect(Objects.requireNonNull(PotionEffectType.getByName(ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase())))) {
                    player.removePotionEffect(Objects.requireNonNull(PotionEffectType.getByName(ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase())));
                    player.sendMessage(lgm.getMessage("PotionMenu.EffectRemoved", player, true));
                }
            } else if (event.isLeftClick()) {
                if(player.hasPotionEffect(Objects.requireNonNull(PotionEffectType.getByName(ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase())))) return;
                if (duration < 0 || amplifier < 1 || duration > 1000000 || amplifier > 255) return;
                PotionEffect effect = new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase())),
                        duration, amplifier - 1, false, false, false);
                GiveEffectToPlayerEvent giveEffectToPlayerEvent = new GiveEffectToPlayerEvent(player, effect);
                try {
                    AdminPanelMain.getAPI().callAdminPanelEvent(giveEffectToPlayerEvent);
                    if (!giveEffectToPlayerEvent.isCancelled()) {
                        try {
                            Bukkit.getPlayer(targetUUID).addPotionEffect(effect);
                            player.sendMessage(lgm.getMessage("PotionMenu.EffectAdded", player, true));
                        } catch (NullPointerException | IllegalArgumentException ignored) {
                        }
                    }
                } catch (NotAPanelEventException notAPanelEventException) {
                    notAPanelEventException.printStackTrace();
                }
            }
        } else if (item.equals(lgm.getItem("General.Close", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(lgm.getMessage("Player.General.NoPermissions", player, true));
                return;
            }
            new PlayerActionsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), targetUUID).open();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.ClearPotions", null, false))) {
            for (PotionEffect effect : Bukkit.getPlayer(targetUUID).getActivePotionEffects()) {
                Bukkit.getPlayer(targetUUID).removePotionEffect(effect.getType());
            }
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.SetDuration", null, false))) {
            player.setMetadata("SetDurationPotionMenu", new FixedMetadataValue(plugin, true));
            player.sendMessage(lgm.getMessage("PotionMenu.EnterDuration", player, false));
            player.closeInventory();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.SetAmplifier", null, false))) {
            player.setMetadata("SetAmplifierPotionMenu", new FixedMetadataValue(plugin, true));
            player.sendMessage(lgm.getMessage("PotionMenu.EnterAmplifier", player, false));
            player.closeInventory();
        } else if (item.equals(lgm.getItem("General.Left", null, false)) ||
                item.equals(lgm.getItem("General.Right", null, false))) {
            if (item.equals(lgm.getItem("General.Left", null, false))) {
                if (page == 0) {
                    player.sendMessage(lgm.getMessage("Player.General.AlreadyOnFirstPage", player, true));
                } else {
                    page = page - 1;
                    super.open();
                }
            } else if (item.equals(lgm.getItem("General.Right", null, false))) {
                if (!((index + 1) >= potionList.size())) {
                    page = page + 1;
                    super.open();
                } else {
                    player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player, true));
                }
            }
        }
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("SetDurationPotionMenu")) {
            try {
                player.removeMetadata("SetDurationPotionMenu", plugin);
                duration = Integer.parseInt(event.getMessage());
                player.sendMessage(lgm.getMessage("PotionMenu.DurationSet", player, false));
            } catch (NumberFormatException e) {
                player.sendMessage(lgm.getMessage("PotionMenu.NotANumber", player, false));
            } finally {
                this.open();
                event.setCancelled(true);
            }
        } else if (player.hasMetadata("SetAmplifierPotionMenu")) {
            try {
                player.removeMetadata("SetAmplifierPotionMenu", plugin);
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

    @Override
    public void setMenuItems() {
        Player player = playerMenuUtility.getOwner();
        addMenuBorder();
        lgm.addPlaceholder(PlaceholderType.ALL, "%duration%", duration, false);
        lgm.addPlaceholder(PlaceholderType.ALL, "%amplifier%", amplifier, false);

        inventory.setItem(getSlot("PlayerManager.ActionsMenu.ClearPotions", 47), lgm.getItem("PlayerManager.ActionsMenu.ClearPotions", player, false));
        inventory.setItem(getSlot("PlayerManager.ActionsMenu.SetDuration", 45), lgm.getItem("PlayerManager.ActionsMenu.SetDuration", player, false));
        inventory.setItem(getSlot("PlayerManager.ActionsMenu.SetAmplifier", 46), lgm.getItem("PlayerManager.ActionsMenu.SetAmplifier", player, false));


        List<PotionEffectType> potionList = new ArrayList<>();
        Collections.addAll(potionList, PotionEffectType.values());

        if (!potionList.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= potionList.size()) break;
                if (potionList.get(index) != null) {
                    ///////////////////////////

                    if (potionList.get(index) != null) {
                        ItemStack item = new ItemStack(Material.POTION);
                        ItemMeta meta = item.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a" + potionList.get(index).getName()));

                        List<String> lore = new ArrayList<>();
                        lore.add(Utils.chat("&7Left-Click &a- &7Add Effect"));
                        lore.add(Utils.chat("&7Right-Click &a- &7Remove Effect"));

                        meta.setLore(lore);
                        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                        item.setItemMeta(meta);
                        inventory.addItem(item);
                    }

                    ////////////////////////
                }
            }
        }
    }
}
