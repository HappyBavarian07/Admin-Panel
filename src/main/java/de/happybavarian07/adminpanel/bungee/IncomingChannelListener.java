package de.happybavarian07.adminpanel.bungee;/*
 * @Author HappyBavarian07
 * @Date 02.09.2022 | 15:03
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.adminpanel.utils.LogPrefixExtension;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public record IncomingChannelListener(String incomingChannelName) implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(incomingChannelName)) return;

        DataInputStream input = new DataInputStream(new ByteArrayInputStream(message));
        String subchannel;
        try {
            subchannel = input.readUTF();
        } catch (IOException e) {
            AdminPanelMain.logError("Could not read the subchannel of a plugin message", e);
            return;
        }

        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.FINE, "Received Plugin Message", LogPrefixExtension.ADMINPANEL_MAIN, false);

        if (subchannel.equals("BroadcastServerWide")) {
            List<String> args = new ArrayList<>();
            try {
                String temp;
                while (!(temp = input.readUTF()).equals("")) {
                    args.add(temp);
                }
            } catch (IOException e) {
                AdminPanelMain.logError("Could not read the arguments of a broadcast plugin message", e);
                return;
            }

            String messageToSend = AdminPanelUtils.arrayToString(args.toArray(new String[0]));
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, "Received Broadcast Command! Message: " + messageToSend, LogPrefixExtension.ADMINPANEL_MAIN, true);
            Bukkit.broadcastMessage(AdminPanelUtils.chat("&f|&9BROADCAST&f| &r" + messageToSend));
        }
    }
}
