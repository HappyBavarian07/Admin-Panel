package de.happybavarian07.adminpanel.warnings;

import de.happybavarian07.adminpanel.data.PlayerWarning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarningManagerTest {
    private static final long DAY = TimeUnit.DAYS.toMillis(1);

    private final FakePlayerWarningRepository repository = new FakePlayerWarningRepository();
    private final AtomicLong now = new AtomicLong(1_000_000);
    private final List<EscalationRule> escalated = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private final UUID player = UUID.randomUUID();
    private final UUID moderator = UUID.randomUUID();
    private WarningSettings settings = new WarningSettings(30 * DAY, List.of());
    private EscalationExecutor escalation = (rule, target) -> escalated.add(rule);
    private WarningManager manager;

    @BeforeEach
    void setUp() {
        manager = newManager();
    }

    private WarningManager newManager() {
        return new WarningManager(repository, () -> settings, (rule, target) -> escalation.run(rule, target),
                Runnable::run, Runnable::run, now::get, (message, error) -> errors.add(message));
    }

    @Test
    void addStoresAnActiveWarningWithTheDefaultExpiry() {
        PlayerWarning warning = manager.add(player, "Spam", moderator).join();

        assertEquals("Spam", warning.getReason());
        assertEquals(moderator.toString(), warning.getIssuer());
        assertEquals(now.get() + 30 * DAY, warning.getExpiresAt());
        assertEquals(WarningStatus.ACTIVE, warning.getStatus());
        assertEquals(1, manager.number(warning));
        assertSame(warning, repository.rows.get(warning.getId()));
    }

    @Test
    void aMissingIssuerIsTheConsoleAndZeroDurationNeverExpires() {
        PlayerWarning warning = manager.add(player, "Spam", null, 0).join();

        assertEquals(PlayerWarning.CONSOLE, warning.getIssuer());
        assertEquals(0, warning.getExpiresAt());
        now.addAndGet(1000 * DAY);
        assertEquals(1, manager.activeCount(player));
    }

    @Test
    void numbersFollowTheCreationOrderOverAllStatuses() {
        PlayerWarning first = manager.add(player, "one", null).join();
        now.addAndGet(10);
        PlayerWarning second = manager.add(player, "two", null).join();
        manager.expireNow(first.getId(), moderator).join();

        assertEquals(1, manager.number(first));
        assertEquals(2, manager.number(second));
        assertSame(second, manager.byNumber(player, 2));
        assertNull(manager.byNumber(player, 3));
        assertNull(manager.byNumber(player, 0));
    }

    @Test
    void editReasonChangesTheReasonUnlessRemoved() {
        PlayerWarning warning = manager.add(player, "old", null).join();

        assertTrue(manager.editReason(warning.getId(), "new", moderator).join());
        assertEquals("new", repository.rows.get(warning.getId()).getReason());

        manager.remove(warning.getId(), moderator).join();
        assertFalse(manager.editReason(warning.getId(), "again", moderator).join());
        assertFalse(manager.editReason(9999, "unknown", moderator).join());
    }

    @Test
    void expireNowEndsOnlyActiveWarnings() {
        PlayerWarning warning = manager.add(player, "Spam", null).join();
        now.addAndGet(5);

        assertTrue(manager.expireNow(warning.getId(), moderator).join());
        assertEquals(WarningStatus.EXPIRED, warning.getStatus());
        assertEquals(now.get(), warning.getEndedAt());
        assertEquals(moderator.toString(), warning.getEndedBy());
        assertEquals(0, manager.activeCount(player));
        assertFalse(manager.expireNow(warning.getId(), moderator).join());
    }

    @Test
    void removeKeepsTheRowAndHidesItFromTheActiveList() {
        PlayerWarning warning = manager.add(player, "Spam", null).join();

        assertTrue(manager.remove(warning.getId(), null).join());

        assertEquals(WarningStatus.REMOVED, repository.rows.get(warning.getId()).getStatus());
        assertEquals(PlayerWarning.CONSOLE, warning.getEndedBy());
        assertEquals(1, manager.all(player).size());
        assertTrue(manager.active(player).isEmpty());
        assertFalse(manager.remove(warning.getId(), null).join());
    }

    @Test
    void expireAllEndsEveryActiveWarning() {
        manager.add(player, "one", null).join();
        manager.add(player, "two", null).join();
        PlayerWarning removed = manager.add(player, "three", null).join();
        manager.remove(removed.getId(), null).join();

        assertEquals(2, manager.expireAll(player, moderator).join());
        assertEquals(0, manager.activeCount(player));
        assertEquals(WarningStatus.REMOVED, removed.getStatus());
    }

    @Test
    void activeCountIgnoresWarningsPastTheirExpiryBeforeTheSweep() {
        manager.add(player, "short", null, DAY).join();
        manager.add(player, "long", null, 10 * DAY).join();

        now.addAndGet(2 * DAY);

        assertEquals(1, manager.activeCount(player));
        assertEquals("long", manager.active(player).get(0).getReason());
        assertEquals(2, manager.all(player).size());
    }

    @Test
    void sweepExpiresWarningsInTheDatabaseAndTheCache() {
        PlayerWarning cached = manager.add(player, "cached", null, DAY).join();
        PlayerWarning later = manager.add(player, "later", null, 10 * DAY).join();
        UUID other = UUID.randomUUID();
        PlayerWarning uncached = new PlayerWarning(other.toString(), "stored", PlayerWarning.CONSOLE, now.get(), now.get() + DAY);
        repository.save(uncached);
        long expiry = cached.getExpiresAt();

        now.addAndGet(2 * DAY);

        assertEquals(2, manager.sweepNow());
        assertEquals(WarningStatus.EXPIRED, cached.getStatus());
        assertEquals(PlayerWarning.SYSTEM, cached.getEndedBy());
        assertEquals(expiry, cached.getEndedAt());
        assertEquals(WarningStatus.EXPIRED, repository.rows.get(uncached.getId()).getStatus());
        assertEquals(WarningStatus.ACTIVE, later.getStatus());
        assertEquals(0, manager.sweepNow());
    }

    @Test
    void warningsAreLoadedOncePerPlayer() {
        repository.save(new PlayerWarning(player.toString(), "old", PlayerWarning.CONSOLE, 5, 0));

        assertEquals(1, manager.load(player).join().size());
        manager.all(player);
        manager.activeCount(player);
        manager.add(player, "new", null).join();

        assertEquals(1, repository.playerQueries);
        assertEquals(2, manager.all(player).size());
    }

    @Test
    void aRuleRunsWhenTheActiveCountAfterTheNewWarningEqualsItsCount() {
        EscalationRule rule = new EscalationRule(2, EscalationRule.Action.KICK, "", 0, "");
        settings = new WarningSettings(30 * DAY, List.of(rule));

        manager.add(player, "one", null).join();
        assertTrue(escalated.isEmpty());
        manager.add(player, "two", null).join();
        assertEquals(List.of(rule), escalated);
        manager.add(player, "three", null).join();
        assertEquals(1, escalated.size());
    }

    @Test
    void endedWarningsDoNotCountTowardsARule() {
        EscalationRule rule = new EscalationRule(2, EscalationRule.Action.KICK, "", 0, "");
        settings = new WarningSettings(30 * DAY, List.of(rule));

        manager.add(player, "one", null).join();
        manager.add(player, "two", null).join();
        manager.expireAll(player, null).join();
        manager.add(player, "three", null).join();
        assertEquals(1, escalated.size());
        manager.add(player, "four", null).join();
        assertEquals(2, escalated.size());
    }

    @Test
    void severalRulesWithTheSameCountAllRun() {
        EscalationRule kick = new EscalationRule(1, EscalationRule.Action.KICK, "", 0, "");
        EscalationRule command = new EscalationRule(1, EscalationRule.Action.COMMAND, "", 0, "say %player%");
        settings = new WarningSettings(30 * DAY, List.of(kick, command));

        manager.add(player, "one", null).join();

        assertEquals(List.of(kick, command), escalated);
    }

    @Test
    void aFailingRuleNeverBreaksAddingAWarning() {
        settings = new WarningSettings(30 * DAY, List.of(new EscalationRule(1, EscalationRule.Action.KICK, "", 0, "")));
        escalation = (rule, target) -> {
            throw new IllegalStateException("boom");
        };

        PlayerWarning warning = manager.add(player, "one", null).join();

        assertEquals(1, manager.number(warning));
        assertEquals(1, errors.size());
    }

    @Test
    void invalidRulesInTheConfigAreSkippedWhenAddingAWarning() {
        List<String> log = new ArrayList<>();
        settings = WarningSettings.parse("30d", List.of(
                java.util.Map.of("count", 1, "action", "EXPLODE"),
                java.util.Map.of("count", 1, "action", "KICK")), log::add);

        manager.add(player, "one", null).join();

        assertEquals(1, log.size());
        assertEquals(1, escalated.size());
        assertEquals(EscalationRule.Action.KICK, escalated.get(0).action());
    }

    @Test
    void recordStoresAHistoryEntryThatIsNeverActive() {
        PlayerWarning entry = manager.record(player, "Banned (7d): Hacking", moderator.toString()).join();

        assertEquals(WarningStatus.RECORD, entry.getStatus());
        assertEquals(moderator.toString(), entry.getIssuer());
        assertEquals(0, entry.getExpiresAt());
        assertFalse(entry.isActiveAt(now.get()));
        assertFalse(entry.isActiveAt(now.get() + 1000 * DAY));
        assertEquals(0, manager.activeCount(player));
        assertTrue(manager.active(player).isEmpty());
        assertSame(entry, repository.rows.get(entry.getId()));
    }

    @Test
    void recordNeverRunsAnEscalationRuleAndDoesNotCountTowardsOne() {
        EscalationRule rule = new EscalationRule(1, EscalationRule.Action.KICK, "", 0, "");
        settings = new WarningSettings(30 * DAY, List.of(rule));

        manager.record(player, "Kicked: Spam", PlayerWarning.CONSOLE).join();
        assertTrue(escalated.isEmpty());

        manager.add(player, "one", null).join();
        assertEquals(List.of(rule), escalated);
        assertEquals(1, manager.activeCount(player));
    }

    @Test
    void recordsAreListedAndIgnoredByEndingAndTheSweep() {
        PlayerWarning entry = manager.record(player, "Kicked: Spam", PlayerWarning.SYSTEM).join();
        manager.add(player, "short", null, DAY).join();
        now.addAndGet(100 * DAY);

        assertEquals(1, manager.sweepNow());
        assertEquals(WarningStatus.RECORD, entry.getStatus());
        assertFalse(manager.expireNow(entry.getId(), moderator).join());
        assertEquals(0, manager.expireAll(player, moderator).join());
        assertEquals(2, manager.all(player).size());
        assertEquals(List.of(entry), manager.current(player));
        assertEquals(1, manager.number(entry));
    }

    @Test
    void recordsAreLoadedFromTheDatabase() {
        repository.save(new PlayerWarning(player.toString(), "Banned: Hacking", PlayerWarning.CONSOLE, 5, 0,
                WarningStatus.RECORD, 0, ""));

        List<PlayerWarning> loaded = manager.load(player).join();

        assertEquals(1, loaded.size());
        assertEquals(WarningStatus.RECORD, loaded.get(0).getStatus());
        assertEquals(0, manager.activeCount(player));
    }

    @Test
    void aRecordCanBeRemovedLikeAWarning() {
        PlayerWarning entry = manager.record(player, "Kicked: Spam", PlayerWarning.CONSOLE).join();

        assertTrue(manager.remove(entry.getId(), moderator).join());
        assertEquals(WarningStatus.REMOVED, entry.getStatus());
        assertTrue(manager.current(player).isEmpty());
    }

    @Test
    void theDefaultExpiryComesFromTheCurrentSettings() {
        settings = new WarningSettings(DAY, List.of());
        PlayerWarning warning = manager.add(player, "one", null).join();

        assertEquals(now.get() + DAY, warning.getExpiresAt());
        assertEquals(DAY, manager.defaultExpiryMillis());
    }
}
