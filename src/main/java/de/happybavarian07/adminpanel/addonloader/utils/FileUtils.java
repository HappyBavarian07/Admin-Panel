package de.happybavarian07.adminpanel.addonloader.utils;


import de.happybavarian07.adminpanel.addonloader.loadingutils.AddonClassLoader;
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


    public static <T> Class<? extends T> findClass(@NotNull final File file,
                                                   @NotNull final Class<T> clazz,
                                                   @Nullable final AddonClassLoader addonClassLoader) throws IOException, ClassNotFoundException {
        if (!file.exists()) {
            return null;
        }

        final URL jar = file.toURI().toURL();
        URLClassLoader loader;
        if (addonClassLoader == null)
            loader = new URLClassLoader(new URL[]{jar}, clazz.getClassLoader());
        else
            loader = new URLClassLoader(new URL[]{jar}, addonClassLoader);

        final List<String> matches = new ArrayList<>();
        final List<Class<? extends T>> classes = new ArrayList<>();

        try (final JarInputStream stream = new JarInputStream(jar.openStream())) {
            JarEntry entry;
            while ((entry = stream.getNextJarEntry()) != null) {
                final String name = entry.getName();
                if (!name.endsWith(".class") || name.contains("package-info") || name.contains("module-info")) {
                    continue;
                }

                matches.add(name.substring(0, name.lastIndexOf('.')).replace('/', '.'));
            }

            for (final String match : matches) {
                try {
                    if (match.contains("Addon")) {
                        System.out.println("Addon: " + match);
                    }
                    final Class<?> loaded = Class.forName(match, false, loader);
                    if (match.contains("Addon")) {
                        System.out.println("Loaded: " + loaded);
                    }
                    if (clazz.isAssignableFrom(loaded)) {
                        classes.add((Class<? extends T>) loaded);
                    }
                } catch (final NoClassDefFoundError | IllegalAccessError ignored) {
                }
            }
        }
        if (classes.isEmpty()) {
            if (addonClassLoader == null && !(loader.getParent() instanceof AddonClassLoader))
                loader.close();
            return null;
        }
        return classes.get(0);
    }
}
