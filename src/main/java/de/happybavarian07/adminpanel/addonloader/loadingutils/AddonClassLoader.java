package de.happybavarian07.adminpanel.addonloader.loadingutils;

import de.happybavarian07.adminpanel.addonloader.api.Addon;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class AddonClassLoader extends URLClassLoader {
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    private Addon addon;
    private final File dataFolder;
    private final File file;
    private final JarFile jar;
    private final Manifest manifest;
    private final URL url;
    private IllegalStateException addonState;

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public AddonClassLoader(ClassLoader parent, Addon addon, File file) throws IOException {
        super(new URL[]{file.toURI().toURL()}, parent);
        //Validate.notNull(addon, "Addon cannot be null");
        this.addon = addon;
        this.file = file;
        this.jar = new JarFile(file);
        this.manifest = jar.getManifest();
        this.url = file.toURI().toURL();
        // Get the Name from the Jar File and remove the .jar and if there is a version, like <Name>-1.jar or <Name>-1.0.jar or <Name>-1.0.0.jar, remove it too
        this.dataFolder = new File(file.getParentFile(), file.getName().replaceAll("\\.jar$", "").replaceAll("-\\d+(\\.\\d+)*$", ""));
    }

    @Override
    public URL getResource(String name) {
        return findResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return findResources(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClass0(name, resolve, true);
    }

    private Class<?> loadClass0(@NotNull String name, boolean resolve, boolean checkGlobal) throws ClassNotFoundException {
        try {
            Class<?> result = super.loadClass(name, resolve);
            if (checkGlobal || result.getClassLoader() == this) {
                return result;
            }
        } catch (ClassNotFoundException ignored) {
        }

        if (checkGlobal) {
            Class<?> result = findClass(name);
            if (result != null) {
                return result;
            }
        }

        throw new ClassNotFoundException(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> result = classes.get(name);

        if (result == null) {
            String path = name.replace('.', '/').concat(".class");
            JarEntry entry = jar.getJarEntry(path);

            if (entry != null) {
                byte[] classBytes;

                try (InputStream is = jar.getInputStream(entry)) {
                    classBytes = is.readAllBytes();
                } catch (IOException ex) {
                    throw new ClassNotFoundException(name, ex);
                }

                int dot = name.lastIndexOf('.');
                if (dot != -1) {
                    String pkgName = name.substring(0, dot);
                    if (getDefinedPackage(pkgName) == null) {
                        try {
                            if (manifest != null) {
                                definePackage(pkgName, manifest, url);
                            } else {
                                definePackage(pkgName, null, null, null, null, null, null, null);
                            }
                        } catch (IllegalArgumentException ex) {
                            if (getDefinedPackage(pkgName) == null) {
                                throw new IllegalStateException("Cannot find package " + pkgName);
                            }
                        }
                    }
                }

                CodeSigner[] signers = entry.getCodeSigners();
                CodeSource source = new CodeSource(url, signers);

                result = defineClass(name, classBytes, 0, classBytes.length, source);
            }

            if (result == null) {
                result = super.findClass(name);
            }

            classes.put(name, result);
        }

        return result;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            jar.close();
        }
    }

    @NotNull
    public Collection<Class<?>> getClasses() {
        return classes.values();
    }

    public Addon getAddon() {
        return addon;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public synchronized void initialize(Addon addon) {
        Validate.notNull(addon, "Initializing addon cannot be null");
        Validate.isTrue(addon.getClass().getClassLoader() == this, "Cannot initialize addon outside of this class loader");
        if (this.addon != null || this.addonState != null) {
            throw new IllegalArgumentException("Addon already initialized!", addonState);
        }

        addonState = new IllegalStateException("Initial initialization");
        this.addon = addon;

        addon.init(addon.getAddonLoader(), dataFolder, file, this);
    }
}