package de.happybavarian07.adminpanel.menusystem.menu.playermanager;/*
 * @Author HappyBavarian07
 * @Date 27.11.2022 | 10:51
 */

import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PaginatedMenu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.bukkit.Bukkit.getServer;

public class CustomPlayerSelector<T, R> extends PaginatedMenu {
    private final Function<T, R> functionToExecute;
    private final Map<UUID, InventoryAction> clickedPlayers = new HashMap<>();
    private final Menu oldMenu;
    private final List<Player> players;
    private final String action;
    private final String infoItemExtraInfos;
    private final boolean multiSelect;
    // T is either a UUID or a Map<UUID, InventoryAction> (If you want to use Multi-Select)
    // This Predicate is used for making the Program using this Class able to filter on what actions they want to have the Name in what Color.
    private final Function<Map.Entry<UUID, InventoryAction>, ChatColor> functionForDecidingItemColor;
    private CompletableFuture<R> future;

    /**
     * This Constructor is used to set up a Custom Player Selector Menu.<br>
     * If you want to use Multi-Select, you need to have T as a {@code Map<UUID, InventoryAction>}.
     * <br>
     * <br>
     * The following Values can be null if Multi-Select isn't enabled:<br>
     * - functionForDecidingItemColor<br>
     *
     * @param action                       Info for the Player what the Menu is used for.
     *                                     (Example: "Select a Player to invite to your Guild")
     * @param infoItemExtraInfos           Extra Infos for the Info Item.
     *                                     (Example: "(Green = Online, Red = Offline, Black = Inactive)")
     * @param playerMenuUtility            The PlayerMenuUtility for the Menu.
     * @param functionToExecute            The Function that should be executed after the Player selected a Player.
     *                                     (If Multi-Select is enabled, this will first be executed after the Player clicked the Confirm Button)
     * @param functionForDecidingItemColor The Function that should be executed to decide what Color the Name of the Player should have.
     * @param oldMenu                      The Menu that should be opened after the Player selected a Player.
     * @param players                      The List of Players that should be displayed in the Menu.
     * @param multiSelect                  If the Player should be able to select multiple Players.
     */
    public CustomPlayerSelector(String action,
                                String infoItemExtraInfos,
                                PlayerMenuUtility playerMenuUtility,
                                Function<T, R> functionToExecute,
                                Function<Map.Entry<UUID, InventoryAction>, ChatColor> functionForDecidingItemColor,
                                Menu oldMenu,
                                List<Player> players,
                                boolean multiSelect) {
        super(playerMenuUtility);
        this.oldMenu = oldMenu;
        this.players = players;
        this.functionToExecute = functionToExecute;
        this.action = action;
        this.infoItemExtraInfos = infoItemExtraInfos;
        this.multiSelect = multiSelect;
        this.functionForDecidingItemColor = functionForDecidingItemColor;
    }

    public CompletableFuture<R> getFuture() {
        return future;
    }


