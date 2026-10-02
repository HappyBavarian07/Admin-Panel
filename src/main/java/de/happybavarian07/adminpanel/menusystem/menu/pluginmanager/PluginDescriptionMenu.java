package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.CoolStuffLib;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.Plugin;

public class PluginDescriptionMenu extends Menu {
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

    private void promptResourceId(Player player, String message) {
        MenuAction.prompt(message, (p, text) -> {
            try {
                int resourceID = Integer.parseInt(text);
                plugin.getPluginDescriptionManager().addPluginDescription(playerMenuUtility.getData("CurrentSelectedPlugin", Plugin.class), plugin.getPluginDescriptionManager().getDescriptionFromResourceID(resourceID));
                p.sendMessage(lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.SuccessfullySetResourceInChat", p, true));
                new PluginSettingsMenu(CoolStuffLib.getLib().getPlayerMenuUtility(p.getUniqueId())).open();
            } catch (NumberFormatException ex) {
                p.sendMessage(lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.SetResourceInChatError", p, true));
                promptResourceId(p, null);
            }
        }).execute(player, null);
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
                promptResourceId(p, lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.SetResourceInChat", p, true));
            }
        }, null);
        registerButton(getSlot(path + "SetWithChat", 5), lgm.getItem(path + "SetWithChat", player, false), MenuAction.prompt(lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.SetDescriptionInChat", player, true), (p, text) -> {
            plugin.getPluginDescriptionManager().addPluginDescription(playerMenuUtility.getData("CurrentSelectedPlugin", Plugin.class), text);
            p.sendMessage(lgm.getMessage("Player.PluginManager.PluginDescriptionMenu.SuccessfullySetDescriptionInChat", p, true));
            new PluginSettingsMenu(CoolStuffLib.getLib().getPlayerMenuUtility(p.getUniqueId())).open();
        }), null);
        registerButton(getSlot("General.Close", 8), lgm.getItem("General.Back", player, false),
                MenuAction.of((p, e) ->
                        new PluginSettingsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open()).requires("AdminPanel.Button.Close"), null);
    }
}
