package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

import de.happybavarian07.adminpanel.language.LanguageManager;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class PluginDescriptionMenu extends Menu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();

    public PluginDescriptionMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PluginManager.PluginDescriptionMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PluginDescriptionMenu";
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String itemPath = "PluginManager.PluginDescriptionMenu.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item == null || !item.hasItemMeta()) return;
        if (item.equals(lgm.getItem(itemPath + "LoadFromResourceID", player, false))) {
            // Try to read ResourceID from Description Manager and get description via that and set it to the plugin
            // If not found send a message asking them to set it in chat
            int resourceID = plugin.getPluginDescriptionManager().getResourceID(playerMenuUtility.getData("CurrentSelectedPlugin", Plugin.class).getName());
            if (resourceID != -1) {
                // Get Description from ResourceID
                String description = plugin.getPluginDescriptionManager().getDescriptionFromResourceID(resourceID);
                plugin.getPluginDescriptionManager().addPluginDescription(playerMenuUtility.getData("CurrentSelectedPlugin", Plugin.class), description);
                player.sendMessage(lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.LoadedFromResourceID", player, true));
                new PluginSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
            } else {
                player.sendMessage(lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.SetResourceInChat", player, true));
                player.closeInventory();
                playerMenuUtility.addData("SetResourceIDInChat", true);
            }
        } else if (item.equals(lgm.getItem(itemPath + "SetWithChat", player, false))) {
            player.sendMessage(lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.SetDescriptionInChat", player, true));
            player.closeInventory();
            playerMenuUtility.addData("SetDescriptionInChat", true);
        } else if (item.equals(lgm.getItem("General.Close", null, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        }
    }

    @EventHandler
    public void onChat(PlayerChatEvent e) {
        Player player = e.getPlayer();
        if (playerMenuUtility.getOwner() != player) return;

        if (playerMenuUtility.getData("SetDescriptionInChat") != null) {
            e.setCancelled(true);
            plugin.getPluginDescriptionManager().addPluginDescription(playerMenuUtility.getData("CurrentSelectedPlugin", Plugin.class), e.getMessage());
            player.sendMessage(lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.SuccessfullySetDescriptionInChat", player, true));
            new PluginSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
            playerMenuUtility.removeData("SetDescriptionInChat");
        } else if (playerMenuUtility.getData("SetResourceIDInChat") != null) {
            e.setCancelled(true);
            try {
                int resourceID = Integer.parseInt(e.getMessage());
                plugin.getPluginDescriptionManager().addPluginDescription(playerMenuUtility.getData("CurrentSelectedPlugin", Plugin.class), plugin.getPluginDescriptionManager().getDescriptionFromResourceID(resourceID));
                player.sendMessage(lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.SuccessfullySetResourceInChat", player, true));
                new PluginSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
                playerMenuUtility.removeData("SetResourceIDInChat");
            } catch (NumberFormatException ex) {
                player.sendMessage(lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.SetResourceInChatError", player, true));
            }
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
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "PluginManager.PluginDescriptionMenu.";

        inventory.setItem(getSlot(path + "LoadFromResourceID", 3), lgm.getItem(path + "LoadFromResourceID", player, false));
        inventory.setItem(getSlot(path + "SetWithChat", 5), lgm.getItem(path + "SetWithChat", player, false));
        inventory.setItem(8, lgm.getItem("General.Close", player, false));
    }
}
