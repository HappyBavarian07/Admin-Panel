package de.happybavarian07.addonloader.loadingutils;/*
 * @Author HappyBavarian07
 * @Date 12.01.2022 | 16:56
 */

import de.happybavarian07.addonloader.api.Addon;
import de.happybavarian07.addonloader.utils.FileUtils;
import de.happybavarian07.main.AdminPanelMain;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;

public class AddonLoader {
    private static AdminPanelMain plugin;
    private final Map<File, List<Class<?>>> loadedJarFiles;
    private final File addonFolder;
    private final URLClassLoader urlClassLoader;

    public AddonLoader(File addonFolder) {
        plugin = AdminPanelMain.getPlugin();
        this.addonFolder = addonFolder;
        this.loadedJarFiles = new HashMap<>();
        this.urlClassLoader = new URLClassLoader(new URL[]{}, ClassLoader.getSystemClassLoader());

        if (!addonFolder.isDirectory()) {
            addonFolder.mkdir();
        }
        List<File> jarfiles = findJarFiles(addonFolder);
        for (File jarFile : jarfiles) {
            plugin.getStartUpLogger().coloredMessage(ChatColor.BLUE, jarFile.toString());
            loadedJarFiles.put(jarFile, null);
            //loadLibrary(new File(jarFile.getName()));
        }
    }

    public static <T> Class<? extends T> findClass(@NotNull final File file,
                                                   @NotNull final Class<T> clazz) throws IOException, ClassNotFoundException {
        if (!file.exists()) {
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
                if (name.isEmpty() || !name.endsWith(".class")) {
                    continue;
                }

                matches.add(name.substring(0, name.lastIndexOf('.')).replace('/', '.'));
            }

            for (final String match : matches) {
                try {
                    final Class<?> loaded = loader.loadClass(match);
                    if (clazz.isAssignableFrom(loaded)) {
                        classes.add(loaded.asSubclass(clazz));
                    }
                } catch (final NoClassDefFoundError ignored) {
                }
            }
        }
        if (classes.isEmpty()) {
            loader.close();
            return null;
        }
        return classes.get(0);
    }

    public List<File> findJarFiles(File folder) {
        List<File> jarFiles = new ArrayList<>();
        File[] files = folder.listFiles();
        if (files == null) return jarFiles;
        for (File f : files) {
            if (f.getName().endsWith(".jar")) {
                jarFiles.add(f);
            }
        }
        return jarFiles;
    }

    public List<Class<?>> loadAddon(File addon) throws IOException, ClassNotFoundException {
        if (!addon.exists()) {
            return null;
        }

        final URL jar = addon.toURI().toURL();
        final URLClassLoader loader = new URLClassLoader(new URL[]{jar}, urlClassLoader);
        final List<String> classNames = new ArrayList<>();
        List<Class<?>> classes = new ArrayList<>();

        try (final JarInputStream stream = new JarInputStream(jar.openStream())) {
            JarEntry entry;
            while ((entry = stream.getNextJarEntry()) != null) {
                final String name = entry.getName();
                if (!name.endsWith(".class")) {
                    continue;
                }

                classNames.add(name.substring(0, name.lastIndexOf('.')).replace('/', '.'));
            }

            for (final String names : classNames) {
                try {
                    final Class<?> loaded = loader.loadClass(names);
                    classes.add(loaded);
                } catch (final NoClassDefFoundError ignored) {
                }
            }
        }

        if (classes.isEmpty()) {
            loader.close();
            return null;
        }

        loadedJarFiles.replace(addon, null, classes);
        return classes;
    }

    public Class<? extends Addon> getMainClassOfAddon(File addon) {
        try {
            return FileUtils.findClass(addon, Addon.class);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Adds the supplied Java Archive library to java.class.path. This is benign
     * if the library is already loaded.
     */
    public synchronized void loadLibrary(File jar) {
        try {
            /*We are using reflection here to circumvent encapsulation; addURL is not public*/
            URLClassLoader loader = this.getUrlClassLoader();
            URL url = jar.toURI().toURL();
            /*Disallow if already loaded*/
            for (URL it : loader.getURLs()) {
                if (it.equals(url)) {
                    return;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public URLClassLoader getUrlClassLoader() {
        return urlClassLoader;
    }

    public File getAddonFolder() {
        return addonFolder;
    }

    public Map<File, List<Class<?>>> getLoadedJarFiles() {
        return loadedJarFiles;
    }

    public Object executeMethod(String name, Class<?> clazz, Object... args) {
        try {
            Method m = clazz.getMethod(name);
            return m.invoke(clazz.newInstance(), args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This stops all Addons and makes them unreachable!
     * !!Use at your own Risk!!
     * I warned you!
     */
    public void crashAddons() {
        getLoadedJarFiles().clear();
        try {
            getUrlClassLoader().close();
        } catch (IOException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", "Addon-Loader");
            e.printStackTrace();
        }
    }
}
