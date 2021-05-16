package de.happybavarian07.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import de.happybavarian07.api.StartUpLogger;
import de.happybavarian07.gui.ExampleGui;
import de.happybavarian07.gui.PlayerManagerGUI;
import de.happybavarian07.gui.PluginStopGUI;
import de.happybavarian07.gui.ServerManagment;
import de.happybavarian07.gui.Time_Weather_Changer_GUi;
import de.happybavarian07.gui.TrollGUI;
import de.happybavarian07.gui.WorldManagment;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin implements Listener {
	
	public static Economy eco;
	File configFile = new File(this.getDataFolder(), "config.yml");
	
	static File messagesfile = new File("plugins/Admin-Panel", "messages.yml");
	static FileConfiguration messages = YamlConfiguration.loadConfiguration(messagesfile);
	
	static File trollguiitemsfile = new File("plugins/Admin-Panel/Items", "TrollGUI.yml");
	static FileConfiguration trollitems = YamlConfiguration.loadConfiguration(trollguiitemsfile);
	
	static File banfile = new File("plugins/Admin-Panel", "bans.yml");
	FileConfiguration bans = YamlConfiguration.loadConfiguration(banfile);
	
	public static FileConfiguration getMessages() {
		return messages;
	}

	public static void setMessages(FileConfiguration messages) {
		Main.messages = messages;
	}

	public static String prefix;
	
	public static String getPrefix() {
		return prefix;
	}

	public static void setPrefix(String prefix) {
		Main.prefix = prefix;
	}

	public static Main plugin;
	@Override
	public void onEnable() {
		ConsoleCommandSender ccs = Bukkit.getConsoleSender();
		final StartUpLogger logger = StartUpLogger.create();
		
		logger
			.coloredSpacer(ChatColor.GREEN)
				.messages(
						"�e�lStarting Admin Panel Plugin:�r"
                );
		logger
		.coloredSpacer(ChatColor.DARK_RED)
			.messages(
					"�c�lStarting Vault initialization!�r"
            );
		if(!setupEconomy()) {
			logger
				.spacer()
					.coloredMessage(ChatColor.RED, "")
					.coloredMessage(ChatColor.RED, "No Vault found please install Vault before starting again!")
					.coloredMessage(ChatColor.RED, "and you must have an Economy Plugin installed!")
					.coloredMessage(ChatColor.RED, "");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		logger
				.messages(
						"�c�lFinished Vault initialization!�r"
				);
		if(!configFile.exists()) {
			logger.coloredSpacer(ChatColor.DARK_RED).message("�c�lCreating Default Config!�r");
		}
		saveDefaultConfig();
		if(!messagesfile.exists()) {
			logger
			.spacer()
				.messages(
						"�c�lCreating messages.yml file!�r",
						"�2�lAdding Default Messages!�r"
				);
			try {
				messagesfile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			loadMessages();
			logger.message("�e�lDone!�r");
		}
//		ccs.sendMessage("|---------�c�lCreating Item Configuration files if not exists!�r---------|");
//		ccs.sendMessage("|-----------------------�c�lLoading Troll Items!�r-----------------------|");
//		loadTrollItems();
//		ccs.sendMessage("|-----------------------------�e�lDone!�r--------------------------------|");
		if(!banfile.exists()) {
			logger.spacer().message("�c�lCreating bans.yml file!�r");
			try {
				banfile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.message("�e�lDone!�r");
		}
		logger.coloredSpacer(ChatColor.DARK_RED).message("�4�lInitialize Plugin Main Variable to this!�r");
		setPlugin(this);
		logger.message("�e�lVariable Done!�r");
		logger.message("�3�lMain.Prefix �9= �7Config.Plugin.Prefix�r");
		setPrefix(getConfig().getString("Plugin.Prefix").replace('&', '�'));
		logger.message("�e�lPrefix Done!�r");
		logger.coloredSpacer(ChatColor.DARK_RED).message("�2�lStarting Registration of Events:�r");
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new ExampleGui(this, messages, this.getConfig()), this);
		logger.message("�3�lLoading AdminPanel Main GUI Events!�r");
		pm.registerEvents(new PlayerManagerGUI(this, messages, this.getConfig(), bans, banfile), this);
		logger.message("�3�lLoading Player Manager GUI Events!�r");
		pm.registerEvents(new PluginStopGUI(this, messages, this.getConfig()), this);
		logger.message("�3�lLoading Plugin Manager GUI Events!�r");
		pm.registerEvents(new WorldManagment(this, messages, this.getConfig()), this);
		logger.message("�3�lLoading Time/Weather Changer GUI Events!�r");
		pm.registerEvents(new Time_Weather_Changer_GUi(this, messages, this.getConfig()), this);
		logger.message("�3�lLoading Player Troll GUI Events!�r");
		pm.registerEvents(new TrollGUI(this, messages, this.getConfig()), this);
		logger.message("�3�lLoading Server Manager GUI Events!�r");
		pm.registerEvents(new ServerManagment(this, messages, this.getConfig()), this);
		logger.message("�4�lEventregistration: Done!�r");
		logger.coloredSpacer(ChatColor.DARK_RED).message("�e�lStarting Done!�r");
		pm.registerEvents(this, this);
		logger.coloredSpacer(ChatColor.GREEN);
		ccs.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("Plugin.EnablingMessage").replace("%startprefix%", getConfig().getString("Plugin.StartPrefix"))));
	}
	
//	private void loadTrollItems(ConsoleCommandSender ccs) {
//		ccs.sendMessage("TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST");
//		ccs.sendMessage("TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST12121");
//	}
//	
	private void loadMessages() {
		messages.addDefault("Plugin.EnablingMessage", "%startprefix% successfully enabled!");
		messages.addDefault("Plugin.DisablingMessage", "%startprefix% successfully disabled!");
		messages.addDefault("No-Permission-Message", "&cYou don't have access to this Command!");
		messages.addDefault("ConsoleExecutesPlayerCommand", "&cYou have to be a player!");
		messages.addDefault("OpeningMessageSelf", "&cYou have opened the Admin Panel for you!");
		messages.addDefault("OpeningMessageSelfOpenedForOther", "&cYou have opened the Admin Panel for %targetplayer%!");
		messages.addDefault("OpeningMessageOther", "&c%player% has openend for you the Admin Panel!");
		messages.addDefault("TargetedPlayerIsNull", "&cThis player is not online or does not exist!");
		messages.addDefault("PlayerManager.SelfBanningMessage", "&cYou cannot Ban yourself!");
		messages.addDefault("PlayerManager.KickReason", "&cKick for Safety!");
		messages.addDefault("PlayerManager.KickSourceMessage", "%player%");
		messages.addDefault("PlayerManager.BanReason", "&cBan for Safety!");
		messages.addDefault("PlayerManager.BanSourceMessage", "%player%");
		messages.addDefault("TomanyArguments", "&4There are to many Arguments!");
		messages.addDefault("ServerManager.KickAllPlayersReason", "�cKick for Safety");
		messages.addDefault("ServerManager.KickAllPlayersSource", "%player%");
		messages.addDefault("ServerManager.ChatClearHeader", "&a+---------------------------------------------------+");
		messages.addDefault("ServerManager.ChatClearMessage", "    &2The Chat has been cleared by &3%player%");
		messages.addDefault("ServerManager.ChatClearFooter", "&a+---------------------------------------------------+");
		messages.addDefault("ServerManager.MaintenanceMode", "&5%targetplayer% &cThe Server entered the &4&lMaintenance Mode!");
		messages.addDefault("Pman.Money.NotEnoughMoneyToTake", "%targetplayer% has not enoug Money (%balance%)");
		messages.addDefault("ChatMute.PlayerMessage", "&4The Chat is muted try again later %player%!");
		messages.addDefault("ChatMute.Broadcastheader", "&a+---------------------------------------------------+");
		messages.addDefault("ChatMute.Broadcast", "       &4The Chat has been muted by %player%!");
		messages.addDefault("ChatMute.Broadcastfooter", "&a+---------------------------------------------------+");
		messages.addDefault("ChatUnMute.Broadcastheader", "&a+---------------------------------------------------+");
		messages.addDefault("ChatUnMute.Broadcast", "       &4The Chat has been unmuted by %player%!");
		messages.addDefault("ChatUnMute.Broadcastfooter", "&a+---------------------------------------------------+");
		messages.addDefault("TrollGui.Messages.Test", "Test");
		messages.options().copyDefaults(true);
		try {
			messages.save(messagesfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String isProtocolVanishEnabled() {
		if(plugin.getServer().getPluginManager().isPluginEnabled("ProtocolVanish")) {
			return "ProtocolVanish";
		} else if(plugin.getServer().getPluginManager().isPluginEnabled("SuperVanish-6.2.4")) {
			return "SuperVanish-6.2.4";
		} else {
			return null;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		if(ServerManagment.isMaintenance_mode() == true) {
			e.disallow(null, messages.getString("ServerManager.MaintenanceMode").replace('&', '�').replace("%targetplayer%", e.getPlayer().getName()));
		}
		if(p.isBanned()) {
			e.setKickMessage("�cDu wurdest vom Server gebannt!\n" + 
							"\n" + 
							"�3Von: �e" + Bukkit.getBanList(Type.NAME).getBanEntry(p.getName()).getSource().toString() + "\n" + 
							"\n" + 
							"�3Reason: �e" + Bukkit.getBanList(Type.NAME).getBanEntry(p.getName()).getReason().toString() + "\n" + 
							"\n" + 
							"�3Permanently banned!" + "\n" + 
							"\n" + 
							"�3Du kannst �c�nkeinen�3 Entbannungsantrag stellen!");
		}
	}
	
	@Override
	public void onDisable() {
		if(messagesfile.exists()) {
			Bukkit.getConsoleSender().sendMessage(messages.getString("Plugin.DisablingMessage").replace('&', '�').replace("%startprefix%", getConfig().getString("Plugin.StartPrefix").replace('&', '�')));
		} else {
			Bukkit.getConsoleSender().sendMessage("[Admin-Panel] disabled!");
		}
	}
	
	
	
	
	
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economy = getServer().
				getServicesManager().getRegistration(
						net.milkbowl.vault.economy.Economy.class);
		if(economy != null)
			eco = economy.getProvider();
		return (eco != null);
	}
	
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("adminpanel") || cmd.getName().equalsIgnoreCase("apanel") || cmd.getName().equalsIgnoreCase("adminp") || cmd.getName().equalsIgnoreCase("ap")) {
			if(args.length == 0) {
				if(s instanceof Player) {
					Player p = (Player) s;
					if(p.hasPermission("AdminPanel.open")) {
						p.sendMessage(messages.getString("OpeningMessageSelf").replace('&', '�').replace("%prefix%", getConfig().getString("Plugin.Prefix")).replace("%player%", p.getName()));
						ExampleGui.openInv(p);
						if(getConfig().getBoolean("Panel.PlaySoundsWhenOponed") == true) {
							if(getConfig().getString("Panel.SoundWhenOpened") != null) {
								String sound = getConfig().getString("Panel.SoundWhenOpened");
								p.playSound(p.getLocation(), Sound.valueOf(sound), (float) getConfig().getDouble("Panel.SoundVolume"), (float) getConfig().getDouble("Panel.SoundPitch"));
							}
						}
					} else {
						p.sendMessage(messages.getString("No-Permission-Message").replace('&', '�').replace("%permission%", "AdminPanel.open").replace("%prefix%", getConfig().getString("Plugin.Prefix").replace('&', '�')));
					}
				} else {
					s.sendMessage(messages.getString("ConsoleExecutesPlayerCommand").replace('&', '�').replace("%prefix%", getConfig().getString("Plugin.Prefix").replace('&', '�')));
				}
			} else if(args.length == 1) {
				if(s instanceof Player) {
					Player p = (Player) s;
					Player target = Bukkit.getPlayerExact(args[0]);
					if(p.hasPermission("AdminPanel.open.other")) {
						if(messages.getString("OpeningMessageSelfOpenedForOther") != "" || messages.getString("OpeningMessageSelfOpenedForOther") != null) {
							try {
								ExampleGui.openInv(target);
								p.sendMessage(messages.getString("OpeningMessageSelfOpenedForOther").replace('&', '�').replace("%prefix%", getConfig().getString("Plugin.Prefix")).replace("%player%", p.getName()).replace("%targetplayer%", args[0]));
								target.sendMessage(messages.getString("OpeningMessageOther").replace('&', '�').replace("%prefix%", getConfig().getString("Plugin.Prefix")).replace("%player%", p.getName()).replace("%targetplayer%", target.getName()).replace("", ""));
							} catch (NullPointerException e) {
								p.sendMessage(messages.getString("TargetedPlayerIsNull").replace('&', '�').replace("%prefix%", getConfig().getString("Plugin.Prefix")).replace("%player%", p.getName()).replace("%targetplayer%", args[0]).replace("", ""));
							}
						} else {
							try {
								ExampleGui.openInv(target);
								p.sendMessage(messages.getString("OpeningMessageSelfOpenedForOther").replace('&', '�').replace("%prefix%", getConfig().getString("Plugin.Prefix")).replace("%player%", p.getName()).replace("%targetplayer%", args[0]));
							} catch (NullPointerException e) {
								p.sendMessage(messages.getString("TargetedPlayerIsNull").replace('&', '�').replace("%prefix%", getConfig().getString("Plugin.Prefix")).replace("%player%", p.getName()).replace("%targetplayer%", target.getName()).replace("", ""));
							}
						}
					} else {
						p.sendMessage(messages.getString("No-Permission-Message").replace('&', '�').replace("%permission%", "AdminPanel.open").replace("%prefix%", getConfig().getString("Plugin.Prefix").replace('&', '�')));
					}
				}
				if(s instanceof ConsoleCommandSender) {
					ConsoleCommandSender console = (ConsoleCommandSender) s;
					Player target = Bukkit.getPlayerExact(args[0]);
					if(messages.getString("OpeningMessageSelfOpenedForOther") != "" || messages.getString("OpeningMessageSelfOpenedForOther") != null) {
						try {
							ExampleGui.openInv(target);
							console.sendMessage(messages.getString("OpeningMessageSelfOpenedForOther").replace('&', '�').replace("%prefix%", getConfig().getString("Plugin.Prefix")).replace("%player%", console.getName()).replace("%targetplayer%", target.getName()).replace("", ""));
						} catch (NullPointerException e) {
							console.sendMessage(messages.getString("TargetedPlayerIsNull").replace('&', '�').replace("%prefix%", getConfig().getString("Plugin.Prefix")).replace("%player%", console.getName()).replace("%targetplayer%", args[0]).replace("", ""));
						}
					} else {
						try {
							ExampleGui.openInv(target);
						} catch (NullPointerException e) {
							console.sendMessage(messages.getString("TargetedPlayerIsNull").replace('&', '�').replace("%prefix%", getConfig().getString("Plugin.Prefix")).replace("%player%", console.getName()).replace("%targetplayer%", args[0]).replace("", ""));
						}
					}
				}
			} else {
				s.sendMessage(messages.getString("TomanyArguments").replace('&', '�').replace("%prefix%", getConfig().getString("Plugin.Prefix")).replace("%player%", s.getName()));
			}
		}
		return true;
	}

	public static void serverReload(Player p, int time) throws InterruptedException {
		p.closeInventory();
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�a+---------------------------------------------------+");
		Bukkit.broadcastMessage("�r[�4�lWARNING�r] " + "�c�lThe server is about to reload, please do not move or write in the chat until the reload is finished");
		Bukkit.broadcastMessage("�a+---------------------------------------------------+");
		Thread.sleep(3000);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�aServerreload in: �c�l6");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�aServerreload in: �c�l5");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�6Serverreload in: �c�l4");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�6Serverreload in: �c�l3");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�4Serverreload in: �c�l2");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�4Serverreload in: �c�l1");
		Thread.sleep(time);
		clearChat(100, false, p);
		List<Player> players = new ArrayList<Player>();
		players.addAll(Bukkit.getOnlinePlayers());
		for(int i = 0; i < players.size(); i++) {
			players.get(i).addScoreboardTag("reload");
			players.get(i).closeInventory();
		}
		Bukkit.broadcastMessage("�a+---------------------------------------------------+");
		Bukkit.broadcastMessage("        �r[�4�lAnnouncement�r] " + "�c�lReload started!");
		Bukkit.broadcastMessage("�a+---------------------------------------------------+");
		Bukkit.reload();
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�a+---------------------------------------------------+");
		Bukkit.broadcastMessage("        �r[�4�lAnnouncement�r] " + "�c�lReload finished!");
		Bukkit.broadcastMessage("�a+---------------------------------------------------+");
	}
	
	public static void serverStop(Player p, int time, int time2) throws InterruptedException {
		List<Player> players = new ArrayList<Player>();
		players.addAll(Bukkit.getOnlinePlayers());
		for(int i = 0; i < players.size(); i++) {
			players.get(i).closeInventory();
		}
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�a+---------------------------------------------------+");
		Bukkit.broadcastMessage("�r[�4�lWARNING�r] " + "�c�lThe server will now shut down and all players will be kicked!");
		Bukkit.broadcastMessage("�a+---------------------------------------------------+");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�aServerstop in: �c�l6");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�aServerstop in: �c�l5");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�6Serverstop in: �c�l4");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�6Serverstop in: �c�l3");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�4Serverstop in: �c�l2");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�4Serverstop in: �c�l1");
		Thread.sleep(time);
		clearChat(100, false, p);
		Bukkit.broadcastMessage("�a+---------------------------------------------------+");
		Bukkit.broadcastMessage("�r[�4�lWARNING�r] " + "�c�lServer Stop initiated!");
		Bukkit.broadcastMessage("�a+---------------------------------------------------+");
		Thread.sleep(time2);
		for(Player p2 : Bukkit.getServer().getOnlinePlayers()) {
			p2.kickPlayer("�4�lThe server is now shuting down!");
		}
		
		Thread.sleep(500);
		Bukkit.shutdown();
	}
	
	public static void clearChat(int lines, boolean showplayername, Player player) {
		if(showplayername == false) {
			for(int i = 0; i <= lines; i++) {
				Bukkit.getServer().broadcastMessage("");
			}
		} else {
				for(int i = 0; i <= lines; i++) {
					Bukkit.getServer().broadcastMessage("");
				}
				Bukkit.getServer().broadcastMessage(getMessages().getString("ServerManager.ChatClearHeader").replace('&', '�').replace("%prefix%", Main.plugin.getConfig().getString("Plugin.Prefix")).replace("%player%", player.getName()));
				Bukkit.getServer().broadcastMessage(getMessages().getString("ServerManager.ChatClearMessage").replace('&', '�').replace("%prefix%", Main.plugin.getConfig().getString("Plugin.Prefix")).replace("%player%", player.getName()));
				Bukkit.getServer().broadcastMessage(getMessages().getString("ServerManager.ChatClearFooter").replace('&', '�').replace("%prefix%", Main.plugin.getConfig().getString("Plugin.Prefix")).replace("%player%", player.getName()));
		}
	}

	public Main getPlugin() {
		return plugin;
	}

	public static void setPlugin(Main plugin) {
		Main.plugin = plugin;
	}
}