package de.happybavarian07.adminpanel.menusystem.menu;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.startup.Startup;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Tells admins why a feature does not work instead of failing on a missing object. A feature is a startup stage; the
 * reason is what the stage recorded.
 */
public final class FeatureGate {
    private FeatureGate() {
    }

    /**
     * @return whether the stage failed or was skipped; a stage that is off on purpose is not broken
     */
    public static boolean broken(String stage) {
        Startup.Result result = AdminPanelMain.getPlugin().getStartup().result(stage);
        return result != null && (result.status() == Startup.Status.FAILED || result.status() == Startup.Status.SKIPPED);
    }

    public static void sendUnavailable(CommandSender to, String stage) {
        LanguageManager lgm = AdminPanelMain.getPlugin().getLanguageManager();
        Startup.Result result = AdminPanelMain.getPlugin().getStartup().result(stage);
        CommandText.send(lgm, to, "StartupStatus.Unavailable", "%feature%", featureName(lgm, to, stage),
                "%reason%", result == null || result.detail().isEmpty() ? "-" : result.detail());
    }

    /**
     * Puts a barrier item at the slot that shows the reason, and a click repeats it in the chat.
     */
    public static void placeUnavailable(Menu menu, int slot, String stage, Player viewer) {
        LanguageManager lgm = AdminPanelMain.getPlugin().getLanguageManager();
        Startup.Result result = AdminPanelMain.getPlugin().getStartup().result(stage);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%feature%", featureName(lgm, viewer, stage), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%reason%", result == null || result.detail().isEmpty() ? "-" : result.detail(), false);
        menu.registerButton(slot, lgm.getItem("StartupStatus.Unavailable", viewer, true),
                MenuAction.of((p, e) -> sendUnavailable(p, stage)));
    }

    public static String featureName(LanguageManager lgm, CommandSender to, String stage) {
        return CommandText.get(lgm, to, "StartupStatus.Stage." + stage);
    }
}
