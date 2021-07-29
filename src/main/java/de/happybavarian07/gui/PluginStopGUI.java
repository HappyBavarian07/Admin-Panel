package de.happybavarian07.gui;

import de.happybavarian07.events.plugins.PluginDisableEvent;
import de.happybavarian07.events.plugins.PluginEnableEvent;
import de.happybavarian07.events.plugins.PluginRestartEvent;
import de.happybavarian07.main.Main;
import de.happybavarian07.main.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginStopGUI implements Listener {
	
	static Main plugin;
	
	static FileConfiguration cfg;
	FileConfiguration messages;
    private static Inventory inv2;
	public static List<Inventory> invs = new ArrayList<>();
    private static Inventory page2_2;
	public static List<Inventory> page2invs = new ArrayList<>();
    private static Inventory pluginmanager2;
	public static List<Inventory> pluginmanagerinvs = new ArrayList<>();
    private static Inventory permissionspage1_2;
	public static List<Inventory> permissionspage1invs = new ArrayList<>();
    private static Inventory permissionspage2_2;
	public static List<Inventory> permissionspage2invs = new ArrayList<>();
    private static Inventory permissionspage3_2;
	public static List<Inventory> permissionspage3invs = new ArrayList<>();
    
    public PluginStopGUI(Main main, FileConfiguration messages2, FileConfiguration config) {
        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example
        
        cfg = config;
        messages = messages2;
    	plugin = main;
    	
        // Put the items into the inventory
    }
    
	// You can call this whenever you want to put the items in
    public static void initializeItems(Inventory inv, Inventory page2) {
    	Plugin[] PluginArray = plugin.getServer().getPluginManager().getPlugins();
		if(PluginArray.length > 0) {
			for(int i = 0; i < PluginArray.length; i++) {
				if(i <= 44) {
					String name = PluginArray[i].getName();
					inv.setItem(i, createGuiItem(Material.LIME_WOOL, 1, name, "§2Name: §6" + plugin.getServer().getPluginManager().getPlugin(name).getDescription().getName(), "§2Version: §6" + plugin.getServer().getPluginManager().getPlugin(name).getDescription().getVersion(), "§2Author(s): §6" + plugin.getServer().getPluginManager().getPlugin(name).getDescription().getAuthors(), "§2Website: §6" + plugin.getServer().getPluginManager().getPlugin(name).getDescription().getWebsite(), "§2API-Version: §6" + plugin.getServer().getPluginManager().getPlugin(name).getDescription().getAPIVersion(), "§2Full Name: §6" + plugin.getServer().getPluginManager().getPlugin(name).getDescription().getFullName()));
				} else if(i >= 44) {
					String name2 = PluginArray[i].getName();
					page2.setItem((i-45), createGuiItem(Material.LIME_WOOL, 1, name2, "§2Name: §6" + plugin.getServer().getPluginManager().getPlugin(name2).getDescription().getName(), "§2Version: §6" + plugin.getServer().getPluginManager().getPlugin(name2).getDescription().getVersion(), "§2Author(s): §6" + plugin.getServer().getPluginManager().getPlugin(name2).getDescription().getAuthors(),"§2Website: §6" + plugin.getServer().getPluginManager().getPlugin(name2).getDescription().getWebsite(), "§2API-Version: §6" + plugin.getServer().getPluginManager().getPlugin(name2).getDescription().getAPIVersion(), "§2Full Name: §6" + plugin.getServer().getPluginManager().getPlugin(name2).getDescription().getFullName()));
				}
			}
			for(int i = 45; i < inv.getSize() ; i++) {
				if(inv.getItem(i) == null) {
					inv.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, 1, " "));
					page2.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, 1, " "));
				}
			}
		inv.setItem(49, createGuiItem(Material.BARRIER, 1, "§aBack"));
		inv.setItem(50, createGuiItem(Material.ARROW, 1, "§aPage forward"));
		inv.setItem(53, createGuiItem(Material.DIAMOND, 1, "§aNumber of Plugins: " + PluginArray.length));
		page2.setItem(49, createGuiItem(Material.BARRIER, 1, "§aBack"));
		page2.setItem(48, createGuiItem(Material.ARROW, 1, "§aPage back"));
		page2.setItem(53, createGuiItem(Material.DIAMOND, 1, "§aNumber of Plugins: " + PluginArray.length));
		}
    }

    // Nice little method to create a gui item with a custom name, and description
    protected static ItemStack createGuiItem(final Material material, final int amount, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        
        // Set the name of the item
        meta.setDisplayName(name);
        
        //Set the Amount of the Item
        item.setAmount(amount);
        
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        // Set the lore of the item
        meta.setLore(Arrays.asList(lore));
        
        item.setItemMeta(meta);
        
        return item;
    }
    
    // You can open the inventory with this
    public static void openInv(final HumanEntity ent) {
        inv2 = Bukkit.createInventory(null, 54, "§4§lPluginManager: §rPage 1");
        page2_2 = Bukkit.createInventory(null, 54, "§4§lPluginManager: §rPage 2");
        initializeItems(inv2, page2_2);
        invs.add(inv2);
        ent.openInventory(inv2);
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
    
    public static void openPage2(final HumanEntity ent) {
    	page2invs.add(page2_2);
    	ent.openInventory(page2_2);
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
    
    public static void openPluginMenu(final HumanEntity ent) {
    	pluginmanagerinvs.add(pluginmanager2);
    	ent.openInventory(pluginmanager2);
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
    
    public static void openPermissionsPage1(final HumanEntity ent) {
    	permissionspage1invs.add(permissionspage1_2);
    	ent.openInventory(permissionspage1_2);
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
    
    public static void openPermissionsPage2(final HumanEntity ent) {
    	permissionspage2invs.add(permissionspage2_2);
    	ent.openInventory(permissionspage2_2);
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
    
    public static void openPermissionsPage3(final HumanEntity ent) {
    	permissionspage3invs.add(permissionspage3_2);
    	ent.openInventory(permissionspage3_2);
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
	@SuppressWarnings({"unlikely-arg-type"})
	@EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!invs.contains(e.getInventory()) && !page2invs.contains(e.getInventory()) && !pluginmanagerinvs.contains(e.getInventory()) && !permissionspage1invs.contains(e.getInventory()) && !permissionspage2invs.contains(e.getInventory()) && !permissionspage3invs.contains(e.getInventory())) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();
        
        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        final Player p = (Player) e.getWhoClicked();

        String nopermissionmessage = Utils.getInstance().replacePlaceHolders(p, messages.getString("No-Permission-Message"), Main.getPrefix());

        // Using slots click is a best option for your inventory click's
        if(clickedItem.getType() == Material.LIME_WOOL && !clickedItem.getItemMeta().getDisplayName().equals("§aEnabled") && !clickedItem.getItemMeta().getDisplayName().equals("§cDisabled")) {
	        if(p.hasPermission("AdminPanel.PluginManager.PluginSettings.Open")) {
	        	p.closeInventory();
	        	pluginmanager2 = Bukkit.createInventory(null, 9*1, clickedItem.getItemMeta().getDisplayName());
	        	if(plugin.getServer().getPluginManager().isPluginEnabled(clickedItem.getItemMeta().getDisplayName())) {
	        		pluginmanager2.setItem(0, createGuiItem(Material.LIME_WOOL, 1, "§aEnabled", "§cClick to disable!"));
	        	} else {
	        		pluginmanager2.setItem(0, createGuiItem(Material.RED_WOOL, 1, "§cDisabled", "§aClick to enable!"));
	        	}
	        	pluginmanager2.setItem(1, createGuiItem(Material.HOPPER, 1, "§aRestart", "§cRestart the Plugin!"));
	        	if(plugin.getServer().getPluginManager().getPlugin(clickedItem.getItemMeta().getDisplayName()).getDescription().getCommands().size() == 0) {
	        		pluginmanager2.setItem(2, createGuiItem(Material.COMMAND_BLOCK, 1, "§aCommands", "§aShow all Commands for the Plugin!", "", "§cNo Commands registered!"));
	        	} else {
	        		pluginmanager2.setItem(2, createGuiItem(Material.COMMAND_BLOCK, 1, "§aCommands", "§aShow all Commands for the Plugin!", "", "§cCommands of the Plugin: " + plugin.getServer().getPluginManager().getPlugin(clickedItem.getItemMeta().getDisplayName()).getDescription().getCommands().size()));
	        	}
	        	if(plugin.getServer().getPluginManager().getPlugin(clickedItem.getItemMeta().getDisplayName()).getDescription().getPermissions().size() == 0) {
	        		pluginmanager2.setItem(3, createGuiItem(Material.BOOK, 1, "§aPermissions", "§aShow all Permissions for the Plugin!", "§cMax Permissions that are listed: 90", "", "§cNo Permissions registered!"));
	        	} else {
	        		pluginmanager2.setItem(3, createGuiItem(Material.BOOK, 1, "§aPermissions", "§aShow all Permissions for the Plugin!", "", "§aMax Permissions that are listed: 135", "", "§cPermissions of the Plugin: " + plugin.getServer().getPluginManager().getPlugin(clickedItem.getItemMeta().getDisplayName()).getDescription().getPermissions().size()));
	        	}
	        	pluginmanager2.setItem(8, createGuiItem(Material.BARRIER, 1, "§cBack", "§cGo back to the Plugin Menu!"));
	        	PluginStopGUI.openPluginMenu(p);
	        } else {
				p.sendMessage(nopermissionmessage);
			}
        }
        else if (clickedItem.getType() == Material.LIME_WOOL && clickedItem.getItemMeta().getDisplayName().equals("§aEnabled")) {
        	if(p.hasPermission("AdminPanel.PluginManager.PluginSettings.Disable")) {
	        	if(e.getRawSlot() == 0) {
	        		Plugin pluginToDisable = plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle());
					PluginDisableEvent disableEvent = new PluginDisableEvent(p, pluginToDisable);
					Bukkit.getPluginManager().callEvent(disableEvent);
					if(!disableEvent.isCancelled()) {
						assert pluginToDisable != null;
						Bukkit.getPluginManager().disablePlugin(pluginToDisable);
						pluginmanager2.setItem(0, createGuiItem(Material.RED_WOOL, 1, "§cDisabled"));
						for(int i = 0; i < plugin.getServer().getPluginManager().getPlugins().length; i++) {
							ItemStack pluginitemred = new ItemStack(Material.RED_WOOL);
							ItemMeta meta1 = pluginitemred.getItemMeta();
							meta1.setDisplayName(p.getOpenInventory().getTitle());
							pluginitemred.setItemMeta(meta1);
							if(inv2.getItem(i).getItemMeta().getDisplayName().equals(pluginitemred.getItemMeta().getDisplayName()) && inv2.getItem(i).getType().equals(pluginitemred.getType())) {
								inv2.setItem(i, createGuiItem(Material.RED_WOOL, 1, p.getOpenInventory().getTitle(), "§2Name: §6" + pluginToDisable.getDescription().getName(), "§2Version: §6" + pluginToDisable.getDescription().getVersion(), "§2Author(s): §6" + pluginToDisable.getDescription().getAuthors()));
							}
						}
						p.sendMessage("§aPlugin §c" + p.getOpenInventory().getTitle() + "§a successfully disabled!");
						p.closeInventory();
						PluginStopGUI.openInv(p);
					}
	        	}
        	} else {
				p.sendMessage(nopermissionmessage);
			}
        }
        else if (clickedItem.getType() == Material.RED_WOOL && clickedItem.getItemMeta().getDisplayName().equals("§cDisabled")) {
        	if(p.hasPermission("AdminPanel.PluginManager.PluginSettings.Enable")) {
	        	if(e.getRawSlot() == 0) {
	        		Plugin pluginToEnable = plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle());
					PluginEnableEvent enableEvent = new PluginEnableEvent(p, pluginToEnable);
					Bukkit.getPluginManager().callEvent(enableEvent);
					if(!enableEvent.isCancelled()) {
						assert pluginToEnable != null;
						Bukkit.getPluginManager().enablePlugin(pluginToEnable);
						pluginmanager2.setItem(0, createGuiItem(Material.LIME_WOOL, 1, "§aEnabled"));
						for(int i = 0; i < plugin.getServer().getPluginManager().getPlugins().length; i++) {
							ItemStack pluginitemlime = new ItemStack(Material.LIME_WOOL);
							ItemMeta meta1 = pluginitemlime.getItemMeta();
							meta1.setDisplayName(p.getOpenInventory().getTitle());
							pluginitemlime.setItemMeta(meta1);
							if(inv2.getItem(i).getItemMeta().getDisplayName().equals(pluginitemlime.getItemMeta().getDisplayName()) && inv2.getItem(i).getType().equals(pluginitemlime.getType())) {
								inv2.setItem(i, createGuiItem(Material.LIME_WOOL, 1, p.getOpenInventory().getTitle(), "§2Name: §6" + pluginToEnable.getDescription().getName(), "§2Version: §6" + pluginToEnable.getDescription().getVersion(), "§2Author(s): §6" + pluginToEnable.getDescription().getAuthors()));
							}
						}
						p.sendMessage("§aPlugin §c" + p.getOpenInventory().getTitle() + "§a successfully enabled!");
						p.closeInventory();
						PluginStopGUI.openInv(p);
					}
	        	}
        	} else {
				p.sendMessage(nopermissionmessage);
			}
        }
        else if(clickedItem.getType() == Material.HOPPER && clickedItem.getItemMeta().getDisplayName().equals("§aRestart")) {
        	Plugin pluginToRestart = plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle());
        	if(p.hasPermission("AdminPanel.PluginManager.PluginSettings.Restart")) {
				PluginRestartEvent restartEvent = new PluginRestartEvent(p, pluginToRestart);
				Bukkit.getPluginManager().callEvent(restartEvent);
				if(!restartEvent.isCancelled()) {
					assert pluginToRestart != null;
					Bukkit.getPluginManager().disablePlugin(pluginToRestart);
					Bukkit.getPluginManager().enablePlugin(pluginToRestart);
					p.sendMessage("§aSuccessfully restart §c" + pluginToRestart);
					p.closeInventory();
					PluginStopGUI.openInv(p);
				}
        	} else {
				p.sendMessage(nopermissionmessage);
			}
        }
        else if(clickedItem.getType() == Material.COMMAND_BLOCK && clickedItem.getItemMeta().getDisplayName().equals("§aCommands")) {
        	if(p.hasPermission("AdminPanel.PluginManager.PluginSettings.Commands")) {
        		if(plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle()).getDescription().getCommands().size() > 0) {
		        	p.sendMessage("§6Commands for §2" + plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).toString() + "§6:");
			        for(int i = 0; i < plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle()).getDescription().getCommands().size(); i++) {
				        p.sendMessage("§5-----------------------------------------------------");
				        p.sendMessage("§6- §2" + ((Command) plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle()).getDescription().getCommands().get(i)).getName().toString() + ":");
				        p.sendMessage("§2Description: §6" + ((Command) plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getCommands().get(i)).getDescription().toString() + ",");
				        p.sendMessage("§2Permission: §6" + ((Command) plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getCommands().get(i)).getPermission().toString() + ",");
				        p.sendMessage("§2Message: §6" + ((Command) plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getCommands().get(i)).getPermissionMessage().toString() + ",");
				        p.sendMessage("§2Usage: §6" + ((Command) plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getCommands().get(i)).getUsage().toString() + ",");
				        p.sendMessage("§2Aliases: §6" + ((Command) plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getCommands().get(i)).getAliases().toString());
			        }
			        p.sendMessage("§cENDE");
		        	p.closeInventory();
        		} else {
        			p.sendMessage("§cDas Plugin hat keine Commands registiert!");
        		}
        	} else {
				p.sendMessage(nopermissionmessage);
			}
        }
        else if(clickedItem.getType() == Material.BOOK && clickedItem.getItemMeta().getDisplayName().equals("§aPermissions")) {
        	if(p.hasPermission("AdminPanel.PluginManager.PluginSettings.Permissions")) {
        		if(plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle()).getDescription().getPermissions().size() > 0) {
        			permissionspage1_2 = Bukkit.createInventory(null, 54, plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle()).toString());
        			permissionspage2_2 = Bukkit.createInventory(null, 54, plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle()).toString());
        			permissionspage3_2 = Bukkit.createInventory(null, 54, plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle()).toString());
			        for(int i = 0; i < plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle()).getDescription().getPermissions().size(); i++) {
			        	if(i <= 44) {
			        		permissionspage1_2.setItem(i, createGuiItem(Material.BOOK, i+1, plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getPermissions().get(i).getName().toString(), "§6Description: §2" + plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getPermissions().get(i).getDescription().toString(), "§6Childrens: §2" + plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getPermissions().get(i).getChildren().toString(), "§6Default: §2" + plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getPermissions().get(i).getDefault().toString()));
			        	} else if (i >= 44) {
			        		permissionspage2_2.setItem((i-45), createGuiItem(Material.BOOK, (i-44), plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getPermissions().get(i).getName().toString(), "§6Description: §2" + plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getPermissions().get(i).getDescription().toString(), "§6Childrens: §2" + plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getPermissions().get(i).getChildren().toString(), "§6Default: §2" + plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getPermissions().get(i).getDefault().toString()));
			        	}
			        	if (i >= 90) {
			        		permissionspage3_2.setItem((i-90), createGuiItem(Material.BOOK, (i-89), plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getPermissions().get(i).getName().toString(), "§6Description: §2" + plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getPermissions().get(i).getDescription().toString(), "§6Childrens: §2" + plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getPermissions().get(i).getChildren().toString(), "§6Default: §2" + plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle().toString()).getDescription().getPermissions().get(i).getDefault().toString()));
			        	}
			        }
					for(int i = 45; i < inv2.getSize() ; i++) {
						permissionspage1_2.setItem(i, createGuiItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1, " "));
						permissionspage2_2.setItem(i, createGuiItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1, " "));
						permissionspage3_2.setItem(i, createGuiItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1, " "));
					}
			        permissionspage1_2.setItem(49, createGuiItem(Material.BARRIER, 1, "§cBack", "§cClick to go back to the Plugin Menu"));
			        permissionspage2_2.setItem(49, createGuiItem(Material.BARRIER, 1, "§cBack", "§cClick to go back to the Plugin Menu"));
			        permissionspage3_2.setItem(49, createGuiItem(Material.BARRIER, 1, "§cBack", "§cClick to go back to the Plugin Menu"));
			        if(plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle()).getDescription().getPermissions().size() > 45) {
				        permissionspage1_2.setItem(50, createGuiItem(Material.ARROW, 1, "§cPage forward!", "Click to go a Page forward"));
				        permissionspage2_2.setItem(48, createGuiItem(Material.ARROW, 1, "§cPage back!", "Click to go a Page forward"));
				        if(plugin.getServer().getPluginManager().getPlugin(p.getOpenInventory().getTitle()).getDescription().getPermissions().size() > 90) {
					        permissionspage3_2.setItem(48, createGuiItem(Material.ARROW, 1, "§cPage back", "Click to go a Page forward"));
					        permissionspage2_2.setItem(50, createGuiItem(Material.ARROW, 1, "§cPage forward", "Click to go a Page forward"));
				        } else {
				        	
				        }
			        } else {
				        permissionspage2_2.setItem(48, createGuiItem(Material.ARROW, 1, "§cPage back!", "Click to go a Page forward"));
			        }
		        	p.closeInventory();
			        PluginStopGUI.openPermissionsPage1(p);
        		} else {
        			p.sendMessage("§cDas Plugin hat keine Permissions registiert!");
        		}
        	} else {
				p.sendMessage(nopermissionmessage);
			}
        }
        else if(clickedItem.getType() == Material.BARRIER) {
        	if(e.getRawSlot() != 8) {
        		if(clickedItem.getItemMeta().getDisplayName().equals("§cBack")) {
        			PluginStopGUI.openInv(p);
        			return;
        		}
        		ExampleGui.openInv(p);
        	} else {
        		PluginStopGUI.openInv(p);
        	}
        }
        else if (clickedItem.getType() == Material.ARROW) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§cPage forward")) {
        		if(p.hasPermission("AdminPanel.Button.PageForward")) {
        			PluginStopGUI.openPermissionsPage3(p);
        		} else {
					p.sendMessage(nopermissionmessage);
				}
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§cPage forward!")) {
        		if(p.hasPermission("AdminPanel.Button.PageForward")) {
        			PluginStopGUI.openPermissionsPage2(p);
        		} else {
					p.sendMessage(nopermissionmessage);
				}
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§cPage back")) {
        		if(p.hasPermission("AdminPanel.Button.PageForward")) {
        			PluginStopGUI.openPermissionsPage2(p);
        		} else {
					p.sendMessage(nopermissionmessage);
				}
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§cPage back!")) {
        		if(p.hasPermission("AdminPanel.Button.PageBack")) {
        			PluginStopGUI.openPermissionsPage1(p);
        		} else {
					p.sendMessage(nopermissionmessage);
				}
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aPage forward")) {
        		if(p.getOpenInventory().getTitle().equals("§4§lPluginManager: §rPage 1") && plugin.getServer().getPluginManager().getPlugins().length > 45) {
            		if(p.hasPermission("AdminPanel.Button.PageForward")) {
            			PluginStopGUI.openPage2(p);
            		} else {
						p.sendMessage(nopermissionmessage);
					}
        		} else {
        			if(!p.getOpenInventory().getTitle().equals("§4§lPluginManager: §rPage 1")) {
        				p.sendMessage("§cYou can't open the Page you are looking at!");
        				return;
        			}
        			if(plugin.getServer().getPluginManager().getPlugins().length <= 45) {
        				p.sendMessage("§cEs sind nicht mehr Plugins vorhanden!");
        				return;
        			}
        		}
        	} else if(clickedItem.getItemMeta().getDisplayName().equals("§aPage back")) {
            	if(p.getOpenInventory().getTitle().equals("§4§lPluginManager: §rPage 2")) {
            		if(p.hasPermission("AdminPanel.Button.PageBack")) {
            			PluginStopGUI.openInv(p);
            		} else {
						p.sendMessage(nopermissionmessage);
					}
            	} else {
            		p.sendMessage("§cYou can't open the Page you are looking at!");
            	}
        	}
        }
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
        if (invs.contains(e.getInventory()) && page2invs.contains(e.getInventory()) && pluginmanagerinvs.contains(e.getInventory()) && permissionspage1invs.contains(e.getInventory()) && permissionspage2invs.contains(e.getInventory()) && permissionspage3invs.contains(e.getInventory())) {
          e.setCancelled(true);
        }
    }
}