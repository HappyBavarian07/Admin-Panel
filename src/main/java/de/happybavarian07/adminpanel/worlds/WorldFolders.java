package de.happybavarian07.adminpanel.worlds;

import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reads, copies and deletes world folders on disk. Nothing in here touches the Bukkit world list.
 */
public final class WorldFolders {
    private static final Set<String> NOT_COPIED = Set.of("uid.dat", "session.lock");

    private WorldFolders() {
    }

    /**
     * The folders of the container that hold a {@code level.dat} and whose names are not in {@code loadedNames}, sorted
     * by name. Names are compared ignoring the case because the file system does.
     */
    public static List<String> unloaded(File container, Collection<String> loadedNames) {
        File[] children = container.listFiles(File::isDirectory);
        if (children == null) return List.of();
        Set<String> loaded = loadedNames.stream().map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        return Arrays.stream(children)
                .filter(WorldFolders::isWorldFolder)
                .map(File::getName)
                .filter(name -> !loaded.contains(name.toLowerCase(Locale.ROOT)))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public static boolean isWorldFolder(File folder) {
        return new File(folder, "level.dat").isFile();
    }

    /**
     * Worlds that Bukkit created for the nether or the end keep their dimension folder ({@code DIM-1}, {@code DIM1})
     * instead of a {@code region} folder, which is the only hint about the environment in the folder.
     */
    public static World.Environment guessEnvironment(File folder) {
        if (new File(folder, "region").isDirectory()) return World.Environment.NORMAL;
        if (new File(folder, "DIM-1").isDirectory()) return World.Environment.NETHER;
        if (new File(folder, "DIM1").isDirectory()) return World.Environment.THE_END;
        return World.Environment.NORMAL;
    }

    /**
     * Copies a world folder without the files that tie it to the running server ({@code uid.dat}, {@code session.lock}).
     */
    public static void copy(File from, File to) throws IOException {
        Path source = from.toPath();
        Path target = to.toPath();
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir).toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!NOT_COPIED.contains(file.getFileName().toString())) {
                    Files.copy(file, target.resolve(source.relativize(file).toString()), StandardCopyOption.COPY_ATTRIBUTES);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void delete(File folder) throws IOException {
        if (!folder.exists()) return;
        Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) throw exc;
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
