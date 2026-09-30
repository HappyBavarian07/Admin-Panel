package de.happybavarian07.adminpanel.utils.pattern;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PatternTool {
    public static void main(String[] args) throws Exception {
        File projectRoot = new File(System.getProperty("user.dir"));
        Path dataDir = projectRoot.toPath().resolve("data");
        File patternsDir = dataDir.resolve("patterns").toFile();
        PatternFileGenerator.ensurePatternsDir(patternsDir);

        File overridesFile = patternsDir.toPath().resolve("pattern-config.yml").toFile();
        Map<String, String> overrides = new HashMap<>();
        if (overridesFile.exists()) {
            Yaml yaml = new Yaml();
            try (FileInputStream fis = new FileInputStream(overridesFile)) {
                Object loaded = yaml.load(fis);
                if (loaded instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<Object, Object> m = (Map<Object, Object>) loaded;
                    for (Map.Entry<Object, Object> e : m.entrySet()) {
                        String k = String.valueOf(e.getKey());
                        Object v = e.getValue();
                        if (v != null) overrides.put(k, String.valueOf(v));
                    }
                }
            }
        }

        if (!Files.exists(dataDir)) {
            Logger.getLogger("Admin-Panel").warning("No data directory found at " + dataDir.toAbsolutePath());
            return;
        }

        try (var stream = Files.list(dataDir)) {
            stream.forEach(p -> {
                try {
                    File f = p.toFile();
                    if (f.isDirectory()) return;
                    String name = f.getName().toLowerCase();
                    File out = patternsDir.toPath().resolve(f.getName() + ".pattern").toFile();
                    if (name.endsWith(".yml") || name.endsWith(".yaml")) {
                        PatternFileGenerator.generateYamlPattern(f, out, overrides);
                        Logger.getLogger("Admin-Panel").info("Generated YAML pattern: " + out.getAbsolutePath());
                    } else if (name.endsWith(".properties")) {
                        PatternFileGenerator.generatePropertiesPattern(f, out, overrides);
                        Logger.getLogger("Admin-Panel").info("Generated properties pattern: " + out.getAbsolutePath());
                    } else {
                        Logger.getLogger("Admin-Panel").info("Skipping unsupported file type: " + f.getName());
                    }
                } catch (IOException e) {
                    AdminPanelMain.logError("Failed to generate pattern for " + p, e);
                }
            });
        }
    }
}
