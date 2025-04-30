package de.happybavarian07.adminpanel.utils.dependencyloading.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation zur Konfiguration von Repositories und Dependencies.
 * <p>
 * Diese Annotation ermöglicht es, mehrere Repositories und Dependencies
 * an einer Klassen-Definition zu konfigurieren.
 *
 * <p><b>Beispiel:</b></p>
 * <pre>
 * {@code
 * @DependenciesConfig(
 *     repositories = {
 *         @Repository(name = "ExampleRepo", url = "https://example.com/repo")
 *     },
 *     dependencies = {
 *         @Dependency(
 *             group = "com.example",
 *             artifact = "example-library",
 *             version = "1.0.0"
 *         )
 *     }
 * )
 * public class ExampleClass {
 *     // Klassenimplementierung
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // Used at class level
public @interface DependenciesConfig {
    Repository[] repositories() default {};

    Dependency[] dependencies() default {};
}
