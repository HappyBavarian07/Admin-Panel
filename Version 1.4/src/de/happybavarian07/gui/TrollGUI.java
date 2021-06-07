package de.happybavarian07.gui;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.happybavarian07.main.Main;

public class TrollGUI implements Listener {
	
	Main plugin;
	
	public static boolean villagersounds = false;
	
	static FileConfiguration cfg;
	FileConfiguration messages;
	public static Map<Player, Boolean> hurtingwater = new HashMap<Player, Boolean>();
	public static Map<Player, Boolean> chatmute = new HashMap<Player, Boolean>();
	public static List<Inventory> invs = new ArrayList<Inventory>();
    
	static int particle;
	
    public TrollGUI(Main main, FileConfiguration messages2, FileConfiguration config) {
    	
    	cfg = config;
    	messages = messages2;
    	main = plugin;
        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example
    	
        // Put the items into the inventory
    }

    // You can call this whenever you want to put the items in
	public static boolean isVillagersounds() {
		return villagersounds;
	}

	public static void setVillagersounds(boolean villagersounds) {
		TrollGUI.villagersounds = villagersounds;
	}

	public static void initializeTrollItems(Inventory inv, Player target) {
		if(inv.getContents() != null) {
	    	for(int i = 0; i < inv.getSize() ; i++) {
	    		if(inv.getItem(i) == null) {
	    			inv.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
	    		}
	    	}
	    	Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
				
				@Override
				public void run() {
					if(isVillagersounds()) {
				    	inv.setItem(13, createGuiItem(Material.VILLAGER_SPAWN_EGG, "§aAnnoying Villager Sounds", "§aClick to play Annoying Villager Sounds", "§afor the Player!"));
					} else {
				    	inv.setItem(13, createGuiItem(Material.VILLAGER_SPAWN_EGG, "§cAnnoying Villager Sounds", "§aClick to play Annoying Villager Sounds", "§afor the Player!"));
					}
					if(target.getScoreboardTags().contains("blockbreakplacepreventertag")) {
				    	inv.setItem(12, createGuiItem(Material.DIAMOND_PICKAXE, "§aBlock breaking / placing", "§aClick to prevent the Player from Building"));
					} else {
				    	inv.setItem(12, createGuiItem(Material.DIAMOND_PICKAXE, "§cBlock breaking / placing", "§aClick to prevent the Player from Building"));
					}
					if(hurtingwater.containsKey(target)) {
				    	inv.setItem(4, createGuiItem(Material.WATER_BUCKET, "§aHurting Water", "§aClick to deactivate that if,", "§athe Player is in Water he becomes Damage!"));
					} else {
				    	inv.setItem(4, createGuiItem(Material.WATER_BUCKET, "§cHurting Water", "§aClick to activate that if,", "§athe Player is in Water he becomes Damage!"));
					}
					if(chatmute.containsKey(target)) {
						inv.setItem(24, createGuiItem(Material.PAPER, "§a§lMute the Player in Chat", "§aClick to unmute the Player for the Chat!"));
					} else {
						inv.setItem(24, createGuiItem(Material.PAPER, "§c§lMute the Player in Chat", "§aClick to mute the Player for the Chat!"));
					}
					if(target.getScoreboardTags().contains("dupemobsonkill")) {
				    	inv.setItem(20, createGuiItem(Material.CARROT, "§a§lDuplicate Mobs on Kill", "§aClick deactivate that if the Player,", "§akills a Mob it Duplicates!"));
					} else {
				    	inv.setItem(20, createGuiItem(Material.CARROT, "§c§lDuplicate Mobs on Kill", "§aClick activate that if the Player,", "§akills a Mob it Duplicates!"));
					}
				}
			}, 0L, 20L);
	    	inv.setItem(33, createGuiItem(Material.PISTON, "§aKick for Error!", "§aClick to kick the Player for Server stopped Error!"));
	    	inv.setItem(30, createGuiItem(Material.PISTON, "§aKick for ...Connection reset!", "§aClick to kick the Player for ...Connection reset!"));
	    	inv.setItem(29, createGuiItem(Material.PISTON, "§aKick for Serverstop!", "§aClick to kick the Player for Server Stop!"));
	    	inv.setItem(32, createGuiItem(Material.PISTON, "§aKick for Whitelist!", "§aClick to kick the Player for Whitelist!"));
	    	inv.setItem(14, createGuiItem(Material.DIAMOND_CHESTPLATE, "§aDrop Players Inv", "§aThe Player drops all of the Items", "§ain his Inv!"));
	    	inv.setItem(21, createGuiItem(Material.PAPER, "§c§lFake Op", "§aClick send the Player an Fake Op Message!"));
	    	inv.setItem(23, createGuiItem(Material.PAPER, "§c§lFake Deop", "§aClick send the Player an Fake Deop Message!"));
	    	inv.setItem(22, createGuiItem(Material.TNT, "§c§lFake Tnt", "§aClick here to spawn an ignited TNT for the player!"));
	    	inv.setItem(40, createGuiItem(Material.BARRIER, "§cBack", "Click to get back to the Actions Menu"));
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
    public static void openTrollInv(final HumanEntity ent, Player target) {
        Inventory inv = Bukkit.createInventory(null, 45, "§c§lTr§a§lo§e§ll§d§ll §6§lG§5§lU§b§lI§r " + target.getName());
        initializeTrollItems(inv, target);
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
		particle = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				Player p = (Player) ent;
				if(!p.getScoreboardTags().contains("AdminPanelOpen")) {
					Bukkit.getScheduler().cancelTask(particle);
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
    public void onKill(EntityDeathEvent e) {
    	if(e.getEntity().getKiller() != null && e.getEntity() != null) {
	    	if(e.getEntity().getKiller().getScoreboardTags().contains("dupemobsonkill")) {
	    		e.getDrops().clear();
	    		e.setDroppedExp(0);
	    		for(int i = 0; i < Main.plugin.getConfig().getInt("Pman.Troll.MobDupe"); i++) {
		    		e.getEntity().getKiller().getWorld().spawnEntity(e.getEntity().getLocation(), e.getEntityType());
	    		}
	    		e.getEntity().getKiller().getWorld().spawnEntity(e.getEntity().getLocation(), e.getEntityType());
	    	} else {
	    		return;
	    	}
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
        if (!invs.contains(e.getInventory())) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        final Player p = (Player) e.getWhoClicked();
        Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle().replace("§c§lTr§a§lo§e§ll§d§ll §6§lG§5§lU§b§lI§r ", ""));
        // Using slots click is a best option for your inventory click's
        if(clickedItem.getType() == Material.BARRIER) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§cBack")) {
		        if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Actions")) {
		        	p.closeInventory();
		        	PlayerManagerGUI.openActions(p);
		        } else {
					p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Actions").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
				}
        	}
        }
        if(clickedItem.getType() == Material.PAPER) {
        	if(target.isOnline()) {
	        	if(clickedItem.getItemMeta().getDisplayName().equals("§c§lFake Deop")) {
	        		if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FakeDeop")) {
	        	        target.sendMessage("§7[Server: Made " + target.getName() + " no longer a server operator]");
	        		}
	        	}
	        	if(clickedItem.getItemMeta().getDisplayName().equals("§c§lFake Op")) {
	        		if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FakeOp")) {
	        			target.sendMessage("§7[Server: Made " + target.getName() + " a server operator]");
	        		}
	        	}
	        	if(clickedItem.getItemMeta().getDisplayName().equals("§a§lMute the Player in Chat")) {
	        		if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.MuteChat")) {
	        			chatmute.remove(target);
	        		}
	        	}
	        	if(clickedItem.getItemMeta().getDisplayName().equals("§c§lMute the Player in Chat")) {
	        		if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.MuteChat")) {
	        			chatmute.put(target, true);
	        		}
	        	}
        	}
        }
        if(clickedItem.getType() == Material.WATER_BUCKET) {
        	if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.HurtingWater")) {
        		if(!hurtingwater.containsKey(target)) {
        			hurtingwater.put(target, true);
        		} else {
        			hurtingwater.remove(target);
        		}
        	}
        }
        if(clickedItem.getType() == Material.DIAMOND_CHESTPLATE) {
        	if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.DropPlayersInv")) {
        		
                List<ItemStack> items = new ArrayList<ItemStack>();
             
                for(int i = 0; i < target.getInventory().getSize(); i++) {
                	if(target.getInventory().getItem(i) != null) {
                		items.add(target.getInventory().getItem(i));
                	}
                }
             
                target.getInventory().clear();
             
                for(ItemStack item : items) {
                	target.getWorld().dropItem(target.getLocation(), item).setPickupDelay(20);
                }
             
                items.clear();
        	}
        }
        if(clickedItem.getType() == Material.PISTON) {
        	if(target.isOnline()) {
	        	if(clickedItem.getItemMeta().getDisplayName().equals("§aKick for ...Connection reset!")) {
	        		if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForConnectionReset")) {
	        			target.kickPlayer("Internal exception: java.net.SocketException: Connection reset.");
	        		} else {
						p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForConnectionReset").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
					}
	        	}
	        	if(clickedItem.getItemMeta().getDisplayName().equals("§aKick for Whitelist!")) {
	        		if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForWhitelist")) {
	        			target.kickPlayer("You are not whitelisted on this server!");
	        		} else {
						p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForWhitelist").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
					}
	        	}
	        	if(clickedItem.getItemMeta().getDisplayName().equals("§aKick for Serverstop!")) {
	        		if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForServerstop")) {
	        			target.kickPlayer("Server closed");
	        		} else {
						p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForServerstop").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
					}
	        	}
	        	if(clickedItem.getItemMeta().getDisplayName().equals("§aKick for Error!")) {
	        		if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForError")) {
	        			target.kickPlayer("io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: no further informations:");
	        		} else {
						p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForError").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
					}
	        	}
        	} else {
        		p.sendMessage(messages.getString("TargetedPlayerIsNull").replace('&', '§').replace("%prefix", cfg.getString("Plugin.Prefix")).replace("%player%", p.getName()).replace("%targetplayer%", target.getName()));
        	}
        }
        if(clickedItem.getType() == Material.DIAMOND_PICKAXE) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aBlock breaking / placing")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.BuildPrevent")) {
        			if(target.getScoreboardTags().contains("blockbreakplacepreventertag")) {
        				target.removeScoreboardTag("blockbreakplacepreventertag");
        			}
        		} else {
					p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.BuildPrevent").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
				}
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§cBlock breaking / placing")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.BuildPrevent")) {
        			target.addScoreboardTag("blockbreakplacepreventertag");
        		} else {
					p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.BuildPrevent").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
				}
        	}
        }
        if(clickedItem.getType() == Material.TNT) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§c§lFake Tnt")) {
		        if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FakeTNT")) {
		        	Location loc = target.getLocation();
		        	TNTPrimed tnt = target.getWorld().spawn(loc, TNTPrimed.class);
		        	tnt.setFuseTicks(20);
		        	tnt.setYield(0);
		        } else {
					p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FakeTNT").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
				}
        	}
        }
        if(clickedItem.getType() == Material.CARROT) {
        	if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.dupemobsonkill")) {
        		if(!target.getScoreboardTags().contains("dupemobsonkill")) {
        			target.addScoreboardTag("dupemobsonkill");
        		} else {
        			target.removeScoreboardTag("dupemobsonkill");
        		}
        	}
        }
        if(clickedItem.getType() == Material.VILLAGER_SPAWN_EGG) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§cAnnoying Villager Sounds")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.VillagerSounds"))
        			setVillagersounds(true);
        		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
					
					@Override
					public void run() {
						if(isVillagersounds()) {
							target.playSound(target.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 50, (float) 1.0);
						}
					}
				}, 0L, 10L);
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aAnnoying Villager Sounds")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.VillagerSounds")) {
        			setVillagersounds(false);
        		}
        	}
        }
    }
	
	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if(e.getPlayer().getScoreboardTags().contains("blockbreakplacepreventertag")) {
			e.setCancelled(true);
		} else {
			return;
		}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if(chatmute.containsKey(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		if(e.getPlayer().getScoreboardTags().contains("blockbreakplacepreventertag")) {
			e.setCancelled(true);
		} else {
			return;
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if(hurtingwater.containsKey(e.getPlayer())) {
			Bukkit.getScheduler().scheduleAsyncRepeatingTask(Main.plugin, new Runnable() {
				
				@Override
				public void run() {
					if(e.getPlayer().getLocation().getBlock().getType() == Material.WATER) {
						if(e.getPlayer().getHealth() != 0.0) {
							e.getPlayer().setHealth(e.getPlayer().getHealth() - 0.1);
						}
					}
				}
			}, 20L, 1000L);
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