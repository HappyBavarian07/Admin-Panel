package de.happybavarian07.adminpanel.trolls;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Turns the target's view every tick for 5 seconds, then ends by itself. */
public final class SpinCamera implements Troll {
    private static final int DURATION_TICKS = 100;
    private static final float DEGREES_PER_TICK = 15.0f;

    private final Plugin plugin;
    private final Map<UUID, BukkitTask> running = new HashMap<>();

    public SpinCamera(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String id() {
        return "SpinCamera";
    }

    @Override
    public Kind kind() {
        return Kind.ONE_SHOT;
    }

    @Override
    public boolean isActive(UUID target) {
        return running.containsKey(target);
    }

    @Override
    public void start(Player staff, Player target) {
        stop(target);
        UUID id = target.getUniqueId();
        running.put(id, new BukkitRunnable() {
            private int ticks;

            @Override
            public void run() {
                if (ticks++ >= DURATION_TICKS || !target.isOnline()) {
                    running.remove(id);
                    cancel();
                    return;
                }
                Location view = target.getLocation();
                view.setYaw(view.getYaw() + DEGREES_PER_TICK);
                target.teleport(view);
            }
        }.runTaskTimer(plugin, 0L, 1L));
    }

    @Override
    public void stop(Player target) {
        BukkitTask task = running.remove(target.getUniqueId());
        if (task != null) task.cancel();
    }
}
