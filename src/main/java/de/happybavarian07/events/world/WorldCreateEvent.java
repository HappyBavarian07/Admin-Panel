package de.happybavarian07.events.world;

import de.happybavarian07.events.AdminPanelEvent;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class WorldCreateEvent extends AdminPanelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private String name;
    private WorldType worldType;
    private World.Environment worldEnvironment;
    private boolean generateStructures;
    private boolean hardcore;

    public WorldCreateEvent(Player player, String name, WorldType worldType, World.Environment worldEnvironment, boolean generateStructures, boolean hardcore) {
        this.player = player;
        this.name = name;
        this.worldType = worldType;
        this.worldEnvironment = worldEnvironment;
        this.generateStructures = generateStructures;
        this.hardcore = hardcore;
    }

    public Player getPlayer() {
        return player;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGenerateStructures(boolean generateStructures) {
        this.generateStructures = generateStructures;
    }

    public void setHardcore(boolean hardcore) {
        this.hardcore = hardcore;
    }

    public void setWorldEnvironment(World.Environment worldEnvironment) {
        this.worldEnvironment = worldEnvironment;
    }

    public void setWorldType(WorldType worldType) {
        this.worldType = worldType;
    }

    public String getName() {
        return name;
    }

    public World.Environment getWorldEnvironment() {
        return worldEnvironment;
    }

    public WorldType getWorldType() {
        return worldType;
    }

    public boolean isGenerateStructures() {
        return generateStructures;
    }

    public boolean isHardcore() {
        return hardcore;
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
    public HandlerList getHandlers() {
        return handlers;
    }
}
