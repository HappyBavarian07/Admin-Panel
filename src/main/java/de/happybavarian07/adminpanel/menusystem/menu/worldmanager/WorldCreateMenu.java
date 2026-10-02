package de.happybavarian07.adminpanel.menusystem.menu.worldmanager;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.world.WorldCreateEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.worlds.WorldNames;
import de.happybavarian07.adminpanel.worlds.WorldOperations;
import de.happybavarian07.adminpanel.worlds.WorldPreset;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * Collects the settings of a new world and creates it. Creating a world generates its spawn area on the main thread, so
 * the server pauses for a moment.
 */
public class WorldCreateMenu extends Menu {
    private static final String PERMISSION = "AdminPanel.WorldManager.Create";
    private static final String PATH = "WorldManager.CreateMenu.";

    private String worldName = "Name";
    private String seedText = "";
    private WorldPreset preset = WorldPreset.NORMAL;
    private World.Environment environment = World.Environment.NORMAL;
    private Difficulty difficulty = Difficulty.NORMAL;
    private boolean structures = true;
    private boolean hardcore = false;
    private boolean teleportAfter = true;

    public WorldCreateMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission(PERMISSION);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("WorldManager.WorldCreateMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "WorldCreateMenu";
    }

    @Override
    public int getSlots() {
        return 36;
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
    }

    @Override
    public void setMenuItems() {
        Player owner = playerMenuUtility.getOwner();
        setFillerGlass();

        registerButton(getSlot(PATH + "Name", 10), item(PATH + "Name", owner),
                MenuAction.prompt(CommandText.get(lgm, owner, "WorldCreate.EnterName"), (p, input) -> {
                    worldName = WorldNames.normalize(input);
                    open();
                }));
        registerButton(getSlot(PATH + "Seed", 11), item(PATH + "Seed", owner),
                MenuAction.prompt(CommandText.get(lgm, owner, "WorldCreate.EnterSeed"), (p, input) -> {
                    seedText = input.trim().equals("-") ? "" : input.trim();
                    open();
                }));
        registerButton(getSlot(PATH + "Preset", 12), item(PATH + "Preset", owner),
                MenuAction.of((p, e) -> preset = preset.next()).then((p, e) -> refresh()));
        registerButton(getSlot(PATH + "Environment." + environment, 13), item(PATH + "Environment." + environment, owner),
                MenuAction.of((p, e) -> {
                    World.Environment[] all = {World.Environment.NORMAL, World.Environment.NETHER, World.Environment.THE_END};
                    environment = all[(Arrays.asList(all).indexOf(environment) + 1) % all.length];
                }).then((p, e) -> refresh()));
        registerButton(getSlot(PATH + "Difficulty." + difficulty, 14), lgm.getItem(PATH + "Difficulty." + difficulty, owner, false),
                MenuAction.of((p, e) -> difficulty = Difficulty.values()[(difficulty.ordinal() + 1) % Difficulty.values().length])
                        .then((p, e) -> refresh()));
        registerButton(getSlot(PATH + "GenerateStructures." + structures, 15), lgm.getItem(PATH + "GenerateStructures." + structures, owner, false),
                MenuAction.of((p, e) -> structures = !structures).then((p, e) -> refresh()));
        registerButton(getSlot(PATH + "Hardcore." + hardcore, 16), lgm.getItem(PATH + "Hardcore." + hardcore, owner, false),
                MenuAction.of((p, e) -> hardcore = !hardcore).then((p, e) -> refresh()));
        registerButton(getSlot(PATH + "TeleportAfter." + teleportAfter, 24), lgm.getItem(PATH + "TeleportAfter." + teleportAfter, owner, false),
                MenuAction.of((p, e) -> teleportAfter = !teleportAfter).then((p, e) -> refresh()));
        registerButton(getSlot(PATH + "CreateWorld", 22), lgm.getItem(PATH + "CreateWorld", owner, false),
                MenuAction.of((p, e) -> createWorld(p)).requires(PERMISSION));
        registerButton(getSlot("General.Close", 35), lgm.getItem("General.Back", owner, false),
                MenuAction.open(() -> new WorldSelectMenu(playerMenuUtility, new AdminPanelStartMenu(playerMenuUtility)))
                        .requires("AdminPanel.Button.Close"));
    }

    private ItemStack item(String path, Player owner) {
        lgm.addPlaceholder(PlaceholderType.ITEM, "%name%", worldName, false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%seed%", seedText.isEmpty() ? "-" : seedText, false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%preset%", CommandText.get(lgm, owner, "WorldCreate.Preset." + preset.languageKey()), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%environment%", environment.name(), false);
        return lgm.getItem(path, owner, false);
    }

    private void createWorld(Player staff) {
        WorldCreateEvent event = new WorldCreateEvent(staff, worldName, preset.type(), environment, structures, hardcore,
                WorldNames.seed(seedText));
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(event);
        } catch (NotAPanelEventException invalid) {
            AdminPanelMain.logError("The world create action was not a valid panel event", invalid);
            return;
        }
        if (event.isCancelled()) return;
        staff.closeInventory();
        WorldOperations.Result result = WorldOperations.create(new WorldOperations.CreateRequest(event.getName(), preset,
                event.getWorldEnvironment(), event.getSeed(), difficulty, event.isGenerateStructures(), event.isHardcore()));
        WorldResults.send(lgm, staff, "Create", result, event.getName());
        if (result != WorldOperations.Result.OK) {
            open();
        } else if (teleportAfter && Bukkit.getWorld(event.getName()) != null) {
            staff.teleport(Bukkit.getWorld(event.getName()).getSpawnLocation());
        } else {
            new WorldSelectMenu(playerMenuUtility, new AdminPanelStartMenu(playerMenuUtility)).open();
        }
    }
}
