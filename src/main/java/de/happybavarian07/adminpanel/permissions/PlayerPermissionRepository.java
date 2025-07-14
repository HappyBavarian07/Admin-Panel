package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.coolstufflib.jpa.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerPermissionRepository extends Repository<PlayerPermission, String> {
    List<PlayerPermission> findByPlayerUUIDAndValue(UUID playerUUID, boolean value);

    List<PlayerPermission> findByPlayerUUID(UUID playerUUID);

    List<PlayerPermission> findByPlayerUUIDAndPermission(UUID playerUUID, String permission);

    default void deleteAllByPlayerUUID(UUID playerUUID) {
        List<PlayerPermission> permissions = findByPlayerUUID(playerUUID);
        for (PlayerPermission permission : permissions) {
            delete(permission);
        }
    }

    default void deleteById(String entryID) {
        Optional<PlayerPermission> permission = findById(entryID);
        permission.ifPresent(this::delete);
    }

    int countEntriesByUUID(UUID playerUUID);

    long countDistinctPlayers();

    long countDistinctPermissions();

    long countByValue(boolean value);

    long countByPermissionPattern(String permissionPattern);

    long countPlayersWithPermission(String permission);

    long countByPlayerUUIDAndValue(UUID playerUUID, boolean value);

    boolean existsByPlayerUUIDAndPermission(UUID playerUUID, String permission);

    void deleteByPlayerUUIDAndValue(UUID playerUUID, boolean value);

    void deleteByValue(boolean value);

    List<PlayerPermission> findByValue(boolean value);

    List<PlayerPermission> findByPermissionLike(String permissionPattern);

    long countTruePermissions();

    long countFalsePermissions();

    long countPermissionsStartingWith(String prefix);
}
