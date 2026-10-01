package de.happybavarian07.adminpanel.playeractions;

import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Spawns entities for the spawning menu.
 */
public final class EntitySpawner {
    private static final Set<String> EXCLUDED = Set.of("player", "item", "item_frame", "glow_item_frame",
            "area_effect_cloud", "eye_of_ender", "fishing_bobber", "lightning_bolt", "painting", "leash_knot", "marker",
            "interaction", "block_display", "item_display", "text_display");

    private EntitySpawner() {
    }

    /**
     * What to spawn next to the type.
     *
     * @param count   how many entities to spawn
     * @param baby    spawn them as babies where the entity has a baby form
     * @param name    the custom name with {@code &} color codes, empty for none
     * @param glowing whether the entities glow
     * @param armor   whether mobs get an iron armor set that does not drop
     * @param rider   whether every entity gets a skeleton as its rider
     */
    public record Options(int count, boolean baby, String name, boolean glowing, boolean armor, boolean rider) {
    }

    /**
     * The entity types the menu offers, sorted by key.
     */
    public static List<EntityType> spawnableTypes() {
        List<EntityType> types = new ArrayList<>();
        for (EntityType type : EntityType.values()) {
            String key = key(type);
            if (key == null || EXCLUDED.contains(key) || !type.isSpawnable()) continue;
            types.add(type);
        }
        types.sort(Comparator.comparing(EntitySpawner::key));
        return types;
    }

    /**
     * The key of an entity type without namespace, or {@code null} for the unknown type.
     */
    public static String key(EntityType type) {
        try {
            return type.getKey().getKey();
        } catch (IllegalArgumentException unknown) {
            return null;
        }
    }

    /**
     * Spawns entities at a location.
     *
     * @return how many entities were spawned, fewer than requested if the server refused the type
     */
    public static int spawn(Location at, EntityType type, Options options) {
        World world = at.getWorld();
        if (world == null) return 0;
        int spawned = 0;
        for (int i = 0; i < options.count(); i++) {
            try {
                Entity entity = world.spawnEntity(at, type);
                decorate(entity, options);
                if (options.rider()) entity.addPassenger(world.spawnEntity(at, EntityType.SKELETON));
                spawned++;
            } catch (IllegalArgumentException refused) {
                break;
            }
        }
        return spawned;
    }

    private static void decorate(Entity entity, Options options) {
        if (options.baby()) {
            if (entity instanceof Zombie zombie) zombie.setBaby(true);
            else if (entity instanceof Ageable ageable) ageable.setBaby();
        }
        if (!options.name().isEmpty()) {
            entity.setCustomName(AdminPanelUtils.chat(options.name()));
            entity.setCustomNameVisible(true);
        }
        if (options.glowing()) entity.setGlowing(true);
        if (options.armor() && entity instanceof Mob mob && mob.getEquipment() != null) {
            EntityEquipment equipment = mob.getEquipment();
            equipment.setHelmet(new ItemStack(Material.IRON_HELMET));
            equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            equipment.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
            equipment.setBoots(new ItemStack(Material.IRON_BOOTS));
            equipment.setHelmetDropChance(0f);
            equipment.setChestplateDropChance(0f);
            equipment.setLeggingsDropChance(0f);
            equipment.setBootsDropChance(0f);
        }
    }
}
