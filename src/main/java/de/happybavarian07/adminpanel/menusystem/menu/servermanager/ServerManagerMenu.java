package de.happybavarian07.adminpanel.menusystem.menu.servermanager;

import de.happybavarian07.adminpanel.main.PlaceholderType;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.server.KickAllPlayersEvent;
import de.happybavarian07.adminpanel.events.server.MaintenanceModeToggleEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

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
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String path = "ServerManager.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item == null || !item.hasItemMeta()) return;
        if (item.equals(lgm.getItem(path + "Broadcast", player, false))) {
            if (!player.hasPermission("AdminPanel.ServerManagment.Broadcast")) {
                player.sendMessage(noPerms);
                return;
            }
            player.closeInventory();
            player.setMetadata("serverManagerBroadcastMessage", new FixedMetadataValue(plugin, true));
            player.sendMessage(lgm.getMessage("Player.ServerManager.PleaseEnterAMessage", player, true));
        } else if (item.equals(lgm.getItem(path + "ChatManagerItem", player, false))) {
            if (!player.hasPermission("AdminPanel.ServerManagment.ChatManager.Open")) {
                player.sendMessage(noPerms);
                return;
            }
            new ChatManagerMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem(path + "WhiteListMenuItem", player, false))) {
            if (!player.hasPermission("AdminPanel.ServerManagment.Whitelist")) {
                player.sendMessage(noPerms);
                return;
            }
            new WhitelistManagerMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem(path + "KickAllPlayers", player, false))) {
            if (!player.hasPermission("AdminPanel.ServerManagment.KickAllPlayers")) {
                player.sendMessage(noPerms);
                return;
            }
            List<Player> playersToKick = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("AdminPanel.Bypass.KickAll")) {
                    playersToKick.add(online);
                }
            }
            playersToKick.remove(player);
            KickAllPlayersEvent kickAllPlayersEvent = new KickAllPlayersEvent(player, playersToKick);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(kickAllPlayersEvent);
                if (!kickAllPlayersEvent.isCancelled()) {
                    for (Player online : playersToKick) {
                        Utils.kick(player, online.getName(),
                                lgm.getMessage("Player.ServerManager.KickAllPlayersReason", online, false),
                                lgm.getMessage("Player.ServerManager.KickAllPlayersSource", player, false));
                    }
                    player.sendMessage(lgm.getMessage("Player.ServerManager.AllPlayersKicked", player, true));
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "MaintenanceMode.true", player, false))) {
            if (!player.hasPermission("AdminPanel.ServerManagment.MaintenanceMode")) {
                player.sendMessage(noPerms);
                return;
            }
            MaintenanceModeToggleEvent kickAllPlayersEvent = new MaintenanceModeToggleEvent(player);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(kickAllPlayersEvent);
                if (!kickAllPlayersEvent.isCancelled()) {
                    plugin.setInMaintenanceMode(false);
                    if(!plugin.isInMaintenanceMode()) {
                        Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.MaintenanceModeOff", player, true));
                        super.open();
                    }
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
            super.open();
        } else if (item.equals(lgm.getItem(path + "MaintenanceMode.false", player, false))) {
            if (!player.hasPermission("AdminPanel.ServerManagment.MaintenanceMode")) {
                player.sendMessage(noPerms);
                return;
            }
            MaintenanceModeToggleEvent kickAllPlayersEvent = new MaintenanceModeToggleEvent(player);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(kickAllPlayersEvent);
                if (!kickAllPlayersEvent.isCancelled()) {
                    plugin.setInMaintenanceMode(true);
                    if(plugin.isInMaintenanceMode()) {
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            if (!online.hasPermission("AdminPanel.Bypass.KickInMaintenanceMode")) {
                                online.kickPlayer(lgm.getMessage("Player.ServerManager.MaintenanceMode", online, false));
                            }
                        }
                        Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.MaintenanceModeOn", player, true));
                    }
                    super.open();
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("General.Close", player, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new AdminPanelStartMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "ServerManager.";

        // Items
        inventory.setItem(getSlot("Broadcast", 2), lgm.getItem(path + "Broadcast", player, false));
        inventory.setItem(getSlot("ChatManagerItem", 4), lgm.getItem(path + "ChatManagerItem", player, false));
        inventory.setItem(getSlot(path + "WhiteListMenuItem", 6), lgm.getItem(path + "WhiteListMenuItem", player, false));
        inventory.setItem(getSlot(path + "KickAllPlayers", 12), lgm.getItem(path + "KickAllPlayers", player, false));
        if (plugin.isInMaintenanceMode()) {
            inventory.setItem(getSlot(path + "MaintenanceMode.true", 14), lgm.getItem(path + "MaintenanceMode.true", player, false));
        } else {
            inventory.setItem(getSlot(path + "MaintenanceMode.false", 14), lgm.getItem(path + "MaintenanceMode.false", player, false));
        }
        inventory.setItem(getSlot("General.Close", 22), lgm.getItem("General.Close", player, false));
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("serverManagerBroadcastMessage")) {
            String message = Utils.chat(event.getMessage());
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%message%", message, true);
            Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.BroadcastHeader", player, false));
            Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.BroadcastMessage", player, false));
            Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.BroadcastFooter", player, true));
            player.removeMetadata("serverManagerBroadcastMessage", plugin);
            super.open();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if(player.hasPermission("AdminPanel.Bypass.KickInMainTenanceMode")) return;
        if (plugin.isInMaintenanceMode()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, lgm.getMessage("Player.ServerManager.MaintenanceMode", player, true));
        } else {
            event.allow();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerPing(ServerListPingEvent event) {
        if (plugin.isInMaintenanceMode()) {
            event.setMotd(lgm.getMessage("Player.ServerManager.MaintenanceModeMOTD", null, true));
            event.setMaxPlayers(lgm.getCustomObject("Messages.Player.ServerManager.MaintenanceMaxPlayerCount", null, 0, false));
        } else {
            event.setServerIcon(Bukkit.getServerIcon());
            event.setMotd(Bukkit.getMotd());
            event.setMaxPlayers(Bukkit.getMaxPlayers());
        }
    }
}
