package de.happybavarian07.adminpanel.sanctions;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.profile.PlayerProfile;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * One entry of the server's ban list.
 *
 * @param target  the banned name, the UUID text of a profile without a name, or the banned IP address
 * @param uuid    the UUID of the banned profile, {@code null} for IP bans and when the server does not tell
 * @param ip      whether the entry is in the IP ban list
 * @param reason  why the target was banned, may be empty
 * @param source  who banned the target
 * @param created when the ban was made, in epoch milliseconds, {@code 0} if unknown
 * @param expires when the ban ends, in epoch milliseconds, {@code 0} for a permanent ban
 */
public record BanInfo(String target, UUID uuid, boolean ip, String reason, String source, long created, long expires) {
    /**
     * Whether the ban never ends.
     */
    public boolean permanent() {
        return expires <= 0;
    }

    /**
     * How long the ban lasts from {@code now}.
     *
     * @return the remaining time in milliseconds, {@code 0} if the ban is permanent or already over
     */
    public long remainingMillis(long now) {
        return expires <= 0 ? 0 : Math.max(0, expires - now);
    }

    /**
     * Reads the name and IP ban list of the server, newest ban first.
     */
    public static List<BanInfo> readAll() {
        List<BanInfo> all = new ArrayList<>();
        collect(all, Bukkit.getBanList(BanList.Type.NAME), false);
        collect(all, Bukkit.getBanList(BanList.Type.IP), true);
        all.sort(Comparator.comparingLong(BanInfo::created).reversed());
        return all;
    }

    private static void collect(List<BanInfo> into, BanList<?> banList, boolean ip) {
        for (BanEntry<?> entry : banList.getBanEntries()) {
            BanInfo info = of(entry, ip);
            if (info != null) into.add(info);
        }
    }

    private static BanInfo of(BanEntry<?> entry, boolean ip) {
        String target = null;
        UUID uuid = null;
        try {
            Object banTarget = entry.getBanTarget();
            if (banTarget instanceof PlayerProfile profile) {
                uuid = profile.getUniqueId();
                target = profile.getName() != null ? profile.getName() : uuid != null ? uuid.toString() : null;
            } else if (banTarget instanceof InetAddress address) {
                target = address.getHostAddress();
            } else if (banTarget != null) {
                target = String.valueOf(banTarget);
            }
        } catch (NoSuchMethodError oldServer) {
            target = null;
        }
        if (target == null) target = entry.getTarget();
        if (target == null) return null;
        Date created = entry.getCreated();
        Date expiration = entry.getExpiration();
        return new BanInfo(target, uuid, ip, entry.getReason() == null ? "" : entry.getReason(),
                entry.getSource() == null ? "" : entry.getSource(),
                created == null ? 0 : created.getTime(), expiration == null ? 0 : expiration.getTime());
    }
}
