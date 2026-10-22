package de.happybavarian07.adminpanel.data;

import de.happybavarian07.coolstufflib.jpa.repository.Repository;

public interface AdminActionRepository extends Repository<AdminAction, Long> {
    /** How many rows were created before the time. */
    long countByCreatedAtLessThan(long before);

    /** Deletes the rows created before the time, for the clean up of old entries. */
    void deleteByCreatedAtLessThan(long before);
}
