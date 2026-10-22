package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.coolstufflib.jpa.repository.Repository;

import java.util.List;

public interface GroupNodeRepository extends Repository<GroupNodeEntity, String> {
    List<GroupNodeEntity> findByGroupName(String groupName);

    /** Entries pointing at a permission, for example every group that has the parent {@code group.<name>}. */
    List<GroupNodeEntity> findByPermission(String permission);

    /** Temporary entries whose end lies between the two times, both included. */
    List<GroupNodeEntity> findByExpiresAtBetween(long from, long to);

    void deleteByGroupName(String groupName);
}
