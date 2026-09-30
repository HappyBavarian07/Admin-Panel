package de.happybavarian07.adminpanel.menusystem.menu.servermanager;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.server.KickAllPlayersEvent;
import de.happybavarian07.adminpanel.events.server.MaintenanceModeToggleEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.PermissionAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.ArrayList;
import java.util.List;

public class ServerManagerMenu extends Menu implements Listener {
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
        String path = "ServerManager.";

        // Items
        registerButton(getSlot("Broadcast", 2), lgm.getItem(path + "Broadcast", player, false),
                new PermissionAction("AdminPanel.ServerManagment.Broadcast", (p, e) -> {
                    p.closeInventory();
                    playerMenuUtility.addData("serverManagerBroadcastMessage", true);
                    p.sendMessage(lgm.getMessage("Player.ServerManager.PleaseEnterAMessage", p, true));
                }, lgm), null);
        registerButton(getSlot("ChatManagerItem", 4), lgm.getItem(path + "ChatManagerItem", player, false),
                new PermissionAction("AdminPanel.ServerManagment.ChatManager.Open", (p, e) ->
                        new ChatManagerMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open(), lgm), null);
        registerButton(getSlot(path + "WhiteListMenuItem", 6), lgm.getItem(path + "WhiteListMenuItem", player, false),
                new PermissionAction("AdminPanel.ServerManagment.Whitelist", (p, e) ->
                        new WhitelistManagerMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open(), lgm), null);
        registerButton(getSlot(path + "KickAllPlayers", 12), lgm.getItem(path + "KickAllPlayers", player, false),
                new PermissionAction("AdminPanel.ServerManagment.KickAllPlayers", (p, e) -> {
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
                }, lgm), null);
        if (plugin.getPluginStateManager().isInMaintenanceMode()) {
            registerButton(getSlot(path + "MaintenanceMode.true", 14), lgm.getItem(path + "MaintenanceMode.true", player, false),
                    new PermissionAction("AdminPanel.ServerManagment.MaintenanceMode", (p, e) -> {
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
                    }, lgm), null);
        } else {
            registerButton(getSlot(path + "MaintenanceMode.false", 14), lgm.getItem(path + "MaintenanceMode.false", player, false),
                    new PermissionAction("AdminPanel.ServerManagment.MaintenanceMode", (p, e) -> {
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
                    }, lgm), null);
        }
        registerButton(getSlot("General.Close", 22), lgm.getItem("General.Close", player, false),
                new PermissionAction("AdminPanel.Button.Close", (p, e) ->
                        new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open(), lgm), null);
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playerMenuUtility.getOwner() != player) return;

        if (playerMenuUtility.hasData("serverManagerBroadcastMessage") && player == playerMenuUtility.getOwner()) {
            String message = AdminPanelUtils.chat(event.getMessage());
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%message%", message, true);
            Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.BroadcastHeader", player, false));
            Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.BroadcastMessage", player, false));
            Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.BroadcastFooter", player, true));
            playerMenuUtility.removeData("serverManagerBroadcastMessage");
            super.open();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("AdminPanel.Bypass.KickInMaintenanceMode") ||
                player.hasPermission("AdminPanel.*") ||
                player.hasPermission("AdminPanel.Bypass.*")) return;
        if (plugin.getPluginStateManager().isInMaintenanceMode()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, lgm.getMessage("Player.ServerManager.MaintenanceMode", player, true));
        } else {
            event.allow();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerPing(ServerListPingEvent event) {
        if (plugin.getPluginStateManager().isInMaintenanceMode()) {
            event.setMotd(lgm.getMessage("Player.ServerManager.MaintenanceModeMOTD", null, true));
            event.setMaxPlayers(lgm.getCustomObject("Messages.Player.ServerManager.MaintenanceMaxPlayerCount", null, 0, false));
        } else {
            event.setServerIcon(Bukkit.getServerIcon());
            event.setMotd(Bukkit.getMotd());
            event.setMaxPlayers(Bukkit.getMaxPlayers());
        }
    }
}
