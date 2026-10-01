package de.happybavarian07.adminpanel.trolls;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/** An explosion sound and particles close to the target, only the target sees and hears them. Nothing is damaged. */
public final class FakeExplosion implements Troll {
    @Override
    public String id() {
        return "FakeExplosion";
    }

    @Override
    public Kind kind() {
        return Kind.ONE_SHOT;
    }

    @Override
    public void start(Player staff, Player target) {
        Location spot = TrollSupport.ahead(target.getLocation(), 3.0).add(0, 1, 0);
        target.playSound(spot, Sound.ENTITY_GENERIC_EXPLODE, 4.0f, 1.0f);
        Particle particle = TrollSupport.particle("EXPLOSION_EMITTER", "EXPLOSION_HUGE");
        if (particle != null) target.spawnParticle(particle, spot, 1);
    }
}
