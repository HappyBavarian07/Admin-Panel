package de.happybavarian07.main;

import de.happybavarian07.api.StartUpLogger;
import de.happybavarian07.events.general.AdminPanelOpenEvent;
import de.happybavarian07.events.general.AdminPanelOpenForOtherEvent;
import de.happybavarian07.gui.*;
import de.happybavarian07.placeholders.PanelExpansion;
import de.happybavarian07.placeholders.PlayerExpansion;
import de.happybavarian07.placeholders.PluginExpansion;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
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

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin implements Listener {
	
	public MessagesManager mm;
	
	public Economy eco = null;
	public Permission perms = null;
	public Chat chat = null;
	final StartUpLogger logger = StartUpLogger.create();
	File configFile = new File(this.getDataFolder(), "config.yml");
	
	static File messagesfile = new File("plugins/Admin-Panel", "messages.yml");
	static FileConfiguration messages = YamlConfiguration.loadConfiguration(messagesfile);
	
	static File trollguiitemsfile = new File("plugins/Admin-Panel/Items", "TrollGUI.yml");
	static FileConfiguration trollitems = YamlConfiguration.loadConfiguration(trollguiitemsfile);

	static File banfile = new File("plugins/Admin-Panel", "bans.yml");
	private FileConfiguration banConfig = YamlConfiguration.loadConfiguration(banfile);
	
	public static FileConfiguration getMessages() { return messages; }

	public static void setMessages(FileConfiguration messages) {
		Main.messages = messages;
	}

	private static String prefix;
	
	public static String getPrefix() {
		return prefix;
	}

	public static void setPrefix(String prefix) {
		Main.prefix = prefix;
	}

	public FileConfiguration getBanConfig() {
		return banConfig;
	}

	private static Main plugin;
	@Override
	public void onEnable() {
		Utils Utils = new Utils(this);
		ChatUtil ChatUtil = new ChatUtil();
		Server server = getServer();
		ConsoleCommandSender ccs = server.getConsoleSender();

		// bStats
		int bStatsID = 11778;

		Metrics metrics = new Metrics(this, bStatsID);

		logger
			.coloredSpacer(ChatColor.GREEN)
				.messages(
						"§e§lStarting Admin Panel Plugin:§r"
                );
		logger.coloredSpacer(ChatColor.DARK_RED).message("§4§lInitialize Plugin Main Variable to this!§r");
		setPlugin(this);
		logger.message("§e§lVariable Done!§r");
		mm = new MessagesManager(this);
		if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new PlayerExpansion().register();
			new PluginExpansion().register();
			new PanelExpansion().register();
			logger.message("§a§lInitialized PlaceHolderAPI with Placeholders!");
		} else {
			logger.coloredSpacer(ChatColor.RED);
			logger.message("§4§lCould not find PlaceholderAPI!!");
			logger.message("§4§lPlugin can not work without it!");
			logger.coloredSpacer(ChatColor.RED);
			getServer().getPluginManager().disablePlugin(this);
		}
		logger
		.coloredSpacer(ChatColor.DARK_RED)
			.messages(
					"§c§lStarting Vault initialization!§r"
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
		setupPermission();
		setupChat();
		logger
				.messages(
						"§c§lFinished Vault initialization!§r"
				);
		if(!configFile.exists()) {
			logger.coloredSpacer(ChatColor.DARK_RED).message("§c§lCreating Default Config!§r");
		}
		saveDefaultConfig();
		if(!messagesfile.exists()) {
			logger
			.spacer()
				.messages(
						"§c§lCreating messages.yml file!§r",
						"§2§lAdding Default Messages!§r"
				);
			try {
				messagesfile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			loadMessages();
			logger.message("§e§lDone!§r");
		}
//		ccs.sendMessage("|---------§c§lCreating Item Configuration files if not exists!§r---------|");
//		ccs.sendMessage("|-----------------------§c§lLoading Troll Items!§r-----------------------|");
//		loadTrollItems();
//		ccs.sendMessage("|-----------------------------§e§lDone!§r--------------------------------|");
		if(!banfile.exists()) {
			logger.spacer().message("§c§lCreating bans.yml file!§r");
			try {
				banfile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.message("§e§lDone!§r");
		}
		logger.message("§3§lMain.Prefix §9= §7Config.Plugin.Prefix§r");
		setPrefix(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Plugin.Prefix")));
		logger.message("§e§lPrefix Done!§r");
		logger.coloredSpacer(ChatColor.DARK_RED).message("§2§lStarting Registration of Events:§r");
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new ExampleGui(this, messages, this.getConfig()), this);
		logger.message("§3§lLoading AdminPanel Main GUI Events!§r");
		pm.registerEvents(new PlayerManagerGUI(this, messages, this.getConfig(), banConfig, banfile), this);
		logger.message("§3§lLoading Player Manager GUI Events!§r");
		pm.registerEvents(new PluginStopGUI(this, messages, this.getConfig()), this);
		logger.message("§3§lLoading Plugin Manager GUI Events!§r");
		pm.registerEvents(new WorldManagment(this, messages, this.getConfig()), this);
		logger.message("§3§lLoading Time/Weather Changer GUI Events!§r");
		pm.registerEvents(new Time_Weather_Changer_GUi(this, messages, this.getConfig()), this);
		logger.message("§3§lLoading Player Troll GUI Events!§r");
		pm.registerEvents(new TrollGUI(this, messages, this.getConfig()), this);
		logger.message("§3§lLoading Server Manager GUI Events!§r");
		pm.registerEvents(new ServerManagment(this, messages, this.getConfig()), this);
		logger.message("§4§lEventregistration: Done!§r");
		logger.coloredSpacer(ChatColor.DARK_RED).message("§e§lStarting Done!§r");
		pm.registerEvents(this, this);
		logger.coloredSpacer(ChatColor.GREEN);
		if(messages != null && messages.getConfigurationSection("Plugin") != null) {
			ccs.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("Plugin.EnablingMessage")));
		} else {
			getServer().getConsoleSender().sendMessage("[Admin-Panel] enabled!");
		}
		//logger.coloredSpacer(ChatColor.GREEN);
		//logger.message("Searching for Updates: .");
		//logger.message("Searching for Updates: ..");
		//logger.message("Searching for Updates: ...");
		//logger.message("Searching for Updates: .....");
		//logger.message("Searching for Updates: ......");
		//UpdateChecker updater = new UpdateChecker(this);
		//updater.fetch();
		//logger.coloredSpacer(ChatColor.GREEN);
	}

	public StartUpLogger getStartUpLogger() { return logger; }



//	private void loadTrollItems(ConsoleCommandSender ccs) {
//		ccs.sendMessage("TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST");
//		ccs.sendMessage("TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST12121");
//	}








	private void loadMessages() {
//		messages.addDefault("Plugin.EnablingMessage", "%startprefix% successfully enabled!");
//		messages.addDefault("Plugin.DisablingMessage", "%startprefix% successfully disabled!");
//		messages.addDefault("No-Permission-Message", "&cYou don't have access to this Command!");
//		messages.addDefault("ConsoleExecutesPlayerCommand", "&cYou have to be a player!");
//		messages.addDefault("OpeningMessageSelf", "&cYou have opened the Admin Panel for you!");
//		messages.addDefault("OpeningMessageSelfOpenedForOther", "&cYou have opened the Admin Panel for %targetplayer%!");
//		messages.addDefault("OpeningMessageOther", "&c%player% has openend for you the Admin Panel!");
//		messages.addDefault("TargetedPlayerIsNull", "&cThis player is not online or does not exist!");
//		messages.addDefault("PlayerManager.SelfBanningMessage", "&cYou cannot Ban yourself!");
//		messages.addDefault("PlayerManager.KickReason", "&cKick for Safety!");
//		messages.addDefault("PlayerManager.KickSourceMessage", "%player%");
//		messages.addDefault("PlayerManager.BanReason", "&cBan for Safety!");
//		messages.addDefault("PlayerManager.BanSourceMessage", "%player%");
//		messages.addDefault("TomanyArguments", "&4There are to many Arguments!");
//		messages.addDefault("ServerManager.KickAllPlayersReason", "§cKick for Safety");
//		messages.addDefault("ServerManager.KickAllPlayersSource", "%player%");
//		messages.addDefault("ServerManager.ChatClearHeader", "&a+---------------------------------------------------+");
//		messages.addDefault("ServerManager.ChatClearMessage", "    &2The Chat has been cleared by &3%player%");
//		messages.addDefault("ServerManager.ChatClearFooter", "&a+---------------------------------------------------+");
//		messages.addDefault("ServerManager.MaintenanceMode", "&5%targetplayer% &cThe Server entered the &4&lMaintenance Mode!");
//		messages.addDefault("Pman.Money.NotEnoughMoneyToTake", "%targetplayer% has not enoug Money (%balance%)");
//		messages.addDefault("ChatMute.PlayerMessage", "&4The Chat is muted try again later %player%!");
//		messages.addDefault("ChatMute.Broadcastheader", "&a+---------------------------------------------------+");
//		messages.addDefault("ChatMute.Broadcast", "       &4The Chat has been muted by %player%!");
//		messages.addDefault("ChatMute.Broadcastfooter", "&a+---------------------------------------------------+");
//		messages.addDefault("ChatUnMute.Broadcastheader", "&a+---------------------------------------------------+");
//		messages.addDefault("ChatUnMute.Broadcast", "       &4The Chat has been unmuted by %player%!");
//		messages.addDefault("ChatUnMute.Broadcastfooter", "&a+---------------------------------------------------+");
//		messages.addDefault("TrollGui.Messages.Test", "Test");
//		messages.options().copyDefaults(true);
//		try {
//			messages.save(messagesfile);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	public static boolean isSuperVanishEnabled() {
		return plugin.getServer().getPluginManager().isPluginEnabled("SuperVanish");
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		if(ServerManagment.isMaintenance_mode() == true) {
			e.disallow(null, Utils.getInstance().replacePlaceHolders(p, messages.getString("ServerManager.MaintenanceMode"), Main.getPrefix()));
		}
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
	
	@Override
	public void onDisable() {
		if(messages != null && messages.getConfigurationSection("Plugin") != null) {
			getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("Plugin.DisablingMessage")));
		} else {
			getServer().getConsoleSender().sendMessage("[Admin-Panel] disabled!");
		}
		
	}

	private boolean setupPermission() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

	private boolean setupChat() {
		RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
		chat = rsp.getProvider();
		return chat != null;
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economy = getServer().
				getServicesManager().getRegistration(
						Economy.class);
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
					String sound = getConfig().getString("Panel.SoundWhenOpened");
					String nopermissionmessage = Utils.getInstance().replacePlaceHolders(p, messages.getString("No-Permission-Message"), Main.getPrefix());
					String openingselfmessage = Utils.getInstance().replacePlaceHolders(p, messages.getString("OpeningMessageSelf"), Main.getPrefix());
					if(p.hasPermission("AdminPanel.open")) {
						AdminPanelOpenEvent openEvent = new AdminPanelOpenEvent(p, openingselfmessage, Sound.valueOf(sound));
						Bukkit.getPluginManager().callEvent(openEvent);
						if(!openEvent.isCancelled()) {
							p.sendMessage(openEvent.getMessage());
							ExampleGui.openInv(p);
							if(getConfig().getBoolean("Panel.PlaySoundsWhenOponed") == true) {
								if(getConfig().getString("Panel.SoundWhenOpened") != null) {
									p.playSound(p.getLocation(), openEvent.getOpeningSound(), (float) getConfig().getDouble("Panel.SoundVolume"), (float) getConfig().getDouble("Panel.SoundPitch"));
								}
							}
						}
					} else {
						p.sendMessage(nopermissionmessage);
					}
				} else {
					s.sendMessage(Utils.getInstance().replacePlaceHolders(null, messages.getString("ConsoleExecutesPlayerCommand"), Main.getPrefix()));
				}
			} else if(args.length == 1) {
				if(s instanceof Player) {
					Player p = (Player) s;
					Player target = Bukkit.getPlayerExact(args[0]);
					String OpeningMessageOther = Utils.getInstance().replacePlaceHolders(p, messages.getString("OpeningMessageOther"), Main.getPrefix());
					String targetplayerisnull = Utils.getInstance().replacePlaceHolders(p, messages.getString("TargetedPlayerIsNull"), Main.getPrefix());
					String OpeningMessageSelfOpenedForOther = Utils.getInstance().replacePlaceHolders(p, messages.getString("OpeningMessageSelfOpenedForOther"), Main.getPrefix());
					if(p.hasPermission("AdminPanel.open.other")) {
						AdminPanelOpenForOtherEvent openForOtherEvent = new AdminPanelOpenForOtherEvent(p, target, OpeningMessageSelfOpenedForOther, OpeningMessageOther);
						Bukkit.getPluginManager().callEvent(openForOtherEvent);
						if(!openForOtherEvent.isCancelled()) {
							if (!messages.getString("OpeningMessageSelfOpenedForOther").equals("") || messages.getString("OpeningMessageSelfOpenedForOther") != null) {
								try {
									ExampleGui.openInv(target);
									p.sendMessage(OpeningMessageSelfOpenedForOther);
									target.sendMessage(OpeningMessageOther);
								} catch (NullPointerException e) {
									p.sendMessage(targetplayerisnull);
								}
							} else {
								try {
									ExampleGui.openInv(target);
									p.sendMessage(OpeningMessageSelfOpenedForOther);
								} catch (NullPointerException e) {
									p.sendMessage(targetplayerisnull);
								}
							}
						}
					} else {
						p.sendMessage(Utils.getInstance().replacePlaceHolders(p, messages.getString("No-Permission-Message"), Main.getPrefix()));
					}
				}
				if(s instanceof ConsoleCommandSender) {
					ConsoleCommandSender console = (ConsoleCommandSender) s;
					Player target = Bukkit.getPlayerExact(args[0]);
					String OpeningMessageOther = Utils.getInstance().replacePlaceHolders(target, messages.getString("OpeningMessageOther"), Main.getPrefix());
					String targetplayerisnull = Utils.getInstance().replacePlaceHolders(target, messages.getString("TargetedPlayerIsNull"), Main.getPrefix());
					String OpeningMessageSelfOpenedForOther = Utils.getInstance().replacePlaceHolders(target, messages.getString("OpeningMessageSelfOpenedForOther"), Main.getPrefix());
					AdminPanelOpenForOtherEvent openForOtherEvent = new AdminPanelOpenForOtherEvent(null, target, OpeningMessageOther, OpeningMessageSelfOpenedForOther);
					Bukkit.getPluginManager().callEvent(openForOtherEvent);
					if(!openForOtherEvent.isCancelled()) {
						if(messages.getString("OpeningMessageSelfOpenedForOther") != "" || messages.getString("OpeningMessageSelfOpenedForOther") != null) {
							try {
								ExampleGui.openInv(target);
								console.sendMessage(openForOtherEvent.getMessageToPlayer());
								target.sendMessage(openForOtherEvent.getMessageToTarget());
							} catch (NullPointerException e) {
								console.sendMessage(targetplayerisnull);
							}
						} else {
							try {
								console.sendMessage(openForOtherEvent.getMessageToPlayer());
								ExampleGui.openInv(target);
							} catch (NullPointerException e) {
								console.sendMessage(targetplayerisnull);
							}
						}
					}
				}
			} else {
				s.sendMessage(Utils.getInstance().replacePlaceHolders(null, messages.getString("TomanyArguments"), Main.getPrefix()));
			}
		}
		return true;
	}

	/*private String getAlphaNumericString(int n) {
		// lower limit for LowerCase Letters
		int lowerLimit = 97;

		// lower limit for LowerCase Letters
		int upperLimit = 122;

		Random random = new Random();

		// Create a StringBuffer to store the result
		StringBuffer r = new StringBuffer(n);

		for (int i = 0; i < n; i++) {

			// take a random value between 97 and 122
			int nextRandomChar = lowerLimit
					+ (int) (random.nextFloat()
					* (upperLimit - lowerLimit + 1));

			// append a character at the end of bs
			r.append((char) nextRandomChar);
		}

		// return the resultant string
		return r.toString();
	}*/

	public static Main getPlugin() {
		return plugin;
	}

	public static void setPlugin(Main plugin) {
		Main.plugin = plugin;
	}
}
