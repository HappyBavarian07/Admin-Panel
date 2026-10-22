package de.happybavarian07.adminpanel.menusystem.menu.worldmanager;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.world.WorldSelectEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.worlds.WorldFolders;
import de.happybavarian07.adminpanel.worlds.WorldNames;
import de.happybavarian07.adminpanel.worlds.WorldOperations;
import de.happybavarian07.coolstufflib.CoolStuffLib;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Lists the loaded worlds and the world folders that are not loaded. A loaded world opens its settings, a click with
 * the right button teleports, an unloaded one is loaded.
 */
public class WorldSelectMenu extends PaginatedMenu<WorldSelectMenu.Entry> {
    private static final String PERMISSION = "AdminPanel.WorldManager.Open";
    private static final String PATH = "WorldManager.Select.";

    /**
     * @param world the loaded world, {@code null} for a folder that is not loaded
     */
    public record Entry(String name, World world) {
        boolean loaded() {
            return world != null;
        }
    }

    private enum Filter {ALL, LOADED, UNLOADED}

    private Filter filter = Filter.ALL;

    public WorldSelectMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission(PERMISSION);
        setSearchable(Entry::name);
        setSearchSlot(6);
    }

    @Override
    public String getMenuName() {
        return AdminPanelMain.getPlugin().getLanguageManager().getMenuTitle("WorldManager.WorldSelector", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "WorldSelectMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void preSetMenuItems() {
        List<Entry> entries = new ArrayList<>();
        List<String> loadedNames = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            loadedNames.add(world.getName());
            entries.add(new Entry(world.getName(), world));
        }
        for (String name : WorldFolders.unloaded(Bukkit.getWorldContainer(), loadedNames)) {
            entries.add(new Entry(name, null));
        }
        List<Entry> shown = entries.stream()
                .filter(entry -> filter == Filter.ALL || (filter == Filter.LOADED) == entry.loaded())
                .toList();
        setPaginatedData(shown, this::getPageItem, this::handleEntryClick);
        int perPage = getPaginatedItemSlots().length;
        if (perPage > 0) page = Math.min(page, Math.max(0, (paginatedData.size() - 1) / perPage));
    }

    @Override
    public void postSetMenuItems() {
        Player owner = playerMenuUtility.getOwner();
        registerButton(45, lgm.getItem("WorldManager.Create", owner, false),
                MenuAction.of((p, e) -> new WorldCreateMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open())
                        .requires("AdminPanel.WorldManager.Create"));
        registerButton(getSlot("WorldManager.Import", 46), lgm.getItem("WorldManager.Import", owner, false),
                MenuAction.prompt(CommandText.get(lgm, owner, "WorldSelect.EnterImport"), this::importWorld)
                        .requires("AdminPanel.WorldManager.Import"));

        registerButton(getSlot(PATH + "Filter", 7), lgm.getItem(PATH + "Filter." + filter, owner, false),
                MenuAction.of((p, e) -> {
                    filter = Filter.values()[(filter.ordinal() + 1) % Filter.values().length];
                    page = 0;
                }).then((p, e) -> refresh()));
    }

    private void handleEntryClick(Player staff, Entry entry, InventoryClickEvent event) {
        if (entry.loaded()) {
            if (event.isRightClick()) {
                MenuAction.of((p, e) -> {
                    p.closeInventory();
                    p.teleport(entry.world().getSpawnLocation());
                    CommandText.send(lgm, p, "WorldSelect.Teleported", "%world%", entry.name());
                }).requires("AdminPanel.WorldManager.Teleport").execute(staff, event);
                return;
            }
            MenuAction.of((p, e) -> openSettings(p, entry.world())).requires("AdminPanel.WorldManager.Settings").execute(staff, event);
        } else {
            MenuAction.of((p, e) -> loadWorld(p, entry.name())).requires("AdminPanel.WorldManager.Load").execute(staff, event);
        }
    }

    private void openSettings(Player staff, World world) {
        WorldSelectEvent event = new WorldSelectEvent(staff, world);
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(event);
            if (!event.isCancelled()) {
                new WorldSettingsMenu(CoolStuffLib.getLib().getPlayerMenuUtility(staff.getUniqueId()), world).open();
            }
        } catch (NotAPanelEventException invalid) {
            AdminPanelMain.logError("The world select action was not a valid panel event", invalid);
        }
    }

    private void loadWorld(Player staff, String name) {
        WorldResults.send(lgm, staff, "Load", WorldOperations.load(name), name);
        refresh();
    }

    private void importWorld(Player staff, String text) {
        String name = WorldNames.normalize(text);
        WorldResults.send(lgm, staff, "Import", WorldOperations.load(name), name);
        open();
    }

    public ItemStack getPageItem(Entry entry) {
        Player viewer = playerMenuUtility.getOwner();
        lgm.addPlaceholder(PlaceholderType.ITEM, "%world%", entry.name(), false);
        if (entry.loaded()) {
            World world = entry.world();
            lgm.addPlaceholder(PlaceholderType.ITEM, "%environment%", world.getEnvironment().name(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%players%", world.getPlayers().size(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%difficulty%", world.getDifficulty().name(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%main%", WorldOperations.isMainWorld(world)
                    ? CommandText.get(lgm, viewer, "WorldSelect.MainWorld") : "", false);
            return lgm.getItem(PATH + "Loaded", viewer, true);
        }
        lgm.addPlaceholder(PlaceholderType.ITEM, "%environment%",
                WorldFolders.guessEnvironment(WorldOperations.folder(entry.name())).name(), false);
        return lgm.getItem(PATH + "Unloaded", viewer, true);
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
