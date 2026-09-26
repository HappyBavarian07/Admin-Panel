package de.happybavarian07.adminpanel.commands;

import de.happybavarian07.adminpanel.commands.subcommands.addoncommands.*;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddonCommandNamesTest {
    @Test
    void everyAddonCommandHasItsOwnNameAndPermission() {
        List<SubCommand> commands = List.of(new AddonListCommand("addon"), new AddonEnableCommand("addon"),
                new AddonDisableCommand("addon"), new AddonReloadCommand("addon"), new AddonInfoCommand("addon"));

        Map<String, String> permissions = commands.stream()
                .collect(Collectors.toMap(SubCommand::name, SubCommand::permissionAsString));

        assertEquals(Map.of(
                "listaddons", "AdminPanel.AddonManageCMD.List",
                "enable", "AdminPanel.AddonManageCMD.Enable",
                "disable", "AdminPanel.AddonManageCMD.Disable",
                "reload", "AdminPanel.AddonManageCMD.Reload",
                "info", "AdminPanel.AddonManageCMD.Info"), permissions);
    }
}
