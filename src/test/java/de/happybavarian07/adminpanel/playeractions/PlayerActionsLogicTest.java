package de.happybavarian07.adminpanel.playeractions;

import org.bukkit.entity.Cow;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zombie;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerActionsLogicTest {
    @Test
    void prettyWritesRegistryKeysAsTitles() {
        assertEquals("Night Vision", KeyNames.pretty("night_vision"));
        assertEquals("Speed", KeyNames.pretty("speed"));
        assertEquals("Zombified Piglin", KeyNames.pretty("zombified_piglin"));
    }

    @Test
    void entitiesAreSortedIntoTheFilterCategories() {
        assertEquals(SpawnCategory.HOSTILE, SpawnCategory.of(Zombie.class));
        assertEquals(SpawnCategory.PASSIVE, SpawnCategory.of(Cow.class));
        assertEquals(SpawnCategory.PASSIVE, SpawnCategory.of(Villager.class));
        assertEquals(SpawnCategory.WATER, SpawnCategory.of(Squid.class));
        assertEquals(SpawnCategory.BOSS, SpawnCategory.of(EnderDragon.class));
        assertEquals(SpawnCategory.BOSS, SpawnCategory.of(Wither.class));
        assertNull(SpawnCategory.of(Minecart.class));
        assertNull(SpawnCategory.of(null));
    }

    @Test
    void effectsWithoutTheCategoryApiAreSortedByKey() {
        assertEquals(EffectTypes.Category.HARMFUL, EffectTypes.fallbackCategory("poison"));
        assertEquals(EffectTypes.Category.HARMFUL, EffectTypes.fallbackCategory("levitation"));
        assertEquals(EffectTypes.Category.BENEFICIAL, EffectTypes.fallbackCategory("speed"));
        assertEquals(EffectTypes.Category.NEUTRAL, EffectTypes.fallbackCategory("glowing"));
    }

    @Test
    void everyPresetHasEffects() {
        for (PotionPreset preset : PotionPreset.values()) {
            assertFalse(preset.effects().isEmpty(), preset.name());
            for (PotionPreset.Effect effect : preset.effects()) {
                assertTrue(effect.level() >= 1 && effect.level() <= 255 && effect.seconds() > 0, preset.name());
            }
        }
    }
}
