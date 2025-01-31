package de.happybavarian07.adminpanel.addonloader.loadingutils;

import de.happybavarian07.adminpanel.addonloader.api.Addon;
import de.happybavarian07.adminpanel.addonloader.utils.FileUtils;
import de.happybavarian07.adminpanel.utils.LogPrefix;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.StartUpLogger;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class AddonLoader {
    private static AdminPanelMain plugin;
    private final Map<File, List<Class<?>>> loadedJarFiles;
    private final Map<File, Addon> loadedAddonMainClasses;
    private final Map<String, File> addonNamesToFiles;
    private final Map<File, AddonClassLoader> addonClassLoaders; // New map to store AddonClassLoaders
    private final File addonFolder;
    private final StartUpLogger logger;

    public AddonLoader(File addonFolder) {
        plugin = AdminPanelMain.getPlugin();
        this.logger = plugin.getStartUpLogger();
        this.addonFolder = addonFolder;
        this.loadedJarFiles = new HashMap<>();
        this.loadedAddonMainClasses = new HashMap<>();
        this.addonNamesToFiles = new HashMap<>();
        this.addonClassLoaders = new HashMap<>(); // Initialize the map

        if (!addonFolder.isDirectory()) {
            addonFolder.mkdir();
        }
        List<File> jarfiles = findJarFiles(addonFolder);
        for (File jarFile : jarfiles) {
            plugin.getStartUpLogger().coloredMessage(ChatColor.BLUE, jarFile.toString());
            loadedJarFiles.put(jarFile, null);
        }
    }

    public Map<File, List<Class<?>>> getLoadedJarFiles() {
        return loadedJarFiles;
    }

    public File getAddonFolder() {
        return addonFolder;
    }

    public File getAddonFile(Addon addon) {
        for (File file : loadedAddonMainClasses.keySet()) {
            if (loadedAddonMainClasses.get(file).equals(addon)) {
                return file;
            }
        }
        return null;
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
                    Addon addonTemp = getMainClassOfAddon(addon, true);
                    Class<?> loaded = addonTemp.getClassLoader().loadClass(names);

                    classes.add(loaded);
                } catch (final NoClassDefFoundError ignored) {
                }
            }
        }

        if (classes.isEmpty()) {
            return null;
        }

        loadedJarFiles.replace(addon, null, classes);
        return classes;
    }

    public Addon getMainClassOfAddon(File addon, boolean addToMap) {
        try {
            if (loadedAddonMainClasses.containsKey(addon)) {
                return loadedAddonMainClasses.get(addon);
            } else {
                Addon temp = null;
                try {
                    temp = FileUtils.findClass(addon, Addon.class).getDeclaredConstructor().newInstance();
                    checkValidAddonValues(temp);
                } catch (NullPointerException e) {
                    if (temp == null) {
                        plugin.getFileLogger().writeToLog(Level.SEVERE, "The Addon " + addon.getName() + " has no valid Addon Values or doesn't exist entirely!", LogPrefix.ADDONLOADER);
                        throw new NullPointerException("The Addon " + addon.getName() + " has no valid Addon Values or doesn't exist entirely!");
                    }
                } catch (InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }

                // Create a custom ClassLoader to load classes from the addon's JAR
                AddonClassLoader addonClassLoader = new AddonClassLoader(getClass().getClassLoader(), null, addon);

                // Try and unload the Main Class that got loaded by the old one, because it has to be loaded by the Addon Class Loader
                String className = temp.getClass().getName();
                loadedAddonMainClasses.remove(addon);
                temp.getClass().getClassLoader().clearAssertionStatus(); // Clear the assertion status of the class
                ((URLClassLoader) temp.getClass().getClassLoader()).close();

                // Load the main class without initializing it
                Class<?> mainClass = addonClassLoader.loadClass(className, false);
                // Initialize the Addon instance
                temp = (Addon) mainClass.getDeclaredConstructor().newInstance();
                addonClassLoader.initialize(temp);

                if (addToMap) {
                    addonClassLoaders.put(addon, addonClassLoader); // Store the AddonClassLoader
                    loadedAddonMainClasses.put(addon, temp);
                    addonNamesToFiles.put(temp.getName(), addon);
                }
                return temp;
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Addon getAddonMainClassByName(String name) {
        return loadedAddonMainClasses.get(addonNamesToFiles.get(name));
    }

    public List<Addon> getLoadedAddons() {
        List<Addon> addons = new ArrayList<>();
        for (File file : loadedAddonMainClasses.keySet()) {
            addons.add(loadedAddonMainClasses.get(file));
        }
        return addons;
    }

    public EnableResult enableAddon(File addonFile, Set<Addon> currentlyEnabling) throws IOException, ClassNotFoundException {
        if (addonFile == null) return EnableResult.NULL_ADDON;
        Addon addon = getMainClassOfAddon(addonFile, false);

        if (currentlyEnabling.contains(addon)) {
            logger.coloredMessage(ChatColor.DARK_RED, "Circular Dependency Detected: " + addon.getName());
            return EnableResult.CIRCULAR_DEPENDENCY;
        }

        currentlyEnabling.add(addon);
        DependencyManager dependencyManager = new DependencyManager(this);

        if (dependencyManager.checkAndLoadPluginDependencies(addon, currentlyEnabling)) {
            if (!dependencyManager.checkAndLoadMavenDependencies(addon)) {
                return EnableResult.DEPENDENCY_MISSING;
            }
            initAddon(addon);
            addon.onEnable();
            addon.setEnabled(true);
            plugin.getLogger().log(Level.INFO, "Enabled Addon: " + addon.getName());
        } else {
            return EnableResult.DEPENDENCY_MISSING;
        }

        currentlyEnabling.remove(addon);
        return EnableResult.SUCCESS;
    }

    public EnableResult disableAddon(File file) {
        Addon addon = getMainClassOfAddon(file, false);
        if (addon == null) {
            return EnableResult.NULL_ADDON;
        }
        if (!addon.isEnabled()) {
            return EnableResult.ALREADY_ENABLED;
        }
        addon.onDisable();
        addon.setEnabled(false);
        return EnableResult.SUCCESS;
    }

    public enum EnableResult {
        SUCCESS,
        NULL_ADDON,
        DEPENDENCY_MISSING,
        CIRCULAR_DEPENDENCY,
        ALREADY_ENABLED,
        ERROR;
    }

    private void checkValidAddonValues(Addon temp) throws NullPointerException {
        if (temp.getName() == null || temp.getName().isEmpty()) {
            throw new NullPointerException("The Addon Name is null or empty!");
        }
        if (temp.getVersion() == null || temp.getVersion().isEmpty()) {
            throw new NullPointerException("The Addon Version is null or empty!");
        }
        if (temp.getDescription() == null || temp.getDescription().isEmpty()) {
            throw new NullPointerException("The Addon Description is null or empty!");
        }
    }

    public void initAddon(Addon addon) {
        File addonDataFolder = new File(addonFolder, addon.getName());
        if (!addonDataFolder.exists()) {
            addonDataFolder.mkdirs();
        }
        if(addonClassLoaders.get(addon.getFile()).getAddon() == null) {
            addonClassLoaders.get(addon.getFile()).initialize(addon);
        }
        /*try {
            Method initMethod = Addon.class.getDeclaredMethod("init", AddonLoader.class, File.class, File.class, AddonClassLoader.class);
            initMethod.setAccessible(true);

            initMethod.invoke(addon, this, addonDataFolder, addonFile, addonClassLoaders.get(addon.getFile()));
            initMethod.setAccessible(false);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }*/
    }


    public void crashAddons() {
        for (File addonFile : getLoadedJarFiles().keySet()) {
            getMainClassOfAddon(addonFile, false).onDisable();
        }
        getLoadedJarFiles().clear();
        try {
            for (AddonClassLoader classLoader : addonClassLoaders.values()) {
                classLoader.close();
            }
            addonClassLoaders.clear();
        } catch (IOException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", LogPrefix.ADDONLOADER);
            e.printStackTrace();
        } finally {
            // Ensure all AddonClassLoaders are closed to prevent memory leaks
            for (AddonClassLoader classLoader : addonClassLoaders.values()) {
                try {
                    classLoader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            addonClassLoaders.clear(); // Clear the map to allow garbage collection
        }
    }
}