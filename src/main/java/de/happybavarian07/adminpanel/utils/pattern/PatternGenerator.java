package de.happybavarian07.adminpanel.utils.pattern;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PatternGenerator {
    @FunctionalInterface
    public interface TokenAnalyzer {
        String analyze(String token);
    }

    private final List<TokenAnalyzer> analyzers = new ArrayList<>();

    public PatternGenerator() {
        analyzers.add(this::numberAnalyzer);
        analyzers.add(this::uuidLikeAnalyzer);
        analyzers.add(this::wordAnalyzer);
    }

    public void registerAnalyzer(TokenAnalyzer analyzer) {
        analyzers.add(0, analyzer);
    }

    public Path generatePattern(File dataFile, Path outputPattern) throws IOException {
        List<String> lines = Files.readAllLines(dataFile.toPath());
        StringBuilder sb = new StringBuilder();
        sb.append("file: ").append(dataFile.getName()).append("\n");
        sb.append("pattern:\n");
        for (String line : lines) {
            if (line.isEmpty()) {
                sb.append("  - []\n");
                continue;
            }
            String[] tokens = line.split("\\s+");
            sb.append("  - [");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i];
                String type = analyzeToken(token);
                sb.append("\"").append(escape(token)).append("\":").append(type);
                if (i < tokens.length - 1) sb.append(", ");
            }
            sb.append("]\n");
        }
        if (Files.notExists(outputPattern.getParent())) Files.createDirectories(outputPattern.getParent());
        Files.writeString(outputPattern, sb.toString());
        return outputPattern;
    }

    private String analyzeToken(String token) {
        for (TokenAnalyzer a : analyzers) {
            String t = a.analyze(token);
            if (t != null) return t;
        }
        return "RAW";
    }

    private String numberAnalyzer(String token) {
        int len = token.length();
        boolean allDigits = len > 0;
        int digits = 0;
        int dots = 0;
        for (char c : token.toCharArray()) {
            if (Character.isDigit(c)) digits++;
            else if (c == '.' || c == ',') dots++;
            else allDigits = false;
        }
        if (allDigits && digits >= 1) {
            return (dots > 0) ? "FLOAT" : "INT";
        }
        return null;
    }

    private String uuidLikeAnalyzer(String token) {
        if (token.length() >= 32 && token.length() <= 36 && token.chars().filter(ch -> ch == '-').count() <= 4)
            return "UUID";
        return null;
    }

    private String wordAnalyzer(String token) {
        boolean hasLetter = false;
        for (char c : token.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
                break;
            }
        }
        if (hasLetter) return "WORD";
        return null;
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

