package de.happybavarian07.adminpanel.menusystem.menu.worldmanager.weather;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.world.WeatherChangeEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.worldmanager.WorldSettingsMenu;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class WeatherChangeMenu extends Menu {
    private final World world;

    public WeatherChangeMenu(PlayerMenuUtility playerMenuUtility, World world) {
        super(playerMenuUtility);
        this.world = world;
        setOpeningPermission("AdminPanel.WorldManager.Weather");
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

        registerButton(getSlot(path + Weather.CLEAR, 0), lgm.getItem(path + Weather.CLEAR, player, false), (p, e) -> setWeatherWithEvent(p, Weather.CLEAR), null);
        registerButton(getSlot(path + Weather.RAINING, 1), lgm.getItem(path + Weather.RAINING, player, false), (p, e) -> setWeatherWithEvent(p, Weather.RAINING), null);
        registerButton(getSlot(path + Weather.THUNDERING, 2), lgm.getItem(path + Weather.THUNDERING, player, false), (p, e) -> setWeatherWithEvent(p, Weather.THUNDERING), null);
        registerButton(getSlot("General.Close", 8), lgm.getItem("General.Close", player, false),
                MenuAction.of((p, e) -> new WorldSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), world).open()).requires("AdminPanel.Button.Close"), null);
    }

    private void setWeatherWithEvent(Player player, Weather weather) {
        WeatherChangeEvent changeEvent = new WeatherChangeEvent(player, world, weather);
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(changeEvent);
            if (!changeEvent.isCancelled()) {
                if (weather == Weather.CLEAR) {
                    world.setStorm(false);
                    world.setThundering(false);
                } else if (weather == Weather.RAINING) {
                    world.setStorm(true);
                } else if (weather == Weather.THUNDERING) {
                    world.setStorm(true);
                    world.setThundering(true);
                }
            }
        } catch (NotAPanelEventException ex) {
            AdminPanelMain.logError("The weather change action was not a valid panel event", ex);
        }
    }
}
