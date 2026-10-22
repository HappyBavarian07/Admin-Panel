package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.journal.AdminJournal;
import de.happybavarian07.adminpanel.menusystem.menu.FeatureGate;
import de.happybavarian07.adminpanel.utils.AttributeLookup;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.utils.FireworkGenerator;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerActionsMenu extends Menu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    public PlayerActionsMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Actions");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.PlayerActions", playerMenuUtility.getTarget());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PlayerActionsMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        String path = "PlayerManager.ActionsMenu.";
        Player target = playerMenuUtility.getTarget();
        Player player = playerMenuUtility.getOwner();

        // Header
        inventory.setItem(getSlot("PlayerManager.PlayerHead", 4), lgm.getItem("PlayerManager.PlayerHead", target, false));
        if (inventory.getItem(4).getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(target);
            inventory.getItem(4).setItemMeta(meta);
        }

        // First Row
        registerButton(getSlot(path + "Heal", 11), lgm.getItem(path + "Heal", target, false),
            MenuAction.of((p, e) -> target.setHealth(target.getAttribute(AttributeLookup.maxHealth()).getValue()))
                .requires("AdminPanel.PlayerManager.PlayerSettings.Actions.Heal"));

        registerButton(getSlot(path + "Kill", 13), lgm.getItem(path + "Kill", target, false),
            MenuAction.of((p, e) -> {
                target.setHealth(0.0);
                target.spigot().respawn();
            }).requires("AdminPanel.PlayerManager.PlayerSettings.Actions.Kill"));

        registerButton(getSlot(path + "Feed", 15), lgm.getItem(path + "Feed", target, false),
            MenuAction.of((p, e) -> target.setFoodLevel(20))
                .requires("AdminPanel.PlayerManager.PlayerSettings.Actions.Feed"));

        // Second Row
        registerButton(getSlot(path + "Potions", 19), lgm.getItem(path + "Potions", target, false),
            MenuAction.of((p, e) -> new PotionMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open())
                .requires("AdminPanel.PlayerManager.PlayerSettings.Actions.Potions"));

        registerButton(getSlot(path + "Spawning", 21), lgm.getItem(path + "Spawning", target, false),
            MenuAction.of((p, e) -> new SpawningMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open())
                .requires("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner"));

        // GameMode Cycling
        GameMode currentMode = target.getGameMode();
        String modePath;
        GameMode nextMode;
        if (currentMode == GameMode.SURVIVAL) {
            modePath = path + "GameMode.Survival";
            nextMode = GameMode.ADVENTURE;
        } else if (currentMode == GameMode.ADVENTURE) {
            modePath = path + "GameMode.Adventure";
            nextMode = GameMode.CREATIVE;
        } else if (currentMode == GameMode.CREATIVE) {
            modePath = path + "GameMode.Creative";
            nextMode = GameMode.SPECTATOR;
        } else {
            modePath = path + "GameMode.Spectator";
            nextMode = GameMode.SURVIVAL;
        }

        registerButton(getSlot(modePath, 23), lgm.getItem(modePath, target, false), 
            MenuAction.of((p, e) -> target.setGameMode(nextMode))
                .then((p, e) -> refresh())
                .requires("AdminPanel.PlayerManager.PlayerSettings.Actions.Gamemode"));

        registerButton(getSlot(path + "PlayerSpawnLocation", 25), lgm.getItem(path + "PlayerSpawnLocation", target, false),
            MenuAction.of((p, e) -> target.setBedSpawnLocation(p.getLocation(), true))
                .requires("AdminPanel.PlayerManager.PlayerSettings.Actions.PlayerSpawnLocation"));

        // Third Row
        registerButton(getSlot(path + "Burn", 29), lgm.getItem(path + "Burn", target, false),
            MenuAction.of((p, e) -> {
                int burnduration = plugin.getConfig().getInt("Pman.Actions.BurnDuration");
                target.setFireTicks(burnduration * 20);
            }).requires("AdminPanel.PlayerManager.PlayerSettings.Actions.Burn"));

        if (plugin.getStartup().isOk("trolls")) {
            registerButton(getSlot(path + "Troll", 31), lgm.getItem(path + "Troll", target, false),
                MenuAction.open(() -> new PlayerTrollMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)))
                    .requires("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll"));
        } else {
            FeatureGate.placeUnavailable(this, getSlot(path + "Troll", 31), "trolls", player);
        }

        registerButton(getSlot(path + "Lightning", 33), lgm.getItem(path + "Lightning", target, false),
            MenuAction.of((p, e) -> target.getWorld().strikeLightning(target.getLocation()))
                .requires("AdminPanel.PlayerManager.PlayerSettings.Actions.Lightning"));

        if(Bukkit.getPluginManager().getPlugin("SuperVanish") != null) {
            boolean invisible = VanishAPI.isInvisible(target);
            String vanishPath = invisible ? path + "Vanish.true" : path + "Vanish.false";
            registerButton(getSlot(vanishPath, 35), lgm.getItem(vanishPath, target, false),
                MenuAction.of((p, e) -> {
                    if (invisible) VanishAPI.showPlayer(target); else VanishAPI.hidePlayer(target);
                }).then((p, e) -> refresh())
                    .requires("AdminPanel.PlayerManager.PlayerSettings.Actions.Vanish"));
        }

        // Fourth Row
        registerButton(getSlot(path + "Firework", 37), lgm.getItem(path + "Firework", target, false),
            MenuAction.of((p, e) -> {
                FireworkGenerator fwg = new FireworkGenerator(plugin);
                fwg.setLocation(target.getLocation().add(0, 1.7, 0));
                fwg.setPower(1);
                fwg.setEffect(FireworkEffect.builder().withColor(Color.RED).withColor(Color.AQUA).withColor(Color.YELLOW).withColor(Color.BLUE).withColor(Color.GREEN).with(FireworkEffect.Type.BALL_LARGE).withFlicker().build());
                fwg.setLifeTime(30);
                fwg.spawn();
            }).requires("AdminPanel.PlayerManager.PlayerSettings.Actions.Firework"));

        registerButton(getSlot(path + "TeleportYouToPlayer", 39), lgm.getItem(path + "TeleportYouToPlayer", target, false),
            MenuAction.of((p, e) -> p.teleport(target))
                .requires("AdminPanel.PlayerManager.PlayerSettings.Actions.TeleportYouToPlayer"));

        registerButton(getSlot(path + "TeleportPlayerToYou", 41), lgm.getItem(path + "TeleportPlayerToYou", target, false),
            MenuAction.of((p, e) -> target.teleport(p))
                .requires("AdminPanel.PlayerManager.PlayerSettings.Actions.TeleportPlayerToYou"));

        registerButton(getSlot(path + "Op", 43), lgm.getItem(path + "Op", target, false),
            MenuAction.of((p, e) -> {
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", target.getName(), true);
                if (target.isOp()) {
                    p.sendMessage(lgm.getMessage("Player.PlayerManager.AlreadyOp", p, true));
                    return;
                }
                p.sendMessage(lgm.getMessage("Player.PlayerManager.OppedMessage", p, true));
                target.setOp(true);
                AdminJournal.log(p, "OP", target.getName(), "");
            }).requires("AdminPanel.PlayerManager.PlayerSettings.Actions.Op"));

        // Fifth Row
        registerButton(getSlot(path + "Inventory", 47), lgm.getItem(path + "Inventory", target, false),
            MenuAction.of((p, e) -> p.openInventory(target.getInventory()))
                .requires("AdminPanel.PlayerManager.PlayerSettings.Actions.Inventoryview"));

        registerButton(getSlot(path + "Deop", 49), lgm.getItem(path + "Deop", target, false),
            MenuAction.of((p, e) -> {
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", target.getName(), true);
                if (!target.isOp()) {
                    p.sendMessage(lgm.getMessage("Player.PlayerManager.NotOp", p, true));
                    return;
                }
                p.sendMessage(lgm.getMessage("Player.PlayerManager.DeoppedMessage", p, true));
                target.setOp(false);
                AdminJournal.log(p, "DEOP", target.getName(), "");
            }).requires("AdminPanel.PlayerManager.PlayerSettings.Actions.Deop"));

        registerButton(getSlot(path + "Armor", 51), lgm.getItem(path + "Armor", target, false),
            MenuAction.open(() -> new ArmorMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)))
                .requires("AdminPanel.PlayerManager.PlayerSettings.Actions.Inventoryview"));

        // Close
        registerButton(getSlot("General.Close", 53), lgm.getItem("General.Back", target, false),
            MenuAction.open(() -> new PlayerSelectMenu(playerMenuUtility, new AdminPanelStartMenu(playerMenuUtility)))
                .requires("AdminPanel.Button.Close"));
    }
}
