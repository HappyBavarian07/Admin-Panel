package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

import de.happybavarian07.adminpanel.journal.AdminJournal;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.plugins.PluginInstallEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PluginInstallMenu extends Menu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    private int resourceID = 0;
    private static final String DEFAULT_FILE_NAME = "InstalledPlugin";
    private String fileName = DEFAULT_FILE_NAME;
    private boolean enableAfterInstall = false;

    public PluginInstallMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PluginManager.InstallPlugins");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PluginManager.InstallMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PluginInstallMenu";
    }

    @Override
    public int getSlots() {
        return 9;
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
        String path = "PluginManager.InstallMenu.";

        ItemStack resourceIDItem = lgm.getItem(path + "ResourceID", player, false);
        ItemMeta resourceIDMeta = resourceIDItem.getItemMeta();
        List<String> updatedResourceIDLore = new ArrayList<>();
        assert resourceIDMeta != null;
        for (String s : Objects.requireNonNull(resourceIDMeta.getLore())) {
            updatedResourceIDLore.add(s.replace("%resourceid%", String.valueOf(this.resourceID)));
        }
        resourceIDMeta.setLore(updatedResourceIDLore);
        resourceIDItem.setItemMeta(resourceIDMeta);
        registerButton(getSlot(path + "ResourceID", 0), resourceIDItem,
                MenuAction.prompt(lgm.getMessage("Player.PluginManager.TypeResourceIDInChat", player, true), (p, input) -> {
                    try {
                        this.resourceID = Integer.parseInt(input);
                    } catch (NumberFormatException ex) {
                        AdminPanelMain.logError("The entered Spiget resource ID is not a number", ex);
                        p.sendMessage(lgm.getMessage("Player.PlayerManager.Money.NotANumber", p, true));
                    }
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%resourceid%", input, true);
                    p.sendMessage(lgm.getMessage("Player.PluginManager.ResourceIDSelected", p, true));
                    open();
                }));

        ItemStack nameItem = lgm.getItem(path + "Name", player, false);
        ItemMeta nameMeta = nameItem.getItemMeta();
        List<String> updatedNameLore = new ArrayList<>();
        assert nameMeta != null;
        for (String s : Objects.requireNonNull(nameMeta.getLore())) {
            updatedNameLore.add(s.replace("%filename%", this.fileName));
        }
        nameMeta.setLore(updatedNameLore);
        nameItem.setItemMeta(nameMeta);
        registerButton(getSlot(path + "Name", 1), nameItem,
                MenuAction.prompt(lgm.getMessage("Player.PluginManager.TypeFileNameInChat", player, true), (p, input) -> {
                    this.fileName = input.replace(" ", "-");
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%filename%", this.fileName, true);
                    p.sendMessage(lgm.getMessage("Player.PluginManager.FileNameSelected", p, true));
                    open();
                }));

        registerButton(getSlot(path + "EnableAfterInstall." + enableAfterInstall, 2), lgm.getItem(path + "EnableAfterInstall." + enableAfterInstall, player, false),
                MenuAction.of((p, e) -> enableAfterInstall = !enableAfterInstall).then((p, e) -> refresh()));
        registerButton(getSlot(path + "InstallButton", 7), lgm.getItem(path + "InstallButton", player, false), MenuAction.of((p, e) -> {
            PluginInstallEvent installEvent = new PluginInstallEvent(p, resourceID, fileName, enableAfterInstall);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(installEvent);
                if (!installEvent.isCancelled()) {
                    try {
                        Plugin installedPlugin = AdminPanelMain.getAPI().downloadPluginFromSpiget(installEvent.getResourceID(), installEvent.getFileName(), installEvent.isEnableAfterInstall());
                        plugin.getPluginDescriptionManager().addPluginDescription(installedPlugin, "", installEvent.getResourceID());
                        AdminJournal.log(p, "PLUGIN_INSTALL", installedPlugin.getName(), "Spiget resource " + installEvent.getResourceID());
                        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%resourceid%", resourceID, true);
                        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%filename%", fileName, false);
                        p.sendMessage(lgm.getMessage("Player.PluginManager.SuccessfullyInstalled", p, true));
                    } catch (FileNotFoundException fileNotFoundException) {
                        AdminPanelMain.logError("The plugin file to install was not found", fileNotFoundException);
                        p.sendMessage(lgm.getMessage("Player.PluginManager.FileNotFound", p, true));
                    } catch (IOException | InvalidPluginException | InvalidDescriptionException exception) {
                        AdminPanelMain.logError("Could not install the plugin from Spiget", exception);
                        p.sendMessage(lgm.getMessage("Player.PluginManager.ErrorInInstallProccess", p, true));
                    } catch (UnknownDependencyException unknownDependencyException) {
                        AdminPanelMain.logError("The plugin to install has unknown dependencies", unknownDependencyException);
                        p.sendMessage(lgm.getMessage("Player.PluginManager.MissingDependencies", p, true));
                    }
                }
            } catch (NotAPanelEventException ex) {
                AdminPanelMain.logError("The plugin install action was not a valid panel event", ex);
            }
            this.resourceID = 0;
            this.fileName = DEFAULT_FILE_NAME;
            enableAfterInstall = false;
        }).thenClose());
        registerButton(getSlot("General.Close", 8), lgm.getItem("General.Back", player, false),
                MenuAction.open(() -> new PluginSelectMenu(playerMenuUtility, new AdminPanelStartMenu(playerMenuUtility)))
                        .requires("AdminPanel.Button.Close"));
    }
}
