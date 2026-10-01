package de.happybavarian07.adminpanel.startup;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StartupTest {
    private final List<String> logged = new ArrayList<>();
    private final Startup startup = new Startup((message, cause) -> logged.add(message + ": " + cause.getMessage()));

    @Test
    void failedOptionalStageIsRecordedAndStartupGoesOn() {
        assertTrue(startup.run("core", true, () -> {
        }));
        assertFalse(startup.run("updater", false, () -> {
            throw new IllegalStateException("no network");
        }));
        assertTrue(startup.run("after", false, () -> {
        }));

        assertEquals(Startup.Status.FAILED, startup.result("updater").status());
        assertEquals("no network", startup.result("updater").detail());
        assertTrue(startup.hasFailures());
        assertEquals(1, logged.size());
    }

    @Test
    void failedRequiredStageAbortsAndStaysRecorded() {
        StartupAbortedException abort = assertThrows(StartupAbortedException.class, () -> startup.run("database", true, () -> {
            throw new java.io.IOException("locked");
        }));
        assertEquals("database", abort.stage());
        assertEquals(Startup.Status.FAILED, startup.result("database").status());
    }

    @Test
    void linkageErrorsAreCaughtToo() {
        assertFalse(startup.run("vault", false, () -> {
            throw new NoClassDefFoundError("net/milkbowl/vault/economy/Economy");
        }));
        assertEquals(Startup.Status.FAILED, startup.result("vault").status());
    }

    @Test
    void stageWithAFailedDependencySkipsWithoutRunning() {
        startup.run("warnings", false, () -> {
            throw new IllegalStateException("table");
        });
        boolean[] ran = {false};
        assertFalse(startup.run("sweep", false, () -> ran[0] = true, "warnings"));
        assertFalse(ran[0]);
        assertEquals(Startup.Status.SKIPPED, startup.result("sweep").status());
        assertEquals("needs warnings", startup.result("sweep").detail());
    }

    @Test
    void requiredStageWithAMissingDependencyAborts() {
        assertThrows(StartupAbortedException.class, () -> startup.run("commands", true, () -> {
        }, "never-ran"));
    }

    @Test
    void disabledStagesAreNotFailures() {
        startup.run("core", true, () -> {
        });
        startup.disabled("vault", "not installed");
        assertFalse(startup.hasFailures());
        assertFalse(startup.isOk("vault"));
        assertEquals("Startup: 1 ok, 1 disabled, 0 broken", startup.summary());
    }

    @Test
    void summaryNamesTheBrokenStages() {
        startup.run("bungee", false, () -> {
            throw new IllegalStateException("connection refused");
        });
        assertEquals("Startup: 0 ok, 0 disabled, 1 broken [bungee: connection refused]", startup.summary());
    }

    @Test
    void resultsKeepTheOrderOfTheStages() {
        startup.run("b", false, () -> {
        });
        startup.run("a", false, () -> {
        });
        assertEquals(List.of("b", "a"), startup.results().stream().map(Startup.Result::id).toList());
    }
}
