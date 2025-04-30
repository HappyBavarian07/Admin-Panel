package de.happybavarian07.adminpanel.syncing.utils;

import de.happybavarian07.adminpanel.bungee.IncomingChannelListener;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.messaging.Messenger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BungeeUtils {
    private final AdminPanelMain plugin;
    private final Messenger messenger;
    private String incomingChannelName;
    private String outgoingChannelName;
    private final IncomingChannelListener channelListener;

    public BungeeUtils(String incomingChannelName, String outgoingChannelName) {
        this.incomingChannelName = incomingChannelName;
        this.outgoingChannelName = outgoingChannelName;
        if (incomingChannelName.isEmpty()) {
            this.incomingChannelName = "adminpanel:bungeein";
        }

        if (outgoingChannelName.isEmpty()) {
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeUTF(action);
            for (String s : args) {
                dos.writeUTF(s);
            }
            dos.flush();
            Bukkit.getServer().sendPluginMessage(this.plugin, this.outgoingChannelName, baos.toByteArray());
        } catch (IOException e) {
            this.plugin.getStartUpLogger().coloredSpacer(ChatColor.RED);
            this.plugin.getStartUpLogger().message("&cThere was an Error while sending Data to the BungeeServer: &4" + e);
            e.printStackTrace();
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
    