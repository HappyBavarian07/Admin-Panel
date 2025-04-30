package de.happybavarian07.adminpanel.utils.dependencyloading;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.StartUpLogger;
import de.happybavarian07.adminpanel.utils.dependencyloading.annotations.*;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.bukkit.ChatColor;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import java.io.*;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * Optimierter DependencyLoader.
 * <p>Dieser Loader löst Maven-Abhängigkeiten asynchron, nutzt ein verbessertes Caching,
 * führt einen gezielten Annotation-Scan durch und lädt JARs dynamisch in einen eigenen ClassLoader.</p>
 */
public final class DependencyLoader {
    private static final StartUpLogger logger = AdminPanelMain.getPlugin().getStartUpLogger();
    private final RepositorySystem repositorySystem;
    private final RepositorySystemSession session;
    // Verwende eine Threadpool-Instanz, deren maximale Threadzahl konfigurierbar ist.
    private final ExecutorService dependencyResolverPool;
    private final List<RemoteRepository> repositories = new ArrayList<>();
    private final Set<String> loadedDependencies = ConcurrentHashMap.newKeySet();
    private final Set<String> failedDeps = ConcurrentHashMap.newKeySet();
    private final DynamicClassLoader dynamicClassLoader;
    private final File failedDepsFile;
    private final File dependencyCacheFile;
    // Mapping: Maven-Koordinate -> Pfad zur JAR-Datei
    private final Map<String, String> dependencyCache = new ConcurrentHashMap<>();

    public DependencyLoader() {
        this.repositorySystem = newRepositorySystem();
        this.session = newSession(repositorySystem);
        // Setze einen Threadpool – maximal genutzt werden sollen z. B. doppelt so viele Threads wie Processor verfügbar sind
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maxPoolSize = corePoolSize * 2;
        this.dependencyResolverPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        this.dynamicClassLoader = new DynamicClassLoader("DynamicDependencyClassLoader",
                AdminPanelMain.getPlugin().getClass().getClassLoader());
        this.failedDepsFile = new File(AdminPanelMain.getPlugin().getDataFolder(), ".m2/failed_dependencies.txt");
        this.dependencyCacheFile = new File(AdminPanelMain.getPlugin().getDataFolder(), ".m2/dependency_cache.properties");

        loadFailedDependencies();
        loadDependencyCache();

        // Registriere Standard-Repositories aus der zentralen Konfiguration
        // Zentralisierte Konfiguration (hier exemplarisch, statisch definiert)
        DependencyConfig dependencyConfig = new DependencyConfig();
        dependencyConfig.getRepositories().forEach(repo ->
                registerRepository(repo.name(), repo.url(), true));
        // Preload eines Beispiel-Abhängigkeit (ClassGraph) – dies wird später aus dem Cache geladen
        loadDependency("io.github.classgraph:classgraph:4.8.137", new String[0], true, true, "", false);
    }

    // --- Repository System Setup ---

