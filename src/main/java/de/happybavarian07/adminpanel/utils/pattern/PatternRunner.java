package de.happybavarian07.adminpanel.utils.pattern;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PatternRunner {
    public static void main(String[] args) throws Exception {
        File projectRoot = new File(System.getProperty("user.dir"));
        Path dataDir = projectRoot.toPath().resolve("data");
        Path patternsDir = dataDir.resolve("patterns");

        if (!Files.exists(dataDir)) {
            System.out.println("No data directory found at " + dataDir.toAbsolutePath());
            return;
        }

        if (!Files.exists(patternsDir)) {
            Files.createDirectories(patternsDir);
        }

        PatternGenerator generator = new PatternGenerator();

        Map<String, String> report = new HashMap<>();

        try (var stream = Files.list(dataDir)) {
            stream.forEach(p -> {
                try {
                    File f = p.toFile();
                    if (f.isDirectory()) return;
                    Path patternPath = patternsDir.resolve(f.getName() + ".pattern");
                    if (!Files.exists(patternPath)) {
                        try {
                            generator.generatePattern(f, patternPath);
                            report.put(f.getName(), "PATTERN_GENERATED");
                        } catch (Exception ge) {
                            report.put(f.getName(), "GEN_ERROR: " + ge.getMessage());
                            return;
                        }
                    }
                    PatternValidator.ValidationResult vr = PatternValidator.validateYaml(f, patternPath.toFile());
                    report.put(f.getName(), vr.summary());
                } catch (Exception e) {
                    report.put(p.getFileName().toString(), "ERROR: " + e.getMessage());
                }
            });
        }

        System.out.println("Pattern validation report:");
        for (Map.Entry<String, String> e : report.entrySet()) {
            System.out.println("- " + e.getKey() + ":\n" + e.getValue());
        }
    }
}
