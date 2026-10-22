package de.happybavarian07.adminpanel.menusystem.menu.permissionmanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.permissions.NodeInput;
import de.happybavarian07.adminpanel.permissions.PermissionService;
import de.happybavarian07.adminpanel.permissions.engine.Node;
import de.happybavarian07.adminpanel.permissions.engine.Resolution;
import de.happybavarian07.adminpanel.sanctions.Durations;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The entries of a player or a group: permissions and group memberships. Left click turns an entry from allow to deny, right click removes
 * it, shift left click asks for a new duration, shift right click asks for a world. Two buttons at the bottom add a permission or a group.
 */
public class PermissionEntriesMenu extends PaginatedMenu<Node> {
    private static final String PATH = "PermissionManager.Entries.";

    private final PermissionSubject subject;

    /** The entries of a player, for the shortcut in the player menu. */
    public static PermissionEntriesMenu ofPlayer(PlayerMenuUtility playerMenuUtility, Menu savedMenu, java.util.UUID user, String name) {
        return new PermissionEntriesMenu(playerMenuUtility, savedMenu, PermissionSubject.ofUser(user, name));
    }

    PermissionEntriesMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu, PermissionSubject subject) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission(subject.isUser() ? "AdminPanel.Permissions.User" : "AdminPanel.Permissions.Group");
        this.subject = subject;
        setSearchable(node -> node.permission() + " " + node.contextText());
    }

    @Override
    public String getMenuName() {
        lgm.addPlaceholder(PlaceholderType.MENUTITLE, "%subject%", subject.label(), false);
        return lgm.getMenuTitle("PermissionManager.Entries", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PermissionEntriesMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    private PermissionService service() {
        return AdminPanelMain.getPlugin().getPermissionService();
    }

    @Override
    public void preSetMenuItems() {
        long now = System.currentTimeMillis();
        List<Node> nodes = new ArrayList<>(subject.nodes(service()));
        nodes.removeIf(node -> node.expired(now));
        nodes.sort(Comparator.comparing(Node::isGroup).reversed().thenComparing(Node::permission));
        setPaginatedData(nodes, this::getPageItem, this::handleEntryClick);
        int perPage = getPaginatedItemSlots().length;
        if (perPage > 0) page = Math.min(page, Math.max(0, (paginatedData.size() - 1) / perPage));
    }

    private void badArgument(Player p, IllegalArgumentException e) {
        p.sendMessage(lgm.getMessage("Permissions.Command.BadArgument", p, true).replace("%error%", String.valueOf(e.getMessage())));
    }

    @Override
    public void postSetMenuItems() {
        Player viewer = playerMenuUtility.getOwner();
        registerButton(45, lgm.getItem(PATH + "AddPermission", viewer, false),
                MenuAction.prompt(lgm.getMessage("Permissions.Menu.PromptPermission", viewer, true), (p, text) -> {
                    try {
                        Node node = NodeInput.parseLine(text, System.currentTimeMillis());
                        if (!subject.set(service(), p, node)) {
                            p.sendMessage(lgm.getMessage("Permissions.Command.GroupCycle", p, true).replace("%group%", subject.label()));
                        }
                    } catch (IllegalArgumentException e) {
                        badArgument(p, e);
                    }
                    open();
                }).requires(subject.editPermission()), null);
        registerButton(46, lgm.getItem(PATH + (subject.isUser() ? "AddGroup" : "AddParent"), viewer, false),
                MenuAction.prompt(lgm.getMessage("Permissions.Menu.PromptGroup", viewer, true), (p, text) -> {
                    String[] words = text.trim().split("\\s+");
                    try {
                        if (service().group(words[0]) == null) {
                            p.sendMessage(lgm.getMessage("Permissions.Command.GroupNotFound", p, true).replace("%group%", words[0]));
                        } else {
                            Node node = NodeInput.parse(Node.GROUP_PREFIX + words[0], words, 1, System.currentTimeMillis());
                            if (!subject.set(service(), p, node)) {
                                p.sendMessage(lgm.getMessage("Permissions.Command.GroupCycle", p, true).replace("%group%", words[0]));
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        badArgument(p, e);
                    }
                    open();
                }).requires(subject.editPermission()), null);
        if (subject.isUser()) {
            Resolution resolution = service().resolve(subject.user());
            lgm.addPlaceholder(PlaceholderType.ITEM, "%subject%", subject.label(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%groups%", resolution.groups().isEmpty() ? "-" : String.join(", ", resolution.groups()), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%prefix%", resolution.prefix().isEmpty() ? "-" : resolution.prefix(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%suffix%", resolution.suffix().isEmpty() ? "-" : resolution.suffix(), false);
            inventory.setItem(47, lgm.getItem(PATH + "UserInfo", viewer, true));
        }
    }

    private void handleEntryClick(Player staff, Node node, InventoryClickEvent event) {
        PermissionService service = service();
        MenuAction action = switch (event.getClick()) {
            case LEFT -> MenuAction.of((p, e) -> {
                if (!node.isGroup()) subject.set(service, p, node.withValue(!node.value()));
                refresh();
            });
            case RIGHT -> MenuAction.of((p, e) -> {
                subject.unset(service, p, node);
                refresh();
            });
            case SHIFT_LEFT -> MenuAction.prompt(lgm.getMessage("Permissions.Menu.PromptDuration", staff, true), (p, text) -> {
                try {
                    long duration = Durations.parse(text.trim());
                    subject.set(service, p, new Node(node.permission(), node.value(), duration > 0 ? System.currentTimeMillis() + duration : 0, node.context()));
                } catch (IllegalArgumentException e) {
                    badArgument(p, e);
                }
                open();
            });
            case SHIFT_RIGHT -> MenuAction.prompt(lgm.getMessage("Permissions.Menu.PromptWorld", staff, true), (p, text) -> {
                Map<String, String> context = new TreeMap<>(node.context());
                String world = text.trim();
                if (world.equals("-") || world.isEmpty()) context.remove("world");
                else context.put("world", world);
                subject.unset(service, p, node);
                subject.set(service, p, new Node(node.permission(), node.value(), node.expiresAt(), context));
                open();
            });
            default -> MenuAction.none();
        };
        action.requires(subject.editPermission()).execute(staff, event);
    }

    public ItemStack getPageItem(Node node) {
        String kind = node.isGroup() ? (subject.isUser() ? "Group" : "Parent") : node.value() ? "Allow" : "Deny";
        lgm.addPlaceholder(PlaceholderType.ITEM, "%permission%", node.isGroup() ? node.groupName() : node.permission(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%value%", node.value(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%expires%", node.isTemporary()
                ? Durations.format(node.expiresAt() - System.currentTimeMillis())
                : lgm.getMessage("Warnings.Format.Permanent", playerMenuUtility.getOwner(), false), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%context%", node.context().isEmpty() ? "-" : node.contextText(), false);
        return lgm.getItem(PATH + "Entry." + kind, playerMenuUtility.getOwner(), true);
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
