package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.PermissionAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.lang.reflect.Field;

public class PluginCommandSettingsMenu extends Menu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final PluginCommand currentCommand;

    public PluginCommandSettingsMenu(PlayerMenuUtility playerMenuUtility, PluginCommand currentCommand) {
        super(playerMenuUtility);
        this.currentCommand = currentCommand;
        setOpeningPermission("AdminPanel.PluginManager.PluginSettings.Commands");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PluginManager.Commands.Settings", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PluginCommandSettingsMenu";
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        // Logic is now handled by registered MenuActions in setMenuItems
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {

    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "PluginManager.Settings.Commands.Settings.";
        registerButton(getSlot(path + "Register", 0), lgm.getItem(path + "Register", player, false), (p, e) -> {
            if (!currentCommand.isRegistered()) {
                currentCommand.register(getCommandMap());
                plugin.getDisabledCommands().remove(currentCommand.getName());
            }
        }, null);
        registerButton(getSlot(path + "Unregister", 1), lgm.getItem(path + "Unregister", player, false), (p, e) -> {
            if (currentCommand.isRegistered()) {
                currentCommand.unregister(getCommandMap());
                plugin.getDisabledCommands().add(currentCommand.getName());
            }
        }, null);
        registerButton(getSlot(path + "Execute", 2), lgm.getItem(path + "Execute", player, false), (p, e) -> {
            if (currentCommand.isRegistered()) {
                currentCommand.execute(p, currentCommand.getLabel(), new String[]{});
            }
        }, null);
        registerButton(getSlot("General.Close", 8), lgm.getItem("General.Close", player, false),
                new PermissionAction("AdminPanel.Button.Close", (p, e) -> {
                    playerMenuUtility.addData("CurrentSelectedPlugin", currentCommand.getPlugin());
                    new PluginCommandsListMenu(playerMenuUtility, this).open();
                }, lgm), null);
    }

    public CommandMap getCommandMap() {
        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            return (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
        } catch (Exception e) {
            AdminPanelMain.logError("Could not access the server command map", e);
            return null;
        }
    }

    @EventHandler
    public void onCmdPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().replaceFirst("/", "");
        if (plugin.getDisabledCommands().contains(command)) {
            event.setCancelled(true);
        }
    }
}
