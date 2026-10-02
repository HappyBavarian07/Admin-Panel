package de.happybavarian07.adminpanel.menusystem.menu.worldmanager;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.world.MenuGameruleChangeEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.worlds.GameRuleCategory;
import de.happybavarian07.adminpanel.worlds.GameRuleEdit;
import de.happybavarian07.adminpanel.worlds.GameRules;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Changes the gamerules of one world. Left and right click step a number by 1, shift by 10, a boolean flips on any
 * of them, the middle mouse button asks for a value in the chat and the drop key restores the default.
 */
public class GameRuleMenu extends PaginatedMenu<GameRule<?>> {
    private static final String PERMISSION = "AdminPanel.WorldManager.Gamerules";
    private static final String PATH = "WorldManager.GameRuleMenu.";

    private final World world;
    private final List<GameRule<?>> allRules;
    private String search = "";
    private GameRuleCategory category;

    public GameRuleMenu(PlayerMenuUtility playerMenuUtility, World world) {
        super(playerMenuUtility);
        this.world = world;
        setOpeningPermission(PERMISSION);
        allRules = Arrays.stream(world.getGameRules())
                .map(GameRules::byName)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(GameRules::nameOf, String.CASE_INSENSITIVE_ORDER))
                .<GameRule<?>>map(rule -> rule)
                .toList();
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("WorldManager.GameRuleMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "GameRuleMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void preSetMenuItems() {
        String needle = search.toLowerCase(Locale.ROOT);
        List<GameRule<?>> entries = allRules.stream()
                .filter(rule -> category == null || GameRuleCategory.of(GameRules.nameOf(rule)) == category)
                .filter(rule -> needle.isEmpty() || GameRules.nameOf(rule).toLowerCase(Locale.ROOT).contains(needle))
                .toList();
        setPaginatedData(entries, this::getPageItem, this::handleEntryClick);
        int perPage = getPaginatedItemSlots().length;
        if (perPage > 0) page = Math.min(page, Math.max(0, (entries.size() - 1) / perPage));
    }

    @Override
    public void postSetMenuItems() {
        Player owner = playerMenuUtility.getOwner();
        lgm.addPlaceholder(PlaceholderType.ITEM, "%search%", search.isEmpty() ? "-" : search, false);
        MenuAction promptSearch = MenuAction.prompt(CommandText.get(lgm, owner, "GameRuleMenu.EnterSearch"), (p, text) -> {
            search = text.trim().equals("-") ? "" : text.trim();
            page = 0;
            CommandText.send(lgm, p, search.isEmpty() ? "GameRuleMenu.SearchCleared" : "GameRuleMenu.SearchSet", "%search%", search);
            open();
        });
        registerButton(getSlot(PATH + "Search", 6), lgm.getItem(PATH + "Search", owner, true), (p, e) -> {
            if (e.isRightClick()) {
                search = "";
                page = 0;
                refresh();
            } else {
                promptSearch.execute(p, e);
            }
        });

        String filterPath = PATH + "Filter." + (category == null ? "All" : categoryKey(category));
        registerButton(getSlot(PATH + "Filter", 7), lgm.getItem(filterPath, owner, false),
                MenuAction.of((p, e) -> {
                    category = nextCategory(category);
                    page = 0;
                }).then((p, e) -> refresh()));

        registerButton(getSlot("General.Close", 53), lgm.getItem("General.Close", owner, false),
                MenuAction.of((p, e) -> new WorldSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), world).open())
                        .requires("AdminPanel.Button.Close"));
    }

    private void handleEntryClick(Player staff, GameRule<?> rule, InventoryClickEvent event) {
        MenuAction.of((p, e) -> {
            Object current = world.getGameRuleValue(rule);
            switch (e.getClick()) {
                case DROP, CONTROL_DROP -> apply(p, rule, current, world.getGameRuleDefault(rule));
                case MIDDLE -> promptValue(p, rule, current, e);
                case LEFT -> apply(p, rule, current, GameRuleEdit.next(current, GameRuleEdit.Step.UP));
                case RIGHT -> apply(p, rule, current, GameRuleEdit.next(current, GameRuleEdit.Step.DOWN));
                case SHIFT_LEFT -> apply(p, rule, current, GameRuleEdit.next(current, GameRuleEdit.Step.UP_BIG));
                case SHIFT_RIGHT -> apply(p, rule, current, GameRuleEdit.next(current, GameRuleEdit.Step.DOWN_BIG));
                default -> {
                }
            }
        }).requires(PERMISSION).execute(staff, event);
    }

    private void promptValue(Player staff, GameRule<?> rule, Object current, InventoryClickEvent event) {
        MenuAction.prompt(CommandText.get(lgm, staff, "GameRuleMenu.EnterValue", "%rule%", GameRules.nameOf(rule)), (p, text) -> {
            Object typed = GameRuleEdit.parse(current, text);
            if (typed == null) {
                CommandText.send(lgm, p, "GameRuleMenu.NotAValue", "%rule%", GameRules.nameOf(rule));
                open();
                return;
            }
            apply(p, rule, current, typed);
        }).execute(staff, event);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void apply(Player staff, GameRule<?> rule, Object current, Object next) {
        if (next == null || next.equals(current)) {
            open();
            return;
        }
        MenuGameruleChangeEvent event = new MenuGameruleChangeEvent(staff, world, rule, current);
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(event);
        } catch (NotAPanelEventException invalid) {
            AdminPanelMain.logError("The gamerule change action was not a valid panel event", invalid);
            return;
        }
        if (event.isCancelled()) return;
        world.setGameRule((GameRule) rule, next);
        CommandText.send(lgm, staff, "GameRuleMenu.ValueSet", "%rule%", GameRules.nameOf(rule), "%value%", next, "%world%", world.getName());
        open();
    }

    public ItemStack getPageItem(GameRule<?> rule) {
        Player viewer = playerMenuUtility.getOwner();
        Object value = world.getGameRuleValue(rule);
        Object standard = world.getGameRuleDefault(rule);
        String type = value instanceof Boolean ? "Boolean." + (((Boolean) value) ? "Enabled" : "Disabled")
                : value instanceof Integer ? "Number" : "Unknown";
        lgm.addPlaceholder(PlaceholderType.ITEM, "%rule%", GameRules.nameOf(rule), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%value%", value, false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%default%", standard, false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%mark%", Objects.equals(value, standard) ? "" : " &e*", false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%category%",
                CommandText.get(lgm, viewer, "GameRuleMenu.Category." + categoryKey(GameRuleCategory.of(GameRules.nameOf(rule)))), false);
        return lgm.getItem(PATH + "Rule." + type, viewer, true);
    }

    private static GameRuleCategory nextCategory(GameRuleCategory current) {
        if (current == null) return GameRuleCategory.values()[0];
        int next = current.ordinal() + 1;
        return next >= GameRuleCategory.values().length ? null : GameRuleCategory.values()[next];
    }

    private static String categoryKey(GameRuleCategory value) {
        String name = value.name();
        return name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT);
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
