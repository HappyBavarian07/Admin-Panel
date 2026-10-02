package de.happybavarian07.adminpanel.menusystem.menu.servermanager;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class WhitelistManagerMenu extends Menu {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    public WhitelistManagerMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.ServerManagment.ManageWhitelist");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("ServerManager.WhitelistManager.Menu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "WhitelistManagerMenu";
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
        String path = "ServerManager.WhitelistMenu.";

        registerButton(getSlot(path + "AddPlayer", 11), lgm.getItem(path + "AddPlayer", player, false), (p, e) -> promptName(p, true, lgm.getMessage("Player.ServerManager.WhitelistManager.PleaseEnterName", p, true)), null);
        registerButton(getSlot(path + "AddPlayer", 13), lgm.getItem(path + "RemovePlayer", player, false), (p, e) -> promptName(p, false, lgm.getMessage("Player.ServerManager.WhitelistManager.PleaseEnterName", p, true)), null);
        registerButton(getSlot(path + "AddPlayer", 15), lgm.getItem(path + "ListPlayers", player, false),
                (p, e) -> new WhitelistedPlayersMenu(playerMenuUtility, this).open(), null);
        registerButton(getSlot(path + "TurnOn", 3), lgm.getItem(path + "TurnOn", player, false), (p, e) -> Bukkit.setWhitelist(true), null);
        registerButton(getSlot(path + "TurnOff", 5), lgm.getItem(path + "TurnOff", player, false), (p, e) -> Bukkit.setWhitelist(false), null);
        registerButton(getSlot(path + "Reload", 4), lgm.getItem(path + "Reload", player, false), (p, e) -> Bukkit.reloadWhitelist(), null);
        registerButton(getSlot("General.Close", 26), lgm.getItem("General.Back", player, false),
                MenuAction.of((p, e) -> new ServerManagerMenu(playerMenuUtility).open()).requires("AdminPanel.Button.Close"), null);
    }

    private void promptName(Player player, boolean add, String message) {
        MenuAction.prompt(message, (p, text) -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(text);
            if (target.isWhitelisted() == add) {
                promptName(p, add, null);
                return;
            }
            target.setWhitelisted(add);
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", target.getName(), true);
            p.sendMessage(lgm.getMessage("Player.ServerManager.WhitelistManager." + (add ? "AddedPlayer" : "RemovedPlayer"), p, true));
            open();
        }).execute(player, null);
    }
}
