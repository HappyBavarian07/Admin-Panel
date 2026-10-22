package de.happybavarian07.adminpanel.menusystem.menu;

import de.happybavarian07.adminpanel.data.AdminAction;
import de.happybavarian07.adminpanel.journal.AdminJournal;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.warnings.WarningText;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Predicate;

/**
 * The admin journal, newest entry first, with the library's search over actor, action, target and details.
 * Only the newest {@value #LIMIT} entries are listed; the CSV export has all of them.
 */
public class JournalMenu extends PaginatedMenu<AdminAction> {
    public static final String PERMISSION = "AdminPanel.Journal.View";
    private static final int LIMIT = 500;

    private final Predicate<AdminAction> filter;
    private final String titleKey;

    public JournalMenu(PlayerMenuUtility playerMenuUtility) {
        this(playerMenuUtility, new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(playerMenuUtility.getOwner())),
                "Journal", action -> true, PERMISSION);
    }

    /**
     * @param savedMenu  the menu the back button returns to
     * @param titleKey   the key under {@code MenuTitles} of the title
     * @param filter     which entries are listed
     * @param permission what the viewer needs to open it
     */
    public JournalMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu, String titleKey, Predicate<AdminAction> filter, String permission) {
        super(playerMenuUtility);
        this.filter = filter;
        this.titleKey = titleKey;
        setOpeningPermission(permission);
        setSavedMenu(savedMenu);
        setSearchable(action -> action.getActor() + " " + action.getAction() + " " + action.getTarget() + " " + action.getDetails());
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle(titleKey, null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "JournalMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void preSetMenuItems() {
        AdminJournal journal = AdminPanelMain.getPlugin().getJournal();
        List<AdminAction> entries = journal == null ? List.of() : journal.recent(filter, LIMIT);
        setPaginatedData(entries, this::getPageItem);
        int perPage = getPaginatedItemSlots().length;
        if (perPage > 0) page = Math.min(page, Math.max(0, (paginatedData.size() - 1) / perPage));
    }

    @Override
    public void postSetMenuItems() {
    }

    public ItemStack getPageItem(AdminAction action) {
        lgm.addPlaceholder(PlaceholderType.ITEM, "%action%", action.getAction(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%target%", action.getTarget().isBlank() ? "-" : action.getTarget(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%actor%", action.getActor(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%time%", WarningText.date(lgm, playerMenuUtility.getOwner(), action.getCreatedAt()), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%details%", action.getDetails().isBlank() ? "-" : action.getDetails(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%id%", action.getId(), false);
        return lgm.getItem("Journal.Entry", playerMenuUtility.getOwner(), true);
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
