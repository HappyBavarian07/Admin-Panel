package de.happybavarian07.gui;

import de.happybavarian07.main.Main;
import de.happybavarian07.main.Utils;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
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

public class WorldManagment implements Listener {
	
	public static Main plugin;
	
	static FileConfiguration cfg;
    private static Inventory worldmanagerinv;
    private static Inventory gamerules;
    FileConfiguration messages;
	public static List<Inventory> worldmanagerinvs = new ArrayList<Inventory>();
	public static List<Inventory> gamerulesinvs = new ArrayList<Inventory>();
    
    public WorldManagment(Main main, FileConfiguration messages2, FileConfiguration config) {
    	
    	cfg = config;
    	messages = messages2;
    	plugin = main;
        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example

        // Put the items into the inventory
    }

    // You can call this whenever you want to put the items in
	public static void initializeItems(Inventory worldmanager) {
    	for(int i = 0; i < worldmanager.getSize() ; i++) {
    		if(worldmanager.getItem(i) == null) {
    			worldmanager.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, false, " "));
    		}
    	}
    	Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			for(int i = 0; i < Bukkit.getWorlds().size(); i++) {
				if(!Bukkit.getWorlds().get(i).getPVP()) {
					worldmanager.setItem(11, createGuiItem(Material.DIAMOND_SWORD, false, "§4§lGobal §cPVP", "§aClick to activate the PVP in all Worlds!"));
				} else {
					worldmanager.setItem(11, createGuiItem(Material.DIAMOND_SWORD, false, "§4§lGobal §aPVP", "§aClick to deactivate the PVP in all Worlds!"));
				}
			}
		}, 0L, 20L);
    	//Adding Control Items
    	worldmanager.setItem(22, createGuiItem(Material.BARRIER, false, "§4Close", "Close this Page!"));
    	worldmanager.setItem(13, createGuiItem(Material.IRON_SWORD, false, "§4Time/Weather Changer", "§aUse the Item to open the Inv", "§ato change the Time and the Weather of the World"));
    	worldmanager.setItem(15, createGuiItem(Material.COMMAND_BLOCK, false, "§aGamerules", "Click to change the Gamerules of your World", "Sorry for the weird Shape its because of the Item \"Builder\""));
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
    	worldmanagerinv = Bukkit.createInventory(null, 27, "§5§lWorld Manager");
    	initializeItems(worldmanagerinv);
    	worldmanagerinvs.add(worldmanagerinv);
		ent.openInventory(worldmanagerinv);
		if(cfg.getBoolean("Panel.PlaySoundsWhenOponed") == true) {
			if(cfg.getString("Panel.SoundWhenOpened") != null) {
				String sound = cfg.getString("Panel.SoundWhenOpened");
				((Player) ent).playSound(ent.getLocation(), Sound.valueOf(sound), (float) cfg.getDouble("Panel.SoundVolume"), (float) cfg.getDouble("Panel.SoundPitch"));
			}
		}
		if(!ent.getScoreboardTags().contains("AdminPanelOpen")) {
			ent.addScoreboardTag("AdminPanelOpen");
		}
    }
    
    // You can open the inventory with this
    public static void openGameruleInv(final HumanEntity ent) {
		gamerules = Bukkit.createInventory(null, 9*4, "§aGamerules");
		initializeGameruleItems((Player) ent, gamerules);
		gamerulesinvs.add(gamerules);
		ent.openInventory(gamerules);
		if(cfg.getBoolean("Panel.PlaySoundsWhenOponed") == true) {
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
    
    // TODO Spawn Location
    // Check for clicks on items
	@SuppressWarnings("deprecation")
	@EventHandler
    public void onInventoryClick(final InventoryClickEvent e) throws InterruptedException {
        if (!worldmanagerinvs.contains(e.getInventory()) && !gamerulesinvs.contains(e.getInventory())) return;
        
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
        if(clickedItem.getType() == Material.IRON_SWORD) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Time/Weather Changer")) {
        		if(p.hasPermission("AdminPanel.WorldManagment.Time_Weather.Open")) {
        			Time_Weather_Changer_GUi.openInv(p);
					if(cfg.getBoolean("Panel.PlaySoundsWhenOponed") == true) {
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
        if(clickedItem.getType() == Material.DIAMOND_SWORD) {
        	if(p.hasPermission("AdminPanel.WorldManagment.PVP")) {
	        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lGobal §aPVP")) {
					for(int i = 0; i < Bukkit.getWorlds().size(); i++) {
						Bukkit.getWorlds().get(i).setPVP(false);
					}
	        	}
	        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lGobal §cPVP")) {
					for(int i = 0; i < Bukkit.getWorlds().size(); i++) {
						Bukkit.getWorlds().get(i).setPVP(true);
					}
	        	}
        	} else {
				p.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.GREEN_TERRACOTTA) {
        	if(!clickedItem.getItemMeta().getDisplayName().equals("§2Afternoon")) {
	        	if(p.hasPermission("AdminPanel.WorldManagment.Gamerules")) {
	        		p.getWorld().setGameRuleValue(clickedItem.getItemMeta().getDisplayName(), "false");
	        	} else {
					p.sendMessage(nopermissionmessage);
				}
        	}
        }
        if(clickedItem.getType() == Material.RED_TERRACOTTA) {
            if(p.hasPermission("AdminPanel.WorldManagment.Gamerules")) {
            	p.getWorld().setGameRuleValue(clickedItem.getItemMeta().getDisplayName(), "true");
            } else {
				p.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.COMMAND_BLOCK) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aGamerules")) {
        		if(p.hasPermission("AdminPanel.WorldManagment.Gamerules")) {
        			WorldManagment.openGameruleInv(p);
        		} else {
					p.sendMessage(nopermissionmessage);
				}
        	}
        }
    }

    private static void initializeGameruleItems(Player p, Inventory gameruleinv) {
    	for(int i = 0; i < gamerules.getSize(); i++) {
    		gameruleinv.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, false, " "));
    	}
    	Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			
			@Override
			public void run() {
		    	for(int i = 0; i < p.getWorld().getGameRules().length; i++) {
		    		if(!p.getWorld().getGameRules()[i].equalsIgnoreCase("randomTickSpeed") && !p.getWorld().getGameRules()[i].equalsIgnoreCase("maxEntityCramming") && !p.getWorld().getGameRules()[i].equalsIgnoreCase("maxCommandChainLength")) {
		    			try {
							if((boolean) p.getWorld().getGameRuleValue(GameRule.getByName(p.getWorld().getGameRules()[i]))) {
								gameruleinv.setItem(i, createGuiItem(Material.GREEN_TERRACOTTA, false, p.getWorld().getGameRules()[i], "§1§lValue: §atrue"));
				    		} else {
				    			gameruleinv.setItem(i, createGuiItem(Material.RED_TERRACOTTA, false, p.getWorld().getGameRules()[i], "§1§lValue: §cfalse"));
				    		}
		    			} catch (ClassCastException ignored) { }
		    		}
		    	}
			}
		}, 0L, 20L);
    	gamerules.setItem(35, createGuiItem(Material.BARRIER, false, "§4Back"));
	}

	// Cancel dragging in our inventory
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (worldmanagerinvs.contains(e.getInventory()) && gamerulesinvs.contains(e.getInventory())) {
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