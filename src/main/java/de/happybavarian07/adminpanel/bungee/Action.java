package de.happybavarian07.adminpanel.bungee;/*
 * @Author HappyBavarian07
 * @Date 11.10.2022 | 16:11
 */

public enum Action {
    SENDTOSERVER,
    SENDTOALL,
    SENDTOCLIENT,
    SENDPERMISSIONS,
    SENDPLAYERDATA,
    PINGSERVER,
    PINGCLIENT,
    PINGSERVERANSWERED,
    PINGCLIENTANSWERED,
    CONNECTEDCLIENTS,
    SENDCUSTOMMAP,
    NOACTION;

    public static Action fromString(String s) {
        if(!contains(s)) return Action.NOACTION;

        for(Action a : values()) {
            if(a.name().equals(s.toUpperCase())) return a;
        }
        return Action.NOACTION;
    }

    private static boolean contains(String s) {
        for(Action a : values()) {
            if(a.name().equals(s.toUpperCase())) return true;
        }
        return false;
    }
}
