package de.happybavarian07.main;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Utils {
	
	public static String chat(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}
	
	static FileConfiguration cfg;
	
	public static ItemStack createItem(Inventory inv, String materialString, int amount, int invSlot, String displayName, String... loreString) {
		
		ItemStack item;
		List<String> lore = new ArrayList<String>();
		
		item = new ItemStack(Material.matchMaterial(materialString), amount);
		
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(Utils.chat(displayName));
		for(String s : loreString) {
			lore.add(Utils.chat(s));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		inv.setItem(invSlot - 1, item);
		return item;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack createItemByte(Inventory inv, String materialString, int byteId, int amount, int invSlot, String displayName, String... loreString) {
		
		ItemStack item;
		List<String> lore = new ArrayList<String>();
		
		item = new ItemStack(Material.matchMaterial(materialString), amount, (short) byteId);
		
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(Utils.chat(displayName));
		for(String s : loreString) {
			lore.add(Utils.chat(s));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		inv.setItem(invSlot - 1, item);
		return item;
	}
	
	public static ItemStack buildItem(Material material, String displayname, int amount, String... lore) {
		
		ItemStack item;
		List<String> loreArray = new ArrayList<String>();
		
		item = new ItemStack(material, amount);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayname);
		for(String s : lore) {
			loreArray.add(s);
		}
		meta.setLore(loreArray);
		item.setItemMeta(meta);
		return item;
	}
	
	@SuppressWarnings({"deprecation" })
	public static void ban(final Player p, final String target, final String reason, final String sourcename) {
		try {
			OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(target);
			if(bannedPlayer.isBanned()) {
				p.sendMessage("§cDer Spieler §a" + bannedPlayer.getName() + "§c ist bereits gebannt!");
			} else {
				if(sourcename == "") {
					Bukkit.getBanList(Type.NAME).addBan(bannedPlayer.getName(), reason, null, p.getName());
					if(bannedPlayer.isOnline()) {
						((Player) bannedPlayer).kickPlayer("§cDu wurdest vom Server gebannt!\n" + 
						"\n" + 
						"§3Von: §e" + Bukkit.getBanList(Type.NAME).getBanEntry(bannedPlayer.getName()).getSource().toString() + "\n" + 
						"\n" + 
						"§3Reason: §e" + Bukkit.getBanList(Type.NAME).getBanEntry(bannedPlayer.getName()).getReason().toString() + "\n" + 
						"\n" + 
						"§3Permanently banned!" + "\n" + 
						"\n" + 
						"§3Du kannst §c§nkeinen§3 Entbannungsantrag stellen!");
					}
				} else if(sourcename != "") {
					Bukkit.getBanList(Type.NAME).addBan(bannedPlayer.getName(), reason, null, sourcename);
					if(bannedPlayer.isOnline()) {
						((Player) bannedPlayer).kickPlayer("§cDu wurdest vom Server gebannt!\n" + 
						"\n" + 
						"§3Von: §e" + sourcename + "\n" + 
						"\n" + 
						"§3Reason: §e" + Bukkit.getBanList(Type.NAME).getBanEntry(bannedPlayer.getName()).getReason().toString() + "\n" + 
						"\n" + 
						"§3Permanently banned!" + "\n" + 
						"\n" + 
						"§3Du kannst §c§nkeinen§3 Entbannungsantrag stellen!");
					}
				}
				p.sendMessage("§cDu hast erfolgreich §a" + bannedPlayer.getName() + " §cf§r §a" + Bukkit.getBanList(Type.NAME).getBanEntry(bannedPlayer.getName()).getReason().toString() + "§cgebannt!");
			}
		} catch (NullPointerException e) {
			p.sendMessage("§cDer Spieler ist nicht Online oder existiert nicht!");
		}
	}
	
	@SuppressWarnings({"deprecation"})
	public static void unban(Player Player, String target) {
		try {
			OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(target);
			if(!bannedPlayer.isBanned()) {
				Player.sendMessage("§cDer Spieler §a" + bannedPlayer.getName() + "§c ist nicht gebannt!");
			}
			if(bannedPlayer.isBanned()) {
				Bukkit.getBanList(Type.NAME).pardon(bannedPlayer.getName());
				Player.sendMessage("§cDu hast erfolgreich §a" + bannedPlayer.getName() + "§c entgebannt!");
			}
		} catch (NullPointerException e) {
			Player.sendMessage("§cDer Spieler ist nicht Online oder existiert nicht!");
		}
	}
	
	public static void kick(final Player p, final String target, final String reason, final String sourcename) {
		try {
			Player kickedPlayer = Bukkit.getPlayerExact(target);
			if(kickedPlayer.isOnline()) {
				if(sourcename != "") {
					kickedPlayer.kickPlayer("§cYou got kicked!\n" + 
					"\n" + 
					"§3By: §e" + sourcename + "\n" + 
					"\n" + 
					"§3Reason: §e" + reason + "\n" + 
					"\n" + 
					"§3Please join again!");
				} else if(sourcename == "") {
					kickedPlayer.kickPlayer("§cYou got kicked!\n" + 
					"\n" + 
					"§3By: §e" + p.getName() + "\n" + 
					"\n" + 
					"§3Reason: §e" + reason + "\n" + 
					"\n" + 
					"§3Please join again!");
				}
			}
		} catch (NullPointerException e) {
			p.sendMessage("§cDer Spieler ist nicht Online oder existiert nicht!");
		}
	}
}
