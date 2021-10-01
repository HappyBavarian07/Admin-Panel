package de.happybavarian07.listeners;

import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.menusystem.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class MenuListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {

        InventoryHolder holder = e.getInventory().getHolder();
        //If the inventoryholder of the inventory clicked on
        // is an instance of Menu, then gg. The reason that
        // an InventoryHolder can be a Menu is because our Menu
        // class implements InventoryHolder!!
        if (holder instanceof Menu) {
            e.setCancelled(true); //prevent them from fucking with the inventory
            if (e.getCurrentItem() == null) { //deal with null exceptions
                return;
            }
            //Since we know our inventoryholder is a menu, get the Menu Object representing
            // the menu we clicked on
            Menu menu = (Menu) holder;
            //Call the handleMenu object which takes the event and processes it
            menu.handleMenu(e);
        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Menu) {
            if (event.getPlayer().hasMetadata("AdminPanelOpen")) {
                event.getPlayer().removeMetadata("AdminPanelOpen", AdminPanelMain.getPlugin());
            }
        }
    }

    @EventHandler
    public void onInvOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getInventory().getHolder() instanceof Menu) {
            if (!event.getPlayer().hasMetadata("AdminPanelOpen")) {
                event.getPlayer().setMetadata("AdminPanelOpen", new FixedMetadataValue(AdminPanelMain.getPlugin(), true));
            }
            FileConfiguration cfg = AdminPanelMain.getPlugin().getConfig();
            if (cfg.getBoolean("Panel.PlaySoundsWhenOpened")) {
                if (cfg.getString("Panel.SoundWhenOpened") != null) {
                    String sound = cfg.getString("Panel.SoundWhenOpened");
                    player.playSound(player.getLocation(), Sound.valueOf(sound),
                            (float) cfg.getDouble("Panel.SoundVolume"),
                            (float) cfg.getDouble("Panel.SoundPitch"));
                }
            }
            new BukkitRunnable() {
                @SuppressWarnings("deprecation")
                @Override
                public void run() {
                    if (player.getScoreboardTags().contains("AdminPanelOpen")) {
                        if (cfg.getBoolean("Panel.ShowEffectWhenOpened")) {
                            Location loc = player.getLocation();
                            loc.setY(loc.getY() + 3);
                            player.playEffect(loc, Effect.valueOf(cfg.getString("Panel.EffectWhenOpened")), 0);
                            player.playEffect(loc, Effect.valueOf(cfg.getString("Panel.EffectWhenOpened")), 0);
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                online.playEffect(loc, Effect.valueOf(cfg.getString("Panel.EffectWhenOpened")), 0);
                                online.playEffect(loc, Effect.valueOf(cfg.getString("Panel.EffectWhenOpened")), 0);
                            }
                        }
                    }
                }
            }.runTaskTimer(AdminPanelMain.getPlugin(), 0L, 50L);
        }
    }

}

