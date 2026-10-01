package de.happybavarian07.adminpanel.playeractions;

import org.bukkit.Keyed;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Looks up the potion effect types of the server. The effect category and the type list are read by reflection
 * because the API they come from differs between server versions.
 */
public final class EffectTypes {
    private static final Set<String> HARMFUL_FALLBACK = Set.of("slowness", "mining_fatigue", "instant_damage", "nausea",
            "blindness", "hunger", "weakness", "poison", "wither", "levitation", "unluck", "darkness", "bad_omen",
            "raid_omen", "trial_omen", "infested", "oozing", "weaving", "wind_charged");
    private static final Set<String> NEUTRAL_FALLBACK = Set.of("glowing", "invisibility");

    private EffectTypes() {
    }

    public enum Category {
        BENEFICIAL,
        HARMFUL,
        NEUTRAL
    }

    /**
     * All effect types of the server sorted by key.
     */
    public static List<PotionEffectType> all() {
        List<PotionEffectType> types = new ArrayList<>();
        try {
            Object[] values = (Object[]) PotionEffectType.class.getMethod("values").invoke(null);
            for (Object value : values) {
                if (value instanceof PotionEffectType type) types.add(type);
            }
        } catch (ReflectiveOperationException | ClassCastException e) {
            return types;
        }
        types.sort(Comparator.comparing(EffectTypes::key));
        return types;
    }

    /**
     * The key of an effect type without namespace, for example {@code night_vision}.
     */
    public static String key(PotionEffectType type) {
        return ((Keyed) type).getKey().getKey();
    }

    /**
     * The readable name of an effect type, for example {@code Night Vision}.
     */
    public static String name(PotionEffectType type) {
        return KeyNames.pretty(key(type));
    }

    /**
     * Finds an effect type by its key.
     *
     * @param key the key without namespace
     * @return the type, or {@code null} if the server has no such effect
     */
    public static PotionEffectType byKey(String key) {
        for (PotionEffectType type : all()) {
            if (key(type).equals(key)) return type;
        }
        return null;
    }

    /**
     * The category of an effect type. Servers without the category API get it from a fixed list of keys.
     */
    public static Category category(PotionEffectType type) {
        for (String methodName : new String[]{"getCategory", "getEffectCategory"}) {
            try {
                Method method = PotionEffectType.class.getMethod(methodName);
                Object result = method.invoke(type);
                if (result != null) return Category.valueOf(result.toString().toUpperCase(Locale.ROOT));
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            }
        }
        return fallbackCategory(key(type));
    }

    static Category fallbackCategory(String key) {
        if (HARMFUL_FALLBACK.contains(key)) return Category.HARMFUL;
        if (NEUTRAL_FALLBACK.contains(key)) return Category.NEUTRAL;
        return Category.BENEFICIAL;
    }
}