    @Override
    public String getMenuName() {
        return "Player Selector";
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "CustomPlayerSelector";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item.getType().equals(lgm.getItem("CustomPlayerSelector.PlayerItem", null, false).getType())) {
            UUID target = Bukkit.getOfflinePlayer(ChatColor.stripColor(item.getItemMeta().getDisplayName())).getUniqueId();
            playerMenuUtility.setTargetUUID(target);
            if (multiSelect) {
                if (clickedPlayers.containsKey(target)) {
                    clickedPlayers.remove(target);
                } else {
                    clickedPlayers.put(target, e.getAction());
                }
                super.open();
                return;
            }
            future = CompletableFuture.supplyAsync(() -> {
                R value = functionToExecute.apply((T) target);
                future.complete(value);
                return value;
            });
            future.thenAccept(result -> Bukkit.getScheduler().runTask(plugin, () -> {
                if (oldMenu != null) {
                    oldMenu.open();
                } else {
                    player.closeInventory();
                }
            })).exceptionally(throwable -> {
                throwable.printStackTrace();
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%error%", throwable + ": " + throwable.getMessage(), false);
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%stacktrace%", Arrays.toString(throwable.getStackTrace()), false);
                player.sendMessage(lgm.getMessage("Player.General.Error", player, true));
                return null;
            });
        } else if (item.equals(lgm.getItem("General.Close", null, false))) {
            if (oldMenu != null) {
                oldMenu.open();
            } else {
                player.closeInventory();
            }
        } else if (item.equals(lgm.getItem("General.Left", null, false))) {
            if (page == 0) {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnFirstPage", player, true));
            } else {
                page = page - 1;
                super.open();
            }
        } else if (item.equals(lgm.getItem("General.Right", null, false))) {
            if (!((index + 1) >= players.size())) {
                page = page + 1;
                super.open();
            } else {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player, true));
            }
        } else if (item.equals(lgm.getItem("General.Refresh", null, false))) {
            super.open();
        } else if (multiSelect) {
            if (item.equals(lgm.getItem("CustomPlayerSelector.Confirm", null, false))) {
                try {
                    future = CompletableFuture.supplyAsync(() -> {
                        R value = functionToExecute.apply((T) clickedPlayers);
                        future.complete(value);
                        return value;
                    });
                } catch (ClassCastException throwable) {
                    System.out.println("Using Multi-Select you need to have T as a Map<UUID, InventoryAction> (If you see this message from a Plugin you didn't write. Contact the Plugin Dev and tell him about this Error)!");
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%error%", throwable + ": " + throwable.getMessage(), false);
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%stacktrace%", Arrays.toString(throwable.getStackTrace()), false);
                    player.sendMessage(lgm.getMessage("Player.General.Error", player, true));
                    if (oldMenu != null) {
                        oldMenu.open();
                    } else {
                        player.closeInventory();
                    }
                    return;
                }
                future.thenAccept(result -> Bukkit.getScheduler().runTask(plugin, () -> {
                    if (oldMenu != null) {
                        oldMenu.open();
                    } else {
                        player.closeInventory();
                    }
                })).exceptionally(throwable -> {
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%error%", throwable + ": " + throwable.getMessage(), false);
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%stacktrace%", Arrays.toString(throwable.getStackTrace()), false);
                    player.sendMessage(lgm.getMessage("Player.General.Error", player, true));
                    return null;
                });
            } // Cancel = Close Button, because the Button Stands for Cancelling out of the Action. The Player can still deselect Players if he wants to.
        }
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {

    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        ///////////////////////////////////// Pagination loop template
        if (!players.isEmpty()) {
            for (int i = 0; i < super.maxItemsPerPage; i++) {
                index = super.maxItemsPerPage * page + i;
                if (index >= players.size()) break;
                if (players.get(index) != null) {
                    ItemStack head = lgm.getItem("CustomPlayerSelector.PlayerItem", players.get(index), false);
                    SkullMeta meta = (SkullMeta) head.getItemMeta();
                    meta.setOwningPlayer(players.get(index));
                    if (multiSelect && clickedPlayers.containsKey(players.get(index).getUniqueId())) {
                        ChatColor color = functionForDecidingItemColor.apply(new AbstractMap.SimpleEntry<>(players.get(index).getUniqueId(), clickedPlayers.get(players.get(index).getUniqueId())));
                        meta.setDisplayName(color + players.get(index).getName());
                        meta.addEnchant(Enchantment.DURABILITY, 0, true);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                    head.setItemMeta(meta);
                    inventory.addItem(head);
                }
            }
        }
        ////////////////////////

        lgm.addPlaceholder(PlaceholderType.ITEM, "%action%", action, false);
        if (infoItemExtraInfos != null && !infoItemExtraInfos.isEmpty())
            lgm.addPlaceholder(PlaceholderType.ITEM, "%info%", infoItemExtraInfos, false);
        inventory.setItem(getSlot("CustomPlayerSelector.InfoItem", 4), lgm.getItem("CustomPlayerSelector.InfoItem", playerMenuUtility.getOwner(), false));

        // Confirm Button
        if (multiSelect) {
            inventory.setItem(getSlot("CustomPlayerSelector.Confirm", 46), lgm.getItem("CustomPlayerSelector.Confirm", playerMenuUtility.getOwner(), false));
            inventory.setItem(getSlot("CustomPlayerSelector.DeSelect", 47), lgm.getItem("CustomPlayerSelector.DeSelect", playerMenuUtility.getOwner(), false));
        }
    }
}
