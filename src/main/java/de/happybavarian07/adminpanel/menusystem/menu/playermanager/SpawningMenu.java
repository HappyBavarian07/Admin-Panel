package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpawningMenu extends PaginatedMenu<EntityType> {
    private final List<EntityType> entityTypes = new ArrayList<>();

    public SpawningMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Spawner");
        Collections.addAll(entityTypes, EntityType.values());
        entityTypes.removeIf(entity -> entity == null ||
                entity.equals(EntityType.PLAYER) ||
                entity.equals(EntityType.ITEM_FRAME) ||
                entity.equals(EntityType.DROPPED_ITEM) ||
                entity.equals(EntityType.AREA_EFFECT_CLOUD) ||
                entity.equals(EntityType.ENDER_SIGNAL) ||
                entity.equals(EntityType.UNKNOWN));
        setPaginatedData(entityTypes, this::getPageItem);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.SpawningMenu", playerMenuUtility.getTarget());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "SpawningMenu";
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
    }

    @Override
    protected void handlePageItemClick(int indexOnPage, ItemStack item, InventoryClickEvent e) {
        if (indexOnPage < 0 || indexOnPage >= entityTypes.size()) return;
        EntityType type = entityTypes.get(indexOnPage);
        try {
            Location loc = playerMenuUtility.getTarget().getLocation().add(0, 2, 0);
            playerMenuUtility.getTarget().getWorld().spawnEntity(loc, type);
        } catch (NullPointerException | IllegalArgumentException ignored) {
        }
    }

    @Override
    protected void handleCustomItemClick(int slot, ItemStack item, InventoryClickEvent e) {
    }

    public ItemStack getPageItem(EntityType type) {
        ItemStack item = new ItemStack(Material.GHAST_SPAWN_EGG);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utils.chat("&a" + type.getName()));
        item.setItemMeta(meta);
        return item;
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
