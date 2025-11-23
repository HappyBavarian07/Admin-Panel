package de.happybavarian07.adminpanel.utils.pattern;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class PatternValidator {
    public static final class ValidationResult {
        public final List<String> missingKeys = new ArrayList<>();
        public final List<String> unexpectedKeys = new ArrayList<>();
        public final List<String> mismatchedValues = new ArrayList<>();

        public boolean isValid() {
            return missingKeys.isEmpty() && unexpectedKeys.isEmpty() && mismatchedValues.isEmpty();
        }

        public String summary() {
            StringBuilder sb = new StringBuilder();
            sb.append("valid=").append(isValid()).append('\n');
            if (!missingKeys.isEmpty()) sb.append("missing:").append(missingKeys).append('\n');
            if (!unexpectedKeys.isEmpty()) sb.append("unexpected:").append(unexpectedKeys).append('\n');
            if (!mismatchedValues.isEmpty()) sb.append("mismatched:").append(mismatchedValues).append('\n');
            return sb.toString();
        }
    }

    public static ValidationResult validateYaml(File dataFile, File patternFile) throws IOException {
        Yaml yaml = new Yaml();
        Object data;
        Object pattern;
        try (FileInputStream fis = new FileInputStream(dataFile)) {
            data = yaml.load(fis);
        }
        try (FileInputStream fis = new FileInputStream(patternFile)) {
            pattern = yaml.load(fis);
        }
        ValidationResult res = new ValidationResult();
        compareRecursive("", data, pattern, res);
        return res;
    }

    private static void compareRecursive(String path, Object data, Object pattern, ValidationResult res) {
        if (pattern instanceof Map<?, ?> pmap) {
            if (!(data instanceof Map<?, ?> dmap)) {
                res.mismatchedValues.add(path + " expected map");
                return;
            }
            // match data keys to pattern keys allowing regex-like pattern keys
            for (Map.Entry<?, ?> pe : pmap.entrySet()) {
                String pkey = String.valueOf(pe.getKey());
                boolean matched = false;
                // first try exact
                if (dmap.containsKey(pkey)) {
                    compareRecursive(appendPath(path, pkey), dmap.get(pkey), pe.getValue(), res);
                    matched = true;
                } else {
                    // treat pattern key as regex if contains regex metachar
                    if (containsRegexMeta(pkey)) {
                        Pattern kp = Pattern.compile(pkey);
                        for (Object dk : dmap.keySet()) {
                            String dkStr = String.valueOf(dk);
                            if (kp.matcher(dkStr).matches()) {
                                compareRecursive(appendPath(path, dkStr), dmap.get(dk), pe.getValue(), res);
                                matched = true;
                            }
                        }
                    }
                }
                if (!matched) {
                    res.missingKeys.add(appendPath(path, pkey));
                }
            }
            // any dmap keys not matched by any pattern key are unexpected
            for (Object dk : dmap.keySet()) {
                String dkStr = String.valueOf(dk);
                boolean matched = false;
                if (pmap.containsKey(dkStr)) matched = true;
                else {
                    for (Object pk : pmap.keySet()) {
                        String pks = String.valueOf(pk);
                        if (containsRegexMeta(pks)) {
                            if (Pattern.compile(pks).matcher(dkStr).matches()) {
                                matched = true;
                                break;
                            }
                        }
                    }
                }
                if (!matched) res.unexpectedKeys.add(appendPath(path, dkStr));
            }
            return;
        }
        if (pattern instanceof List<?> plist) {
            if (!(data instanceof List<?> dlist)) {
                res.mismatchedValues.add(path + " expected list");
                return;
            }
            int min = Math.min(plist.size(), dlist.size());
            for (int i = 0; i < min; i++) {
                compareRecursive(path + "[" + i + "]", dlist.get(i), plist.get(i), res);
            }
            if (dlist.size() > plist.size()) {
                for (int i = plist.size(); i < dlist.size(); i++) res.unexpectedKeys.add(path + "[" + i + "]");
            }
            if (plist.size() > dlist.size()) {
                for (int i = dlist.size(); i < plist.size(); i++) res.missingKeys.add(path + "[" + i + "]");
            }
            return;
        }
        // pattern is a scalar, expect pattern to be string "REGEX:..." or literal
        String pat = String.valueOf(pattern);
        String dataStr = data == null ? "" : String.valueOf(data);
        if (pat.startsWith("REGEX:")) {
            String re = pat.substring("REGEX:".length());
            if (!Pattern.compile(re, Pattern.DOTALL).matcher(dataStr).matches()) {
                res.mismatchedValues.add(path + " value='" + dataStr + "' does not match " + re);
            }
        } else {
            if (!pat.equals(dataStr)) {
                res.mismatchedValues.add(path + " value='" + dataStr + "' does not equal expected '" + pat + "'");
            }
        }
    }

    private static boolean containsRegexMeta(String s) {
        if (s == null) return false;
        String meta = ".\\+*?[](){}|^$";
        for (int i = 0; i < s.length(); i++) {
            if (meta.indexOf(s.charAt(i)) >= 0) return true;
        }
        return false;
    }

    private static String appendPath(String parent, String key) {
        if (parent == null || parent.isEmpty()) return key;
        if (key == null || key.isEmpty()) return parent;
        return parent + "." + key;
    }

    private PatternValidator() {
    }
}
