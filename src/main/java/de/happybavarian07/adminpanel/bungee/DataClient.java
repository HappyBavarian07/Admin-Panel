package de.happybavarian07.adminpanel.bungee;/*
 * @Author HappyBavarian07
 * @Date 03.09.2022 | 14:53
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DataClient {
    private final String hostName;
    private final int port;
    private final String clientName;
    private final Map<String, Boolean> ready = new HashMap<>();
    private Socket serverSocket;
    private boolean connected = false;
    private PrintWriter serverOutputStream;
    private BufferedReader serverInputStream;
    private DataClientUtils dataClientUtils;

    public DataClient(String hostName, int port, String clientName) {
        this.hostName = hostName;
        this.port = port;
        this.clientName = clientName;
    }

    public void connect() {
        // if(dataClientUtils == null) dataClientUtils = AdminPanelMain.getPlugin().getDataClientUtils();
        if (connected) return;
        try {
            serverSocket = new Socket(hostName, port);
            serverOutputStream = new PrintWriter(serverSocket.getOutputStream(), true);
            serverInputStream = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            String fromServer = "";
            while (Objects.equals(fromServer, "")) {
                fromServer = serverInputStream.readLine();
            }
            if (Objects.equals(fromServer, "SendClientNameToServer")) {
                serverOutputStream.println(clientName.replace(' ', '_'));
            }

            new Thread() {
                @Override
                public void run() {
                    while (connected) {
                        String fromServer = "";
                        while (Objects.equals(fromServer, "")) {
                            try {
                                fromServer = serverInputStream.readLine();
                            } catch (SocketException e) {
                                break;
                            } catch (IOException e) {
                                e.printStackTrace();
                                disconnect();
                                break;
                            }
                        }
                        // Handle
                        //System.out.println("From Server: " + fromServer);
                        if (fromServer == null) continue;
                        if (fromServer.equals("GetReadyForData")) {
                            serverOutputStream.println("ReadyForData");
                        } else if (fromServer.equals("StartingWithDataTransfer")) {
                            List<String> data = new ArrayList<>();
                            String currentData = "";
                            while (!Objects.equals(currentData, "FinishedWithDataTransfer")) {
                                try {
                                    currentData = serverInputStream.readLine();
                                    if (currentData.equals("FinishedWithDataTransfer")) break;
                                    data.add(currentData);
                                } catch (SocketException e) {
                                    break;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    disconnect();
                                    break;
                                }
                            }
                            System.out.println("Data: " + data);
                            if (data.get(0).equals("PlayerPermissionsSend")) {

                            }
                            serverOutputStream.println("ReceivedData");
                        } else if (fromServer.startsWith("Hello " + clientName)) {
                            System.out.println("Server gave Ping back!");
                            //System.out.println(fromServer);
                        } else if (fromServer.equals("DisconnectClient")) {
                            AdminPanelMain.getPlugin().getStartUpLogger().message("Disconnected from Server!");
                            try {
                                if (serverSocket != null && !serverSocket.isClosed())
                                    serverSocket.close();
                            } catch (SocketException e) {
                                return;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            serverSocket = null;
                            serverOutputStream = null;
                            serverInputStream = null;
                            connected = false;
                        } else if (fromServer.equals("ClientCanDisconnect")) {
                            try {
                                if (serverSocket != null && !serverSocket.isClosed())
                                    serverSocket.close();
                            } catch (SocketException e) {
                                return;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            serverSocket = null;
                            serverOutputStream = null;
                            serverInputStream = null;
                            connected = false;
                        } else if (fromServer.equals("ReadyForData")) {
                            ready.replace("readyfordata", true);
                        } else if (fromServer.equals("ReceivedDestination")) {
                            ready.replace("destinationreceived", true);
                        } else if (fromServer.equals("ReceivedData")) {
                            ready.replace("receiveddata", true);
                        }
                    }
                    stop();
                }
            }.start();
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
            return;
        }
        connected = true;
        AdminPanelMain.getPlugin().getStartUpLogger().message("Connected to Server!");
    }

    public int sendDataToServer(String destination, String... data) {
        AtomicInteger response = new AtomicInteger(-1);
        if (serverSocket == null) return response.get();
        new Thread(() -> {
            serverOutputStream.println("GetReadyForData");
            ready.put("readyfordata", false);
            while (!ready.get("readyfordata")) {
                if (ready.get("readyfordata")) {
                    ready.remove("readyfordata");
                    break;
                }
            }

            serverOutputStream.println("Destination:" + destination);
            ready.put("destinationreceived", false);
            while (!ready.get("destinationreceived")) {
                if (ready.get("destinationreceived")) {
                    ready.remove("destinationreceived");
                    break;
                }
            }
            serverOutputStream.println("StartingWithDataTransfer");
            for (String s : data) {
                serverOutputStream.println(s);
            }
            serverOutputStream.println("FinishedWithDataTransfer");
            ready.put("receiveddata", false);
            while (!ready.get("receiveddata")) {
                if (ready.get("receiveddata")) {
                    ready.remove("receiveddata");
                    break;
                }
            }
            response.set(0);
        }).start();


        return response.get();
    }

    public void sendDataToAllClients(String... data) {
        sendDataToServer("EveryClient", data);
    }

    public void reconnect() {
        if (serverSocket != null && serverSocket.isConnected() && !serverSocket.isClosed() || !connected) {
            disconnect();
            String outputServer = "";
            while (!Objects.equals(outputServer, "ClientCanDisconnect")) {
                try {
                    outputServer = serverInputStream.readLine();
                } catch (SocketException e) {
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    disconnect();
                    break;
                }
            }
        }
        connect();
    }

    public void sayHello() {
        serverOutputStream.println("Hello");
    }

    public void disconnect() {
        if (!connected) return;
        AdminPanelMain.getPlugin().getStartUpLogger().message("Disconnected from Server!");
        serverOutputStream.println("ClientDisconnecting");
    }

    public boolean isConnected() {
        return connected;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public String getClientName() {
        return clientName;
    }

    public BufferedReader getServerInputStream() {
        return serverInputStream;
    }

    public PrintWriter getServerOutputStream() {
        return serverOutputStream;
    }

    public Socket getServerSocket() {
        return serverSocket;
    }

    public static class TestClass {
        private String name;
        private int age;

        public TestClass(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
