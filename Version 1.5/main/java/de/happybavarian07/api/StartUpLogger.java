package de.happybavarian07.api;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class StartUpLogger {

    private final String SPACER_FORMAT = "+------------------------------------------------------------------+";
    private final String MESSAGE_FORMAT = "|------------------------------------------------------------------|";

    private final ConsoleCommandSender sender = Bukkit.getConsoleSender();

    public StartUpLogger spacer() {
        sender.sendMessage(SPACER_FORMAT);
        return this;
    }

    public StartUpLogger coloredSpacer(ChatColor color) {
        sender.sendMessage(color + SPACER_FORMAT);
        return this;
    }

    public StartUpLogger emptySpacer() {
        sender.sendMessage("");
        return this;
    }

    public StartUpLogger message(String message) {
        sender.sendMessage(getMessageWithFormat(message));
        return this;
    }

    public StartUpLogger coloredMessage(ChatColor color, String message) {
        sender.sendMessage(color + getMessageWithFormat(message));
        return this;
    }

    public StartUpLogger rawMessage(String message) {
        sender.sendMessage(message);
        return this;
    }

    public StartUpLogger messages(String... messages) {
        for(String message : messages) sender.sendMessage(getMessageWithFormat(message));
        return this;
    }

    public StartUpLogger rawMessages(String... messages) {
        sender.sendMessage(messages);
        return this;
    }

    private String getMessageWithFormat(String message) {
        final int messageSpacerLength = MESSAGE_FORMAT.length();
        final int messageLength = message.replaceAll("§([a-fA-F0-9]|r|l|m|n|o|k)", "").length();

        // Return the default message if it is too long for the actual spacer
        if(messageLength > messageSpacerLength - 2) return message;

        final int partLength = (messageSpacerLength - messageLength) / 2;

        final String startPart = MESSAGE_FORMAT.substring(0, partLength);
        final String endPart = MESSAGE_FORMAT.substring(messageSpacerLength - partLength, messageSpacerLength);

        return startPart + message + endPart;
    }

    public static StartUpLogger create() {
        return new StartUpLogger();
    }

}