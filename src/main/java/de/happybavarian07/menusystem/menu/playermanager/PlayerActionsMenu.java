package de.happybavarian07.menusystem.menu.playermanager;

import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.utils.Fireworkgenerator;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class PlayerActionsMenu extends Menu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final UUID targetUUID;

    public PlayerActionsMenu(PlayerMenuUtility playerMenuUtility, UUID targetUUID) {
        super(playerMenuUtility);
        this.targetUUID = targetUUID;
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Actions");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.PlayerActions", Bukkit.getPlayer(targetUUID));
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Player target = Bukkit.getPlayer(targetUUID);
        ItemStack item = e.getCurrentItem();
        String path = "PlayerManager.ActionsMenu.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta() || target == null || !target.isOnline()) return;
        if (item.equals(lgm.getItem(path + "Heal", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Heal")) {
                player.sendMessage(noPerms);
                return;
            }
            target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        } else if (item.equals(lgm.getItem(path + "Kill", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Kill")) {
                player.sendMessage(noPerms);
                return;
            }
            target.setHealth(0.0);
            target.spigot().respawn();
        } else if (item.equals(lgm.getItem(path + "Feed", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Feed")) {
                player.sendMessage(noPerms);
                return;
            }
            target.setFoodLevel(20);
        } else if (item.equals(lgm.getItem(path + "GameMode.Survival", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Gamemode")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem(path + "GameMode.Adventure", target));
            target.setGameMode(GameMode.ADVENTURE);
        } else if (item.equals(lgm.getItem(path + "GameMode.Adventure", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Gamemode")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem(path + "GameMode.Creative", target));
            target.setGameMode(GameMode.CREATIVE);
        } else if (item.equals(lgm.getItem(path + "GameMode.Creative", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Gamemode")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem(path + "GameMode.Spectator", target));
            target.setGameMode(GameMode.SPECTATOR);
        } else if (item.equals(lgm.getItem(path + "GameMode.Spectator", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Gamemode")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem(path + "GameMode.Survival", target));
            target.setGameMode(GameMode.SURVIVAL);
        } else if (item.equals(lgm.getItem(path + "Troll", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll")) {
                player.sendMessage(noPerms);
                return;
            }
            new PlayerTrollMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), targetUUID).open();
        } else if (item.equals(lgm.getItem(path + "Vanish.false", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Vanish")) {
                player.sendMessage(noPerms);
                return;
            }
            VanishAPI.hidePlayer(target);
            super.open();
        } else if (item.equals(lgm.getItem(path + "Vanish.true", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Vanish")) {
                player.sendMessage(noPerms);
                return;
            }
            VanishAPI.showPlayer(target);
            super.open();
        } else if (item.equals(lgm.getItem(path + "Potions", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Potions")) {
                player.sendMessage(noPerms);
                return;
            }
            new PotionMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), targetUUID).open();
        } else if (item.equals(lgm.getItem(path + "Spawning", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
                player.sendMessage(noPerms);
                return;
            }
            new SpawningMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), targetUUID).open();
        } else if (item.equals(lgm.getItem(path + "PlayerSpawnLocation", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.PlayerSpawnLocation")) {
                player.sendMessage(noPerms);
                return;
            }
            target.setBedSpawnLocation(player.getLocation(), true);
        } else if (item.equals(lgm.getItem(path + "Burn", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Burn")) {
                player.sendMessage(noPerms);
                return;
            }
            int burnduration = plugin.getConfig().getInt("Pman.Actions.BurnDuration");
            target.setFireTicks(burnduration * 20);
        } else if (item.equals(lgm.getItem(path + "Lightning", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Lightning")) {
                player.sendMessage(noPerms);
                return;
            }
            target.getWorld().strikeLightning(target.getLocation());
        } else if (item.equals(lgm.getItem(path + "Firework", target))) {
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
        } else if (item.equals(lgm.getItem(path + "TeleportYouToPlayer", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.TeleportYouToPlayer")) {
                player.sendMessage(noPerms);
                return;
            }
            player.teleport(target);
        } else if (item.equals(lgm.getItem(path + "TeleportPlayerToYou", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.TeleportPlayerToYou")) {
                player.sendMessage(noPerms);
                return;
            }
            target.teleport(player);
        } else if (item.equals(lgm.getItem(path + "Op", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Op")) {
                player.sendMessage(noPerms);
                return;
            }
            if (target.isOp()) {
                player.sendMessage(lgm.getMessage("Player.PlayerManager.AlreadyOp", target));
                return;
            }
            player.sendMessage(lgm.getMessage("Player.PlayerManager.OppedMessage", target));
            target.setOp(true);
        } else if (item.equals(lgm.getItem(path + "Inventory", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Inventoryview")) {
                player.sendMessage(noPerms);
                return;
            }
            player.openInventory(target.getInventory());
        } else if (item.equals(lgm.getItem(path + "Deop", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Deop")) {
                player.sendMessage(noPerms);
                return;
            }
            if (!target.isOp()) {
                player.sendMessage(lgm.getMessage("Player.PlayerManager.NotOp", target));
                return;
            }
            player.sendMessage(lgm.getMessage("Player.PlayerManager.DeoppedMessage", target));
            target.setOp(false);
        } else if (item.equals(lgm.getItem(path + "Armor", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Inventoryview")) {
                player.sendMessage(noPerms);
                return;
            }
            new ArmorMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), targetUUID).open();
        } else if (item.equals(lgm.getItem("General.Close", target))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new PlayerActionSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), targetUUID).open();
        }
    }

    @Override
    public void setMenuItems() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, super.FILLER);
        }
        String path = "PlayerManager.ActionsMenu.";
        Player target = Bukkit.getPlayer(targetUUID);
        inventory.setItem(getSlot("PlayerManager.PlayerHead", 4), lgm.getItem("PlayerManager.PlayerHead", target));
        SkullMeta meta = (SkullMeta) inventory.getItem(4).getItemMeta();
        meta.setOwningPlayer(target);
        inventory.getItem(4).setItemMeta(meta);
        // First Row
        inventory.setItem(getSlot(path + "Heal", 11), lgm.getItem(path + "Heal", target));
        inventory.setItem(getSlot(path + "Kill", 13), lgm.getItem(path + "Kill", target));
        inventory.setItem(getSlot(path + "Feed", 15), lgm.getItem(path + "Feed", target));
        // Second Row
        inventory.setItem(getSlot(path + "Potions", 19), lgm.getItem(path + "Potions", target));
        inventory.setItem(getSlot(path + "Spawning", 21), lgm.getItem(path + "Spawning", target));
        if (target.getGameMode().equals(GameMode.SURVIVAL)) {
            inventory.setItem(getSlot(path + "GameMode.Survival", 23), lgm.getItem(path + "GameMode.Survival", target));
        } else if (target.getGameMode().equals(GameMode.ADVENTURE)) {
            inventory.setItem(getSlot(path + "GameMode.Adventure", 23), lgm.getItem(path + "GameMode.Adventure", target));
        } else if (target.getGameMode().equals(GameMode.CREATIVE)) {
            inventory.setItem(getSlot(path + "GameMode.Creative", 23), lgm.getItem(path + "GameMode.Creative", target));
        } else if (target.getGameMode().equals(GameMode.SPECTATOR)) {
            inventory.setItem(getSlot(path + "GameMode.Spectator", 23), lgm.getItem(path + "GameMode.Spectator", target));
        }
        inventory.setItem(getSlot(path + "PlayerSpawnLocation", 25), lgm.getItem(path + "PlayerSpawnLocation", target));
        // Third Row
        inventory.setItem(getSlot(path + "Burn", 29), lgm.getItem(path + "Burn", target));
        inventory.setItem(getSlot(path + "Troll", 31), lgm.getItem(path + "Troll", target));
        inventory.setItem(getSlot(path + "Lightning", 33), lgm.getItem(path + "Lightning", target));
        if(Bukkit.getPluginManager().getPlugin("SuperVanish") != null) {
            if (VanishAPI.isInvisible(target)) {
                inventory.setItem(getSlot(path + "Vanish.true", 35), lgm.getItem(path + "Vanish.true", target));
            } else {
                inventory.setItem(getSlot(path + "Vanish.false", 35), lgm.getItem(path + "Vanish.false", target));
            }
        }
        // Fourth Row
        inventory.setItem(getSlot(path + "Firework", 37), lgm.getItem(path + "Firework", target));
        inventory.setItem(getSlot(path + "TeleportYouToPlayer", 39), lgm.getItem(path + "TeleportYouToPlayer", target));
        inventory.setItem(getSlot(path + "TeleportPlayerToYou", 41), lgm.getItem(path + "TeleportPlayerToYou", target));
        inventory.setItem(getSlot(path + "Op", 43), lgm.getItem(path + "Op", target));
        // Fifth Row
        inventory.setItem(getSlot(path + "Inventory", 47), lgm.getItem(path + "Inventory", target));
        inventory.setItem(getSlot(path + "Deop", 49), lgm.getItem(path + "Deop", target));
        inventory.setItem(getSlot(path + "Armor", 51), lgm.getItem(path + "Armor", target));
        // Close
        inventory.setItem(getSlot("General.Close", 53), lgm.getItem("General.Close", target));
    }
}
