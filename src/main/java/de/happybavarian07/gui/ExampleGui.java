package de.happybavarian07.gui;

import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.menusystem.menu.playermanager.PlayerSelectMenu;
import de.happybavarian07.utils.Utils;
import org.bukkit.*;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExampleGui implements Listener {
	
	public Main plugin;
	
	static FileConfiguration cfg;
	FileConfiguration messages;
	public static List<Inventory> invs = new ArrayList<>();
    
    public ExampleGui(Main main, FileConfiguration messages2, FileConfiguration config) {
    	
    	cfg = config;
    	messages = messages2;
    	this.plugin = main;
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
        Inventory inv = Bukkit.createInventory(null, 27, "§4§lAdmin Panel");
        initializeItems(inv);
        invs.add(inv);
        ent.openInventory(inv);
		if(cfg.getBoolean("Panel.PlaySoundsWhenOpened") == true) {
			if(cfg.getString("Panel.SoundWhenOpened") != null) {
				String sound = cfg.getString("Panel.SoundWhenOpened");
				((Player) ent).playSound(ent.getLocation(), Sound.valueOf(sound), (float) cfg.getDouble("Panel.SoundVolume"), (float) cfg.getDouble("Panel.SoundPitch"));
			}
		}
		if(!ent.getScoreboardTags().contains("AdminPanelOpen")) {
			ent.addScoreboardTag("AdminPanelOpen");
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {
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
						for(Player online : Bukkit.getOnlinePlayers()) {
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
        if (!invs.contains(e.getInventory())) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        final Player player = (Player) e.getWhoClicked();

		LanguageManager lgm = plugin.getLanguageManager();

		String nopermissionmessage = lgm.getMessage("Player.General.NoPermissions", player);

        // Using slots click is a best option for your inventory click's
        if(e.getRawSlot() == 13) {
        	if(clickedItem.getType() == Material.REDSTONE_BLOCK) {
	        	if(player.hasPermission("AdminPanel.PluginManager.open")) {
	        		player.closeInventory();
	        		PluginStopGUI.openInv(player);
	        	} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        }
        if(clickedItem.getType() == Material.DIAMOND_BLOCK) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§6Server Managment")) {
        		if(player.hasPermission("AdminPanel.ServerManagment.Open")) {
        			ServerManagment.openInv(player);
        		}
        	}
        }
        if(e.getRawSlot() == 14) {
        	if(clickedItem.getType() == Material.DIAMOND) {
	        	if(player.hasPermission("AdminPanel.ServerStop")) {
	        		player.closeInventory();
					Utils.getInstance().serverStop(player, 1000, 2000);
	        	} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        }
        if(e.getRawSlot() == 4) {
        	if(clickedItem.getType() == Material.ACACIA_BOAT) {
        		if(player.hasPermission("AdminPanel.ServerReload")) {
					Utils.getInstance().serverReload(player, 1000);
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        }
        if(e.getRawSlot() == 12) {
        	if(clickedItem.getType() == Material.PLAYER_HEAD) {
        		if(player.hasPermission("AdminPanel.PlayerManager.open")) {
        			new PlayerSelectMenu(Main.getPlayerMenuUtility(player)).open();
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        }
        if(e.getRawSlot() == 10) {
        	if(clickedItem.getType() == Material.GRASS_BLOCK) {
        		if(player.hasPermission("AdminPanel.WorldManagment.Open")) {
        			player.closeInventory();
        			WorldManagment.openInv(player);
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        }
    }

	// Cancel dragging in our inventory
	@EventHandler
	public void onInventoryClick(final InventoryDragEvent e) {
		if (invs.contains(e.getInventory())) {
			e.setCancelled(true);
		}
	}
}