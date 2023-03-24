package de.happybavarian07.adminpanel.utils;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StartUpLogger {
    private final BlockingQueue<String[]> messageQueue;
    private final Thread messageQueueThread;
    private final String SPACER_FORMAT;
    private final ConsoleCommandSender sender = Bukkit.getConsoleSender();
    private boolean enabled;

    public StartUpLogger() {
        messageQueue = new LinkedBlockingQueue<>();
        enabled = true;
        SPACER_FORMAT = AdminPanelMain.getPlugin().getConfig().getString("Plugin.StartUpLogger.Spacer_Format",
                "+-------------------------------------------------------------+");
        messageQueueThread = new Thread(() -> {
            while (true) {
                if (!enabled) continue;
                try {
                    String[] messages = messageQueue.take();
                    //System.out.println("Messages: " + Arrays.toString(messages));
                    if (messages.length == 0) continue;
                    for (String message : messages) {
                        if (message == null || message.isEmpty()) continue;

                        sender.sendMessage(message);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "StartUpLogger Messag Queue Thread");
        messageQueueThread.setDaemon(true);
        messageQueueThread.start();
    }

    public static StartUpLogger create() {
        return new StartUpLogger();
    }

    public StartUpLogger spacer() {
        //sender.sendMessage(SPACER_FORMAT);
        addMessageToQueue(SPACER_FORMAT);
        return this;
    }

    public String getSpacer() {
        return SPACER_FORMAT;
    }

    public StartUpLogger coloredSpacer(ChatColor color) {
        //sender.sendMessage(color + SPACER_FORMAT);
        addMessageToQueue(color + SPACER_FORMAT);
        return this;
    }

    public String getColoredSpacer(ChatColor color) {
        return color + SPACER_FORMAT;
    }

    public StartUpLogger emptySpacer() {
        //sender.sendMessage("");
        addMessageToQueue("");
        return this;
    }

    public String getEmptySpacer() {
        return "";
    }

    public StartUpLogger message(String message) {
        /*sender.sendMessage(Utils.format(null, getMessageWithFormat(message),
                AdminPanelMain.getPrefix() != null ? AdminPanelMain.getPrefix() : "[Admin-Panel]"));*/
        addMessageToQueue(Utils.format(null, getMessageWithFormat(message),
                AdminPanelMain.getPrefix() != null ? AdminPanelMain.getPrefix() : "[Admin-Panel]"));
        return this;
    }

    /**
     * Formatted Message
     *
     * @param message Message
     * @return Formatted Message
     */
    public String getMessage(String message) {
        return Utils.format(null, getMessageWithFormat(message),
                AdminPanelMain.getPrefix() != null ? AdminPanelMain.getPrefix() : "[Admin-Panel]");
    }

    public StartUpLogger coloredMessage(ChatColor color, String message) {
        //sender.sendMessage(color + getMessageWithFormat(message));
        addMessageToQueue(color + message);
        return this;
    }

    public String getColoredMessage(ChatColor color, String message) {
        return color + getMessageWithFormat(message);
    }

    public StartUpLogger dataClientMessage(ChatColor color, String message, boolean headerAndFooter, boolean title) {
        String[] finalMessage = new String[4];
        if (headerAndFooter)
            finalMessage[0] = getColoredSpacer(color);
        else
            finalMessage[0] = "";
        if (title)
            finalMessage[1] = getColoredMessage(color, "Java Socket Bungeecord Data Sync System (short: JSBDSS):");
        else
            finalMessage[1] = "";
        finalMessage[2] = getColoredMessage(color, message);
        if (headerAndFooter)
            finalMessage[3] = getColoredSpacer(color);
        else
            finalMessage[3] = "";
        addMessageToQueue(finalMessage);
        return this;
    }

    public StartUpLogger dataClientMessage(ChatColor color, boolean headerAndFooter, boolean title, String... messages) {
        String[] finalMessage = new String[4 + messages.length];
        if (headerAndFooter)
            finalMessage[0] = getColoredSpacer(color);
        else
            finalMessage[0] = "";
        if (title)
            finalMessage[1] = getColoredMessage(color, "Java Socket Bungeecord Data Sync System (short: JSBDSS):");
        else
            finalMessage[1] = "";
        int count = 0;
        for (String message : messages) {
            finalMessage[2 + count] = getColoredMessage(color, message);
            count++;
        }
        if (headerAndFooter)
            finalMessage[2 + messages.length] = getColoredSpacer(color);
        else
            finalMessage[3 + messages.length] = "";
        addMessageToQueue(finalMessage);
        return this;
    }

    public StartUpLogger rawMessage(String message) {
        //sender.sendMessage(message);
        addMessageToQueue(message);
        return this;
    }

    public StartUpLogger messages(String... messages) {
        /*for (String message : messages)
            sender.sendMessage(Utils.format(null, getMessageWithFormat(message),
                    AdminPanelMain.getPrefix() != null ? AdminPanelMain.getPrefix() : "[Admin-Panel]"));*/
        for (String message : messages)
            addMessageToQueue(Utils.format(null, getMessageWithFormat(message),
                    AdminPanelMain.getPrefix() != null ? AdminPanelMain.getPrefix() : "[Admin-Panel]"));
        return this;
    }

    public StartUpLogger rawMessages(String... messages) {
        //sender.sendMessage(messages);
        addMessageToQueue(messages);
        return this;
    }

    private String getMessageWithFormat(String message) {
        String MESSAGE_FORMAT = AdminPanelMain.getPlugin().getConfig().getString("Plugin.StartUpLogger.Message_Format",
                "|------------------------------------------------------------------|");
        final int messageSpacerLength = MESSAGE_FORMAT.length();
        final int messageLength = message.replaceAll("§([a-fA-F0-9]|r|l|m|n|o|k)", "").length();

        // Return the default message if it is too long for the actual spacer
        if (messageLength > messageSpacerLength - 2) return message;

        final int partLength = (messageSpacerLength - messageLength) / 2;

        final String startPart = MESSAGE_FORMAT.substring(0, partLength);
        final String endPart = MESSAGE_FORMAT.substring(messageSpacerLength - partLength, messageSpacerLength);

        return startPart + message + endPart;
    }

    public void addMessageToQueue(String... message) {
        messageQueue.add(message);
    }

    public void addMessageToQueue(String message) {
        messageQueue.add(new String[]{message});
    }

    public void enableMessageSystem() {
        if (isMessageSystemEnabled()) return;
        enabled = true;
        messageQueueThread.resume();
    }

    public boolean isMessageSystemEnabled() {
        return enabled;
    }

    public void disableMessageSystem() {
        if (!isMessageSystemEnabled()) return;
        enabled = false;
        messageQueueThread.suspend();
    }
}