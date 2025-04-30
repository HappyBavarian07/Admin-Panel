package de.happybavarian07.adminpanel.utils.dependencyloading.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wrapper-Annotation für mehrere {@link Repository}-Annotationen.
 * <p>
 * Diese Annotation ermöglicht es, mehrere Repository-Angaben an einer Klasse zu definieren.
 *
 * <p><b>Beispiel:</b></p>
 * <pre>
 * {@code
 * @Repositories(
 * {
 *     @Repository(name = "Repo1", url = "https://repo1.example.com"),
 *     @Repository(name = "Repo2", url = "https://repo2.example.com")
 * })
 * public class ExampleClass {
 *     // Klassenimplementierung
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Repositories {
    Repository[] value();
}
