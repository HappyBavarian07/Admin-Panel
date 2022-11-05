package de.happybavarian07.adminpanel.bungee;/*
 * @Author HappyBavarian07
 * @Date 10.10.2022 | 15:51
 */

import de.happybavarian07.adminpanel.bungee.events.JavaSocketConnectedEvent;
import de.happybavarian07.adminpanel.bungee.events.JavaSocketDisconnectedEvent;
import de.happybavarian07.adminpanel.bungee.events.JavaSocketMessageReceivedEvent;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class NewDataClient {
    private final LinkedBlockingQueue<Object> messages;
    private final String ipAddress;
    private final int port;
    private final Thread messageHandling;
    private ConnectionToServer server;
    private Socket socket;
    volatile private String clientName;
    private boolean enabled;

    public NewDataClient(String IPAddress, int port, String clientName) throws IOException {
        socket = new Socket(IPAddress, port);
        this.ipAddress = IPAddress;
        this.port = port;
        //System.out.println("Socket: " + socket);
        //System.out.println("IP: " + IPAddress);
        //System.out.println("Port: " + port);
        enabled = true;
        // TODO Disable and Disconnect and Reconnect
        //System.out.println("Creating Blocking Queue");
        messages = new LinkedBlockingQueue<>();
        //System.out.println("Creating Server Connection Object");
        server = new ConnectionToServer(socket);
        this.clientName = clientName;

        //System.out.println("Creating Message Handling");
        messageHandling = new Thread(() -> {
            while (true) {
                if (!enabled || socket == null || !socket.isConnected()) continue;
                try {
                    Object message = messages.take();
                    // Do some handling here...
                    if (message instanceof Message) {
                        Message clientMessage = (Message) message;
                        String client = clientMessage.getSenderName();
                        String destination = clientMessage.getDestination();
                        Action action = clientMessage.getAction();
                        List<String> data = clientMessage.getData();
                        // Event Handling Start
                        JavaSocketMessageReceivedEvent receivedEvent = new JavaSocketMessageReceivedEvent(clientMessage);
                        AdminPanelMain.getAPI().callAdminPanelEvent(receivedEvent);
                        if (receivedEvent.isCancelled()) continue;
                        // Event Handling Stop
                        System.out.println("Client Message: " + clientMessage);
                        if (action.equals(Action.SENDPERMISSIONS)) {
                            AdminPanelMain.getPlugin().getDataClientUtils().applyReceivedPermissions(UUID.fromString(data.get(0)), data.get(1));
                        }
                        continue;
                    }
                    if (message instanceof String && ((String) message).startsWith("RegisteredClientName:")) {
                        String objString = (String) message;
                        String[] array = objString.split(":");
                        if (array.length == 2) {
                            System.out.println("Message: " + objString);
                            System.out.println("Array Name: " + array[1]);
                            System.out.println("Name: " + this.clientName);
                            setClientName(array[1]);
                            System.out.println("Name: " + this.clientName);
                            System.out.println("Array Name: " + array[1]);
                            AdminPanelMain.getPlugin().getStartUpLogger()
                                    .dataClientMessage(ChatColor.GREEN, "Client Registered! New Name: '" + this.clientName + "'");
                            continue;
                        }
                    }
                    if (message instanceof String) {
                        String messageString = (String) message;
                        if (messageString.equals("DisconnectingClientFromServer")) {
                            disconnect(false, false);
                        }
                    }
                    System.out.println("Message Received: " + message);
                } catch (InterruptedException | NotAPanelEventException ignored) {
                }
            }
        });

        messageHandling.setDaemon(true);
        messageHandling.start();
        send("NameForClientIs:" + clientName);
        //send(new Message(clientName, "Test/Action", Action.SENDTOSERVER, "Data2", "Data3").toStringArray());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void disconnect(boolean stopThreads, boolean notifyServer) {
        server.disconnect(notifyServer);
        enabled = false;
        if (!stopThreads) messageHandling.suspend();
        else messageHandling.interrupt();
        messages.clear();
        try {
            JavaSocketDisconnectedEvent receivedEvent = new JavaSocketDisconnectedEvent(notifyServer);
            AdminPanelMain.getAPI().callAdminPanelEvent(receivedEvent);
            //if (receivedEvent.isCancelled()) return;
        } catch (NotAPanelEventException e) {
            e.printStackTrace();
        }
    }

    public void connnect() {
        enabled = true;
        try {
            socket = new Socket(ipAddress, port);
            server = new ConnectionToServer(socket);
            messages.clear();
            messageHandling.resume();
        } catch (IOException e) {
            e.printStackTrace();
        }
        send("NameForClientIs:" + clientName);
        try {
            JavaSocketConnectedEvent receivedEvent = new JavaSocketConnectedEvent(ipAddress, port, clientName);
            AdminPanelMain.getAPI().callAdminPanelEvent(receivedEvent);
            //if (receivedEvent.isCancelled()) return;
        } catch (NotAPanelEventException e) {
            e.printStackTrace();
        }
    }

    public void reconnect() {
        disconnect(false, true);
        connnect();
    }

    public void send(Object obj) {
        if(obj instanceof Message) {
            // Event Handling Start
            try {
            JavaSocketMessageReceivedEvent receivedEvent = new JavaSocketMessageReceivedEvent((Message) obj);
                AdminPanelMain.getAPI().callAdminPanelEvent(receivedEvent);
            if (receivedEvent.isCancelled()) return;
            } catch (NotAPanelEventException e) {
                e.printStackTrace();
            }
            // Event Handling Stop
        }
        server.write(obj);
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    private class ConnectionToServer {
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private Socket socket;
        private boolean disabled = false;

        ConnectionToServer(Socket socket) throws IOException {
            //System.out.println("Creating Socket Second Object");
            this.socket = socket;
            //System.out.println("Socket: " + socket + " | Connection: " + socket.isConnected());
            //System.out.println("Socket Input Stream: " + socket.getInputStream());
            //System.out.println("Creating Output Stream");
            out = new ObjectOutputStream(socket.getOutputStream());
            //System.out.println("Creating Input Stream");
            in = new ObjectInputStream(socket.getInputStream());

            //System.out.println("Starting Read Thread");
            Thread read = new Thread(() -> {
                while (!disabled && socket.isConnected()) {
                    try {
                        Object obj = in.readObject();
                        if (obj.getClass().isAssignableFrom(String[].class)) {
                            String[] array = (String[]) obj;
                            Message message = Message.fromStringArray(array);
                            if (message != null && message.getSenderName().equals("SERVER")) {
                                messages.put(message);
                                continue;
                            }
                        }
                        messages.put(obj);
                    } catch (IOException | ClassNotFoundException | InterruptedException e) {
                        //e.printStackTrace();
                        break;
                    }
                }
            });

            read.setDaemon(true);
            read.start();
        }

        public void disconnect(boolean notifyServer) {
            disabled = true;
            try {
                System.out.println("Disconnect Message: " + (notifyServer) + " | " + (socket.isConnected()) + " | " + (socket.isClosed()));
                if (notifyServer && socket.isConnected() && !socket.isClosed())
                    System.out.println("Disconnect Message");
                send("DisconnectingFromServer:" + clientName);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AdminPanelMain.getPlugin().getStartUpLogger()
                    .dataClientMessage(ChatColor.RED, "Client disconnected from Server!");
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            socket = null;
            server = null;
        }

        private void write(Object obj) {
            try {
                out.writeObject(obj);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
