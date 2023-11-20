package de.happybavarian07.adminpanel.menusystem.menu.worldmanager.time;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.world.TimeChangeEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.menusystem.menu.worldmanager.WorldSettingsMenu;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class TimeChangeMenu extends Menu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final World world;

    public TimeChangeMenu(PlayerMenuUtility playerMenuUtility, World world) {
        super(playerMenuUtility);
        this.world = world;
        setOpeningPermission("AdminPanel.WorldManagment.Time");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("WorldManager.TimeMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "TimeChangeMenu";
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String itemPath = "WorldManager.Time.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        TimeChangeEvent changeEvent;
        if (item == null || !item.hasItemMeta()) return;
        if (item.equals(lgm.getItem(itemPath + Time.SUNRISE, player, false))) {
            changeEvent = new TimeChangeEvent(player, world, Time.SUNRISE);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(changeEvent);
                if (!changeEvent.isCancelled()) {
                    world.setTime(Time.SUNRISE.getTime());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(itemPath + Time.DAY, player, false))) {
            changeEvent = new TimeChangeEvent(player, world, Time.DAY);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(changeEvent);
                if (!changeEvent.isCancelled()) {
                    world.setTime(Time.DAY.getTime());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(itemPath + Time.MORNING, player, false))) {
            changeEvent = new TimeChangeEvent(player, world, Time.MORNING);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(changeEvent);
                if (!changeEvent.isCancelled()) {
                    world.setTime(Time.MORNING.getTime());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(itemPath + Time.NOON, player, false))) {
            changeEvent = new TimeChangeEvent(player, world, Time.NOON);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(changeEvent);
                if (!changeEvent.isCancelled()) {
                    world.setTime(Time.NOON.getTime());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(itemPath + Time.AFTERNOON, player, false))) {
            changeEvent = new TimeChangeEvent(player, world, Time.AFTERNOON);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(changeEvent);
                if (!changeEvent.isCancelled()) {
                    world.setTime(Time.AFTERNOON.getTime());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(itemPath + Time.SUNSET, player, false))) {
            changeEvent = new TimeChangeEvent(player, world, Time.SUNSET);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(changeEvent);
                if (!changeEvent.isCancelled()) {
                    world.setTime(Time.SUNSET.getTime());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(itemPath + Time.NIGHT, player, false))) {
            changeEvent = new TimeChangeEvent(player, world, Time.NIGHT);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(changeEvent);
                if (!changeEvent.isCancelled()) {
                    world.setTime(Time.NIGHT.getTime());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(itemPath + Time.MIDNIGHT, player, false))) {
            changeEvent = new TimeChangeEvent(player, world, Time.MIDNIGHT);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(changeEvent);
                if (!changeEvent.isCancelled()) {
                    world.setTime(Time.MIDNIGHT.getTime());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("General.Close", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new WorldSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), world).open();
        }
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {

    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "WorldManager.Time.";

        inventory.setItem(getSlot(path + Time.SUNRISE, 0), lgm.getItem(path + Time.SUNRISE, player, false));
        inventory.setItem(getSlot(path + Time.SUNRISE, 1), lgm.getItem(path + Time.DAY, player, false));
        inventory.setItem(getSlot(path + Time.SUNRISE, 2), lgm.getItem(path + Time.MORNING, player, false));
        inventory.setItem(getSlot(path + Time.SUNRISE, 3), lgm.getItem(path + Time.NOON, player, false));
        inventory.setItem(getSlot(path + Time.SUNRISE, 4), lgm.getItem(path + Time.AFTERNOON, player, false));
        inventory.setItem(getSlot(path + Time.SUNRISE, 5), lgm.getItem(path + Time.SUNSET, player, false));
        inventory.setItem(getSlot(path + Time.SUNRISE, 6), lgm.getItem(path + Time.NIGHT, player, false));
        inventory.setItem(getSlot(path + Time.SUNRISE, 7), lgm.getItem(path + Time.MIDNIGHT, player, false));
        inventory.setItem(getSlot("General.Close", 8), lgm.getItem("General.Close", player, false));
    }
}
