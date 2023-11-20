package de.happybavarian07.adminpanel.syncing.utils;

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
        try {
            return valueOf(s.toUpperCase().trim());
        } catch (IllegalArgumentException var2) {
            return NOACTION;
        }
    }

    private static boolean contains(String s) {
        try {
            valueOf(s.toUpperCase().trim());
            return true;
        } catch (IllegalArgumentException var2) {
            return false;
        }
    }
}
    