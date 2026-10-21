package de.happybavarian07.adminpanel.language;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LanguageParityTest {
    @Test
    void englishAndGermanHaveTheSameKeys() throws IOException {
        Set<String> english = keys("languages/en.yml");
        Set<String> german = keys("languages/de.yml");

        Set<String> missingInGerman = new TreeSet<>(english);
        missingInGerman.removeAll(german);
        Set<String> missingInEnglish = new TreeSet<>(german);
        missingInEnglish.removeAll(english);

        assertEquals(Set.of(), missingInGerman, "keys only in en.yml");
        assertEquals(Set.of(), missingInEnglish, "keys only in de.yml");
    }

    @SuppressWarnings("unchecked")
    private static Set<String> keys(String resource) throws IOException {
        try (InputStream in = LanguageParityTest.class.getClassLoader().getResourceAsStream(resource)) {
            Set<String> keys = new TreeSet<>();
            collect("", (Map<Object, Object>) new Yaml().load(in), keys);
            return keys;
        }
    }

    @SuppressWarnings("unchecked")
    private static void collect(String prefix, Map<Object, Object> map, Set<String> out) {
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            String key = prefix + entry.getKey();
            if (entry.getValue() instanceof Map<?, ?> child) collect(key + ".", (Map<Object, Object>) child, out);
            else out.add(key);
        }
    }
}
