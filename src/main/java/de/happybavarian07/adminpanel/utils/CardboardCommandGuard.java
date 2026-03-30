package de.happybavarian07.adminpanel.utils;

import de.happybavarian07.adminpanel.utils.dependencyloading.ServerPlatformDetector;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CardboardCommandGuard {
    private static final ThreadLocal<Boolean> EXECUTING = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final int MAX_DEPTH = 2;
    private static final boolean IS_CARDBOARD = ServerPlatformDetector.getPlatformName().equals("Cardboard");

    public static boolean executeGuarded(CommandSender sender, Command command, String label, String[] args,
                                         CommandExecutor executor) {
        if (!IS_CARDBOARD) {
            return executor.execute(sender, command, label, args);
        }

        if (EXECUTING.get()) {
            int depth = DEPTH.get();
            if (depth >= MAX_DEPTH) {
                Bukkit.getLogger().warning(
                        "[AdminPanel] Detected command recursion on Cardboard (depth " + depth +
                                "), breaking loop for command: " + command.getName()
                );
                return true;
            }
            DEPTH.set(depth + 1);
        } else {
            EXECUTING.set(true);
            DEPTH.set(0);
        }

        try {
            return executor.execute(sender, command, label, args);
        } finally {
            int depth = DEPTH.get();
            if (depth == 0) {
                EXECUTING.set(false);
            } else {
                DEPTH.set(depth - 1);
            }
        }
    }

    @FunctionalInterface
    public interface CommandExecutor {
        boolean execute(CommandSender sender, Command command, String label, String[] args);
    }
}

