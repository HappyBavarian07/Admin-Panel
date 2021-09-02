package de.happybavarian07.menusystem.menu.worldmanager;

import de.happybavarian07.main.Heads;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
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
    private final Main plugin = Main.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();

    public WorldSelectMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.WorldManager.open");
    }

    @Override
    public String getMenuName() {
        return Main.getPlugin().getLanguageManager().getMenuTitle("WorldManager.WorldSelector", null);
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

        ItemStack head = plugin.createSkull(Heads.WORLD.getPrefix() + Heads.WORLD.getTexture(), "World");

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);
        if(item.getType().equals(Material.PLAYER_HEAD)) {
            if(player.equals(Bukkit.getOfflinePlayer(item.getItemMeta().getDisplayName()))) {
                player.sendMessage(lgm.getMessage("Player.PlayerManager.ChooseYourself", player));
                return;
            }
            new WorldSettingsMenu(playerMenuUtility, getWorld(item.getItemMeta().getDisplayName())).open();
        } else if (item.equals(lgm.getItem("WorldManager.Create", player))) {
            new WorldCreateMenu(playerMenuUtility).open();
        } else if (item.equals(lgm.getItem("General.Close", null))) {
            if(!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new AdminPanelStartMenu(Main.getPlayerMenuUtility(player)).open();
        } else if (item.getType().equals(Material.DARK_OAK_BUTTON)) {
            if (item.equals(lgm.getItem("General.Left", null))) {
                if(!player.hasPermission("AdminPanel.Button.pageleft")) {
                    player.sendMessage(noPerms);
                    return;
                }
                if (page == 0) {
                    player.sendMessage(lgm.getMessage("Player.General.AlreadyOnFirstPage", player));
                } else {
                    page = page - 1;
                    super.open();
                }
            } else if (item.equals(lgm.getItem("General.Right", null))) {
                if(!player.hasPermission("AdminPanel.Button.pageright")) {
                    player.sendMessage(noPerms);
                    return;
                }
                if (!((index + 1) >= worlds.size())) {
                    page = page + 1;
                    super.open();
                } else {
                    player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player));
                }
            }
        } else if (item.equals(lgm.getItem("General.Refresh", null))) {
            super.open();
        }
    }

    public World getWorld(String name) {
        for(World world : getServer().getWorlds()) {
            if(world.getName().equals(name)) {
                return world;
            }
        }
        return null;
    }

    @Override
    public void setMenuItems() {
        Player player = playerMenuUtility.getOwner();
        addMenuBorder();

        inventory.setItem(47, lgm.getItem("WorldManager.Create", player));

        //The thing you will be looping through to place items
        List<World> worlds = new ArrayList<>(getServer().getWorlds());

        ///////////////////////////////////// Pagination loop template
        if(worlds != null && !worlds.isEmpty()) {
            for(int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if(index >= worlds.size()) break;
                if (worlds.get(index) != null){
                    ///////////////////////////

                    ItemStack head = plugin.createSkull(Heads.WORLD.getPrefix() + Heads.WORLD.getTexture(), worlds.get(index).getName());
                    ItemMeta meta = head.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    lore.add(Utils.getInstance().chat("&6Players: &b" + worlds.get(index).getPlayers().size()));
                    lore.add(Utils.getInstance().chat("&6Type: &b" + worlds.get(index).getWorldType()));
                    lore.add(Utils.getInstance().chat("&6Environment: &b" + worlds.get(index).getEnvironment()));
                    lore.add(Utils.getInstance().chat("&6GameTime: &b" + worlds.get(index).getGameTime()));
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
