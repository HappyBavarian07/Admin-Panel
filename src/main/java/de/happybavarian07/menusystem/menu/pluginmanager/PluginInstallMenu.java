package de.happybavarian07.menusystem.menu.pluginmanager;

import de.happybavarian07.events.NotAPanelEventException;
import de.happybavarian07.events.plugins.PluginInstallEvent;
import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.UnknownDependencyException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PluginInstallMenu extends Menu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();

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
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String path = "PluginManager.InstallMenu.";
        ItemStack resourceIDItem = lgm.getItem(path + "ResourceID", player);
        ItemMeta resourceIDMeta = resourceIDItem.getItemMeta();
        List<String> updatedResourceIDLore = new ArrayList<>();
        for (String s : resourceIDMeta.getLore()) {
            updatedResourceIDLore.add(s.replace("%resourceid%", String.valueOf(this.resourceID)));
        }
        resourceIDMeta.setLore(updatedResourceIDLore);
        resourceIDItem.setItemMeta(resourceIDMeta);

        ItemStack nameItem = lgm.getItem(path + "Name", player);
        ItemMeta nameMeta = nameItem.getItemMeta();
        List<String> updatedNameLore = new ArrayList<>();
        for (String s : nameMeta.getLore()) {
            updatedNameLore.add(s.replace("%filename%", this.fileName));
        }
        nameMeta.setLore(updatedNameLore);
        nameItem.setItemMeta(nameMeta);
        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta()) return;
        if (item.equals(resourceIDItem)) {
            player.setMetadata("typeResourceIDInChat", new FixedMetadataValue(plugin, true));
            player.sendMessage(lgm.getMessage("Player.PluginManager.TypeResourceIDInChat", player));
            player.closeInventory();
        } else if (item.equals(nameItem)) {
            player.setMetadata("typeFileNameInChat", new FixedMetadataValue(plugin, true));
            player.sendMessage(lgm.getMessage("Player.PluginManager.TypeFileNameInChat", player));
            player.closeInventory();
        } else if (item.equals(lgm.getItem(path + "EnableAfterInstall.true", player))) {
            enableAfterInstall = false;
            super.open();
        } else if (item.equals(lgm.getItem(path + "EnableAfterInstall.false", player))) {
            enableAfterInstall = true;
            super.open();
        } else if (item.equals(lgm.getItem(path + "InstallButton", player))) {
            PluginInstallEvent installEvent = new PluginInstallEvent(player, resourceID, fileName, enableAfterInstall);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(installEvent);
                if (!installEvent.isCancelled()) {
                    try {
                        AdminPanelMain.getAPI().downloadPluginFromSpiget(installEvent.getResourceID(), installEvent.getFileName(), installEvent.isEnableAfterInstall());
                    } catch (FileNotFoundException fileNotFoundException) {
                        fileNotFoundException.printStackTrace();
                        player.sendMessage(lgm.getMessage("Player.PluginManager.FileNotFound", player));
                    } catch (IOException | InvalidPluginException | InvalidDescriptionException exception) {
                        exception.printStackTrace();
                        player.sendMessage(lgm.getMessage("Player.PluginManager.ErrorInInstallProccess", player));
                    } catch (UnknownDependencyException unknownDependencyException) {
                        unknownDependencyException.printStackTrace();
                        player.sendMessage(lgm.getMessage("Player.PluginManager.MissingDependencies", player));
                    }
                    player.sendMessage(lgm.getMessage("Player.PluginManager.SuccessfullyInstalled", player)
                            .replace("%resourceid%", String.valueOf(this.resourceID))
                            .replace("%filename%", this.fileName));
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
            player.closeInventory();
            this.resourceID = 0;
            this.fileName = null;
            enableAfterInstall = false;
        } else if (item.equals(lgm.getItem("General.Close", player))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new PluginSelectMenu(playerMenuUtility).open();
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "PluginManager.InstallMenu.";

        ItemStack resourceIDItem = lgm.getItem(path + "ResourceID", player);
        ItemMeta resourceIDMeta = resourceIDItem.getItemMeta();
        List<String> updatedResourceIDLore = new ArrayList<>();
        for (String s : resourceIDMeta.getLore()) {
            updatedResourceIDLore.add(s.replace("%resourceid%", String.valueOf(this.resourceID)));
        }
        resourceIDMeta.setLore(updatedResourceIDLore);
        resourceIDItem.setItemMeta(resourceIDMeta);
        inventory.setItem(0, resourceIDItem);

        ItemStack nameItem = lgm.getItem(path + "Name", player);
        ItemMeta nameMeta = nameItem.getItemMeta();
        List<String> updatedNameLore = new ArrayList<>();
        for (String s : nameMeta.getLore()) {
            updatedNameLore.add(s.replace("%filename%", this.fileName));
        }
        nameMeta.setLore(updatedNameLore);
        nameItem.setItemMeta(nameMeta);
        inventory.setItem(1, nameItem);

        if (enableAfterInstall) {
            inventory.setItem(2, lgm.getItem(path + "EnableAfterInstall.true", player));
        } else {
            inventory.setItem(2, lgm.getItem(path + "EnableAfterInstall.false", player));
        }
        inventory.setItem(7, lgm.getItem(path + "InstallButton", player));
        inventory.setItem(8, lgm.getItem("General.Close", player));
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("typeFileNameInChat")) {
            event.setCancelled(true);
            String message = event.getMessage().replace(" ", "-");
            this.fileName = message;
            player.removeMetadata("typeFileNameInChat", plugin);
            player.sendMessage(lgm.getMessage("Player.PluginManager.FileNameSelected", player).replace("%filename%", message));
            super.open();
        }
        if (player.hasMetadata("typeResourceIDInChat")) {
            event.setCancelled(true);
            String message = event.getMessage();
            try {
                this.resourceID = Integer.parseInt(message);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                player.sendMessage(lgm.getMessage("Player.PlayerManager.Money.NotANumber", player));
            }
            player.removeMetadata("typeResourceIDInChat", plugin);
            player.sendMessage(lgm.getMessage("Player.PluginManager.ResourceIDSelected", player).replace("%resourceid%", message));
            super.open();
        }
    }
}
