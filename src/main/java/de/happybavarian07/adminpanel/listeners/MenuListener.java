package de.happybavarian07.adminpanel.listeners;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.MenuAddon;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

public class MenuListener implements Listener {
    private BukkitRunnable br;

    public MenuListener() {
        FileConfiguration cfg = AdminPanelMain.getPlugin().getConfig();
        if (cfg.getBoolean("Panel.ShowEffectWhenOpened")) {
            br = new BukkitRunnable() {
                @SuppressWarnings("deprecation")
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        PlayerMenuUtility playerMenuUtility = AdminPanelMain.getAPI().getPlayerMenuUtility(player);
                        if (Bukkit.getPluginManager().getPlugin("SuperVanish") == null) {
                            if (playerMenuUtility.hasData("AdminPanelOpen")) {
                                if (br.isCancelled()) return;

                                Location loc = player.getLocation();
                                loc.setY(loc.getY() + 3);
                                player.playEffect(loc, Effect.valueOf(cfg.getString("Panel.EffectWhenOpened")), 0);
                                for (Player online : Bukkit.getOnlinePlayers()) {
                                    online.playEffect(loc, Effect.valueOf(cfg.getString("Panel.EffectWhenOpened")), 0);
                                }
                            }
                        } else {
                            if (!VanishAPI.isInvisible(player)) {
                                if (playerMenuUtility.hasData("AdminPanelOpen")) {
                                    if (br.isCancelled()) return;

                                    Location loc = player.getLocation();
                                    loc.setY(loc.getY() + 3);
                                    player.playEffect(loc, Effect.valueOf(cfg.getString("Panel.EffectWhenOpened")), 0);
                                    for (Player online : Bukkit.getOnlinePlayers()) {
                                        online.playEffect(loc, Effect.valueOf(cfg.getString("Panel.EffectWhenOpened")), 0);
                                    }
                                }
                            }
                        }
                    }
                }
            };
            br.runTaskTimer(AdminPanelMain.getPlugin(), 0L, 50L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent e) {

        InventoryHolder holder = e.getInventory().getHolder();
        // If the inventoryholder of the inventory clicked on
        // is an instance of Menu, then gg. The reason that
        // an InventoryHolder can be a Menu is because our Menu
        // class implements InventoryHolder!!
        if (holder instanceof Menu) {
            e.setCancelled(true); // prevent them from frickin over the inventory
            if (e.getCurrentItem() == null) { // deal with null exceptions
                return;
            }
            // Since we know our inventoryholder is a menu, get the Menu Object representing
            // the menu we clicked on
            Menu menu = (Menu) holder;
            // Call the handleMenu object which takes the event and processes it
            menu.handleMenu(e);

            for (String menuAddonName : AdminPanelMain.getPlugin().getMenuAddonManager().getMenuAddons(menu.getConfigMenuAddonFeatureName()).keySet()) {
                MenuAddon addon = AdminPanelMain.getPlugin().getMenuAddonManager().getMenuAddons(menu.getConfigMenuAddonFeatureName()).get(menuAddonName);
                addon.handleMenu(e);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        PlayerMenuUtility playerMenuUtility = AdminPanelMain.getAPI().getPlayerMenuUtility(player);
        if (event.getInventory().getHolder() instanceof Menu) {
            Menu holder = (Menu) event.getInventory().getHolder();

            holder.handleCloseMenu(event);

            if (playerMenuUtility.hasData("AdminPanelOpen")) {
                for (String menuAddonName : AdminPanelMain.getPlugin().getMenuAddonManager().getMenuAddons(holder.getConfigMenuAddonFeatureName()).keySet()) {
                    MenuAddon addon = AdminPanelMain.getPlugin().getMenuAddonManager().getMenuAddons(holder.getConfigMenuAddonFeatureName()).get(menuAddonName);
                    addon.onCloseEvent();
                }
                playerMenuUtility.removeData("AdminPanelOpen");
                if (holder.getClass().isAssignableFrom(Listener.class)) HandlerList.unregisterAll((Listener) holder);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        PlayerMenuUtility playerMenuUtility = AdminPanelMain.getAPI().getPlayerMenuUtility(player);
        if (event.getInventory().getHolder() instanceof Menu) {
            Menu holder = (Menu) event.getInventory().getHolder();

            holder.handleOpenMenu(event);

            if (!playerMenuUtility.hasData("AdminPanelOpen")) {
                playerMenuUtility.setData("AdminPanelOpen", true, false);
            }

            if (event.getInventory().getHolder() instanceof AdminPanelStartMenu) {
                FileConfiguration cfg = AdminPanelMain.getPlugin().getConfig();
                if (cfg.getBoolean("Panel.PlaySoundsWhenOpened")) {
                    if (cfg.getString("Panel.SoundWhenOpened") != null) {
                        String sound = cfg.getString("Panel.SoundWhenOpened");
                        player.playSound(player.getLocation(), Sound.valueOf(sound),
                                (float) cfg.getDouble("Panel.SoundVolume"),
                                (float) cfg.getDouble("Panel.SoundPitch"));
                    }
                }
            }
        }
    }
}

