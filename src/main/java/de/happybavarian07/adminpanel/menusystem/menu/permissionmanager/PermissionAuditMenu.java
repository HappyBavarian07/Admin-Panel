package de.happybavarian07.adminpanel.menusystem.menu.permissionmanager;

import de.happybavarian07.adminpanel.journal.AdminJournal;
import de.happybavarian07.adminpanel.menusystem.menu.JournalMenu;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;

/** The journal limited to permission and group changes; the search finds a player, a group or a permission. */
public class PermissionAuditMenu extends JournalMenu {
    public PermissionAuditMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu, "PermissionManager.Audit",
                action -> AdminJournal.category(action.getAction()).equals("Permissions"), "AdminPanel.Permissions.Audit");
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PermissionAuditMenu";
    }
}
