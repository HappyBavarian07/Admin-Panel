package de.happybavarian07.adminpanel.syncing.managers;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.syncing.DataClient;
import de.happybavarian07.adminpanel.syncing.custompayload.CustomAction;
import de.happybavarian07.adminpanel.syncing.custompayload.CustomPacket;
import de.happybavarian07.adminpanel.syncing.utils.Action;
import de.happybavarian07.adminpanel.syncing.utils.Packet;
import de.happybavarian07.adminpanel.utils.Result;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

public class PacketHandler {
    private final DataClient dataClient;
    private final LinkedBlockingQueue<Object> packets;
    private final ConnectionHandler connectionHandler;
    private final LoggingManager loggingManager;
    private final Map<UUID, CompletableFuture<Packet>> responseHandlers = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<CustomPacket>> customResponseHandlers = new ConcurrentHashMap<>();
    private final StatsManager statsManager;
    private boolean packetHandlingEnabled;

    public PacketHandler(DataClient dataClient, ConnectionHandler connectionHandler, LoggingManager loggingManager, StatsManager statsManager) {
        this.dataClient = dataClient;
        this.loggingManager = loggingManager;
        this.packets = connectionHandler.getPackets();
        this.connectionHandler = connectionHandler;
        this.statsManager = statsManager;
        this.packetHandlingEnabled = true;
    }

    public void startPacketHandlingThread() {
        packetHandlingEnabled = true;
        Thread packetHandlingThread = new Thread(() -> {
            while (packetHandlingEnabled) {
                try {
                    Object packet = packets.take();
                    if (!connectionHandler.isNameReceived() || !packetHandlingEnabled) {
                        packets.put(packet);
                        continue;
                    }
                    if (packet instanceof Packet) {
                        handleReceivedPacket((Packet) packet);
                        statsManager.addPacketsReceivedThisSession(1);
                    }
                    if (packet instanceof CustomPacket) {
                        handleReceivedCustomPacket((CustomPacket) packet);
                        statsManager.addPacketsReceivedThisSession(1);
                    }
                } catch (InterruptedException ignored) {
                    statsManager.addErrorsThisSession(1);
                }
            }
        }, "Packet Handling Thread");

        packetHandlingThread.setDaemon(true);
        packetHandlingThread.start();
    }

    public void stopPacketHandlingThread() {
        packetHandlingEnabled = false;
    }

    public boolean isPacketHandlingEnabled() {
        return packetHandlingEnabled;
    }

    public Result send(Object obj, boolean callSendEvent, boolean force) throws IOException {
        return connectionHandler.send(obj, callSendEvent, force);
    }

    public CompletableFuture<Integer> pingServer() {
        CompletableFuture<Integer> pingFuture = new CompletableFuture<>();

        if (!dataClient.isEnabled()) {
            pingFuture.complete(503); // Service Unavailable, HTTP status code 503
            return pingFuture;
        }

        // Create a unique request ID for this ping
        UUID requestId = UUID.randomUUID();

        // Register a response handler for this request ID
        registerResponseHandler(requestId, responsePacket -> {
            if (responsePacket.getAction() == Action.PINGSERVERANSWERED) {
                pingFuture.complete(200); // OK, HTTP status code 200
            } else {
                pingFuture.complete(500); // Internal Server Error, HTTP status code 500
            }
        }, null);

        // Send the ping request to the server
        try {
            if (connectionHandler.getServer().isDisabled() || !connectionHandler.getSocket().isConnected() || connectionHandler.getSocket().isClosed()) {
                pingFuture.complete(503); // Service Unavailable, HTTP status code 503
                return pingFuture;
            }

            Packet pingPacket = new Packet(connectionHandler.getClientName(), "SERVER", Action.PINGSERVER, "PingingServer");
            pingPacket.setRequestId(requestId.toString());
            sendAsync(pingPacket, false, true, 3, 1).exceptionally(ex -> {
                pingFuture.complete(503); // Service Unavailable, HTTP status code 503
                return null;
            });
        } catch (IOException e) {
            pingFuture.complete(503); // Service Unavailable, HTTP status code 503
            statsManager.addErrorsThisSession(1);
        }

        return pingFuture;
    }

