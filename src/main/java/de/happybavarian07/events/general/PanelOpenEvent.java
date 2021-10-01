package de.happybavarian07.events.general;

import de.happybavarian07.events.AdminPanelEvent;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class PanelOpenEvent extends AdminPanelEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private Menu openedMenu;
    private PlayerMenuUtility playerMenuUtility;

    public PanelOpenEvent(Player player, Menu openedMenu, PlayerMenuUtility playerMenuUtility) {
        this.player = player;
        this.openedMenu = openedMenu;
        this.playerMenuUtility = playerMenuUtility;
    }

    public Player getPlayer() {
        return player;
    }

    public Menu getOpenedMenu() {
        return openedMenu;
    }

    public void setOpenedMenu(Menu menu) {
        this.openedMenu = menu;
    }

    public PlayerMenuUtility getPlayerMenuUtility() {
        return playerMenuUtility;
    }

    public void setPlayerMenuUtility(PlayerMenuUtility playerMenuUtility) {
        this.playerMenuUtility = playerMenuUtility;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
