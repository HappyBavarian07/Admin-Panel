package de.happybavarian07.menusystem.menu.pluginmanager;

import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PluginCommandSettingsMenu extends Menu implements Listener {
    private final Main plugin = Main.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();
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
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String path = "PluginManager.Settings.Commands.Settings.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta()) return;
        if (item.equals(lgm.getItem(path + "Register", player))) {
            if(!currentCommand.isRegistered()) {
                currentCommand.register(getCommandMap());
                plugin.getDisabledCommands().remove(currentCommand.getName());
            }
        } else if (item.equals(lgm.getItem(path + "Unregister", player))) {
            if(currentCommand.isRegistered()) {
                currentCommand.unregister(getCommandMap());
                plugin.getDisabledCommands().add(currentCommand.getName());
            }
        } else if (item.equals(lgm.getItem(path + "Execute", player))) {
            if(currentCommand.isRegistered()) {
                currentCommand.execute(player, currentCommand.getLabel(), new String[] {});
            }
        } else if (item.equals(lgm.getItem("General.Close", player))) {
            new PluginCommandsListMenu(playerMenuUtility, currentCommand.getPlugin()).open();
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "PluginManager.Settings.Commands.Settings.";
        inventory.setItem(0, lgm.getItem(path + "Register", player));
        inventory.setItem(1, lgm.getItem(path + "Unregister", player));
        inventory.setItem(2, lgm.getItem(path + "Execute", player));
        inventory.setItem(8, lgm.getItem("General.Close", player));
    }

    public CommandMap getCommandMap() {
        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            return (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @EventHandler
    public void onCmdPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().replaceFirst("/", "");
        if(plugin.getDisabledCommands().contains(command)) {
            event.setCancelled(true);
        }
    }
}
