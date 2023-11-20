package de.happybavarian07.adminpanel.syncing;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.syncing.managers.ConnectionHandler;
import de.happybavarian07.adminpanel.syncing.managers.LoggingManager;
import de.happybavarian07.adminpanel.syncing.managers.PacketHandler;
import de.happybavarian07.adminpanel.syncing.managers.SettingsManager;
import de.happybavarian07.adminpanel.syncing.managers.StatsManager;
import de.happybavarian07.adminpanel.syncing.utils.Action;
import de.happybavarian07.adminpanel.syncing.utils.CheckConnectionRunnable;
import de.happybavarian07.adminpanel.syncing.utils.Packet;
import de.happybavarian07.adminpanel.utils.StartUpLogger;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.net.ssl.SSLSocketFactory;
import org.bukkit.scheduler.BukkitRunnable;

public class DataClient {
    private final StartUpLogger pluginLogger = AdminPanelMain.getPlugin().getStartUpLogger();
    private final SettingsManager settingsManager = new SettingsManager(new File(AdminPanelMain.getPlugin().getDataFolder(), "DataClientSettings.yml"));
    private final ThreadGroup dataClientThreadGroup = new ThreadGroup("Data Client Threads");
    private final ConnectionHandler connectionHandler;
    private final LoggingManager loggingManager;
    private final PacketHandler packetHandler;
    private final StatsManager statsManager = new StatsManager(new File(AdminPanelMain.getPlugin().getDataFolder(), "DataClientStats.yml"));
    private SSLSocketFactory sslSocketFactory;
    private BukkitRunnable checkConnectionRunnable;

    public DataClient(String IPAddress, int port, String clientName) throws IOException {
        this.statsManager.loadStatsFromFile();
        this.loggingManager = new LoggingManager(this.settingsManager);
        this.connectionHandler = new ConnectionHandler(IPAddress, port, clientName, this.dataClientThreadGroup, this.settingsManager, this.loggingManager, this.statsManager);
        this.packetHandler = new PacketHandler(this, this.connectionHandler, this.loggingManager, this.statsManager);
        this.connectionHandler.setPacketHandler(this.packetHandler);
        this.connectionHandler.connect();
        this.packetHandler.startPacketHandlingThread();
        this.packetHandler.requestClientListUpdate();
        if (this.getSettingsManager().isCheckConnection()) {
            this.checkConnectionRunnable = new CheckConnectionRunnable(this.packetHandler, this.connectionHandler, this.loggingManager, this.statsManager, this.pluginLogger, this.settingsManager);
            this.checkConnectionRunnable.runTaskTimer(AdminPanelMain.getPlugin(), this.getSettingsManager().getCheckConnectionTiming() * 20L, this.getSettingsManager().getCheckConnectionTiming() * 20L);
        }

    }

    public void startCheckConnectionRunnable() {
        if (this.checkConnectionRunnable == null) {
            this.checkConnectionRunnable = new CheckConnectionRunnable(this.packetHandler, this.connectionHandler, this.loggingManager, this.statsManager, this.pluginLogger, this.settingsManager);
        }

        try {
            this.checkConnectionRunnable.cancel();
        } catch (IllegalStateException var2) {
        }

        this.checkConnectionRunnable.runTaskTimer(AdminPanelMain.getPlugin(), this.getSettingsManager().getCheckConnectionTiming() * 20L, this.getSettingsManager().getCheckConnectionTiming() * 20L);
    }

    public void stopCheckConnectionRunnable() {
        try {
            this.checkConnectionRunnable.cancel();
        } catch (IllegalStateException var2) {
        }

    }

    public SettingsManager getSettingsManager() {
        return this.settingsManager;
    }

    public boolean isEnabled() {
        return this.connectionHandler.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        this.connectionHandler.setEnabled(enabled);
    }

    public Packet packetFromParams(String clientName, String destination, Action action, String... data) {
        return new Packet(clientName, destination, action, data);
    }

    public Packet packetFromParams(String clientName, String destination, Action action, List<String> data) {
        return new Packet(clientName, destination, action, data);
    }

    public StartUpLogger getPluginLogger() {
        return this.pluginLogger;
    }

    public List<String> getOtherConnectedClients() {
        return this.connectionHandler.getOtherConnectedClients();
    }

    public ConnectionHandler getConnectionHandler() {
        return this.connectionHandler;
    }

    public PacketHandler getPacketHandler() {
        return this.packetHandler;
    }

    public LoggingManager getLoggingManager() {
        return this.loggingManager;
    }

    public ThreadGroup getDataClientThreadGroup() {
        return this.dataClientThreadGroup;
    }

    public void disconnect(boolean notifyServer) {
        this.connectionHandler.disconnect(notifyServer);
    }

    public void connect() throws IOException {
        this.connectionHandler.connect();
    }

    public void reconnect(boolean notifyServer) throws IOException {
        this.connectionHandler.reconnect(notifyServer);
    }

    public BukkitRunnable getCheckConnectionRunnable() {
        return this.checkConnectionRunnable;
    }

    public StatsManager getStatsManager() {
        return this.statsManager;
    }
}
    