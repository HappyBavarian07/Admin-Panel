package de.happybavarian07.adminpanel.warnings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * The {@code Warnings} section of the config.
 *
 * @param defaultExpiryMillis how long a new warning lasts, {@code 0} if it never expires
 * @param rules               the valid escalation rules
 */
public record WarningSettings(long defaultExpiryMillis, List<EscalationRule> rules) {
    private static final long FALLBACK_EXPIRY_MILLIS = TimeUnit.DAYS.toMillis(30);

    /**
     * Reads the settings, skipping what is invalid instead of failing.
     *
     * @param defaultExpiry the value of {@code Warnings.DefaultExpiry}
     * @param rawRules      the value of {@code Warnings.Escalation}
     * @param log           receives a message for every invalid value
     */
    public static WarningSettings parse(String defaultExpiry, List<? extends Map<?, ?>> rawRules, Consumer<String> log) {
        long expiry;
        try {
            expiry = DurationParser.parse(defaultExpiry);
        } catch (IllegalArgumentException e) {
            log.accept("Invalid Warnings.DefaultExpiry, using 30d: " + e.getMessage());
            expiry = FALLBACK_EXPIRY_MILLIS;
        }
        List<EscalationRule> rules = new ArrayList<>();
        if (rawRules != null) {
            for (Map<?, ?> raw : rawRules) {
                EscalationRule rule = EscalationRule.parse(raw, log);
                if (rule != null) rules.add(rule);
            }
        }
        return new WarningSettings(expiry, List.copyOf(rules));
    }

    public List<EscalationRule> rulesFor(int activeCount) {
        return rules.stream().filter(rule -> rule.count() == activeCount).toList();
    }
}
