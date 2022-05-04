package de.happybavarian07.adminpanel.events.player;

import de.happybavarian07.adminpanel.events.AdminPanelEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

public class GiveEffectToPlayerEvent extends AdminPanelEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final PotionEffect effect;

    public GiveEffectToPlayerEvent(Player player, PotionEffect effect) {
        this.player = player;
        this.effect = effect;
    }

    public Player getPlayer() {
        return player;
    }

    public PotionEffect getEffect() {
        return effect;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;

    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
