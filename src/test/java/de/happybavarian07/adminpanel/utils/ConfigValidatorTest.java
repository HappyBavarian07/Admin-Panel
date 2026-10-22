package de.happybavarian07.adminpanel.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigValidatorTest {
    private static YamlConfiguration yaml(String text) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(text);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return config;
    }

    private static final String TEMPLATE = "speed: 1\nname: abc\nenabled: true\nlist:\n  - a\nsection:\n  inner: 5\n";

    @Test
    void aMatchingConfigHasNoProblems() {
        assertEquals(List.of(), ConfigValidator.validate(yaml(TEMPLATE), yaml(TEMPLATE.replace("speed: 1", "speed: 2.5"))));
    }

    @Test
    void unknownMissingAndWrongTypedKeysAreReported() {
        String actual = "speed: fast\nname: abc\nenabled: true\nlist:\n  - a\nsection:\n  inner: 5\n  extra: 1\nstray: 1\n";
        List<ConfigValidator.Problem> problems = ConfigValidator.validate(yaml(TEMPLATE), yaml(actual));
        assertEquals(3, problems.size(), problems.toString());
        assertTrue(problems.contains(new ConfigValidator.Problem("speed", "expected a number but found a text")));
        assertTrue(problems.contains(new ConfigValidator.Problem("section.extra", "unknown key")));
        assertTrue(problems.contains(new ConfigValidator.Problem("stray", "unknown key")));
    }

    @Test
    void aMissingKeyIsReported() {
        List<ConfigValidator.Problem> problems = ConfigValidator.validate(yaml(TEMPLATE), yaml("speed: 1\n"));
        assertTrue(problems.contains(new ConfigValidator.Problem("enabled", "missing")));
    }
}
