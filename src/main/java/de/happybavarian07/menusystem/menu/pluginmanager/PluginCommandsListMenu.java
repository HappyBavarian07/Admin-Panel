package de.happybavarian07.menusystem.menu.pluginmanager;

import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.menusystem.PaginatedMenu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.utils.Utils;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PluginCommandsListMenu extends PaginatedMenu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();
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
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String path = "PluginManager.Commands.";
        Map<String, Map<String, Object>> commands = currentPlugin.getDescription().getCommands();

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta()) return;
        if (item.getType().equals(Material.COMMAND_BLOCK)) {
            if (!player.hasPermission("AdminPanel.PluginManager.PluginSettings.Open")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginCommandSettingsMenu(playerMenuUtility, ((JavaPlugin) currentPlugin).getCommand(item.getItemMeta().getDisplayName())).open();
        } else if (item.equals(lgm.getItem("General.Close", null))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginSettingsMenu(playerMenuUtility, currentPlugin).open();
        } else if (item.equals(lgm.getItem("General.Left", null))) {
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
            if (!((index + 1) >= commands.size())) {
                page = page + 1;
                super.open();
            } else {
                player.sendMessage(lgm.getMessage("Player.General.AlreadyOnLastPage", player));
            }
        } else if (item.equals(lgm.getItem("General.Refresh", player))) {
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

        Map<String, Map<String, Object>> commands = currentPlugin.getDescription().getCommands();
        ///////////////////////////////////// Pagination loop template
        if (commands != null && !commands.isEmpty()) {
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
