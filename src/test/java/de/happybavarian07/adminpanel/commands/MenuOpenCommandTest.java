package de.happybavarian07.adminpanel.commands;

import de.happybavarian07.adminpanel.commands.subcommands.panels.MenuOpenCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenuOpenCommandTest {
    @Test
    void takesNameAndPermissionAndOneOptionalPlayer() {
        MenuOpenCommand command = new MenuOpenCommand("openpanel", "ServerManagerMenu", "AdminPanel.ServerManager.open", pmu -> null);

        assertEquals("ServerManagerMenu", command.name());
        assertEquals("AdminPanel.ServerManager.open", command.permissionAsString());
        assertFalse(command.autoRegisterPermission());
        assertEquals(1, command.arguments().size());
        assertFalse(command.arguments().get(0).required());
        assertEquals(0, command.minArgs());
        assertEquals(1, command.maxArgs());
    }
}
