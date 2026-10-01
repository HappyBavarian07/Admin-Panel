package de.happybavarian07.adminpanel.startup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Runs the startup stages of the plugin one by one and remembers how each went. A stage that throws is recorded as
 * failed; a failed required stage stops the startup with {@link StartupAbortedException}, a failed optional stage only
 * marks itself and the startup goes on.
 */
public final class Startup {
    public enum Status {
        OK, DISABLED, FAILED, SKIPPED
    }

    public record Result(String id, boolean required, Status status, String detail) {
    }

    @FunctionalInterface
    public interface Action {
        void run() throws Exception;
    }

    /**
     * Thrown by a stage that finds itself switched off on purpose: a config switch, or a plugin that is not installed.
     * The stage is recorded as {@link Status#DISABLED}, which is not a failure.
     */
    public static final class DisabledException extends RuntimeException {
        public DisabledException(String reason) {
            super(reason);
        }
    }

    private final Map<String, Result> results = new LinkedHashMap<>();
    private final BiConsumer<String, Throwable> errorLog;

    /**
     * @param errorLog receives a message and the cause of every failed stage
     */
    public Startup(BiConsumer<String, Throwable> errorLog) {
        this.errorLog = errorLog;
    }

    /**
     * @param dependsOn stages that must be OK; if one is not, the action does not run and the stage is skipped, or
     *                  disabled when the stage it needs is disabled
     * @return whether the stage is OK
     * @throws StartupAbortedException if a required stage failed or was skipped
     */
    public boolean run(String id, boolean required, Action action, String... dependsOn) {
        for (String dependency : dependsOn) {
            if (!isOk(dependency)) {
                Result needed = results.get(dependency);
                boolean off = needed != null && needed.status() == Status.DISABLED;
                return record(id, required, off ? Status.DISABLED : Status.SKIPPED, "needs " + dependency);
            }
        }
        try {
            action.run();
        } catch (DisabledException off) {
            return record(id, required, Status.DISABLED, off.getMessage());
        } catch (Exception | LinkageError failure) {
            errorLog.accept("Startup stage '" + id + "' failed", failure);
            String detail = failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage();
            return record(id, required, Status.FAILED, detail);
        }
        return record(id, required, Status.OK, "");
    }

    public boolean isOk(String id) {
        Result result = results.get(id);
        return result != null && result.status() == Status.OK;
    }

    public Result result(String id) {
        return results.get(id);
    }

    public List<Result> results() {
        return new ArrayList<>(results.values());
    }

    /**
     * @return whether a stage failed or was skipped; stages that are disabled on purpose do not count
     */
    public boolean hasFailures() {
        return results.values().stream().anyMatch(r -> r.status() == Status.FAILED || r.status() == Status.SKIPPED);
    }

    public String summary() {
        long ok = count(Status.OK);
        long disabled = count(Status.DISABLED);
        long broken = count(Status.FAILED) + count(Status.SKIPPED);
        StringBuilder text = new StringBuilder("Startup: ").append(ok).append(" ok, ").append(disabled).append(" disabled, ")
                .append(broken).append(" broken");
        for (Result result : results.values()) {
            if (result.status() == Status.FAILED || result.status() == Status.SKIPPED) {
                text.append(" [").append(result.id()).append(": ").append(result.detail()).append(']');
            }
        }
        return text.toString();
    }

    private long count(Status status) {
        return results.values().stream().filter(r -> r.status() == status).count();
    }

    private boolean record(String id, boolean required, Status status, String detail) {
        results.put(id, new Result(id, required, status, detail));
        if (required && (status == Status.FAILED || status == Status.SKIPPED)) {
            throw new StartupAbortedException(id, detail);
        }
        return status == Status.OK;
    }
}
