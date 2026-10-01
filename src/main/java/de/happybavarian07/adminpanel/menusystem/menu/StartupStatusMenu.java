package de.happybavarian07.adminpanel.menusystem.menu;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.startup.Startup;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;

/**
 * Every startup stage with its status and the error text, broken ones first.
 */
public class StartupStatusMenu extends PaginatedMenu<Startup.Result> {
    public static final String PERMISSION = "AdminPanel.AdminPanelAdminCommands.Status";

    public StartupStatusMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission(PERMISSION);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("StartupStatus", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "StartupStatusMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void preSetMenuItems() {
        List<Startup.Result> results = AdminPanelMain.getPlugin().getStartup().results().stream()
                .sorted(Comparator.comparing(result -> !isBroken(result)))
                .toList();
        setPaginatedData(results, this::getPageItem, this::handleEntryClick);
    }

    @Override
    public void postSetMenuItems() {
        registerButton(getSlot("General.Close", 53), lgm.getItem("General.Close", playerMenuUtility.getOwner(), false),
                MenuAction.open(() -> new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(playerMenuUtility.getOwner())))
                        .requires("AdminPanel.Button.Close"));
    }

    private void handleEntryClick(Player staff, Startup.Result entry, InventoryClickEvent event) {
        if (isBroken(entry)) {
            FeatureGate.sendUnavailable(staff, entry.id());
        }
    }

    public ItemStack getPageItem(Startup.Result entry) {
        Player viewer = playerMenuUtility.getOwner();
        lgm.addPlaceholder(PlaceholderType.ITEM, "%stage%", FeatureGate.featureName(lgm, viewer, entry.id()), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%kind%",
                CommandText.get(lgm, viewer, "StartupStatus.Kind." + (entry.required() ? "Required" : "Optional")), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%detail%", entry.detail().isEmpty() ? "-" : entry.detail(), false);
        return lgm.getItem("StartupStatus.Entry." + entry.status().name(), viewer, true);
    }

    private static boolean isBroken(Startup.Result result) {
        return result.status() == Startup.Status.FAILED || result.status() == Startup.Status.SKIPPED;
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
