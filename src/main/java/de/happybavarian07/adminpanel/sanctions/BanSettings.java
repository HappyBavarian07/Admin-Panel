package de.happybavarian07.adminpanel.sanctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The reason presets and ban lengths of the ban and kick menus, read from {@code Pman.Actions.Ban}.
 *
 * @param presets the reason presets in config order
 * @param lengths the ban lengths in config order
 */
public record BanSettings(List<Preset> presets, List<Length> lengths) {
    /**
     * @param name   the text of the button
     * @param reason the reason that is used when the button is clicked
     */
    public record Preset(String name, String reason) {
    }

    /**
     * @param label  the text as written in the config
     * @param millis the ban length, {@code 0} for a permanent ban
     */
    public record Length(String label, long millis) {
    }

    /**
     * Reads the settings, skipping what is invalid instead of failing.
     *
     * @param rawPresets   the value of {@code Pman.Actions.Ban.Presets}: maps with {@code name} and {@code reason}
     * @param rawDurations the value of {@code Pman.Actions.Ban.Durations}: texts such as {@code 1d} or
     *                     {@code permanent}
     * @param log          receives a message for every invalid value
     */
    public static BanSettings parse(List<? extends Map<?, ?>> rawPresets, List<String> rawDurations, Consumer<String> log) {
        List<Preset> presets = new ArrayList<>();
        if (rawPresets != null) {
            for (Map<?, ?> raw : rawPresets) {
                String name = text(raw.get("name"));
                if (name.isBlank()) {
                    log.accept("Skipping Pman.Actions.Ban.Presets entry without a name: " + raw);
                    continue;
                }
                String reason = text(raw.get("reason"));
                presets.add(new Preset(name, reason.isBlank() ? name : reason));
            }
        }
        List<Length> lengths = new ArrayList<>();
        if (rawDurations != null) {
            for (String raw : rawDurations) {
                try {
                    lengths.add(new Length(raw.trim(), Durations.parse(raw)));
                } catch (IllegalArgumentException e) {
                    log.accept("Skipping invalid Pman.Actions.Ban.Durations entry '" + raw + "': " + e.getMessage());
                }
            }
        }
        return new BanSettings(List.copyOf(presets), List.copyOf(lengths));
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
