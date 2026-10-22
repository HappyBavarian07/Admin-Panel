package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.adminpanel.permissions.engine.GroupData;
import de.happybavarian07.adminpanel.permissions.engine.Node;
import de.happybavarian07.adminpanel.permissions.engine.Resolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionDataTest {
    private final MemoryPermissionStore store = new MemoryPermissionStore();
    private final AtomicLong clock = new AtomicLong(1000);
    private PermissionData data;
    private final UUID steve = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        data = new PermissionData(store, Runnable::run, clock::get, (message, error) -> {
            throw new AssertionError(message, error);
        });
        data.load();
    }

    @Test
    void theDefaultGroupExistsAndCannotBeDeleted() {
        assertNotNull(data.group("default"));
        assertTrue(store.groups.containsKey("default"));
        assertFalse(data.deleteGroup("default"));
    }

    @Test
    void groupNamesAreCheckedAndUnique() {
        assertTrue(data.createGroup("Admin"));
        assertFalse(data.createGroup("admin"));
        assertFalse(data.createGroup("bad name"));
        assertFalse(data.createGroup(""));
        assertEquals(List.of("admin", "default"), data.groups().stream().map(GroupData::name).sorted().toList());
    }

    @Test
    void groupSettingsAreKeptAndSaved() {
        data.createGroup("vip");
        data.setWeight("vip", 40);
        data.setPrefix("vip", "[VIP] ");
        data.setSuffix("vip", "*");
        data.setDisplayName("vip", "VIP");
        GroupData group = data.group("vip");
        assertEquals(40, group.weight());
        assertEquals("[VIP] ", store.groups.get("vip").prefix());
        assertEquals("VIP", store.groups.get("vip").displayName());
        assertEquals("*", group.suffix());
    }

    @Test
    void groupEntriesAreSetReplacedAndRemoved() {
        data.createGroup("vip");
        assertTrue(data.setGroupNode("vip", Node.of("fly", true)));
        assertTrue(data.setGroupNode("vip", Node.of("fly", false)));
        assertEquals(1, data.group("vip").nodes().size());
        assertEquals(false, store.groupNodes.get("vip").get("fly|").value());
        assertFalse(data.setGroupNode("ghost", Node.of("fly", true)));
        assertTrue(data.unsetGroupNode("vip", "fly", Map.of()));
        assertFalse(data.unsetGroupNode("vip", "fly", Map.of()));
        assertTrue(store.groupNodes.get("vip").isEmpty());
    }

    @Test
    void aGroupCannotInheritItselfOrItsOwnChild() {
        data.createGroup("a");
        data.createGroup("b");
        data.createGroup("c");
        assertFalse(data.setGroupNode("a", Node.group("a")));
        assertTrue(data.setGroupNode("a", Node.group("b")));
        assertTrue(data.setGroupNode("b", Node.group("c")));
        assertFalse(data.setGroupNode("c", Node.group("a")));
        assertTrue(data.canInherit("a", "c"));
        assertFalse(data.canInherit("c", "a"));
    }

    @Test
    void deletingAGroupRemovesItFromParentsAndMembers() {
        data.createGroup("child");
        data.createGroup("parent");
        data.setGroupNode("child", Node.group("parent"));
        data.setUserNode(steve, Node.group("parent"));
        assertTrue(data.deleteGroup("parent"));
        assertNull(data.group("parent"));
        assertTrue(data.group("child").nodes().isEmpty());
        assertTrue(data.userNodes(steve).isEmpty());
        assertFalse(store.groups.containsKey("parent"));
    }

    @Test
    void userEntriesAreSavedReplacedAndCleared() {
        data.setUserNode(steve, Node.of("build", true));
        data.setUserNode(steve, Node.of("build", false));
        data.setUserNode(steve, new Node("build", true, 0, Map.of("world", "nether")));
        assertEquals(2, data.userNodes(steve).size());
        assertEquals(2, store.users.get(steve).size());
        assertTrue(data.unsetUserNode(steve, "build", Map.of("world", "nether")));
        assertEquals(1, data.userNodes(steve).size());
        data.clearUser(steve);
        assertTrue(data.userNodes(steve).isEmpty());
        assertFalse(store.users.containsKey(steve));
    }

    @Test
    void resolvingUsesGroupsInheritanceAndTheClock() {
        data.createGroup("vip");
        data.setGroupNode("vip", Node.of("kit.vip", true));
        data.setUserNode(steve, new Node("group.vip", true, 5000, Map.of()));
        Resolution during = data.resolve(steve, Map.of());
        assertEquals(true, during.check("kit.vip"));
        clock.set(6000);
        assertNull(data.resolve(steve, Map.of()).check("kit.vip"));
    }

    @Test
    void sweepRemovesEndedEntriesFromMemoryAndStore() {
        data.createGroup("vip");
        data.setGroupNode("vip", new Node("temp.group", true, 2000, Map.of()));
        data.setUserNode(steve, new Node("temp.user", true, 2000, Map.of()));
        data.setUserNode(steve, Node.of("keep", true));
        clock.set(3000);
        List<PermissionStore.Expired> removed = data.sweep();
        assertEquals(2, removed.size());
        assertEquals(List.of(Node.of("keep", true)), data.userNodes(steve));
        assertTrue(data.group("vip").nodes().isEmpty());
        assertTrue(store.users.get(steve).containsKey("keep|"));
    }

    @Test
    void membersOfALoadedGroupAreListed() {
        data.createGroup("vip");
        data.setUserNode(steve, Node.group("vip"));
        assertEquals(Map.of(steve, Node.group("vip")), data.loadedMembers("vip"));
    }
}
