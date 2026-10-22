package de.happybavarian07.adminpanel.data;

import de.happybavarian07.coolstufflib.jpa.repository.Repository;

import java.util.List;

public interface PlayerWarningRepository extends Repository<PlayerWarning, Long> {
    List<PlayerWarning> findByPlayerUuid(String playerUuid);

    List<PlayerWarning> findByStatus(String status);

    /** Warnings with the status whose expiry time lies between the two times, both included. */
    List<PlayerWarning> findByStatusAndExpiresAtBetween(String status, long from, long to);
}
