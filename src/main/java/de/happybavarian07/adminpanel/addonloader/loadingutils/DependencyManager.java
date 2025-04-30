package de.happybavarian07.adminpanel.addonloader.loadingutils;

import de.happybavarian07.adminpanel.addonloader.api.Addon;
import de.happybavarian07.adminpanel.addonloader.utils.PluginDependency;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.StartUpLogger;
import de.happybavarian07.adminpanel.utils.dependencyloading.annotations.Dependencies;
import de.happybavarian07.adminpanel.utils.dependencyloading.annotations.Dependency;
import de.happybavarian07.adminpanel.utils.dependencyloading.annotations.Repositories;
import de.happybavarian07.adminpanel.utils.dependencyloading.annotations.Repository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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

        for (PluginDependency dependency : addon.getDependencies()) {
            if (dependency.addonDependency()) {
                // Handle Addon Dependency
                File dependencyFile = new File(loader.getAddonFolder(), dependency.name() + ".jar");
                if (!loader.getLoadedJarFiles().containsKey(dependencyFile)) {
                    allowedToStart = handleMissingPluginDependency(dependency, addon);
                } else { // TODO Dependencies are not getting loaded for whatever reason (Only the Addon itself)
                    Addon dependencyAddon = loader.getMainClassOfAddon(dependencyFile, true);
                    if (!currentlyEnabling.contains(dependencyAddon)) {
                        currentlyEnabling.add(dependencyAddon);
                        loader.enableAddon(dependencyAddon.getFile(), currentlyEnabling, false); // Recursive call in loader
                        this.logger.coloredMessage(ChatColor.GREEN, "Loaded Dependency: '" + dependencyAddon.getName() + "' for '" + addon.getName());
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
    private boolean checkAndHandlePluginDependency(PluginDependency dependency, Addon addon) {
        if (Bukkit.getPluginManager().getPlugin(dependency.name()) == null) {
            if (!dependency.optional()) {
                logger.coloredMessage(ChatColor.DARK_RED, "Dependency Error enabling Addon: " + addon.getName());
                logger.coloredMessage(ChatColor.DARK_RED, "Please install the following Plugin Dependency '" + dependency.name() + "'!");
                return false; // Mandatory plugin dependency missing
            } else {
                logger.coloredMessage(ChatColor.YELLOW, "Optional Plugin Dependency '" + dependency.name() + "' for Addon '" + addon.getName() + "' is missing but not required.");
            }
        }
        return true;
    }


    private boolean handleMissingPluginDependency(PluginDependency dependency, Addon addon) {
        if (!dependency.optional()) {
            logger.coloredMessage(ChatColor.DARK_RED, "Dependency Error enabling Addon: " + addon.getName());
            logger.coloredMessage(ChatColor.DARK_RED, "Please install the following Dependency '" + dependency.name() + "' (Link: " + dependency.link() + ")!");
            return false;
        } else {
            logger.coloredMessage(ChatColor.YELLOW, "Optional Dependency '" + dependency.name() + "' for Addon '" + addon.getName() + "' is missing but not required.");
            return true;
        }
    }

    // Methods to handle Maven Dependencies
    public boolean checkAndLoadMavenDependencies(Addon addon) {
        AtomicBoolean allowedToStart = new AtomicBoolean(true);

        List<Dependency> dependencyList = new ArrayList<>();
        List<Repository> repositories = new ArrayList<>();
        // Check if the addon has any Lib annotations
        if (addon.getClass().isAnnotationPresent(Dependency.class)) {
            Dependency lib = addon.getClass().getAnnotation(Dependency.class);
            dependencyList.add(lib);
        } else if (addon.getClass().isAnnotationPresent(Dependencies.class)) {
            Dependencies libs = addon.getClass().getAnnotation(Dependencies.class);
            dependencyList.addAll(Arrays.asList(libs.value()));
        } else if (addon.getClass().isAnnotationPresent(Repositories.class)) {
            Repositories libs = addon.getClass().getAnnotation(Repositories.class);
            repositories.addAll(Arrays.asList(libs.value()));
        }

        // Check if the addon has any Lib annotations
        dependencyList.forEach(dependency -> {
            if (dependency.repository() != null && !dependency.repository().url().isEmpty()) {
                if (!repositories.contains(dependency.repository())) {
                    repositories.add(dependency.repository());
                }
            }
        });

        addon.getMavenDependencies().forEach(dependency -> {
            if (dependency.getGroupId() == null || dependency.getArtifactId() == null || dependency.getVersion() == null) {
                logger.coloredMessage(ChatColor.DARK_RED, "Maven dependency has invalid attributes: " + dependency);
                allowedToStart.set(false);
            } else {
                // Convert MavenDependency to Dependency
                Dependency convertedDependency = new Dependency() {
                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return Dependency.class;
                    }

                    @Override
                    public String group() {
                        return dependency.getGroupId();
                    }

                    @Override
                    public String artifact() {
                        return dependency.getArtifactId();
                    }

                    @Override
                    public String version() {
                        return dependency.getVersion();
                    }

                    @Override
                    public String[] exclusions() {
                        return new String[0];
                    }

                    @Override
                    public Repository repository() {
                        return new Repository() {
                            @Override
                            public Class<? extends Annotation> annotationType() {
                                return Repository.class;
                            }

                            @Override
                            public String name() {
                                return dependency.getDirectURL().replace("https://", "");
                            }

                            @Override
                            public String url() {
                                return dependency.getDirectURL();
                            }
                        };
                    }

                    @Override
                    public boolean appendToParentClassLoader() {
                        return dependency.isAppendToParentClassLoader();
                    }

                    @Override
                    public boolean optional() {
                        return dependency.isOptional();
                    }

                    @Override
                    public boolean addonDependency() {
                        return dependency.isAddonDependency();
                    }

                    @Override
                    public boolean logTransistive() {
                        return false;
                    }
                };
                dependencyList.add(convertedDependency);
            }
        });

        for (Dependency dependency : dependencyList) {
            // Validate dependency attributes
            if (dependency.group() == null || dependency.group().isEmpty() ||
                    dependency.artifact() == null || dependency.artifact().isEmpty() ||
                    dependency.version() == null || dependency.version().isEmpty()) {
                logger.coloredMessage(ChatColor.DARK_RED, "Maven dependency has invalid attributes: " + dependency);
                allowedToStart.set(false);
                break;
            }
        }

        AdminPanelMain.getPlugin().getDependencyManager().loadAddonDependencies(dependencyList, repositories, true);

        return allowedToStart.get();
    }
}
