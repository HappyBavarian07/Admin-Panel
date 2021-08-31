package de.happybavarian07.main;

import de.happybavarian07.api.StartUpLogger;
import de.happybavarian07.events.general.AdminPanelOpenEvent;
import de.happybavarian07.events.general.AdminPanelOpenForOtherEvent;
import de.happybavarian07.gui.*;
import de.happybavarian07.listeners.MenuListener;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.menusystem.menu.playermanager.PlayerSelectMenu;
import de.happybavarian07.placeholders.PanelExpansion;
import de.happybavarian07.placeholders.PlayerExpansion;
import de.happybavarian07.placeholders.PluginExpansion;
import de.happybavarian07.utils.ChatUtil;
import de.happybavarian07.utils.Updater;
import de.happybavarian07.utils.Utils;
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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main extends JavaPlugin implements Listener {
	
	public MessagesManager mm;
	
	public Economy eco = null;
	public Permission perms = null;
	public Chat chat = null;
	private Updater updater;
	final StartUpLogger logger = StartUpLogger.create();
	File configFile = new File(this.getDataFolder(), "config.yml");
	
	static File messagesfile = new File("plugins/Admin-Panel", "messages.yml");
	static FileConfiguration messages = YamlConfiguration.loadConfiguration(messagesfile);

	static File banfile = new File("plugins/Admin-Panel", "bans.yml");
	private FileConfiguration banConfig = YamlConfiguration.loadConfiguration(banfile);

	private LanguageManager languageManager;

	public final Map<Player, Boolean> hurtingwater = new HashMap<>();
	public final Map<Player, Boolean> chatmute = new HashMap<>();
	public final Map<Player, Boolean> villagerSounds = new HashMap<>();
	public final Map<Player, Boolean> blockBreakPrevent = new HashMap<>();
	public final Map<Player, Boolean> dupeMobsOnKill = new HashMap<>();
	
	public static FileConfiguration getMessages() { return messages; }

	public static void setMessages(FileConfiguration messages) {
		Main.messages = messages;
	}

	private static String prefix;
	
	public static String getPrefix() {
		return prefix;
	}

	public Updater getUpdater() {
		return updater;
	}

	public static void setPrefix(String prefix) {
		Main.prefix = prefix;
	}

	public FileConfiguration getBanConfig() {
		return banConfig;
	}

	public LanguageManager getLanguageManager() {
		return languageManager;
	}

	private static final Map<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();

	public Map<Player, PlayerMenuUtility> getPlayerMenuUtilityMap() {
		return playerMenuUtilityMap;
	}

	private static Main plugin;
	@Override
	public void onEnable() {
		new Utils(this);
		new ChatUtil();
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
		new File(this.getDataFolder() + "/languages").mkdir();
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
		// Language Manager Enabling
		LanguageFile deLang = new LanguageFile(this, "de");
		LanguageFile enLang = new LanguageFile(this, "en");
		languageManager = new LanguageManager(this, new File(this.getDataFolder() + "/languages"));
		languageManager.addLang(deLang, deLang.getLangName());
		languageManager.addLang(enLang, enLang.getLangName());
		languageManager.setCurrentLang(languageManager.getLang(getConfig().getString("Plugin.language")));
		Bukkit.getPluginManager().registerEvents(new MenuListener(), this);
		if (languageManager != null && languageManager.getMessage("Plugin.EnablingMessage", null) != null &&
				!languageManager.getMessage("Plugin.EnablingMessage", null).equals("null config") &&
				!languageManager.getMessage("Plugin.EnablingMessage", null).startsWith("null path: Messages.")) {
			getServer().getConsoleSender().sendMessage(languageManager.getMessage("Plugin.EnablingMessage", null));
		} else {
			getServer().getConsoleSender().sendMessage("[Admin-Panel] enabled!");
		}
		if(getConfig().getBoolean("Plugin.Updater.checkForUpdates")) {
			updater = new Updater(this, 91800);
			updater.checkForUpdates();
			if(updater.updateAvailable()) {
				updater.downloadPlugin();
			}
		}
	}

	public File getFile() {
		return this.getFile();
	}

	public StartUpLogger getStartUpLogger() { return logger; }

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(PlayerLoginEvent e) {
		Player player = e.getPlayer();
		if(ServerManagment.isMaintenance_mode()) {
			e.disallow(PlayerLoginEvent.Result.KICK_OTHER, languageManager.getMessage("Player.ServerManager.MaintenanceMode", player));
		}
		if(player.isBanned()) {
			e.setKickMessage("§cDu wurdest vom Server gebannt!\n" +
							"\n" + 
							"§3Von: §e" + Objects.requireNonNull(Bukkit.getBanList(Type.NAME).getBanEntry(player.getName())).getSource() + "\n" +
							"\n" + 
							"§3Reason: §e" + Objects.requireNonNull(Bukkit.getBanList(Type.NAME).getBanEntry(player.getName())).getReason() + "\n" +
							"\n" + 
							"§3Permanently banned!" + "\n" +
							"\n" + 
							"§3Du kannst §c§nkeinen§3 Entbannungsantrag stellen!");
		}
	}
	
	@Override
	public void onDisable() {
		if(languageManager != null && languageManager.getMessage("Plugin.DisablingMessage", null) != null &&
				!languageManager.getMessage("Plugin.DisablingMessage", null).equals("null config") &&
				!languageManager.getMessage("Plugin.DisablingMessage", null).startsWith("null path: Messages.")) {
			getServer().getConsoleSender().sendMessage(languageManager.getMessage("Plugin.DisablingMessage", null));
		} else {
			getServer().getConsoleSender().sendMessage("[Admin-Panel] disabled!");
		}
		
	}

	private void setupPermission() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		if (rsp != null)
			perms = rsp.getProvider();
	}

	private void setupChat() {
		RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
		if (rsp != null)
			chat = rsp.getProvider();
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economy = getServer().getServicesManager().getRegistration(Economy.class);
		if(economy != null)
			eco = economy.getProvider();
		return eco != null;
	}

	public static PlayerMenuUtility getPlayerMenuUtility(Player p) {
		PlayerMenuUtility playerMenuUtility;
		if (!(playerMenuUtilityMap.containsKey(p))) { //See if the player has a playermenuutility "saved" for them

			//This player doesn't. Make one for them add add it to the hashmap
			playerMenuUtility = new PlayerMenuUtility(p);
			playerMenuUtilityMap.put(p, playerMenuUtility);

			return playerMenuUtility;
		} else {
			return playerMenuUtilityMap.get(p); //Return the object by using the provided player
		}
	}

	@Override
	public boolean onCommand(@NotNull CommandSender s, Command cmd, @NotNull String label, String[] args) {
		/*if(cmd.getName().equalsIgnoreCase("test")) {
			new PlayerSelectMenu(getPlayerMenuUtility((Player) s)).open();
		}*/
		if(cmd.getName().equalsIgnoreCase("adminpanel") || cmd.getName().equalsIgnoreCase("apanel") || cmd.getName().equalsIgnoreCase("adminp") || cmd.getName().equalsIgnoreCase("ap")) {
			if(args.length == 0) {
				if(s instanceof Player) {
					Player player = (Player) s;
					String sound = getConfig().getString("Panel.SoundWhenOpened");
					String nopermissionmessage = languageManager.getMessage("Player.General.NoPermissions", player);
					String openingselfmessage = languageManager.getMessage("Player.General.OpeningMessageSelf", player);
					if(player.hasPermission("AdminPanel.open")) {
						AdminPanelOpenEvent openEvent = new AdminPanelOpenEvent(player, openingselfmessage, Sound.valueOf(sound));
						Bukkit.getPluginManager().callEvent(openEvent);
						if(!openEvent.isCancelled()) {
							player.sendMessage(openEvent.getMessage());
							ExampleGui.openInv(player);
							if(getConfig().getBoolean("Panel.PlaySoundsWhenOponed")) {
								if(getConfig().getString("Panel.SoundWhenOpened") != null) {
									player.playSound(player.getLocation(), openEvent.getOpeningSound(), (float) getConfig().getDouble("Panel.SoundVolume"), (float) getConfig().getDouble("Panel.SoundPitch"));
								}
							}
						}
					} else {
						player.sendMessage(nopermissionmessage);
					}
				} else {
					s.sendMessage(languageManager.getMessage("Console.ExecutesPlayerCommand", null));
				}
			} else if(args.length == 1) {
				if(s instanceof Player) {
					Player player = (Player) s;
					Player target = Bukkit.getPlayerExact(args[0]);
					String OpeningMessageOther = languageManager.getMessage("Player.General.OpeningMessageOther", player);
					String targetplayerisnull = languageManager.getMessage("Player.General.TargetedPlayerIsNull", player);
					String OpeningMessageSelfOpenedForOther = languageManager.getMessage("Player.General.OpeningMessageSelfOpenedForOther", player);
					if(player.hasPermission("AdminPanel.open.other")) {
						AdminPanelOpenForOtherEvent openForOtherEvent = new AdminPanelOpenForOtherEvent(player, target, OpeningMessageSelfOpenedForOther, OpeningMessageOther);
						Bukkit.getPluginManager().callEvent(openForOtherEvent);
						if(!openForOtherEvent.isCancelled()) {
							if(!OpeningMessageSelfOpenedForOther.equals("null config") &&
									!OpeningMessageSelfOpenedForOther.startsWith("null path: Messages.")) {
								try {
									ExampleGui.openInv(openForOtherEvent.getTargetPlayer());
									player.sendMessage(OpeningMessageSelfOpenedForOther);
									openForOtherEvent.getTargetPlayer().sendMessage(OpeningMessageSelfOpenedForOther);
								} catch (NullPointerException e) {
									player.sendMessage(targetplayerisnull);
								}
							} else {
								try {
									ExampleGui.openInv(openForOtherEvent.getTargetPlayer());
									player.sendMessage(OpeningMessageSelfOpenedForOther);
								} catch (NullPointerException e) {
									player.sendMessage(targetplayerisnull);
								}
							}
						}
					} else {
						player.sendMessage(languageManager.getMessage("Player.General.NoPermissions", player));
					}
				}
				if(s instanceof ConsoleCommandSender) {
					ConsoleCommandSender console = (ConsoleCommandSender) s;
					Player target = Bukkit.getPlayerExact(args[0]);
					String OpeningMessageOther = languageManager.getMessage("Player.General.OpeningMessageOther", target);
					String targetplayerisnull = languageManager.getMessage("Player.General.TargetedPlayerIsNull", target);
					String OpeningMessageSelfOpenedForOther = languageManager.getMessage("Player.General.OpeningMessageSelfOpenedForOther", target);
					AdminPanelOpenForOtherEvent openForOtherEvent =
							new AdminPanelOpenForOtherEvent(null, target, OpeningMessageOther, OpeningMessageSelfOpenedForOther);
					Bukkit.getPluginManager().callEvent(openForOtherEvent);
					if(!openForOtherEvent.isCancelled()) {
						if(!OpeningMessageSelfOpenedForOther.equals("null config") &&
								!OpeningMessageSelfOpenedForOther.startsWith("null path: Messages.")) {
							try {
								ExampleGui.openInv(openForOtherEvent.getTargetPlayer());
								console.sendMessage(openForOtherEvent.getMessageToPlayer());
								openForOtherEvent.getTargetPlayer().sendMessage(openForOtherEvent.getMessageToTarget());
							} catch (NullPointerException e) {
								console.sendMessage(targetplayerisnull);
							}
						} else {
							try {
								console.sendMessage(openForOtherEvent.getMessageToPlayer());
								ExampleGui.openInv(openForOtherEvent.getTargetPlayer());
							} catch (NullPointerException e) {
								console.sendMessage(targetplayerisnull);
							}
						}
					}
				}
			} else {
				s.sendMessage(languageManager.getMessage("Player.ToManyArguments", null));
			}
		}
		return true;
	}

	public static Main getPlugin() {
		return plugin;
	}

	private void setPlugin(Main plugin) {
		Main.plugin = plugin;
	}

	public File getBanFile() {
		return banfile;
	}
}
