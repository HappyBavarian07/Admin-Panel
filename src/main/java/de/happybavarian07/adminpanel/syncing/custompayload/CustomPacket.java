package de.happybavarian07.adminpanel.syncing.custompayload;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CustomPacket {
    private final String senderName;
    private final String destination;
    private final CustomAction action;
    private final Map<String, String> data;
    private String requestId = null;

    public CustomPacket(String senderName, String destination, CustomAction action, Map<String, String> data) {
        this.senderName = senderName;
        this.destination = destination;
        this.action = action;
        this.data = data;
    }

    public CustomPacket(String senderName, String destination, String requestId, CustomAction action, Map<String, String> data) {
        this.senderName = senderName;
        this.destination = destination;
        this.action = action;
        this.data = data;
        this.requestId = requestId;
    }

    public static CustomPacket fromStringArray(String[] stringArray) {
        if (stringArray.length < 5 || !stringArray[0].equals("CustomPacket")) {
            throw new IllegalArgumentException("Invalid input array format");
        }

        Map<String, String> data = new HashMap<>();
        for (int i = 5; i < stringArray.length; i++) {
            String[] parts = stringArray[i].split(":");
            if (parts.length == 2) {
                data.put(parts[0], parts[1]);
            }
        }

        String requestId = stringArray[3].isEmpty() ? null : stringArray[3].trim();

        return new CustomPacket(stringArray[1].trim(), stringArray[2].trim(), requestId, CustomAction.fromString(stringArray[4].trim()), data);
    }


    public static boolean hasPacketArrayLength(String[] array) {
        return array.length >= 5 && array[0].equals("CustomPacket");
    }

    public String getSenderName() {
        return senderName;
    }

    public CustomAction getAction() {
        return action;
    }

    public String getDestination() {
        return destination;
    }

    public Map<String, String> getData() {
        return data;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "CustomPacket{" +
                "senderName='" + senderName + '\'' +
                ", destination='" + destination + '\'' +
                ", action=" + action +
                ", data=" + data +
                ", requestId='" + requestId + '\'' +
                '}';
    }

    public String[] toStringArray() {
        List<String> array = new ArrayList<>();
        array.add("CustomPacket"); // Prefix for CustomPacket
        array.add(senderName.trim());
        array.add(destination.trim());
        array.add(requestId != null ? requestId.trim() : ""); // Include requestId if present
        array.add(action.name().trim());

        for (Map.Entry<String, String> entry : data.entrySet()) {
            array.add(entry.getKey() + ":" + entry.getValue());
        }

        return array.toArray(new String[0]);
    }

}
