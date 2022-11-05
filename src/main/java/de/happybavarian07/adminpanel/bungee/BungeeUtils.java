package de.happybavarian07.adminpanel.bungee;/*
 * @Author HappyBavarian07
 * @Date 02.09.2022 | 14:57
 */

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.messaging.Messenger;

public class BungeeUtils {
    private final AdminPanelMain plugin;
    private final Messenger messenger;
    private String incomingChannelName;
    private String outgoingChannelName;
    private final IncomingChannelListener channelListener;

    // TODO Versuchen mit java Sockets eine Config Option hinzuzufügen, dass man Daten syncen kann ohne Spieler online
    // TODO Auf dem Bungee Cord einen Server machen und jeder Spigot/Paper Server ist dann ein Client der mit dem Server verbunden ist
    // https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
    // https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
    public BungeeUtils(String incomingChannelName, String outgoingChannelName) {
        this.incomingChannelName = incomingChannelName;
        this.outgoingChannelName = outgoingChannelName;
        if (incomingChannelName.equals("")) this.incomingChannelName = "adminpanel:bungeein";
        if (outgoingChannelName.equals("")) this.outgoingChannelName = "adminpanel:bungeeout";
        this.plugin = AdminPanelMain.getPlugin();
        this.channelListener = new IncomingChannelListener(incomingChannelName);
        messenger = Bukkit.getServer().getMessenger();
    }

    public void openBungeeChannel() {
        messenger.registerIncomingPluginChannel(plugin, incomingChannelName, channelListener);
        messenger.registerOutgoingPluginChannel(plugin, outgoingChannelName);
        System.out.println("Incoming: " + messenger.getIncomingChannels(plugin));
        System.out.println("Outgoing: " + messenger.getOutgoingChannels(plugin));
    }

    public void closeBungeeChannel() {
        System.out.println("Incoming: " + messenger.getIncomingChannels(plugin));
        System.out.println("Outgoing: " + messenger.getOutgoingChannels(plugin));
        messenger.unregisterIncomingPluginChannel(plugin, incomingChannelName, channelListener);
        messenger.unregisterOutgoingPluginChannel(plugin, outgoingChannelName);
    }

    public void sendDataToChannel(String action, String... args) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF(action);
        for(String s : args) {
            output.writeUTF(s);
        }
        // Log Success/fail
        // TODO Log More Things in Bungee Cord
        try {
            Bukkit.getServer().sendPluginMessage(plugin, outgoingChannelName, output.toByteArray());
        } catch (Exception e) {
            plugin.getStartUpLogger().coloredSpacer(ChatColor.RED);
            plugin.getStartUpLogger().message("&cThere was an Error while sending Data to the BungeeServer: &4" + e);
            e.printStackTrace();
            plugin.getStartUpLogger().coloredSpacer(ChatColor.RED);
        }
    }

    public String getIncomingChannelName() {
        return incomingChannelName;
    }

    public void setIncomingChannelName(String incomingChannelName) {
        this.incomingChannelName = incomingChannelName;
    }

    public String getOutgoingChannelName() {
        return outgoingChannelName;
    }

    public void setOutgoingChannelName(String outgoingChannelName) {
        this.outgoingChannelName = outgoingChannelName;
    }
}
