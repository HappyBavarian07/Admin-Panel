package de.happybavarian07.adminpanel.worlds;

import java.util.List;
import java.util.Locale;

/**
 * Groups the gamerules for the filter button of the gamerule menu. The group comes from words in the name, not from a
 * list of names, so rules that newer versions add or rename (for example {@code doDaylightCycle} and
 * {@code advance_time}) still land in a fitting group. A rule without a known word is in {@link #OTHER}.
 */
public enum GameRuleCategory {
    MOBS, PLAYER, WORLD, DAMAGE, OTHER;

    private record Rule(GameRuleCategory category, List<String> words) {
    }

    // the first group with a matching word wins, so "spawnradius" and "respawn" are checked before "spawn"
    private static final List<Rule> RULES = List.of(
            new Rule(DAMAGE, List.of("damage")),
            new Rule(PLAYER, List.of("spawnradius", "respawn", "keepinventory", "advancement", "death", "sleeping", "spectator",
                    "elytra", "movementcheck", "crafting", "regeneration", "debuginfo")),
            new Rule(WORLD, List.of("spawnchunk")),
            new Rule(MOBS, List.of("mob", "spawn", "raid", "patrol", "warden", "trader", "phantom", "anger", "forgive",
                    "cramming", "insomnia")),
            new Rule(WORLD, List.of("time", "weather", "daylight", "fire", "tick", "drops", "snow", "source", "vines",
                    "explosion", "explode", "sound")));

    public static GameRuleCategory of(String ruleName) {
        String name = ruleName.toLowerCase(Locale.ROOT).replace("_", "");
        for (Rule rule : RULES) {
            for (String word : rule.words()) {
                if (name.contains(word)) return rule.category();
            }
        }
        return OTHER;
    }
}
