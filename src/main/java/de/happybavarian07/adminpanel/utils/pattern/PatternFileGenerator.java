package de.happybavarian07.adminpanel.utils.pattern;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public final class PatternFileGenerator {
    private static final Pattern UUID_LIKE = Pattern.compile("^[0-9a-fA-F\\-]{36}$");
    private static final Pattern VERSION_LIKE = Pattern.compile("^\\d+(?:\\.\\d+)*(?:[-_].+)?$");

    public interface PatternDetector {
        String detect(String path, Object value);
    }

    public static final class DefaultHeuristicDetector implements PatternDetector {
        @Override
        public String detect(String path, Object value) {
            if (value == null) return "REGEX:.*";
            if (value instanceof Number) return "REGEX:^[0-9]+$";
            if (value instanceof Boolean) return "REGEX:^(?:true|false)$";
            if (value instanceof Map || value instanceof List) return null;
            String s = String.valueOf(value).trim();
            if (UUID_LIKE.matcher(s).matches()) return "REGEX:^[0-9a-fA-F\\-]{36}$";
            if (VERSION_LIKE.matcher(s).matches()) return "REGEX:^\\d+(?:\\.\\d+)*(?:[-_].+)?$";
            if (s.matches("^[0-9]+$")) return "REGEX:^[0-9]+$";
            if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false")) return "REGEX:^(?:true|false)$";
            if (s.endsWith(".jar") || s.contains("%version%")) return "REGEX:^.+$";
            return "REGEX:.*";
        }
    }

    public static final class MapBasedDetector implements PatternDetector {
        private final Map<Pattern, String> rules;

        public MapBasedDetector(Map<String, String> overrides) {
            rules = new LinkedHashMap<>();
            if (overrides != null) {
                for (Map.Entry<String, String> e : overrides.entrySet()) {
                    Pattern p = Pattern.compile(e.getKey());
                    rules.put(p, e.getValue());
                }
            }
        }

        @Override
        public String detect(String path, Object value) {
            for (Map.Entry<Pattern, String> e : rules.entrySet()) {
                if (e.getKey().matcher(path).matches()) return e.getValue();
            }
            return null;
        }
    }

    public static void generateYamlPattern(File input, File output) throws IOException {
        generateYamlPattern(input, output, new DefaultHeuristicDetector());
    }

    public static void generateYamlPattern(File input, File output, Map<String, String> overrides) throws IOException {
        PatternDetector detector = new MapBasedDetector(overrides);
        PatternDetector combined = (path, value) -> {
            String r = detector.detect(path, value);
            if (r != null) return r;
            return new DefaultHeuristicDetector().detect(path, value);
        };
        generateYamlPattern(input, output, combined);
    }

    public static void generateYamlPattern(File input, File output, PatternDetector detector) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream is = new FileInputStream(input)) {
            Object loaded = yaml.load(is);
            Object pattern = toPatternObject(loaded, "", detector);
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml outYaml = new Yaml(options);
            try (FileWriter writer = new FileWriter(output, StandardCharsets.UTF_8)) {
                outYaml.dump(pattern, writer);
            }
        }
    }

    public static void generatePropertiesPattern(File input, File output) throws IOException {
        generatePropertiesPattern(input, output, new DefaultHeuristicDetector());
    }

    public static void generatePropertiesPattern(File input, File output, Map<String, String> overrides) throws IOException {
        PatternDetector detector = new MapBasedDetector(overrides);
        PatternDetector combined = (path, value) -> {
            String r = detector.detect(path, value);
            if (r != null) return r;
            return new DefaultHeuristicDetector().detect(path, value);
        };
        generatePropertiesPattern(input, output, combined);
    }

    public static void generatePropertiesPattern(File input, File output, PatternDetector detector) throws IOException {
        Properties p = new Properties();
        try (InputStream is = new FileInputStream(input)) {
            p.load(is);
        }
        Properties out = new Properties();
        for (String name : p.stringPropertyNames()) {
            String val = p.getProperty(name);
            String pat = detector.detect(name, val);
            if (pat == null) pat = "REGEX:.*";
            out.setProperty(name, pat);
        }
        try (FileOutputStream fos = new FileOutputStream(output)) {
            out.store(fos, "Generated pattern file");
        }
    }

    private static Object toPatternObject(Object value, String path, PatternDetector detector) {
        if (value instanceof Map<?, ?> m) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : m.entrySet()) {
                String key = String.valueOf(e.getKey());
                String childPath = path.isEmpty() ? key : path + "." + key;
                out.put(key, toPatternObject(e.getValue(), childPath, detector));
            }
            return out;
        }
        if (value instanceof List<?> l) {
            List<Object> out = new ArrayList<>();
            for (int i = 0; i < l.size(); i++) {
                Object o = l.get(i);
                String childPath = path + "[" + i + "]";
                out.add(toPatternObject(o, childPath, detector));
            }
            return out;
        }
        String d = detector.detect(path, value);
        if (d != null) return d;
        if (value instanceof Number) return "REGEX:^[0-9]+$";
        if (value instanceof Boolean) return "REGEX:^(?:true|false)$";
        if (value instanceof String) return inferPatternForString((String) value);
        return "REGEX:.*";
    }

    private static String inferPatternForString(String s) {
        if (s == null) return "REGEX:.*";
        String trimmed = s.trim();
        if (UUID_LIKE.matcher(trimmed).matches()) return "REGEX:^[0-9a-fA-F\\-]{36}$";
        if (VERSION_LIKE.matcher(trimmed).matches()) return "REGEX:^\\d+(?:\\.\\d+)*(?:[-_].+)?$";
        if (trimmed.matches("^[0-9]+$")) return "REGEX:^[0-9]+$";
        if (trimmed.equalsIgnoreCase("true") || trimmed.equalsIgnoreCase("false")) return "REGEX:^(?:true|false)$";
        if (trimmed.endsWith(".jar") || trimmed.contains("%version%")) return "REGEX:^.+$";
        return "REGEX:.*";
    }

    public static void ensurePatternsDir(File base) throws IOException {
        Path p = base.toPath();
        if (!Files.exists(p)) Files.createDirectories(p);
    }

    private PatternFileGenerator() {
    }
}
