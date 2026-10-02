package de.happybavarian07.adminpanel.menusystem.menu.servermanager;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.server.KickAllPlayersEvent;
import de.happybavarian07.adminpanel.events.server.MaintenanceModeToggleEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.ArrayList;
import java.util.List;

public class ServerManagerMenu extends Menu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    public ServerManagerMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.ServerManagment.Open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("ServerManager.Menu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "ServerManagerMenu";
    }

    @Override
    public int getSlots() {
        return 27;
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
        String path = "ServerManager.";

        // Items
        registerButton(getSlot("Broadcast", 2), lgm.getItem(path + "Broadcast", player, false),
                MenuAction.prompt(lgm.getMessage("Player.ServerManager.PleaseEnterAMessage", player, true), (p, text) -> {
                    lgm.addPlaceholder(PlaceholderType.MESSAGE, "%message%", AdminPanelUtils.chat(text), true);
                    Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.BroadcastHeader", p, false));
                    Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.BroadcastMessage", p, false));
                    Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.BroadcastFooter", p, true));
                    open();
                }).requires("AdminPanel.ServerManagment.Broadcast"), null);
        registerButton(getSlot("ChatManagerItem", 4), lgm.getItem(path + "ChatManagerItem", player, false),
                MenuAction.of((p, e) ->
                        new ChatManagerMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open()).requires("AdminPanel.ServerManagment.ChatManager.Open"), null);
        registerButton(getSlot(path + "WhiteListMenuItem", 6), lgm.getItem(path + "WhiteListMenuItem", player, false),
                MenuAction.of((p, e) ->
                        new WhitelistManagerMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open()).requires("AdminPanel.ServerManagment.Whitelist"), null);
        registerButton(getSlot(path + "KickAllPlayers", 12), lgm.getItem(path + "KickAllPlayers", player, false),
                MenuAction.of((p, e) -> {
                    List<Player> playersToKick = new ArrayList<>();
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (!online.hasPermission("AdminPanel.Bypass.KickAll")) playersToKick.add(online);
                    }
                    playersToKick.remove(p);
                    KickAllPlayersEvent event = new KickAllPlayersEvent(p, playersToKick);
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(event);
                        if (!event.isCancelled()) {
                            for (Player online : playersToKick) {
                                AdminPanelUtils.kick(p, online.getName(),
                                        lgm.getMessage("Player.ServerManager.KickAllPlayersReason", online, false),
                                        lgm.getMessage("Player.ServerManager.KickAllPlayersSource", p, false));
                            }
                            p.sendMessage(lgm.getMessage("Player.ServerManager.AllPlayersKicked", p, true));
                        }
                    } catch (NotAPanelEventException ex) {
                        AdminPanelMain.logError("A server manager action was not a valid panel event", ex);
                    }
                }).requires("AdminPanel.ServerManagment.KickAllPlayers"), null);
        if (plugin.getPluginStateManager().isInMaintenanceMode()) {
            registerButton(getSlot(path + "MaintenanceMode.true", 14), lgm.getItem(path + "MaintenanceMode.true", player, false),
                    MenuAction.of((p, e) -> {
                        MaintenanceModeToggleEvent event = new MaintenanceModeToggleEvent(p);
                        try {
                            AdminPanelMain.getAPI().callAdminPanelEvent(event);
                            if (!event.isCancelled()) {
                                plugin.getPluginStateManager().setInMaintenanceMode(false);
                                if (!plugin.getPluginStateManager().isInMaintenanceMode()) {
                                    Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.MaintenanceModeOff", p, true));
                                }
                            }
                        } catch (NotAPanelEventException ex) {
                            AdminPanelMain.logError("A server manager action was not a valid panel event", ex);
                        }
                        super.open();
                    }).requires("AdminPanel.ServerManagment.MaintenanceMode"), null);
        } else {
            registerButton(getSlot(path + "MaintenanceMode.false", 14), lgm.getItem(path + "MaintenanceMode.false", player, false),
                    MenuAction.of((p, e) -> {
                        MaintenanceModeToggleEvent event = new MaintenanceModeToggleEvent(p);
                        try {
                            AdminPanelMain.getAPI().callAdminPanelEvent(event);
                            if (!event.isCancelled()) {
                                plugin.getPluginStateManager().setInMaintenanceMode(true);
                                if (plugin.getPluginStateManager().isInMaintenanceMode()) {
                                    for (Player online : Bukkit.getOnlinePlayers()) {
                                        if (!online.hasPermission("AdminPanel.Bypass.KickInMaintenanceMode")) {
                                            online.kickPlayer(lgm.getMessage("Player.ServerManager.MaintenanceMode", online, false));
                                        }
                                    }
                                    Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.MaintenanceModeOn", p, true));
                                }
                            }
                        } catch (NotAPanelEventException ex) {
                            AdminPanelMain.logError("A server manager action was not a valid panel event", ex);
                        }
                        super.open();
                    }).requires("AdminPanel.ServerManagment.MaintenanceMode"), null);
        }
        registerButton(getSlot("General.Close", 22), lgm.getItem("General.Back", player, false),
                MenuAction.of((p, e) ->
                        new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open()).requires("AdminPanel.Button.Close"), null);
    }
}
