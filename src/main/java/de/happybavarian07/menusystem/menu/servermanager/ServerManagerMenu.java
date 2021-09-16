package de.happybavarian07.menusystem.menu.servermanager;

import de.happybavarian07.events.NotAPanelEventException;
import de.happybavarian07.events.server.KickAllPlayersEvent;
import de.happybavarian07.events.server.MaintenanceModeToggleEvent;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import de.happybavarian07.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.utils.Utils;
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
    private final Main plugin = Main.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();

    public ServerManagerMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.ServerManagment.Open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("ServerManager.Menu", null);
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

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta()) return;
        if (item.equals(lgm.getItem(path + "Broadcast", player))) {
            if (!player.hasPermission("AdminPanel.ServerManagment.Broadcast")) {
                player.sendMessage(noPerms);
                return;
            }
            player.closeInventory();
            player.setMetadata("serverManagerBroadcastMessage", new FixedMetadataValue(plugin, true));
            player.sendMessage(lgm.getMessage("Player.ServerManager.PleaseEnterAMessage", player));
        } else if (item.equals(lgm.getItem(path + "ChatManagerItem", player))) {
            if (!player.hasPermission("AdminPanel.ServerManagment.ChatManager.Open")) {
                player.sendMessage(noPerms);
                return;
            }
            new ChatManagerMenu(Main.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem(path + "WhiteListMenuItem", player))) {
            if (!player.hasPermission("AdminPanel.ServerManagment.Whitelist")) {
                player.sendMessage(noPerms);
                return;
            }
            new WhitelistManagerMenu(Main.getAPI().getPlayerMenuUtility(player)).open();
        } else if (item.equals(lgm.getItem(path + "KickAllPlayers", player))) {
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
                Main.getAPI().callAdminPanelEvent(kickAllPlayersEvent);
                if (!kickAllPlayersEvent.isCancelled()) {
                    for(Player online : playersToKick) {
                        Utils.getInstance().kick(player, online.getName(),
                                lgm.getMessage("Player.ServerManager.KickAllPlayersReason", online),
                                lgm.getMessage("Player.ServerManager.KickAllPlayersSource", player));
                    }
                    player.sendMessage(lgm.getMessage("Player.ServerManager.AllPlayersKicked", player));
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "MaintenanceMode.true", player))) {
            if (!player.hasPermission("AdminPanel.ServerManagment.MaintenanceMode")) {
                player.sendMessage(noPerms);
                return;
            }
            MaintenanceModeToggleEvent kickAllPlayersEvent = new MaintenanceModeToggleEvent(player, false);
            try {
                Main.getAPI().callAdminPanelEvent(kickAllPlayersEvent);
                if (!kickAllPlayersEvent.isCancelled()) {
                    plugin.setInMaintenanceMode(false);
                    Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.MaintenanceModeOn", player));
                    super.open();
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
            super.open();
        } else if (item.equals(lgm.getItem(path + "MaintenanceMode.false", player))) {
            if (!player.hasPermission("AdminPanel.ServerManagment.MaintenanceMode")) {
                player.sendMessage(noPerms);
                return;
            }
            MaintenanceModeToggleEvent kickAllPlayersEvent = new MaintenanceModeToggleEvent(player, true);
            try {
                Main.getAPI().callAdminPanelEvent(kickAllPlayersEvent);
                if (!kickAllPlayersEvent.isCancelled()) {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (!online.hasPermission("AdminPanel.Bypass.KickInMainTenanceMode")) {
                            online.kickPlayer(lgm.getMessage("Player.ServerManager.MaintenanceMode", online));
                        }
                    }
                    plugin.setInMaintenanceMode(true);
                    Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.MaintenanceModeOff", player));
                    super.open();
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("General.Close", player))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new AdminPanelStartMenu(Main.getAPI().getPlayerMenuUtility(player)).open();
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "ServerManager.";

        // Items
        inventory.setItem(2, lgm.getItem(path + "Broadcast", player));
        inventory.setItem(4, lgm.getItem(path + "ChatManagerItem", player));
        inventory.setItem(6, lgm.getItem(path + "WhiteListMenuItem", player));
        inventory.setItem(12, lgm.getItem(path + "KickAllPlayers", player));
        if (plugin.isInMaintenanceMode()) {
            inventory.setItem(14, lgm.getItem(path + "MaintenanceMode.true", player));
        } else {
            inventory.setItem(14, lgm.getItem(path + "MaintenanceMode.false", player));
        }
        inventory.setItem(22, lgm.getItem("General.Close", player));
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("serverManagerBroadcastMessage")) {
            String message = Utils.getInstance().chat(event.getMessage());
            Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.BroadcastHeader", player));
            Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.BroadcastMessage", player).replace("%message%", message));
            Bukkit.broadcastMessage(lgm.getMessage("Player.ServerManager.BroadcastFooter", player));
            player.removeMetadata("serverManagerBroadcastMessage", plugin);
            super.open();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (plugin.isInMaintenanceMode() && !player.hasPermission("AdminPanel.Bypass.KickInMainTenanceMode")) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, lgm.getMessage("Player.ServerManager.MaintenanceMode", player));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerPing(ServerListPingEvent event) {
        if (plugin.isInMaintenanceMode()) {
            event.setMotd(lgm.getMessage("Player.ServerManager.MaintenanceModeMOTD", null));
        } else {
            event.setMotd(Bukkit.getMotd());
        }
    }
}
