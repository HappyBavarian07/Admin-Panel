package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.CoolStuffLib;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.PermissionAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.Plugin;

public class PluginDescriptionMenu extends Menu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();

    public PluginDescriptionMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PluginManager.PluginSettings.PluginDescription");
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
        // Logic is now handled by registered MenuActions in setMenuItems
    }

    @EventHandler
    public void onChat(PlayerChatEvent e) {
        Player player = e.getPlayer();
        if (playerMenuUtility.getOwner() != player) return;

        if (playerMenuUtility.getData("SetDescriptionInChat") != null) {
            e.setCancelled(true);
            plugin.getPluginDescriptionManager().addPluginDescription(playerMenuUtility.getData("CurrentSelectedPlugin", Plugin.class), e.getMessage());
            player.sendMessage(lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.SuccessfullySetDescriptionInChat", player, true));
            new PluginSettingsMenu(CoolStuffLib.getLib().getPlayerMenuUtility(player.getUniqueId())).open();
            playerMenuUtility.removeData("SetDescriptionInChat");
        } else if (playerMenuUtility.getData("SetResourceIDInChat") != null) {
            e.setCancelled(true);
            try {
                int resourceID = Integer.parseInt(e.getMessage());
                plugin.getPluginDescriptionManager().addPluginDescription(playerMenuUtility.getData("CurrentSelectedPlugin", Plugin.class), plugin.getPluginDescriptionManager().getDescriptionFromResourceID(resourceID));
                player.sendMessage(lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.SuccessfullySetResourceInChat", player, true));
                new PluginSettingsMenu(CoolStuffLib.getLib().getPlayerMenuUtility(player.getUniqueId())).open();
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

        registerButton(getSlot(path + "LoadFromResourceID", 3), lgm.getItem(path + "LoadFromResourceID", player, false), (p, e) -> {
            int resourceID = plugin.getPluginDescriptionManager().getResourceID(playerMenuUtility.getData("CurrentSelectedPlugin", Plugin.class).getName());
            if (resourceID != -1) {
                String description = plugin.getPluginDescriptionManager().getDescriptionFromResourceID(resourceID);
                plugin.getPluginDescriptionManager().addPluginDescription(playerMenuUtility.getData("CurrentSelectedPlugin", Plugin.class), description);
                p.sendMessage(lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.LoadedFromResourceID", p, true));
                new PluginSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open();
            } else {
                p.sendMessage(lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.SetResourceInChat", p, true));
                p.closeInventory();
                playerMenuUtility.addData("SetResourceIDInChat", true);
            }
        }, null);
        registerButton(getSlot(path + "SetWithChat", 5), lgm.getItem(path + "SetWithChat", player, false), (p, e) -> {
            p.sendMessage(lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.SetDescriptionInChat", p, true));
            p.closeInventory();
            playerMenuUtility.addData("SetDescriptionInChat", true);
        }, null);
        registerButton(getSlot("General.Close", 8), lgm.getItem("General.Close", player, false),
                new PermissionAction("AdminPanel.Button.Close", (p, e) ->
                        new PluginSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open(), lgm), null);
    }
}
