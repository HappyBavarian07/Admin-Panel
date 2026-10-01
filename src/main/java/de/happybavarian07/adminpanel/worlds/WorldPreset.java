package de.happybavarian07.adminpanel.worlds;

import org.bukkit.WorldType;

/**
 * The world kinds of the create menu. The language file names them through {@link #languageKey()}.
 */
public enum WorldPreset {
    NORMAL("Normal", WorldType.NORMAL, ""),
    FLAT("Flat", WorldType.FLAT, ""),
    VOID("Void", WorldType.FLAT, "{\"layers\":[{\"block\":\"minecraft:air\",\"height\":1}],\"biome\":\"minecraft:the_void\"}"),
    AMPLIFIED("Amplified", WorldType.AMPLIFIED, ""),
    LARGE_BIOMES("LargeBiomes", WorldType.LARGE_BIOMES, "");

    private final String languageKey;
    private final WorldType type;
    private final String generatorSettings;

    WorldPreset(String languageKey, WorldType type, String generatorSettings) {
        this.languageKey = languageKey;
        this.type = type;
        this.generatorSettings = generatorSettings;
    }

    public String languageKey() {
        return languageKey;
    }

    public WorldType type() {
        return type;
    }

    /**
     * @return the generator settings for {@code WorldCreator.generatorSettings}, empty for the default generator
     */
    public String generatorSettings() {
        return generatorSettings;
    }

    public WorldPreset next() {
        WorldPreset[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
