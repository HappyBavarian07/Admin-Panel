package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.adminpanel.permissions.engine.GroupData;
import de.happybavarian07.adminpanel.permissions.engine.Node;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Where users, groups and their entries are kept. Calls may block, so {@link PermissionData} runs them on its own thread. */
public interface PermissionStore {
    /** An entry the sweep removed because it ended. {@code owner} is {@code user:<uuid>} or {@code group:<name>}. */
    record Expired(String owner, Node node) {
    }

    List<Node> loadUser(UUID user);

    void saveUserNode(UUID user, Node node);

    void deleteUserNode(UUID user, Node node);

    void deleteUser(UUID user);

    /** @return every user that has at least one entry */
    Set<UUID> knownUsers();

    List<GroupData> loadGroups();

    /** Saves name, display name, weight, prefix and suffix of a group; the entries are saved one by one. */
    void saveGroup(GroupData group);

    void saveGroupNode(String group, Node node);

    void deleteGroupNode(String group, Node node);

    /** Removes the group, its entries and every {@code group.<name>} entry of users and other groups. */
    void deleteGroup(String group);

    /** Removes all entries that ended before {@code now} and returns them. */
    List<Expired> removeExpired(long now);
}
