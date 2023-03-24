package de.happybavarian07.adminpanel.bungee;/*
 * @Author HappyBavarian07
 * @Date 03.09.2022 | 11:15
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BungeeTestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // /bungeetest <text|hello> <text>
        if(args.length >= 1) {
            switch (args[0]) {
                case "text":
                    if (!(sender instanceof Player)) return false;
                    args[0] = "";
                    String text = Utils.arrayToString(args);
                    AdminPanelMain.getPlugin().getBungeeUtils().sendDataToChannel("BroadcastServerWide", text);
                    sender.sendMessage("Sending Message to Bungee Cord!");
                    break;
                case "hello":
                    if (sender instanceof Player) return false;
                    sender.sendMessage("Requesting Server Ping!");
                    sender.sendMessage("Response from Server: " + AdminPanelMain.getPlugin().getDataClient().pingServer(AdminPanelMain.getPlugin().getDataClient().getCheckConnectionThread()));
                    break;
                case "reconnect":
                    sender.sendMessage("Reconnecting to AdminPanel Bungee Server!");
                    try {
                        AdminPanelMain.getPlugin().getDataClient().reconnect(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "test":
                    //AdminPanelMain.getPlugin().getDataClient().sendDataToAllClients("The Data Transfer Test to Server", Serialization.serialize(new DataClient.TestClass("TheWHAAT2223", 2323213)));
                    System.out.println("The Data Transfer begun to Server!");
                case "perms":
                    //if (!(sender instanceof Player)) return false;
                    Map<String, Boolean> perms = new HashMap<>();
                    perms.put("test.test.test.permission", true);
                    perms.put("second.permission.in.list", false);
                    perms.put("and.the.third.one", false);
                    AdminPanelMain.getPlugin().getDataClientUtils().sendPlayerPermissions(UUID.fromString("0c069d0e-5778-4d51-8929-6b2f69b475c0"), perms, "null");
                case "testreportFeature":
                    //AdminPanelMain.getAPI().reportBugToDiscord(UUID.fromString("0c069d0e-5778-4d51-8929-6b2f69b475c0"), "This is a test");
                    //System.out.println("Args:" + Arrays.toString(args));
                    List<String> message = new ArrayList<>(Arrays.asList(args));
                    message.remove(0);
                    //System.out.println("Args formatted: " + message);
                    //System.out.println("Args joined: " + String.join(" ", message));
                    int response = AdminPanelMain.getAPI().reportBugToDiscord(UUID.fromString("0c069d0e-5778-4d51-8929-6b2f69b475c0"), String.join(" ", message));
                    if(response == -2) System.out.println("Cooldown!");
                    else if (response == -1) System.out.println("Geht nicht!");
                    else if (response == 0) System.out.println("Erfolgreich!");
                case "backuptest":
                    AdminPanelMain.getPlugin().getBackupManager().startBackup("ConfigBackup");
                case "loadbackup":
                    AdminPanelMain.getPlugin().getBackupManager().loadBackup(args[1], Integer.parseInt(args[2]));
                /*case "testnewsystem":
                    NewDataClient dataClient = AdminPanelMain.getPlugin().getDataClient();
                    dataClient.send(new Message(dataClient.getClientName(), "TheNewSystem", Action.SENDTOSERVER, "Data23232", "LÖTKJSE").toStringArray());*/

            }
        } else {
            sender.sendMessage("Zu wenig Argumente!");
        }
        return true;
    }
}
