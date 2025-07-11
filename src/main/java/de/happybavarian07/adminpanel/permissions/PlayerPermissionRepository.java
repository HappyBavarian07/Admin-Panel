package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.adminpanel.mysql.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerPermissionRepository extends Repository<PlayerPermission, String> {
    // Method to get all permissions for a player by their UUID having the value true or false
    List<PlayerPermission> findByPlayerUUIDAndValue(UUID playerUUID, boolean value);

    // Get all permissions for a player by UUID
    List<PlayerPermission> findByPlayerUUID(UUID playerUUID);

    // Find a specific permission entry for a player
    List<PlayerPermission> findByPlayerUUIDAndPermission(UUID playerUUID, String permission);

    // Delete all permissions for a player
    default void deleteAllByPlayerUUID(UUID playerUUID) {
        List<PlayerPermission> permissions = findByPlayerUUID(playerUUID);
        for (PlayerPermission permission : permissions) {
            delete(permission);
        }
    }

    // Delete by entry id
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
