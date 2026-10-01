package de.happybavarian07.adminpanel.menusystem.menu.servermanager;
/*
 * @Author HappyBavarian07
 * @Date 02.09.2021
 */

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.server.ClearChatEvent;
import de.happybavarian07.adminpanel.events.server.MuteChatEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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
    public String getConfigMenuAddonFeatureName() {
        return "ChatManagerMenu";
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
        String path = "ChatManager.";

        registerButton(getSlot(path + "ClearChat", 12), lgm.getItem(path + "ClearChat", player, false),
                MenuAction.of((p, e) -> {
                    ClearChatEvent clearChatEvent = new ClearChatEvent(p, 100);
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(clearChatEvent);
                        if (!clearChatEvent.isCancelled()) {
                            AdminPanelUtils.clearChat(clearChatEvent.getLines(), false, p);
                            Bukkit.broadcastMessage(lgm.getMessage("Player.Chat.Header", p, true));
                            Bukkit.broadcastMessage(lgm.getMessage("Player.Chat.Message", p, true));
                            Bukkit.broadcastMessage(lgm.getMessage("Player.Chat.Footer", p, true));
                        }
                    } catch (NotAPanelEventException ex) {
                        AdminPanelMain.logError("The clear chat action was not a valid panel event", ex);
                    }
                }).requires("AdminPanel.ServerManagment.ChatManager.Clear"), null);
        if (plugin.getPluginStateManager().isGlobalChatMuted()) {
            registerButton(getSlot(path + "UnMuteChat", 14), lgm.getItem(path + "UnMuteChat", player, false),
                    MenuAction.of((p, e) -> {
                        MuteChatEvent event = new MuteChatEvent(p, false);
                        try {
                            AdminPanelMain.getAPI().callAdminPanelEvent(event);
                            if (!event.isCancelled()) {
                                plugin.getPluginStateManager().setGlobalChatMuted(false);
                                super.open();
                                Bukkit.broadcastMessage(lgm.getMessage("Player.ChatUnMute.Header", p, true));
                                Bukkit.broadcastMessage(lgm.getMessage("Player.ChatUnMute.Message", p, true));
                                Bukkit.broadcastMessage(lgm.getMessage("Player.ChatUnMute.Footer", p, true));
                            }
                        } catch (NotAPanelEventException ex) {
                            AdminPanelMain.logError("The chat unmute action was not a valid panel event", ex);
                        }
                    }).requires("AdminPanel.ServerManagment.ChatManager.Mute"), null);
        } else {
            registerButton(getSlot(path + "MuteChat", 14), lgm.getItem(path + "MuteChat", player, false),
                    MenuAction.of((p, e) -> {
                        MuteChatEvent event = new MuteChatEvent(p, true);
                        try {
                            AdminPanelMain.getAPI().callAdminPanelEvent(event);
                            if (!event.isCancelled()) {
                                plugin.getPluginStateManager().setGlobalChatMuted(true);
                                super.open();
                                Bukkit.broadcastMessage(lgm.getMessage("Player.ChatMute.Header", p, true));
                                Bukkit.broadcastMessage(lgm.getMessage("Player.ChatMute.Message", p, true));
                                Bukkit.broadcastMessage(lgm.getMessage("Player.ChatMute.Footer", p, true));
                            }
                        } catch (NotAPanelEventException ex) {
                            AdminPanelMain.logError("The chat mute action was not a valid panel event", ex);
                        }
                    }).requires("AdminPanel.ServerManagment.ChatManager.Mute"), null);
        }
        registerButton(getSlot("General.Close", 22), lgm.getItem("General.Close", player, false),
                MenuAction.of((p, e) ->
                        new ServerManagerMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open()).requires("AdminPanel.Button.Close"), null);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.getPluginStateManager().isGlobalChatMuted() && !player.hasPermission("AdminPanel.Bypass.ChatMute")) {
            event.setCancelled(true);
            player.sendMessage(lgm.getMessage("Player.ChatMute.PlayerMessage", player, true));
        }
    }
}
