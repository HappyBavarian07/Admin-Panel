package de.happybavarian07.adminpanel.permissions.engine;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResolverTest {
    private static final long NOW = 1_000_000L;
    private final Map<String, GroupData> groups = new HashMap<>();

    private Function<String, GroupData> lookup() {
        return name -> groups.get(name);
    }

    private void group(String name, int weight, Node... nodes) {
        groups.put(name, GroupData.of(name, weight, nodes));
    }

    private Resolution resolve(Map<String, String> context, Node... userNodes) {
        return Resolver.resolve(List.of(userNodes), lookup(), context, NOW, true);
    }

    private Resolution resolve(Node... userNodes) {
        return resolve(Map.of(), userNodes);
    }

    @Test
    void theUsersOwnEntryBeatsAGroup() {
        group("admin", 10, Node.of("fly", false));
        Resolution result = resolve(Node.of("fly", true), Node.group("admin"));
        assertEquals(true, result.check("fly"));
    }

    @Test
    void aHeavierGroupBeatsALighterOne() {
        group("vip", 5, Node.of("home.limit.10", true));
        group("mod", 50, Node.of("home.limit.10", false));
        Resolution result = resolve(Node.group("vip"), Node.group("mod"));
        assertEquals(false, result.check("home.limit.10"));
        assertEquals(List.of("mod", "vip"), result.groups());
    }

    @Test
    void aGroupInheritsFromItsParentsAndTheChildWins() {
        group("member", 1, Node.of("chat", true), Node.of("build", false));
        group("builder", 2, Node.group("member"), Node.of("build", true));
        Resolution result = resolve(Node.group("builder"));
        assertEquals(true, result.check("chat"));
        assertEquals(true, result.check("build"));
        assertEquals(List.of("builder", "member"), result.groups());
    }

    @Test
    void theDefaultGroupAppliesToEveryoneAndComesLast() {
        group("default", 0, Node.of("spawn", true), Node.of("fly", false));
        group("vip", 5, Node.of("fly", true));
        Resolution result = resolve(Node.group("vip"));
        assertEquals(true, result.check("spawn"));
        assertEquals(true, result.check("fly"));
        assertEquals(List.of("vip", "default"), result.groups());
        assertEquals(true, resolve().check("spawn"));
    }

    @Test
    void aSpecificEntryBeatsAWildcardAndALongerWildcardBeatsAShorterOne() {
        Resolution result = resolve(Node.of("essentials.*", true), Node.of("essentials.fly", false), Node.of("*", false),
                Node.of("essentials.kits.*", false));
        assertEquals(false, result.check("essentials.fly"));
        assertEquals(false, result.check("essentials.kits.starter"));
        assertEquals(true, result.check("essentials.home"));
        assertEquals(false, result.check("other.thing"));
    }

    @Test
    void anUnknownPermissionHasNoAnswer() {
        assertNull(resolve(Node.of("a.b", true)).check("c.d"));
    }

    @Test
    void anExpiredEntryIsIgnoredAndATemporaryOneCountsUntilItEnds() {
        Resolution result = resolve(new Node("old", true, NOW - 1, Map.of()), new Node("temp", true, NOW + 1, Map.of()));
        assertNull(result.check("old"));
        assertEquals(true, result.check("temp"));
    }

    @Test
    void anEntryWithAContextOnlyAppliesThere() {
        Node nether = new Node("fly", true, 0, Map.of("world", "nether"));
        assertEquals(true, resolve(Map.of("world", "nether"), nether).check("fly"));
        assertNull(resolve(Map.of("world", "world"), nether).check("fly"));
        assertNull(resolve(nether).check("fly"));
    }

    @Test
    void aTemporaryMembershipEndsAndCyclesAreCutAndReported() {
        group("trial", 3, Node.of("trial.perk", true));
        Resolution ended = resolve(new Node("group.trial", true, NOW - 5, Map.of()));
        assertNull(ended.check("trial.perk"));
        assertTrue(ended.groups().stream().noneMatch("trial"::equals));

        group("a", 1, Node.group("b"));
        group("b", 1, Node.group("a"), Node.of("x", true));
        Resolution cyclic = resolve(Node.group("a"));
        assertEquals(true, cyclic.check("x"));
        assertFalse(cyclic.problems().isEmpty());
        assertTrue(cyclic.problems().get(0).startsWith("group cycle"));
    }

    @Test
    void aMissingParentIsReported() {
        group("child", 1, Node.group("ghost"));
        assertEquals(List.of("unknown group: ghost"), resolve(Node.group("child")).problems());
    }

    @Test
    void expandCoversKnownPermissionsThroughWildcardsAndKeepsNegations() {
        Resolution result = resolve(Node.of("plugin.*", true), Node.of("plugin.admin", false), Node.of("direct", true));
        Map<String, Boolean> flat = result.expand(List.of("plugin.use", "plugin.admin", "unrelated", "Plugin.Other"));
        assertEquals(true, flat.get("plugin.use"));
        assertEquals(false, flat.get("plugin.admin"));
        assertEquals(true, flat.get("plugin.other"));
        assertEquals(true, flat.get("direct"));
        assertFalse(flat.containsKey("unrelated"));
        assertFalse(flat.containsKey("plugin.*"));
    }

    @Test
    void explainListsTheDefinitionsInOrderAndMarksTheDecidingOne() {
        group("vip", 5, Node.of("fly", true));
        Resolution result = resolve(Node.of("fly", false), Node.group("vip"));
        List<Resolution.Step> steps = result.explain("fly");
        assertEquals(2, steps.size());
        assertEquals("user", steps.get(0).source());
        assertTrue(steps.get(0).decisive());
        assertEquals("vip", steps.get(1).source());
        assertFalse(steps.get(1).decisive());
    }

    @Test
    void prefixAndSuffixComeFromTheHighestPriority() {
        groups.put("admin", new GroupData("admin", "", 10, "[Admin] ", "", List.of()));
        groups.put("vip", new GroupData("vip", "", 5, "[VIP] ", " *", List.of()));
        Resolution result = resolve(Node.group("admin"), Node.group("vip"));
        assertEquals("[Admin] ", result.prefix());
        assertEquals(" *", result.suffix());
        Resolution own = resolve(Node.of("prefix.100.[Boss] ", true), Node.group("admin"));
        assertEquals("[Boss] ", own.prefix());
    }

    @Test
    void contextTextRoundTrips() {
        Node node = new Node("x", true, 0, Map.of("world", "nether", "gamemode", "creative"));
        assertEquals("gamemode=creative;world=nether", node.contextText());
        assertEquals(node.context(), Node.parseContext(node.contextText()));
    }
}
