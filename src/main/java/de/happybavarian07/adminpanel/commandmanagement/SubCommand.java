package de.happybavarian07.adminpanel.commandmanagement;
/*
 * @Author HappyBavarian07
 * @Date 05.10.2021 | 17:28
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.LanguageManager;
import de.happybavarian07.adminpanel.main.Placeholder;
import de.happybavarian07.adminpanel.main.PlaceholderType;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@CommandData
public abstract class SubCommand {
    protected AdminPanelMain plugin = AdminPanelMain.getPlugin();
    protected LanguageManager lgm = plugin.getLanguageManager();
    /*
    /<command> <subcommand> args[0] args[1]
     */

    public SubCommand() {

    }

    public boolean isPlayerRequired(SubCommand sub) {
        if(!sub.getClass().isAnnotationPresent(CommandData.class)) return false;
        return sub.getClass().getAnnotation(CommandData.class).playerRequired();
    }

    public abstract boolean onPlayerCommand(Player player, String[] args);

    public abstract boolean onConsoleCommand(ConsoleCommandSender sender, String[] args);

    public abstract String name();

    public abstract String info();

    public abstract String[] aliases();

    public abstract Map<Integer, String[]> subArgs();

    public abstract String syntax();

    public abstract String permission();

    protected String format(String in, SubCommand cmd) {
        Map<String, Placeholder> placeholders = new HashMap<>();
        placeholders.put("%usage%", new Placeholder("%usage%", cmd.syntax(), PlaceholderType.ALL));
        placeholders.put("%description%", new Placeholder("%description%", cmd.info(), PlaceholderType.ALL));
        placeholders.put("%name%", new Placeholder("%name%", cmd.name(), PlaceholderType.ALL));
        placeholders.put("%permission%", new Placeholder("%permission%", cmd.permission(), PlaceholderType.ALL));
        placeholders.put("%aliases%", new Placeholder("%aliases%", cmd.aliases(), PlaceholderType.ALL));

        return lgm.replacePlaceholders(in, placeholders);
    }
}
