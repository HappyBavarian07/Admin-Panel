package de.happybavarian07.adminpanel.addonloader.loadingutils;

import com.saicone.ezlib.EzlibLoader;
import de.happybavarian07.adminpanel.addonloader.api.Addon;
import de.happybavarian07.adminpanel.addonloader.utils.Dependency;
import de.happybavarian07.adminpanel.addonloader.utils.MavenDependency;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.StartUpLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/*
 * @Author HappyBavarian07
 * @Date September 10, 2024 | 15:39
 */
public class DependencyManager {
    private final AddonLoader loader;
    private final StartUpLogger logger;

    public DependencyManager(AddonLoader loader) {
        this.loader = loader;
        this.logger = AdminPanelMain.getPlugin().getStartUpLogger();
    }

    public boolean checkAndLoadPluginDependencies(Addon addon, Set<Addon> currentlyEnabling) throws IOException, ClassNotFoundException {
        boolean allowedToStart = true;

        for (Dependency dependency : addon.getDependencies()) {
            if (dependency.isAddonDependency()) {
                // Handle Addon Dependency
                File dependencyFile = new File(loader.getAddonFolder(), dependency.getName() + ".jar");
                if (!loader.getLoadedJarFiles().containsKey(dependencyFile)) {
                    allowedToStart = handleMissingPluginDependency(dependency, addon);
                } else {
                    Addon dependencyAddon = loader.getMainClassOfAddon(dependencyFile, true);
                    if (!currentlyEnabling.contains(dependencyAddon)) {
                        currentlyEnabling.add(dependencyAddon);
                        loader.enableAddon(dependencyAddon.getFile(), currentlyEnabling); // Recursive call in loader
                        currentlyEnabling.remove(dependencyAddon);
                    } else {
                        logger.coloredMessage(ChatColor.DARK_RED, "Circular Dependency Detected: " + dependencyAddon.getName());
                        allowedToStart = false;
                    }
                }
            } else {
                // Handle Plugin Dependency
                allowedToStart = checkAndHandlePluginDependency(dependency, addon);
            }

            if (!allowedToStart) break; // Stop if a mandatory dependency is missing or circular
        }

        return allowedToStart;
    }

    // New method to handle plugin dependencies
    private boolean checkAndHandlePluginDependency(Dependency dependency, Addon addon) {
        if (Bukkit.getPluginManager().getPlugin(dependency.getName()) == null) {
            if (!dependency.isOptional()) {
                logger.coloredMessage(ChatColor.DARK_RED, "Dependency Error enabling Addon: " + addon.getName());
                logger.coloredMessage(ChatColor.DARK_RED, "Please install the following Plugin Dependency '" + dependency.getName() + "'!");
                return false; // Mandatory plugin dependency missing
            } else {
                logger.coloredMessage(ChatColor.YELLOW, "Optional Plugin Dependency '" + dependency.getName() + "' for Addon '" + addon.getName() + "' is missing but not required.");
            }
        }
        return true;
    }


    private boolean handleMissingPluginDependency(Dependency dependency, Addon addon) {
        if (!dependency.isOptional()) {
            logger.coloredMessage(ChatColor.DARK_RED, "Dependency Error enabling Addon: " + addon.getName());
            logger.coloredMessage(ChatColor.DARK_RED, "Please install the following Dependency '" + dependency.getName() + "' (Link: " + dependency.getLink() + ")!");
            return false;
        } else {
            logger.coloredMessage(ChatColor.YELLOW, "Optional Dependency '" + dependency.getName() + "' for Addon '" + addon.getName() + "' is missing but not required.");
            return true;
        }
    }
    
    // Methods to handle Maven Dependencies
    public boolean checkAndLoadMavenDependencies(Addon addon) {
        boolean allowedToStart = true;

        for (MavenDependency dependency : addon.getMavenDependencies()) {
            // Validate dependency attributes
            if (dependency.getGroupId() == null || dependency.getGroupId().isEmpty() ||
                    dependency.getArtifactId() == null || dependency.getArtifactId().isEmpty() ||
                    dependency.getVersion() == null || dependency.getVersion().isEmpty()) {
                logger.coloredMessage(ChatColor.DARK_RED, "Maven dependency has invalid attributes: " + dependency);
                allowedToStart = false;
                break;
            }

            try {
                EzlibLoader.Dependency ezlibDependency = new EzlibLoader.Dependency();
                ezlibDependency.path(dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion());

                // Use EZLib Loader to load the dependency
                AdminPanelMain.getPlugin().getEZLibLoader().loadDependency(ezlibDependency);

            } catch (Exception e) {
                logger.coloredMessage(ChatColor.DARK_RED, "Failed to load Maven dependency: " +
                        dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion());
                allowedToStart = false;
                break; // Exit loop because loading crucial dependencies failed
            }
        }

        return allowedToStart;
    }
}
