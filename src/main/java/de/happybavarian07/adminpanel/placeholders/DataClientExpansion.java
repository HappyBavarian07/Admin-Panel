package de.happybavarian07.adminpanel.placeholders;/*
 * @Author HappyBavarian07
 * @Date 02.10.2023 | 11:24
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.syncing.DataClient;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DataClientExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getName() {
        return "Data Client Expansion";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "apdataclient";
    }

    @Override
    public @NotNull String getAuthor() {
        return "HappyBavarian07";
    }

    @Override
    public @NotNull String getVersion() {
        return AdminPanelMain.getPlugin().getDescription().getVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        DataClient dataClient = AdminPanelMain.getPlugin().getDataClient();

        if (dataClient == null) {
            return null;
        }

        switch (params) {
            case "stats_total_packets_send":
                return String.valueOf(dataClient.getStatsManager().getTotalPacketsSend());
            case "stats_total_packets_send_this_session":
                return String.valueOf(dataClient.getStatsManager().getPacketsSendThisSession());
            case "stats_total_packets_received":
                return String.valueOf(dataClient.getStatsManager().getTotalPacketsReceived());
            case "stats_total_packets_received_this_session":
                return String.valueOf(dataClient.getStatsManager().getPacketsReceivedThisSession());
            case "stats_total_errors":
                return String.valueOf(dataClient.getStatsManager().getTotalErrors());
            case "stats_total_errors_this_session":
                return String.valueOf(dataClient.getStatsManager().getErrorsThisSession());
            case "stats_total_connections_to_server":
                return String.valueOf(dataClient.getStatsManager().getTotalConnectionsToServer());
            case "stats_total_connections_to_server_this_session":
                return String.valueOf(dataClient.getStatsManager().getConnectionsToServerThisSession());
            case "stats_total_bytes_send":
                return String.valueOf(dataClient.getStatsManager().getTotalBytesSend());
            case "stats_total_bytes_send_this_session":
                return String.valueOf(dataClient.getStatsManager().getBytesSendThisSession());
            case "stats_total_bytes_received":
                return String.valueOf(dataClient.getStatsManager().getTotalBytesReceived());
            case "stats_total_bytes_received_this_session":
                return String.valueOf(dataClient.getStatsManager().getBytesReceivedThisSession());
            case "auto_check_connection_timing":
                return String.valueOf(dataClient.getSettingsManager().getCheckConnectionTiming());
            case "auto_check_connection":
                return String.valueOf(dataClient.getSettingsManager().isCheckConnection());
            case "file_logging_prefix":
                return String.valueOf(dataClient.getSettingsManager().getFileLoggingPrefix());
        }
        return null;
    }
}
