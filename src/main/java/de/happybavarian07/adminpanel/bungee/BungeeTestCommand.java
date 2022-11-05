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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
                    //AdminPanelMain.getPlugin().getDataClient().sayHello();
                    break;
                case "reconnect":
                    sender.sendMessage("Reconnecting to AdminPanel Bungee Server!");
                    AdminPanelMain.getPlugin().getDataClient().reconnect();
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
