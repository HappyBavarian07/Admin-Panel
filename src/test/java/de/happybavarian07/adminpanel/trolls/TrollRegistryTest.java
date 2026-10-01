package de.happybavarian07.adminpanel.trolls;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrollRegistryTest {
    private final Set<String> disabled = new HashSet<>();
    private final List<String> gateCalls = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private boolean gateAllows = true;
    private final List<Player> online = new ArrayList<>();
    private final TrollRegistry registry = new TrollRegistry(id -> !disabled.contains(id),
            (troll, staff, target, starting) -> {
                gateCalls.add(troll.id() + ":" + (starting ? "start" : "stop"));
                return gateAllows;
            }, () -> online, (message, error) -> errors.add(message));

    private static Player player() {
        UUID id = UUID.randomUUID();
        return (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class}, (proxy, method, args) -> {
            if (method.getName().equals("getUniqueId")) return id;
            if (method.getName().equals("hashCode")) return id.hashCode();
            if (method.getName().equals("equals")) return proxy == args[0];
            if (method.getName().equals("toString")) return "Player[" + id + "]";
            throw new UnsupportedOperationException(method.getName());
        });
    }

    private static class FakeToggle implements Troll {
        final String id;
        final Set<UUID> active = new HashSet<>();
        int starts;
        int stops;
        boolean failOnStop;

        FakeToggle(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public Kind kind() {
            return Kind.TOGGLE;
        }

        @Override
        public boolean isActive(UUID target) {
            return active.contains(target);
        }

        @Override
        public void start(Player staff, Player target) {
            starts++;
            active.add(target.getUniqueId());
        }

        @Override
        public void stop(Player target) {
            stops++;
            if (failOnStop) throw new IllegalStateException("boom");
            active.remove(target.getUniqueId());
        }
    }

    private static class FakeOneShot implements Troll {
        final String id;
        final Set<UUID> pending = new HashSet<>();
        int starts;

        FakeOneShot(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public Kind kind() {
            return Kind.ONE_SHOT;
        }

        @Override
        public boolean isActive(UUID target) {
            return pending.contains(target);
        }

        @Override
        public void start(Player staff, Player target) {
            starts++;
            pending.add(target.getUniqueId());
        }

        @Override
        public void stop(Player target) {
            pending.remove(target.getUniqueId());
        }
    }

    @Test
    void permissionFollowsTheIdByDefault() {
        assertEquals("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.NoJump", new FakeToggle("NoJump").permission());
    }

    @Test
    void startsAndStopsAToggle() {
        FakeToggle slow = new FakeToggle("Slow");
        registry.register(slow);
        Player staff = player();
        Player target = player();

        assertEquals(TrollRegistry.Result.STARTED, registry.start("Slow", staff, target));
        assertTrue(slow.isActive(target.getUniqueId()));
        assertEquals(TrollRegistry.Result.ALREADY_ACTIVE, registry.start("Slow", staff, target));
        assertEquals(1, slow.starts);

        assertEquals(TrollRegistry.Result.STOPPED, registry.stop("Slow", staff, target));
        assertFalse(slow.isActive(target.getUniqueId()));
        assertEquals(TrollRegistry.Result.NOT_ACTIVE, registry.stop("Slow", staff, target));
        assertEquals(List.of("Slow:start", "Slow:stop"), gateCalls);
    }

    @Test
    void toggleSwitchesBetweenStartAndStop() {
        FakeToggle slow = new FakeToggle("Slow");
        registry.register(slow);
        Player target = player();

        assertEquals(TrollRegistry.Result.STARTED, registry.toggle("Slow", null, target));
        assertEquals(TrollRegistry.Result.STOPPED, registry.toggle("Slow", null, target));
        assertEquals(TrollRegistry.Result.STARTED, registry.toggle("Slow", null, target));
    }

    @Test
    void toggleOnAOneShotAlwaysStartsAgain() {
        FakeOneShot hop = new FakeOneShot("Hop");
        registry.register(hop);
        Player target = player();

        assertEquals(TrollRegistry.Result.STARTED, registry.toggle("Hop", null, target));
        assertEquals(TrollRegistry.Result.STARTED, registry.toggle("Hop", null, target));
        assertEquals(2, hop.starts);
    }

    @Test
    void idsAreFoundIgnoringCaseAndUnknownOnesAreReported() {
        registry.register(new FakeToggle("NoJump"));

        assertTrue(registry.find("nojump").isPresent());
        assertEquals(TrollRegistry.Result.UNKNOWN, registry.start("Nope", null, player()));
        assertEquals(TrollRegistry.Result.UNKNOWN, registry.stop("Nope", null, player()));
        assertEquals(TrollRegistry.Result.UNKNOWN, registry.toggle("Nope", null, player()));
    }

    @Test
    void rejectsADuplicateId() {
        registry.register(new FakeToggle("NoJump"));

        assertThrows(IllegalArgumentException.class, () -> registry.register(new FakeOneShot("nojump")));
    }

    @Test
    void aVetoedStartOrStopChangesNothing() {
        FakeToggle slow = new FakeToggle("Slow");
        registry.register(slow);
        Player target = player();

        gateAllows = false;
        assertEquals(TrollRegistry.Result.CANCELLED, registry.start("Slow", null, target));
        assertEquals(0, slow.starts);

        gateAllows = true;
        registry.start("Slow", null, target);
        gateAllows = false;
        assertEquals(TrollRegistry.Result.CANCELLED, registry.stop("Slow", null, target));
        assertTrue(slow.isActive(target.getUniqueId()));
    }

    @Test
    void aTrollSwitchedOffInTheConfigIsHiddenAndRefusedButStillStoppable() {
        FakeToggle slow = new FakeToggle("Slow");
        FakeToggle jump = new FakeToggle("NoJump");
        registry.register(slow);
        registry.register(jump);
        Player target = player();
        registry.start("Slow", null, target);

        disabled.add("Slow");

        assertEquals(List.of(jump), registry.enabled());
        assertEquals(List.of(slow, jump), registry.all());
        assertEquals(TrollRegistry.Result.DISABLED, registry.start("Slow", null, player()));
        assertEquals(TrollRegistry.Result.STOPPED, registry.stop("Slow", null, target));
    }

    @Test
    void stopAllStopsTogglesAndPendingOneShotsOfOnlyThatTarget() {
        FakeToggle slow = new FakeToggle("Slow");
        FakeOneShot tnt = new FakeOneShot("Tnt");
        FakeOneShot hiss = new FakeOneShot("Hiss");
        registry.register(slow);
        registry.register(tnt);
        registry.register(hiss);
        Player target = player();
        Player other = player();
        registry.start("Slow", null, target);
        registry.start("Tnt", null, target);
        registry.start("Slow", null, other);
        tnt.pending.add(other.getUniqueId());

        assertEquals(List.of(slow, tnt), registry.active(target.getUniqueId()));
        assertEquals(2, registry.stopAll(target));

        assertTrue(registry.active(target.getUniqueId()).isEmpty());
        assertTrue(slow.isActive(other.getUniqueId()));
        assertTrue(tnt.isActive(other.getUniqueId()));
        assertEquals(List.of("Slow:start", "Tnt:start", "Slow:start"), gateCalls);
    }

    @Test
    void stopAllCannotBeVetoedAndKeepsGoingWhenOneTrollFails() {
        FakeToggle broken = new FakeToggle("Broken");
        FakeToggle slow = new FakeToggle("Slow");
        registry.register(broken);
        registry.register(slow);
        Player target = player();
        registry.start("Broken", null, target);
        registry.start("Slow", null, target);
        broken.failOnStop = true;
        gateAllows = false;

        assertEquals(1, registry.stopAll(target));

        assertFalse(slow.isActive(target.getUniqueId()));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("Broken"));
    }

    @Test
    void quitCleanupIsStopAllOfThePlayerWhoLeft() {
        FakeToggle slow = new FakeToggle("Slow");
        registry.register(slow);
        Player leaving = player();
        Player staying = player();
        registry.start("Slow", null, leaving);
        registry.start("Slow", null, staying);

        registry.stopAll(leaving);

        assertFalse(slow.isActive(leaving.getUniqueId()));
        assertTrue(slow.isActive(staying.getUniqueId()));
    }

    @Test
    void stopEverythingReachesEveryOnlinePlayer() {
        FakeToggle slow = new FakeToggle("Slow");
        FakeOneShot tnt = new FakeOneShot("Tnt");
        registry.register(slow);
        registry.register(tnt);
        Player first = player();
        Player second = player();
        online.add(first);
        online.add(second);
        registry.start("Slow", null, first);
        registry.start("Tnt", null, second);

        assertEquals(2, registry.stopEverything());

        assertTrue(registry.active(first.getUniqueId()).isEmpty());
        assertTrue(registry.active(second.getUniqueId()).isEmpty());
    }
}
