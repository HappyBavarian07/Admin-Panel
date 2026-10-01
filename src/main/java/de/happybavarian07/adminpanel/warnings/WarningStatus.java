package de.happybavarian07.adminpanel.warnings;

public enum WarningStatus {
    ACTIVE,
    EXPIRED,
    REMOVED,
    /**
     * A history entry such as a ban or a kick. It never counts as active, never triggers escalation and never expires.
     */
    RECORD
}