    public void handleReceivedCustomPacket(CustomPacket customPacket) {
        //System.out.println("Received customPacket: " + customPacket);
        if (customPacket.getRequestId() != null && !customPacket.getRequestId().isEmpty()) {
            if (customResponseHandlers.containsKey(UUID.fromString(customPacket.getRequestId()))) {
                customResponseHandlers.get(UUID.fromString(customPacket.getRequestId())).complete(customPacket);
                //System.out.println("Responded to Server Response Handler: " + customPacket.getRequestId());
            }
        }
        String clientName = customPacket.getSenderName();
        String destination = customPacket.getDestination();
        String requestId = customPacket.getRequestId();
        CustomAction action = customPacket.getAction();
        Map<String, String> data = customPacket.getData();

        loggingManager.logIntoFile(Level.INFO, "Received Custom Packet from Server: " + customPacket);

        boolean destinationCheck = !destination.equals(connectionHandler.getClientName()) && !destination.equals("null");

        if (!CustomAction.dataHasCorrectArgs(data, action)) {
            if (dataClient.getSettingsManager().isDebugEnabled()) {
                dataClient.getPluginLogger().dataClientMessage(ChatColor.RED, "Received CustomPacket from Server with wrong data format: " + customPacket, true, true);
            }
            loggingManager.logIntoFile(Level.INFO, "Received CustomPacket from Server with wrong data format: " + customPacket);
            return;
        }

        switch (action) {
            case EXECUTE_COMMAND:
                if (!destinationCheck) {
                    Player player;

                    if (data.containsKey("UUID"))
                        player = Bukkit.getPlayer(UUID.fromString(data.get("UUID")));
                    else
                        player = Bukkit.getPlayer(data.get("Player"));
                    if (player == null)
                        AdminPanelMain.getPlugin().getServer().dispatchCommand(AdminPanelMain.getPlugin().getServer().getConsoleSender(), data.get("Command"));
                    else
                        player.performCommand(data.get("Command"));
                }
                break;
            case SEND_MESSAGE:
                if (!destinationCheck) {
                    Player player;

                    if (data.containsKey("UUID"))
                        player = Bukkit.getPlayer(UUID.fromString(data.get("UUID")));
                    else
                        player = Bukkit.getPlayer(data.get("Player"));
                    if (player == null)
                        break;

                    player.sendMessage(data.get("Message"));
                }
                break;
            case BROADCAST_MESSAGE:
                if (!destinationCheck) {
                    String prefix = Utils.format(null, data.get("Prefix"), AdminPanelMain.getPrefix());
                    String message = Utils.format(null, data.get("Message"), prefix);
                    Bukkit.broadcastMessage(message);
                }
                break;
            default:
                // Received unknown Packet from Server (Logging into LogFile atm)
                loggingManager.logIntoFile(Level.INFO, "Received unknown Packet from Server: " + customPacket);
                break;
        }
    }

