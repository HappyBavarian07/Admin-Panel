package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

import de.happybavarian07.adminpanel.menusystem.PaginatedMenu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PluginCommandsListMenu extends PaginatedMenu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final Plugin currentPlugin;

    public PluginCommandsListMenu(PlayerMenuUtility playerMenuUtility, Plugin currentPlugin) {
        super(playerMenuUtility);
        this.currentPlugin = currentPlugin;
        setOpeningPermission("AdminPanel.PluginManager.PluginSettings.Commands");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PluginManager.Commands.Menu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PluginCommandsListMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String path = "PluginManager.Commands.";
        Map<String, Map<String, Object>> commands = currentPlugin.getDescription().getCommands();

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item == null || !item.hasItemMeta()) return;
        if (item.getType().equals(Material.COMMAND_BLOCK)) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Open")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginCommandSettingsMenu(playerMenuUtility, ((JavaPlugin) currentPlugin).getCommand(item.getItemMeta().getDisplayName())).open();
        } else if (item.equals(lgm.getItem("General.Close", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            playerMenuUtility.setData("CurrentSelectedPlugin", currentPlugin, true);
            new PluginSettingsMenu(playerMenuUtility).open();
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
            if (!((index + 1) >= commands.size())) {
                page = page + 1;
                super.open();
            } else {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player, true));
            }
        } else if (item.equals(lgm.getItem("General.Refresh", player, false))) {
            if (!player.hasPermission("AdminPanel.Button.refresh")) {
                player.sendMessage(noPerms);
                return;
            }
            super.open();
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

        Map<String, Map<String, Object>> commands = currentPlugin.getDescription().getCommands();
        ///////////////////////////////////// Pagination loop template
        if (!commands.isEmpty()) {
            int i = 0;
            for (String cmdName : commands.keySet()) {
                index = super.maxItemsPerPage * page + i;
                if (index >= commands.size()) break;
                if (commands.get(cmdName) != null) {
                    ///////////////////////////

                    PluginCommand currentCommand = ((JavaPlugin) currentPlugin).getCommand(cmdName);
                    if (currentCommand == null) continue;
                    ItemStack command = new ItemStack(Material.COMMAND_BLOCK, 1);
                    ItemMeta commandMeta = command.getItemMeta();
                    commandMeta.setDisplayName(cmdName);
                    List<String> lore = new ArrayList<>();
                    Utils utils = Utils.getInstance();
                    lore.add(Utils.chat("&6Permission: &a" + currentCommand.getPermission()));
                    lore.add(Utils.chat("&6Permission-Message: &a" + currentCommand.getPermissionMessage()));
                    lore.add(Utils.chat("&6Label: &a" + currentCommand.getLabel()));
                    lore.add(Utils.chat("&6Aliases: &a" + currentCommand.getAliases()));
                    lore.add(Utils.chat("&6Usage: &a" + currentCommand.getUsage()));
                    lore.add(Utils.chat("&6Description: &a" + currentCommand.getDescription()));
                    lore.add(Utils.chat("&6Registered: &a" + currentCommand.isRegistered()));
                    commandMeta.setLore(lore);
                    command.setItemMeta(commandMeta);
                    inventory.addItem(command);

                    ////////////////////////
                }
                i++;
            }
        }
        ////////////////////////
    }
}
