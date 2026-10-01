package de.happybavarian07.adminpanel.data;

import de.happybavarian07.adminpanel.warnings.WarningStatus;
import de.happybavarian07.coolstufflib.jpa.annotations.Column;
import de.happybavarian07.coolstufflib.jpa.annotations.Entity;
import de.happybavarian07.coolstufflib.jpa.annotations.GeneratedValue;
import de.happybavarian07.coolstufflib.jpa.annotations.GenerationType;
import de.happybavarian07.coolstufflib.jpa.annotations.Id;
import de.happybavarian07.coolstufflib.jpa.annotations.Table;

import java.util.UUID;

/**
 * One warning of a player. Rows are never deleted: ending a warning only changes its {@link #getStatus() status},
 * so the history stays available.
 */
@Entity
@Table(name = "player_warnings")
public class PlayerWarning {
    /**
     * Issuer and ender value for actions taken from the console.
     */
    public static final String CONSOLE = "CONSOLE";
    /**
     * Ender value for warnings that ran out by themselves.
     */
    public static final String SYSTEM = "SYSTEM";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", primaryKey = true)
    private long id;

    @Column(name = "player_uuid")
    private String playerUuid;

    @Column(name = "reason")
    private volatile String reason;

    @Column(name = "issuer")
    private String issuer;

    @Column(name = "created_at")
    private long createdAt;

    @Column(name = "expires_at")
    private long expiresAt;

    @Column(name = "status")
    private volatile String status;

    @Column(name = "ended_at")
    private volatile long endedAt;

    @Column(name = "ended_by")
    private volatile String endedBy;

    public PlayerWarning() {
    }

    /**
     * Creates an active warning.
     *
     * @param playerUuid the warned player
     * @param reason     why the player was warned
     * @param issuer     the issuer's UUID text or {@link #CONSOLE}
     * @param createdAt  creation time in epoch milliseconds
     * @param expiresAt  expiry time in epoch milliseconds, {@code 0} if it never expires
     */
    public PlayerWarning(String playerUuid, String reason, String issuer, long createdAt, long expiresAt) {
        this(playerUuid, reason, issuer, createdAt, expiresAt, WarningStatus.ACTIVE, 0, "");
    }

    public PlayerWarning(String playerUuid, String reason, String issuer, long createdAt, long expiresAt,
                         WarningStatus status, long endedAt, String endedBy) {
        this.playerUuid = playerUuid;
        this.reason = reason;
        this.issuer = issuer;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = status.name();
        this.endedAt = endedAt;
        this.endedBy = endedBy;
    }

    public long getId() {
        return id;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public UUID playerId() {
        return UUID.fromString(playerUuid);
    }

    public String getReason() {
        return reason;
    }

    public String getIssuer() {
        return issuer;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public WarningStatus getStatus() {
        return WarningStatus.valueOf(status);
    }

    public long getEndedAt() {
        return endedAt;
    }

    public String getEndedBy() {
        return endedBy;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Whether the warning counts: it has the status {@code ACTIVE} and its expiry time has not passed yet.
     *
     * @param now the current time in epoch milliseconds
     */
    public boolean isActiveAt(long now) {
        return getStatus() == WarningStatus.ACTIVE && (expiresAt <= 0 || expiresAt > now);
    }

    /**
     * Ends the warning with the given status.
     *
     * @param newStatus {@code EXPIRED} or {@code REMOVED}
     * @param at        when it ended, in epoch milliseconds
     * @param by        who ended it: a UUID text, {@link #CONSOLE} or {@link #SYSTEM}
     */
    public void end(WarningStatus newStatus, long at, String by) {
        this.status = newStatus.name();
        this.endedAt = at;
        this.endedBy = by;
    }
}
