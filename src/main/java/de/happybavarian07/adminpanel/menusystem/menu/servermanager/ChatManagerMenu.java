package de.happybavarian07.adminpanel.menusystem.menu.servermanager;

/**
 * @Author HappyBavarian07
 * @Date 02.09.2021
 */

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.server.ClearChatEvent;
import de.happybavarian07.adminpanel.events.server.MuteChatEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

public class ChatManagerMenu extends Menu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    public ChatManagerMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.ServerManagment.ChatManager.Open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("ServerManager.ChatManagerMenu", null);
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String path = "ChatManager.";

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);

        if (item == null || !item.hasItemMeta()) return;
        if (item.equals(lgm.getItem(path + "ClearChat", player, false))) {
            if (!player.hasPermission("AdminPanel.ServerManagment.ChatManager.Clear")) {
                player.sendMessage(noPerms);
                return;
            }
            ClearChatEvent clearChatEvent = new ClearChatEvent(player, 100);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(clearChatEvent);
                if (!clearChatEvent.isCancelled()) {
                    Utils.clearChat(clearChatEvent.getLines(), false, player);
                    Bukkit.broadcastMessage(lgm.getMessage("Player.Chat.Header", player, true));
                    Bukkit.broadcastMessage(lgm.getMessage("Player.Chat.Message", player, true));
                    Bukkit.broadcastMessage(lgm.getMessage("Player.Chat.Footer", player, true));
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "UnMuteChat", player, false))) {
            if (!player.hasPermission("AdminPanel.ServerManagment.ChatManager.Mute")) {
                player.sendMessage(noPerms);
                return;
            }
            MuteChatEvent muteChatEvent = new MuteChatEvent(player, false);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(muteChatEvent);
                if (!muteChatEvent.isCancelled()) {
                    plugin.setChatMuted(false);
                    super.open();
                    Bukkit.broadcastMessage(lgm.getMessage("Player.ChatUnMute.Header", player, true));
                    Bukkit.broadcastMessage(lgm.getMessage("Player.ChatUnMute.Message", player, true));
                    Bukkit.broadcastMessage(lgm.getMessage("Player.ChatUnMute.Footer", player, true));
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem(path + "MuteChat", player, false))) {
            if (!player.hasPermission("AdminPanel.ServerManagment.ChatManager.Mute")) {
                player.sendMessage(noPerms);
                return;
            }
            MuteChatEvent muteChatEvent = new MuteChatEvent(player, true);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(muteChatEvent);
                if (!muteChatEvent.isCancelled()) {
                    plugin.setChatMuted(true);
                    super.open();
                    Bukkit.broadcastMessage(lgm.getMessage("Player.ChatMute.Header", player, true));
                    Bukkit.broadcastMessage(lgm.getMessage("Player.ChatMute.Message", player, true));
                    Bukkit.broadcastMessage(lgm.getMessage("Player.ChatMute.Footer", player, true));
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("General.Close", player, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new ServerManagerMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "ChatManager.";

        inventory.setItem(getSlot(path + "ClearChat", 12), lgm.getItem(path + "ClearChat", player, false));
        if (plugin.isChatMuted()) {
            inventory.setItem(getSlot(path + "UnMuteChat", 14), lgm.getItem(path + "UnMuteChat", player, false));
        } else {
            inventory.setItem(getSlot(path + "MuteChat", 14), lgm.getItem(path + "MuteChat", player, false));
        }
        inventory.setItem(getSlot("General.Close", 22), lgm.getItem("General.Close", player, false));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.isChatMuted()) {
            event.setCancelled(true);
            player.sendMessage(lgm.getMessage("Player.ChatMute.PlayerMessage", player, true));
        }
    }
}
