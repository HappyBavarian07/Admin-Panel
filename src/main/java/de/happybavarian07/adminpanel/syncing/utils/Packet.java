package de.happybavarian07.adminpanel.syncing.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class Packet {
    private final String senderName;
    private final String destination;
    private final Action action;
    private final List<String> data;
    private String requestId;

    public Packet(String senderName, String destination, Action action, List<String> data) {
        this.requestId = null;
        this.senderName = senderName;
        this.destination = destination;
        this.action = action;
        this.data = data;
    }

    public Packet(String senderName, String destination, Action action, @NotNull String... data) {
        this(senderName, destination, action, Arrays.asList(data));
    }

    public Packet(String senderName, String destination, String requestId, Action action, List<String> data) {
        this.requestId = null;
        this.senderName = senderName;
        this.destination = destination;
        this.action = action;
        this.data = data;
        this.requestId = requestId;
    }

    public Packet(String senderName, String destination, String requestId, Action action, String... data) {
        this(senderName, destination, requestId, action, Arrays.asList(data));
    }

    public static Packet fromStringArray(String[] stringArray) {
        if (stringArray.length < 4) {
            throw new IllegalArgumentException("Invalid input array length");
        } else {
            List<String> data = new ArrayList(Arrays.asList(stringArray).subList(4, stringArray.length));
            data.removeIf(Objects::isNull);
            String requestId = stringArray[3].isEmpty() ? null : stringArray[3].trim();
            Packet packet = new Packet(stringArray[0].trim(), stringArray[1].trim(), Action.fromString(stringArray[2].trim()), data);
            if (!stringArray[3].isEmpty()) {
                packet.setRequestId(requestId);
            }

            return packet;
        }
    }

    public static boolean hasPacketArrayLength(String[] array) {
        return array.length >= 4;
    }

    public String getSenderName() {
        return this.senderName;
    }

    public Action getAction() {
        return this.action;
    }

    public String getDestination() {
        return this.destination;
    }

    public List<String> getData() {
        return this.data;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String toString() {
        return "Packet{senderName='" + this.senderName + "', destination='" + this.destination + "', action=" + this.action + ", data=" + this.data + ", requestId='" + this.requestId + "'}";
    }

    public String[] toStringArray() {
        String[] array = new String[this.data.size() + 4];
        array[0] = this.senderName.trim();
        array[1] = this.destination.trim();
        array[2] = this.action.name().trim();
        array[3] = this.requestId != null ? this.requestId.trim() : "";
        int count = 4;

        String item;
        for(Iterator var3 = this.data.iterator(); var3.hasNext(); array[count++] = item.trim()) {
            item = (String)var3.next();
        }

        return array;
    }
}