package de.happybavarian07.menusystem.menu.worldmanager;

import de.happybavarian07.events.NotAPanelEventException;
import de.happybavarian07.events.world.WorldSelectEvent;
import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.main.Head;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.menusystem.PaginatedMenu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class WorldSelectMenu extends PaginatedMenu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    public WorldSelectMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.WorldManager.open");
    }

    @Override
    public String getMenuName() {
        return AdminPanelMain.getPlugin().getLanguageManager().getMenuTitle("WorldManager.WorldSelector", null);
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();
        List<World> worlds = new ArrayList<>(getServer().getWorlds());

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);
        if (item.getType().equals(Material.PLAYER_HEAD)) {
            WorldSelectEvent worldSelectEvent = new WorldSelectEvent(player, Bukkit.getWorld(item.getItemMeta().getDisplayName()));
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(worldSelectEvent);
                if (!worldSelectEvent.isCancelled()) {
                    new WorldSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), Bukkit.getWorld(item.getItemMeta().getDisplayName())).open();
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("WorldManager.Create", player, false))) {
            new WorldCreateMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem("General.Close", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem("General.Left", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.pageleft")) {
                player.sendMessage(noPerms);
                return;
            }
            if (page == 0) {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnFirstPage", player, true));
            } else {
                page = page - 1;
                super.open();
            }
        } else if (item.equals(lgm.getItem("General.Right", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.pageright")) {
                player.sendMessage(noPerms);
                return;
            }
            if (!((index + 1) >= worlds.size())) {
                page = page + 1;
                super.open();
            } else {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player, true));
            }
        } else if (item.equals(lgm.getItem("General.Refresh", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.refresh")) {
                player.sendMessage(noPerms);
                return;
            }
            super.open();
        }
    }

    public World getWorld(String name) {
        for (World world : getServer().getWorlds()) {
            if (world.getName().equals(name)) {
                return world;
            }
        }
        return null;
    }

    @Override
    public void setMenuItems() {
        Player player = playerMenuUtility.getOwner();
        addMenuBorder();

        inventory.setItem(getSlot("WorldManager.Create", 47), lgm.getItem("WorldManager.Create", player, false));

        //The thing you will be looping through to place items
        List<World> worlds = new ArrayList<>(getServer().getWorlds());

        ///////////////////////////////////// Pagination loop template
        if (worlds != null && !worlds.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= worlds.size()) break;
                if (worlds.get(index) != null) {
                    ///////////////////////////

                    ItemStack head = AdminPanelMain.getAPI().createSkull(Head.WORLD, worlds.get(index).getName());
                    ItemMeta meta = head.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    lore.add(Utils.chat("&6Players: &b" + worlds.get(index).getPlayers().size()));
                    lore.add(Utils.chat("&6Type: &b" + worlds.get(index).getWorldType()));
                    lore.add(Utils.chat("&6Environment: &b" + worlds.get(index).getEnvironment()));
                    lore.add(Utils.chat("&6GameTime: &b" + worlds.get(index).getGameTime()));
                    meta.setLore(lore);
                    head.setItemMeta(meta);
                    inventory.addItem(head);

                    ////////////////////////
                }
            }
        }
        ////////////////////////
    }
}
