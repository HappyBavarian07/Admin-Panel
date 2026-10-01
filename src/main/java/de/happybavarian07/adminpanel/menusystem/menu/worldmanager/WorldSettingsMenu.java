package de.happybavarian07.adminpanel.menusystem.menu.worldmanager;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.events.AdminPanelEvent;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.world.AutoSaveToggleEvent;
import de.happybavarian07.adminpanel.events.world.PVPToggleEvent;
import de.happybavarian07.adminpanel.events.world.WorldDeleteEvent;
import de.happybavarian07.adminpanel.events.world.WorldSaveEvent;
import de.happybavarian07.adminpanel.events.world.WorldUnloadEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.worlds.WorldNames;
import de.happybavarian07.adminpanel.worlds.WorldOperations;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

/**
 * The settings of one loaded world. Actions that take the world away (unload, rename, delete) move its players to the
 * main world and refuse the main world itself.
 */
public class WorldSettingsMenu extends Menu {
    private static final String PATH = "WorldManager.Settings.";
    private static final String SETTINGS = "AdminPanel.WorldManager.Settings";
    private static final double DEFAULT_BORDER = 59_999_968;

    private final World world;

    public WorldSettingsMenu(PlayerMenuUtility playerMenuUtility, World world) {
        super(playerMenuUtility);
        this.world = world;
        setOpeningPermission(SETTINGS);
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
        lgm.addPlaceholder(PlaceholderType.ITEM, "%world%", world.getName(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%size%", (long) world.getWorldBorder().getSize(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%players%", world.getPlayers().size(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%environment%", world.getEnvironment().name(), false);

        button("Info", 8, MenuAction.of((p, e) -> {
        }));
        button("TPToSpawn", 0, MenuAction.of((p, e) -> {
            p.closeInventory();
            p.teleport(world.getSpawnLocation());
        }).requires("AdminPanel.WorldManager.Teleport"));
        button("SetSpawn", 1, MenuAction.of((p, e) -> setSpawn(p)).requires(SETTINGS));
        button("Players", 2, MenuAction.of((p, e) -> new WorldPlayersMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), world).open())
                .requires("AdminPanel.WorldManager.Players"));
        button("Seed", 3, MenuAction.of((p, e) -> {
            p.closeInventory();
            CommandText.send(lgm, p, "WorldSettings.Seed", "%world%", world.getName(), "%seed%", world.getSeed());
        }).requires(SETTINGS));
        button("GameRule", 4, MenuAction.of((p, e) -> new GameRuleMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), world).open())
                .requires("AdminPanel.WorldManager.Gamerules"));
        button("TimeWeather", 5, MenuAction.of((p, e) -> new WorldTimeWeatherMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), world).open())
                .requires("AdminPanel.WorldManager.Time"));
        button("Border", 6, MenuAction.prompt(CommandText.get(lgm, playerMenuUtility.getOwner(), "WorldSettings.EnterBorder"), this::setBorder)
                .requires(SETTINGS));

        boolean pvp = world.getPVP();
        button("PVP." + pvp, 19, MenuAction.of((p, e) -> togglePvp(p, !pvp)).requires("AdminPanel.WorldManager.PVP"));
        boolean autoSave = world.isAutoSave();
        button("AutoSave." + autoSave, 20, MenuAction.of((p, e) -> toggleAutoSave(p, !autoSave)).requires(SETTINGS));
        button("Difficulty." + world.getDifficulty(), 21, MenuAction.of((p, e) -> {
            Difficulty[] all = Difficulty.values();
            world.setDifficulty(all[(world.getDifficulty().ordinal() + 1) % all.length]);
        }).then((p, e) -> refresh()).requires(SETTINGS));
        boolean monsters = world.getAllowMonsters();
        button("Monsters." + monsters, 22, MenuAction.of((p, e) -> world.setSpawnFlags(!monsters, world.getAllowAnimals()))
                .then((p, e) -> refresh()).requires(SETTINGS));
        boolean animals = world.getAllowAnimals();
        button("Animals." + animals, 23, MenuAction.of((p, e) -> world.setSpawnFlags(world.getAllowMonsters(), !animals))
                .then((p, e) -> refresh()).requires(SETTINGS));
        boolean keepInventory = Boolean.TRUE.equals(world.getGameRuleValue(GameRule.KEEP_INVENTORY));
        button("KeepInventory." + keepInventory, 24, MenuAction.of((p, e) -> world.setGameRule(GameRule.KEEP_INVENTORY, !keepInventory))
                .then((p, e) -> refresh()).requires(SETTINGS));

        button("Save", 28, MenuAction.of((p, e) -> saveWorld(p)).requires(SETTINGS));
        button("Unload", 29, MenuAction.of((p, e) -> unloadWorld(p)).requires("AdminPanel.WorldManager.Unload"));
        button("Duplicate", 30, MenuAction.prompt(CommandText.get(lgm, playerMenuUtility.getOwner(), "WorldSettings.EnterDuplicate"),
                this::duplicateWorld).requires("AdminPanel.WorldManager.Duplicate"));
        button("Rename", 31, MenuAction.prompt(CommandText.get(lgm, playerMenuUtility.getOwner(), "WorldSettings.EnterRename"),
                this::renameWorld).requires("AdminPanel.WorldManager.Rename"));
        button("Delete", 32, MenuAction.of((p, e) -> deleteWorld(p)).requires("AdminPanel.WorldManager.Delete"));

        registerButton(getSlot("General.Refresh", 45), lgm.getItem("General.Refresh", playerMenuUtility.getOwner(), false),
                MenuAction.of((p, e) -> refresh()).requires("AdminPanel.Button.refresh"));
        registerButton(getSlot("General.Close", 53), lgm.getItem("General.Close", playerMenuUtility.getOwner(), false),
                MenuAction.of((p, e) -> openSelect()).requires("AdminPanel.Button.Close"));
    }

    private void button(String key, int slot, MenuAction action) {
        String path = PATH + key;
        registerButton(getSlot(path, slot), lgm.getItem(path, playerMenuUtility.getOwner(), true), action);
    }

    private void openSelect() {
        new WorldSelectMenu(playerMenuUtility, new AdminPanelStartMenu(playerMenuUtility)).open();
    }

    private void setSpawn(Player staff) {
        if (!staff.getWorld().equals(world)) {
            CommandText.send(lgm, staff, "WorldSettings.SpawnWrongWorld", "%world%", world.getName());
            return;
        }
        Location at = staff.getLocation();
        world.setSpawnLocation(at.getBlockX(), at.getBlockY(), at.getBlockZ());
        CommandText.send(lgm, staff, "WorldSettings.SpawnSet", "%world%", world.getName(),
                "%x%", at.getBlockX(), "%y%", at.getBlockY(), "%z%", at.getBlockZ());
    }

    private void setBorder(Player staff, String text) {
        try {
            double size = Double.parseDouble(text.trim());
            if (size < 0 || size > DEFAULT_BORDER) throw new NumberFormatException("out of range");
            world.getWorldBorder().setSize(size == 0 ? DEFAULT_BORDER : size);
            CommandText.send(lgm, staff, "WorldSettings.BorderSet", "%world%", world.getName(), "%size%", (long) world.getWorldBorder().getSize());
        } catch (NumberFormatException invalid) {
            CommandText.send(lgm, staff, "WorldSettings.NotASize");
        }
        open();
    }

    private void togglePvp(Player player, boolean enabled) {
        PVPToggleEvent event = new PVPToggleEvent(player, world, enabled);
        if (!fire(event)) return;
        world.setPVP(enabled);
        refresh();
    }

    private void toggleAutoSave(Player player, boolean enabled) {
        AutoSaveToggleEvent event = new AutoSaveToggleEvent(player, world, enabled);
        if (!fire(event)) return;
        world.setAutoSave(enabled);
        refresh();
    }

    private void saveWorld(Player player) {
        if (!fire(new WorldSaveEvent(player, world))) return;
        WorldResults.send(lgm, player, "Save", WorldOperations.save(world), world.getName());
    }

    private void unloadWorld(Player player) {
        if (!fire(new WorldUnloadEvent(player, world))) return;
        WorldOperations.Result result = WorldOperations.unload(world, world.isAutoSave());
        WorldResults.send(lgm, player, "Unload", result, world.getName());
        if (result == WorldOperations.Result.OK) {
            openSelect();
        } else {
            refresh();
        }
    }

    private void duplicateWorld(Player staff, String text) {
        String name = WorldNames.normalize(text);
        WorldOperations.duplicate(world, name, result -> {
            WorldResults.send(lgm, staff, "Duplicate", result, name);
            if (staff.isOnline()) openSelect();
        });
    }

    private void renameWorld(Player staff, String text) {
        String name = WorldNames.normalize(text);
        String oldName = world.getName();
        WorldOperations.Result result = WorldOperations.rename(world, name);
        WorldResults.send(lgm, staff, "Rename", result, result == WorldOperations.Result.OK ? name : oldName);
        World renamed = result == WorldOperations.Result.OK ? Bukkit.getWorld(name) : world;
        new WorldSettingsMenu(playerMenuUtility, renamed).open();
    }

    private void deleteWorld(Player player) {
        if (WorldOperations.isMainWorld(world)) {
            WorldResults.send(lgm, player, "Delete", WorldOperations.Result.MAIN_WORLD, world.getName());
            return;
        }
        if (!fire(new WorldDeleteEvent(player, world))) return;
        String name = world.getName();
        Utils.openConfirmationMenu(
                "World Deletion of World: " + name,
                (p, e) -> WorldOperations.delete(world, result -> {
                    WorldResults.send(lgm, p, "Delete", result, name);
                    if (p.isOnline()) openSelect();
                }),
                player,
                this
        );
    }

    private boolean fire(AdminPanelEvent event) {
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(event);
        } catch (NotAPanelEventException invalid) {
            AdminPanelMain.logError("A world settings action was not a valid panel event", invalid);
            return false;
        }
        return !(event instanceof Cancellable cancellable) || !cancellable.isCancelled();
    }
}
