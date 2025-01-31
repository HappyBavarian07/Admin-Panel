package de.happybavarian07.adminpanel.addonloader.api;

import de.happybavarian07.adminpanel.addonloader.loadingutils.AddonClassLoader;
import de.happybavarian07.adminpanel.addonloader.loadingutils.AddonLoader;
import de.happybavarian07.adminpanel.addonloader.utils.Dependency;
import de.happybavarian07.adminpanel.addonloader.utils.MavenDependency;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class Addon implements Comparable<Addon> {
    private boolean enabled = false;
    private AddonLoader loader = null;
    private AddonClassLoader classLoader = null;
    private File file = null;
    private File dataFolder = null;
    private FileConfiguration newConfig = null;
    private File configFile = null;
    private AddonLogger logger = null;

    public abstract String getName();
    public abstract String getVersion();
    public abstract String getDescription();

    public String getPrefix() {
        return "[" + getName() + "] ";
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public List<Dependency> getDependencies() {
        return new ArrayList<>();
    }

    public List<MavenDependency> getMavenDependencies() {
        return new ArrayList<>();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @NotNull
    public FileConfiguration getConfig() {
        if (newConfig == null) {
            reloadConfig();
        }
        return newConfig;
    }

    public void reloadConfig() {
        newConfig = YamlConfiguration.loadConfiguration(configFile);

        final InputStream defConfigStream = getResource("config.yml");
        if (defConfigStream == null) {
            return;
        }

        newConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
    }

    public void init(@NotNull AddonLoader loader, @NotNull File dataFolder, @NotNull File file, @NotNull AddonClassLoader classLoader) {
        this.loader = loader;
        this.dataFolder = dataFolder;
        this.file = file;
        this.classLoader = classLoader;
        this.configFile = new File(dataFolder, "config.yml");
        this.logger = new AddonLogger(this);
    }

    public AddonLogger getLogger() {
        return logger;
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
    }

    public void saveResource(@NotNull String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + file);
        }

        File outFile = new File(dataFolder, resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(dataFolder, resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
                logger.log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }

    public InputStream getResource(@NotNull String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        try {
            URL url = classLoader.getResource(filename);

            if (url == null) {
                return null;
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

    @Nullable
    protected final Reader getTextResource(@NotNull String file) {
        final InputStream in = getResource(file);

        return in == null ? null : new InputStreamReader(in, StandardCharsets.UTF_8);
    }

    public void saveConfig() {
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }

    public AddonLoader getAddonLoader() {
        return loader;
    }

    public File getFile() {
        return file;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public File getConfigFile() {
        return configFile;
    }

    public AddonClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public int compareTo(@NotNull Addon o) {
        int name = this.getName().compareTo(o.getName());
        if (name != 0) return name;

        int version = this.getVersion().compareTo(o.getVersion());
        if (version != 0) return version;

        int author = this.getDescription().compareTo(o.getDescription());
        if (author != 0) return author;

        return this.getDependencies().size() - o.getDependencies().size();
    }

    @NotNull
    public static <T extends Addon> T getAddon(@NotNull Class<T> clazz) {
        if (!Addon.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(clazz + " does not extend " + Addon.class);
        }
        final ClassLoader cl = clazz.getClassLoader();
        if (!(cl instanceof AddonClassLoader)) {
            throw new IllegalArgumentException(clazz + " is not initialized by " + AddonClassLoader.class);
        }
        Addon addon = ((AddonClassLoader) cl).getAddon();
        if (addon == null) {
            throw new IllegalStateException("Cannot get addon for " + clazz + " from a static initializer");
        }
        return clazz.cast(addon);
    }
}