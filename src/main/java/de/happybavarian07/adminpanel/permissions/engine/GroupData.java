package de.happybavarian07.adminpanel.permissions.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * A group as the resolver sees it.
 *
 * @param name        the group name, lower case
 * @param displayName shown in menus, may be empty
 * @param weight      higher weights win when two groups disagree
 * @param prefix      chat prefix of members, may be empty
 * @param suffix      chat suffix of members, may be empty
 * @param nodes       the entries of the group, including {@code group.<parent>} entries
 */
public record GroupData(String name, String displayName, int weight, String prefix, String suffix, List<Node> nodes) {
    public GroupData {
        name = name == null ? "" : name.trim().toLowerCase(java.util.Locale.ROOT);
        displayName = displayName == null ? "" : displayName;
        prefix = prefix == null ? "" : prefix;
        suffix = suffix == null ? "" : suffix;
        nodes = nodes == null ? List.of() : List.copyOf(new ArrayList<>(nodes));
    }

    public static GroupData of(String name, int weight, Node... nodes) {
        return new GroupData(name, "", weight, "", "", List.of(nodes));
    }
}
