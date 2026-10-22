package de.happybavarian07.adminpanel.menusystem.menu.permissionmanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.permissions.PermissionService;
import de.happybavarian07.adminpanel.permissions.engine.GroupData;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

/** All groups, heaviest first. A click opens the group, the button at the bottom creates a new one. */
public class PermissionGroupsMenu extends PaginatedMenu<GroupData> {
    private static final String PATH = "PermissionManager.Groups.";

    public PermissionGroupsMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission("AdminPanel.Permissions.Group");
        setSearchable(group -> group.name() + " " + group.displayName());
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PermissionManager.Groups", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PermissionGroupsMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void preSetMenuItems() {
        setPaginatedData(AdminPanelMain.getPlugin().getPermissionService().groups(), this::getPageItem, this::handleEntryClick);
        int perPage = getPaginatedItemSlots().length;
        if (perPage > 0) page = Math.min(page, Math.max(0, (paginatedData.size() - 1) / perPage));
    }

    @Override
    public void postSetMenuItems() {
        registerButton(45, lgm.getItem(PATH + "Create", playerMenuUtility.getOwner(), false),
                MenuAction.prompt(lgm.getMessage("Permissions.Menu.PromptGroupName", playerMenuUtility.getOwner(), true), (p, text) -> {
                    PermissionService service = AdminPanelMain.getPlugin().getPermissionService();
                    if (service.createGroup(p, text.trim())) {
                        p.sendMessage(lgm.getMessage("Permissions.Command.GroupCreated", p, true).replace("%group%", text.trim().toLowerCase()));
                    } else {
                        p.sendMessage(lgm.getMessage("Permissions.Command.GroupCreateFailed", p, true).replace("%group%", text.trim()));
                    }
                    open();
                }).requires("AdminPanel.Permissions.Group.Edit"), null);
    }

    private void handleEntryClick(Player staff, GroupData group, InventoryClickEvent event) {
        new PermissionGroupMenu(playerMenuUtility, this, group.name()).open();
    }

    public ItemStack getPageItem(GroupData group) {
        PermissionService service = AdminPanelMain.getPlugin().getPermissionService();
        lgm.addPlaceholder(PlaceholderType.ITEM, "%group%", group.name(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%display%", group.displayName().isEmpty() ? "-" : group.displayName(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%weight%", group.weight(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%entries%", group.nodes().size(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%members%", service.data().loadedMembers(group.name()).size(), false);
        return lgm.getItem(PATH + "Entry", playerMenuUtility.getOwner(), true);
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
