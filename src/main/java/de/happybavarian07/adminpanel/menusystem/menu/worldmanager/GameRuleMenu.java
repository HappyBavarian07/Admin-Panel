package de.happybavarian07.adminpanel.menusystem.menu.worldmanager;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.world.MenuGameruleChangeEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.Head;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameRuleMenu extends PaginatedMenu<GameRule<?>> {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final List<GameRule<?>> gamerules;
    private final World world;

    public GameRuleMenu(PlayerMenuUtility playerMenuUtility, World world) {
        super(playerMenuUtility);
        this.world = world;
        setOpeningPermission("AdminPanel.WorldManager.Gamerules");
        gamerules = new ArrayList<>();
        for (String name : world.getGameRules()) {
            GameRule<?> rule = GameRule.getByName(name);
            if (rule != null) gamerules.add(rule);
        }
        setPaginatedData(gamerules, this::getPageItem);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("WorldManager.GameRuleMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "GameRuleMenu";
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
    protected void handlePageItemClick(int indexOnPage, ItemStack item, InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (indexOnPage < 0 || indexOnPage >= gamerules.size()) return;
        GameRule<?> gameRule = gamerules.get(indexOnPage);
        MenuGameruleChangeEvent gameruleChangeEvent = new MenuGameruleChangeEvent(
                player, world, gameRule, world.getGameRuleValue(gameRule));
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(gameruleChangeEvent);
            if (!gameruleChangeEvent.isCancelled()) {
                if (world.getGameRuleValue(gameRule) instanceof Boolean) {
                    boolean value = Boolean.parseBoolean((String) world.getGameRuleValue(gameRule));
                    world.setGameRule((GameRule<Boolean>) gameRule, !value);
                } else if (world.getGameRuleValue(gameRule) instanceof Integer) {
                    int value = Integer.parseInt((String) Objects.requireNonNull(world.getGameRuleValue(gameRule)));
                    if (event.isShiftClick()) {
                        if (event.isLeftClick()) {
                            value = value + 10;
                        } else if (event.isRightClick()) {
                            value = value - 10;
                        }
                    } else {
                        if (event.isLeftClick()) {
                            value = value + 1;
                        } else if (event.isRightClick()) {
                            value = value - 1;
                        }
                    }
                    world.setGameRule((GameRule<Integer>) gameRule, value);
                }
                super.open();
            }
        } catch (NotAPanelEventException notAPanelEventException) {
            notAPanelEventException.printStackTrace();
        }
    }

    @Override
    protected void handleCustomItemClick(int slot, ItemStack item, InventoryClickEvent event) {
    }

    public ItemStack getPageItem(GameRule<?> gameRule) {
        ItemStack item = lgm.getItem("General.EmptySlot", playerMenuUtility.getOwner(), false);
        List<String> lore = new ArrayList<>();
        Object value = world.getGameRuleValue(gameRule);
        if (value instanceof Boolean boolValue) {
            if (boolValue) {
                item = Head.BLANK_GREEN.getAsItem();
                lore.add(AdminPanelUtils.chat("&6Value: &atrue"));
            } else {
                item = Head.BLANK_RED.getAsItem();
                lore.add(AdminPanelUtils.chat("&6Value: &cfalse"));
            }
        } else if (value instanceof Integer intValue) {
            if (intValue > 0) {
                item = Head.WHITE_MINUS.getAsItem();
                lore.add(AdminPanelUtils.chat("&6Value: &a" + intValue));
            } else {
                item = Head.WHITE_PLUS.getAsItem();
                lore.add(AdminPanelUtils.chat("&6Value: &c" + intValue));
            }
        } else {
            lore.add(lgm.getMessage("WorldManager.GameRuleMenu.UnknownValue", playerMenuUtility.getOwner(), true));
        }
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(gameRule.getName());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
