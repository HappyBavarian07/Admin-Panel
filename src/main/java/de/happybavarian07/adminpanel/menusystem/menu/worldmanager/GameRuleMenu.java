package de.happybavarian07.adminpanel.menusystem.menu.worldmanager;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.world.MenuGameruleChangeEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.Head;
import de.happybavarian07.adminpanel.menusystem.PaginatedMenu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GameRuleMenu extends PaginatedMenu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
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
    public String getConfigMenuAddonFeatureName() {
        return "GameRuleMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();

        List<GameRule<?>> gamerules = new ArrayList<>();
        for (String name : world.getGameRules()) {
            if (GameRule.getByName(name) == null) continue;

            gamerules.add(GameRule.getByName(name));
        }

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item == null || !item.hasItemMeta()) return;
        if (item.getType().equals(Material.PLAYER_HEAD)) {
            int count = 0;
            for (GameRule<?> gameRule : gamerules) {
                if (gameRule.getName().equals(item.getItemMeta().getDisplayName())) {
                    MenuGameruleChangeEvent gameruleChangeEvent = new MenuGameruleChangeEvent(
                            player, world, gameRule, world.getGameRuleValue(gamerules.get(count)));
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(gameruleChangeEvent);
                        if (!gameruleChangeEvent.isCancelled()) {
                            if (world.getGameRuleValue(gamerules.get(count)) instanceof Boolean) {
                                if (event.getAction().equals(InventoryAction.PICKUP_ALL)) {
                                    boolean value = Boolean.parseBoolean(world.getGameRuleValue(gamerules.get(count).getName()));
                                    world.setGameRule((GameRule<Boolean>) gameRule, !value);
                                } else {
                                    return;
                                }
                            } else if (world.getGameRuleValue(gamerules.get(count)) instanceof Integer) {
                                int value = Integer.parseInt(world.getGameRuleValue(gamerules.get(count).getName()));
                                System.out.println("Shift click: " + event.isShiftClick() + " | " + event.getClick().isShiftClick());
                                System.out.println("Value 1: " + value);
                                System.out.println("Inventory Action: " + event.getAction());
                                if (event.isShiftClick()) {
                                    if(!event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) return;
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
                                System.out.println("Value 2: " + value);
                                world.setGameRule((GameRule<Integer>) gameRule, value);
                            }
                            super.open();
                            return;
                        }
                    } catch (NotAPanelEventException notAPanelEventException) {
                        notAPanelEventException.printStackTrace();
                    }
                }
                count++;
            }
        } else if (item.equals(lgm.getItem("General.Close", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new WorldSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player), world).open();
        } else if (item.getType().equals(Material.DARK_OAK_BUTTON)) {
            if (item.equals(lgm.getItem("General.Left", null, false))) {
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
                if (!((index + 1) >= gamerules.size())) {
                    page = page + 1;
                    super.open();
                } else {
                    player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player, true));
                }
            }
        } else if (item.equals(lgm.getItem("General.Refresh", null, false))) {
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
        List<GameRule<?>> gamerules = new ArrayList<>();
        for (String name : world.getGameRules()) {
            if (GameRule.getByName(name) == null) continue;

            gamerules.add(GameRule.getByName(name));
        }

        ///////////////////////////////////// Pagination loop template
        if (!gamerules.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= gamerules.size()) break;
                if (gamerules.get(index) != null) {
                    ///////////////////////////

                    List<String> lore = new ArrayList<>();
                    ItemStack head = lgm.getItem("General.EmptySlot", playerMenuUtility.getOwner(), false);
                    Object value = world.getGameRuleValue(gamerules.get(index));
                    if (value instanceof Boolean) {
                        if ((Boolean) value) {
                            head = Head.BLANK_GREEN.getAsItem();
                            lore.add(Utils.chat("&6Value: &atrue"));
                        } else {
                            head = Head.BLANK_RED.getAsItem();
                            lore.add(Utils.chat("&6Value: &cfalse"));
                        }
                    } else if (value instanceof Integer) {
                        if ((Integer) value > 0) {
                            head = Head.BLANK_GREEN.getAsItem();
                            lore.add(Utils.chat("&6Value: &a" + value));
                        } else {
                            head = Head.BLANK_RED.getAsItem();
                            lore.add(Utils.chat("&6Value: &c" + value));
                        }
                    }
                    ItemMeta meta = head.getItemMeta();
                    meta.setDisplayName(gamerules.get(index).getName());
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
