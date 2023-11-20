package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.utils.Fireworkgenerator;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

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
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Player target = playerMenuUtility.getTarget();
        ItemStack item = e.getCurrentItem();
        String path = "PlayerManager.ActionsMenu.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item == null || !item.hasItemMeta() || target == null || !target.isOnline()) return;
        if (item.equals(lgm.getItem(path + "Heal", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Heal")) {
                player.sendMessage(noPerms);
                return;
            }
            target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        } else if (item.equals(lgm.getItem(path + "Kill", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Kill")) {
                player.sendMessage(noPerms);
                return;
            }
            target.setHealth(0.0);
            target.spigot().respawn();
        } else if (item.equals(lgm.getItem(path + "Feed", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Feed")) {
                player.sendMessage(noPerms);
                return;
            }
            target.setFoodLevel(20);
        } else if (item.equals(lgm.getItem(path + "GameMode.Survival", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Gamemode")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem(path + "GameMode.Adventure", target, false));
            target.setGameMode(GameMode.ADVENTURE);
        } else if (item.equals(lgm.getItem(path + "GameMode.Adventure", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Gamemode")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem(path + "GameMode.Creative", target, false));
            target.setGameMode(GameMode.CREATIVE);
        } else if (item.equals(lgm.getItem(path + "GameMode.Creative", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Gamemode")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem(path + "GameMode.Spectator", target, false));
            target.setGameMode(GameMode.SPECTATOR);
        } else if (item.equals(lgm.getItem(path + "GameMode.Spectator", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Gamemode")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem(path + "GameMode.Survival", target, false));
            target.setGameMode(GameMode.SURVIVAL);
        } else if (item.equals(lgm.getItem(path + "Troll", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll")) {
                player.sendMessage(noPerms);
                return;
            }
            new PlayerTrollMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem(path + "Vanish.false", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Vanish")) {
                player.sendMessage(noPerms);
                return;
            }
            VanishAPI.hidePlayer(target);
            super.open();
        } else if (item.equals(lgm.getItem(path + "Vanish.true", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Vanish")) {
                player.sendMessage(noPerms);
                return;
            }
            VanishAPI.showPlayer(target);
            super.open();
        } else if (item.equals(lgm.getItem(path + "Potions", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Potions")) {
                player.sendMessage(noPerms);
                return;
            }
            new PotionMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem(path + "Spawning", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
                player.sendMessage(noPerms);
                return;
            }
            new SpawningMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem(path + "PlayerSpawnLocation", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.PlayerSpawnLocation")) {
                player.sendMessage(noPerms);
                return;
            }
            target.setBedSpawnLocation(player.getLocation(), true);
        } else if (item.equals(lgm.getItem(path + "Burn", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Burn")) {
                player.sendMessage(noPerms);
                return;
            }
            int burnduration = plugin.getConfig().getInt("Pman.Actions.BurnDuration");
            target.setFireTicks(burnduration * 20);
        } else if (item.equals(lgm.getItem(path + "Lightning", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Lightning")) {
                player.sendMessage(noPerms);
                return;
            }
            target.getWorld().strikeLightning(target.getLocation());
        } else if (item.equals(lgm.getItem(path + "Firework", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Firework")) {
                player.sendMessage(noPerms);
                return;
            }
            Fireworkgenerator fwg = new Fireworkgenerator(plugin);
            fwg.setLocation(target.getLocation().add(0, 1.7, 0));
            fwg.setPower(1);
            fwg.setEffect(FireworkEffect.builder().withColor(Color.RED).withColor(Color.AQUA).withColor(Color.YELLOW).withColor(Color.BLUE).withColor(Color.GREEN).with(FireworkEffect.Type.BALL_LARGE).withFlicker().build());
            fwg.setLifeTime(30);
            fwg.spawn();
        } else if (item.equals(lgm.getItem(path + "TeleportYouToPlayer", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.TeleportYouToPlayer")) {
                player.sendMessage(noPerms);
                return;
            }
            player.teleport(target);
        } else if (item.equals(lgm.getItem(path + "TeleportPlayerToYou", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.TeleportPlayerToYou")) {
                player.sendMessage(noPerms);
                return;
            }
            target.teleport(player);
        } else if (item.equals(lgm.getItem(path + "Op", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Op")) {
                player.sendMessage(noPerms);
                return;
            }
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", target.getName(), true);
            if (target.isOp()) {
                player.sendMessage(lgm.getMessage("Player.PlayerManager.AlreadyOp", player, true));
                return;
            }
            player.sendMessage(lgm.getMessage("Player.PlayerManager.OppedMessage", player, true));
            target.setOp(true);
        } else if (item.equals(lgm.getItem(path + "Inventory", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Inventoryview")) {
                player.sendMessage(noPerms);
                return;
            }
            player.openInventory(target.getInventory());
        } else if (item.equals(lgm.getItem(path + "Deop", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Deop")) {
                player.sendMessage(noPerms);
                return;
            }
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", target.getName(), true);
            if (!target.isOp()) {
                player.sendMessage(lgm.getMessage("Player.PlayerManager.NotOp", player, true));
                return;
            }
            player.sendMessage(lgm.getMessage("Player.PlayerManager.DeoppedMessage", player, true));
            target.setOp(false);
        } else if (item.equals(lgm.getItem(path + "Armor", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Inventoryview")) {
                player.sendMessage(noPerms);
                return;
            }
            new ArmorMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem("General.Close", target, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new PlayerActionSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        }
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {

    }

    @Override
    public void setMenuItems() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, super.FILLER);
        }
        String path = "PlayerManager.ActionsMenu.";
        Player target = playerMenuUtility.getTarget();
        inventory.setItem(getSlot("PlayerManager.PlayerHead", 4), lgm.getItem("PlayerManager.PlayerHead", target, false));
        SkullMeta meta = (SkullMeta) inventory.getItem(4).getItemMeta();
        meta.setOwningPlayer(target);
        inventory.getItem(4).setItemMeta(meta);
        // First Row
        inventory.setItem(getSlot(path + "Heal", 11), lgm.getItem(path + "Heal", target, false));
        inventory.setItem(getSlot(path + "Kill", 13), lgm.getItem(path + "Kill", target, false));
        inventory.setItem(getSlot(path + "Feed", 15), lgm.getItem(path + "Feed", target, false));
        // Second Row
        inventory.setItem(getSlot(path + "Potions", 19), lgm.getItem(path + "Potions", target, false));
        inventory.setItem(getSlot(path + "Spawning", 21), lgm.getItem(path + "Spawning", target, false));
        if (target.getGameMode().equals(GameMode.SURVIVAL)) {
            inventory.setItem(getSlot(path + "GameMode.Survival", 23), lgm.getItem(path + "GameMode.Survival", target, false));
        } else if (target.getGameMode().equals(GameMode.ADVENTURE)) {
            inventory.setItem(getSlot(path + "GameMode.Adventure", 23), lgm.getItem(path + "GameMode.Adventure", target, false));
        } else if (target.getGameMode().equals(GameMode.CREATIVE)) {
            inventory.setItem(getSlot(path + "GameMode.Creative", 23), lgm.getItem(path + "GameMode.Creative", target, false));
        } else if (target.getGameMode().equals(GameMode.SPECTATOR)) {
            inventory.setItem(getSlot(path + "GameMode.Spectator", 23), lgm.getItem(path + "GameMode.Spectator", target, false));
        }
        inventory.setItem(getSlot(path + "PlayerSpawnLocation", 25), lgm.getItem(path + "PlayerSpawnLocation", target, false));
        // Third Row
        inventory.setItem(getSlot(path + "Burn", 29), lgm.getItem(path + "Burn", target, false));
        inventory.setItem(getSlot(path + "Troll", 31), lgm.getItem(path + "Troll", target, false));
        inventory.setItem(getSlot(path + "Lightning", 33), lgm.getItem(path + "Lightning", target, false));
        if(Bukkit.getPluginManager().getPlugin("SuperVanish") != null) {
            if (VanishAPI.isInvisible(target)) {
                inventory.setItem(getSlot(path + "Vanish.true", 35), lgm.getItem(path + "Vanish.true", target, false));
            } else {
                inventory.setItem(getSlot(path + "Vanish.false", 35), lgm.getItem(path + "Vanish.false", target, false));
            }
        }
        // Fourth Row
        inventory.setItem(getSlot(path + "Firework", 37), lgm.getItem(path + "Firework", target, false));
        inventory.setItem(getSlot(path + "TeleportYouToPlayer", 39), lgm.getItem(path + "TeleportYouToPlayer", target, false));
        inventory.setItem(getSlot(path + "TeleportPlayerToYou", 41), lgm.getItem(path + "TeleportPlayerToYou", target, false));
        inventory.setItem(getSlot(path + "Op", 43), lgm.getItem(path + "Op", target, false));
        // Fifth Row
        inventory.setItem(getSlot(path + "Inventory", 47), lgm.getItem(path + "Inventory", target, false));
        inventory.setItem(getSlot(path + "Deop", 49), lgm.getItem(path + "Deop", target, false));
        inventory.setItem(getSlot(path + "Armor", 51), lgm.getItem(path + "Armor", target, false));
        // Close
        inventory.setItem(getSlot("General.Close", 53), lgm.getItem("General.Close", target, false));
    }
}
