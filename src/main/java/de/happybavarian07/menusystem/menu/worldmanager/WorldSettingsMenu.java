package de.happybavarian07.menusystem.menu.worldmanager;

import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.menusystem.menu.worldmanager.time.TimeChangeMenu;
import de.happybavarian07.menusystem.menu.worldmanager.weather.WeatherChangeMenu;
import de.happybavarian07.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

import static org.bukkit.Bukkit.getServer;

public class WorldSettingsMenu extends Menu {
    private final Main plugin = Main.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();
    private final World world;

    public WorldSettingsMenu(PlayerMenuUtility playerMenuUtility, World world) {
        super(playerMenuUtility);
        this.world = world;
        setOpeningPermission("AdminPanel.WorldManagment.Settings");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("WorldManager.WorldSettings", null);
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String itemPath = "WorldManager.Settings.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta()) return;
        if(item.equals(lgm.getItem(itemPath + "PVP.true", player))) {
            world.setPVP(false);
            super.open();
        } else if (item.equals(lgm.getItem(itemPath + "PVP.false", player))) {
            world.setPVP(true);
            super.open();
        } else if (item.equals(lgm.getItem(itemPath + "AutoSave.true", player))) {
            world.setAutoSave(false);
            super.open();
        } else if (item.equals(lgm.getItem(itemPath + "AutoSave.false", player))) {
            world.setAutoSave(true);
            super.open();
        } else if (item.equals(lgm.getItem(itemPath + "Time", player))) {
            new TimeChangeMenu(playerMenuUtility, world).open();
        } else if (item.equals(lgm.getItem(itemPath + "Weather", player))) {
            new WeatherChangeMenu(playerMenuUtility, world).open();
        } else if (item.equals(lgm.getItem(itemPath + "Load", player))) {
            try {
                if (!new File(world.getWorldFolder() + "\\data\\raids.dat").exists()) {
                    new File(world.getWorldFolder() + "\\data\\").mkdir();
                    new File(world.getWorldFolder() + "\\data\\raids.dat").createNewFile();
                }
                if(!new File(world.getWorldFolder() + "\\DIM-1\\data\\raids.dat").exists()) {
                    new File(world.getWorldFolder() + "\\DIM-1\\data\\").mkdir();
                    new File(world.getWorldFolder() + "\\DIM-1\\data\\raids.dat").createNewFile();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            Bukkit.createWorld(new WorldCreator(world.getName()));
            super.open();
        } else if (item.equals(lgm.getItem(itemPath + "Unload", player))) {
            Bukkit.unloadWorld(world, world.isAutoSave());
            super.open();
        } else if (item.equals(lgm.getItem(itemPath + "Save", player))) {
            try {
                if (!new File(world.getWorldFolder() + "\\data\\raids.dat").exists()) {
                    new File(world.getWorldFolder() + "\\data\\raids.dat").mkdir();
                    new File(world.getWorldFolder() + "\\data\\raids.dat").createNewFile();
                }
                if(!new File(world.getWorldFolder() + "\\DIM-1\\data\\raids.dat").exists()) {
                    new File(world.getWorldFolder() + "\\DIM-1\\data\\raids.dat").mkdir();
                    new File(world.getWorldFolder() + "\\DIM-1\\data\\raids.dat").createNewFile();
                }
                world.save();
            } catch (Exception ignored) {}
            super.open();
        } else if (item.equals(lgm.getItem(itemPath + "TPToSpawn", player))) {
            player.closeInventory();
            player.teleport(world.getSpawnLocation());
            super.open();
        } else if (item.equals(lgm.getItem(itemPath + "Delete", player))) {
            for(Player playerInWorld : world.getPlayers()) {
                playerInWorld.kickPlayer(Utils.getInstance().chat("The World just got deleted!"));
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.unloadWorld(world, false);
                    getServer().getWorlds().remove(world);
                    deleteWorldFolder(world.getWorldFolder().getAbsoluteFile());
                    new WorldSelectMenu(playerMenuUtility).open();
                }
            }.runTaskLater(plugin, 5);
        } else if (item.equals(lgm.getItem(itemPath + "GameRule", player))) {
            new GameRuleMenu(playerMenuUtility, world).open();
        } else if (item.equals(lgm.getItem("General.Refresh", player))) {
            super.open();
        } else if (item.equals(lgm.getItem("General.Close", player))) {
            new WorldSelectMenu(playerMenuUtility).open();
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String itemPath = "WorldManager.Settings.";
        if(world.getPVP()) {
            inventory.setItem(10, lgm.getItem(itemPath + "PVP.true", player));
        } else {
            inventory.setItem(10, lgm.getItem(itemPath + "PVP.false", player));
        }
        if(world.isAutoSave()) {
            inventory.setItem(12, lgm.getItem(itemPath + "AutoSave.true", player));
        } else {
            inventory.setItem(12, lgm.getItem(itemPath + "AutoSave.false", player));
        }
        inventory.setItem(14, lgm.getItem(itemPath + "Time", player));
        inventory.setItem(16, lgm.getItem(itemPath + "Weather", player));
        inventory.setItem(20, lgm.getItem(itemPath + "Load", player));
        inventory.setItem(22, lgm.getItem(itemPath + "Unload", player));
        inventory.setItem(24, lgm.getItem(itemPath + "Save", player));
        inventory.setItem(8, lgm.getItem(itemPath + "Delete", player));
        inventory.setItem(4, lgm.getItem(itemPath + "GameRule", player));
        inventory.setItem(0, lgm.getItem(itemPath + "TPToSpawn", player));
        inventory.setItem(18, lgm.getItem("General.Refresh", player));
        inventory.setItem(26, lgm.getItem("General.Close", player));
    }

    public boolean deleteWorldFolder(File path) {
        if(path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteWorldFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        return(path.delete());
    }
}
