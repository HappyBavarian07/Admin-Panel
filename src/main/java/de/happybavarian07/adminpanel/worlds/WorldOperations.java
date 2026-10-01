package de.happybavarian07.adminpanel.worlds;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

/**
 * The world actions behind the world menus. Every action reports what happened as a {@link Result} so the menu can
 * answer with a language message, and nothing here kicks players: they are moved to the spawn of the main world.
 * Copying and deleting folders runs off the main thread, loading and unloading worlds never does.
 */
public final class WorldOperations {
    public enum Result {
        OK, EXISTS, MAIN_WORLD, INVALID, MISSING, FAILED
    }

    /**
     * Everything the create menu collects.
     */
    public record CreateRequest(String name, WorldPreset preset, World.Environment environment, Long seed,
                                Difficulty difficulty, boolean structures, boolean hardcore) {
    }

    private WorldOperations() {
    }

    public static World mainWorld() {
        return Bukkit.getWorlds().get(0);
    }

    public static boolean isMainWorld(World world) {
        return world.equals(mainWorld());
    }

    public static boolean exists(String name) {
        return Bukkit.getWorld(name) != null || folder(name).exists();
    }

    public static File folder(String name) {
        return new File(Bukkit.getWorldContainer(), name);
    }

    public static Result create(CreateRequest request) {
        if (!WorldNames.isValid(request.name())) return Result.INVALID;
        if (exists(request.name())) return Result.EXISTS;
        WorldCreator creator = new WorldCreator(request.name())
                .type(request.preset().type())
                .environment(request.environment())
                .generateStructures(request.structures())
                .hardcore(request.hardcore());
        if (request.seed() != null) creator.seed(request.seed());
        if (!request.preset().generatorSettings().isEmpty()) creator.generatorSettings(request.preset().generatorSettings());
        World world = createWorld(creator);
        if (world == null) return Result.FAILED;
        world.setDifficulty(request.difficulty());
        return Result.OK;
    }

    public static Result load(String name) {
        if (name.isBlank() || !isInsideContainer(name)) return Result.INVALID;
        if (Bukkit.getWorld(name) != null) return Result.EXISTS;
        File folder = folder(name);
        if (!WorldFolders.isWorldFolder(folder)) return Result.MISSING;
        World world = createWorld(new WorldCreator(name).environment(WorldFolders.guessEnvironment(folder)));
        return world == null ? Result.FAILED : Result.OK;
    }

    public static Result unload(World world, boolean save) {
        if (isMainWorld(world)) return Result.MAIN_WORLD;
        evacuate(world);
        return Bukkit.unloadWorld(world, save) ? Result.OK : Result.FAILED;
    }

    public static Result save(World world) {
        try {
            world.save();
            return Result.OK;
        } catch (RuntimeException failed) {
            AdminPanelMain.logError("Could not save the world " + world.getName(), failed);
            return Result.FAILED;
        }
    }

    /**
     * Copies the world under a new name and loads the copy. {@code done} runs on the main thread.
     */
    public static void duplicate(World source, String newName, Consumer<Result> done) {
        if (!WorldNames.isValid(newName)) {
            done.accept(Result.INVALID);
            return;
        }
        if (exists(newName)) {
            done.accept(Result.EXISTS);
            return;
        }
        if (save(source) != Result.OK) {
            done.accept(Result.FAILED);
            return;
        }
        File from = source.getWorldFolder();
        File to = folder(newName);
        Bukkit.getScheduler().runTaskAsynchronously(AdminPanelMain.getPlugin(), () -> {
            boolean copied = true;
            try {
                WorldFolders.copy(from, to);
            } catch (IOException failed) {
                AdminPanelMain.logError("Could not copy the world " + source.getName() + " to " + newName, failed);
                copied = false;
            }
            boolean finalCopied = copied;
            Bukkit.getScheduler().runTask(AdminPanelMain.getPlugin(), () -> done.accept(finalCopied ? load(newName) : Result.FAILED));
        });
    }

    /**
     * Unloads the world, renames its folder and loads it again. A world that cannot be unloaded is left as it was.
     */
    public static Result rename(World world, String newName) {
        if (isMainWorld(world)) return Result.MAIN_WORLD;
        if (!WorldNames.isValid(newName)) return Result.INVALID;
        if (exists(newName)) return Result.EXISTS;
        File from = world.getWorldFolder();
        Result unloaded = unload(world, true);
        if (unloaded != Result.OK) return unloaded;
        try {
            Files.move(from.toPath(), folder(newName).toPath());
        } catch (IOException failed) {
            AdminPanelMain.logError("Could not rename the folder of the world " + world.getName(), failed);
            load(from.getName());
            return Result.FAILED;
        }
        return load(newName);
    }

    /**
     * Unloads the world without saving and deletes its folder. {@code done} runs on the main thread.
     */
    public static void delete(World world, Consumer<Result> done) {
        if (isMainWorld(world)) {
            done.accept(Result.MAIN_WORLD);
            return;
        }
        File folder = world.getWorldFolder();
        Result unloaded = unload(world, false);
        if (unloaded != Result.OK) {
            done.accept(unloaded);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(AdminPanelMain.getPlugin(), () -> {
            boolean deleted = true;
            try {
                WorldFolders.delete(folder);
            } catch (IOException failed) {
                AdminPanelMain.logError("Could not delete the folder of the world " + folder.getName(), failed);
                deleted = false;
            }
            boolean finalDeleted = deleted;
            Bukkit.getScheduler().runTask(AdminPanelMain.getPlugin(), () -> done.accept(finalDeleted ? Result.OK : Result.FAILED));
        });
    }

    private static void evacuate(World world) {
        World main = mainWorld();
        for (Player player : world.getPlayers()) {
            player.teleport(main.getSpawnLocation());
        }
    }

    private static World createWorld(WorldCreator creator) {
        try {
            return creator.createWorld();
        } catch (RuntimeException failed) {
            AdminPanelMain.logError("Could not create the world " + creator.name(), failed);
            return null;
        }
    }

    private static boolean isInsideContainer(String name) {
        try {
            return folder(name).getCanonicalFile().getParentFile().equals(Bukkit.getWorldContainer().getCanonicalFile());
        } catch (IOException unreadable) {
            return false;
        }
    }
}
