package de.happybavarian07.adminpanel.trolls;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.util.Vector;

final class TrollSupport {
    private TrollSupport() {
    }

    /**
     * Finds a particle by the first name that exists on the running server, since particle names changed between
     * versions.
     *
     * @return the particle, or {@code null} if no name matches
     */
    static Particle particle(String... names) {
        for (String name : names) {
            try {
                return Particle.valueOf(name);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    /**
     * Finds an attribute by the first name that exists on the running server. The constants are looked up by
     * reflection because their names (and the type itself) differ between the API this plugin is built against and
     * newer servers.
     *
     * @return the attribute, or {@code null} if no name matches
     */
    static Attribute attribute(String... names) {
        for (String name : names) {
            try {
                return (Attribute) Attribute.class.getField(name).get(null);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    /** A point in the horizontal looking direction of {@code from}, or {@code from} itself when looking straight up or down. */
    static Location ahead(Location from, double distance) {
        Vector direction = from.getDirection().setY(0);
        if (direction.lengthSquared() < 1.0E-6) return from.clone();
        return from.clone().add(direction.normalize().multiply(distance));
    }
}
