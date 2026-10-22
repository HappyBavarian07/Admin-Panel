package de.happybavarian07.adminpanel.menusystem.menu.permissionmanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.permissions.PermissionService;
import de.happybavarian07.adminpanel.permissions.engine.Resolution;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Everyone who is online or has entries of their own. A click opens the entries of that player; the button at the bottom looks a player up by name. */
public class PermissionPlayersMenu extends PaginatedMenu<OfflinePlayer> {
    private static final String PATH = "PermissionManager.Players.";

    public PermissionPlayersMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission("AdminPanel.Permissions.User");
        setSearchable(player -> String.valueOf(player.getName()));
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PermissionManager.Players", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PermissionPlayersMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void preSetMenuItems() {
        PermissionService service = AdminPanelMain.getPlugin().getPermissionService();
        Set<UUID> ids = new LinkedHashSet<>();
        Bukkit.getOnlinePlayers().forEach(online -> ids.add(online.getUniqueId()));
        ids.addAll(service.knownUsers());
        List<OfflinePlayer> players = new ArrayList<>();
        for (UUID id : ids) players.add(Bukkit.getOfflinePlayer(id));
        setPaginatedData(players, this::getPageItem, this::handleEntryClick);
        int perPage = getPaginatedItemSlots().length;
        if (perPage > 0) page = Math.min(page, Math.max(0, (paginatedData.size() - 1) / perPage));
    }

    @Override
    public void postSetMenuItems() {
        registerButton(45, lgm.getItem(PATH + "Lookup", playerMenuUtility.getOwner(), false),
                MenuAction.prompt(lgm.getMessage("Permissions.Menu.PromptPlayer", playerMenuUtility.getOwner(), true), (p, text) -> {
                    OfflinePlayer target = Bukkit.getPlayerExact(text);
                    for (OfflinePlayer known : Bukkit.getOfflinePlayers()) {
                        if (target == null && text.equalsIgnoreCase(known.getName())) target = known;
                    }
                    if (target == null) {
                        p.sendMessage(lgm.getMessage("Permissions.Command.PlayerNotFound", p, true).replace("%target%", text));
                        open();
                        return;
                    }
                    new PermissionEntriesMenu(playerMenuUtility, this, PermissionSubject.ofUser(target.getUniqueId(), String.valueOf(target.getName()))).open();
                }).requires("AdminPanel.Permissions.User"), null);
    }

    private void handleEntryClick(Player staff, OfflinePlayer target, InventoryClickEvent event) {
        new PermissionEntriesMenu(playerMenuUtility, this, PermissionSubject.ofUser(target.getUniqueId(), String.valueOf(target.getName()))).open();
    }

    public ItemStack getPageItem(OfflinePlayer target) {
        PermissionService service = AdminPanelMain.getPlugin().getPermissionService();
        Resolution resolution = service.resolve(target.getUniqueId());
        lgm.addPlaceholder(PlaceholderType.ITEM, "%target%", String.valueOf(target.getName()), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%groups%", resolution.groups().isEmpty() ? "-" : String.join(", ", resolution.groups()), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%entries%", service.nodesOf(target.getUniqueId()).size(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%online%", target.isOnline(), false);
        ItemStack item = lgm.getItem(PATH + "Entry", playerMenuUtility.getOwner(), true);
        if (item.getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(target);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
