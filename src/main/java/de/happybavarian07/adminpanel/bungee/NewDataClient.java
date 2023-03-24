package de.happybavarian07.adminpanel.bungee;/*
 * @Author HappyBavarian07
 * @Date 10.10.2022 | 15:51
 */

import de.happybavarian07.adminpanel.bungee.events.JavaSocketConnectedEvent;
import de.happybavarian07.adminpanel.bungee.events.JavaSocketDisconnectedEvent;
import de.happybavarian07.adminpanel.bungee.events.JavaSocketMessageReceivedEvent;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.Result;
import de.happybavarian07.adminpanel.utils.StartUpLogger;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class NewDataClient {
    private final StartUpLogger pluginLogger;
    private final LinkedBlockingQueue<Object> messages;
    private final String ipAddress;
    private final int port;
    private final Thread messageHandling;
    private final Map<String, Boolean> booleanMap;
    private final Settings settings;
    private final List<String> namesOfAllClients = new ArrayList<>();
    private final ThreadGroup dataClientThreadGroup;
    private ConnectionToServer server;
    private Socket socket;
    private volatile String clientName;
    private boolean enabled;
    private boolean nameReceived = false;
    private boolean clientListUpdateRequested = false;
    private BukkitRunnable checkConnectionRunnable;
    private Thread checkConnectionThread;

    public NewDataClient(String IPAddress, int port, String clientName) throws IOException {
        // Settings Init
        dataClientThreadGroup = new ThreadGroup("Data Client Threads");
        pluginLogger = AdminPanelMain.getPlugin().getStartUpLogger();
        FileConfiguration pluginConfig = AdminPanelMain.getPlugin().getConfig();
        String configSettingsPath = "Plugin.BungeeSyncSystem.JavaSockets.Settings.";
        /*settings = new Settings(
                pluginConfig.getBoolean(configSettingsPath + "General.Debug", false),
                pluginConfig.getBoolean(configSettingsPath + "Permissions.OverwritePerms", false)
        );*/

        settings = new Settings(
                new File(AdminPanelMain.getPlugin().getDataFolder(), "DataClientSettings.yml")
        );

        socket = new Socket(IPAddress, port);
        booleanMap = new HashMap<>();
        this.ipAddress = IPAddress;
        this.port = port;
        //System.out.println("Socket: " + socket);
        //System.out.println("IP: " + IPAddress);
        //System.out.println("Port: " + port);
        enabled = true;
        //System.out.println("Creating Blocking Queue");
        messages = new LinkedBlockingQueue<>();
        //System.out.println("Creating Server Connection Object");
        server = new ConnectionToServer(socket);
        this.clientName = clientName;

        //System.out.println("Creating Message Handling");
        messageHandling = new Thread(dataClientThreadGroup, () -> {
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
                        //JavaSocketMessageReceivedEvent receivedEvent = new JavaSocketMessageReceivedEvent(clientMessage);
                        //AdminPanelMain.getAPI().callAdminPanelEvent(receivedEvent);
                        //if (receivedEvent.isCancelled()) continue;
                        logIntoFile(Level.INFO, "Client Message Received: " + clientMessage);
                        // Event Handling Stop
                        if (getSettings().isDebugEnabled())
                            pluginLogger.dataClientMessage(ChatColor.BLUE, "Client Message Received: " + clientMessage, false, true);
                        boolean destinationCheck = !destination.equals(clientName) && !destination.equals("null");
                        if (action.equals(Action.SENDPERMISSIONS)) {
                            if (destinationCheck) continue;
                            AdminPanelMain.getPlugin().getDataClientUtils().applyReceivedPermissions(UUID.fromString(data.get(0)), data.get(1));
                        } else if (action.equals(Action.CONNECTEDCLIENTS)) {
                            if (destinationCheck) continue;
                            for (String name : data) {
                                if (namesOfAllClients.contains(name)) continue;
                                namesOfAllClients.add(name);
                            }
                            clientListUpdateRequested = false;
                        } else if (action.equals(Action.SENDCUSTOMMAP)) {
                            if (destinationCheck) continue;
                            AdminPanelMain.getPlugin().getDataClientUtils().receiveCustomMap(data.get(1), data.get(0));
                        }
                        continue;
                    }
                    if (message instanceof String && ((String) message).startsWith("RegisteredClientName:")) {
                        String objString = (String) message;
                        String[] array = objString.split(":");
                        if (array.length == 2) {
                            //System.out.println("Message: " + objString);
                            //System.out.println("Array Name: " + array[1]);
                            //System.out.println("Name: " + this.clientName);
                            setClientName(array[1]);
                            //System.out.println("Name: " + this.clientName);
                            //System.out.println("Array Name: " + array[1]);
                            AdminPanelMain.getPlugin().getStartUpLogger()
                                    .dataClientMessage(ChatColor.GREEN, "Client Registered! New Name: '" + this.clientName + "'", true, true);
                            nameReceived = true;
                            continue;
                        }
                    }
                    if (message instanceof String) {
                        String messageString = (String) message;
                        if (messageString.equals("DisconnectingClientFromServer")) {
                            disconnect(false, false);
                        }
                    }
                    logIntoFile(Level.INFO, "Normal Message Received: " + message);
                    //System.out.println("Message Received: " + message);
                } catch (InterruptedException /*| NotAPanelEventException*/ ignored) {
                }
            }
        }, "Message Handling Thread");

        messageHandling.setDaemon(true);
        messageHandling.start();
        send("NameForClientIs:" + clientName, true, true);
        Thread connectedClientsThread = new Thread(dataClientThreadGroup, () -> {
            if (!nameReceived) {
                int loopCounter = 0;
                while (!nameReceived && loopCounter <= 15) {
                    try {
                        //System.out.println("Loop Counter: " + loopCounter);
                        //System.out.println("Name Received: " + nameReceived);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    loopCounter++;
                }
            }
            try {
                send(new Message(clientName, clientName, Action.CONNECTEDCLIENTS, "RequestingConnectedClients"), false, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            logIntoFile(Level.INFO, "Requested Client List updated!");
        }, "Connected Clients Request Thread");
        connectedClientsThread.setDaemon(true);
        connectedClientsThread.start();

        if (getSettings().isCheckConnection() && false) {
            checkConnectionThread = new Thread(dataClientThreadGroup, () -> {
                checkConnectionRunnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        boolean response = pingServer(checkConnectionThread);
                        if (!response) {
                            pluginLogger.dataClientMessage(ChatColor.RED, false, true,
                                    "Client Connection to Server got interrupted or the Server is not responding and noticed by Check Connection Thread!",
                                    "Trying Automatic Reconnect....");
                            boolean reconnectReponse = false;
                            try {
                                reconnectReponse = reconnect(false);
                            } catch (IOException e) {
                                //e.printStackTrace();
                            }
                            try {
                                checkConnectionThread.wait(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            boolean secondPing = pingServer(checkConnectionThread);
                            pluginLogger.dataClientMessage(reconnectReponse ? ChatColor.GREEN : ChatColor.RED,
                                    reconnectReponse ? "Server reconnected. (Server responding? " + secondPing + ")"
                                            : "Automatic Reconnect failed.", false, true);
                            logIntoFile(reconnectReponse ? Level.WARNING : Level.SEVERE,
                                    reconnectReponse ? "Server reconnected. (Server responding? " + secondPing + ")"
                                            : "Automatic Reconnect failed.");
                        }
                        logIntoFile(Level.INFO, "Check Connection Thread checked");
                    }
                };
                checkConnectionRunnable.runTaskTimer(AdminPanelMain.getPlugin(), getSettings().getCheckConnectionTiming() * 20, getSettings().getCheckConnectionTiming() * 20);
            }, "Check Connection Thread");
            checkConnectionThread.setDaemon(true);
            checkConnectionThread.start();
        }
        //send(new Message(clientName, "Test/Action", Action.SENDTOSERVER, "Data2", "Data3").toStringArray());
    }

    public Thread getCheckConnectionThread() {
        return checkConnectionThread;
    }

    public Settings getSettings() {
        return settings;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void disconnect(boolean stopThreads, boolean notifyServer) {
        if (server != null)
            server.disconnect(notifyServer);
        enabled = false;
        if (messageHandling != null)
            if (!stopThreads) messageHandling.suspend();
            else messageHandling.interrupt();
        messages.clear();
        try {
            JavaSocketDisconnectedEvent disconnectedEvent = new JavaSocketDisconnectedEvent(notifyServer);
            AdminPanelMain.getAPI().callAdminPanelEvent(disconnectedEvent);
            //if (disconnectedEvent.isCancelled()) return;
        } catch (NotAPanelEventException e) {
            e.printStackTrace();
        }
    }

    public Message messageFromParams(String clientName, String destination, Action action, String... data) {
        return new Message(clientName, destination, action, data);
    }

    public Message messageFromParams(String clientName, String destination, Action action, List<String> data) {
        return new Message(clientName, destination, action, data);
    }

    public boolean connnect() throws IOException {
        try {
            socket = new Socket(ipAddress, port);
            server = new ConnectionToServer(socket);
            messages.clear();
            messageHandling.resume();
        } catch (IOException e) {
            e.printStackTrace();
            logIntoFile(Level.SEVERE, "IO Exception while connecting to Server: " + e + ":" + e.getMessage());
            return false;
        }
        send("NameForClientIs:" + clientName, true, true);
        requestClientListUpdate();
        logIntoFile(Level.INFO, "Client connected to Server.");
        try {
            JavaSocketConnectedEvent connectedEvent = new JavaSocketConnectedEvent(ipAddress, port, clientName);
            AdminPanelMain.getAPI().callAdminPanelEvent(connectedEvent);
            //if (connectedEvent.isCancelled()) return;
        } catch (NotAPanelEventException e) {
            e.printStackTrace();
            return false;
        }
        enabled = true;
        return true;
    }

    public boolean reconnect(boolean notifyServer) throws IOException {
        disconnect(false, notifyServer);
        return connnect();
    }

    public void logIntoFile(Level record, String stringToLog) {
        if (getSettings().isFileLogging()) {
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(record, stringToLog, getSettings().getFileLoggerPrefix());
        }
    }

    public Result send(Object obj, boolean callSendEvent, boolean force) throws IOException {
        if (getSettings().isDebugEnabled())
            pluginLogger.dataClientMessage(ChatColor.BLUE, true, true,
                    "Object to Send: " + obj,
                    "Is Message: " + (obj instanceof Message));
        if (obj instanceof Message) {
            // Event Handling Start
            try {
                if (callSendEvent) {
                    JavaSocketMessageReceivedEvent sendEvent = new JavaSocketMessageReceivedEvent((Message) obj);
                    AdminPanelMain.getAPI().callAdminPanelEvent(sendEvent);
                    if (sendEvent.isCancelled() && !force) return Result.FAILURE;
                }
                server.write(((Message) obj).toStringArray());
            } catch (NotAPanelEventException e) {
                e.printStackTrace();
                return Result.ERROR;
            }
            // Event Handling Stop
            logIntoFile(Level.INFO, "Message send to Server: " + obj + "(CallSendEvent: " + callSendEvent + ", Force:" + force + ")");
            return Result.SUCCESS;
        }
        server.write(obj);
        logIntoFile(Level.INFO, "Message send to Server: " + obj + "(CallSendEvent: " + callSendEvent + ", Force:" + force + ")");
        return Result.SUCCESS;
    }

    /**
     * Checks if {clientListUpdateRequested} is true.
     * If true, just returns.
     * If false, requests Server to send a updated Client List.
     * This all happens async to the main Thread.
     * So check if clientListUpdateRequested is false before executing this Method and trying to interact with the outdated/updated Client List
     */
    public void requestClientListUpdate() {
        if (clientListUpdateRequested) return;
        clientListUpdateRequested = true;
        Thread connectedClientsThread = new Thread(dataClientThreadGroup, () -> {
            if (!nameReceived) {
                int loopCounter = 0;
                while (!nameReceived && loopCounter <= 15) {
                    //System.out.println("Test1234");
                    try {
                        //System.out.println("Loop Counter: " + loopCounter);
                        //System.out.println("Name Received: " + nameReceived);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        clientListUpdateRequested = false;
                        break;
                    }
                    loopCounter++;
                }
            }
            try {
                send(new Message(clientName, clientName, Action.CONNECTEDCLIENTS, "RequestingConnectedClients"), false, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            logIntoFile(Level.INFO, "Requested Client List updated!");
        }, "Connected Clients Request Thread");
        connectedClientsThread.setDaemon(true);
        connectedClientsThread.start();
    }

    /**
     * It will wait until the Server answered the Ping
     * So if you want to continue running your Programm
     * Make the Method Call Async
     *
     * @return Ping answered
     */
    public boolean pingServer(Thread calledWith) {
        boolean response = false;
        try {
            // TODO Test Ping Method
            if (!enabled || server.disabled) return false;
            if(!socket.isConnected() || socket.isClosed()) return false;
            if (booleanMap.containsKey("ServerPingWaitingForResponse")) return false;
            booleanMap.put("ServerPingWaitingForResponse", false);
            send(new Message(clientName, "SERVER", Action.PINGSERVER, "PingingServer"), true, true);
            int loopCount = 0;
            while (!booleanMap.get("ServerPingWaitingForResponse") && loopCount <= 15) {
                if (booleanMap.get("ServerPingWaitingForResponse")) {
                    response = true;
                    break;
                }
                loopCount++;
                try {
                    calledWith.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            booleanMap.remove("ServerPingWaitingForResponse");
        } catch (Exception e) {
            booleanMap.remove("ServerPingWaitingForResponse");
            return false;
        }
        return response;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public StartUpLogger getPluginLogger() {
        return pluginLogger;
    }

    public List<String> getNamesOfAllClients() {
        return namesOfAllClients;
    }

    public static class Settings {
        // Debug Messages
        private boolean debugEnabled;
        // Overwrite Permission Setting for Permission Sync
        private boolean overwritePermissionsEnabled;
        // Check Connection To Server Boolean
        private boolean checkConnection;
        // Check Connection To Server Timing in Seconds
        private long checkConnectionTiming;
        // Logs into the plugin.log File
        private boolean fileLogging;
        // File Logger Prefix
        private String fileLoggerPrefix;
        // Settings Config
        private File settingsFile;
        private FileConfiguration settingsConfig;

        public Settings(boolean debug, boolean overwritePermissions, boolean checkConnection, long checkConnectionTiming, boolean fileLogging, String fileLoggerPrefix) {
            this.debugEnabled = debug;
            this.overwritePermissionsEnabled = overwritePermissions;
            this.checkConnection = checkConnection;
            this.checkConnectionTiming = checkConnectionTiming;
            this.fileLogging = fileLogging;
            this.fileLoggerPrefix = fileLoggerPrefix;
        }

        public Settings(File settingsFile) {
            saveDefaultConfig();
            this.settingsFile = settingsFile;
            this.settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
            debugEnabled = settingsConfig.getBoolean("Settings.General.Debug");
            overwritePermissionsEnabled = settingsConfig.getBoolean("Settings.Permissions.OverwritePerms");
            checkConnection = settingsConfig.getBoolean("Settings.General.CheckConnection");
            checkConnectionTiming = settingsConfig.getLong("Settings.General.CheckConnectionTiming", 300);
            fileLogging = settingsConfig.getBoolean("Settings.General.FileLogging", false);
            fileLoggerPrefix = settingsConfig.getString("Settings.General.FileLoggerPrefix", "JSBDSS");
        }

        public boolean isCheckConnection() {
            return checkConnection;
        }

        public long getCheckConnectionTiming() {
            return checkConnectionTiming;
        }

        public boolean isDebugEnabled() {
            return debugEnabled;
        }

        public boolean isOverwritePermissionsEnabled() {
            return overwritePermissionsEnabled;
        }

        public boolean isFileLogging() {
            return fileLogging;
        }

        public String getFileLoggerPrefix() {
            return fileLoggerPrefix;
        }

        public FileConfiguration getSettingsConfig() {
            return settingsConfig;
        }

        public File getSettingsFile() {
            return settingsFile;
        }

        public void reloadConfig() {
            if (this.settingsFile == null)
                this.settingsFile = new File(AdminPanelMain.getPlugin().getDataFolder(), "DataClientSettings.yml");

            this.settingsConfig = YamlConfiguration.loadConfiguration(this.settingsFile);

            InputStream defaultStream = AdminPanelMain.getPlugin().getResource("DataClientSettings.yml");
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
                this.settingsConfig.setDefaults(defaultConfig);
            }
        }

        public void saveConfig() {
            if (this.settingsConfig == null || this.settingsFile == null)
                return;

            try {
                settingsConfig.save(settingsFile);
            } catch (IOException e) {
                AdminPanelMain.getPlugin().getLogger().log(Level.SEVERE, "Could not save Config to " + this.settingsFile, e);
            }
        }

        public void saveDefaultConfig() {
            if (this.settingsFile == null)
                this.settingsFile = new File(AdminPanelMain.getPlugin().getDataFolder(), "DataClientSettings.yml");

            if (!this.settingsFile.exists()) {
                AdminPanelMain.getPlugin().saveResource("DataClientSettings.yml", false);
            }
        }

        public void reloadValuesFromConfig() {
            reloadConfig();
            debugEnabled = settingsConfig.getBoolean("Settings.General.Debug");
            overwritePermissionsEnabled = settingsConfig.getBoolean("Settings.Permissions.OverwritePerms");
            checkConnection = settingsConfig.getBoolean("Settings.General.CheckConnection");
            checkConnectionTiming = settingsConfig.getLong("Settings.General.CheckConnectionTiming", 300);
            fileLogging = settingsConfig.getBoolean("Settings.General.CheckConnectionTiming", false);
        }
    }

    private class ConnectionToServer {
        private final ObjectInputStream in;
        private final ObjectOutputStream out;
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
            Thread read = new Thread(dataClientThreadGroup, () -> {
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
                        logIntoFile(Level.INFO, "Reading Message from Server Out: " + obj);
                    } catch (IOException | ClassNotFoundException | InterruptedException e) {
                        //e.printStackTrace();
                        logIntoFile(Level.INFO, "Error while reading Message: " + e + ":" + e.getMessage() + " (Shutting down Read Thread for good)");
                        break;
                    }
                }
            }, "Read Messages Thread");

            read.setDaemon(true);
            read.start();
        }

        public void disconnect(boolean notifyServer) {
            disabled = true;
            try {
                if (getSettings().isDebugEnabled())
                    pluginLogger.dataClientMessage(ChatColor.BLUE, false, true,
                            "Disconnect Message: " + notifyServer,
                            "Server connected: " + socket.isConnected(),
                            "Socket closed: " + (socket.isClosed()));
                if (notifyServer && socket.isConnected() && !socket.isClosed())
                    send("DisconnectingFromServer:" + clientName, true, true);
                socket.close();
            } catch (IOException e) {
                logIntoFile(Level.INFO, "Error while disconnecting: " + e + ":" + e.getMessage());
                e.printStackTrace();
            }
            pluginLogger.dataClientMessage(ChatColor.RED, "Client disconnected from Server!", true, true);
            logIntoFile(Level.INFO, "Client disconnected from Server.");
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            socket = null;
            server = null;
        }

        private void write(Object obj) throws IOException {
            out.writeObject(obj);
        }
    }
}
