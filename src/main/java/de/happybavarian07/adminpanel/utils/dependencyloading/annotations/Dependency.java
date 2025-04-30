package de.happybavarian07.adminpanel.utils.dependencyloading.annotations;

import java.lang.annotation.*;

/**
 * <p>
 * Annotation for defining a dependency.
 * </p>
 * <p>
 * This annotation is used to specify dependency information such as group ID, artifact ID, and version
 * for automatic dependency management. It supports additional options like exclusions, repository details,
 * and whether the dependency is optional or an addon dependency.
 * </p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>
 * {@code
 * @Dependency(
 *     group = "com.example",
 *     artifact = "example-library",
 *     version = "1.0.0",
 *     exclusions = {"excluded-artifact"},
 *     repository = @Repository(name = "ExampleRepo", url = "https://example.com/repo"),
 *     appendToParentClassLoader = true,
 *     optional = false,
 *     addonDependency = true,
 *     logTransistive = true
 * )
 * public class ExampleClass {
 *     // Class implementation
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // Used at class level
@Repeatable(Dependencies.class) // Supports multiple @Dependency annotations
public @interface Dependency {
    /**
     * <p>Specifies the group ID of the dependency.</p>
     *
     * @return the group ID
     */
    String group();

    /**
     * <p>Specifies the artifact ID of the dependency.</p>
     *
     * @return the artifact ID
     */
    String artifact();

    /**
     * <p>Specifies the version number of the dependency.</p>
     *
     * @return the version number
     */
    String version();

    /**
     * <p>Specifies an array of artifact IDs to be excluded from the dependency.</p>
     *
     * @return an array of exclusions
     */
    String[] exclusions() default {};

    /**
     * <p>Specifies the repository where the dependency can be found.</p>
     *
     * @return the repository
     */
    Repository repository() default @Repository(name = "", url = "");

    /**
     * <p>Determines whether the dependency should be added to the parent ClassLoader.</p>
     *
     * @return true if the dependency should be appended to the parent ClassLoader, otherwise false
     */
    boolean appendToParentClassLoader() default false;

    /**
     * <p>Specifies whether the dependency is optional.</p>
     *
     * @return true if the dependency is optional, otherwise false
     */
    boolean optional() default false;

    /**
     * <p>Specifies whether the dependency is an addon dependency.</p>
     *
     * @return true if it is an addon dependency, otherwise false
     */
    boolean addonDependency() default false;

    /**
     * <p>Determines whether transitive dependencies should be logged.</p>
     *
     * @return true if transitive dependencies should be logged, otherwise false
     */
    boolean logTransistive() default false;
}
