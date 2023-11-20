package de.happybavarian07.adminpanel.syncing.managers;/*
 * @Author HappyBavarian07
 * @Date 02.10.2023 | 11:28
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class StatsManager {
    private final File statsFile;
    private final FileConfiguration statsConfig;

    // Excluding the Messages for connecting or disconnecting and any string messages, that aren't Packets
    private int totalPacketsSend;
    // Excluding the Messages for connecting or disconnecting and any string messages, that aren't Packets
    private int packetsSendThisSession;
    // Excluding the Messages for connecting or disconnecting and any string messages, that aren't Packets
    private int totalPacketsReceived;
    // Excluding the Messages for connecting or disconnecting and any string messages, that aren't Packets
    private int packetsReceivedThisSession;
    private int totalBytesSend;
    private int bytesSendThisSession;
    private int totalBytesReceived;
    private int bytesReceivedThisSession;
    private int totalErrors;
    private int errorsThisSession;
    // Represents the Number of Connections this Client had to the Server (How many times the Client connected to the Server)
    private int totalConnectionsToServer;
    private int connectionsToServerThisSession;
    private int totalBytesEncrypted;
    private int bytesEncryptedThisSession;
    private int totalBytesDecrypted;
    private int bytesDecryptedThisSession;

    public StatsManager(File statsFile) {
        this.statsFile = statsFile;
        if(!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                addErrorsThisSession(1);
                throw new RuntimeException(e);
            }
        }
        this.statsConfig = YamlConfiguration.loadConfiguration(statsFile);
    }

    public void loadStatsFromFile() {
        totalPacketsSend = statsConfig.getInt("Stats.TotalPacketsSend", 0);
        packetsSendThisSession = 0;
        totalPacketsReceived = statsConfig.getInt("Stats.TotalPacketsReceived", 0);
        packetsReceivedThisSession = 0;
        totalBytesSend = statsConfig.getInt("Stats.TotalBytesSend", 0);
        bytesSendThisSession = 0;
        totalBytesReceived = statsConfig.getInt("Stats.TotalBytesReceived", 0);
        bytesReceivedThisSession = 0;
        totalErrors = statsConfig.getInt("Stats.TotalErrors", 0);
        errorsThisSession = 0;
        totalConnectionsToServer = statsConfig.getInt("Stats.TotalConnectionsToServer", 0);
        connectionsToServerThisSession = 0;
        totalBytesEncrypted = statsConfig.getInt("Stats.TotalBytesEncrypted", 0);
        bytesEncryptedThisSession = 0;
        totalBytesDecrypted = statsConfig.getInt("Stats.TotalBytesDecrypted", 0);
        bytesDecryptedThisSession = 0;
    }

    public void saveStatsToFile() {
        statsConfig.set("Stats.TotalPacketsSend", totalPacketsSend + packetsSendThisSession);
        statsConfig.set("Stats.TotalPacketsReceived", totalPacketsReceived + packetsReceivedThisSession);
        statsConfig.set("Stats.TotalBytesSend", totalBytesSend + bytesSendThisSession);
        statsConfig.set("Stats.TotalBytesReceived", totalBytesReceived + bytesReceivedThisSession);
        statsConfig.set("Stats.TotalErrors", totalErrors + errorsThisSession);
        statsConfig.set("Stats.TotalConnectionsToServer", totalConnectionsToServer + connectionsToServerThisSession);
        statsConfig.set("Stats.TotalBytesEncrypted", totalBytesEncrypted + bytesEncryptedThisSession);
        statsConfig.set("Stats.TotalBytesDecrypted", totalBytesDecrypted + bytesDecryptedThisSession);

        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            addErrorsThisSession(1);
            throw new RuntimeException(e);
        }
    }

    public File getStatsFile() {
        return statsFile;
    }

    public FileConfiguration getStatsConfig() {
        return statsConfig;
    }

    public int getTotalPacketsSend() {
        return totalPacketsSend;
    }

    public int getPacketsSendThisSession() {
        return packetsSendThisSession;
    }

    public int getTotalPacketsReceived() {
        return totalPacketsReceived;
    }

    public int getPacketsReceivedThisSession() {
        return packetsReceivedThisSession;
    }

    public int getTotalBytesSend() {
        return totalBytesSend;
    }

    public int getBytesSendThisSession() {
        return bytesSendThisSession;
    }

    public int getTotalBytesReceived() {
        return totalBytesReceived;
    }

    public int getBytesReceivedThisSession() {
        return bytesReceivedThisSession;
    }

    public int getTotalErrors() {
        return totalErrors;
    }

    public int getErrorsThisSession() {
        return errorsThisSession;
    }

    public int getTotalConnectionsToServer() {
        return totalConnectionsToServer;
    }

    public int getConnectionsToServerThisSession() {
        return connectionsToServerThisSession;
    }

    public int getTotalBytesEncrypted() {
        return totalBytesEncrypted;
    }

    public int getBytesEncryptedThisSession() {
        return bytesEncryptedThisSession;
    }

    public int getTotalBytesDecrypted() {
        return totalBytesDecrypted;
    }

    public int getBytesDecryptedThisSession() {
        return bytesDecryptedThisSession;
    }

    public void setPacketsSendThisSession(int packetsSendThisSession) {
        this.packetsSendThisSession = packetsSendThisSession;
    }

    public void setPacketsReceivedThisSession(int packetsReceivedThisSession) {
        this.packetsReceivedThisSession = packetsReceivedThisSession;
    }

    public void setBytesSendThisSession(int bytesSendThisSession) {
        this.bytesSendThisSession = bytesSendThisSession;
    }

    public void setBytesReceivedThisSession(int bytesReceivedThisSession) {
        this.bytesReceivedThisSession = bytesReceivedThisSession;
    }

    public void setErrorsThisSession(int errorsThisSession) {
        this.errorsThisSession = errorsThisSession;
    }

    public void setConnectionsToServerThisSession(int connectionsToServerThisSession) {
        this.connectionsToServerThisSession = connectionsToServerThisSession;
    }

    public void setBytesEncryptedThisSession(int bytesEncryptedThisSession) {
        this.bytesEncryptedThisSession = bytesEncryptedThisSession;
    }

    public void setBytesDecryptedThisSession(int bytesDecryptedThisSession) {
        this.bytesDecryptedThisSession = bytesDecryptedThisSession;
    }

    public void addPacketsSendThisSession(int packetsSendThisSession) {
        this.packetsSendThisSession += packetsSendThisSession;
    }

    public void addPacketsReceivedThisSession(int packetsReceivedThisSession) {
        this.packetsReceivedThisSession += packetsReceivedThisSession;
    }

    public void addBytesSendThisSession(int bytesSendThisSession) {
        this.bytesSendThisSession += bytesSendThisSession;
    }

    public void addBytesReceivedThisSession(int bytesReceivedThisSession) {
        this.bytesReceivedThisSession += bytesReceivedThisSession;
    }

    public void addErrorsThisSession(int errorsThisSession) {
        this.errorsThisSession += errorsThisSession;
    }

    public void addConnectionsToServerThisSession(int connectionsToServerThisSession) {
        this.connectionsToServerThisSession += connectionsToServerThisSession;
    }

    public void addBytesEncryptedThisSession(int bytesEncryptedThisSession) {
        this.bytesEncryptedThisSession += bytesEncryptedThisSession;
    }

    public void addBytesDecryptedThisSession(int bytesDecryptedThisSession) {
        this.bytesDecryptedThisSession += bytesDecryptedThisSession;
    }
}