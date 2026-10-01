package de.happybavarian07.adminpanel.menusystem.menu.worldmanager.time;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.world.TimeChangeEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.worldmanager.WorldSettingsMenu;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class TimeChangeMenu extends Menu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final World world;

    public TimeChangeMenu(PlayerMenuUtility playerMenuUtility, World world) {
        super(playerMenuUtility);
        this.world = world;
        setOpeningPermission("AdminPanel.WorldManager.Time");
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

        registerButton(getSlot(path + Time.SUNRISE, 0), lgm.getItem(path + Time.SUNRISE, player, false), (p, e) -> setTimeWithEvent(p, Time.SUNRISE), null);
        registerButton(getSlot(path + Time.SUNRISE, 1), lgm.getItem(path + Time.DAY, player, false), (p, e) -> setTimeWithEvent(p, Time.DAY), null);
        registerButton(getSlot(path + Time.SUNRISE, 2), lgm.getItem(path + Time.MORNING, player, false), (p, e) -> setTimeWithEvent(p, Time.MORNING), null);
        registerButton(getSlot(path + Time.SUNRISE, 3), lgm.getItem(path + Time.NOON, player, false), (p, e) -> setTimeWithEvent(p, Time.NOON), null);
        registerButton(getSlot(path + Time.SUNRISE, 4), lgm.getItem(path + Time.AFTERNOON, player, false), (p, e) -> setTimeWithEvent(p, Time.AFTERNOON), null);
        registerButton(getSlot(path + Time.SUNRISE, 5), lgm.getItem(path + Time.SUNSET, player, false), (p, e) -> setTimeWithEvent(p, Time.SUNSET), null);
        registerButton(getSlot(path + Time.SUNRISE, 6), lgm.getItem(path + Time.NIGHT, player, false), (p, e) -> setTimeWithEvent(p, Time.NIGHT), null);
        registerButton(getSlot(path + Time.SUNRISE, 7), lgm.getItem(path + Time.MIDNIGHT, player, false), (p, e) -> setTimeWithEvent(p, Time.MIDNIGHT), null);
        registerButton(getSlot("General.Close", 8), lgm.getItem("General.Close", player, false),
                MenuAction.of((p, e) -> new WorldSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p), world).open()).requires("AdminPanel.Button.Close"), null);
    }

    private void setTimeWithEvent(Player player, Time time) {
        TimeChangeEvent changeEvent = new TimeChangeEvent(player, world, time);
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(changeEvent);
            if (!changeEvent.isCancelled()) {
                world.setTime(time.getTime());
            }
        } catch (NotAPanelEventException ex) {
            AdminPanelMain.logError("The time change action was not a valid panel event", ex);
        }
    }
}
