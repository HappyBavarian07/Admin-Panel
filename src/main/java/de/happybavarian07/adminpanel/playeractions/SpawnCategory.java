package de.happybavarian07.adminpanel.playeractions;

import org.bukkit.entity.Boss;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.WaterMob;

/**
 * The groups of the category filter in the spawning menu.
 */
public enum SpawnCategory {
    HOSTILE,
    PASSIVE,
    WATER,
    BOSS;

    /**
     * Sorts an entity class into a category.
     *
     * @param type the entity class of an entity type
     * @return the category, or {@code null} for entities that are no mobs, such as minecarts or projectiles
     */
    public static SpawnCategory of(Class<?> type) {
        if (type == null) return null;
        if (Boss.class.isAssignableFrom(type)) return BOSS;
        if (WaterMob.class.isAssignableFrom(type)) return WATER;
        if (Enemy.class.isAssignableFrom(type)) return HOSTILE;
        if (LivingEntity.class.isAssignableFrom(type)) return PASSIVE;
        return null;
    }
}
