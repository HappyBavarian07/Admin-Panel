package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

import org.bukkit.entity.Player;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
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
        setSearchable(name -> name);
        setPaginatedData(commandNames, this::getPageItem, (player, commandName, event) ->
                new PluginCommandSettingsMenu(playerMenuUtility, ((JavaPlugin) currentPlugin).getCommand(commandName)).open());
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

    public ItemStack getPageItem(String commandName) {
        Player viewer = playerMenuUtility.getOwner();
        PluginCommand command = ((JavaPlugin) currentPlugin).getCommand(commandName);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%command%", commandName, false);
        if (command == null) return lgm.getItem("PluginManager.CommandsList.NotFound", viewer, true);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%permission%", command.getPermission() != null ? command.getPermission() : "None", false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%permission_message%", command.getPermissionMessage() != null ? command.getPermissionMessage() : "None", false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%label%", command.getLabel(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%aliases%", command.getAliases().isEmpty() ? "None" : String.join(", ", command.getAliases()), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%usage%", command.getUsage(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%description%", command.getDescription(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%registered%", command.isRegistered() ? "Yes" : "No", false);
        return lgm.getItem("PluginManager.CommandsList.Entry", viewer, true);
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
