package de.happybavarian07.adminpanel.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Compares a loaded config with the one shipped in the jar and reports what looks wrong: keys the plugin does not know,
 * keys that are missing and values of another type than the shipped default. It only reports, it never changes a file.
 */
public final class ConfigValidator {
    /**
     * @param path    the full key
     * @param message what is wrong with it
     */
    public record Problem(String path, String message) {
        @Override
        public String toString() {
            return path + ": " + message;
        }
    }

    private ConfigValidator() {
    }

    /**
     * @param template the config of the jar
     * @param actual   the config the server owner edited
     * @return the problems, in the order of the file; empty if everything fits
     */
    public static List<Problem> validate(FileConfiguration template, FileConfiguration actual) {
        List<Problem> problems = new ArrayList<>();
        for (String path : actual.getKeys(true)) {
            if (actual.isConfigurationSection(path)) {
                if (!template.contains(path) && !insideValue(template, path)) problems.add(new Problem(path, "unknown section"));
                continue;
            }
            if (!template.contains(path)) {
                if (!insideValue(template, path)) problems.add(new Problem(path, "unknown key"));
                continue;
            }
            Object expected = template.get(path);
            Object found = actual.get(path);
            if (expected != null && found != null && !sameKind(expected, found)) {
                problems.add(new Problem(path, "expected " + kind(expected) + " but found " + kind(found)));
            }
        }
        for (String path : template.getKeys(true)) {
            if (!template.isConfigurationSection(path) && !actual.contains(path)) problems.add(new Problem(path, "missing"));
        }
        return problems;
    }

    /** True if an ancestor of the path is a plain value in the template, so the owner may put anything below it. */
    private static boolean insideValue(FileConfiguration template, String path) {
        int dot = path.lastIndexOf('.');
        while (dot > 0) {
            String parent = path.substring(0, dot);
            if (template.contains(parent) && !(template.get(parent) instanceof ConfigurationSection)) return true;
            dot = parent.lastIndexOf('.');
        }
        return false;
    }

    private static boolean sameKind(Object expected, Object found) {
        return kind(expected).equals(kind(found));
    }

    private static String kind(Object value) {
        if (value instanceof Boolean) return "a boolean";
        if (value instanceof Number) return "a number";
        if (value instanceof List) return "a list";
        if (value instanceof String) return "a text";
        return value.getClass().getSimpleName();
    }
}
