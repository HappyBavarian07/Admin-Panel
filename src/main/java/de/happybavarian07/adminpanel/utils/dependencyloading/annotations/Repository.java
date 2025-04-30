package de.happybavarian07.adminpanel.utils.dependencyloading.annotations;

import java.lang.annotation.*;

/**
 * Definiert ein Repository, von dem Abhängigkeiten bezogen werden können.
 *
 * <p><b>Beispiel:</b></p>
 * <pre>
 * {@code
 * @Repository(name = "ExampleRepo", url = "https://example.com/repo")
 * public class ExampleClass {
 *     // Klassenimplementierung
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // Used at class level
@Repeatable(Repositories.class) // Supports multiple @Repository annotations
public @interface Repository {
    String name();

    String url();
}

