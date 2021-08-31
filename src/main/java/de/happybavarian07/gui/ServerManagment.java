package de.happybavarian07.gui;

import de.happybavarian07.events.server.ClearChatEvent;
import de.happybavarian07.events.server.KickAllPlayersEvent;
import de.happybavarian07.events.server.MaintenanceModeToggleEvent;
import de.happybavarian07.events.server.MuteChatEvent;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.utils.ChatUtil;
import de.happybavarian07.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerManagment implements Listener {
	
	static Main plugin;
	public static boolean chatmuted = false;
	
	static FileConfiguration cfg;
    private static Inventory servermanager2;
	public static List<Inventory> servermanagerinvs = new ArrayList<Inventory>();
    private static Inventory chatmanager2;
	public static List<Inventory> chatmanagerinvs = new ArrayList<Inventory>();
    FileConfiguration messages;
    public static boolean maintenance_mode = false;
    
    public ServerManagment(Main main, FileConfiguration messages2, FileConfiguration config) {
    	
    	cfg = config;
    	messages = messages2;
    	plugin = main;
        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example

        // Put the items into the inventory
    }

    // You can call this whenever you want to put the items in
	public static void initializeItems(Inventory servermanager) {
    	for(int i = 0; i < servermanager.getSize() ; i++) {
    		if(servermanager.getItem(i) == null) {
    			servermanager.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, false, " "));
    		}
    	}
    	
    	//Adding Control Items
    	servermanager.setItem(22, createGuiItem(Material.BARRIER, false, "§4Close", "Close this Page!"));
    	servermanager.setItem(12, createGuiItem(Material.IRON_SWORD, true, "§4Kick all Players", "§aClick to kick all Players from the Server!"));
    	servermanager.setItem(4, createGuiItem(Material.ARMOR_STAND, false, "§e§lChat Manager", "§aClick to go to the Chat Manager"));
    	Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			
			@Override
			public void run() {
		    	if(!isMaintenance_mode()) {
		    		servermanager.setItem(14, createGuiItem(Material.REDSTONE, false, "§c§lMaintenance Mode", "§aClick to activate the Maintenance Mode"));
		    	} else {
		    		servermanager.setItem(14, createGuiItem(Material.GLOWSTONE_DUST, false, "§e§lMaintenance Mode", "§aClick to deactivate the Maintenance Mode"));
		    	}
			}
		}, 0L, 20L);
	}

    // You can call this whenever you want to put the items in
	public static void initializeChatItems(Inventory chatmanager) {
    	for(int i = 0; i < chatmanager.getSize() ; i++) {
    		if(chatmanager.getItem(i) == null) {
    			chatmanager.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, false, " "));
    		}
    	}
    	
    	//Adding Control Items
    	chatmanager.setItem(22, createGuiItem(Material.BARRIER, false, "§4Back", "Close this Page!"));
    	chatmanager.setItem(12, createGuiItem(Material.DIAMOND_SWORD, true, "§4Clear Chat", "§aClick to kick all Players from the Server!"));
    	Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				if(isChatmuted()) {
					chatmanager.setItem(14, createGuiItem(Material.GREEN_DYE, false, "§a§lMute Chat", "Click to deactivate the Global Chat Mute"));
				} else {
					chatmanager.setItem(14, createGuiItem(Material.RED_DYE, false, "§a§lMute Chat", "Click to activate the Global Chat Mute"));
				}
			}
		}, 0L, 20L);
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
    	servermanager2 = Bukkit.createInventory(null, 27, "§6§lServer Manager");
        initializeItems(servermanager2);
        servermanagerinvs.add(servermanager2);
		ent.openInventory(servermanager2);
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
    
    // You can open the inventory with this
    public static void openChatInv(final HumanEntity ent) {
    	chatmanager2 = Bukkit.createInventory(null, 27, "§6§lChat Manager");
        initializeChatItems(chatmanager2);
        chatmanagerinvs.add(chatmanager2);
		ent.openInventory(chatmanager2);
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
	
	public static boolean isChatmuted() {
		return chatmuted;
	}

	public static void setChatmuted(boolean chatmuted2) {
		chatmuted = chatmuted2;
	}

	public static boolean isMaintenance_mode() {
		return maintenance_mode;
	}

	public static void setMaintenance_mode(boolean maintenance_mode) {
		ServerManagment.maintenance_mode = maintenance_mode;
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if(isChatmuted()) {
			if(!e.getPlayer().hasPermission("AdminPanel.Bypass.ChatMute")) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(Utils.getInstance().replacePlaceHolders(e.getPlayer(), messages.getString("ChatMute.PlayerMessage"), Main.getPrefix()));
			}
		} else {
			return;
		}
	}
	// TODO Spawn Location setzen
	// Check for clicks on items
	@EventHandler
    public void onInventoryClick(final InventoryClickEvent e) throws InterruptedException {
        if (!servermanagerinvs.contains(e.getInventory()) && !chatmanagerinvs.contains(e.getInventory())) return;
        
        e.setCancelled(true);
        final ItemStack clickedItem = e.getCurrentItem();
        
        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        final Player player = (Player) e.getWhoClicked();

		LanguageManager lgm = plugin.getLanguageManager();

		String nopermissionmessage = lgm.getMessage("Player.General.NoPermissions", player);

        // Using slots click is a best option for your inventory click's
        if(clickedItem.getType() == Material.BARRIER) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Back")) {
		        if(player.hasPermission("AdminPanel.Button.Back")) {
		        	ServerManagment.openInv(player);
		        } else {
					player.sendMessage(nopermissionmessage);
				}
        	} else {
        		if(clickedItem.getItemMeta().getDisplayName().equals("§4Close")) {
    	        	if(player.hasPermission("AdminPanel.Button.Close")) {
    	        		ExampleGui.openInv(player);
    	        	} else {
						player.sendMessage(nopermissionmessage);
					}
        		}
        	}
        }
        if(clickedItem.getType() == Material.ARMOR_STAND) {
        	if(player.hasPermission("AdminPanel.ServerManagment.ChatManager.Open")) {
        		ServerManagment.openChatInv(player);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.REDSTONE) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§c§lMaintenance Mode")) {
        		if(player.hasPermission("AdminPanel.ServerManagment.MaintenanceMode")) {
					MaintenanceModeToggleEvent modeToggleEvent = new MaintenanceModeToggleEvent(player, true);
					Bukkit.getPluginManager().callEvent(modeToggleEvent);
					if(!modeToggleEvent.isCancelled()) {
						setMaintenance_mode(modeToggleEvent.isMaintenanceMode());
						for(Player online : Bukkit.getOnlinePlayers()) {
							if(online != player && !online.hasPermission("AdminPanel.Bypass.KickInMainTenanceMode")) {
								online.kickPlayer(Utils.getInstance().replacePlaceHolders(online, messages.getString("ServerManager.MaintenanceMode"), Main.getPrefix()));
							}
						}
						ChatUtil.getInstance().broadcast(" &4The Server is now in the Maintenance Mode all Players that have no Perms got kicked!", Main.getPrefix());
					}
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        }
        if(clickedItem.getType() == Material.GLOWSTONE_DUST) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§e§lMaintenance Mode")) {
        		if(player.hasPermission("AdminPanel.ServerManagment.MaintenanceMode")) {
					MaintenanceModeToggleEvent modeToggleEvent = new MaintenanceModeToggleEvent(player, false);
					Bukkit.getPluginManager().callEvent(modeToggleEvent);
					if(!modeToggleEvent.isCancelled()) {
						setMaintenance_mode(modeToggleEvent.isMaintenanceMode());
						for(Player online : Bukkit.getOnlinePlayers()) {
							if(online != player && !online.hasPermission("AdminPanel.Bypass.KickInMainTenanceMode")) {
								online.kickPlayer(Utils.getInstance().replacePlaceHolders(online, messages.getString("ServerManager.MaintenanceMode"), Main.getPrefix()));
							}
						}
						ChatUtil.getInstance().broadcast(" &4The Server is no longer in the Maintenance Mode all Players can join again!", Main.getPrefix());
					}
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        }
        if(clickedItem.getType() == Material.IRON_SWORD) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Kick all Players")) {
        		if(player.hasPermission("AdminPanel.ServerManagment.KickAllPlayers")) {
        			List<Player> kickedPlayers = new ArrayList<>();
        			for(Player online : Bukkit.getOnlinePlayers()) {
        				if(!online.hasPermission("AdminPanel.Bypass.KickAll")) {
							kickedPlayers.add(online);
						}
					}
					KickAllPlayersEvent kickAllPlayersEvent = new KickAllPlayersEvent(player, kickedPlayers);
        			Bukkit.getPluginManager().callEvent(kickAllPlayersEvent);
        			if(!kickAllPlayersEvent.isCancelled()) {
						for(Player online : kickedPlayers) {
							if(!online.getName().equals(online.getName())) {
								Utils.getInstance().kick(online, online.getName(),
										Utils.getInstance().replacePlaceHolders(online,
												messages.getString("ServerManager.KickAllPlayersReason"),
												Main.getPrefix()), Utils.getInstance().replacePlaceHolders(online,
												messages.getString("ServerManager.KickAllPlayersSource"),
												Main.getPrefix()));
							}
						}
					}
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        }
        if(clickedItem.getType() == Material.DIAMOND_SWORD) {
        	if(player.hasPermission("AdminPanel.ServerManagment.ChatManager.Clear")) {
				ClearChatEvent clearChatEvent = new ClearChatEvent(player, 100, true);
				Bukkit.getPluginManager().callEvent(clearChatEvent);
				if(!clearChatEvent.isCancelled()) {
					Utils.getInstance().clearChat(clearChatEvent.getLines(), clearChatEvent.showPlayerName(), player);
				}
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.GREEN_DYE) {
        	if(player.hasPermission("AdminPanel.ServerManagment.ChatManager.Mute")) {
				MuteChatEvent muteChatEvent = new MuteChatEvent(player, false);
				Bukkit.getPluginManager().callEvent(muteChatEvent);
				if(!muteChatEvent.isCancelled()) {
					ServerManagment.setChatmuted(muteChatEvent.isChatMuted());
					Bukkit.getServer().broadcastMessage(Utils.getInstance().replacePlaceHolders(player, messages.getString("ChatUnMute.Broadcastheader"), Main.getPrefix()));
					Bukkit.getServer().broadcastMessage(Utils.getInstance().replacePlaceHolders(player, messages.getString("ChatUnMute.Broadcast"), Main.getPrefix()));
					Bukkit.getServer().broadcastMessage(Utils.getInstance().replacePlaceHolders(player, messages.getString("ChatUnMute.Broadcastfooter"), Main.getPrefix()));
				}
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.RED_DYE) {
        	if(player.hasPermission("AdminPanel.ServerManagment.ChatManager.Mute")) {
				MuteChatEvent muteChatEvent = new MuteChatEvent(player, true);
				Bukkit.getPluginManager().callEvent(muteChatEvent);
				if(!muteChatEvent.isCancelled()) {
					ServerManagment.setChatmuted(muteChatEvent.isChatMuted());
					Bukkit.getServer().broadcastMessage(Utils.getInstance().replacePlaceHolders(player, messages.getString("ChatMute.Broadcastheader"), Main.getPrefix()));
					Bukkit.getServer().broadcastMessage(Utils.getInstance().replacePlaceHolders(player, messages.getString("ChatMute.Broadcast"), Main.getPrefix()));
					Bukkit.getServer().broadcastMessage(Utils.getInstance().replacePlaceHolders(player, messages.getString("ChatMute.Broadcastfooter"), Main.getPrefix()));
				}
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (servermanagerinvs.contains(e.getInventory()) && chatmanagerinvs.contains(e.getInventory())) {
          e.setCancelled(true);
        }
    }
}