package de.happybavarian07.menusystem.menu.worldmanager.time;

import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.menusystem.menu.worldmanager.WorldSettingsMenu;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class TimeChangeMenu extends Menu {
    private final Main plugin = Main.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();
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
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String itemPath = "WorldManager.Time.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta()) return;
        if (item.equals(lgm.getItem(itemPath + Time.SUNRISE, player))) {
            world.setTime(Time.SUNRISE.getTime());
        } else if (item.equals(lgm.getItem(itemPath + Time.DAY, player))) {
            world.setTime(Time.DAY.getTime());
        } else if (item.equals(lgm.getItem(itemPath + Time.MORNING, player))) {
            world.setTime(Time.MORNING.getTime());
        } else if (item.equals(lgm.getItem(itemPath + Time.NOON, player))) {
            world.setTime(Time.NOON.getTime());
        } else if (item.equals(lgm.getItem(itemPath + Time.AFTERNOON, player))) {
            world.setTime(Time.AFTERNOON.getTime());
        } else if (item.equals(lgm.getItem(itemPath + Time.SUNSET, player))) {
            world.setTime(Time.SUNSET.getTime());
        } else if (item.equals(lgm.getItem(itemPath + Time.NIGHT, player))) {
            world.setTime(Time.NIGHT.getTime());
        } else if (item.equals(lgm.getItem(itemPath + Time.MIDNIGHT, player))) {
            world.setTime(Time.MIDNIGHT.getTime());
        } else if (item.equals(lgm.getItem("General.Close", null))) {
            if(!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new WorldSettingsMenu(playerMenuUtility, world).open();
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String itemPath = "WorldManager.Time.";

        inventory.setItem(0, lgm.getItem(itemPath + Time.SUNRISE, player));
        inventory.setItem(1, lgm.getItem(itemPath + Time.DAY, player));
        inventory.setItem(2, lgm.getItem(itemPath + Time.MORNING, player));
        inventory.setItem(3, lgm.getItem(itemPath + Time.NOON, player));
        inventory.setItem(4, lgm.getItem(itemPath + Time.AFTERNOON, player));
        inventory.setItem(5, lgm.getItem(itemPath + Time.SUNSET, player));
        inventory.setItem(6, lgm.getItem(itemPath + Time.NIGHT, player));
        inventory.setItem(7, lgm.getItem(itemPath + Time.MIDNIGHT, player));
        inventory.setItem(8, lgm.getItem("General.Close", player));
    }
}
