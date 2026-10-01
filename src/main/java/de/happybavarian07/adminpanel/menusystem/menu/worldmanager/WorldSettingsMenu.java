package de.happybavarian07.adminpanel.menusystem.menu.worldmanager;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.world.*;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.menusystem.menu.worldmanager.time.TimeChangeMenu;
import de.happybavarian07.adminpanel.menusystem.menu.worldmanager.weather.WeatherChangeMenu;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class WorldSettingsMenu extends Menu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final World world;

    public WorldSettingsMenu(PlayerMenuUtility playerMenuUtility, World world) {
        super(playerMenuUtility);
        this.world = world;
        setOpeningPermission("AdminPanel.WorldManager.Settings");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("WorldManager.WorldSettings", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "WorldSettingsMenu";
    }

    @Override
    public int getSlots() {
        return 27;
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
        Player player = playerMenuUtility.getOwner();
        String path = "WorldManager.Settings.";
        if (world.getPVP()) {
            registerButton(getSlot(path + "PVP.true", 10), lgm.getItem(path + "PVP.true", player, false), (p, e) -> togglePvp(p, false), null);
        } else {
            registerButton(getSlot(path + "PVP.false", 10), lgm.getItem(path + "PVP.false", player, false), (p, e) -> togglePvp(p, true), null);
        }
        if (world.isAutoSave()) {
            registerButton(getSlot(path + "AutoSave.true", 12), lgm.getItem(path + "AutoSave.true", player, false), (p, e) -> toggleAutoSave(p, false), null);
        } else {
            registerButton(getSlot(path + "AutoSave.false", 12), lgm.getItem(path + "AutoSave.false", player, false), (p, e) -> toggleAutoSave(p, true), null);
        }
        registerButton(getSlot(path + "Time", 14), lgm.getItem(path + "Time", player, false), (p, e) -> new TimeChangeMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), world).open(), null);
        registerButton(getSlot(path + "Weather", 16), lgm.getItem(path + "Weather", player, false), (p, e) -> new WeatherChangeMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), world).open(), null);
        registerButton(getSlot(path + "Load", 20), lgm.getItem(path + "Load", player, false), (p, e) -> loadWorld(p), null);
        registerButton(getSlot(path + "Unload", 22), lgm.getItem(path + "Unload", player, false), (p, e) -> unloadWorld(p), null);
        registerButton(getSlot(path + "Save", 24), lgm.getItem(path + "Save", player, false), (p, e) -> saveWorld(p), null);
        registerButton(getSlot(path + "Delete", 8), lgm.getItem(path + "Delete", player, false), (p, e) -> deleteWorld(p), null);
        registerButton(getSlot(path + "GameRule", 4), lgm.getItem(path + "GameRule", player, false), (p, e) -> new GameRuleMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), world).open(), null);
        registerButton(getSlot(path + "TPToSpawn", 0), lgm.getItem(path + "TPToSpawn", player, false), (p, e) -> {
            p.closeInventory();
            p.teleport(world.getSpawnLocation());
            super.open();
        }, null);
        registerButton(getSlot("General.Refresh", 18), lgm.getItem("General.Refresh", player, false),
                MenuAction.of((p, e) -> super.open()).requires("AdminPanel.Button.refresh"), null);
        registerButton(getSlot("General.Close", 26), lgm.getItem("General.Close", player, false),
                MenuAction.of((p, e) ->
                        new WorldSelectMenu(playerMenuUtility, new AdminPanelStartMenu(playerMenuUtility)).open()).requires("AdminPanel.Button.Close"), null);
    }

    private void togglePvp(Player player, boolean enabled) {
        PVPToggleEvent event = new PVPToggleEvent(player, world, enabled);
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(event);
            if (!event.isCancelled()) {
                world.setPVP(enabled);
                super.open();
            }
        } catch (NotAPanelEventException ex) {
            AdminPanelMain.logError("A world settings action was not a valid panel event", ex);
        }
    }

    private void toggleAutoSave(Player player, boolean enabled) {
        AutoSaveToggleEvent event = new AutoSaveToggleEvent(player, world, enabled);
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(event);
            if (!event.isCancelled()) {
                world.setAutoSave(enabled);
                super.open();
            }
        } catch (NotAPanelEventException ex) {
            AdminPanelMain.logError("A world settings action was not a valid panel event", ex);
        }
    }

    private void loadWorld(Player player) {
        WorldLoadEvent loadEvent = new WorldLoadEvent(player, world);
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(loadEvent);
            if (!loadEvent.isCancelled()) {
                try {
                    if (!new File(world.getWorldFolder() + "\\data\\raids.dat").exists()) {
                        new File(world.getWorldFolder() + "\\data\\").mkdir();
                        new File(world.getWorldFolder() + "\\data\\raids.dat").createNewFile();
                    }
                    if (!new File(world.getWorldFolder() + "\\DIM-1\\data\\raids.dat").exists()) {
                        new File(world.getWorldFolder() + "\\DIM-1\\data\\").mkdir();
                        new File(world.getWorldFolder() + "\\DIM-1\\data\\raids.dat").createNewFile();
                    }
                } catch (IOException ex) {
                    AdminPanelMain.logError("Could not create the raids.dat files of the world", ex);
                }
                Bukkit.createWorld(new WorldCreator(world.getName()));
                super.open();
            }
        } catch (NotAPanelEventException ex) {
            AdminPanelMain.logError("A world settings action was not a valid panel event", ex);
        }
    }

    private void unloadWorld(Player player) {
        WorldUnloadEvent unloadEvent = new WorldUnloadEvent(player, world);
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(unloadEvent);
            if (!unloadEvent.isCancelled()) {
                Bukkit.unloadWorld(world, world.isAutoSave());
                super.open();
            }
        } catch (NotAPanelEventException ex) {
            AdminPanelMain.logError("A world settings action was not a valid panel event", ex);
        }
    }

    private void saveWorld(Player player) {
        WorldSaveEvent saveEvent = new WorldSaveEvent(player, world);
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(saveEvent);
            if (!saveEvent.isCancelled()) {
                try {
                    if (!new File(world.getWorldFolder() + "\\data\\raids.dat").exists()) {
                        new File(world.getWorldFolder() + "\\data\\raids.dat").mkdir();
                        new File(world.getWorldFolder() + "\\data\\raids.dat").createNewFile();
                    }
                    if (!new File(world.getWorldFolder() + "\\DIM-1\\data\\raids.dat").exists()) {
                        new File(world.getWorldFolder() + "\\DIM-1\\data\\raids.dat").mkdir();
                        new File(world.getWorldFolder() + "\\DIM-1\\data\\raids.dat").createNewFile();
                    }
                    world.save();
                } catch (Exception ignored) {
                }
                super.open();
            }
        } catch (NotAPanelEventException ex) {
            AdminPanelMain.logError("A world settings action was not a valid panel event", ex);
        }
    }

    private void deleteWorld(Player player) {
        WorldDeleteEvent deleteEvent = new WorldDeleteEvent(player, world);
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(deleteEvent);
            if (!deleteEvent.isCancelled()) {
                Utils.openConfirmationMenu(
                        "World Deletion of World: " + world.getName(),
                        (p, e) -> AdminPanelUtils.deleteMinecraftWorld(world),
                        player,
                        this
                );
            }
        } catch (NotAPanelEventException ex) {
            AdminPanelMain.logError("A world settings action was not a valid panel event", ex);
        }
    }

    public void deleteWorldFolder(File path) {
        if (path.exists() && path.isDirectory()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteWorldFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        path.delete();
    }
}
