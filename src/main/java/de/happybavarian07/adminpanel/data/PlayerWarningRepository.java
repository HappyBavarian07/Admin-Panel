package de.happybavarian07.adminpanel.data;

import de.happybavarian07.coolstufflib.jpa.repository.Repository;

import java.util.List;

public interface PlayerWarningRepository extends Repository<PlayerWarning, Long> {
    List<PlayerWarning> findByPlayerUuid(String playerUuid);

    List<PlayerWarning> findByStatus(String status);
}
