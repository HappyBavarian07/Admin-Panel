package de.happybavarian07.adminpanel.bungee;/*
 * @Author HappyBavarian07
 * @Date 02.09.2022 | 15:03
 */

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class IncomingChannelListener implements PluginMessageListener {
    private final String incomingChannelName;

    public IncomingChannelListener(String incomingChannelName) {
        this.incomingChannelName = incomingChannelName;
    }
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if(!channel.equals(incomingChannelName)) return;

        ByteArrayDataInput input = ByteStreams.newDataInput(message);
        String subchannel /*First Argument in the Input*/ = input.readUTF();

        System.out.println("Received Plugin Message");

        if(subchannel.equals("BroadcastServerWide")) {
            List<String> args = new ArrayList<>();

            while (!input.readUTF().equals("")) {
                args.add(input.readUTF());
            }

            String messageToSend = Utils.arrayToString(args.toArray(new String[]{}));
            System.out.println("Received Broadcast Command!" + " Message: " + messageToSend);
            Bukkit.broadcastMessage(Utils.chat("&f|&9BROADCAST&f| &r" + messageToSend));
        }
    }
}
