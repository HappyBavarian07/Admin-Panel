package de.happybavarian07.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
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

import com.azortis.protocolvanish.api.VanishAPI;

import de.happybavarian07.api.Fireworkgenerator;
import de.happybavarian07.main.Main;
import de.happybavarian07.main.Utils;

public class PlayerManagerGUI implements Listener {
	
	Main plugin;
	
	static FileConfiguration cfg;
    private static Inventory playermanager2;
	public static List<Inventory> playermanagerinvs = new ArrayList<Inventory>();
    private static Inventory actions2;
	public static List<Inventory> actionsinvs = new ArrayList<Inventory>();
    private static Inventory armor2;
	public static List<Inventory> armorinvs = new ArrayList<Inventory>();
    private static Inventory playermanactionselector2;
	public static List<Inventory> playermanactionselectorinvs = new ArrayList<Inventory>();
    private static Inventory money2;
	public static List<Inventory> moneyinvs = new ArrayList<Inventory>();
    private static Inventory baninv2;
	public static List<Inventory> baninvs = new ArrayList<Inventory>();
    private static Inventory kickinv2;
	public static List<Inventory> kickinvs = new ArrayList<Inventory>();
    private static Inventory spawnerinv2;
	public static List<Inventory> spawnerinvs = new ArrayList<Inventory>();
    private static Inventory potioninv2;
	public static List<Inventory> potioninvs = new ArrayList<Inventory>();
    private static Inventory moneygiveinv2;
	public static List<Inventory> moneygiveinvs = new ArrayList<Inventory>();
    private static Inventory moneysetinv2;
	public static List<Inventory> moneysetinvs = new ArrayList<Inventory>();
    private static Inventory moneytakeinv2;
	public static List<Inventory> moneytakeinvs = new ArrayList<Inventory>();
    FileConfiguration messages;
    
