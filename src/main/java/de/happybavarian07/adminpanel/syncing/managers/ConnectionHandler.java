package de.happybavarian07.adminpanel.syncing.managers;/*
 * @Author HappyBavarian07
 * @Date 17.09.2023 | 19:14
 */

import au.com.xandar.crypto.CryptoPacket;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.syncing.custompayload.CustomPacket;
import de.happybavarian07.adminpanel.syncing.utils.EncryptionUtils;
import de.happybavarian07.adminpanel.syncing.utils.Packet;
import de.happybavarian07.adminpanel.syncing.events.JavaSocketConnectedEvent;
import de.happybavarian07.adminpanel.syncing.events.JavaSocketDisconnectedEvent;
import de.happybavarian07.adminpanel.syncing.events.JavaSocketPacketReceivedEvent;
import de.happybavarian07.adminpanel.utils.Result;
import de.happybavarian07.adminpanel.utils.StartUpLogger;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class ConnectionHandler {
    private final String ipAddress;
    private final int port;
    private final SettingsManager settingsManager;
    private final EncryptionUtils clientEncryptionUtils;
    private final ThreadGroup dataClientThreadGroup;
    private final LinkedBlockingQueue<Object> packets;
    private final List<String> otherConnectedClients = new ArrayList<>();
    private final LoggingManager loggingManager;
    private final StartUpLogger pluginLogger;
    private String clientName;
    private Socket socket;
    private ConnectionToServer server;
    private boolean enabled;
    private boolean nameReceived;
    private boolean clientListUpdateRequested = false;
    private PacketHandler packetHandler;
    private final StatsManager statsManager;

    public ConnectionHandler(String ipAddress, int port, String clientName, ThreadGroup dataClientThreadGroup, SettingsManager settingsManager, LoggingManager loggingManager, StatsManager statsManager) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.clientName = clientName;
        this.settingsManager = settingsManager;
        this.loggingManager = loggingManager;
        this.dataClientThreadGroup = dataClientThreadGroup;
        this.clientEncryptionUtils = new EncryptionUtils(true);
        this.packets = new LinkedBlockingQueue<>();
        this.pluginLogger = AdminPanelMain.getPlugin().getStartUpLogger();
        this.statsManager = statsManager;

        // Initialize other members as needed
    }

    public ConnectionHandler(String ipAddress, int port, String clientName, ThreadGroup dataClientThreadGroup, SettingsManager settingsManager, LoggingManager loggingManager, PacketHandler packetHandler, StatsManager statsManager) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.clientName = clientName;
        this.settingsManager = settingsManager;
        this.loggingManager = loggingManager;
        this.dataClientThreadGroup = dataClientThreadGroup;
        this.clientEncryptionUtils = new EncryptionUtils(true);
        this.packets = new LinkedBlockingQueue<>();
        this.pluginLogger = AdminPanelMain.getPlugin().getStartUpLogger();
        this.packetHandler = packetHandler;
        this.statsManager = statsManager;
        // Initialize other members as needed
    }

    public boolean connect() throws IOException {
        try {
            //socket = (SSLSocket) sslSocketFactory.createSocket(ipAddress, port);
            socket = new Socket(ipAddress, port);
            server = new ConnectionToServer(socket);
            packets.clear();
        } catch (IOException e) {
            e.printStackTrace();
            loggingManager.logIntoFile(Level.SEVERE, "IO Exception while connecting to Server: " + e + ":" + e.getMessage());
            statsManager.addErrorsThisSession(1);
            return false;
        }
        send("NameForClientIs:" + clientName + ":" + clientEncryptionUtils.getPublicKeyAsBase64(), true, true);
        loggingManager.logIntoFile(Level.INFO, "Client connected to Server.");
        try {
            JavaSocketConnectedEvent connectedEvent = new JavaSocketConnectedEvent(ipAddress, port, clientName);
            AdminPanelMain.getAPI().callAdminPanelEvent(connectedEvent);
            //if (connectedEvent.isCancelled()) return;
        } catch (NotAPanelEventException e) {
            e.printStackTrace();
            statsManager.addErrorsThisSession(1);
            return false;
        }
        statsManager.addConnectionsToServerThisSession(1);
        enabled = true;
        return true;
    }

    public boolean reconnect(boolean notifyServer) throws IOException {
        disconnect(notifyServer);
        return connect();
    }

    public void disconnect(boolean notifyServer) {
        if (server != null)
            server.disconnect(notifyServer);
        enabled = false;
        packets.clear();
        try {
            JavaSocketDisconnectedEvent disconnectedEvent = new JavaSocketDisconnectedEvent(notifyServer);
            AdminPanelMain.getAPI().callAdminPanelEvent(disconnectedEvent);
            //if (disconnectedEvent.isCancelled()) return;
        } catch (NotAPanelEventException e) {
            e.printStackTrace();
            statsManager.addErrorsThisSession(1);
        }
    }

    protected Result send(Object obj, boolean callSendEvent, boolean force) throws IOException {
        if (settingsManager.isDebugEnabled())
            pluginLogger.dataClientMessage(ChatColor.BLUE, true, true,
                    "Object to Send: " + obj,
                    "Is Packet: " + (obj instanceof Packet));
        if (obj instanceof Packet) {
            // Event Handling Start
            try {
                if (callSendEvent) {
                    JavaSocketPacketReceivedEvent sendEvent = new JavaSocketPacketReceivedEvent((Packet) obj);
                    AdminPanelMain.getAPI().callAdminPanelEvent(sendEvent);
                    if (sendEvent.isCancelled() && !force) return Result.FAILURE;
                }
                server.write(((Packet) obj).toStringArray(), true);
            } catch (NotAPanelEventException e) {
                e.printStackTrace();
                statsManager.addErrorsThisSession(1);
                return Result.ERROR;
            }
            // Event Handling Stop

            statsManager.addPacketsSendThisSession(1);
            statsManager.addBytesSendThisSession(Arrays.toString(((Packet) obj).toStringArray()).getBytes().length);

            loggingManager.logIntoFile(Level.INFO, "Packet send to Server: " + obj + "(CallSendEvent: " + callSendEvent + ", Force:" + force + ")");
            return Result.SUCCESS;
        }
        server.write(obj, true);
        statsManager.addPacketsSendThisSession(1);
        loggingManager.logIntoFile(Level.INFO, "Packet send to Server: " + obj + "(CallSendEvent: " + callSendEvent + ", Force:" + force + ")");
        return Result.SUCCESS;
    }

    public EncryptionUtils getClientEncryptionUtils() {
        return clientEncryptionUtils;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isClientListUpdateRequested() {
        return clientListUpdateRequested;
    }

    public void setClientListUpdateRequested(boolean clientListUpdateRequested) {
        this.clientListUpdateRequested = clientListUpdateRequested;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    public LinkedBlockingQueue<Object> getPackets() {
        return packets;
    }

    public ConnectionToServer getServer() {
        return server;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isNameReceived() {
        return nameReceived;
    }

    public void setNameReceived(boolean nameReceived) {
        this.nameReceived = nameReceived;
    }

    public List<String> getOtherConnectedClients() {
        return otherConnectedClients;
    }

    public ThreadGroup getDataClientThreadGroup() {
        return dataClientThreadGroup;
    }

    public int getPort() {
        return port;
    }

    public class ConnectionToServer {
        private final ObjectInputStream in;
        private final ObjectOutputStream out;
        private final LinkedBlockingQueue<Object> packetQueue;
        private Socket socket;
        private boolean disabled = false;
        private EncryptionUtils serverEncryptionUtils;
        // The UUID the Server gave the Client
        private UUID clientUUID;

        ConnectionToServer(Socket socket) throws IOException {
            this.packetQueue = new LinkedBlockingQueue<>();
            this.socket = socket;
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Start a separate thread for reading server responses
            Thread readThread = new Thread(this::readServerResponses, "Read Packet Thread");
            readThread.setDaemon(true);
            readThread.start();

            // Start a separate thread for handling received packets
            Thread packetHandlerThread = new Thread(this::handleReceivedPackets, "Packet Handling Thread");
            packetHandlerThread.setDaemon(true);
            packetHandlerThread.start();
        }

        private void readServerResponses() {
            do {
                try {
                    Object obj = in.readObject();
                    if (obj instanceof String) {
                        String packetString = (String) obj;
                        if (packetString.startsWith("RegisteredClientName:")) {
                            handleRegisteredClientName(packetString);
                        } else if (packetString.startsWith("DisconnectingClientFromServer")) {
                            disconnect(false);
                        }else if(packetString.startsWith("CustomPacket:")) {
                            packetQueue.put(nameReceived ? handleEncodedCustomPacket(packetString) : packetString);
                        } else {
                            packetQueue.put(nameReceived ? handleEncodedPacket(packetString) : packetString);
                        }
                        statsManager.addBytesReceivedThisSession(packetString.getBytes().length);
                    } else if (obj instanceof String[]) {
                        String[] array = (String[]) obj;
                        if(Objects.equals(array[0], "CustomPacket")) {
                            CustomPacket packet = CustomPacket.fromStringArray(array);
                            packetQueue.put(packet);
                            continue;
                        }
                        Packet packet = Packet.fromStringArray(array);
                        if (packet.getSenderName().equals("SERVER")) {
                            packetQueue.put(packet);
                        }
                        statsManager.addBytesReceivedThisSession(Arrays.toString(array).getBytes().length);
                    } else {
                        packetQueue.put(obj);
                        statsManager.addBytesReceivedThisSession(obj.toString().getBytes().length);
                        loggingManager.logIntoFile(Level.INFO, "Reading Packet from Server Out: " + obj);
                    }
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    loggingManager.logIntoFile(Level.INFO, "Error while reading Packet: " + e + ":" + e.getMessage() + " (Shutting down Read Thread for good)");
                    statsManager.addErrorsThisSession(1);
                    break;
                }
            } while (!disabled && socket.isConnected());
        }

        private void handleReceivedPackets() {
            do {
                try {
                    if (!nameReceived) {
                        continue;
                    }
                    Object packet = packetQueue.take();
                    if (packet instanceof String) {
                        packets.put(handleEncodedPacket((String) packet));
                    } else {
                        packets.put(packet);
                    }
                } catch (InterruptedException e) {
                    statsManager.addErrorsThisSession(1);
                    break;
                }
            } while (!disabled && socket.isConnected());
        }

        private void handleRegisteredClientName(String packetString) {
            String[] array = packetString.split(":");
            System.out.println("Registered Client: " + Arrays.toString(array));

            if (array.length >= 3) {
                setClientName(array[1]);
                clientUUID = UUID.fromString(array[2]);
                if (array.length >= 4) {
                    String encodedKey = array[3];
                    serverEncryptionUtils = new EncryptionUtils(encodedKey, null);
                }
                AdminPanelMain.getPlugin().getStartUpLogger()
                        .dataClientMessage(ChatColor.GREEN, "Client Registered! New Name: '" + getClientName() + "'", true, true);
                nameReceived = true;
            }
        }

        private Object handleEncodedPacket(String packetString) {
            String[] encryptedPacketArray = packetString.split(":");
            if (encryptedPacketArray.length != 3) return packetString;
            String decryptedString = serverEncryptionUtils.decrypt(encryptedPacketArray[0], encryptedPacketArray[1], encryptedPacketArray[2]);

            System.out.println("Decrypted String: " + decryptedString);

            statsManager.addBytesDecryptedThisSession(decryptedString.getBytes().length);

            // Remove extra '[' and ']' characters, if present
            decryptedString = decryptedString.trim();
            if (decryptedString.startsWith("[") && decryptedString.endsWith("]")) {
                decryptedString = decryptedString.substring(1, decryptedString.length() - 1);
            }

            String[] packetArray = decryptedString.split(",");
            for (int i = 0; i < packetArray.length; i++) {
                packetArray[i] = packetArray[i].trim();
            }

            System.out.println("Decrypted Packet: " + Arrays.toString(packetArray));
            System.out.println("Decrypted String: " + decryptedString);

            if(Packet.hasPacketArrayLength(packetArray)) {
                return Packet.fromStringArray(packetArray);
            } else {
                return decryptedString;
            }
        }

        private Object handleEncodedCustomPacket(String packetString) {
            if(packetString.startsWith("CustomPacket:")) {
                packetString = packetString.replaceFirst("CustomPacket:", "");
            }
            String[] encryptedPacketArray = packetString.split(":");
            if (encryptedPacketArray.length != 3) return packetString;
            String decryptedString = serverEncryptionUtils.decrypt(encryptedPacketArray[0], encryptedPacketArray[1], encryptedPacketArray[2]);

            System.out.println("Decrypted String: " + decryptedString);

            statsManager.addBytesDecryptedThisSession(decryptedString.getBytes().length);

            // Remove extra '[' and ']' characters, if present
            decryptedString = decryptedString.trim();
            if (decryptedString.startsWith("[") && decryptedString.endsWith("]")) {
                decryptedString = decryptedString.substring(1, decryptedString.length() - 1);
            }

            String[] packetArray = decryptedString.split(",");
            for (int i = 0; i < packetArray.length; i++) {
                packetArray[i] = packetArray[i].trim();
            }

            System.out.println("Decrypted Packet: " + Arrays.toString(packetArray));
            System.out.println("Decrypted String: " + decryptedString);

            if(CustomPacket.hasPacketArrayLength(packetArray)) {
                return CustomPacket.fromStringArray(packetArray);
            } else {
                return decryptedString;
            }
        }

        public void disconnect(boolean notifyServer) {
            disabled = true;
            try {
                if (settingsManager.isDebugEnabled())
                    pluginLogger.dataClientMessage(ChatColor.BLUE, false, true,
                            "Disconnect Message: " + notifyServer,
                            "Server connected: " + socket.isConnected(),
                            "Socket closed: " + (socket.isClosed()));
                if (notifyServer && socket.isConnected() && !socket.isClosed())
                    send("DisconnectingFromServer:" + clientName, true, true);
                socket.close();
            } catch (IOException e) {
                loggingManager.logIntoFile(Level.INFO, "Error while disconnecting: " + e + ":" + e.getMessage());
                e.printStackTrace();
                statsManager.addErrorsThisSession(1);
            }
            pluginLogger.dataClientMessage(ChatColor.RED, "Client disconnected from Server!", true, true);
            loggingManager.logIntoFile(Level.INFO, "Client disconnected from Server.");
            try {
                socket.close();
            } catch (IOException ignored) {
                statsManager.addErrorsThisSession(1);
            }
            socket = null;
            server = null;
        }

        public void write(Object obj, boolean encryption) {
            System.out.println("Write Method got finally called: " + obj + " : " + encryption);
            try {
                if (obj instanceof String) {
                    encryption = !((String) obj).startsWith("NameForClientIs:");
                }
                if (clientEncryptionUtils == null || clientEncryptionUtils.getKeyPair() == null ||
                        clientEncryptionUtils.getKeyPair().getBase64PrivateKey() == null || clientEncryptionUtils.getPrivateKeyAsBase64().isEmpty()) {
                    out.writeObject(obj.toString());
                } else {
                    if (encryption) {
                        CryptoPacket cryptoPacket = encryptPacket(obj);
                        out.writeObject(cryptoPacketToString(cryptoPacket));
                    } else {
                        out.writeObject(obj.toString());
                    }
                }
                out.flush();
            } catch (IOException e) {
                loggingManager.logIntoFile(Level.INFO, "Error while writing Packet: " + e + ":" + e.getMessage());
                statsManager.addErrorsThisSession(1);
                e.printStackTrace();
            }
        }

        private CryptoPacket encryptPacket(Object obj) {
            String content;
            if (obj instanceof Packet) {
                content = Arrays.toString(((Packet) obj).toStringArray()).trim();
            } else if (obj instanceof String) {
                content = ((String) obj).trim();
            } else if (obj instanceof String[]) {
                content = Arrays.toString((String[]) obj).trim();
            } else {
                content = obj.toString().trim();
            }

            statsManager.addBytesEncryptedThisSession(content.getBytes().length);

            return clientEncryptionUtils.encrypt(content);
        }

        private String cryptoPacketToString(CryptoPacket cryptoPacket) {
            return Base64.getEncoder().encodeToString(cryptoPacket.getEncryptedData()) + ":" +
                    Base64.getEncoder().encodeToString(cryptoPacket.getEncryptedSymmetricKey()) + ":" +
                    Base64.getEncoder().encodeToString(cryptoPacket.getSymmetricCipherInitializationVector());
        }

        public boolean isDisabled() {
            return disabled;
        }

        public Socket getSocket() {
            return socket;
        }

        public UUID getClientUUID() {
            return clientUUID;
        }
    }
}
