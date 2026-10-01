package de.happybavarian07.adminpanel.trolls;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Throws the target up and a little forward. The next fall damage of the target is cancelled, at the latest for
 * {@value #PROTECTION_TICKS} ticks.
 */
public final class Launch implements Troll, Listener {
    private static final int PROTECTION_TICKS = 200;

    private final Plugin plugin;
    private final Map<UUID, BukkitTask> protectedUntilLanding = new HashMap<>();

    public Launch(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String id() {
        return "Launch";
    }

    @Override
    public Kind kind() {
        return Kind.ONE_SHOT;
    }

    @Override
    public boolean isActive(UUID target) {
        return protectedUntilLanding.containsKey(target);
    }

    @Override
    public void start(Player staff, Player target) {
        UUID id = target.getUniqueId();
        BukkitTask previous = protectedUntilLanding.remove(id);
        if (previous != null) previous.cancel();
        protectedUntilLanding.put(id, plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> protectedUntilLanding.remove(id), PROTECTION_TICKS));
        Vector push = target.getLocation().getDirection().setY(0);
        push = push.lengthSquared() < 1.0E-6 ? new Vector() : push.normalize().multiply(0.6);
        target.setVelocity(push.setY(1.0));
    }

    @Override
    public void stop(Player target) {
        BukkitTask task = protectedUntilLanding.remove(target.getUniqueId());
        if (task != null) task.cancel();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFall(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        BukkitTask task = protectedUntilLanding.remove(event.getEntity().getUniqueId());
        if (task == null) return;
        task.cancel();
        event.setCancelled(true);
    }
}
