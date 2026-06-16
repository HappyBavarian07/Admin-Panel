package de.happybavarian07.adminpanel.menusystem.menu.pluginmanager;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.plugins.PluginInstallEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
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

public class PluginInstallMenu extends Menu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    private int resourceID = 0;
    private String fileName = "InstalledPlugin";
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
        registerButton(0, resourceIDItem, (p, e) -> {
            playerMenuUtility.addData("typeResourceIDInChat", true);
            p.sendMessage(lgm.getMessage("Player.PluginManager.TypeResourceIDInChat", p, true));
            p.closeInventory();
        }, null);

        ItemStack nameItem = lgm.getItem(path + "Name", player, false);
        ItemMeta nameMeta = nameItem.getItemMeta();
        List<String> updatedNameLore = new ArrayList<>();
        assert nameMeta != null;
        for (String s : Objects.requireNonNull(nameMeta.getLore())) {
            updatedNameLore.add(s.replace("%filename%", this.fileName));
        }
        nameMeta.setLore(updatedNameLore);
        nameItem.setItemMeta(nameMeta);
        registerButton(1, nameItem, (p, e) -> {
            playerMenuUtility.addData("typeFileNameInChat", true);
            p.sendMessage(lgm.getMessage("Player.PluginManager.TypeFileNameInChat", p, true));
            p.closeInventory();
        }, null);

        if (enableAfterInstall) {
            registerButton(2, lgm.getItem(path + "EnableAfterInstall.true", player, false), (p, e) -> {
                enableAfterInstall = false;
                super.open();
            }, null);
        } else {
            registerButton(2, lgm.getItem(path + "EnableAfterInstall.false", player, false), (p, e) -> {
                enableAfterInstall = true;
                super.open();
            }, null);
        }
        registerButton(7, lgm.getItem(path + "InstallButton", player, false), (p, e) -> {
            PluginInstallEvent installEvent = new PluginInstallEvent(p, resourceID, fileName, enableAfterInstall);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(installEvent);
                if (!installEvent.isCancelled()) {
                    try {
                        Plugin installedPlugin = AdminPanelMain.getAPI().downloadPluginFromSpiget(installEvent.getResourceID(), installEvent.getFileName(), installEvent.isEnableAfterInstall());
                        plugin.getPluginDescriptionManager().addPluginDescription(installedPlugin, "", installEvent.getResourceID());
                    } catch (FileNotFoundException fileNotFoundException) {
                        fileNotFoundException.printStackTrace();
                        p.sendMessage(lgm.getMessage("Player.PluginManager.FileNotFound", p, true));
                    } catch (IOException | InvalidPluginException | InvalidDescriptionException exception) {
                        exception.printStackTrace();
                        p.sendMessage(lgm.getMessage("Player.PluginManager.ErrorInInstallProccess", p, true));
                    } catch (UnknownDependencyException unknownDependencyException) {
                        unknownDependencyException.printStackTrace();
                        p.sendMessage(lgm.getMessage("Player.PluginManager.MissingDependencies", p, true));
                    }
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%resourceid%", resourceID, true);
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%filename%", fileName, false);
                    p.sendMessage(lgm.getMessage("Player.PluginManager.SuccessfullyInstalled", p, true));
                }
            } catch (NotAPanelEventException ex) {
                ex.printStackTrace();
            }
            p.closeInventory();
            this.resourceID = 0;
            this.fileName = null;
            enableAfterInstall = false;
        }, null);
        registerButton(8, lgm.getItem("General.Close", player, false),
                new PermissionAction("AdminPanel.Button.Close", (p, e) ->
                        new PluginSelectMenu(playerMenuUtility, new AdminPanelStartMenu(playerMenuUtility)).open(), lgm), null);
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playerMenuUtility.getOwner() != player) return;

        if (playerMenuUtility.hasData("typeFileNameInChat")) {
            event.setCancelled(true);
            String message = event.getMessage().replace(" ", "-");
            this.fileName = message;
            playerMenuUtility.removeData("typeFileNameInChat");
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%filename%", message, true);
            player.sendMessage(lgm.getMessage("Player.PluginManager.FileNameSelected", player, true));
            super.open();
        }
        if (playerMenuUtility.hasData("typeResourceIDInChat")) {
            event.setCancelled(true);
            String message = event.getMessage();
            try {
                this.resourceID = Integer.parseInt(message);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.NotANumber", player, true));
            }
            playerMenuUtility.removeData("typeResourceIDInChat");
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%resourceid%", message, true);
            player.sendMessage(lgm.getMessage("Player.PluginManager.ResourceIDSelected", player, true));
            super.open();
        }
    }
}
