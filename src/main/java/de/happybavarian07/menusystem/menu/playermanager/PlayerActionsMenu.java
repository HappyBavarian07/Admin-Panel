package de.happybavarian07.menusystem.menu.playermanager;

import de.happybavarian07.api.Fireworkgenerator;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.menusystem.menu.playermanager.money.PlayerActionSelectMenu;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class PlayerActionsMenu extends Menu {
    private final Main plugin = Main.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();
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

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta() || target == null || !target.isOnline()) return;
        if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.Heal", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Heal")) {
                player.sendMessage(noPerms);
                return;
            }
            target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.Kill", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Kill")) {
                player.sendMessage(noPerms);
                return;
            }
            target.setHealth(0.0);
            target.spigot().respawn();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.Feed", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Feed")) {
                player.sendMessage(noPerms);
                return;
            }
            target.setFoodLevel(20);
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.GameMode.Survival", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Gamemode")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.ActionsMenu.GameMode.Adventure", target));
            target.setGameMode(GameMode.ADVENTURE);
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.GameMode.Adventure", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Gamemode")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.ActionsMenu.GameMode.Creative", target));
            target.setGameMode(GameMode.CREATIVE);
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.GameMode.Creative", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Gamemode")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.ActionsMenu.GameMode.Spectator", target));
            target.setGameMode(GameMode.SPECTATOR);
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.GameMode.Spectator", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Gamemode")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.ActionsMenu.GameMode.Survival", target));
            target.setGameMode(GameMode.SURVIVAL);
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.Troll", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll")) {
                player.sendMessage(noPerms);
                return;
            }
            new PlayerTrollMenu(playerMenuUtility, targetUUID).open();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.Potions", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Potions")) {
                player.sendMessage(noPerms);
                return;
            }
            new PotionMenu(playerMenuUtility, targetUUID).open();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.Spawning", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner")) {
                player.sendMessage(noPerms);
                return;
            }
            new SpawningMenu(playerMenuUtility, targetUUID).open();
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.PlayerSpawnLocation", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.PlayerSpawnLocation")) {
                player.sendMessage(noPerms);
                return;
            }
            target.setBedSpawnLocation(player.getLocation(), true);
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.Burn", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Burn")) {
                player.sendMessage(noPerms);
                return;
            }
            int burnduration = plugin.getConfig().getInt("Pman.Actions.BurnDuration");
            target.setFireTicks(burnduration * 20);
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.Lightning", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Lightning")) {
                player.sendMessage(noPerms);
                return;
            }
            target.getWorld().strikeLightning(target.getLocation());
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.Firework", target))) {
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
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.TeleportYouToPlayer", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.TeleportYouToPlayer")) {
                player.sendMessage(noPerms);
                return;
            }
            player.teleport(target);
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.TeleportPlayerToYou", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.TeleportPlayerToYou")) {
                player.sendMessage(noPerms);
                return;
            }
            target.teleport(player);
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.Op", target))) {
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
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.Inventory", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Inventoryview")) {
                player.sendMessage(noPerms);
                return;
            }
            player.openInventory(target.getInventory());
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.Deop", target))) {
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
        } else if (item.equals(lgm.getItem("PlayerManager.ActionsMenu.Armor", target))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Inventoryview")) {
                player.sendMessage(noPerms);
                return;
            }
            new ArmorMenu(playerMenuUtility, targetUUID).open();
        } else if (item.equals(lgm.getItem("General.Close", target))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new PlayerActionSelectMenu(playerMenuUtility, targetUUID).open();
        }
    }

    @Override
    public void setMenuItems() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, super.FILLER);
        }
        Player target = Bukkit.getPlayer(targetUUID);
        inventory.setItem(4, lgm.getItem("PlayerManager.PlayerHead", target));
        SkullMeta meta = (SkullMeta) inventory.getItem(4).getItemMeta();
        meta.setOwningPlayer(target);
        inventory.getItem(4).setItemMeta(meta);
        // First Row
        inventory.setItem(11, lgm.getItem("PlayerManager.ActionsMenu.Heal", target));
        inventory.setItem(13, lgm.getItem("PlayerManager.ActionsMenu.Kill", target));
        inventory.setItem(15, lgm.getItem("PlayerManager.ActionsMenu.Feed", target));
        // Second Row
        inventory.setItem(19, lgm.getItem("PlayerManager.ActionsMenu.Potions", target));
        inventory.setItem(21, lgm.getItem("PlayerManager.ActionsMenu.Spawning", target));
        if (target.getGameMode().equals(GameMode.SURVIVAL)) {
            inventory.setItem(23, lgm.getItem("PlayerManager.ActionsMenu.GameMode.Survival", target));
        } else if (target.getGameMode().equals(GameMode.ADVENTURE)) {
            inventory.setItem(23, lgm.getItem("PlayerManager.ActionsMenu.GameMode.Adventure", target));
        } else if (target.getGameMode().equals(GameMode.CREATIVE)) {
            inventory.setItem(23, lgm.getItem("PlayerManager.ActionsMenu.GameMode.Creative", target));
        } else if (target.getGameMode().equals(GameMode.SPECTATOR)) {
            inventory.setItem(23, lgm.getItem("PlayerManager.ActionsMenu.GameMode.Spectator", target));
        }
        inventory.setItem(25, lgm.getItem("PlayerManager.ActionsMenu.PlayerSpawnLocation", target));
        // Third Row
        inventory.setItem(29, lgm.getItem("PlayerManager.ActionsMenu.Burn", target));
        inventory.setItem(31, lgm.getItem("PlayerManager.ActionsMenu.Troll", target));
        inventory.setItem(33, lgm.getItem("PlayerManager.ActionsMenu.Lightning", target));
        // Fourth Row
        inventory.setItem(37, lgm.getItem("PlayerManager.ActionsMenu.Firework", target));
        inventory.setItem(39, lgm.getItem("PlayerManager.ActionsMenu.TeleportYouToPlayer", target));
        inventory.setItem(41, lgm.getItem("PlayerManager.ActionsMenu.TeleportPlayerToYou", target));
        inventory.setItem(43, lgm.getItem("PlayerManager.ActionsMenu.Op", target));
        // Fifth Row
        inventory.setItem(47, lgm.getItem("PlayerManager.ActionsMenu.Inventory", target));
        inventory.setItem(49, lgm.getItem("PlayerManager.ActionsMenu.Deop", target));
        inventory.setItem(51, lgm.getItem("PlayerManager.ActionsMenu.Armor", target));
        // Close
        inventory.setItem(53, lgm.getItem("General.Close", target));
    }
}
