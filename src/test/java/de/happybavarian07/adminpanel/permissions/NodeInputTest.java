package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.adminpanel.permissions.engine.Node;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NodeInputTest {
    @Test
    void wordsMayComeInAnyOrder() {
        Node node = NodeInput.parseLine("Essentials.Fly false 2h world=nether", 1000);
        assertEquals(new Node("essentials.fly", false, 1000 + 2 * 3600_000L, Map.of("world", "nether")), node);
    }

    @Test
    void aPermissionAloneIsAPermanentGrant() {
        assertEquals(Node.of("kit.vip", true), NodeInput.parseLine("kit.vip", 5));
    }

    @Test
    void permanentEndsNothing() {
        assertEquals(0, NodeInput.parseLine("a permanent", 5).expiresAt());
    }

    @Test
    void unknownWordsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> NodeInput.parseLine("a nonsense", 5));
    }
}
