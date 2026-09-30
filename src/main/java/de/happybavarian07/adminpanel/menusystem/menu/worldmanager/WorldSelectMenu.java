package de.happybavarian07.adminpanel.menusystem.menu.worldmanager;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.world.WorldSelectEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.Head;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.CoolStuffLib;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.actions.PermissionAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class WorldSelectMenu extends PaginatedMenu<World> {
    public WorldSelectMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission("AdminPanel.WorldManager.open");
        List<World> worlds = new ArrayList<>(getServer().getWorlds());
        setPaginatedData(worlds, this::getPageItem);
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
    }

    @Override
    public void postSetMenuItems() {
        Player player = playerMenuUtility.getOwner();
        registerButton(45, lgm.getItem("WorldManager.Create", player, false),
                new PermissionAction("AdminPanel.WorldManager.Create", (p, e) ->
                        new WorldCreateMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open(), lgm), null);
    }

    @Override
    protected void handlePageItemClick(int indexOnPage, ItemStack item, InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        List<World> worlds = new ArrayList<>(getServer().getWorlds());
        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);
        if (indexOnPage < 0 || indexOnPage >= worlds.size()) return;
        World world = worlds.get(indexOnPage);
        WorldSelectEvent worldSelectEvent = new WorldSelectEvent(player, world);
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(worldSelectEvent);
            if (!worldSelectEvent.isCancelled()) {
                new WorldSettingsMenu(CoolStuffLib.getLib().getPlayerMenuUtility(player.getUniqueId()), world).open();
            }
        } catch (NotAPanelEventException notAPanelEventException) {
            AdminPanelMain.logError("The world select action was not a valid panel event", notAPanelEventException);
        }
    }

    @Override
    protected void handleCustomItemClick(int slot, ItemStack item, InventoryClickEvent e) {
        // Logic is now handled by registered MenuActions in postSetMenuItems
    }

    public ItemStack getPageItem(World world) {
        ItemStack item = Head.WORLD.getAsItem();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(world.getName());
        List<String> lore = new ArrayList<>();
        lore.add(AdminPanelUtils.chat("&6Players: &6" + world.getPlayers().size()));
        lore.add(AdminPanelUtils.chat("&6Type: &6" + world.getWorldType().name()));
        lore.add(AdminPanelUtils.chat("&6Environment: &6" + world.getEnvironment().name()));
        lore.add(AdminPanelUtils.chat("&6GameTime: &6" + world.getGameTime()));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