    public void handleReceivedPacket(Packet packet) {
        //System.out.println("Received packet: " + packet);
        if (packet.getRequestId() != null && !packet.getRequestId().isEmpty()) {
            if (responseHandlers.containsKey(UUID.fromString(packet.getRequestId()))) {
                responseHandlers.get(UUID.fromString(packet.getRequestId())).complete(packet);
                //System.out.println("Responded to Server Response Handler: " + packet.getRequestId());
            }
        }
        String clientName = packet.getSenderName();
        String destination = packet.getDestination();
        String requestId = packet.getRequestId();
        Action action = packet.getAction();
        List<String> data = packet.getData();

        //System.out.println("ClientName: " + clientName);
        //System.out.println("Destination: " + destination);
        //System.out.println("Action: " + action);
        //System.out.println("Data: " + data);

        loggingManager.logIntoFile(Level.INFO, "Received Packet from Server: " + packet);

        boolean destinationCheck = !destination.equals(connectionHandler.getClientName()) && !destination.equals("null");

        switch (action) {
            case SENDPERMISSIONS:
                if (!destinationCheck) {
                    AdminPanelMain.getPlugin().getDataClientUtils().applyReceivedPermissions(UUID.fromString(data.get(0)), data.get(1));
                }
                break;
            case SENDCUSTOMMAP:
                if (!destinationCheck) {
                    AdminPanelMain.getPlugin().getDataClientUtils().receiveCustomMap(data.get(1), data.get(0));
                }
                break;
            case PINGCLIENT:
                if (!destinationCheck) {
                    Packet pingPacket = new Packet(connectionHandler.getClientName(), "SERVER", requestId, Action.PINGSERVERANSWERED, "PingingServerAnswered");
                    pingPacket.setRequestId(packet.getRequestId());
                    try {
                        send(pingPacket, false, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                        statsManager.addErrorsThisSession(1);
                    }
                }
                break;
            case CONNECTEDCLIENTS:
                if (!destinationCheck) {
                    connectionHandler.getOtherConnectedClients().clear();
                    connectionHandler.getOtherConnectedClients().addAll(data);
                }
                break;
            default:
                // Received unknown Packet from Server (Logging into LogFile atm)
                loggingManager.logIntoFile(Level.INFO, "Received unknown Packet from Server: " + packet);
                break;
        }

        // Your additional packet handling code goes here
    }

    public void requestClientListUpdate() {
        if (connectionHandler.isClientListUpdateRequested()) return;
        connectionHandler.setClientListUpdateRequested(true);
        try {
            sendAsync(new Packet(connectionHandler.getClientName(), connectionHandler.getClientName(), Action.CONNECTEDCLIENTS, "RequestingConnectedClients"), false, true)
                    .thenAccept(response -> {
                        if (response != null && response.getAction() == Action.CONNECTEDCLIENTS) {
                            connectionHandler.getOtherConnectedClients().clear();
                            connectionHandler.getOtherConnectedClients().addAll(response.getData());
                        }
                        connectionHandler.setClientListUpdateRequested(false);
                    });
        } catch (IOException e) {
            e.printStackTrace();
            statsManager.addErrorsThisSession(1);
        }
    }

    public CompletableFuture<Packet> sendAsync(Packet packet, boolean callSendEvent, boolean force, int maxAttempts, int retryIntervalSeconds) throws IOException {
        CompletableFuture<Packet> responseFuture = new CompletableFuture<>();
        // Generate a unique ID for this request
        UUID requestId = ((packet.getRequestId() == null || packet.getRequestId().isEmpty()) ? UUID.randomUUID() : UUID.fromString(packet.getRequestId()));

        // Register a response handler for this request ID
        responseHandlers.put(requestId, responseFuture);

        // Add the request ID to the packet so the server can identify it
        packet.setRequestId(requestId.toString());

        // Send the packet
        send(packet, callSendEvent, force);

        retryAsync(maxAttempts, retryIntervalSeconds, requestId, responseFuture);

        return responseFuture;
    }

    public CompletableFuture<Packet> sendAsync(Packet packet, boolean callSendEvent, boolean force) throws IOException {
        return sendAsync(packet, callSendEvent, force, 15, 1);
    }

    private void retryAsync(int attemptsLeft, int retryIntervalSeconds, UUID requestId, CompletableFuture<Packet> responseFuture) {
        if (attemptsLeft > 0) {
            CompletableFuture.delayedExecutor(retryIntervalSeconds, TimeUnit.SECONDS).execute(() -> {
                Packet responsePacket = responseHandlers.get(requestId).join();
                if (responsePacket != null) {
                    // Response received, complete the future
                    responseFuture.complete(responsePacket);
                } else {
                    // Retry the request
                    retryAsync(attemptsLeft - 1, retryIntervalSeconds, requestId, responseFuture);
                }
            });
        } else {
            // No response received within the maximum attempts
            responseFuture.completeExceptionally(new RuntimeException("Request timed out"));
        }
    }

    // Method to register a custom response handler
    public UUID registerResponseHandler(UUID requestId, Consumer<Packet> responseHandler, Function<Throwable, Void> exceptionHandler) {
        if (responseHandlers.containsKey(requestId)) return requestId;
        if (requestId == null) {
            requestId = UUID.randomUUID();
        }
        responseHandlers.put(requestId, new CompletableFuture<>());

        // Add a custom response handler
        if (exceptionHandler == null) {
            responseHandlers.get(requestId).thenAccept(responseHandler);
        } else {
            responseHandlers.get(requestId).thenAccept(responseHandler).exceptionally(exceptionHandler);
        }
        return requestId;
    }

    // Method to register a custom response handler
    public UUID registerCustomResponseHandler(UUID requestId, Consumer<CustomPacket> responseHandler, Function<Throwable, Void> exceptionHandler) {
        if (customResponseHandlers.containsKey(requestId)) return requestId;
        if (requestId == null) {
            requestId = UUID.randomUUID();
        }
        customResponseHandlers.put(requestId, new CompletableFuture<>());

        // Add a custom response handler
        if (exceptionHandler == null) {
            customResponseHandlers.get(requestId).thenAccept(responseHandler);
        } else {
            customResponseHandlers.get(requestId).thenAccept(responseHandler).exceptionally(exceptionHandler);
        }
        return requestId;
    }

    // Method to unregister a response handler
    public void unregisterResponseHandler(UUID requestId) {
        responseHandlers.get(requestId).cancel(true);
        responseHandlers.remove(requestId);
    }
}
