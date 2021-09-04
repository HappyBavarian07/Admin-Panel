package de.happybavarian07.utils;

import de.happybavarian07.main.Main;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {

	private final FileConfiguration config;
	private final Main plugin;
	private static Utils instance;

	public Utils(Main main) {
		this.plugin = main;
		setInstance(this);
		this.config = plugin.getConfig();
	}

	public String chat(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	public ItemStack createItem(Inventory inv, String materialString, int amount, int invSlot, String displayName, String... loreString) {
		
		ItemStack item;
		List<String> lore = new ArrayList<>();
		
		item = new ItemStack(Material.matchMaterial(materialString), amount);
		
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(this.chat(displayName));
		for(String s : loreString) {
			lore.add(this.chat(s));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		inv.setItem(invSlot - 1, item);
		return item;
	}

	@SuppressWarnings("deprecation")
	public ItemStack createItemByte(Inventory inv, String materialString, int byteId, int amount, int invSlot, String displayName, String... loreString) {
		
		ItemStack item;
		List<String> lore = new ArrayList<>();
		
		item = new ItemStack(Material.matchMaterial(materialString), amount, (short) byteId);
		
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(this.chat(displayName));
		for(String s : loreString) {
			lore.add(this.chat(s));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		inv.setItem(invSlot - 1, item);
		return item;
	}

	@SuppressWarnings({"deprecation" })
	public void ban(final Player p, final String target, final String reason, final String sourcename) {
		try {
			OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(target);
			if(bannedPlayer.isBanned()) {
				p.sendMessage("§cThe Player §a" + bannedPlayer.getName() + "§c is already banned!");
			} else {
				if(sourcename.equals("")) {
					Bukkit.getBanList(Type.NAME).addBan(bannedPlayer.getName(), reason, null, p.getName());
					if(bannedPlayer.isOnline()) {
						((Player) bannedPlayer).kickPlayer("§cYou got banned from that Server!\n" +
						"\n" + 
						"§3By: §e" + Bukkit.getBanList(Type.NAME).getBanEntry(bannedPlayer.getName()).getSource().toString() + "\n" +
						"\n" + 
						"§3Reason: §e" + Bukkit.getBanList(Type.NAME).getBanEntry(bannedPlayer.getName()).getReason().toString() + "\n" + 
						"\n" + 
						"§3Permanently banned!" + "\n");
					}
				} else if(sourcename != "") {
					Bukkit.getBanList(Type.NAME).addBan(bannedPlayer.getName(), reason, null, sourcename);
					if(bannedPlayer.isOnline()) {
						((Player) bannedPlayer).kickPlayer("§cYou got banned from that Server!\n" +
						"\n" + 
						"§3By: §e" + sourcename + "\n" +
						"\n" + 
						"§3Reason: §e" + Bukkit.getBanList(Type.NAME).getBanEntry(bannedPlayer.getName()).getReason().toString() + "\n" + 
						"\n" + 
						"§3Permanently banned!" + "\n");
					}
				}
				plugin.getBanConfig().set(bannedPlayer.getUniqueId().toString(), true);
				try {
					plugin.getBanConfig().save(plugin.getBanFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
				p.sendMessage("§c§cYou have successfully banned §a" + bannedPlayer.getName() + " §cfor §a" + Bukkit.getBanList(Type.NAME).getBanEntry(bannedPlayer.getName()).getReason().toString() + "§c!");
			}
		} catch (NullPointerException e) {
			p.sendMessage("§cThe Player is not online or doesn't exists!");
		}
	}

	@SuppressWarnings({"deprecation"})
	public void unban(Player Player, String target) {
		try {
			OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(target);
			if(!bannedPlayer.isBanned()) {
				Player.sendMessage("§cThe Player §a" + bannedPlayer.getName() + "§c is not banned!");
				return;
			}
			if(bannedPlayer.isBanned()) {
				Bukkit.getBanList(Type.NAME).pardon(bannedPlayer.getName());
				Player.sendMessage("§cYou have successfully unbanned §a" + bannedPlayer.getName() + "§c!");
				plugin.getBanConfig().set(bannedPlayer.getUniqueId().toString(), null);
				try {
					plugin.getBanConfig().save(plugin.getBanFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (NullPointerException e) {
			Player.sendMessage("§cThe Player is not online or doesn't exists!");
		}
	}

	public void kick(final Player p, final String target, final String reason, final String sourcename) {
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
			p.sendMessage("§cThe Player is not online or doesn't exists!");
		}
	}

	public void serverStop(int time, int time2) throws InterruptedException {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.closeInventory();
		}
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Bukkit.broadcastMessage("§r[§4§lWARNING§r] " + "§c§lThe server will now shut down and all players will be kicked!");
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Thread.sleep(time);
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§aServerstop in: §c§l6");
		Thread.sleep(time);
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§aServerstop in: §c§l5");
		Thread.sleep(time);
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§6Serverstop in: §c§l4");
		Thread.sleep(time);
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§6Serverstop in: §c§l3");
		Thread.sleep(time);
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§4Serverstop in: §c§l2");
		Thread.sleep(time);
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§4Serverstop in: §c§l1");
		Thread.sleep(time);
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Bukkit.broadcastMessage("§r[§4§lWARNING§r] " + "§c§lServer Stop initiated!");
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Thread.sleep(time2);
		for(Player p2 : Bukkit.getServer().getOnlinePlayers()) {
			p2.kickPlayer("§4§lThe server is now shuting down!");
		}
		Bukkit.shutdown();
	}

	public void serverReload(int time) throws InterruptedException {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.closeInventory();
		}
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Bukkit.broadcastMessage("§r[§4§lWARNING§r] " + "§c§lThe server is about to reload, please do not move or write in the chat until the reload is finished");
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Thread.sleep(3000);
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§aServerreload in: §c§l6");
		Thread.sleep(time);
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§aServerreload in: §c§l5");
		Thread.sleep(time);
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§6Serverreload in: §c§l4");
		Thread.sleep(time);
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§6Serverreload in: §c§l3");
		Thread.sleep(time);
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§4Serverreload in: §c§l2");
		Thread.sleep(time);
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§4Serverreload in: §c§l1");
		Thread.sleep(time);
		clearChat(100, false, null);
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.addScoreboardTag("reload");
			player.closeInventory();
		}
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Bukkit.broadcastMessage("        §r[§4§lAnnouncement§r] " + "§c§lReload started!");
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Bukkit.reload();
		clearChat(100, false, null);
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Bukkit.broadcastMessage("        §r[§4§lAnnouncement§r] " + "§c§lReload finished!");
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
	}

	public String replacePlaceHolders(Player player, String message, String prefix) {
		return PlaceholderAPI.setPlaceholders(player, ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", prefix)));
	}

	public void clearChat(int lines, boolean broadcastChatClear, Player player) {
		if(!broadcastChatClear) {
			for(int i = 0; i <= lines; i++) {
				Bukkit.getServer().broadcastMessage("");
			}
		} else {
			for(int i = 0; i <= lines; i++) {
				Bukkit.getServer().broadcastMessage("");
			}
			Bukkit.getServer().broadcastMessage(plugin.getLanguageManager().getMessage("Player.Chat.Header", player));
			Bukkit.getServer().broadcastMessage(plugin.getLanguageManager().getMessage("Player.Chat.Message", player));
			Bukkit.getServer().broadcastMessage(plugin.getLanguageManager().getMessage("Player.Chat.Footer", player));
		}
	}

	public static Utils getInstance() { return instance; }

	private void setInstance(Utils instance) { Utils.instance = instance; }

	public Economy getEconomy() {
		return plugin.eco;
	}
	public Permission getPermissions() {
		return plugin.perms;
	}
	public Chat getChat() {
		return plugin.chat;
	}
}
