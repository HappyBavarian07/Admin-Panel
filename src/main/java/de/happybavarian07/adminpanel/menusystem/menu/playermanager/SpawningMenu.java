package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.playeractions.EntitySpawner;
import de.happybavarian07.adminpanel.playeractions.KeyNames;
import de.happybavarian07.adminpanel.playeractions.SpawnCategory;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;

/**
 * Spawns entities at the targeted player. The options at the top apply to every spawn from this menu: how many,
 * baby, name, glowing, armor and a rider. The number is capped by {@code Pman.Actions.Spawning.MaxCount}.
 */
public class SpawningMenu extends PaginatedMenu<EntityType> {
    private static final String PERMISSION = "AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner";
    private static final String PATH = "PlayerManager.SpawningMenu.";
    private static final int DEFAULT_MAX_COUNT = 10;

    private final List<EntityType> allTypes = EntitySpawner.spawnableTypes();
    private int count = 1;
    private boolean baby;
    private String customName = "";
    private boolean glowing;
    private boolean armor;
    private boolean rider;
    private SpawnCategory category;

    public SpawningMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission(PERMISSION);
        setSearchable(type -> EntitySpawner.key(type) + " " + EntitySpawner.key(type).replace('_', ' '));
        setSavedMenu(new PlayerActionsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(playerMenuUtility.getOwner())));
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.SpawningMenu", playerMenuUtility.getTarget());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "SpawningMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void preSetMenuItems() {
        List<EntityType> entries = allTypes.stream()
                .filter(type -> category == null || SpawnCategory.of(type.getEntityClass()) == category)
                .toList();
        setSearchSlot(getSlot(PATH + "Search", 45));
        setPaginatedData(entries, this::getPageItem, this::handleEntryClick);
        int perPage = getPaginatedItemSlots().length;
        if (perPage > 0) page = Math.min(page, Math.max(0, (paginatedData.size() - 1) / perPage));
        count = Math.min(count, maxCount());
    }

    @Override
    public void postSetMenuItems() {
        Player owner = playerMenuUtility.getOwner();

        lgm.addPlaceholder(PlaceholderType.ITEM, "%count%", count, false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%max%", maxCount(), false);
        registerButton(getSlot(PATH + "Count", 1), lgm.getItem(PATH + "Count", owner, true), (p, e) -> {
            int step = e.isShiftClick() ? 5 : 1;
            count = e.isRightClick() ? Math.max(1, count - step) : Math.min(maxCount(), count + step);
            refresh();
        });
        toggle(PATH + "Baby", 2, baby, () -> baby = !baby);
        MenuAction promptName = MenuAction.prompt(CommandText.get(lgm, owner, "SpawningMenu.EnterName"), (p, text) -> {
            customName = text.trim().equals("-") ? "" : text.trim();
            CommandText.send(lgm, p, customName.isEmpty() ? "SpawningMenu.NameCleared" : "SpawningMenu.NameSet", "%name%", customName);
            open();
        });
        lgm.addPlaceholder(PlaceholderType.ITEM, "%name%", customName.isEmpty() ? "-" : customName, false);
        registerButton(getSlot(PATH + "Name", 3), lgm.getItem(PATH + "Name", owner, true), (p, e) -> {
            if (e.isRightClick()) {
                customName = "";
                refresh();
            } else {
                promptName.execute(p, e);
            }
        });
        toggle(PATH + "Glowing", 4, glowing, () -> glowing = !glowing);
        toggle(PATH + "Armor", 5, armor, () -> armor = !armor);
        toggle(PATH + "Rider", 6, rider, () -> rider = !rider);

        String filterPath = PATH + "Filter." + (category == null ? "All" : categoryKey(category));
        registerButton(getSlot(PATH + "Filter", 7), lgm.getItem(filterPath, owner, false),
                MenuAction.of((p, e) -> {
                    category = nextCategory(category);
                    page = 0;
                }).then((p, e) -> refresh()));

    }

    private void toggle(String path, int defaultSlot, boolean enabled, Runnable flip) {
        String itemPath = path + (enabled ? ".Enabled" : ".Disabled");
        registerButton(getSlot(path, defaultSlot), lgm.getItem(itemPath, playerMenuUtility.getOwner(), false),
                MenuAction.of((p, e) -> flip.run()).then((p, e) -> refresh()));
    }

    private void handleEntryClick(Player staff, EntityType type, InventoryClickEvent event) {
        MenuAction.of((p, e) -> {
            Player target = playerMenuUtility.getTarget();
            if (target == null) {
                CommandText.send(lgm, p, "Player.General.TargetedPlayerIsNull");
                return;
            }
            int wanted = Math.min(count, maxCount());
            int spawned = EntitySpawner.spawn(target.getLocation(), type,
                    new EntitySpawner.Options(wanted, baby, customName, glowing, armor, rider));
            CommandText.send(lgm, p, spawned > 0 ? "SpawningMenu.Spawned" : "SpawningMenu.SpawnFailed",
                    "%count%", spawned, "%entity%", KeyNames.pretty(EntitySpawner.key(type)), "%target%", target.getName());
        }).requires(PERMISSION).execute(staff, event);
    }

    private int maxCount() {
        return Math.max(1, AdminPanelMain.getPlugin().getConfig().getInt("Pman.Actions.Spawning.MaxCount", DEFAULT_MAX_COUNT));
    }

    public ItemStack getPageItem(EntityType type) {
        Player viewer = playerMenuUtility.getOwner();
        String key = EntitySpawner.key(type);
        SpawnCategory typeCategory = SpawnCategory.of(type.getEntityClass());
        lgm.addPlaceholder(PlaceholderType.ITEM, "%entity%", KeyNames.pretty(key), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%category%",
                CommandText.get(lgm, viewer, "SpawningMenu.Category." + (typeCategory == null ? "Other" : categoryKey(typeCategory))), false);
        ItemStack item = lgm.getItem(PATH + "EntityItem", viewer, true);
        Material egg = Material.matchMaterial(key.toUpperCase(Locale.ROOT) + "_SPAWN_EGG");
        if (egg != null) item.setType(egg);
        return item;
    }

    private static SpawnCategory nextCategory(SpawnCategory current) {
        if (current == null) return SpawnCategory.HOSTILE;
        SpawnCategory[] all = SpawnCategory.values();
        return current.ordinal() + 1 < all.length ? all[current.ordinal() + 1] : null;
    }

    private static String categoryKey(SpawnCategory value) {
        String name = value.name();
        return name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT);
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
