package de.happybavarian07.adminpanel.listeners;/*
 * @Author HappyBavarian07
 * @Date 14.05.2022 | 20:34
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.LanguageManager;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class DisabledItemsListener implements Listener {
    private final AdminPanelMain plugin;
    private final LanguageManager lgm;

    public DisabledItemsListener() {
        this.plugin = AdminPanelMain.getPlugin();
        this.lgm = plugin.getLanguageManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onIactWithDisabledItems(PlayerInteractEvent event) {
        ItemStack stack = event.getItem();
        assert stack != null;
        if (Utils.isVanillaItemDisabled(stack) && !event.getPlayer().hasPermission("AdminPanel.Bypass.ItemDisable")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreativeWithDisabledItems(InventoryCreativeEvent event) {
        ItemStack stack = event.getCurrentItem();
        assert stack != null;
        if (Utils.isVanillaItemDisabled(stack) && !event.getWhoClicked().hasPermission("AdminPanel.Bypass.ItemDisable")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClickWithDisabledItems(InventoryClickEvent event) {
        ItemStack stack = event.getCurrentItem();
        assert stack != null;
        if (Utils.isVanillaItemDisabled(stack) && !event.getWhoClicked().hasPermission("AdminPanel.Bypass.ItemDisable")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDragWithDisabledItems(InventoryDragEvent event) {
        ItemStack stack = event.getCursor();
        assert stack != null;
        if (Utils.isVanillaItemDisabled(stack) && !event.getWhoClicked().hasPermission("AdminPanel.Bypass.ItemDisable")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onIactWithDisabledItems(FurnaceSmeltEvent event) {
        if ((Utils.isVanillaItemDisabled(event.getSource()) || Utils.isVanillaItemDisabled(event.getResult()))) {
            event.setCancelled(true);
        }
    }
}
