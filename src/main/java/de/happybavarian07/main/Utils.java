package de.happybavarian07.main;

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

	/**
	 * Replace Alternate Color Codes to the Color Code from the Server
	 * @param s Message to format
	 * @return ColorCode Formatted String
	 */
	public String chat(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	/**
	 * Creates an Item and sets that Item into the Inventory param in the Slot (invSlot - 1).
	 * That means that instead of 0, 1, 2, 3, ... you can use 1, 2, 3, 4, 5, ...
	 * @param inv Inv
	 * @param materialString Material
	 * @param amount Amount
	 * @param invSlot Slot in Inv
	 * @param displayName Display name
	 * @param loreString Lore
	 * @return Item Stack without a Byte
	 */
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

	/**
	 * Creates an Item with a Byte and sets that Item into the Inventory param in the Slot (invSlot - 1).
	 * That means that instead of 0, 1, 2, 3, ... you can use 1, 2, 3, 4, 5, ...
	 * @param inv Inv
	 * @param materialString Material
	 * @param byteId Byte
	 * @param amount Amount
	 * @param invSlot Slot in Inv
	 * @param displayName Display name
	 * @param loreString Lore
	 * @return Item Stack with a Byte
	 */
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

	/**
	 * Bans the Player (target), by Source (p) or if given the Source Name, the Reason (reason).
	 * Pattern:
	 *                                  You got banned from that Server!
	 *
	 *                                              By: Source
	 *
	 *                                            Reason: Reason
	 *
	 *                                         Permanently banned!
	 * @param p sourceplayer
	 * @param target target
	 * @param reason reason
	 * @param sourcename sourcename
	 */
	@SuppressWarnings({"deprecation" })
	public void ban(final Player p, final String target, final String reason, final String sourcename) {
		try {
			OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(target);
			if(bannedPlayer.isBanned()) {
				p.sendMessage("§cThe Player §a" + bannedPlayer.getName() + "§c got already banned!");
			} else {
				if(sourcename == "") {
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
				p.sendMessage("§c§cYou have successfully banned §a" + bannedPlayer.getName() + " §cfor §a" + Bukkit.getBanList(Type.NAME).getBanEntry(bannedPlayer.getName()).getReason().toString() + "§c!");
			}
		} catch (NullPointerException e) {
			p.sendMessage("§cThe Player is not online or doesn't exists!");
		}
	}

	/**
	 * Unban the Player with the Name (target) and if something went wrong the Message,
	 * will be send to the Player (player).
	 * @param Player player
	 * @param target target
	 */
	@SuppressWarnings({"deprecation"})
	public void unban(Player Player, String target) {
		try {
			OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(target);
			if(!bannedPlayer.isBanned()) {
				Player.sendMessage("§cThe Player §a" + bannedPlayer.getName() + "§c is not banned!");
			}
			if(bannedPlayer.isBanned()) {
				Bukkit.getBanList(Type.NAME).pardon(bannedPlayer.getName());
				Player.sendMessage("§cYou have successfully unbanned §a" + bannedPlayer.getName() + "§c!");
			}
		} catch (NullPointerException e) {
			Player.sendMessage("§cThe Player is not online or doesn't exists!");
		}
	}

	/**
	 * Kick a Player (target) with the Source Player (player) or if given the sourcename (sourcename)
	 * Pattern:
	 *                              You got kicked! \n
	 *                                     \n
	 *                               By: sourcename\n
	 *                                     \n
	 *                              Reason: Reason\n
	 *                                     \n
	 *                             Please join again!\n
	 * @param p player
	 * @param target target
	 * @param reason reason
	 * @param sourcename sourcename
	 */
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

	/**
	 * Stops the Server after a specific amount of time and kicks all Players
	 * @param p the Player the that is needed by the ClearChat Method
	 * @param time time between actions (millis)
	 * @param time2 time before all players get kicked (millis)
	 * @throws InterruptedException When the Times to wait are not going well
	 */
	public void serverStop(Player p, int time, int time2) throws InterruptedException {
		List<Player> players = new ArrayList<Player>();
		players.addAll(Bukkit.getOnlinePlayers());
		for(int i = 0; i < players.size(); i++) {
			players.get(i).closeInventory();
		}
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Bukkit.broadcastMessage("§r[§4§lWARNING§r] " + "§c§lThe server will now shut down and all players will be kicked!");
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§aServerstop in: §c§l6");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§aServerstop in: §c§l5");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§6Serverstop in: §c§l4");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§6Serverstop in: §c§l3");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§4Serverstop in: §c§l2");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§4Serverstop in: §c§l1");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Bukkit.broadcastMessage("§r[§4§lWARNING§r] " + "§c§lServer Stop initiated!");
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Thread.sleep(time2);
		for(Player p2 : Bukkit.getServer().getOnlinePlayers()) {
			p2.kickPlayer("§4§lThe server is now shuting down!");
		}
		Bukkit.shutdown();
	}

	/**
	 * Reloads the Server after a specific amount of time
	 *
	 * @param p the Player the that is needed by the ClearChat Method
	 * @param time time between actions (millis)
	 * @throws InterruptedException When the Times to wait are not going well
	 */
	public void serverReload(Player p, int time) throws InterruptedException {
		p.closeInventory();
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Bukkit.broadcastMessage("§r[§4§lWARNING§r] " + "§c§lThe server is about to reload, please do not move or write in the chat until the reload is finished");
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Thread.sleep(3000);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§aServerreload in: §c§l6");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§aServerreload in: §c§l5");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§6Serverreload in: §c§l4");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§6Serverreload in: §c§l3");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§4Serverreload in: §c§l2");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§4Serverreload in: §c§l1");
		Thread.sleep(time);
		clearChat(100, false, p);
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.addScoreboardTag("reload");
			player.closeInventory();
		}
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Bukkit.broadcastMessage("        §r[§4§lAnnouncement§r] " + "§c§lReload started!");
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Bukkit.reload();
		clearChat(100, false, p);
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
		Bukkit.broadcastMessage("        §r[§4§lAnnouncement§r] " + "§c§lReload finished!");
		Bukkit.broadcastMessage("§a+---------------------------------------------------+");
	}

	/**
	 * Replaces all PlaceHolderAPI placeholders (player) and the Prefix from the AdminPanel Plugin
	 * and alternate Color Codes
	 * @param player Player that gets used by PlaceholderAPI
	 * @param message The Message to replace the Things
	 * @param prefix The Prefix to replace %prefix%
	 * @return Formatted String
	 */
	public String replacePlaceHolders(Player player, String message, String prefix) {
		return PlaceholderAPI.setPlaceholders(player, ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", prefix)));
	}

	/**
	 * Clears the Chat for the amount of lines
	 * @param lines amount of lines that should be cleared
	 * @param showplayername should the Playername get broadcasted
	 * @param player Player as the Source
	 */
	public void clearChat(int lines, boolean showplayername, Player player) {
		if(!showplayername) {
			for(int i = 0; i <= lines; i++) {
				Bukkit.getServer().broadcastMessage("");
			}
		} else {
			for(int i = 0; i <= lines; i++) {
				Bukkit.getServer().broadcastMessage("");
			}
			Bukkit.getServer().broadcastMessage(replacePlaceHolders(player, plugin.getMessages().getString("ServerManager.ChatClearHeader"), Main.getPrefix()));
			Bukkit.getServer().broadcastMessage(replacePlaceHolders(player, plugin.getMessages().getString("ServerManager.ChatClearMessage"), Main.getPrefix()));
			Bukkit.getServer().broadcastMessage(replacePlaceHolders(player, plugin.getMessages().getString("ServerManager.ChatClearFooter"), Main.getPrefix()));
		}
	}

	public static Utils getInstance() { return instance; }

	private void setInstance(Utils instance) { this.instance = instance; }

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
