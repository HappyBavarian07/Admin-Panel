package de.happybavarian07.adminpanel.menusystem.menu.permissionmanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.permissions.PermissionService;
import de.happybavarian07.adminpanel.permissions.engine.GroupData;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.function.BiConsumer;

/** One group: weight, texts, its entries and parents, and deleting it. */
public class PermissionGroupMenu extends Menu {
    private static final String PATH = "PermissionManager.Group.";
    private static final String EDIT = "AdminPanel.Permissions.Group.Edit";

    private final String group;

    public PermissionGroupMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu, String group) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission("AdminPanel.Permissions.Group");
        this.group = group;
    }

    @Override
    public String getMenuName() {
        lgm.addPlaceholder(PlaceholderType.MENUTITLE, "%group%", group, false);
        return lgm.getMenuTitle("PermissionManager.Group", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PermissionGroupMenu";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        PermissionService service = AdminPanelMain.getPlugin().getPermissionService();
        GroupData data = service.group(group);
        if (data == null) {
            savedMenu.open();
            return;
        }
        lgm.addPlaceholder(PlaceholderType.ITEM, "%group%", data.name(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%weight%", data.weight(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%display%", data.displayName().isEmpty() ? "-" : data.displayName(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%prefix%", data.prefix().isEmpty() ? "-" : data.prefix(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%suffix%", data.suffix().isEmpty() ? "-" : data.suffix(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%entries%", data.nodes().size(), false);

        inventory.setItem(getSlot(PATH + "Info", 4), lgm.getItem(PATH + "Info", player, true));
        registerButton(getSlot(PATH + "Entries", 10), lgm.getItem(PATH + "Entries", player, true),
                MenuAction.of((p, e) -> new PermissionEntriesMenu(playerMenuUtility, this, PermissionSubject.ofGroup(group)).open())
                        .requires("AdminPanel.Permissions.Group"), null);
        registerButton(getSlot(PATH + "Weight", 12), lgm.getItem(PATH + "Weight", player, true), prompt("PromptWeight", (p, text) -> {
            try {
                service.setGroupWeight(p, group, Integer.parseInt(text.trim()));
            } catch (NumberFormatException ex) {
                p.sendMessage(lgm.getMessage("Permissions.Menu.NotANumber", p, true));
            }
        }), null);
        registerButton(getSlot(PATH + "Prefix", 13), lgm.getItem(PATH + "Prefix", player, true),
                prompt("PromptText", (p, text) -> service.setGroupPrefix(p, group, clear(text))), null);
        registerButton(getSlot(PATH + "Suffix", 14), lgm.getItem(PATH + "Suffix", player, true),
                prompt("PromptText", (p, text) -> service.setGroupSuffix(p, group, clear(text))), null);
        registerButton(getSlot(PATH + "Display", 15), lgm.getItem(PATH + "Display", player, true),
                prompt("PromptText", (p, text) -> service.setGroupDisplayName(p, group, clear(text))), null);
        registerButton(getSlot(PATH + "Delete", 16), lgm.getItem(PATH + "Delete", player, true),
                MenuAction.of((p, e) -> {
                    if (service.deleteGroup(p, group)) {
                        p.sendMessage(lgm.getMessage("Permissions.Command.GroupDeleted", p, true).replace("%group%", group));
                        savedMenu.open();
                    } else {
                        p.sendMessage(lgm.getMessage("Permissions.Command.GroupDeleteFailed", p, true).replace("%group%", group));
                    }
                }).requires(EDIT).confirm(lgm.getMessage("Permissions.Menu.ConfirmDelete", player, true)), null);
        registerButton(getSlot("General.Close", 26), lgm.getItem("General.Back", player, false),
                MenuAction.of((p, e) -> savedMenu.open()).requires("AdminPanel.Button.Close"), null);
    }

    private static String clear(String text) {
        return text.trim().equals("-") ? "" : text;
    }

    private MenuAction prompt(String messageKey, BiConsumer<Player, String> apply) {
        return MenuAction.prompt(lgm.getMessage("Permissions.Menu." + messageKey, playerMenuUtility.getOwner(), true), (p, text) -> {
            apply.accept(p, text);
            open();
        }).requires(EDIT);
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
