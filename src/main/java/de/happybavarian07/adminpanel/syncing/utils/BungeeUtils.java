package de.happybavarian07.adminpanel.syncing.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.bungee.IncomingChannelListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.messaging.Messenger;

public class BungeeUtils {
    private final AdminPanelMain plugin;
    private final Messenger messenger;
    private String incomingChannelName;
    private String outgoingChannelName;
    private final IncomingChannelListener channelListener;

    public BungeeUtils(String incomingChannelName, String outgoingChannelName) {
        this.incomingChannelName = incomingChannelName;
        this.outgoingChannelName = outgoingChannelName;
        if (incomingChannelName.equals("")) {
            this.incomingChannelName = "adminpanel:bungeein";
        }

        if (outgoingChannelName.equals("")) {
            this.outgoingChannelName = "adminpanel:bungeeout";
        }

        this.plugin = AdminPanelMain.getPlugin();
        this.channelListener = new IncomingChannelListener(incomingChannelName);
        this.messenger = Bukkit.getServer().getMessenger();
    }

    public void openBungeeChannel() {
        this.messenger.registerIncomingPluginChannel(this.plugin, this.incomingChannelName, this.channelListener);
        this.messenger.registerOutgoingPluginChannel(this.plugin, this.outgoingChannelName);
        System.out.println("Incoming: " + this.messenger.getIncomingChannels(this.plugin));
        System.out.println("Outgoing: " + this.messenger.getOutgoingChannels(this.plugin));
    }

    public void closeBungeeChannel() {
        System.out.println("Incoming: " + this.messenger.getIncomingChannels(this.plugin));
        System.out.println("Outgoing: " + this.messenger.getOutgoingChannels(this.plugin));
        this.messenger.unregisterIncomingPluginChannel(this.plugin, this.incomingChannelName, this.channelListener);
        this.messenger.unregisterOutgoingPluginChannel(this.plugin, this.outgoingChannelName);
    }

    public void sendDataToChannel(String action, String... args) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF(action);

        for (String s : args) {
            output.writeUTF(s);
        }

        try {
            Bukkit.getServer().sendPluginMessage(this.plugin, this.outgoingChannelName, output.toByteArray());
        } catch (Exception var8) {
            this.plugin.getStartUpLogger().coloredSpacer(ChatColor.RED);
            this.plugin.getStartUpLogger().message("&cThere was an Error while sending Data to the BungeeServer: &4" + var8);
            var8.printStackTrace();
            this.plugin.getStartUpLogger().coloredSpacer(ChatColor.RED);
        }

    }

    public String getIncomingChannelName() {
        return this.incomingChannelName;
    }

    public void setIncomingChannelName(String incomingChannelName) {
        this.incomingChannelName = incomingChannelName;
    }

    public String getOutgoingChannelName() {
        return this.outgoingChannelName;
    }

    public void setOutgoingChannelName(String outgoingChannelName) {
        this.outgoingChannelName = outgoingChannelName;
    }
}
    