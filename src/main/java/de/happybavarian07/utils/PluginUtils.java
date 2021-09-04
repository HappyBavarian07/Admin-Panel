package de.happybavarian07.utils;

import de.happybavarian07.main.Main;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.plugin.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.NotDirectoryException;
import java.util.*;

public class PluginUtils {
    private Main plugin;

    public PluginUtils() {
        this.plugin = Main.getPlugin();
    }

    public void load(Plugin plugin) {
        load(plugin.getName(), plugin.getDescription().getVersion());
    }

    public void load(String name, String version) {

        Plugin target = null;

        File pluginDir = new File("plugins");

        if (!pluginDir.isDirectory()) {
            return;
        }

        File pluginFile = new File(pluginDir, name + "-" + version + ".jar");

        if (!pluginFile.isFile()) {
            for (File f : pluginDir.listFiles()) {
                if (f.getName().endsWith(".jar")) {
                    try {
                        PluginDescriptionFile desc = plugin.getPluginLoader().getPluginDescription(f);
                        if (desc.getName().equalsIgnoreCase(name)) {
                            pluginFile = f;
                            break;
                        }
                    } catch (InvalidDescriptionException e) {
                        return;
                    }
                }
            }
        }

        try {
            target = Bukkit.getPluginManager().loadPlugin(pluginFile);
        } catch (InvalidDescriptionException | InvalidPluginException e) {
            e.printStackTrace();
            return;
        }
        Bukkit.getPluginManager().enablePlugin(target);
    }

    public Plugin load(File pluginFile) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
        return Bukkit.getPluginManager().loadPlugin(pluginFile);
    }

    public List<Plugin> getAllPlugins() {
        List<Plugin> plugins = new ArrayList<>();
        for (String pluginName : getPluginNames(false)) {
            plugins.add(getPluginByName(pluginName));
        }
        return plugins;
    }

    public Plugin getPluginByName(String name) {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (name.equalsIgnoreCase(plugin.getName())) {
                return plugin;
            }
        }
        throw new NullPointerException("Plugin: " + name + " is null!");
    }

    public List<String> getPluginNames(boolean fullName) {
        List<String> plugins = new ArrayList<>();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            plugins.add(fullName ? plugin.getDescription().getFullName() : plugin.getName());
        }
        return plugins;
    }

    public void unload(Plugin plugin) {

        String name = plugin.getName();

        PluginManager pluginManager = Bukkit.getPluginManager();

        SimpleCommandMap commandMap = null;

        List<Plugin> plugins = null;

        Map<String, Plugin> names = null;
        Map<String, Command> commands = null;
        Map<Event, SortedSet<RegisteredListener>> listeners = null;

        boolean reloadlisteners = true;

        pluginManager.disablePlugin(plugin);

        try {

            Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
            pluginsField.setAccessible(true);
            plugins = (List<Plugin>) pluginsField.get(pluginManager);

            Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);
            names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

            try {
                Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
                listenersField.setAccessible(true);
                listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(pluginManager);
            } catch (Exception e) {
                reloadlisteners = false;
            }

            Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            commands = (Map<String, Command>) knownCommandsField.get(commandMap);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        pluginManager.disablePlugin(plugin);

        if (plugins != null && plugins.contains(plugin))
            plugins.remove(plugin);

        if (names != null && names.containsKey(name))
            names.remove(name);

        if (listeners != null && reloadlisteners) {
            for (SortedSet<RegisteredListener> set : listeners.values()) {
                for (Iterator<RegisteredListener> it = set.iterator(); it.hasNext(); ) {
                    RegisteredListener value = it.next();
                    if (value.getPlugin() == plugin) {
                        it.remove();
                    }
                }
            }
        }

        if (commandMap != null) {
            assert commands != null;
            for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Command> entry = it.next();
                if (entry.getValue() instanceof PluginCommand) {
                    PluginCommand c = (PluginCommand) entry.getValue();
                    if (c.getPlugin() == plugin) {
                        c.unregister(commandMap);
                        it.remove();
                    }
                }
            }
        }

        // Attempt to close the classloader to unlock any handles on the plugin's jar file.
        ClassLoader cl = plugin.getClass().getClassLoader();

        if (cl instanceof URLClassLoader) {

            try {

                Field pluginField = cl.getClass().getDeclaredField("plugin");
                pluginField.setAccessible(true);
                pluginField.set(cl, null);

                Field pluginInitField = cl.getClass().getDeclaredField("pluginInit");
                pluginInitField.setAccessible(true);
                pluginInitField.set(cl, null);

            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                ex.printStackTrace();
            }

            try {

                ((URLClassLoader) cl).close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

        // Will not work on processes started with the -XX:+DisableExplicitGC flag, but lets try it anyway.
        // This tries to get around the issue where Windows refuses to unlock jar files that were previously loaded into the JVM.
        System.gc();
    }

    public void reload(Plugin plugin) {
        if (plugin != null) {
            unload(plugin);
            load(plugin);
        } else {
            throw new NullPointerException("Plugin is null!");
        }
    }

    public Plugin downloadPluginFromSpiget(int resourceID, String fileName, Boolean enableAfterStart) throws IOException, InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
        File pluginDir = new File("plugins");

        if (!pluginDir.isDirectory()) {
            throw new NotDirectoryException("No Plugins Directory found!");
        }

        File pluginFile = new File(pluginDir, fileName + ".jar");
        URL downloadURL = new URL("https://api.spiget.org/v2/resources/" + resourceID + "/download");
        FileUtils.copyURLToFile(downloadURL, pluginFile);
        Plugin target = load(pluginFile);
        if (enableAfterStart) {
            Bukkit.getPluginManager().enablePlugin(target);
        }
        return target;
    }
}
