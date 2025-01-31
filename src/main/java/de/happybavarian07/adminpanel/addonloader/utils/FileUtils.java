package de.happybavarian07.adminpanel.addonloader.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class FileUtils {

    @Nullable
    public static <T> Class<? extends T> findClass(@NotNull final File file,
                                                   @NotNull final Class<T> clazz) throws IOException, ClassNotFoundException {
        if (!file.exists()) {
            //System.out.println("Test 1 File doesnt exist!");
            return null;
        }

        final URL jar = file.toURI().toURL();
        final URLClassLoader loader = new URLClassLoader(new URL[]{jar}, clazz.getClassLoader());
        final List<String> matches = new ArrayList<>();
        final List<Class<? extends T>> classes = new ArrayList<>();

        try (final JarInputStream stream = new JarInputStream(jar.openStream())) {
            JarEntry entry;
            while ((entry = stream.getNextJarEntry()) != null) {
                final String name = entry.getName();
                if (!name.endsWith(".class")) {
                    continue;
                }

                matches.add(name.substring(0, name.lastIndexOf('.')).replace('/', '.'));
                //System.out.println("Matches: " + matches);
            }

            for (final String match : matches) {
                try {
                    //System.out.println("Match: " + match);
                    final Class<?> loaded = loader.loadClass(match);
                    //System.out.println("Loaded Class: " + loaded + " | " + clazz.isAssignableFrom(loaded));
                    if (clazz.isAssignableFrom(loaded)) {
                        classes.add(loaded.asSubclass(clazz));
                    }
                } catch (final NoClassDefFoundError ignored) {
                }
            }
        }
        if (classes.isEmpty()) {
            loader.close();
            //System.out.println("Jar doesnt contain class");
            return null;
        }
        return classes.get(0);
    }
}