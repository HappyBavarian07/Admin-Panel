package de.happybavarian07.adminpanel.menusystem.menu.worldmanager;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.worlds.WorldOperations;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * The players in one world. A click teleports you to the player, a right click sends the player to the spawn of the
 * main world.
 */
public class WorldPlayersMenu extends PaginatedMenu<Player> {
    private static final String PERMISSION = "AdminPanel.WorldManager.Players";

    private final World world;

    public WorldPlayersMenu(PlayerMenuUtility playerMenuUtility, World world) {
        super(playerMenuUtility);
        this.world = world;
        setOpeningPermission(PERMISSION);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("WorldManager.WorldPlayers", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "WorldPlayersMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void preSetMenuItems() {
        setPaginatedData(world.getPlayers(), this::getPageItem, this::handleEntryClick);
        int perPage = getPaginatedItemSlots().length;
        if (perPage > 0) page = Math.min(page, Math.max(0, (world.getPlayers().size() - 1) / perPage));
    }

    @Override
    public void postSetMenuItems() {
        registerButton(getSlot("General.Close", 53), lgm.getItem("General.Close", playerMenuUtility.getOwner(), false),
                MenuAction.of((p, e) -> new WorldSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), world).open())
                        .requires("AdminPanel.Button.Close"));
    }

    private void handleEntryClick(Player staff, Player entry, InventoryClickEvent event) {
        MenuAction.of((p, e) -> {
            if (!entry.isOnline()) {
                refresh();
                return;
            }
            if (e.isRightClick()) {
                entry.teleport(WorldOperations.mainWorld().getSpawnLocation());
                CommandText.send(lgm, p, "WorldPlayers.Sent", "%target%", entry.getName());
                refresh();
            } else {
                p.closeInventory();
                p.teleport(entry);
                CommandText.send(lgm, p, "WorldPlayers.Teleported", "%target%", entry.getName());
            }
        }).requires(PERMISSION).execute(staff, event);
    }

    public ItemStack getPageItem(Player entry) {
        ItemStack item = lgm.getItem("WorldManager.Players.Entry", entry, false);
        if (item.getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(entry);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
