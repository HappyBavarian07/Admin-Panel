package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.PaginatedMenu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SpawningMenu extends PaginatedMenu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final UUID targetUUID;

    public SpawningMenu(PlayerMenuUtility playerMenuUtility, UUID targetUUID) {
        super(playerMenuUtility);
        this.targetUUID = targetUUID;
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.SpawningMenu", Bukkit.getPlayer(targetUUID));
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();
        List<EntityType> entityList = new ArrayList<>();
        Collections.addAll(entityList, EntityType.values());
        entityList.removeIf(entity -> entity == null ||
                entity.equals(EntityType.PLAYER) ||
                entity.equals(EntityType.ITEM_FRAME) ||
                entity.equals(EntityType.DROPPED_ITEM) ||
                entity.equals(EntityType.AREA_EFFECT_CLOUD) ||
                entity.equals(EntityType.ENDER_SIGNAL) ||
                entity.equals(EntityType.UNKNOWN));
        if (item.getType().equals(Material.GHAST_SPAWN_EGG)) {
            try {
                Location loc = Bukkit.getPlayer(targetUUID).getLocation().add(0, 2, 0);
                Bukkit.getPlayer(targetUUID).getWorld().spawnEntity(loc,
                        EntityType.valueOf(ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase()));
            } catch (NullPointerException | IllegalArgumentException ignored) {
            }
        } else if (item.equals(lgm.getItem("General.Close", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(lgm.getMessage("Player.General.NoPermissions", player, true));
                return;
            }
            new PlayerActionsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), targetUUID).open();
        } else if (item.equals(lgm.getItem("General.Left", null, false))) {
            if (page == 0) {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnFirstPage", player, true));
            } else {
                page = page - 1;
                super.open();
            }
        } else if (item.equals(lgm.getItem("General.Right", null, false))) {
            if (!((index + 1) >= entityList.size())) {
                page = page + 1;
                super.open();
            } else {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player, true));
            }
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        List<EntityType> entityList = new ArrayList<>();
        Collections.addAll(entityList, EntityType.values());
        entityList.removeIf(entity -> entity == null ||
                entity.equals(EntityType.PLAYER) ||
                entity.equals(EntityType.ITEM_FRAME) ||
                entity.equals(EntityType.DROPPED_ITEM) ||
                entity.equals(EntityType.AREA_EFFECT_CLOUD) ||
                entity.equals(EntityType.ENDER_SIGNAL) ||
                entity.equals(EntityType.UNKNOWN));

        if (entityList != null && !entityList.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= entityList.size()) break;
                if (entityList.get(index) != null) {
                    ///////////////////////////

                    ItemStack item = new ItemStack(Material.GHAST_SPAWN_EGG);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a" + entityList.get(index).getName()));
                    item.setItemMeta(meta);
                    inventory.addItem(item);

                    ////////////////////////
                }
            }
        }
    }
}
