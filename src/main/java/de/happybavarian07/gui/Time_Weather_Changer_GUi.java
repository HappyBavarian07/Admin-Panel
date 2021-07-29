package de.happybavarian07.gui;

import de.happybavarian07.main.Main;
import de.happybavarian07.main.Utils;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Time_Weather_Changer_GUi implements Listener {
	
	public Main plugin;
	
	static FileConfiguration cfg;
    private static Inventory timeweatherinventar2;
	public static List<Inventory> timeweatherinvs = new ArrayList<Inventory>();
    FileConfiguration messages;
    
    public Time_Weather_Changer_GUi(Main main, FileConfiguration messages2, FileConfiguration config) {
    	
    	cfg = config;
    	messages = messages2;
    	this.plugin = main;
        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example

        // Put the items into the inventory
    }

    // You can call this whenever you want to put the items in
	public static void initializeItems(Inventory timeweatherinventar) {
    	for(int i = 0; i < timeweatherinventar.getSize() ; i++) {
    		if(timeweatherinventar.getItem(i) == null) {
    			timeweatherinventar.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, false, " "));
    		}
    	}
		timeweatherinventar.setItem(1, createGuiItem(Material.MAGENTA_CONCRETE, false, "§aDay", "§aClick to change the Time to §cMorning"));
		timeweatherinventar.setItem(2, createGuiItem(Material.LIGHT_BLUE_CONCRETE, false, "§aMorning", "§aClick to change the Time to §cNoon"));
		timeweatherinventar.setItem(3, createGuiItem(Material.LIME_CONCRETE, false, "§aNoon", "§aClick to change the Time to §cAfternoon"));
		timeweatherinventar.setItem(4, createGuiItem(Material.GREEN_CONCRETE, false, "§aAfternoon", "§aClick to change the Time to §cSunset"));
		timeweatherinventar.setItem(5, createGuiItem(Material.YELLOW_CONCRETE,false, "§aSunset", "§aClick to change the Time to §cNight"));
		timeweatherinventar.setItem(6, createGuiItem(Material.GRAY_CONCRETE,false, "§aNight", "§aClick to change the Time to §cMidnight"));
		timeweatherinventar.setItem(7, createGuiItem(Material.BLACK_CONCRETE, false, "§aMidnight", "§aClick to change the Time to §cSunrise"));
		timeweatherinventar.setItem(8, createGuiItem(Material.ORANGE_CONCRETE, false, "§aSunrise", "§aClick to change the Time to §cDay"));
		timeweatherinventar.setItem(10, createGuiItem(Material.WATER_BUCKET, false, "§aRain", "§aClick to change the Weather to Thunder"));
		timeweatherinventar.setItem(12, createGuiItem(Material.BUCKET, false, "§aSun", "§aClick to change the Weather to Rain"));
		timeweatherinventar.setItem(11, createGuiItem(Material.LAVA_BUCKET, false, "§aThunder", "§aClick to change the Weather to Clear"));
    	
    	//Adding Control Items
    	timeweatherinventar.setItem(22, createGuiItem(Material.BARRIER, false, "§4Back", "Close this Page!"));
    	timeweatherinventar.setItem(0, createGuiItem(Material.IRON_SWORD, false, "§4Time Changer", "§aUse the Items next to it", "to change the Time of the World"));
    	timeweatherinventar.setItem(9, createGuiItem(Material.DIAMOND_SWORD, false, "§4Weather Changer", "§aUse the Items next to it", "to change the Weather of the World"));
	}

	// Nice little method to create a gui item with a custom name, and description
    protected static ItemStack createGuiItem(final Material material, final boolean enchanted, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        
        // Set the name of the item
        meta.setDisplayName(name);
        
        //Add Item Hiding Flag
        
        if(enchanted == true) {
        	item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        }
        // Set the lore of the item
        meta.setLore(Arrays.asList(lore));
        
        item.setItemMeta(meta);
        
        return item;
    }
    
    // You can open the inventory with this
    public static void openInv(final HumanEntity ent) {
    	timeweatherinventar2 = Bukkit.createInventory(null, 27, "§5§lWorld Manager");
        initializeItems(timeweatherinventar2);
        timeweatherinvs.add(timeweatherinventar2);
		ent.openInventory(timeweatherinventar2);
		if(cfg.getBoolean("Panel.PlaySoundsWhenOpened") == true) {
			if(cfg.getString("Panel.SoundWhenOpened") != null) {
				String sound = cfg.getString("Panel.SoundWhenOpened");
				((Player) ent).playSound(ent.getLocation(), Sound.valueOf(sound), (float) cfg.getDouble("Panel.SoundVolume"), (float) cfg.getDouble("Panel.SoundPitch"));
			}
		}
		if(!ent.getScoreboardTags().contains("AdminPanelOpen")) {
			ent.addScoreboardTag("AdminPanelOpen");
		}
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
        if (!timeweatherinvs.contains(e.getInventory())) return;
        
        e.setCancelled(true);
        final ItemStack clickedItem = e.getCurrentItem();
        
        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        final Player p = (Player) e.getWhoClicked();

        String nopermissionmessage = Utils.getInstance().replacePlaceHolders(p, messages.getString("No-Permission-Message"), Main.getPrefix());

        // Using slots click is a best option for your inventory click's
        if(clickedItem.getType() == Material.BARRIER) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Back")) {
		        if(p.hasPermission("AdminPanel.Button.Back")) {
		        	WorldManagment.openInv(p);
					if(cfg.getBoolean("Panel.PlaySoundsWhenOpened") == true) {
						if(cfg.getString("Panel.SoundWhenOpened") != null) {
							String sound = cfg.getString("Panel.SoundWhenOpened");
							p.playSound(p.getLocation(), Sound.valueOf(sound), (float) cfg.getDouble("Panel.SoundVolume"), (float) cfg.getDouble("Panel.SoundPitch"));
						}
					}
		        } else {
					p.sendMessage(nopermissionmessage);
				}
        	} else {
        		if(clickedItem.getItemMeta().getDisplayName().equals("§4Close")) {
    	        	if(p.hasPermission("AdminPanel.Button.Close")) {
    	        		ExampleGui.openInv(p);
						if(cfg.getBoolean("Panel.PlaySoundsWhenOpened") == true) {
							if(cfg.getString("Panel.SoundWhenOpened") != null) {
								String sound = cfg.getString("Panel.SoundWhenOpened");
								p.playSound(p.getLocation(), Sound.valueOf(sound), (float) cfg.getDouble("Panel.SoundVolume"), (float) cfg.getDouble("Panel.SoundPitch"));
							}
						}
    	        	} else {
						p.sendMessage(nopermissionmessage);
					}
        		}
        	}
        }
    	if(clickedItem.getType() == Material.MAGENTA_CONCRETE) {
    		if(clickedItem.getItemMeta().getDisplayName().equals("§aDay")) {
    			if(p.hasPermission("AdminPanel.WorldManagment.Time.Change")) {
    				p.getWorld().setTime(0);
    			} else {
					p.sendMessage(nopermissionmessage);
				}
    		}
    	}
    	if(clickedItem.getType() == Material.LIGHT_BLUE_CONCRETE) {
    		if(clickedItem.getItemMeta().getDisplayName().equals("§aMorning")) {
    			if(p.hasPermission("AdminPanel.WorldManagment.Time.Change")) {
    				p.getWorld().setTime(1000);
    			} else {
					p.sendMessage(nopermissionmessage);
				}
    		}
    	}
    	if(clickedItem.getType() == Material.LIME_CONCRETE) {
    		if(clickedItem.getItemMeta().getDisplayName().equals("§aNoon")) {
    			if(p.hasPermission("AdminPanel.WorldManagment.Time.Change")) {
    				p.getWorld().setTime(6000);
    			} else {
					p.sendMessage(nopermissionmessage);
				}
    		}
    	}
    	if(clickedItem.getType() == Material.GREEN_CONCRETE) {
    		if(clickedItem.getItemMeta().getDisplayName().equals("§aAfternoon")) {
    			if(p.hasPermission("AdminPanel.WorldManagment.Time.Change")) {
    				p.getWorld().setTime(9000);
    			} else {
					p.sendMessage(nopermissionmessage);
				}
    		}
    	}
    	if(clickedItem.getType() == Material.YELLOW_CONCRETE) {
    		if(clickedItem.getItemMeta().getDisplayName().equals("§aSunset")) {
    			if(p.hasPermission("AdminPanel.WorldManagment.Time.Change")) {
    				p.getWorld().setTime(12000);
    			} else {
					p.sendMessage(nopermissionmessage);
				}
    		}
    	}
    	if(clickedItem.getType() == Material.GRAY_CONCRETE) {
    		if(clickedItem.getItemMeta().getDisplayName().equals("§aNight")) {
    			if(p.hasPermission("AdminPanel.WorldManagment.Time.Change")) {
    				p.getWorld().setTime(14000);
    			} else {
					p.sendMessage(nopermissionmessage);
				}
    		}
    	}
    	if(clickedItem.getType() == Material.BLACK_CONCRETE) {
    		if(clickedItem.getItemMeta().getDisplayName().equals("§aMidnight")) {
    			if(p.hasPermission("AdminPanel.WorldManagment.Time.Change")) {
    				p.getWorld().setTime(18000);
    			} else {
					p.sendMessage(nopermissionmessage);
				}
    		}
    	}
    	if(clickedItem.getType() == Material.ORANGE_CONCRETE) {
    		if(clickedItem.getItemMeta().getDisplayName().equals("§aSunrise")) {
				p.getWorld().setTime(23000);
    			if(p.hasPermission("AdminPanel.WorldManagment.Time.Change")) {
    				p.getWorld().setTime(23000);
    			} else {
					p.sendMessage(nopermissionmessage);
				}
    		}
    	}
    	if(clickedItem.getType() == Material.WATER_BUCKET) {
    		if(clickedItem.getItemMeta().getDisplayName().equals("§aRain")) {
    			if(p.hasPermission("AdminPanel.WorldManagment.Weather.Change")) {
    				p.getWorld().setStorm(true);
    			} else {
					p.sendMessage(nopermissionmessage);
				}
    		}
    	}
    	if(clickedItem.getType() == Material.BUCKET) {
    		if(clickedItem.getItemMeta().getDisplayName().equals("§aSun")) {
    			if(p.hasPermission("AdminPanel.WorldManagment.Weather.Change")) {
    				p.getWorld().setThundering(false);
    				p.getWorld().setStorm(false);
    			} else {
					p.sendMessage(nopermissionmessage);
				}
    		}
    	}
    	if(clickedItem.getType() == Material.LAVA_BUCKET) {
    		if(clickedItem.getItemMeta().getDisplayName().equals("§aThunder")) {
    			if(p.hasPermission("AdminPanel.WorldManagment.Weather.Change")) {
    				p.sendMessage("§cCurrently in development! : (");
    			} else {
					p.sendMessage(nopermissionmessage);
				}
    		}
    	}
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (timeweatherinvs.contains(e.getInventory())) {
          e.setCancelled(true);
        }
    }
    
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		if(p.isBanned()) {
			e.setKickMessage("§cDu wurdest vom Server gebannt!\n" + 
							"\n" + 
							"§3Von: §e" + Bukkit.getBanList(Type.NAME).getBanEntry(p.getName()).getSource().toString() + "\n" + 
							"\n" + 
							"§3Reason: §e" + Bukkit.getBanList(Type.NAME).getBanEntry(p.getName()).getReason().toString() + "\n" + 
							"\n" + 
							"§3Permanently banned!" + "\n" + 
							"\n" + 
							"§3Du kannst §c§nkeinen§3 Entbannungsantrag stellen!");
		}
	}
}