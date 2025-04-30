package de.happybavarian07.adminpanel.bungee;/*
 * @Author HappyBavarian07
 * @Date 02.09.2022 | 14:57
 */

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
        if (incomingChannelName.isEmpty()) this.incomingChannelName = "adminpanel:bungeein";
        if (outgoingChannelName.isEmpty()) this.outgoingChannelName = "adminpanel:bungeeout";
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeUTF(action);
        for(String s : args) {
            dos.writeUTF(s);
        }
            dos.flush();
        } catch (IOException e) {
            plugin.getStartUpLogger().coloredSpacer(ChatColor.RED);
            plugin.getStartUpLogger().message("&cFehler beim Schreiben in den OutputStream: &4" + e);
            e.printStackTrace();
            plugin.getStartUpLogger().coloredSpacer(ChatColor.RED);
            return;
        }
        // Log Success/fail
        // TODO Log More Things in Bungee Cord
        try {
            Bukkit.getServer().sendPluginMessage(plugin, outgoingChannelName, baos.toByteArray());
        } catch (Exception e) {
            plugin.getStartUpLogger().coloredSpacer(ChatColor.RED);
            plugin.getStartUpLogger().message("&cEs trat ein Fehler beim Senden der Daten an den BungeeServer auf: &4" + e);
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
