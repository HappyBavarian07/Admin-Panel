package de.happybavarian07.menusystem.menu.worldmanager;

import de.happybavarian07.events.NotAPanelEventException;
import de.happybavarian07.events.world.MenuGameruleChangeEvent;
import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.main.Head;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.menusystem.PaginatedMenu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.utils.Utils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameRuleMenu extends PaginatedMenu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();
    private final World world;

    public GameRuleMenu(PlayerMenuUtility playerMenuUtility, World world) {
        super(playerMenuUtility);
        this.world = world;
        setOpeningPermission("AdminPanel.WorldManagment.Gamerules");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("WorldManager.GameRuleMenu", null);
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();

        List<String> gamerules = new ArrayList<>(Arrays.asList(world.getGameRules()));

        gamerules.removeIf(gmName -> Integer.getInteger(world.getGameRuleValue(gmName)) != null ||
                gmName.equals("maxCommandChainLength") || gmName.equals("maxEntityCramming") ||
                gmName.equals("spawnRadius") || gmName.equals("randomTickSpeed"));

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta()) return;
        if (item.getType().equals(Material.PLAYER_HEAD)) {
            int count = 0;
            for (String gmName : gamerules) {
                if (Boolean.parseBoolean(world.getGameRuleValue(gamerules.get(count))) && gmName.equals(item.getItemMeta().getDisplayName())) {
                    MenuGameruleChangeEvent gameruleChangeEvent = new MenuGameruleChangeEvent(
                            player, world, gmName, Boolean.parseBoolean(world.getGameRuleValue(gamerules.get(count))));
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(gameruleChangeEvent);
                        if (!gameruleChangeEvent.isCancelled()) {
                            world.setGameRuleValue(gmName, "false");
                            super.open();
                            return;
                        }
                    } catch (NotAPanelEventException notAPanelEventException) {
                        notAPanelEventException.printStackTrace();
                    }
                } else if (!Boolean.parseBoolean(world.getGameRuleValue(gamerules.get(count))) && gmName.equals(item.getItemMeta().getDisplayName())) {
                    MenuGameruleChangeEvent gameruleChangeEvent = new MenuGameruleChangeEvent(
                            player, world, gmName, Boolean.parseBoolean(world.getGameRuleValue(gamerules.get(count))));
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(gameruleChangeEvent);
                        if (!gameruleChangeEvent.isCancelled()) {
                            world.setGameRuleValue(gmName, "true");
                            super.open();
                            return;
                        }
                    } catch (NotAPanelEventException notAPanelEventException) {
                        notAPanelEventException.printStackTrace();
                    }
                }
                count++;
            }
        } else if (item.equals(lgm.getItem("General.Close", null))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new WorldSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), world).open();
        } else if (item.getType().equals(Material.DARK_OAK_BUTTON)) {
            if (item.equals(lgm.getItem("General.Left", null))) {
                if (!player.hasPermission("AdminPanel.Button.pageleft")) {
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
                if (!player.hasPermission("AdminPanel.Button.pageright")) {
                    player.sendMessage(noPerms);
                    return;
                }
                if (!((index + 1) >= gamerules.size())) {
                    page = page + 1;
                    super.open();
                } else {
                    player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player));
                }
            }
        } else if (item.equals(lgm.getItem("General.Refresh", null))) {
            if (!player.hasPermission("AdminPanel.Button.refresh")) {
                player.sendMessage(noPerms);
                return;
            }
            super.open();
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        //The thing you will be looping through to place items
        List<String> gamerules = new ArrayList<>(Arrays.asList(world.getGameRules()));

        gamerules.removeIf(gmName -> Integer.getInteger(world.getGameRuleValue(gmName)) != null ||
                gmName.equals("maxCommandChainLength") || gmName.equals("maxEntityCramming") ||
                gmName.equals("spawnRadius") || gmName.equals("randomTickSpeed"));

        ///////////////////////////////////// Pagination loop template
        if (gamerules != null && !gamerules.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= gamerules.size()) break;
                if (gamerules.get(index) != null) {
                    ///////////////////////////

                    List<String> lore = new ArrayList<>();
                    ItemStack head = lgm.getItem("General.EmptySlot", playerMenuUtility.getOwner());
                    if (Boolean.parseBoolean(world.getGameRuleValue(gamerules.get(index)))) {
                        head = Head.BLANK_GREEN.getAsItem();
                        lore.add(Utils.chat("&6Value: &atrue"));
                    } else if (!Boolean.parseBoolean(world.getGameRuleValue(gamerules.get(index)))) {
                        head = Head.BLANK_RED.getAsItem();
                        lore.add(Utils.chat("&6Value: &cfalse"));
                    }
                    ItemMeta meta = head.getItemMeta();
                    meta.setDisplayName(gamerules.get(index));
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
