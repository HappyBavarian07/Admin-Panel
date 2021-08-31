package de.happybavarian07.gui;

import de.happybavarian07.api.Fireworkgenerator;
import de.happybavarian07.events.player.*;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.utils.Utils;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
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
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerManagerGUI implements Listener {
	
	static Main plugin;
	
	static FileConfiguration cfg;
    private static Inventory playermanager2;
	public static List<Inventory> playermanagerinvs = new ArrayList<>();
    @SuppressWarnings("unused")
	private static Inventory playermanager2_2;
	public static List<Inventory> playermanagerinvs_2 = new ArrayList<>();
    @SuppressWarnings("unused")
    private static Inventory playermanager2_3;
	public static List<Inventory> playermanagerinvs_3 = new ArrayList<>();
    @SuppressWarnings("unused")
    private static Inventory playermanager2_4;
	public static List<Inventory> playermanagerinvs_4 = new ArrayList<>();
    @SuppressWarnings("unused")
    private static Inventory playermanager2_5;
	public static List<Inventory> playermanagerinvs_5 = new ArrayList<>();
    private static Inventory bannedplayers;
	public static List<Inventory> bannedplayersinvs = new ArrayList<>();
	
	
    private static Inventory actions2;
	public static List<Inventory> actionsinvs = new ArrayList<>();
    private static Inventory armor2;
	public static List<Inventory> armorinvs = new ArrayList<>();
    private static Inventory playermanactionselector2;
	public static List<Inventory> playermanactionselectorinvs = new ArrayList<>();
    private static Inventory money2;
	public static List<Inventory> moneyinvs = new ArrayList<>();
    private static Inventory baninv2;
	public static List<Inventory> baninvs = new ArrayList<>();
    private static Inventory kickinv2;
	public static List<Inventory> kickinvs = new ArrayList<>();
    private static Inventory spawnerinv2;
	public static List<Inventory> spawnerinvs = new ArrayList<>();
    private static Inventory potioninv2;
	public static List<Inventory> potioninvs = new ArrayList<>();
    private static Inventory moneygiveinv2;
	public static List<Inventory> moneygiveinvs = new ArrayList<>();
    private static Inventory moneysetinv2;
	public static List<Inventory> moneysetinvs = new ArrayList<>();
    private static Inventory moneytakeinv2;
	public static List<Inventory> moneytakeinvs = new ArrayList<>();
    FileConfiguration messages;
    FileConfiguration bans;
    File banfile;
    
    public PlayerManagerGUI(Main main, FileConfiguration messages2, FileConfiguration config, FileConfiguration banconfig, File banfile) {
    	
    	this.banfile = banfile;
    	bans = banconfig;
    	cfg = config;
    	messages = messages2;
    	plugin = main;
        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example
		
        // Put the items into the inventory
    }
    
    public static void initializePotionItems(Inventory potioninv) {
    	// 33 Effekte
    	for(int i = 0; i < potioninv.getSize() ; i++) {
    		potioninv.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
    	}
    	
    	potioninv.setItem(0, createGuiItem(Material.POTION, "§2§lAbsorption"));
    	potioninv.setItem(1, createGuiItem(Material.POTION, "§4§lBad Omen"));
    	potioninv.setItem(2, createGuiItem(Material.POTION, "§4§lBlindness"));
    	potioninv.setItem(3, createGuiItem(Material.POTION, "§2§lConduit Power"));
    	potioninv.setItem(4, createGuiItem(Material.POTION, "§2§lDolphins Grace"));
    	potioninv.setItem(5, createGuiItem(Material.POTION, "§2§lFire Resistance"));
    	potioninv.setItem(6, createGuiItem(Material.POTION, "§2§lGlowing"));
    	potioninv.setItem(7, createGuiItem(Material.POTION, "§2§lHaste"));
    	potioninv.setItem(8, createGuiItem(Material.POTION, "§2§lHealth Boost"));
    	potioninv.setItem(9, createGuiItem(Material.POTION, "§2§lHero of the Village"));
    	potioninv.setItem(10, createGuiItem(Material.POTION, "§4§lHunger"));
    	potioninv.setItem(11, createGuiItem(Material.POTION, "§4§lInstant Damage"));
    	potioninv.setItem(12, createGuiItem(Material.POTION, "§2§lInstant Health"));
    	potioninv.setItem(13, createGuiItem(Material.POTION, "§2§lInvisibility"));
    	potioninv.setItem(14, createGuiItem(Material.POTION, "§2§lJump Boost"));
    	potioninv.setItem(15, createGuiItem(Material.POTION, "§2§lLevitation"));
    	potioninv.setItem(16, createGuiItem(Material.POTION, "§2§lLuck"));
    	potioninv.setItem(17, createGuiItem(Material.POTION, "§4§lMining Fatigue"));
    	potioninv.setItem(18, createGuiItem(Material.POTION, "§4§lNausea"));
    	potioninv.setItem(19, createGuiItem(Material.POTION, "§2§lNight Vision"));
    	potioninv.setItem(20, createGuiItem(Material.POTION, "§4§lPoison"));
    	potioninv.setItem(21, createGuiItem(Material.POTION, "§2§lRegeneration"));
    	potioninv.setItem(22, createGuiItem(Material.POTION, "§2§lResistance"));
    	potioninv.setItem(23, createGuiItem(Material.POTION, "§2§lSaturation"));
    	potioninv.setItem(24, createGuiItem(Material.POTION, "§2§lSlow Falling"));
    	potioninv.setItem(25, createGuiItem(Material.POTION, "§4§lSlowness"));
    	potioninv.setItem(26, createGuiItem(Material.POTION, "§2§lSpeed"));
    	potioninv.setItem(27, createGuiItem(Material.POTION, "§2§lStrength"));
    	potioninv.setItem(28, createGuiItem(Material.POTION, "§4§lUnluck"));
    	potioninv.setItem(29, createGuiItem(Material.POTION, "§2§lWater Breathing"));
    	potioninv.setItem(30, createGuiItem(Material.POTION, "§4§lWeakness"));
    	potioninv.setItem(31, createGuiItem(Material.POTION, "§4§lWither"));
    	
    	potioninv.setItem(41, createGuiItem(Material.BEACON, "§a§lLevel", "§cChange the Level by Clicking"));
    	potioninv.setItem(39, createGuiItem(Material.CLOCK, "§a§lTime", "§cChange the Time by Clicking"));
    	potioninv.setItem(40, createGuiItem(Material.RED_STAINED_GLASS_PANE, "§4§lRemove All", "§aClick to remove all effects from the Player!"));
    	potioninv.setItem(44, createGuiItem(Material.BARRIER, "§4§lBack", "§aClick to get Back!"));
    }
    
    // Spawner Inv initialize
    public static void initializeSpawnerItems(Inventory spawnerinv) {
    	spawnerinv.setItem(0, createGuiItem(Material.BAT_SPAWN_EGG, "§2§lBat"));
    	spawnerinv.setItem(1, createGuiItem(Material.BEE_SPAWN_EGG, "§2§lBee"));
    	spawnerinv.setItem(2, createGuiItem(Material.BLAZE_SPAWN_EGG, "§4§lBlaze"));
    	spawnerinv.setItem(3, createGuiItem(Material.CAT_SPAWN_EGG, "§2§lCat"));
    	spawnerinv.setItem(4, createGuiItem(Material.CAVE_SPIDER_SPAWN_EGG, "§4§lCave Spider"));
    	spawnerinv.setItem(5, createGuiItem(Material.CHICKEN_SPAWN_EGG, "§2§lChicken"));
    	spawnerinv.setItem(6, createGuiItem(Material.COD_SPAWN_EGG, "§2§lCod"));
    	spawnerinv.setItem(7, createGuiItem(Material.COW_SPAWN_EGG, "§2§lCow"));
    	spawnerinv.setItem(8, createGuiItem(Material.CREEPER_SPAWN_EGG, "§4§lCreeper"));
    	spawnerinv.setItem(9, createGuiItem(Material.DOLPHIN_SPAWN_EGG, "§2§lDolphin"));
    	spawnerinv.setItem(10, createGuiItem(Material.DONKEY_SPAWN_EGG, "§2§lDonkey"));
    	spawnerinv.setItem(11, createGuiItem(Material.DROWNED_SPAWN_EGG, "§4§lDrowned"));
    	spawnerinv.setItem(12, createGuiItem(Material.ELDER_GUARDIAN_SPAWN_EGG, "§4§lElder Guardian"));
    	spawnerinv.setItem(13, createGuiItem(Material.ENDERMAN_SPAWN_EGG, "§4§lEnderman"));
    	spawnerinv.setItem(14, createGuiItem(Material.ENDERMITE_SPAWN_EGG, "§4§lEndermite"));
    	spawnerinv.setItem(15, createGuiItem(Material.EVOKER_SPAWN_EGG, "§4§lEvoker"));
    	spawnerinv.setItem(16, createGuiItem(Material.FOX_SPAWN_EGG, "§2§lFox"));
    	spawnerinv.setItem(17, createGuiItem(Material.GHAST_SPAWN_EGG, "§4§lGhast"));
    	spawnerinv.setItem(18, createGuiItem(Material.GUARDIAN_SPAWN_EGG, "§4§lGuardian"));
    	spawnerinv.setItem(19, createGuiItem(Material.HORSE_SPAWN_EGG, "§2§lHorse"));
    	spawnerinv.setItem(20, createGuiItem(Material.HUSK_SPAWN_EGG, "§4§lHusk"));
    	spawnerinv.setItem(21, createGuiItem(Material.LLAMA_SPAWN_EGG, "§2§lLlama"));
    	spawnerinv.setItem(22, createGuiItem(Material.MAGMA_CUBE_SPAWN_EGG, "§4§lMagma Cube"));
    	spawnerinv.setItem(23, createGuiItem(Material.MOOSHROOM_SPAWN_EGG, "§2§lMooshroom"));
    	spawnerinv.setItem(24, createGuiItem(Material.MULE_SPAWN_EGG, "§2§lMule"));
    	spawnerinv.setItem(25, createGuiItem(Material.OCELOT_SPAWN_EGG, "§2§lOcelot"));
    	spawnerinv.setItem(26, createGuiItem(Material.PANDA_SPAWN_EGG, "§2§lPanda"));
    	spawnerinv.setItem(27, createGuiItem(Material.PARROT_SPAWN_EGG, "§2§lParrot"));
    	spawnerinv.setItem(28, createGuiItem(Material.PHANTOM_SPAWN_EGG, "§4§lPhantom"));
    	spawnerinv.setItem(29, createGuiItem(Material.PIG_SPAWN_EGG, "§2§lPig"));
    	spawnerinv.setItem(30, createGuiItem(Material.PILLAGER_SPAWN_EGG, "§4§lPillager"));
    	spawnerinv.setItem(31, createGuiItem(Material.POLAR_BEAR_SPAWN_EGG, "§6§lPolar Bear"));
    	spawnerinv.setItem(32, createGuiItem(Material.PUFFERFISH_SPAWN_EGG, "§6§lPufferfish"));
    	spawnerinv.setItem(33, createGuiItem(Material.RABBIT_SPAWN_EGG, "§2§lRabbit"));
    	spawnerinv.setItem(34, createGuiItem(Material.RAVAGER_SPAWN_EGG, "§4§lRavager"));
    	spawnerinv.setItem(35, createGuiItem(Material.SALMON_SPAWN_EGG, "§2§lSalmon"));
    	spawnerinv.setItem(36, createGuiItem(Material.SHEEP_SPAWN_EGG, "§2§lSheep"));
    	spawnerinv.setItem(37, createGuiItem(Material.SHULKER_SPAWN_EGG, "§4§lShulker"));
    	spawnerinv.setItem(38, createGuiItem(Material.SILVERFISH_SPAWN_EGG, "§4§lSilverfish"));
    	spawnerinv.setItem(39, createGuiItem(Material.SKELETON_SPAWN_EGG, "§4§lSkeleton"));
    	spawnerinv.setItem(40, createGuiItem(Material.SKELETON_HORSE_SPAWN_EGG, "§2§lSkeleton Horse"));
    	spawnerinv.setItem(41, createGuiItem(Material.SLIME_SPAWN_EGG, "§4§lSlime"));
    	spawnerinv.setItem(42, createGuiItem(Material.SPIDER_SPAWN_EGG, "§4§lSpider"));
    	spawnerinv.setItem(43, createGuiItem(Material.SQUID_SPAWN_EGG, "§2§lSquid"));
    	spawnerinv.setItem(44, createGuiItem(Material.STRAY_SPAWN_EGG, "§2§lStray"));
    	spawnerinv.setItem(45, createGuiItem(Material.TROPICAL_FISH_SPAWN_EGG, "§2§lTropical Fish"));
    	spawnerinv.setItem(46, createGuiItem(Material.TURTLE_SPAWN_EGG, "§2§lTurtle"));
    	spawnerinv.setItem(47, createGuiItem(Material.VEX_SPAWN_EGG, "§4§lVex"));
    	spawnerinv.setItem(48, createGuiItem(Material.VILLAGER_SPAWN_EGG, "§2§lVillager"));
    	spawnerinv.setItem(49, createGuiItem(Material.VINDICATOR_SPAWN_EGG, "§4§lVindicator"));
    	spawnerinv.setItem(50, createGuiItem(Material.WITCH_SPAWN_EGG, "§4§lWitch"));
    	spawnerinv.setItem(51, createGuiItem(Material.WOLF_SPAWN_EGG, "§6§lWolf"));
    	spawnerinv.setItem(52, createGuiItem(Material.ZOMBIE_SPAWN_EGG, "§4§lZombie"));
    	
    	spawnerinv.setItem(53, createGuiItem(Material.BARRIER, "§4§lBack", "§aClick to get Back!"));
    }
    
    public static void initializeMoneyItems(Inventory money) {
    	for(int i = 0; i < money2.getSize() ; i++) {
    		money2.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
    	}
    	money2.setItem(11, createGuiItem(Material.GREEN_WOOL, "§aGive", "§aGive the Player Money", "§c(Requires Vault + Economy Plugin)"));
    	money2.setItem(13, createGuiItem(Material.BOOK, "§aSet", "§aSet the Players Money", "§c(Requires Vault + Economy Plugin)"));
    	money2.setItem(15, createGuiItem(Material.RED_WOOL, "§aTake", "§aTake the Player Money", "§c(Requires Vault + Economy Plugin)"));
    	money2.setItem(26, createGuiItem(Material.BARRIER, "§4Back", "§aClick to get Back!"));
    }
    
    // You can call this whenever you want to put the items in
    public static void initializeItems(Inventory playermanager) {
    	for(int i = 0; i < playermanager.getSize() ; i++) {
    		if(playermanager.getItem(i) == null) {
    			playermanager.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
    		}
    	}
    	playermanager.setItem(47, createGuiItem(Material.RED_CONCRETE, "§aBanned Players", "§aList all Banned Players"));
    	playermanager.setItem(49, createGuiItem(Material.BARRIER, "§4Close", "§cClose the Menu!"));
    	playermanager.setItem(48, createGuiItem(Material.ARROW, "§4Page Back"));
    	playermanager.setItem(50, createGuiItem(Material.ARROW, "§4Page Forward"));
    	Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			OfflinePlayer[] onlineplayer = Bukkit.getOfflinePlayers();
			for (int i = 0; i < Bukkit.getOfflinePlayers().length; i++) {
				if (i <= (i + 45)) {
					if (onlineplayer[i].isOnline()) {
						ItemStack head = plugin.getLanguageManager().getItem("PlayerManager.PlayerHead", (Player) onlineplayer[i]);
						SkullMeta meta = (SkullMeta) head.getItemMeta();
						meta.setOwningPlayer(onlineplayer[i]);
						head.setItemMeta(meta);
						playermanager.setItem(i, head);
					}
				}
			}
		}, 0L, 40L);
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
    public static void openInv(final HumanEntity ent) {
    	playermanager2 = Bukkit.createInventory(null, 54, "§5§lPlayer Manager");
    	initializeItems(playermanager2);
    	playermanagerinvs.add(playermanager2);
		ent.openInventory(playermanager2);
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
    public static void openBannedPlayers(final HumanEntity ent) {
    	bannedplayers = Bukkit.createInventory(null, 54, "§5§lBanned Players");
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			
			@Override
			public void run() {
        		for(int i = 0; i < Bukkit.getOfflinePlayers().length; i++) {
        			if(Bukkit.getOfflinePlayers()[i].isBanned()) {
        				if(i <= 45) {
	        				ItemStack bannedplayeritem = new ItemStack(Material.PLAYER_HEAD);
	        				SkullMeta meta = (SkullMeta) bannedplayeritem.getItemMeta();
	        				meta.setDisplayName(Bukkit.getOfflinePlayers()[i].getName());
	        				List<String> lore = new ArrayList<String>();
	        				lore.add("§aTest");
	        				meta.setOwningPlayer(Bukkit.getOfflinePlayers()[i]);
	        				meta.setLore(lore);
	        				bannedplayeritem.setItemMeta(meta);
	        				bannedplayers.setItem((i-1), bannedplayeritem);
        				}
        			}
        		}
			}
		}, 0L, 20L);
		bannedplayers.setItem(49, createGuiItem(Material.BARRIER, "§cBack"));
    	bannedplayersinvs.add(bannedplayers);
		ent.openInventory(bannedplayers);
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
    
    public static void openActions(final HumanEntity ent) {
        actionsinvs.add(actions2);
    	ent.openInventory(actions2);
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
    
    public static void openPlayerMenuSelector(final HumanEntity ent) {
    	ent.openInventory(playermanactionselector2);
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
    
    public static void openSpawnerInv(final HumanEntity ent, Player target) {
		spawnerinv2 = Bukkit.createInventory(null, 9*6, target.getName());
		initializeSpawnerItems(spawnerinv2);
		spawnerinvs.add(spawnerinv2);
    	ent.openInventory(spawnerinv2);
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
    public static void openPotionInv(final HumanEntity ent, Player target) {
		potioninv2 = Bukkit.createInventory(null, 9*5, target.getName());
		initializePotionItems(potioninv2);
		potioninvs.add(potioninv2);
		ent.openInventory(potioninv2);
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
    public static void openMoneyGiveInv(final HumanEntity ent, Player target) {
    	moneygiveinv2 = Bukkit.createInventory(null, 9*4, target.getName());
		initializeMoneyGiveItems(moneygiveinv2);
		moneygiveinvs.add(moneygiveinv2);
		ent.openInventory(moneygiveinv2);
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
    public static void openMoneySetInv(final HumanEntity ent, Player target) {
    	moneysetinv2 = Bukkit.createInventory(null, 9*4, target.getName());
		initializeMoneySetItems(moneysetinv2);
		moneysetinvs.add(moneysetinv2);
		ent.openInventory(moneysetinv2);
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
    public static void openMoneyTakeInv(final HumanEntity ent, Player target) {
    	moneytakeinv2 = Bukkit.createInventory(null, 9*4, target.getName());
		initializeMoneyTakeItems(moneytakeinv2);
		moneytakeinvs.add(moneytakeinv2);
		ent.openInventory(moneytakeinv2);
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
	
    private static void initializeMoneyGiveItems(Inventory moneyGiveInv) {
    	moneyGiveInv.setItem(0, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "100"));
    	moneyGiveInv.setItem(1, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "200"));
    	moneyGiveInv.setItem(2, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "300"));
    	moneyGiveInv.setItem(3, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "400"));
    	moneyGiveInv.setItem(4, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "500"));
    	moneyGiveInv.setItem(5, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "600"));
    	moneyGiveInv.setItem(6, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "700"));
    	moneyGiveInv.setItem(7, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "800"));
    	moneyGiveInv.setItem(8, createGuiItem(Material.PAPER, "§a§L" + cfg.getString("Pman.Money.currency") + "900"));
    	moneyGiveInv.setItem(9, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "1.000"));
    	moneyGiveInv.setItem(10, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "1.500"));
    	moneyGiveInv.setItem(11, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "3.000"));
    	moneyGiveInv.setItem(12, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "4.500"));
    	moneyGiveInv.setItem(13, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "6.000"));
    	moneyGiveInv.setItem(14, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "7.500"));
    	moneyGiveInv.setItem(15, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "9.000"));
    	moneyGiveInv.setItem(16, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "10.500"));
    	moneyGiveInv.setItem(17, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "12.000"));
    	moneyGiveInv.setItem(18, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "13.500"));
    	moneyGiveInv.setItem(19, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "15.000"));
    	moneyGiveInv.setItem(20, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "30.000"));
    	moneyGiveInv.setItem(21, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "45.000"));
    	moneyGiveInv.setItem(22, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "60.000"));
    	moneyGiveInv.setItem(23, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "75.000"));
    	moneyGiveInv.setItem(24, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "90.000"));
    	moneyGiveInv.setItem(25, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "100.000"));
    	moneyGiveInv.setItem(26, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "200.000"));
    	moneyGiveInv.setItem(27, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "300.000"));
    	moneyGiveInv.setItem(28, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "400.000"));
    	moneyGiveInv.setItem(29, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "500.000"));
    	moneyGiveInv.setItem(30, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "600.000"));
    	moneyGiveInv.setItem(31, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "700.000"));
    	moneyGiveInv.setItem(32, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "800.000"));
    	moneyGiveInv.setItem(33, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "900.000"));
    	moneyGiveInv.setItem(34, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "1.000.000"));
    	
    	moneyGiveInv.setItem(35, createGuiItem(Material.BARRIER, "§4Back!"));
	}

    private static void initializeMoneySetItems(Inventory moneySetInv) {
    	moneySetInv.setItem(0, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "100"));
    	moneySetInv.setItem(1, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "200"));
    	moneySetInv.setItem(2, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "300"));
    	moneySetInv.setItem(3, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "400"));
    	moneySetInv.setItem(4, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "500"));
    	moneySetInv.setItem(5, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "600"));
    	moneySetInv.setItem(6, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "700"));
    	moneySetInv.setItem(7, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "800"));
    	moneySetInv.setItem(8, createGuiItem(Material.PAPER, "§a§L" + cfg.getString("Pman.Money.currency") + "900"));
    	moneySetInv.setItem(9, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "1.000"));
    	moneySetInv.setItem(10, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "1.500"));
    	moneySetInv.setItem(11, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "3.000"));
    	moneySetInv.setItem(12, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "4.500"));
    	moneySetInv.setItem(13, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "6.000"));
    	moneySetInv.setItem(14, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "7.500"));
    	moneySetInv.setItem(15, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "9.000"));
    	moneySetInv.setItem(16, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "10.500"));
    	moneySetInv.setItem(17, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "12.000"));
    	moneySetInv.setItem(18, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "13.500"));
    	moneySetInv.setItem(19, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "15.000"));
    	moneySetInv.setItem(20, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "30.000"));
    	moneySetInv.setItem(21, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "45.000"));
    	moneySetInv.setItem(22, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "60.000"));
    	moneySetInv.setItem(23, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "75.000"));
    	moneySetInv.setItem(24, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "90.000"));
    	moneySetInv.setItem(25, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "100.000"));
    	moneySetInv.setItem(26, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "200.000"));
    	moneySetInv.setItem(27, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "300.000"));
    	moneySetInv.setItem(28, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "400.000"));
    	moneySetInv.setItem(29, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "500.000"));
    	moneySetInv.setItem(30, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "600.000"));
    	moneySetInv.setItem(31, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "700.000"));
    	moneySetInv.setItem(32, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "800.000"));
    	moneySetInv.setItem(33, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "900.000"));
    	moneySetInv.setItem(34, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "1.000.000"));
    	
    	moneySetInv.setItem(35, createGuiItem(Material.BARRIER, "§4Back!"));
	}

    private static void initializeMoneyTakeItems(Inventory moneyTakeInv) {
    	moneyTakeInv.setItem(0, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "100"));
    	moneyTakeInv.setItem(1, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "200"));
    	moneyTakeInv.setItem(2, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "300"));
    	moneyTakeInv.setItem(3, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "400"));
    	moneyTakeInv.setItem(4, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "500"));
    	moneyTakeInv.setItem(5, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "600"));
    	moneyTakeInv.setItem(6, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "700"));
    	moneyTakeInv.setItem(7, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "800"));
    	moneyTakeInv.setItem(8, createGuiItem(Material.PAPER, "§a§L" + cfg.getString("Pman.Money.currency") + "900"));
    	moneyTakeInv.setItem(9, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "1.000"));
    	moneyTakeInv.setItem(10, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "1.500"));
    	moneyTakeInv.setItem(11, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "3.000"));
    	moneyTakeInv.setItem(12, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "4.500"));
    	moneyTakeInv.setItem(13, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "6.000"));
    	moneyTakeInv.setItem(14, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "7.500"));
    	moneyTakeInv.setItem(15, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "9.000"));
    	moneyTakeInv.setItem(16, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "10.500"));
    	moneyTakeInv.setItem(17, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "12.000"));
    	moneyTakeInv.setItem(18, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "13.500"));
    	moneyTakeInv.setItem(19, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "15.000"));
    	moneyTakeInv.setItem(20, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "30.000"));
    	moneyTakeInv.setItem(21, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "45.000"));
    	moneyTakeInv.setItem(22, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "60.000"));
    	moneyTakeInv.setItem(23, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "75.000"));
    	moneyTakeInv.setItem(24, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "90.000"));
    	moneyTakeInv.setItem(25, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "100.000"));
    	moneyTakeInv.setItem(26, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "200.000"));
    	moneyTakeInv.setItem(27, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "300.000"));
    	moneyTakeInv.setItem(28, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "400.000"));
    	moneyTakeInv.setItem(29, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "500.000"));
    	moneyTakeInv.setItem(30, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "600.000"));
    	moneyTakeInv.setItem(31, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "700.000"));
    	moneyTakeInv.setItem(32, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "800.000"));
    	moneyTakeInv.setItem(33, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "900.000"));
    	moneyTakeInv.setItem(34, createGuiItem(Material.PAPER, "§a§l" + cfg.getString("Pman.Money.currency") + "1.000.000"));
    	
    	moneyTakeInv.setItem(35, createGuiItem(Material.BARRIER, "§4Back!"));
	}
    
	// You can open the inventory with this
    public static void openMoneyInv(final HumanEntity ent, Player target) {
		money2 = Bukkit.createInventory(null, 9*3, target.getName());
		initializeMoneyItems(money2);
		moneyinvs.add(money2);
		ent.openInventory(money2);
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
    public static void openBanInv(final HumanEntity ent, OfflinePlayer target) {
		baninv2 = Bukkit.createInventory(null, 9*3, target.getName());
    	for(int i = 0; i < baninv2.getSize(); i++) {
    		baninv2.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
    	}
    	baninv2.setItem(4, createGuiItem(Material.BEDROCK, "§4Ban", "§4§lBanne den Spieler permanent vom Server!"));
    	baninv2.setItem(13, createGuiItem(Material.BEACON, "§aUnban", "§a§lEntbanne den Spieler!"));
    	baninv2.setItem(22, createGuiItem(Material.BARRIER, "§4Back", "Click to go back to the Action Selector"));
		baninvs.add(baninv2);
		ent.openInventory(baninv2);
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
    public static void openKickInv(final HumanEntity ent, Player target) {
		kickinv2 = Bukkit.createInventory(null, 9*3, target.getName());
    	for(int i = 0; i < kickinv2.getSize(); i++) {
    		kickinv2.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
    	}
    	kickinv2.setItem(13, createGuiItem(Material.PISTON, "§5Kick", "§aKicke den Spieler vom Server!"));
    	kickinv2.setItem(22, createGuiItem(Material.BARRIER, "§4Back", "Click to go back to the Action Selector"));
		kickinvs.add(kickinv2);
		ent.openInventory(kickinv2);
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
	@SuppressWarnings({ "deprecation", })
	@EventHandler
    public void onInventoryClick(final InventoryClickEvent e) throws InterruptedException {
        if (!bannedplayersinvs.contains(e.getInventory()) && !playermanagerinvs.contains(e.getInventory()) && !actionsinvs.contains(e.getInventory()) && !armorinvs.contains(e.getInventory()) && !playermanactionselectorinvs.contains(e.getInventory()) && !spawnerinvs.contains(e.getInventory()) && !potioninvs.contains(e.getInventory()) && !moneyinvs.contains(e.getInventory()) && !moneygiveinvs.contains(e.getInventory()) && !moneysetinvs.contains(e.getInventory()) && !moneytakeinvs.contains(e.getInventory()) && !baninvs.contains(e.getInventory()) && !kickinvs.contains(e.getInventory())) return;
        
        e.setCancelled(true);
        final ItemStack clickedItem = e.getCurrentItem();
        
        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        final Player player = (Player) e.getWhoClicked();

		LanguageManager lgm = plugin.getLanguageManager();

		String nopermissionmessage = lgm.getMessage("Player.General.NoPermissions", player);

		// Using slots click is a best option for your inventory click's
        if(clickedItem.getType() == Material.BARRIER) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§cBack")) {
		        if(player.hasPermission("AdminPanel.Button.Back")) {
		        	PlayerManagerGUI.openInv(player);
		        } else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Back")) {
		        if(player.hasPermission("AdminPanel.Button.Back")) {
		        	PlayerManagerGUI.openPlayerMenuSelector(player);
		        } else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Back!")) {
		        if(player.hasPermission("AdminPanel.Button.Back")) {
		        	Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
		        	PlayerManagerGUI.openMoneyInv(player, target);
		        } else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lBack")) {
		        if(player.hasPermission("AdminPanel.Button.Back")) {
		        	PlayerManagerGUI.openActions(player);
		        } else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Close")) {
    	        if(player.hasPermission("AdminPanel.Button.Close")) {
    	        	ExampleGui.openInv(player);
    	        } else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Page Forward
        if(clickedItem.getType() == Material.ARROW) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Page Forward")) {
    	        if(player.hasPermission("AdminPanel.Button.pageforward")) {
    	        	if(player.getOpenInventory().getTitle().equals("")) {
    	        		
    	        	}
    	        	if(player.getOpenInventory().getTitle().equals("")) {
    	        		
    	        	}
    	        	if(player.getOpenInventory().getTitle().equals("")) {
    	        		
    	        	}
    	        	if(player.getOpenInventory().getTitle().equals("")) {
    	        		
    	        	}
    	        	if(player.getOpenInventory().getTitle().equals("")) {
    	        		
    	        	}
    	        } else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Page Back")) {
    	        if(player.hasPermission("AdminPanel.Button.pageback")) {
    	        	
    	        } else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Page Back
        if(clickedItem.getType() == Material.DIAMOND_SWORD) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aActions")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Actions")) {
        			PlayerManagerGUI.openActions(player);
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Actions Menu
        if(clickedItem.getType() == Material.BEACON) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§a§lLevel")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Potions")) {
        			if(!(clickedItem.getAmount() == 20)) {
        				clickedItem.setAmount(clickedItem.getAmount() + 1);
        			} else {
        				clickedItem.setAmount(1);
        			}
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Effect Time
        if(clickedItem.getType() == Material.CLOCK) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§a§lTime")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Potions")) {
        			if(!(clickedItem.getAmount() == 64)) {
        				clickedItem.setAmount(clickedItem.getAmount() + 1);
        			} else {
        				clickedItem.setAmount(1);
        			}
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Effect Strengthness
        if(clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
    		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
    		player.performCommand("minecraft:effect clear " + target.getName());
        } // Clear Effect
        if(clickedItem.getType() == Material.POTION) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lAbsorption")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lBad Omen")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.BAD_OMEN, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lBlindness")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lConduit Power")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lDolphins Grace")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lFire Resistance")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lGlowing")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lHaste")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lHealth Boost")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lHero of the Village")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lHunger")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lInstant Damage")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.HARM, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lInstant Health")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lInvisibility")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lJump Boost")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lLevitation")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lLuck")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lMining Fatigue")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lNausea")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lNight Vision")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lPoison")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lRegeneration")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lResistance")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lSaturation")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lSlow Falling")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lSlowness")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lSpeed")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lStrength")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lUnluck")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lWater Breathing")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lWeakness")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lWither")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        } // Give Effect
        if(clickedItem.getType() == Material.PLAYER_HEAD) {
        	if(!e.getInventory().equals(bannedplayers)) {
	        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Actions") && !clickedItem.getItemMeta().getDisplayName().equals(player.getName())) {
	        		if(!Bukkit.getPlayerExact(clickedItem.getItemMeta().getDisplayName().replace("§e§l", "")).hasPermission("AdminPanel.Bypass.AffectedByPlayerManager") && !(Bukkit.getPlayerExact(clickedItem.getItemMeta().getDisplayName().replace("§e§l", "")) == null)) {
						Player target = Bukkit.getPlayerExact(clickedItem.getItemMeta().getDisplayName().replace("§e§l", ""));
						SelectPlayerEvent selectPlayerEvent = new SelectPlayerEvent(player, target);
						Bukkit.getPluginManager().callEvent(selectPlayerEvent);
						if(!selectPlayerEvent.isCancelled()) {
							initializeItems(playermanager2);
							actions2 = Bukkit.createInventory(null, 9*6, clickedItem.getItemMeta().getDisplayName().replace("§e§l", ""));
							playermanactionselector2 = Bukkit.createInventory(null, 9*3, clickedItem.getItemMeta().getDisplayName().replace("§e§l", ""));
							for(int i = 0; i < actions2.getSize(); i++) {
								actions2.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
							}
							for(int i = 0; i < playermanactionselector2.getSize(); i++) {
								playermanactionselector2.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
							}
							actions2.setItem(47, createGuiItem(Material.COBBLESTONE, "§2Inventory", "§aShow the Player's Inventory!"));
							playermanactionselector2.setItem(4, clickedItem);
							actions2.setItem(4, clickedItem);
							actions2.setItem(21, lgm.getItem("PlayerManager.ActionsMenu.Spawning", player));
							actions2.setItem(19, lgm.getItem("PlayerManager.ActionsMenu.Potions", player));
							actions2.setItem(15, lgm.getItem("PlayerManager.ActionsMenu.Feed", player));
							actions2.setItem(13, lgm.getItem("PlayerManager.ActionsMenu.Kill", player));
							actions2.setItem(11, lgm.getItem("PlayerManager.ActionsMenu.Heal", player));
							playermanactionselector2.setItem(26, createGuiItem(Material.BARRIER, "§cBack", "Click to go back to the Player Manager"));
							Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

								@Override
								public void run() {
									assert target != null;
									if(target.getGameMode() == GameMode.ADVENTURE) {
										actions2.setItem(23, createGuiItem(Material.GRASS_BLOCK, "§1Adventure", "§aClick to change game mode!"));
									} else if(target.getGameMode() == GameMode.CREATIVE) {
										actions2.setItem(23, createGuiItem(Material.BRICKS, "§1Creative", "§aClick to change game mode!"));
									} else if(target.getGameMode() == GameMode.SPECTATOR) {
										actions2.setItem(23, createGuiItem(Material.SPLASH_POTION, "§1Spectator", "§aClick to change game mode!"));
									} else {
										actions2.setItem(23, createGuiItem(Material.DIRT, "§1Survival", "§aClick to change game mode!"));
									}
									//if(VanishAPI.isInvisible(player)) { actions2.setItem(25, createGuiItem(Material.FEATHER, "§b§lVanish §4CURRENTLY IN DEVELOPMENT!3", "§aClick to activate the Vanish", "§afor the Player!", "Vanish-Plugin: SuperVanish")); } else { actions2.setItem(25, createGuiItem(Material.FEATHER, "§c§lVanish §4CURRENTLY IN DEVELOPMENT!4", "§aClick to deactivate the Vanish", "§afor the Player!", "Vanish-Plugin: SuperVanish")); }
								}
							}, 0L, 10L);
							actions2.setItem(25, createGuiItem(Material.RED_BED, "§cPlayer Spawn Location", "§aClick to set the Spawn Location", "§aof the Player, to your Position!"));
							actions2.setItem(31, createGuiItem(Material.TNT, "§c§lTroll Menu", "§r§cRed §6= §3off§6, §aGreen §6= §3on"));
							actions2.setItem(43, createGuiItem(Material.PAPER, "§aOp", "§aMake the player an operator!"));
							actions2.setItem(49, createGuiItem(Material.PAPER, "§cDeop", "§aTake away the player's operator!"));
							actions2.setItem(41, createGuiItem(Material.END_CRYSTAL, "§1§lTeleport the Player to You"));
							actions2.setItem(39, createGuiItem(Material.ENDER_PEARL, "§2§lTeleport to Player"));
							actions2.setItem(37, createGuiItem(Material.FIREWORK_ROCKET, "§1F§2i§3r§4e§5w§6o§7r§8k", "§aThe player fires a", "§aFireworks rocket out of his head!"));
							actions2.setItem(33, createGuiItem(Material.BLAZE_ROD, "§eLightning", "§cShot an Lightning on the Player!"));
							actions2.setItem(29, createGuiItem(Material.FLINT_AND_STEEL, "§4§lBurn Player", "§aClick to burn the Player!"));
							actions2.setItem(51, createGuiItem(Material.DIAMOND_CHESTPLATE, "§2Armor", "§aShows you the Armor of the player"));
							actions2.setItem(53, createGuiItem(Material.BARRIER, "§4Back", "Back to the Player Menu!"));
							playermanactionselector2.setItem(10, createGuiItem(Material.DIAMOND_SWORD, "§aActions", "§aClick to open the Actions Menu!"));
							playermanactionselector2.setItem(12, createGuiItem(Material.PAPER, "§aMoney", "§aClick to open the Money Menu!"));
							playermanactionselector2.setItem(14, createGuiItem(Material.BLACK_TERRACOTTA, "§aKick Player", "§aClick to open the Kick Menu!"));
							playermanactionselector2.setItem(16, createGuiItem(Material.BEDROCK, "§aBan Player", "§aClick to open the Ban Menu!"));
							actionsinvs.add(actions2);
							armorinvs.add(armor2);

							playermanactionselectorinvs.add(playermanactionselector2);
							PlayerManagerGUI.openPlayerMenuSelector(player);
						}
	        		} else {
	        			player.sendMessage(lgm.getMessage("Player.PlayerManager.Bypass", player));
	        		}
	        	} else {
	        		if(!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Open")) {
						player.sendMessage(nopermissionmessage);
					}
	        		if(clickedItem.getItemMeta().getDisplayName().equals(player.getName())) {
	        			player.sendMessage(lgm.getMessage("Player.PlayerManager.ChooseYourself", player));
	        		}
				}
        	}
        	if(e.getInventory().equals(bannedplayers)) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Ban")) {
					Utils.getInstance().unban(player, clickedItem.getItemMeta().getDisplayName());
        		} else {
	        		if(!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Ban")) {
						player.sendMessage(nopermissionmessage);
					}
        		}
        	}
        } // Select Player
        if(clickedItem.getType() == Material.TNT) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§c§lTroll Menu")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll")) {
        			TrollGUI.openTrollInv(player, Bukkit.getPlayerExact(player.getOpenInventory().getTitle()));
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Troll Menu
        if(clickedItem.getType() == Material.BLACK_TERRACOTTA) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aKick Player")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Kick")) {
					PlayerManagerGUI.openKickInv(player, Bukkit.getPlayerExact(player.getOpenInventory().getTitle()));
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Open Kick Inv
        if(clickedItem.getType() == Material.BLAZE_ROD) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§eLightning")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Lightning")) {
        			Bukkit.getPlayerExact(player.getOpenInventory().getTitle()).getWorld().strikeLightning(Bukkit.getPlayerExact(player.getOpenInventory().getTitle()).getLocation());
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        }  // Lightning Strike
        if(clickedItem.getType() == Material.GRASS_BLOCK || clickedItem.getType() == Material.BRICKS || clickedItem.getType() == Material.SPLASH_POTION || clickedItem.getType() == Material.DIRT) { // GameMode
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Gamemode")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
	        	if(clickedItem.getItemMeta().getDisplayName().equals("§1Adventure")) {
	        		target.setGameMode(GameMode.CREATIVE);
	        		actions2.setItem(23, createGuiItem(Material.BRICKS, "§1Creative", "§aKlicke um den Gamemode zu wechseln!"));
	        	}
	        	if(clickedItem.getItemMeta().getDisplayName().equals("§1Creative")) {
	        		target.setGameMode(GameMode.SPECTATOR);
	        		actions2.setItem(23, createGuiItem(Material.SPLASH_POTION, "§1Spectator", "§aKlicke um den Gamemode zu wechseln!"));
	        	}
	        	if(clickedItem.getItemMeta().getDisplayName().equals("§1Spectator")) {
	        		target.setGameMode(GameMode.SURVIVAL);
	        		actions2.setItem(23, createGuiItem(Material.DIRT, "§1Survival", "§aKlicke um den Gamemode zu wechseln!"));
	        	}
	        	if(clickedItem.getItemMeta().getDisplayName().equals("§1Survival")) {
	        		target.setGameMode(GameMode.ADVENTURE);
	        		actions2.setItem(23, createGuiItem(Material.GRASS_BLOCK, "§1Adventure", "§aKlicke um den Gamemode zu wechseln!"));
	        	}
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.GOLDEN_APPLE) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§e§lHeal")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Heal")) {
        			Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        			target.setHealth(cfg.getDouble("Pman.Actions.HealthHealAmount"));
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Heal
        if(clickedItem.getType() == Material.COOKED_BEEF) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§dFeed")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Feed")) {
        			Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        			target.setFoodLevel(cfg.getInt("Pman.Actions.FoodLevelFillAmount"));
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Feed
        if(clickedItem.getType() == Material.FIREWORK_ROCKET) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Firework")) {
        		Fireworkgenerator fwg = new Fireworkgenerator(plugin);
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		fwg.setLocation(target.getLocation().add(0, 1.7, 0));
        		fwg.setPower(1);
        		fwg.setEffect(FireworkEffect.builder().withColor(Color.RED).withColor(Color.AQUA).withColor(Color.YELLOW).withColor(Color.BLUE).withColor(Color.GREEN).with(Type.BALL_LARGE).withFlicker().build());
        		fwg.setLifeTime(30);
        		fwg.spawn();
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        } // Firework
        if(clickedItem.getType() == Material.ENDER_PEARL) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.TeleportYouToPlayer")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		player.teleport(target);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        } // Teleport Player to Target
        if(clickedItem.getType() == Material.GREEN_WOOL) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aGive")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Money.Give")) {
        			Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
					PlayerManagerGUI.openMoneyGiveInv(player, target);
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Money Give Menu
        if(clickedItem.getType() == Material.BOOK) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aSet")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Money.Set")) {
        			Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
					PlayerManagerGUI.openMoneySetInv(player, target);
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Money Set inv
        if(clickedItem.getType() == Material.RED_WOOL) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aTake")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Money.Take")) {
        			Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
					PlayerManagerGUI.openMoneyTakeInv(player, target);
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Money Take Inv
        if(clickedItem.getType() == Material.PAPER) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aMoney")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Money")) {
        			Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        			PlayerManagerGUI.openMoneyInv(player, target);
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	} else {
        		if(moneygiveinvs.contains(e.getInventory())) {
					double moneytogive = Double.parseDouble(clickedItem.getItemMeta().getDisplayName().replace(".", "").replace("§a§l", "").replace(cfg.getString("Pman.Money.currency"), ""));
					OfflinePlayer target = Bukkit.getOfflinePlayer(player.getOpenInventory().getTitle());
					MoneyGiveEvent giveEvent = new MoneyGiveEvent(player, target, moneytogive);
					Bukkit.getPluginManager().callEvent(giveEvent);
					if(!giveEvent.isCancelled()) {
						if(!Utils.getInstance().getEconomy().hasAccount(target)) {
							Utils.getInstance().getEconomy().createPlayerAccount(target);
						}
						Utils.getInstance().getEconomy().depositPlayer(target.getName(), giveEvent.getAmount());
						player.sendMessage("§a" + target.getName() + " §agot " + clickedItem.getItemMeta().getDisplayName() + " from you and has now " + Utils.getInstance().getEconomy().getBalance(target));
						if(target.isOnline()) {
							((Player) target).sendMessage("§aYou got from " + player.getName() + " " + clickedItem.getItemMeta().getDisplayName());
						}
					}
        		} // Money Give
        		if(moneysetinvs.contains(e.getInventory())) {
	        		OfflinePlayer target = Bukkit.getOfflinePlayer(player.getOpenInventory().getTitle());
	        		if(!Utils.getInstance().getEconomy().hasAccount(target)) {
						Utils.getInstance().getEconomy().createPlayerAccount(target);
	        		}
	        		player.sendMessage("§4CURRENTLY IN DEVELOPMENT!");
        		} // Money Set
        		if(moneytakeinvs.contains(e.getInventory())) {
					OfflinePlayer target = Bukkit.getOfflinePlayer(player.getOpenInventory().getTitle());
					double moneytotake = Double.parseDouble(clickedItem.getItemMeta().getDisplayName().replace(".", "").replace("§a§l", "").replace(cfg.getString("Pman.Money.currency"), ""));
					double bal = Utils.getInstance().getEconomy().getBalance(target);
					MoneyTakeEvent takeEvent = new MoneyTakeEvent(player, target, moneytotake, bal);
					if(!takeEvent.isCancelled()) {
						if(!Utils.getInstance().getEconomy().hasAccount(target))
							Utils.getInstance().getEconomy().createPlayerAccount(target);

						if(takeEvent.getCurrentBal() < takeEvent.getAmount()) {
							player.sendMessage(Utils.getInstance().replacePlaceHolders(player, messages.getString("Pman.Money.NotEnoughMoneyToTake"), Main.getPrefix()));
							return;
						}
						Utils.getInstance().getEconomy().withdrawPlayer(target.getName(), takeEvent.getAmount());
						player.sendMessage("§aYou have taken from " + target.getName() + " " + clickedItem.getItemMeta().getDisplayName() + " and now he has " + Utils.getInstance().getEconomy().getBalance(target) + "!");
						if(target.isOnline()) {
							((Player) target).sendMessage("§a" + player.getName() + " take " + clickedItem.getItemMeta().getDisplayName() + " §afrom you!");
						}
					}
        		} // Money Take
        	}
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.TeleportPlayerToYou")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		if(clickedItem.getItemMeta().getDisplayName().equals("§aOp")) {
        			if(!target.isOp()) {
        				target.setOp(true);
        				player.sendMessage("§6Made §c" + target.getName() + "§6 a server operator!");
        			} else {
        				player.sendMessage("§c" + target.getName() + " §6is already OP!");
        			}
        		}
        		if(clickedItem.getItemMeta().getDisplayName().equals("§cDeop")) {
        			if(target.isOp()) {
        				target.setOp(false);
        				player.sendMessage("§6Made §c" + target.getName() + "§6 no longer a server operator!");
        			} else {
        				player.sendMessage("§c" + target.getName() + " §6is not OP!");
        			}
        		}
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.END_CRYSTAL) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.TeleportPlayerToYou")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		target.teleport(player);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        } // Teleported Target to player
//        if(clickedItem.getType() == Material.RED_BED) {
//        	if(clickedItem.getItemMeta().getDisplayName().equals("§cPlayer Spawn Location")) {
//        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.ReSpawnLocation")) {
//        			Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
//        			target.setBedSpawnLocation(player.getLocation(), true);
//        			bans.set(target.getName() + ".Name", target.getName());
//        			bans.set(target.getName() + ".UUID", target.getUniqueId());
//        			bans.set(target.getName() + ".Adress.HostName", target.getAddress().getHostName());
//        			bans.set(target.getName() + ".Adress.HostString", target.getAddress().getHostString());
//        			bans.set(target.getName() + ".Adress.Adress", target.getAddress().getAddress());
//        			bans.set(target.getName() + ".Adress.Port", target.getAddress().getPort());
//        			bans.set(target.getName() + ".AllowFlight", target.getAllowFlight());
//        			bans.set(target.getName() + ".BedSpawnLocation", target.getBedSpawnLocation());
//        			bans.set(target.getName() + ".PickupItems", target.getCanPickupItems());
//        			bans.set(target.getName() + ".Name", target.getCustomName());
//        			bans.set(target.getName() + ".Name", target.getDisplayName());
//    				bans.set(target.getName() + ".Equipment", target.getEquipment());
//    				bans.set(target.getName() + ".EntityID", target.getEntityId());
//    				bans.set(target.getName() + ".EnderChest", target.getEnderChest());
//    				bans.set(target.getName() + ".Exp", target.getExp());
//    				bans.set(target.getName() + ".ExpToLevel", target.getExpToLevel());
//    				bans.set(target.getName() + ".TotalExp", target.getTotalExperience());
//    				bans.set(target.getName() + ".EyeHeight", target.getEyeHeight());
//    				bans.set(target.getName() + ".Facing", target.getFacing());
//    				bans.set(target.getName() + ".FallDistance", target.getFallDistance());
//    				bans.set(target.getName() + ".FireTicks", target.getFireTicks());
//    				bans.set(target.getName() + ".FirstPlayed", target.getFirstPlayed());
//    				bans.set(target.getName() + ".FlySpeed", target.getFlySpeed());
//    				bans.set(target.getName() + ".FoodLevel", target.getFoodLevel());
//    				bans.set(target.getName() + ".MaxFireTicks", target.getMaxFireTicks());
//    				bans.set(target.getName() + ".Gamemode", target.getGameMode());
//    				bans.set(target.getName() + ".Health", target.getHealth());
//    				bans.set(target.getName() + ".HealthScale", target.getHealthScale());
//    				bans.set(target.getName() + ".MaxHealth", target.getMaxHealth());
//    				bans.set(target.getName() + ".Height", target.getHeight());
//    				bans.set(target.getName() + ".Inventory", target.getInventory());
//    				bans.set(target.getName() + ".ItemInHand", target.getItemInHand());
//    				bans.set(target.getName() + ".ItemOnCursor", target.getItemOnCursor());
//    				bans.set(target.getName() + ".LastPlayed", target.getLastPlayed());
//    				bans.set(target.getName() + ".Level", target.getLevel());
//    				bans.set(target.getName() + ".Location", target.getLocation());
//    				bans.set(target.getName() + ".PlayerTime", target.getPlayerTime());
//    				bans.set(target.getName() + ".PlayerTimeOffset", target.getPlayerTimeOffset());
//    				bans.set(target.getName() + ".PlayerWeather", target.getPlayerWeather());
//    				bans.set(target.getName() + ".PortalCooldown", target.getPortalCooldown());
//    				bans.set(target.getName() + ".Pose", target.getPose());
//    				bans.set(target.getName() + ".Saturation", target.getSaturation());
//    				bans.set(target.getName() + ".Scoreboard", target.getScoreboard());
//    				bans.set(target.getName() + ".ScoreBoardTags", target.getScoreboardTags());
//    				bans.set(target.getName() + ".Server", target.getServer());
//    				bans.set(target.getName() + ".TicksLived", target.getTicksLived());
//    				bans.set(target.getName() + ".Type", target.getType());
//    				bans.set(target.getName() + ".WalkSpeed", target.getWalkSpeed());
//    				bans.set(target.getName() + ".Width", target.getWidth());
//    				bans.set(target.getName() + ".World", target.getWorld());
//    				bans.set("Test." + target.getName() + ".T", target.getPlayer());
//        			try {
//						bans.save(banfile);
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}
//        		} else {
//					player.sendMessage(nopermissionmessage);
//				}
//        	}
//        }
        if(clickedItem.getType() == Material.FLINT_AND_STEEL) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Burn")) {
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		int burnduration = cfg.getInt("Pman.Actions.BurnDuration");
        		target.setFireTicks(burnduration*20);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        } // Burn
        if(clickedItem.getType() == Material.DIAMOND_SWORD) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Kill Player")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Kill")) {
        			Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        			target.setHealth(0);
        			target.setFoodLevel(0);
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Kill
        if(clickedItem.getType() == Material.POTION) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§5§lPotions")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Potions")) {
        			Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        			PlayerManagerGUI.openPotionInv(player, target);
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Potions Menu
        if(clickedItem.getType() == Material.SPAWNER) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§6Spawn GUI")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        			Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        			PlayerManagerGUI.openSpawnerInv(player, target);
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Spawner Menu
        if(clickedItem.getType() == Material.COBBLESTONE) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2Inventory")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Inventoryview")) {
        			Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        			if(target == null || !target.isOnline()) {
        				player.sendMessage("§cDer Spieler ist nicht Online oder existiert nicht!");
        			}
        			player.openInventory(target.getInventory());
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Inventory
        if(clickedItem.getType() == Material.DIAMOND_CHESTPLATE) {
			Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2Armor")) {
        		if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Inventoryview")) {
        			if(target == null || !target.isOnline()) {
        				player.sendMessage("§cDer Spieler ist nicht Online oder existiert nicht!");
        			}
        			armor2 = Bukkit.createInventory(null, 9, "§5Armor");
        			armor2.setItem(0, target.getInventory().getHelmet());
        			armor2.setItem(1, target.getInventory().getChestplate());
        			armor2.setItem(2, target.getInventory().getLeggings());
        			armor2.setItem(3, target.getInventory().getBoots());
	        		armor2.setItem(8, createGuiItem(Material.BARRIER, "§4Back", "§aGet back to the Player Menu!"));
	        		armorinvs.add(armor2);
	        		player.openInventory(armor2);
        		} else {
					player.sendMessage(nopermissionmessage);
				}
        	}
        } // Armor
        if(clickedItem.getType() == Material.BEDROCK) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Ban")) {
	        	OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(player.getOpenInventory().getTitle());
	        	if(!target.getName().equals(player.getName())) {
		        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Ban.Ban")) {
						PlayerBanEvent banEvent = new PlayerBanEvent(player, (Player) target, Utils.getInstance().replacePlaceHolders(player,
								messages.getString("PlayerManager.BanReason"), ""),
								messages.getString("PlayerManager.BanSourceMessage"));
						Bukkit.getPluginManager().callEvent(banEvent);
						if(!banEvent.isCancelled()) {
							bans.set(target.getUniqueId().toString(), true);
							try {
								bans.save(banfile);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							Utils.getInstance().ban(player, target.getName(),
									Utils.getInstance().replacePlaceHolders(player, banEvent.getReason(), Main.getPrefix()),
									Utils.getInstance().replacePlaceHolders(player, banEvent.getSourcename(), Main.getPrefix()));
						}
		        	} else {
						player.sendMessage(nopermissionmessage);
					}
	        	} else {
					player.sendMessage(Utils.getInstance().replacePlaceHolders(player,
							messages.getString("PlayerManager.SelfBanningMessage"), Main.getPrefix()));
	        	}
				player.closeInventory();
	        	PlayerManagerGUI.openPlayerMenuSelector(player);
        	} else {
        		PlayerManagerGUI.openBanInv(player, Bukkit.getOfflinePlayer(player.getOpenInventory().getTitle()));
        	}
        } // Ban
        if(clickedItem.getType() == Material.BEACON) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aUnban")) {
	        	OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(player.getOpenInventory().getTitle());
	        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Ban.Unban")) {
					PlayerUnBanEvent unBanEvent = new PlayerUnBanEvent(player, (Player) target);
					Bukkit.getPluginManager().callEvent(unBanEvent);
					if(!unBanEvent.isCancelled()) {
						if(bans.contains(target.getUniqueId().toString())) {
							bans.set(target.getUniqueId().toString(), null);
							try {
								bans.save(banfile);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						Utils.getInstance().unban(player, target.getName());
					}
	        	} else {
					player.sendMessage(nopermissionmessage);
				}
	        	player.closeInventory();
	        	PlayerManagerGUI.openInv(player);
        	}
        } // Unban
        if(clickedItem.getType() == Material.RED_CONCRETE) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aBanned Players")) {
        		PlayerManagerGUI.openBannedPlayers(player);
        	}
        } // Banned Players
        if(clickedItem.getType() == Material.PISTON) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§5Kick")) {
	        	OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(player.getOpenInventory().getTitle());
	        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Kick")) {
					Utils.getInstance().kick(player, target.getName(), Utils.getInstance().replacePlaceHolders(player, messages.getString("PlayerManager.KickReason"), Main.getPrefix()), Utils.getInstance().replacePlaceHolders(player, messages.getString("PlayerManager.KickSourceMessage"), Main.getPrefix()));
	        	} else {
					player.sendMessage(nopermissionmessage);
				}
	        	player.closeInventory();
	        	PlayerManagerGUI.openInv(player);
        	}
        } // Kick
        if(clickedItem.getType() == Material.BAT_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Bat") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.BAT);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        } // Spawn Eggs...
        if(clickedItem.getType() == Material.BEE_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Bee") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.BEE);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.BLAZE_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Blaze") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.BLAZE);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.CAT_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Cat") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.CAT);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.CAVE_SPIDER_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Cave_Spider") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		target.getWorld().spawnEntity(loc, EntityType.CAVE_SPIDER);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.CHICKEN_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Chicken") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		target.getWorld().spawnEntity(loc, EntityType.CHICKEN);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.COD_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Cod") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.COD);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.COW_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Cow") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.COW);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.CREEPER_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Creeper") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.CREEPER);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.DOLPHIN_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Dolphin") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.DOLPHIN);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.DONKEY_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Donkey") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.DONKEY);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.DROWNED_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Drowned") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.DROWNED);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.ELDER_GUARDIAN_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Elder_Guardian") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.ELDER_GUARDIAN);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.ENDERMAN_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Enderman") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.ENDERMAN);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.ENDERMITE_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Endermite") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.ENDERMITE);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.EVOKER_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Evoker") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.EVOKER);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.FOX_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Fox") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.FOX);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.GHAST_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Ghast") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.GHAST);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.GUARDIAN_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Guardian") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.GUARDIAN);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.HORSE_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Horse") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.HORSE);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.HUSK_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Husk") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.HUSK);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.LLAMA_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Llama") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.LLAMA);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.MAGMA_CUBE_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Magma_Cube") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.MAGMA_CUBE);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.MOOSHROOM_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Mooshroom") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.MUSHROOM_COW);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.MULE_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Mule") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.MULE);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.OCELOT_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Ocelot") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.OCELOT);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.PANDA_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Panda") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.PANDA);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.PARROT_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Parrot") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.PARROT);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.PHANTOM_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Phantom") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.PHANTOM);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.PIG_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Pig") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.PIG);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.PILLAGER_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Pillager") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.PILLAGER);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.POLAR_BEAR_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Polar_Bear") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.POLAR_BEAR);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.PUFFERFISH_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Pufferfish") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.PUFFERFISH);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.RABBIT_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Rabbit") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.RABBIT);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.RAVAGER_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Ravager") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.RAVAGER);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.SALMON_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Salmon") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SALMON);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.SHEEP_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Sheep") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SHEEP);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.SHULKER_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Shulker") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SHULKER);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.SILVERFISH_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Silverfish") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		target.getWorld().spawnEntity(loc, EntityType.SILVERFISH);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.SKELETON_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Skeleton") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SKELETON);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.SKELETON_HORSE_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Skeleton_Horse") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SKELETON_HORSE);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.SLIME_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Slime") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SLIME);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.SPIDER_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Spider") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SPIDER);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.SQUID_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Squid") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SQUID);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.STRAY_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Stray") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.STRAY);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.TROPICAL_FISH_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Tropical_Fish") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.TROPICAL_FISH);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.TURTLE_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Turtle") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.TURTLE);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.VEX_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Vex") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.VEX);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.VILLAGER_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Villager") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.VINDICATOR_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Vindicator") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.VINDICATOR);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.WITCH_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Witch") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.WITCH);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.WOLF_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Wolf") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.WOLF);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        }
        if(clickedItem.getType() == Material.ZOMBIE_SPAWN_EGG) {
        	if(player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Zombie") == true) {
        			player.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(player.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        	} else {
				player.sendMessage(nopermissionmessage);
			}
        } // End
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory() == playermanager2 && e.getInventory() == actions2 && e.getInventory() == armor2 && e.getInventory() == playermanactionselector2 && e.getInventory() == spawnerinv2 && e.getInventory() == potioninv2 && e.getInventory() == money2 && baninvs.contains(e.getInventory()) && kickinvs.contains(e.getInventory())) {
          e.setCancelled(true);
        }
    }
    
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		if(p.isBanned() && bans.contains(p.getUniqueId().toString()) && bans.getBoolean(p.getUniqueId().toString()) == true) {
			e.setKickMessage("§cDu wurdest vom Server gebannt!\n" + 
							"\n" + 
							"§3Von: §e" + Bukkit.getBanList(org.bukkit.BanList.Type.NAME).getBanEntry(p.getName()).getSource().toString() + "\n" + 
							"\n" + 
							"§3Reason: §e" + Bukkit.getBanList(org.bukkit.BanList.Type.NAME).getBanEntry(p.getName()).getReason().toString() + "\n" + 
							"\n" + 
							"§3Permanently banned!" + "\n" + 
							"\n" + 
							"§3Du kannst §c§nkeinen§3 Entbannungsantrag stellen!");
		}
	}
}