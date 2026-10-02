import de.happybavarian07.adminpanel.playeractions.EffectTypes;
import de.happybavarian07.adminpanel.playeractions.EntitySpawner;
import de.happybavarian07.adminpanel.playeractions.PotionPreset;
import de.happybavarian07.adminpanel.playeractions.SpawnCategory;
import de.happybavarian07.adminpanel.worlds.GameRuleCategory;
import de.happybavarian07.adminpanel.worlds.GameRuleEdit;
import de.happybavarian07.adminpanel.worlds.GameRules;
import de.happybavarian07.adminpanel.worlds.TimeEdit;
import de.happybavarian07.adminpanel.worlds.WorldOperations;
import de.happybavarian07.adminpanel.worlds.WorldPreset;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Throwaway server-side check of what the potion, spawning and world menus do without a player: runs the same classes
 * on a real server and prints PROBE lines. Used by tools/run-probe.sh, not shipped.
 */
public class ApProbe extends JavaPlugin {
    private int failures;

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, this::run, 40L);
    }

    private void ok(String name, String detail) {
        getLogger().info("PROBE ok " + name + " " + detail);
    }

    private void fail(String name, Throwable error) {
        failures++;
        getLogger().severe("PROBE FAIL " + name + " " + error);
        for (StackTraceElement line : error.getStackTrace()) {
            if (line.getClassName().startsWith("de.happybavarian07") || line.getClassName().equals("ApProbe")) {
                getLogger().severe("PROBE   at " + line);
                break;
            }
        }
    }

    private void check(String name, Runnable check) {
        try {
            check.run();
        } catch (Throwable error) {
            fail(name, error);
        }
    }

    private void expect(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private void run() {
        World main = Bukkit.getWorlds().get(0);
        getLogger().info("PROBE server " + Bukkit.getVersion() + " / " + Bukkit.getBukkitVersion());
        check("effects", this::effects);
        check("effect-apply", () -> effectApply(main));
        check("spawning", () -> spawning(main));
        check("gamerules", () -> gameRules(main));
        check("time-weather", () -> timeWeather(main));
        worldLifecycle();
    }

    private void effects() {
        List<PotionEffectType> all = EffectTypes.all();
        expect(all.size() >= 20, "only " + all.size() + " effect types");
        Map<EffectTypes.Category, Integer> categories = new EnumMap<>(EffectTypes.Category.class);
        for (PotionEffectType type : all) {
            expect(!EffectTypes.key(type).isEmpty(), "empty key");
            expect(!EffectTypes.name(type).isEmpty(), "empty name");
            categories.merge(EffectTypes.category(type), 1, Integer::sum);
        }
        for (PotionPreset preset : PotionPreset.values()) {
            for (PotionPreset.Effect effect : preset.effects()) {
                expect(EffectTypes.byKey(effect.key()) != null, "preset " + preset + " has no effect " + effect.key());
            }
        }
        expect(categories.size() >= 2, "categories " + categories);
        ok("effects", all.size() + " types " + categories);
    }

    private void effectApply(World world) {
        Location at = world.getSpawnLocation();
        LivingEntity target = (LivingEntity) world.spawnEntity(at, EntityType.COW);
        try {
            int applied = 0;
            for (PotionPreset preset : PotionPreset.values()) {
                for (PotionPreset.Effect effect : preset.effects()) {
                    PotionEffectType type = EffectTypes.byKey(effect.key());
                    PotionEffect potion = new PotionEffect(type, effect.seconds() * 20, effect.level() - 1, false, false, false);
                    expect(target.addPotionEffect(potion), "refused " + effect.key());
                    expect(target.hasPotionEffect(type), "not active " + effect.key());
                    target.removePotionEffect(type);
                    applied++;
                }
            }
            ok("effect-apply", applied + " preset effects applied and removed");
        } finally {
            target.remove();
        }
    }

    private void spawning(World world) {
        List<EntityType> types = EntitySpawner.spawnableTypes();
        expect(types.size() >= 50, "only " + types.size() + " spawnable types");
        Location at = world.getSpawnLocation();
        EntitySpawner.Options plain = new EntitySpawner.Options(1, false, "", false, false, false);
        EntitySpawner.Options fancy = new EntitySpawner.Options(2, true, "&aProbe", true, true, true);
        Map<SpawnCategory, Integer> categories = new EnumMap<>(SpawnCategory.class);
        List<String> refused = new ArrayList<>();
        int spawned = 0;
        List<Entity> before = new ArrayList<>(world.getEntities());
        for (EntityType type : types) {
            Class<?> entityClass = type.getEntityClass();
            SpawnCategory category = SpawnCategory.of(entityClass);
            if (category != null) categories.merge(category, 1, Integer::sum);
            String key = EntitySpawner.key(type);
            if (key.equals("ender_dragon") || key.equals("wither")) continue;
            int count = EntitySpawner.spawn(at, type, plain);
            if (count == 0) refused.add(key);
            spawned += count;
        }
        for (EntityType type : List.of(EntityType.ZOMBIE, EntityType.COW, EntityType.SKELETON, EntityType.VILLAGER)) {
            expect(EntitySpawner.spawn(at, type, fancy) == 2, "fancy spawn of " + type);
        }
        for (Entity entity : world.getEntities()) {
            if (!before.contains(entity)) entity.remove();
        }
        expect(spawned >= 40, "only " + spawned + " spawned, refused " + refused);
        ok("spawning", types.size() + " types, " + spawned + " spawned, categories " + categories + ", refused " + refused);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void gameRules(World world) {
        String[] names = world.getGameRules();
        expect(names.length >= 20, "only " + names.length + " gamerules");
        Map<GameRuleCategory, Integer> categories = new EnumMap<>(GameRuleCategory.class);
        List<String> unreadable = new ArrayList<>();
        int changed = 0;
        for (String name : names) {
            GameRule<?> rule = GameRules.byName(name);
            if (rule == null) {
                unreadable.add(name);
                continue;
            }
            categories.merge(GameRuleCategory.of(GameRules.nameOf(rule)), 1, Integer::sum);
            Object current = world.getGameRuleValue(rule);
            Object next = GameRuleEdit.next(current, GameRuleEdit.Step.UP);
            if (next != null && !next.equals(current)) {
                world.setGameRule((GameRule) rule, next);
                expect(next.equals(world.getGameRuleValue(rule)), "rule " + name + " did not change");
                world.setGameRule((GameRule) rule, current);
                changed++;
            }
        }
        expect(unreadable.isEmpty(), "rules without a GameRule object: " + unreadable);
        ok("gamerules", names.length + " rules, " + changed + " changed and restored, categories " + categories);
    }

    private void timeWeather(World world) {
        long before = world.getTime();
        world.setTime(TimeEdit.add(before, 1000));
        expect(world.getTime() == TimeEdit.add(before, 1000) || world.getTime() != before, "time did not move");
        world.setTime(before);
        boolean storm = world.hasStorm();
        world.setStorm(!storm);
        world.setWeatherDuration(100);
        world.setThundering(false);
        world.setStorm(storm);
        world.setClearWeatherDuration(100);
        ok("time-weather", "time and weather set");
    }

    private void worldLifecycle() {
        for (String name : List.of("probe_a", "probe_b", "probe_c")) {
            org.bukkit.World stale = Bukkit.getWorld(name);
            if (stale != null) Bukkit.unloadWorld(stale, false);
            try {
                de.happybavarian07.adminpanel.worlds.WorldFolders.delete(WorldOperations.folder(name));
            } catch (java.io.IOException ignored) {
            }
        }
        long seed = 12345L;
        WorldOperations.Result created = WorldOperations.create(new WorldOperations.CreateRequest("probe_a", WorldPreset.VOID,
                World.Environment.NORMAL, seed, Difficulty.PEACEFUL, false, false));
        if (created != WorldOperations.Result.OK) {
            fail("world-create", new IllegalStateException(String.valueOf(created)));
            finish();
            return;
        }
        World a = Bukkit.getWorld("probe_a");
        check("world-create", () -> {
            expect(a != null, "world is not loaded");
            expect(a.getSeed() == seed, "seed " + a.getSeed());
            expect(a.getDifficulty() == Difficulty.PEACEFUL, "difficulty");
            ok("world-create", "probe_a, void preset, seed " + a.getSeed());
        });
        WorldOperations.save(a);
        WorldOperations.duplicate(a, "probe_b", duplicated -> {
            check("world-duplicate", () -> {
                expect(duplicated == WorldOperations.Result.OK, "duplicate " + duplicated);
                expect(Bukkit.getWorld("probe_b") != null, "copy not loaded");
                ok("world-duplicate", "probe_b");
            });
            check("world-rename", () -> {
                WorldOperations.Result renamed = WorldOperations.rename(Bukkit.getWorld("probe_b"), "probe_c");
                expect(renamed == WorldOperations.Result.OK, "rename " + renamed);
                expect(Bukkit.getWorld("probe_c") != null && Bukkit.getWorld("probe_b") == null, "worlds after rename");
                ok("world-rename", "probe_b -> probe_c");
            });
            check("world-main-protected", () -> {
                expect(WorldOperations.unload(Bukkit.getWorlds().get(0), true) == WorldOperations.Result.MAIN_WORLD, "main world was not refused");
                ok("world-main-protected", "refused");
            });
            World c = Bukkit.getWorld("probe_c");
            WorldOperations.delete(a, deletedA -> {
                if (c == null) {
                    finish();
                    return;
                }
                WorldOperations.delete(c, deletedC -> {
                    check("world-delete", () -> {
                        expect(deletedA == WorldOperations.Result.OK && deletedC == WorldOperations.Result.OK, deletedA + " " + deletedC);
                        expect(!WorldOperations.folder("probe_a").exists() && !WorldOperations.folder("probe_c").exists(), "folders left");
                        ok("world-delete", "folders removed");
                    });
                    finish();
                });
            });
        });
    }

    private void finish() {
        getLogger().info("PROBE DONE failures=" + failures);
    }
}
