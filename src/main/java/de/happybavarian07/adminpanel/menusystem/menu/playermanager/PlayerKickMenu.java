package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerKickMenu extends Menu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private String reason = "";

    public PlayerKickMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Kick");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.KickMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PlayerKickMenu";
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

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playerMenuUtility.hasData("KickPlayerSetNewReason")) {
            this.reason = format(player, event.getMessage());
            playerMenuUtility.removeData("KickPlayerSetNewReason");
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%reason%", reason, true);
            player.sendMessage(lgm.getMessage("Player.PlayerManager.KickMenu.Reason.NewReasonSet", player, true));
            super.open();
            event.setCancelled(true);
        }
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        String path = "PlayerManager.ActionsMenu.KickMenu.";
        lgm.addPlaceholder(PlaceholderType.ITEM, "%reason%", reason, false);

        // Reason
        registerButton(getSlot(path + "Reason", 0), 
            lgm.getItem(path + "Reason", player, false), 
            (p, e) -> {
                playerMenuUtility.addData("KickPlayerSetNewReason", true);
                p.sendMessage(lgm.getMessage("Player.PlayerManager.KickMenu.Reason.EnterNewReason", p, true));
                p.closeInventory();
            }, null);

        // Kick Execution
        registerButton(getSlot(path + "Kick", 7), 
            lgm.getItem(path + "Kick", player, false), 
            MenuAction.of((p, e) -> {
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%reason%", reason, true);
                OfflinePlayer target = Bukkit.getOfflinePlayer(playerMenuUtility.getTargetUUID());
                if (plugin.getConfig().getStringList("Pman.Actions.ExemptPlayers").contains(target.getName())) {
                    p.sendMessage(lgm.getMessage("Player.PlayerManager.KickMenu.NotKickable", p, true));
                    return;
                }
                if (target.isOnline()) {
                    target.getPlayer().kickPlayer(lgm.getMessage("Player.PlayerManager.KickMenu.TargetKickMessage", target.getPlayer(), false));
                }
                p.sendMessage(lgm.getMessage("Player.PlayerManager.KickMenu.SuccessfullyKicked", p, true));
                new PlayerSelectMenu(playerMenuUtility, new AdminPanelStartMenu(playerMenuUtility)).open();
            }).requires("AdminPanel.PlayerManager.PlayerSettings.Kick"), null);

        // General Close
        registerButton(getSlot("General.Close", 8), 
            lgm.getItem("General.Close", player, false), 
            MenuAction.of((p, e) -> {
                new PlayerActionSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open();
            }).requires("AdminPanel.Button.Close"), null);
    }

    public String format(Player player, String message) {
        return AdminPanelUtils.format(player, message, AdminPanelMain.getPrefix());
    }
}