    private static RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    private static RepositorySystemSession newSession(RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        File localRepoDir = new File(AdminPanelMain.getPlugin().getDataFolder(), ".m2/repository");
        if (localRepoDir.mkdirs()) {
            logger.coloredMessage(ChatColor.DARK_RED,
                    "Created local repository directory: " + localRepoDir.getAbsolutePath());
        } else {
            logger.coloredMessage(ChatColor.DARK_RED,
                    "Using existing local repository directory: " + localRepoDir.getAbsolutePath());
        }
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, new LocalRepository(localRepoDir)));
        return session;
    }

    // --- Repository-Registrierung ---

    public void registerRepository(String name, String url, boolean logging) {
        boolean exists = repositories.stream()
                .anyMatch(repo -> repo.getId().equalsIgnoreCase(name) && repo.getUrl().equals(url));
        if (!exists) {
            repositories.add(new RemoteRepository.Builder(name, "default", url).build());
            if (logging) {
                logRepository(name, url);
            }
        }
    }

    private void logRepository(String name, String url) {
        logger.coloredMessage(ChatColor.DARK_RED,
                "Registered repository: " + name + " (" + url + ")");
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(
                Level.INFO,
                "Registered repository: " + name + " (" + url + ")",
                "OptimizedDependencyLoader",
                false);
    }

    // --- Dependency-Loading ---

    /**
     * Synchronous Wrapper.
     */
    public void loadDependency(String coordinate, String[] exclusions, boolean logging, boolean appendToParent, String topCoordinate, boolean logTransitive) {
        try {
            loadDependencyAsync(coordinate, exclusions, logging, appendToParent, topCoordinate, logTransitive).get();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE,
                    "Failed to load dependency: " + coordinate + " Exception: " + e,
                    "OptimizedDependencyLoader", true);
            failedDeps.add(coordinate);
            persistFailedDependencies();
            if (topCoordinate.isEmpty()) {
                logger.coloredMessage(ChatColor.DARK_RED,
                        "Failed to load dependency: " + coordinate + "\nStacktrace:\n" + stackTrace);
            }
        }
    }

    /**
     * Asynchrone Methode zum Laden einer Abhängigkeit.
     */
    public CompletableFuture<Void> loadDependencyAsync(String coordinate, String[] exclusions, boolean logging,
                                                       boolean appendToParent, String topCoordinate, boolean logTransitive) {
        // Falls bereits fehlschlug oder bereits geladen wurde, überspringen
        if (failedDeps.contains(coordinate) || loadedDependencies.contains(coordinate)) {
            return CompletableFuture.completedFuture(null);
        }

        // Prüfe Cache: Wenn vorhanden, versuche JAR direkt hinzuzufügen
        if (dependencyCache.containsKey(coordinate)) {
            File cachedJar = new File(dependencyCache.get(coordinate));
            if (cachedJar.exists()) {
                try {
                    addJarToClassLoader(cachedJar, appendToParent);
                    loadedDependencies.add(coordinate);
                    if (logging) {
                        logDependency(coordinate, topCoordinate);
                    }
                    return CompletableFuture.completedFuture(null);
                } catch (Exception e) {
                    logger.coloredMessage(ChatColor.DARK_RED,
                            "Failed to add cached JAR to ClassLoader: " + cachedJar.getAbsolutePath());
                    dependencyCache.remove(coordinate);
                    persistDependencyCache();
                }
            } else {
                logger.coloredMessage(ChatColor.DARK_RED,
                        "Cached dependency not found: " + cachedJar.getAbsolutePath());
                dependencyCache.remove(coordinate);
                persistDependencyCache();
            }
        }

        // Starte asynchron den Download und die (optionale) transitive Auflösung
        return CompletableFuture.runAsync(() -> {
            try {
                Artifact artifact = new DefaultArtifact(coordinate);
                ArtifactRequest request = new ArtifactRequest();
                request.setArtifact(artifact);
                request.setRepositories(repositories);
                ArtifactResult result = repositorySystem.resolveArtifact(session, request);
                File artifactFile = result.getArtifact().getFile();
                loadedDependencies.add(coordinate);
                dependencyCache.put(coordinate, artifactFile.getAbsolutePath());
                persistDependencyCache();
                // Transitive Abhängigkeiten asynchron laden (max. Tiefe konfigurierbar, z. B. 2)
                loadTransitives(artifact, exclusions, logTransitive, appendToParent, topCoordinate, 2).join();
                addJarToClassLoader(artifactFile, appendToParent);
                if (logging) {
                    logDependency(coordinate, topCoordinate);
                }
            } catch (Exception e) {
                handleDependencyError(coordinate, e, topCoordinate);
            }
        }, dependencyResolverPool);
    }

    private CompletableFuture<Void> loadTransitives(Artifact artifact, String[] exclusions, boolean logTransitive,
                                                    boolean appendToParent, String topCoordinate, int depth) {
        if (depth <= 0) return CompletableFuture.completedFuture(null);
        ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
        descriptorRequest.setArtifact(artifact);
        descriptorRequest.setRepositories(repositories);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return repositorySystem.readArtifactDescriptor(session, descriptorRequest);
            } catch (Exception e) {
                handleDependencyError(artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion(), e, topCoordinate);
                return null;
            }
        }, dependencyResolverPool).thenCompose(descriptorResult -> {
            if (descriptorResult == null) return CompletableFuture.completedFuture(null);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (org.eclipse.aether.graph.Dependency dependency : descriptorResult.getDependencies()) {
                Artifact depArtifact = dependency.getArtifact();
                String depCoordinate = depArtifact.getGroupId() + ":" + depArtifact.getArtifactId() + ":" + depArtifact.getVersion();
                if (Arrays.asList(exclusions).contains(depCoordinate)) continue;
                if (!loadedDependencies.contains(depCoordinate)) {
                    CompletableFuture<Void> future = loadDependencyAsync(depCoordinate, new String[0],
                            logTransitive, appendToParent, topCoordinate, logTransitive)
                            .thenCompose(v -> loadTransitives(depArtifact, exclusions, logTransitive, appendToParent, topCoordinate, depth - 1));
                    futures.add(future);
                }
            }
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        });
    }

    private void addJarToClassLoader(File jarFile, boolean appendToParent) throws Exception {
        // Prüfe, ob es sich um eine JAR-Datei handelt
        if (!jarFile.getName().endsWith(".jar")) {
            logger.coloredMessage(ChatColor.DARK_RED,
                    "File is not a JAR: " + jarFile.getAbsolutePath());
            return;
        }
        URL jarUrl = jarFile.toURI().toURL();
        if (appendToParent) {
            try {
                // Versuche, den JAR zum Parent-Classloader hinzuzufügen
                dynamicClassLoader.appendToParent(jarUrl, AdminPanelMain.getPlugin().getClass().getClassLoader());
            } catch (Throwable t) {
                logger.coloredMessage(
                        ChatColor.DARK_RED,
                        "Failed to append JAR to parent ClassLoader: " + t
                );
                throw new Exception("Failed to append JAR", t);
            }
        } else {
            dynamicClassLoader.add(jarUrl);
        }
    }

    private void logDependency(String coordinate, String topCoordinate) {
        String message = "Loaded dependency: " + coordinate;
        if (!topCoordinate.isEmpty()) {
            message += " (from " + topCoordinate + ")";
        }
        logger.coloredMessage(ChatColor.DARK_RED, message);
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, message, "OptimizedDependencyLoader", false);
    }

    private void handleDependencyError(String coordinate, Exception e, String topCoordinate) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE,
                "Failed to load dependency: " + coordinate + " Exception: " + e,
                "OptimizedDependencyLoader", true);
        failedDeps.add(coordinate);
        persistFailedDependencies();
        if (topCoordinate.isEmpty()) {
            logger.coloredMessage(
                    ChatColor.DARK_RED,
                    "Failed to load dependency: " + coordinate + "\nStacktrace:\n" + stackTrace
            );
        }
    }

    // --- Caching: Laden & Persistieren ---

    private void loadFailedDependencies() {
        if (!failedDepsFile.exists()) return;
        try (Scanner scanner = new Scanner(failedDepsFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    failedDeps.add(line);
                }
            }
        } catch (Exception e) {
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE,
                    "Failed to load failed dependencies file: " + e,
                    "OptimizedDependencyLoader", true);
        }
    }

    private void persistFailedDependencies() {
        try (PrintWriter writer = new PrintWriter(failedDepsFile)) {
            for (String dep : failedDeps) {
                writer.println(dep);
            }
        } catch (Exception e) {
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE,
                    "Failed to persist failed dependencies: " + e,
                    "OptimizedDependencyLoader", true);
        }
    }

    private void loadDependencyCache() {
        if (!dependencyCacheFile.exists()) return;
        try (FileInputStream fis = new FileInputStream(dependencyCacheFile)) {
            Properties properties = new Properties();
            properties.load(fis);
            for (String key : properties.stringPropertyNames()) {
                dependencyCache.put(key, properties.getProperty(key));
            }
        } catch (Exception e) {
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.WARNING,
                    "Failed to load dependency cache: " + e,
                    "OptimizedDependencyLoader", false);
        }
    }

    private void persistDependencyCache() {
        try (FileOutputStream fos = new FileOutputStream(dependencyCacheFile)) {
            Properties properties = new Properties();
            properties.putAll(dependencyCache);
            properties.store(fos, "Cached dependencies: coordinate -> jar file path");
        } catch (Exception e) {
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.WARNING,
                    "Failed to persist dependency cache: " + e,
                    "OptimizedDependencyLoader", false);
        }
    }

    // --- Retry für fehlgeschlagene Abhängigkeiten ---

    public void reattemptFailedDependencies(boolean appendToParent, boolean logTransitive, boolean logging) {
        Set<String> failedCopy = new HashSet<>(failedDeps);
        for (String coordinate : failedCopy) {
            failedDeps.remove(coordinate);
            loadDependency(coordinate, new String[0], logging, appendToParent, "", logTransitive);
        }
        persistFailedDependencies();
    }

    // --- Annotation-Scan und Laden der Dependencies ---

    /**
     * Scannt je nach Parameter entweder nur die übergebene Klasse oder den eingeschränkten Classpath (über Filter!)
     * nach unseren Annotationen und lädt die angegebenen Abhängigkeiten.
     *
     * @param onlyScanCallingClass falls true, nur die annotations der übergebenen Klasse werden verarbeitet
     * @param targetClass          wenn nicht null, wird diese Klasse gescannt; ansonsten wird der Aufrufer (Stacktrace) verwendet
     * @param logging              ob Logging erfolgen soll
     * @return CompletableFuture, das beendet wird, sobald der Scan abgeschlossen ist
     */
    public CompletableFuture<Void> scanAndLoadDependencies(boolean onlyScanCallingClass, Class<?> targetClass, boolean logging) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Optional: Beschränke den Scan auf bestimmte Paket-Pfade, um Zeit zu sparen.
                ClassGraph classGraph = new ClassGraph()
                        .enableAnnotationInfo()
                        .acceptPackages("de.happybavarian07.adminpanel")  // <-- hier anpassen
                        .ignoreClassVisibility();
                classGraph.addClassLoader(dynamicClassLoader);

                Class<?> effectiveClass = (targetClass != null)
                        ? targetClass
                        : Class.forName(new Throwable().getStackTrace()[0].getClassName());

                if (onlyScanCallingClass) {
                    processDirectAnnotations(effectiveClass, logging);
                } else {
                    try (ScanResult scanResult = classGraph.scan()) {
                        processScanResult(scanResult, logging);
                    }
                }
            } catch (Exception e) {
                handleScanError(e);
            }
        }, dependencyResolverPool);
    }

    private void processDirectAnnotations(Class<?> clazz, boolean logging) {
        for (Annotation annotation : clazz.getAnnotations()) {
            if (annotation instanceof Repository repo) {
                registerRepository(repo.name(), repo.url(), logging);
            } else if (annotation instanceof Repositories repos) {
                for (Repository repo : repos.value()) {
                    registerRepository(repo.name(), repo.url(), logging);
                }
            } else if (annotation instanceof Dependency dep) {
                String coordinate = dep.group() + ":" + dep.artifact() + ":" + dep.version();
                loadDependency(coordinate, dep.exclusions(), logging, dep.appendToParentClassLoader(), "", dep.logTransistive());
            } else if (annotation instanceof Dependencies deps) {
                for (Dependency dep : deps.value()) {
                    String coordinate = dep.group() + ":" + dep.artifact() + ":" + dep.version();
                    loadDependency(coordinate, dep.exclusions(), logging, dep.appendToParentClassLoader(), "", dep.logTransistive());
                }
            } else if (annotation instanceof DependenciesConfig config) {
                for (Repository repo : config.repositories()) {
                    registerRepository(repo.name(), repo.url(), logging);
                }
                for (Dependency dep : config.dependencies()) {
                    String coordinate = dep.group() + ":" + dep.artifact() + ":" + dep.version();
                    loadDependency(coordinate, dep.exclusions(), logging, dep.appendToParentClassLoader(), "", dep.logTransistive());
                }
            }
        }
    }

    private void processScanResult(ScanResult scanResult, boolean logging) {
        // Repositories scannen
        scanResult.getClassesWithAnnotation(Repository.class.getName()).forEach(classInfo -> {
            Repository repo = (Repository) classInfo.getAnnotationInfo(Repository.class.getName()).loadClassAndInstantiate();
            registerRepository(repo.name(), repo.url(), logging);
        });
        scanResult.getClassesWithAnnotation(Repositories.class.getName()).forEach(classInfo -> {
            Repositories repos = (Repositories) classInfo.getAnnotationInfo(Repositories.class.getName()).loadClassAndInstantiate();
            for (Repository repo : repos.value()) {
                registerRepository(repo.name(), repo.url(), logging);
            }
        });
        // Dependencies scannen
        scanResult.getClassesWithAnnotation(Dependency.class.getName()).forEach(classInfo -> {
            Dependency dep = (Dependency) classInfo.getAnnotationInfo(Dependency.class.getName()).loadClassAndInstantiate();
            String coordinate = dep.group() + ":" + dep.artifact() + ":" + dep.version();
            loadDependency(coordinate, dep.exclusions(), logging, dep.appendToParentClassLoader(), "", dep.logTransistive());
        });
        scanResult.getClassesWithAnnotation(Dependencies.class.getName()).forEach(classInfo -> {
            Dependencies deps = (Dependencies) classInfo.getAnnotationInfo(Dependencies.class.getName()).loadClassAndInstantiate();
            for (Dependency dep : deps.value()) {
                String coordinate = dep.group() + ":" + dep.artifact() + ":" + dep.version();
                loadDependency(coordinate, dep.exclusions(), logging, dep.appendToParentClassLoader(), "", dep.logTransistive());
            }
        });
        scanResult.getClassesWithAnnotation(DependenciesConfig.class.getName()).forEach(classInfo -> {
            DependenciesConfig config = (DependenciesConfig) classInfo.getAnnotationInfo(DependenciesConfig.class.getName()).loadClassAndInstantiate();
            for (Repository repo : config.repositories()) {
                registerRepository(repo.name(), repo.url(), logging);
            }
            for (Dependency dep : config.dependencies()) {
                String coordinate = dep.group() + ":" + dep.artifact() + ":" + dep.version();
                loadDependency(coordinate, dep.exclusions(), logging, dep.appendToParentClassLoader(), "", dep.logTransistive());
            }
        });
    }

    private void handleScanError(Exception e) {
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE,
                "Failed to scan for dependencies: " + e,
                "OptimizedDependencyLoader", true);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        logger.coloredMessage(ChatColor.DARK_RED,
                "Failed to scan for dependencies:\n" + sw);
    }

    // --- Dynamic ClassLoader ---

    public static final class DynamicClassLoader extends URLClassLoader {
        static {
            ClassLoader.registerAsParallelCapable();
        }

        private final ClassAppender classAppender = new ClassAppender();

        public DynamicClassLoader(String name, ClassLoader parent) {
            super(name, new URL[0], parent);
        }

        public void add(URL url) {
            addURL(url);
        }

        /**
         * Appendet die JAR-URL zum Parent-ClassLoader.
         */
        public void appendToParent(URL url, ClassLoader parent) {
            try {
                classAppender.append(url, parent);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    // --- Zentrale Dependency-Konfiguration (Beispiel) ---

    private static final class DependencyConfig {
        private final List<RepositoryInfo> repositories = List.of(
                new RepositoryInfo("central", "https://repo.maven.apache.org/maven2/"),
                new RepositoryInfo("jitpack.io", "https://jitpack.io"),
                new RepositoryInfo("sonatype", "https://oss.sonatype.org/content/groups/public/")
        );
        private final Map<String, String> classToDependencyMap = Map.of(
                "io.github.classgraph.ClassGraph", "io.github.classgraph:classgraph:4.8.137",
                "org.apache.commons.lang3.StringUtils", "org.apache.commons:commons-lang3:3.12.0",
                "com.google.gson.Gson", "com.google.code.gson:gson:2.8.9",
                "org.slf4j.Logger", "org.slf4j:slf4j-api:1.7.36"
        );

        public List<RepositoryInfo> getRepositories() {
            return repositories;
        }

        public String mapClassNameToDependency(String className) {
            return classToDependencyMap.get(className);
        }
    }

    private record RepositoryInfo(String name, String url) {
    }

    // Methode zum Herunterladen von JARs über HTTP (Dummy-Implementierung, sollte durch Maven Resolver erweitert werden)
    // Hier bleibt die Grundidee erhalten, aber ggf. kann hier noch ein Retry-Mechanismus mit Backoff implementiert werden.
    public File downloadJar(String jarUrlString, boolean logging) throws IOException {
        final int maxRetries = 3;       // Maximale Anzahl der Versuche
        final long baseDelay = 1000L;   // Basisverzögerung in Millisekunden (z. B. 1 Sekunde)
        IOException lastException = null;

        // URL-Objekt erzeugen
        URL jarUrl = new URL(jarUrlString);

        // Zielverzeichnis "dynamic_deps" unter dem Plugin-DataFolder
        File targetDir = new File(AdminPanelMain.getPlugin().getDataFolder(), "dynamic_deps");
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            if (logging) {
                AdminPanelMain.getPlugin().getStartUpLogger().coloredMessage(
                        ChatColor.DARK_RED, "Could not create directory: " + targetDir.getAbsolutePath());
            }
            throw new IOException("Failed to create directory: " + targetDir.getAbsolutePath());
        }

        // Erzeuge einen Dateinamen basierend auf der URL (alles nach dem letzten "/")
        String fileName = jarUrlString.substring(jarUrlString.lastIndexOf("/") + 1);
        File targetFile = new File(targetDir, fileName);

        // Retry-Loop
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                if (logging) {
                    AdminPanelMain.getPlugin().getStartUpLogger().coloredMessage(
                            ChatColor.GREEN, "Attempt " + attempt + " downloading " + jarUrlString);
                }

                // Öffne den Stream und kopiere den Inhalt in die Zieldatei
                try (InputStream in = jarUrl.openStream()) {
                    Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                if (logging) {
                    AdminPanelMain.getPlugin().getStartUpLogger().coloredMessage(
                            ChatColor.GREEN, "Download complete: " + targetFile.getAbsolutePath());
                }
                return targetFile;
            } catch (IOException e) {
                lastException = e;
                if (logging) {
                    AdminPanelMain.getPlugin().getStartUpLogger().coloredMessage(
                            ChatColor.DARK_RED, "Attempt " + attempt + " failed: " + e.getMessage());
                }
                // Exponentielles Backoff: warte attempt * baseDelay Millisekunden
                try {
                    Thread.sleep(attempt * baseDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Download interrupted", ie);
                }
            }
        }
        throw new IOException("Failed to download jar from " + jarUrlString + " after " + maxRetries + " attempts.", lastException);
    }

    // Shutdown-Methode, um den Threadpool sauber zu beenden.
    public void shutdown() {
        dependencyResolverPool.shutdown();
    }
}
