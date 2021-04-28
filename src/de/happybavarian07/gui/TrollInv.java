package de.happybavarian07.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.happybavarian07.main.Main;

public class TrollInv implements Listener {
	
	Main plugin;
	
	static FileConfiguration cfg;
	FileConfiguration messages;
	public static List<Inventory> trollinvs = new ArrayList<Inventory>();
    
    public TrollInv(Main main, FileConfiguration messages2, FileConfiguration config) {
    	
    	cfg = config;
    	messages = messages2;
    	main = plugin;
        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example

        // Put the items into the inventory
    }

    // You can call this whenever you want to put the items in
	public static void initializeItems(Inventory inv) {
		if(inv.getContents() != null) {
	    	for(int i = 0; i < inv.getSize() ; i++) {
	    		if(inv.getItem(i) == null) {
	    			inv.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
	    		}
	    	}
	        inv.setItem(13, createGuiItem(Material.REDSTONE_BLOCK, "§bPlugin Manager", "§aHere you can manage your Plugins", "§aMax-Plugins that were listed: 90", "Unfortunately, it does not yet show in the menu", " that the plugin is disabled / enabled!"));
	        inv.setItem(14, createGuiItem(Material.DIAMOND, "§4Serverstop", "§aHere you can stop the Server", "§athat you are currently playing on!"));
	        inv.setItem(4, createGuiItem(Material.ACACIA_BOAT, "§6Serverreload", "§aHere you can reload the Server", "§athat you are currently playing on!"));
	        inv.setItem(12, createGuiItem(Material.PLAYER_HEAD, "§6Player Manager", "Opens the Player Manager Menu!"));
	        inv.setItem(10, createGuiItem(Material.GRASS_BLOCK, "§2World Managment", "§aManager for the World", "§ayou are currently playing on!"));
	        inv.setItem(16, createGuiItem(Material.DIAMOND_BLOCK, "§6Server Managment", "§aManager for the Server", "§ayou are currently playing on!"));
	        inv.setItem(22, createGuiItem(Material.HOPPER, "§a|", "§cShortcuts"));
		}
    }

    // Nice little method to create a gui item with a custom name, and description
    protected static ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        
        // Set the name of the item
        meta.setDisplayName(name);
        
        // Set the lore of the item
        meta.setLore(Arrays.asList(lore));
        
        item.setItemMeta(meta);
        
        return item;
    }
    
    // You can open the inventory with this
    public static void openInv(final HumanEntity ent) {
        Inventory inv = Bukkit.createInventory(null, 27, "§4§lTroll Panel");
        initializeItems(inv);
        trollinvs.add(inv);
        ent.openInventory(inv);
		if(cfg.getBoolean("Panel.PlaySoundsWhenOponed") == true) {
			if(cfg.getString("Panel.SoundWhenOpened") != null) {
				String sound = cfg.getString("Panel.SoundWhenOpened");
				((Player) ent).playSound(ent.getLocation(), Sound.valueOf(sound), (float) cfg.getDouble("Panel.SoundVolume"), (float) cfg.getDouble("Panel.SoundPitch"));
			}
		}
		if(!ent.getScoreboardTags().contains("AdminPanelOpen")) {
			ent.addScoreboardTag("AdminPanelOpen");
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				Player p = (Player) ent;
				if(!p.getScoreboardTags().contains("AdminPanelOpen")) {
					return;
				} else {
					if(cfg.getBoolean("Panel.ShowEffectWhenOpened") == true) {
						Location loc = p.getLocation();
						loc.setY(loc.getY() + 3);
						p.playEffect(loc, Effect.valueOf(cfg.getString("Panel.EffectWhenOpened")), 0);
						p.playEffect(loc, Effect.valueOf(cfg.getString("Panel.EffectWhenOpened")), 0);
						p.playEffect(loc, Effect.valueOf(cfg.getString("Panel.EffectWhenOpened")), 0);
						for(Player online : Bukkit.getOnlinePlayers()) {
							online.playEffect(loc, Effect.valueOf(cfg.getString("Panel.EffectWhenOpened")), 0);
							online.playEffect(loc, Effect.valueOf(cfg.getString("Panel.EffectWhenOpened")), 0);
							online.playEffect(loc, Effect.valueOf(cfg.getString("Panel.EffectWhenOpened")), 0);
						}
					}
				}
			}
		}, 0L, 50L);
    }
    
    @EventHandler
    public void onInvClose(final InventoryCloseEvent e) {
    	if(e.getPlayer().getScoreboardTags().contains("AdminPanelOpen")) {
    		e.getPlayer().removeScoreboardTag("AdminPanelOpen");
    	}
    }
    
    // Check for clicks on items
	@EventHandler
    public void onInventoryClick(final InventoryClickEvent e) throws InterruptedException {
        if (!trollinvs.contains(e.getInventory())) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        final Player p = (Player) e.getWhoClicked();

        // Using slots click is a best option for your inventory click's
        if(clickedItem.getType() == Material.REDSTONE_BLOCK) {
	        if(p.hasPermission("AdminPanel.PluginManager.open")) {
	        	p.closeInventory();
	        	PluginStopGUI.openInv(p);
	        } else {
				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PluginManager.open").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
			}
        }
    }

	// Cancel dragging in our inventory
	@EventHandler
	public void onInventoryClick(final InventoryDragEvent e) {
		if (trollinvs.contains(e.getInventory())) {
			e.setCancelled(true);
		}
	}
}