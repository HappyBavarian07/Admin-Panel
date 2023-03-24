package de.happybavarian07.adminpanel.bungee;/*
 * @Author HappyBavarian07
 * @Date 10.10.2022 | 16:30
 */

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Message {
    private final String senderName;
    private final String destination;
    private final Action action;
    private final List<String> data;

    public Message(String senderName, String destination, Action action, List<String> data) {
        this.senderName = senderName;
        this.destination = destination;
        this.action = action;
        this.data = data;
    }
    public Message(String senderName, String destination, Action action, @NotNull String... data) {
        this.senderName = senderName;
        this.destination = destination;
        this.action = action;
        this.data = Arrays.asList(data);
    }

    public String getSenderName() {
        return senderName;
    }

    public Action getAction() {
        return action;
    }

    public String getDestination() {
        return destination;
    }

    public List<String> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ClientMessage{" +
                "senderName='" + senderName + '\'' +
                ", destination='" + destination + '\'' +
                ", action='" + action + '\'' +
                ", data=" + data +
                '}';
    }

    public static Message fromStringArray(String[] stringArray) {
        if(stringArray.length < 4) return null;

        List<String> data = new ArrayList<>(Arrays.asList(stringArray).subList(3, stringArray.length));
        data.removeIf(Objects::isNull);
        return new Message(stringArray[0], stringArray[1], Action.fromString(stringArray[2]), data);
    }

    public String[] toStringArray() {
        String[] array = new String[data.size() + 3];
        array[0] = senderName;
        array[1] = destination;
        array[2] = action.name();
        int count = 3;
        for(String item : data) {
            array[count] = item;
            count++;
        }
        return array;
    }
}
