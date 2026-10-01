package de.happybavarian07.adminpanel.startup;

/**
 * A required startup stage failed; the plugin cannot run without it.
 */
public final class StartupAbortedException extends RuntimeException {
    private final String stage;

    public StartupAbortedException(String stage, String detail) {
        super("Required startup stage '" + stage + "' failed: " + detail);
        this.stage = stage;
    }

    public String stage() {
        return stage;
    }
}