    public PlayerManagerGUI(Main main, FileConfiguration messages2, FileConfiguration config) {
    	
    	cfg = config;
    	messages = messages2;
    	main = plugin;
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
    	
    	potioninv.setItem(41, createGuiItem(Material.BEACON, "§a§lLevel", "§cChange the Time by Clicking"));
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
    	playermanager.setItem(49, createGuiItem(Material.BARRIER, "§4Close", "§cClose the Menu!"));
    	Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			
			@SuppressWarnings("static-access")
			@Override
			public void run() {
		    	ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		    	SkullMeta meta = (SkullMeta) head.getItemMeta();
		    	OfflinePlayer[] onlineplayer = Bukkit.getOfflinePlayers();
		    	for(int i = 0; i < Bukkit.getOfflinePlayers().length; i++) {
		    		if(onlineplayer[i].hasPlayedBefore() && onlineplayer[i].isOnline()) {
			    		meta.setOwningPlayer(onlineplayer[i]);
			    		meta.setDisplayName(onlineplayer[i].getName());
			    		List<String> lore = new ArrayList<>();
			    		lore.add("§4Health: §3" + onlineplayer[i].getPlayer().getHealth());
			    		lore.add("§6Food: §9" + onlineplayer[i].getPlayer().getFoodLevel());
			    		lore.add("§2Money: §6" + Main.plugin.eco.getBalance(onlineplayer[i]));
			    		lore.add("§3Gamemode: §6" + onlineplayer[i].getPlayer().getGameMode());
			    		lore.add("§5Adresse: §4" + onlineplayer[i].getPlayer().getAddress());
			    		meta.setLore(lore);
			    		head.setItemMeta(meta);
			    		if(i != 45) {
				    		playermanager.setItem(i, head);
			    		} else {
			    			return;
			    		}
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
		if(cfg.getBoolean("Panel.PlaySoundsWhenOponed") == true) {
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
		if(cfg.getBoolean("Panel.PlaySoundsWhenOponed") == true) {
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
		if(cfg.getBoolean("Panel.PlaySoundsWhenOponed") == true) {
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
		if(cfg.getBoolean("Panel.PlaySoundsWhenOponed") == true) {
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
		if(cfg.getBoolean("Panel.PlaySoundsWhenOponed") == true) {
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
		if(cfg.getBoolean("Panel.PlaySoundsWhenOponed") == true) {
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
		if(cfg.getBoolean("Panel.PlaySoundsWhenOponed") == true) {
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
		if(cfg.getBoolean("Panel.PlaySoundsWhenOponed") == true) {
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
		if(cfg.getBoolean("Panel.PlaySoundsWhenOponed") == true) {
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
    public static void openBanInv(final HumanEntity ent, Player target) {
		baninv2 = Bukkit.createInventory(null, 9*3, target.getName());
    	for(int i = 0; i < baninv2.getSize(); i++) {
    		baninv2.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
    	}
    	baninv2.setItem(4, createGuiItem(Material.BEDROCK, "§4Ban", "§4§lBanne den Spieler permanent vom Server!"));
    	baninv2.setItem(13, createGuiItem(Material.BEACON, "§aUnban", "§a§lEntbanne den Spieler!"));
    	baninv2.setItem(22, createGuiItem(Material.BARRIER, "§4Back", "Click to go back to the Action Selector"));
		baninvs.add(baninv2);
		ent.openInventory(baninv2);
		if(cfg.getBoolean("Panel.PlaySoundsWhenOponed") == true) {
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
		if(cfg.getBoolean("Panel.PlaySoundsWhenOponed") == true) {
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
	@SuppressWarnings("deprecation")
	@EventHandler
    public void onInventoryClick(final InventoryClickEvent e) throws InterruptedException {
        if (!playermanagerinvs.contains(e.getInventory()) && !actionsinvs.contains(e.getInventory()) && !armorinvs.contains(e.getInventory()) && !playermanactionselectorinvs.contains(e.getInventory()) && !spawnerinvs.contains(e.getInventory()) && !potioninvs.contains(e.getInventory()) && !moneyinvs.contains(e.getInventory()) && !moneygiveinvs.contains(e.getInventory()) && !moneysetinvs.contains(e.getInventory()) && !moneytakeinvs.contains(e.getInventory()) && !baninvs.contains(e.getInventory()) && !kickinvs.contains(e.getInventory())) return;
        
        e.setCancelled(true);
        final ItemStack clickedItem = e.getCurrentItem();
        
        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        final Player p = (Player) e.getWhoClicked();
        
        // Using slots click is a best option for your inventory click's
        if(clickedItem.getType() == Material.BARRIER) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§cBack")) {
		        if(p.hasPermission("AdminPanel.Button.Back")) {
		        	PlayerManagerGUI.openInv(p);
		        } else {
					p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.Button.Back").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
				}
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Back")) {
		        if(p.hasPermission("AdminPanel.Button.Back")) {
		        	PlayerManagerGUI.openPlayerMenuSelector(p);
		        } else {
					p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.Button.Back").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
				}
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Back!")) {
		        if(p.hasPermission("AdminPanel.Button.Back")) {
		        	Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
		        	PlayerManagerGUI.openMoneyInv(p, target);
		        } else {
					p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.Button.Back").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
				}
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lBack")) {
		        if(p.hasPermission("AdminPanel.Button.Back")) {
		        	PlayerManagerGUI.openActions(p);
		        } else {
					p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.Button.Back").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
				}
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Close")) {
    	        if(p.hasPermission("AdminPanel.Button.Close")) {
    	        	ExampleGui.openInv(p);
    	        } else {
    				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.Button.Close").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
    			}
        	}
        }
        if(clickedItem.getType() == Material.DIAMOND_SWORD) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aActions")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.OpenMenu.Actions")) {
        			PlayerManagerGUI.openActions(p);
        		} else {
    				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.OpenMenu.Actions").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
    			}
        	}
        }
        if(clickedItem.getType() == Material.BEACON) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§a§lLevel")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.Actions.Potions")) {
        			if(!(clickedItem.getAmount() == 20)) {
        				clickedItem.setAmount(clickedItem.getAmount() + 1);
        			} else {
        				clickedItem.setAmount(1);
        			}
        		}
        	}
        }
        if(clickedItem.getType() == Material.CLOCK) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§a§lTime")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.Actions.Potions")) {
        			if(!(clickedItem.getAmount() == 64)) {
        				clickedItem.setAmount(clickedItem.getAmount() + 1);
        			} else {
        				clickedItem.setAmount(1);
        			}
        		}
        	}
        }
        if(clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
    		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
    		p.performCommand("minecraft:effect clear " + target.getName());
        }
        if(clickedItem.getType() == Material.POTION) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lAbsorption")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lBad Omen")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lBlindness")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lConduit Power")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lDolphins Grace")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lFire Resistance")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lGlowing")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lHaste")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lHealth Boost")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lHero of the Village")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lHunger")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lInstant Damage")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lInstant Health")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lInvisibility")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lJump Boost")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lLevitation")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lLuck")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lMining Fatigue")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lNausea")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lNight Vision")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lPoison")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lRegeneration")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lResistance")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lSaturation")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lSlow Falling")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lSlowness")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lSpeed")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lStrength")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lUnluck")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2§lWater Breathing")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lWeakness")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4§lWither")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().replace("§2§l", "").replace("§4§l", "")), (potioninv2.getItem(39).getAmount() * 1200), (potioninv2.getItem(41).getAmount()) - 1));
        	}
        }
        if(clickedItem.getType() == Material.PLAYER_HEAD) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Open") && !clickedItem.getItemMeta().getDisplayName().equals(p.getName()) || p.hasPermission("test.test.test")) {
        		if(!Bukkit.getPlayerExact(clickedItem.getItemMeta().getDisplayName().replace("§e§l", "")).hasPermission("AdminPanel.Bypass.AffectedByPlayerManager")) {
        			initializeItems(playermanager2);
        			actions2 = Bukkit.createInventory(null, 9*6, clickedItem.getItemMeta().getDisplayName().replace("§e§l", ""));
        			playermanactionselector2 = Bukkit.createInventory(null, 9*3, clickedItem.getItemMeta().getDisplayName().replace("§e§l", ""));
	            	for(int i = 0; i < actions2.getSize(); i++) {
	            		actions2.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
	            	}
	            	for(int i = 0; i < playermanactionselector2.getSize(); i++) {
	            		playermanactionselector2.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
	            	}
	            	Player target = Bukkit.getPlayerExact(clickedItem.getItemMeta().getDisplayName().replace("§e§l", ""));
	            	actions2.setItem(47, createGuiItem(Material.COBBLESTONE, "§2Inventory", "§aZeigt das Inventar des Spielers an!"));
	            	playermanactionselector2.setItem(4, clickedItem);
	            	actions2.setItem(4, clickedItem);
	            	actions2.setItem(21, createGuiItem(Material.SPAWNER, "§6Spawn GUI", "§aSpawn Mobs bei dem Spieler!"));
	            	actions2.setItem(19, createGuiItem(Material.POTION, "§5§lPotions", "§aGebe dem Spieler einen Trankeffekt!", "§4Es funktioniert momentan noch nicht", "§4mit allen Effekten!", "§4Aber ich sitze schon dran!"));
	            	actions2.setItem(15, createGuiItem(Material.COOKED_BEEF, "§dFeed", "§aFüttere den Spieler!"));
	            	actions2.setItem(13, createGuiItem(Material.DIAMOND_SWORD, "§4Kill Player", "§aTöte den Spieler"));
	            	actions2.setItem(11, createGuiItem(Material.GOLDEN_APPLE, "§e§lHeal", "§aHeile den Spieler!"));
	            	playermanactionselector2.setItem(26, createGuiItem(Material.BARRIER, "§cBack", "Click to go back to the Player Manager"));
	            	Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
						
						@Override
						public void run() {
			            	if(target.getGameMode() == GameMode.ADVENTURE) {
				            	actions2.setItem(23, createGuiItem(Material.GRASS_BLOCK, "§1Adventure", "§aKlicke um den Gamemode zu wechseln!"));
			            	}
			            	if(target.getGameMode() == GameMode.CREATIVE) {
				            	actions2.setItem(23, createGuiItem(Material.BRICKS, "§1Creative", "§aKlicke um den Gamemode zu wechseln!"));
			            	}
			            	if(target.getGameMode() == GameMode.SPECTATOR) {
				            	actions2.setItem(23, createGuiItem(Material.SPLASH_POTION, "§1Spectator", "§aKlicke um den Gamemode zu wechseln!"));
			            	}
			            	if(target.getGameMode() == GameMode.SURVIVAL) {
				            	actions2.setItem(23, createGuiItem(Material.DIRT, "§1Survival", "§aKlicke um den Gamemode zu wechseln!"));
			            	}
			            	if(Main.isProtocolVanishEnabled() == "ProtocolVanish" && cfg.getBoolean("Plugin.ProtocolVanish") == true) {
				            	if(VanishAPI.isVanished(p)) {
					            	actions2.setItem(25, createGuiItem(Material.FEATHER, "§b§lVanish §4CURRENTLY IN DEVELOPMENT!1", "§aKlicke um den Vanish", "§azu deaktivieren!", "Vanish-Plugin: ProtocolVanish"));
				            	} else {
					            	actions2.setItem(25, createGuiItem(Material.FEATHER, "§c§lVanish §4CURRENTLY IN DEVELOPMENT!2", "§aKlicke um den Vanish", "§azu aktivieren!", "Vanish-Plugin: ProtocolVanish"));
				            	}
			            	} else if(Main.isProtocolVanishEnabled() == "SuperVanish-" + cfg.getInt("Plugin.SuperVanishVersion") && cfg.getBoolean("Plugin.SuperVanish") == true) {
				            	if(de.myzelyam.api.vanish.VanishAPI.isInvisible(p)) {
					            	actions2.setItem(25, createGuiItem(Material.FEATHER, "§b§lVanish §4CURRENTLY IN DEVELOPMENT!3", "§aKlicke um den Vanish", "§azu deaktivieren!", "Vanish-Plugin: SuperVanish"));
				            	} else {
					            	actions2.setItem(25, createGuiItem(Material.FEATHER, "§c§lVanish §4CURRENTLY IN DEVELOPMENT!4", "§aKlicke um den Vanish", "§azu aktivieren!", "Vanish-Plugin: SuperVanish"));
				            	}
			            	} else {
			            		
			            	}
						}
					}, 0L, 20L);
	            	actions2.setItem(31, createGuiItem(Material.TNT, "§c§lTroll Menu §4§lCurrently in Development!"));
	            	actions2.setItem(43, createGuiItem(Material.PAPER, "§aOp", "§aMache den Spieler zum Operator!"));
	            	actions2.setItem(49, createGuiItem(Material.PAPER, "§cDeop", "§aNehme dem Spieler Operator weg!"));
	            	actions2.setItem(41, createGuiItem(Material.END_CRYSTAL, "§1Teleport the Player to You", "§aTeleportiere den Spieler zu dir!"));
	            	actions2.setItem(39, createGuiItem(Material.ENDER_PEARL, "§2Tele§2§lrt to Player", "§aTeleportiere dich zu dem Spieler!"));
	            	actions2.setItem(37, createGuiItem(Material.FIREWORK_ROCKET, "§1F§2i§3r§4e§5w§6o§7r§8k", "§aDer Spieler feuert eine", "§aFeuerwerksrakete aus seinem Kopf!"));
	            	actions2.setItem(33, createGuiItem(Material.BLAZE_ROD, "§eLightning", "§cSchieße einen Blitz auf den Spieler!"));
	            	actions2.setItem(29, createGuiItem(Material.FLINT_AND_STEEL, "§4§lBurn Player", "§aKlicke um den Spieler anzuzünden!"));
	            	actions2.setItem(51, createGuiItem(Material.DIAMOND_CHESTPLATE, "§2Armor", "§aZeigt dir die Armor des Spielers"));
	            	actions2.setItem(53, createGuiItem(Material.BARRIER, "§4Back", "Zurück zum Player Menu!"));
	            	playermanactionselector2.setItem(10, createGuiItem(Material.DIAMOND_SWORD, "§aActions", "§aClick to open the Actions Menu!"));
	            	playermanactionselector2.setItem(12, createGuiItem(Material.PAPER, "§aMoney", "§aClick to open the Money Menu!"));
	            	playermanactionselector2.setItem(14, createGuiItem(Material.BLACK_TERRACOTTA, "§aKick Player", "§aClick to open the Kick Menu!"));
	            	playermanactionselector2.setItem(16, createGuiItem(Material.BEDROCK, "§aBan Player", "§aClick to open the Ban Menu!"));
	            	actionsinvs.add(actions2);
	            	armorinvs.add(armor2);
	            	
	            	playermanactionselectorinvs.add(playermanactionselector2);
	            	PlayerManagerGUI.openPlayerMenuSelector(p);
        		} else {
        			p.sendMessage("§cDu kannst mit diesem Spieler nichts machen weil er einen Bypass für den PlayerManager hat!");
        		}
        	} else {
        		if(!p.hasPermission("AdminPanel.PlayerManager.Actions.Open")) {
        			p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.Open").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
        		}
        		if(clickedItem.getItemMeta().getDisplayName().equals(p.getName())) {
        			p.sendMessage("§cDu darfst nichts dich selber auswählen");
        		}
			}
        }
        if(clickedItem.getType() == Material.BLACK_TERRACOTTA) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aKick Player")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.OpenMenu.Kick")) {
            		PlayerManagerGUI.openKickInv(p, Bukkit.getPlayerExact(p.getOpenInventory().getTitle()));
        		}
        	}
        }
        if(clickedItem.getType() == Material.BLAZE_ROD) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§eLightning")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.Actions.Lightning")) {
        			Bukkit.getPlayerExact(p.getOpenInventory().getTitle()).getWorld().strikeLightning(Bukkit.getPlayerExact(p.getOpenInventory().getTitle()).getLocation());
        		} else {
    				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.Lightning").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
    			}
        	}
        }
        if(clickedItem.getType() == Material.GRASS_BLOCK || clickedItem.getType() == Material.BRICKS || clickedItem.getType() == Material.SPLASH_POTION || clickedItem.getType() == Material.DIRT) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Gamemode")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
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
				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.Gamemode").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
			}
        }
        if(clickedItem.getType() == Material.GOLDEN_APPLE) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§e§lHeal")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.Actions.Heal")) {
        			Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        			target.setHealth(cfg.getDouble("Pman.Actions.HealthHealAmount"));
        		} else {
    				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.Heal").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
    			}
        	}
        }
        if(clickedItem.getType() == Material.COOKED_BEEF) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§dFeed")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.Actions.Feed")) {
        			Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        			target.setFoodLevel(cfg.getInt("Pman.Actions.FoodLevelFillAmount"));
        		} else {
    				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.Feed").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
    			}
        	}
        }
        if(clickedItem.getType() == Material.FIREWORK_ROCKET) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Firework")) {
        		Fireworkgenerator fwg = new Fireworkgenerator(Main.plugin);
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		fwg.setLocation(target.getLocation());
        		fwg.setPower(1);
        		fwg.setEffect(FireworkEffect.builder().withColor(Color.RED).withColor(Color.AQUA).withColor(Color.YELLOW).withColor(Color.BLUE).withColor(Color.GREEN).with(Type.BALL_LARGE).withFlicker().build());
        		fwg.setLifeTime(30);
        		fwg.spawn();
        	} else {
				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.Firework").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
			}
        }
        if(clickedItem.getType() == Material.ENDER_PEARL) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.TeleportYouToPlayer")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		p.teleport(target);
        	} else {
				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.TeleportYouToPlayer").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
			}
        }
        if(clickedItem.getType() == Material.GREEN_WOOL) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aGive")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.Money.Give")) {
        			Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
					PlayerManagerGUI.openMoneyGiveInv(p, target);
        		}
        	}
        }
        if(clickedItem.getType() == Material.BOOK) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aSet")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.Money.Set")) {
        			Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
					PlayerManagerGUI.openMoneySetInv(p, target);
        		}
        	}
        }
        if(clickedItem.getType() == Material.RED_WOOL) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aTake")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.Money.Take")) {
        			Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
					PlayerManagerGUI.openMoneyTakeInv(p, target);
        		}
        	}
        }
        if(clickedItem.getType() == Material.PAPER) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aMoney")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.OpenMenu.Money")) {
        			Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        			PlayerManagerGUI.openMoneyInv(p, target);
        		}
        	} else {
        		if(moneygiveinvs.contains(e.getInventory())) {
	        		OfflinePlayer target = Bukkit.getOfflinePlayer(p.getOpenInventory().getTitle());
	        		if(!Main.eco.hasAccount(target)) {
	        			Main.eco.createPlayerAccount(target);
	        		}
	        		double moneytogive = Double.parseDouble(clickedItem.getItemMeta().getDisplayName().replace(".", "").replace("§a§l", "").replace(cfg.getString("Pman.Money.currency"), ""));
	    			Main.eco.depositPlayer(target.getName(), moneytogive);
	        		p.sendMessage("§a" + target.getName() + " §agot " + clickedItem.getItemMeta().getDisplayName() + " from you and has now " + Main.eco.getBalance(target));
	        		if(target.isOnline()) {
	        			((Player) target).sendMessage("§aYou got from " + p.getName() + " " + clickedItem.getItemMeta().getDisplayName());
	        		}
        		}
        		if(moneysetinvs.contains(e.getInventory())) {
	        		OfflinePlayer target = Bukkit.getOfflinePlayer(p.getOpenInventory().getTitle());
	        		if(!Main.eco.hasAccount(target)) {
	        			Main.eco.createPlayerAccount(target);
	        		}
	        		p.sendMessage("§4CURRENTLY IN DEVELOPMENT!");
        		}
        		if(moneytakeinvs.contains(e.getInventory())) {
	        		OfflinePlayer target = Bukkit.getOfflinePlayer(p.getOpenInventory().getTitle());
	        		if(!Main.eco.hasAccount(target)) {
	        			Main.eco.createPlayerAccount(target);
	        		}
	        		double moneytotake = Double.parseDouble(clickedItem.getItemMeta().getDisplayName().replace(".", "").replace("§a§l", "").replace(cfg.getString("Pman.Money.currency"), ""));
	        		double bal = Main.eco.getBalance(target);
					if(Main.eco.getBalance(target) < moneytotake) {
	        			p.sendMessage(messages.getString("Pman.Money.NotEnoughMoneyToTake").replace("%targetplayer%", target.getName()).replace("%player%", p.getName()).replace("%balance%", String.valueOf(bal)).replace('&', '§').replace("%prefix%", cfg.getString("Plugin.Prefix")));
	        			return;
					}
	    			Main.eco.withdrawPlayer(target.getName(), moneytotake);
	        		p.sendMessage("§aYou have token from " + target.getName() + " " + clickedItem.getItemMeta().getDisplayName() + " and now he has " + Main.eco.getBalance(target) + "!");
	        		if(target.isOnline()) {
	        			((Player) target).sendMessage("§a" + p.getName() + " take " + clickedItem.getItemMeta().getDisplayName() + " §afrom you!");
	        		}
        		}
        	}
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.TeleportPlayerToYou")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		if(clickedItem.getItemMeta().getDisplayName().equals("§aOp")) {
        			if(!target.isOp()) {
        				target.setOp(true);
        				p.sendMessage("§6Made §c" + target.getName() + "§6 an Op!");
        			} else {
        				p.sendMessage("§c" + target.getName() + " §6ist bereits OP!");
        			}
        		}
        		if(clickedItem.getItemMeta().getDisplayName().equals("§cDeop")) {
        			if(target.isOp()) {
        				target.setOp(false);
        				p.sendMessage("§6Made §c" + target.getName() + "§6 an Player!");
        			} else {
        				p.sendMessage("§c" + target.getName() + " §6ist nicht OP!");
        			}
        		}
        	} else {
				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.Spawner").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
			}
        }
        if(clickedItem.getType() == Material.END_CRYSTAL) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.TeleportPlayerToYou")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		target.teleport(p);
        	} else {
				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.TeleportPlayerToYou").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
			}
        }
        if(clickedItem.getType() == Material.FLINT_AND_STEEL) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Burn")) {
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		int burnduration = cfg.getInt("Pman.Actions.BurnDuration");
        		target.setFireTicks(burnduration*20);
        	} else {
    			p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.Burn").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
    		}
        }
        if(clickedItem.getType() == Material.DIAMOND_SWORD) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Kill Player")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.Actions.Kill")) {
        			Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        			target.setHealth(0);
        			target.setFoodLevel(0);
        		} else {
    				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.Kill").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
    			}
        	}
        }
        if(clickedItem.getType() == Material.POTION) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§5§lPotions")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.Actions.Potions")) {
        			Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        			PlayerManagerGUI.openPotionInv(p, target);
        		} else {
    				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.Potions").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
    			}
        	}
        }
        if(clickedItem.getType() == Material.SPAWNER) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§6Spawn GUI")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        			Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        			PlayerManagerGUI.openSpawnerInv(p, target);
        		} else {
    				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.Spawner").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
    			}
        	}
        }
        if(clickedItem.getType() == Material.COBBLESTONE) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2Inventory")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.Actions.Inventoryview")) {
        			Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        			if(target == null || !target.isOnline()) {
        				p.sendMessage("§cDer Spieler ist nicht Online oder existiert nicht!");
        			}
        			p.openInventory(target.getInventory());
        		} else {
    				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.Inventoryview").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
    			}
        	}
        }
        if(clickedItem.getType() == Material.DIAMOND_CHESTPLATE) {
			Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2Armor")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.Actions.Inventoryview")) {
        			if(target == null || !target.isOnline()) {
        				p.sendMessage("§cDer Spieler ist nicht Online oder existiert nicht!");
        			}
        			armor2 = Bukkit.createInventory(null, 9, "§5Armor");
        			armor2.setItem(0, target.getInventory().getHelmet());
        			armor2.setItem(1, target.getInventory().getChestplate());
        			armor2.setItem(2, target.getInventory().getLeggings());
        			armor2.setItem(3, target.getInventory().getBoots());
	        		armor2.setItem(8, createGuiItem(Material.BARRIER, "§4Back", "§aGet back to the Player Menu!"));
	        		armorinvs.add(armor2);
	        		p.openInventory(armor2);
        		} else {
    				p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.Inventoryview").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
    			}
        	}
        }
        if(clickedItem.getType() == Material.BEDROCK) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§4Ban")) {
	        	OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(p.getOpenInventory().getTitle());
	        	if(player.getName() != p.getName()) {
		        	if(p.hasPermission("AdminPanel.PlayerManager.Ban.Ban")) {
		        		Utils.ban(p, player.getName(), messages.getString("PlayerManager.BanReason").replace("%targetplayer%", player.getName()).replace("%player%", p.getName()).replace('&', '§').replace("%prefix%", cfg.getString("Plugin.Prefix")), messages.getString("PlayerManager.BanSourceMessage").replace("%player%", p.getName()).replace("%targetplayer%", player.getName()).replace("%prefix%", cfg.getString("Plugin.Prefix")));
		        	} else {
						p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Ban.Ban").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
					}
	        	} else {
	        		p.sendMessage(messages.getString("SelfBanningMessage").replace('&', '§').replace("%prefx%", cfg.getString("Plugin.Prefix")).replace("%player%", p.getName()).replace("%targetplayer%", player.getName()));
	        	}
	        	p.closeInventory();
	        	PlayerManagerGUI.openPlayerMenuSelector(p);
        	} else {
        		PlayerManagerGUI.openBanInv(p, Bukkit.getPlayerExact(p.getOpenInventory().getTitle()));
        	}
        }
        if(clickedItem.getType() == Material.BEACON) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§aUnban")) {
	        	OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(p.getOpenInventory().getTitle());
	        	if(p.hasPermission("AdminPanel.PlayerManager.Ban.Unban")) {
	        		Utils.unban(p, player.getName());
	        	} else {
					p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Ban.Unban").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
				}
	        	p.closeInventory();
	        	PlayerManagerGUI.openInv(p);
        	}
        }
        if(clickedItem.getType() == Material.PISTON) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§5Kick")) {
	        	OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(p.getOpenInventory().getTitle());
	        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Kick")) {
	        		Utils.kick(p, player.getName(), messages.getString("PlayerManager.KickReason").replace("%targetplayer%", player.getName()).replace("%player%", p.getName()).replace('&', '§').replace("%prefix%", cfg.getString("Plugin.Prefix")), messages.getString("PlayerManager.KickSourceMessage").replace("%player%", p.getName()).replace("%targetplayer%", player.getName()).replace("%prefix%", cfg.getString("Plugin.Prefix")));
	        	} else {
					p.sendMessage(messages.getString("No-Permission-Message").replace('&', '§').replace("%permission%", "AdminPanel.PlayerManager.Actions.Unban").replace("%prefix%", cfg.getString("Plugin.Prefix").replace('&', '§')).replace("%player%", p.getName()));
				}
	        	p.closeInventory();
	        	PlayerManagerGUI.openInv(p);
        	}
        }
        if(clickedItem.getType() == Material.STONE) {
        	if(clickedItem.getItemMeta().getDisplayName().equals("§2Gamemode")) {
        		if(p.hasPermission("AdminPanel.PlayerManager.Actions.Gamemode")) {
        			Player target = Bukkit.getServer().getPlayerExact(p.getOpenInventory().getTitle());
        			target.setGameMode(GameMode.CREATIVE);
        		}
        	}
        }
        if(clickedItem.getType() == Material.BAT_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Bat") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.BAT);
        	}
        }
        if(clickedItem.getType() == Material.BEE_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Bee") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.BEE);
        	}
        }
        if(clickedItem.getType() == Material.BLAZE_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Blaze") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.BLAZE);
        	}
        }
        if(clickedItem.getType() == Material.CAT_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Cat") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.CAT);
        	}
        }
        if(clickedItem.getType() == Material.CAVE_SPIDER_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Cave_Spider") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		target.getWorld().spawnEntity(loc, EntityType.CAVE_SPIDER);
        	}
        }
        if(clickedItem.getType() == Material.CHICKEN_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Chicken") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		target.getWorld().spawnEntity(loc, EntityType.CHICKEN);
        	}
        }
        if(clickedItem.getType() == Material.COD_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Cod") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.COD);
        	}
        }
        if(clickedItem.getType() == Material.COW_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Cow") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.COW);
        	}
        }
        if(clickedItem.getType() == Material.CREEPER_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Creeper") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.CREEPER);
        	}
        }
        if(clickedItem.getType() == Material.DOLPHIN_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Dolphin") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.DOLPHIN);
        	}
        }
        if(clickedItem.getType() == Material.DONKEY_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Donkey") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.DONKEY);
        	}
        }
        if(clickedItem.getType() == Material.DROWNED_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Drowned") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.DROWNED);
        	}
        }
        if(clickedItem.getType() == Material.ELDER_GUARDIAN_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Elder_Guardian") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.ELDER_GUARDIAN);
        	}
        }
        if(clickedItem.getType() == Material.ENDERMAN_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Enderman") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.ENDERMAN);
        	}
        }
        if(clickedItem.getType() == Material.ENDERMITE_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Endermite") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.ENDERMITE);
        	}
        }
        if(clickedItem.getType() == Material.EVOKER_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Evoker") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.EVOKER);
        	}
        }
        if(clickedItem.getType() == Material.FOX_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Fox") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.FOX);
        	}
        }
        if(clickedItem.getType() == Material.GHAST_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Ghast") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.GHAST);
        	}
        }
        if(clickedItem.getType() == Material.GUARDIAN_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Guardian") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.GUARDIAN);
        	}
        }
        if(clickedItem.getType() == Material.HORSE_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Horse") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.HORSE);
        	}
        }
        if(clickedItem.getType() == Material.HUSK_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Husk") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.HUSK);
        	}
        }
        if(clickedItem.getType() == Material.LLAMA_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Llama") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.LLAMA);
        	}
        }
        if(clickedItem.getType() == Material.MAGMA_CUBE_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Magma_Cube") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.MAGMA_CUBE);
        	}
        }
        if(clickedItem.getType() == Material.MOOSHROOM_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Mooshroom") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.MUSHROOM_COW);
        	}
        }
        if(clickedItem.getType() == Material.MULE_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Mule") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.MULE);
        	}
        }
        if(clickedItem.getType() == Material.OCELOT_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Ocelot") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.OCELOT);
        	}
        }
        if(clickedItem.getType() == Material.PANDA_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Panda") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.PANDA);
        	}
        }
        if(clickedItem.getType() == Material.PARROT_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Parrot") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.PARROT);
        	}
        }
        if(clickedItem.getType() == Material.PHANTOM_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Phantom") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.PHANTOM);
        	}
        }
        if(clickedItem.getType() == Material.PIG_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Pig") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.PIG);
        	}
        }
        if(clickedItem.getType() == Material.PILLAGER_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Pillager") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.PILLAGER);
        	}
        }
        if(clickedItem.getType() == Material.POLAR_BEAR_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Polar_Bear") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.POLAR_BEAR);
        	}
        }
        if(clickedItem.getType() == Material.PUFFERFISH_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Pufferfish") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.PUFFERFISH);
        	}
        }
        if(clickedItem.getType() == Material.RABBIT_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Rabbit") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.RABBIT);
        	}
        }
        if(clickedItem.getType() == Material.RAVAGER_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Ravager") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.RAVAGER);
        	}
        }
        if(clickedItem.getType() == Material.SALMON_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Salmon") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SALMON);
        	}
        }
        if(clickedItem.getType() == Material.SHEEP_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Sheep") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SHEEP);
        	}
        }
        if(clickedItem.getType() == Material.SHULKER_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Shulker") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SHULKER);
        	}
        }
        if(clickedItem.getType() == Material.SILVERFISH_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Silverfish") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		target.getWorld().spawnEntity(loc, EntityType.SILVERFISH);
        	}
        }
        if(clickedItem.getType() == Material.SKELETON_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Skeleton") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SKELETON);
        	}
        }
        if(clickedItem.getType() == Material.SKELETON_HORSE_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Skeleton_Horse") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SKELETON_HORSE);
        	}
        }
        if(clickedItem.getType() == Material.SLIME_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Slime") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SLIME);
        	}
        }
        if(clickedItem.getType() == Material.SPIDER_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Spider") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SPIDER);
        	}
        }
        if(clickedItem.getType() == Material.SQUID_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Squid") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.SQUID);
        	}
        }
        if(clickedItem.getType() == Material.STRAY_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Stray") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.STRAY);
        	}
        }
        if(clickedItem.getType() == Material.TROPICAL_FISH_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Tropical_Fish") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.TROPICAL_FISH);
        	}
        }
        if(clickedItem.getType() == Material.TURTLE_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Turtle") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.TURTLE);
        	}
        }
        if(clickedItem.getType() == Material.VEX_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Vex") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.VEX);
        	}
        }
        if(clickedItem.getType() == Material.VILLAGER_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Villager") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        	}
        }
        if(clickedItem.getType() == Material.VINDICATOR_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Vindicator") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.VINDICATOR);
        	}
        }
        if(clickedItem.getType() == Material.WITCH_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Witch") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.WITCH);
        	}
        }
        if(clickedItem.getType() == Material.WOLF_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Wolf") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.WOLF);
        	}
        }
        if(clickedItem.getType() == Material.ZOMBIE_SPAWN_EGG) {
        	if(p.hasPermission("AdminPanel.PlayerManager.Actions.Spawner")) {
        		if(!cfg.getBoolean("Spawn.Zombie") == true) {
        			p.sendMessage(clickedItem.getItemMeta().getDisplayName() + "§c wurde in der Config deaktiviert!");
        			return;
        		}
        		Player target = Bukkit.getPlayerExact(p.getOpenInventory().getTitle());
        		Location loc = target.getLocation();
        		loc.setY((loc.getY()+2));
        		target.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        	}
        }
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
		if(p.isBanned()) {
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