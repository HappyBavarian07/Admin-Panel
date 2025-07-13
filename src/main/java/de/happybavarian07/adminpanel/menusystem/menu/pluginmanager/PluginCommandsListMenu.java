package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
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

public class PluginCommandsListMenu extends PaginatedMenu<String> {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final Plugin currentPlugin;
    private final List<String> commandNames;

    public PluginCommandsListMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        this.currentPlugin = playerMenuUtility.getData("CurrentSelectedPlugin", Plugin.class);
        setOpeningPermission("AdminPanel.PluginManager.PluginSettings.Commands");
        commandNames = new ArrayList<>(currentPlugin.getDescription().getCommands().keySet());
        setPaginatedData(commandNames, this::getPageItem);
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
    public void preSetMenuItems() {
    }

    @Override
    public void postSetMenuItems() {
    }

    @Override
    protected void handlePageItemClick(int indexOnPage, ItemStack item, InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (indexOnPage < 0 || indexOnPage >= commandNames.size()) return;
        String commandName = commandNames.get(indexOnPage);
        new PluginCommandSettingsMenu(playerMenuUtility, ((JavaPlugin) currentPlugin).getCommand(commandName)).open();
    }

    @Override
    protected void handleCustomItemClick(int slot, ItemStack item, InventoryClickEvent e) {
    }

    public ItemStack getPageItem(String commandName) {
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(commandName);
        // Permission, Permission-Message, Label, Aliases, Usage, Description, Registered
        List<String> lore = new ArrayList<>();
        PluginCommand command = ((JavaPlugin) currentPlugin).getCommand(commandName);
        if (command != null) {
            String permission = command.getPermission() != null ? command.getPermission() : "None";
            String permissionMessage = command.getPermissionMessage() != null ? command.getPermissionMessage() : "None";
            String label = command.getLabel();
            String aliases = command.getAliases().isEmpty() ? "None" : String.join(", ", command.getAliases());
            String usage = command.getUsage();
            String description = command.getDescription();
            boolean registered = plugin.getServer().getPluginCommand(commandName) != null;

            lore.add("&7Permission: &f" + permission);
            lore.add("&7Permission Message: &f" + permissionMessage);
            lore.add("&7Label: &f" + label);
            lore.add("&7Aliases: &f" + aliases);
            lore.add("&7Usage: &f" + usage);
            lore.add("&7Description: &f" + description);
            lore.add("&7Registered: &f" + (registered ? "Yes" : "No"));
        } else {
            lore.add("&cCommand not found in plugin description!");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
