package de.happybavarian07.adminpanel.permissions;

import de.happybavarian07.adminpanel.permissions.engine.Node;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionTransferTest {
    private static PermissionData newData() {
        PermissionData data = new PermissionData(new MemoryPermissionStore(), Runnable::run, () -> 1000, (message, error) -> {
            throw new AssertionError(message, error);
        });
        data.load();
        return data;
    }

    @Test
    void exportedDataComesBackUnchanged() throws Exception {
        PermissionData source = newData();
        UUID steve = UUID.randomUUID();
        source.createGroup("vip");
        source.setWeight("vip", 40);
        source.setPrefix("vip", "[VIP] ");
        source.setGroupNode("vip", new Node("kit.vip", true, 0, Map.of("world", "nether")));
        source.createGroup("mod");
        source.setGroupNode("mod", Node.group("vip"));
        source.setUserNode(steve, new Node("group.mod", true, 5000, Map.of()));
        source.setUserNode(steve, Node.of("fly", false));

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(PermissionTransfer.export(source).saveToString());
        PermissionData target = newData();
        PermissionTransfer.Result result = PermissionTransfer.load(target, yaml);

        assertTrue(result.problems().isEmpty(), result.problems().toString());
        assertEquals(source.group("vip"), target.group("vip"));
        assertEquals(source.group("mod").nodes(), target.group("mod").nodes());
        assertEquals(List.copyOf(source.userNodes(steve)).stream().sorted(java.util.Comparator.comparing(Node::permission)).toList(),
                List.copyOf(target.userNodes(steve)).stream().sorted(java.util.Comparator.comparing(Node::permission)).toList());
        assertEquals(3, result.groups());
        assertEquals(1, result.users());
    }

    @Test
    void badEntriesAreReportedAndSkipped() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString("groups:\n  'bad name':\n    weight: 1\nusers:\n  nope:\n    - {permission: a, value: true}\n");
        PermissionTransfer.Result result = PermissionTransfer.load(newData(), yaml);
        assertEquals(2, result.problems().size());
        assertEquals(0, result.entries());
    }
}
