package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.coolstufflib.jpa.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface PlayerPermissionRepository extends Repository<PlayerPermission, String> {
    List<PlayerPermission> findByPlayerUUID(UUID playerUUID);

    /** Entries pointing at a permission, for example every membership of a group ({@code group.<name>}). */
    List<PlayerPermission> findByPermission(String permission);

    /** Temporary entries whose end lies between the two times, both included. */
    List<PlayerPermission> findByExpiresAtBetween(long from, long to);

    default void deleteAllByPlayerUUID(UUID playerUUID) {
        for (PlayerPermission permission : findByPlayerUUID(playerUUID)) {
            delete(permission);
        }
    }
}
