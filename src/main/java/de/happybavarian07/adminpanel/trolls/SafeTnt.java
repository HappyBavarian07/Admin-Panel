package de.happybavarian07.adminpanel.trolls;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Primed TNT next to the target that is removed before it can explode and only plays the explosion sound and
 * particles. The TNT carries a persistent tag and every explosion and damage event of a tagged TNT is cancelled as a
 * second safety.
 */
public final class SafeTnt implements Troll, Listener {
    private static final int REMOVE_AFTER_TICKS = 30;
    private static final int FUSE_TICKS = 80;

    private final Plugin plugin;
    private final NamespacedKey tag;
    private final Map<UUID, List<Pending>> pending = new HashMap<>();

    private record Pending(TNTPrimed tnt, BukkitTask task) {
    }

    public SafeTnt(Plugin plugin) {
        this.plugin = plugin;
        this.tag = new NamespacedKey(plugin, "safe_tnt");
    }

    @Override
    public String id() {
        return "SafeTnt";
    }

    @Override
    public Kind kind() {
        return Kind.ONE_SHOT;
    }

    @Override
    public boolean isActive(UUID target) {
        List<Pending> list = pending.get(target);
        return list != null && !list.isEmpty();
    }

    @Override
    public void start(Player staff, Player target) {
        Location spot = TrollSupport.ahead(target.getLocation(), 2.0);
        TNTPrimed tnt = target.getWorld().spawn(spot, TNTPrimed.class);
        tnt.getPersistentDataContainer().set(tag, PersistentDataType.BYTE, (byte) 1);
        tnt.setFuseTicks(FUSE_TICKS);
        tnt.setYield(0);
        tnt.setIsIncendiary(false);
        tnt.setPersistent(false);
        UUID id = target.getUniqueId();
        Pending[] self = new Pending[1];
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            remove(id, self[0]);
            detonate(tnt);
        }, REMOVE_AFTER_TICKS);
        self[0] = new Pending(tnt, task);
        pending.computeIfAbsent(id, key -> new ArrayList<>()).add(self[0]);
    }

    @Override
    public void stop(Player target) {
        List<Pending> list = pending.remove(target.getUniqueId());
        if (list == null) return;
        for (Pending entry : list) {
            entry.task().cancel();
            entry.tnt().remove();
        }
    }

    private void remove(UUID target, Pending entry) {
        List<Pending> list = pending.get(target);
        if (list == null) return;
        list.remove(entry);
        if (list.isEmpty()) pending.remove(target);
    }

    private void detonate(TNTPrimed tnt) {
        Location where = tnt.getLocation();
        tnt.remove();
        where.getWorld().playSound(where, Sound.ENTITY_GENERIC_EXPLODE, 4.0f, 1.0f);
        Particle particle = TrollSupport.particle("EXPLOSION_EMITTER", "EXPLOSION_HUGE");
        if (particle != null) where.getWorld().spawnParticle(particle, where, 1);
    }

    private boolean isTagged(Entity entity) {
        return entity instanceof TNTPrimed && entity.getPersistentDataContainer().has(tag, PersistentDataType.BYTE);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(EntityExplodeEvent event) {
        if (!isTagged(event.getEntity())) return;
        event.blockList().clear();
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrime(ExplosionPrimeEvent event) {
        if (isTagged(event.getEntity())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (isTagged(event.getDamager())) event.setCancelled(true);
    }
}
