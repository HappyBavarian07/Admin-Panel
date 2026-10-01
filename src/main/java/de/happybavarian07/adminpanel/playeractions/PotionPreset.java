package de.happybavarian07.adminpanel.playeractions;

import java.util.List;

/**
 * The effect combinations of the one-click buttons in the potion menu. The language file names them through
 * {@link #languageKey()}.
 */
public enum PotionPreset {
    SPEED_RUN("SpeedRun", new Effect("speed", 3, 300), new Effect("jump_boost", 2, 300)),
    NIGHT_VISION("NightVision", new Effect("night_vision", 1, 600)),
    TANK("Tank", new Effect("resistance", 3, 300), new Effect("regeneration", 2, 300),
            new Effect("absorption", 3, 300), new Effect("health_boost", 3, 300)),
    LEVITATION_PRANK("LevitationPrank", new Effect("levitation", 1, 6), new Effect("slow_falling", 1, 20)),
    BLINDNESS_PRANK("BlindnessPrank", new Effect("blindness", 1, 10), new Effect("nausea", 1, 10));

    /**
     * @param key     the effect key without namespace
     * @param level   the effect level starting at 1
     * @param seconds how long the effect lasts
     */
    public record Effect(String key, int level, int seconds) {
    }

    private final String languageKey;
    private final List<Effect> effects;

    PotionPreset(String languageKey, Effect... effects) {
        this.languageKey = languageKey;
        this.effects = List.of(effects);
    }

    /**
     * The name below {@code PlayerManager.PotionMenu.Presets} in the language file.
     */
    public String languageKey() {
        return languageKey;
    }

    public List<Effect> effects() {
        return effects;
    }
}
