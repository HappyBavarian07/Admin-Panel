package de.happybavarian07.adminpanel.utils;/*
 * @Author HappyBavarian07
 * @Date 13.11.2022 | 12:09
 */

import java.util.UUID;

public class Warning {
    private final UUID player;
    private String reason;
    private long expirationDate;
    private long creationDate;
    // Count of the Warning in the List of Warnings the Player has received
    // Diese Verwarnung wird dann aus der Anzahl genommen sobald sie abläuft
    private int warningCount;

    public Warning(UUID player, String reason, long expirationDate, long creationDate, int warningCount) {
        this.player = player;
        this.reason = reason;
        // never = Für immer (Einziger Weg zur Entfernung des Warnings ist über Commands/Configediting)
        this.expirationDate = expirationDate;
        this.creationDate = creationDate;
        this.warningCount = warningCount;
    }

    public UUID getPlayer() {
        return player;
    }

    public String getReason() {
        if(reason == null) return "<Reason>";
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    @Override
    public String toString() {
        return "Warning{" +
                "player=" + player +
                ", reason='" + reason + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", warningCount=" + warningCount +
                '}';
    }
}
