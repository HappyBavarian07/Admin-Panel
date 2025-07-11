package de.happybavarian07.adminpanel.utils.managers;

import de.happybavarian07.adminpanel.hooks.BanManagerHook;
import de.happybavarian07.adminpanel.hooks.LiteBansHook;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.dependencyloading.DependencyLoader;
import de.happybavarian07.adminpanel.utils.dependencyloading.annotations.Dependencies;
import de.happybavarian07.adminpanel.utils.dependencyloading.annotations.Dependency;
import de.happybavarian07.adminpanel.utils.dependencyloading.annotations.Repositories;
import de.happybavarian07.adminpanel.utils.dependencyloading.annotations.Repository;
import dte.hooksystem.api.HookSystemAPI;
import dte.hooksystem.service.HookService;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Manages the loading of dependencies for the Admin Panel.
 *
 * @Author HappyBavarian07
 * @Date April 8, 2025
 */

@Repositories(value = {
        @Repository(name = "sonatype", url = "https://oss.sonatype.org/content/groups/public/"),
        @Repository(name = "jitpack.io", url = "https://jitpack.io"),
        @Repository(name = "placeholderapi", url = "https://repo.extendedclip.com/content/repositories/placeholderapi/"),
        @Repository(name = "maven-central", url = "https://oss.sonatype.org/content/groups/public"),
        @Repository(name = "confuser-repo", url = "https://ci.frostcast.net/plugin/repository/everything"),
        @Repository(name = "maven-repo", url = "https://repo.maven.apache.org/maven2/")
})
@Dependencies(value = {
        @Dependency(group = "org.apache.lucene", artifact = "lucene-core", version = "9.8.0", appendToParentClassLoader = true),
        @Dependency(group = "org.apache.lucene", artifact = "lucene-queryparser", version = "9.8.0", appendToParentClassLoader = true),
        @Dependency(group = "commons-codec", artifact = "commons-codec", version = "1.16.0", appendToParentClassLoader = true),
        @Dependency(group = "commons-io", artifact = "commons-io", version = "2.14.0", appendToParentClassLoader = true),

        @Dependency(group = "org.xerial", artifact = "sqlite-jdbc", version = "3.45.0.0", appendToParentClassLoader = true),
        @Dependency(group = "org.mariadb.jdbc", artifact = "mariadb-java-client", version = "3.3.3", appendToParentClassLoader = true)
})
public class APDependencyManager {
    private final AdminPanelMain plugin;
    private final DependencyLoader dependencyLoader;

    public APDependencyManager(AdminPanelMain plugin) {
        this.plugin = plugin;
        this.dependencyLoader = new DependencyLoader();
    }

    public void loadCoreDependencies() {
        plugin.getStartUpLogger().coloredSpacer(ChatColor.RED).message("&e&lLoading Core Dependencies&r");
        try {
            dependencyLoader.scanAndLoadDependencies(true, getClass(), true).get();
        } catch (InterruptedException | ExecutionException e) {
            plugin.getStartUpLogger().message("&cFailed to load core dependencies: " + e.getMessage() + "&r");
            plugin.getStartUpLogger().message("&cPlease check if all dependencies are available and try again.&r");
            StringBuilder stackTraceBuilder = new StringBuilder(e.toString());
            for (StackTraceElement element : e.getStackTrace()) {
                stackTraceBuilder.append("\n\tat ").append(element.toString());
            }
            plugin.getStartUpLogger().message("&cStacktrace:\n" + stackTraceBuilder + "&r");
            plugin.getStartUpLogger().coloredSpacer(ChatColor.RED);
            plugin.getStartUpLogger().emptySpacer().emptySpacer();
            return;
        }

        plugin.getStartUpLogger().message("&a&lCore Dependencies Loaded Successfully&r").coloredSpacer(ChatColor.RED);
        plugin.getStartUpLogger().emptySpacer().emptySpacer();
    }

    /**
     * Loads additional dependencies for addons.
     *
     * @param dependencies List of dependencies to load.
     * @param repositories List of repositories to use for resolving dependencies.
     * @param logging      Flag to enable detailed logging.
     */
    public void loadAddonDependencies(List<Dependency> dependencies, List<Repository> repositories, boolean logging) {
        plugin.getStartUpLogger().coloredSpacer(ChatColor.RED).message("&e&lLoading Addon Dependencies&r");
        // Register repositories
        for (Repository repository : repositories) {
            dependencyLoader.registerRepository(repository.name(), repository.url(), logging);
        }

        // Load dependencies
        for (Dependency dependency : dependencies) {
            dependencyLoader.loadDependency(dependency.group() + ":" + dependency.artifact() + ":" + dependency.version(),
                    dependency.exclusions(),
                    logging,
                    false,
                    "",
                    dependency.logTransistive()
            );
        }

        plugin.getStartUpLogger().message("&a&lAddon Dependencies Loaded Successfully&r").coloredSpacer(ChatColor.RED);
        plugin.getStartUpLogger().emptySpacer().emptySpacer();
    }

    public void useHookSystem() {
        HookService hookService = HookSystemAPI.getService(plugin);

        hookService.register(new BanManagerHook("BanManager")).orElse((handler) -> plugin.getLogger().warning("""
                Failed to hook into BanManager! If you are using LiteBans as the Ban Plugin, please ignore this message.\s
                Else please check if Ban Manager is installed and running or send me your Ban Plugin if it is publically available over the /admin-panel:report feature,\s
                so i can implement it!"""));
        hookService.register(new LiteBansHook("LiteBans")).orElse((handler) -> plugin.getLogger().warning("""
                Failed to hook into LiteBans! If you are using Ban Manager as the Ban Plugin, please ignore this message.\s
                Else please check if LiteBans is installed and running or send me your Ban Plugin if it is publically available over the /admin-panel:report feature,\s
                so i can implement it!"""));
    }
}
