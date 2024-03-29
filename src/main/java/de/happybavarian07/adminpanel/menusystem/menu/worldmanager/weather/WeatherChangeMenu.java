package de.happybavarian07.adminpanel.menusystem.menu.worldmanager.weather;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.world.WeatherChangeEvent;
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

public class WeatherChangeMenu extends Menu {
    private final World world;

    public WeatherChangeMenu(PlayerMenuUtility playerMenuUtility, World world) {
        super(playerMenuUtility);
        this.world = world;
        setOpeningPermission("AdminPanel.WorldManagment.Weather");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("WorldManager.WeatherMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "WeatherChangeMenu";
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String itemPath = "WorldManager.Weather.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, false);

        WeatherChangeEvent changeEvent;
        if (item == null || !item.hasItemMeta()) return;
        if (item.equals(lgm.getItem(itemPath + Weather.CLEAR, player, false))) {
            changeEvent = new WeatherChangeEvent(player, world, Weather.CLEAR);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(changeEvent);
                if (!changeEvent.isCancelled()) {
                    world.setStorm(false);
                    world.setThundering(false);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(itemPath + Weather.RAINING, player, false))) {
            changeEvent = new WeatherChangeEvent(player, world, Weather.RAINING);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(changeEvent);
                if (!changeEvent.isCancelled()) {
                    world.setStorm(true);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(itemPath + Weather.THUNDERING, player, false))) {
            changeEvent = new WeatherChangeEvent(player, world, Weather.THUNDERING);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(changeEvent);
                if (!changeEvent.isCancelled()) {
                    world.setStorm(true);
                    world.setThundering(true);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("General.Close", player, false))) {
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
        String path = "WorldManager.Weather.";

        inventory.setItem(getSlot(path + Weather.CLEAR, 0), lgm.getItem(path + Weather.CLEAR, player, false));
        inventory.setItem(getSlot(path + Weather.RAINING, 1), lgm.getItem(path + Weather.RAINING, player, false));
        inventory.setItem(getSlot(path + Weather.THUNDERING, 2), lgm.getItem(path + Weather.THUNDERING, player, false));
        inventory.setItem(getSlot("General.Close", 8), lgm.getItem("General.Close", player, false));
    }
}
